#!/usr/bin/env python3
"""
1:1 signature checker.

Compares our mirror of the FTC SDK against the REAL SDK jars (pulled from Maven Central)
using `javap`. For every class we claim to mirror, it reports:

  DRIFT  = we declare a member with the SAME NAME as the real SDK but a DIFFERENT signature.
           This is a 1:1 violation and fails the check (exit 1).
  GAP    = a real public member we have not mirrored yet. Informational only.
  (extra sim-only members we add are ignored.)

Usage:  python3 scripts/check_sdk_signatures.py
Needs:  javap on PATH (JDK), internet (first run, to download the SDK jars).
"""
import os, re, sys, subprocess, urllib.request, zipfile, io

SDK_VERSION = "12.0.0"
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SDKREF = os.path.join(ROOT, ".sdkref")
OURS = os.path.join(ROOT, "build", "classes", "java", "main")
MODULES = ["RobotCore", "Hardware"]
BASE = "https://repo1.maven.org/maven2/org/firstinspires/ftc"

# Classes we claim to mirror (public API surface opmodes use).
CLASSES = [
    "com.qualcomm.robotcore.hardware.HardwareDevice",
    "com.qualcomm.robotcore.hardware.HardwareDevice$Manufacturer",
    "com.qualcomm.robotcore.hardware.DcMotorSimple",
    "com.qualcomm.robotcore.hardware.DcMotorSimple$Direction",
    "com.qualcomm.robotcore.hardware.DcMotor",
    "com.qualcomm.robotcore.hardware.DcMotor$RunMode",
    "com.qualcomm.robotcore.hardware.DcMotor$ZeroPowerBehavior",
    "com.qualcomm.robotcore.hardware.DcMotorEx",
    "com.qualcomm.robotcore.hardware.Servo",
    "com.qualcomm.robotcore.hardware.Servo$Direction",
    "com.qualcomm.robotcore.hardware.CRServo",
    "com.qualcomm.robotcore.hardware.ServoController",
    "com.qualcomm.robotcore.hardware.PIDCoefficients",
    "com.qualcomm.robotcore.hardware.PIDFCoefficients",
    "com.qualcomm.robotcore.hardware.MotorControlAlgorithm",
    "com.qualcomm.robotcore.hardware.Gamepad",
    "com.qualcomm.robotcore.eventloop.opmode.OpMode",
    "com.qualcomm.robotcore.eventloop.opmode.LinearOpMode",
    "com.qualcomm.robotcore.eventloop.opmode.TeleOp",
    "com.qualcomm.robotcore.eventloop.opmode.Autonomous",
    "com.qualcomm.robotcore.eventloop.opmode.Disabled",
    "org.firstinspires.ftc.robotcore.external.Telemetry",
    "org.firstinspires.ftc.robotcore.external.Telemetry$Item",
    "org.firstinspires.ftc.robotcore.external.Telemetry$Line",
    "org.firstinspires.ftc.robotcore.external.Telemetry$Log",
    "org.firstinspires.ftc.robotcore.external.navigation.AngleUnit",
    "org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit",
]

SHOW_GAPS = "--gaps" in sys.argv

MODIFIERS = {"public", "abstract", "final", "static", "default", "volatile",
             "transient", "synchronized", "native", "strictfp"}


def ensure_jars():
    jars = []
    for m in MODULES:
        jar = os.path.join(SDKREF, m, "classes.jar")
        if not os.path.exists(jar):
            url = f"{BASE}/{m}/{SDK_VERSION}/{m}-{SDK_VERSION}.aar"
            print(f"  downloading {m}-{SDK_VERSION}.aar ...")
            data = urllib.request.urlopen(url).read()
            with zipfile.ZipFile(io.BytesIO(data)) as z:
                os.makedirs(os.path.join(SDKREF, m), exist_ok=True)
                with z.open("classes.jar") as src, open(jar, "wb") as dst:
                    dst.write(src.read())
        jars.append(jar)
    return os.pathsep.join(jars)


def javap(classpath, fqcn):
    r = subprocess.run(["javap", "-classpath", classpath, fqcn],
                       capture_output=True, text=True)
    return r.stdout if r.returncode == 0 else None


def normalize(line):
    line = line.strip().rstrip(";")
    if " throws " in line:
        line = line.split(" throws ")[0]
    toks = line.split()
    toks = [t for t in toks if t not in MODIFIERS]
    sig = " ".join(toks)
    return re.sub(r"\s+", " ", sig).strip()


def member_name(sig):
    m = re.search(r"(\w+)\s*\(", sig)      # method/constructor
    if m:
        return m.group(1)
    return sig.split()[-1] if sig.split() else sig  # field / enum constant


def public_sigs(javap_out):
    sigs = set()
    for ln in javap_out.splitlines():
        s = ln.strip()
        if not s.startswith("public "):
            continue
        if "{" in s or "}" in s:      # class declaration / static initializer
            continue
        sigs.add(normalize(s))
    sigs.discard("")
    return sigs


def main():
    print(f"=== FTC SDK 1:1 signature check (SDK {SDK_VERSION}) ===")
    real_cp = ensure_jars()
    if not os.path.isdir(OURS):
        print(f"ERROR: compiled classes not found at {OURS}. Run ./gradlew compileJava first.")
        return 2

    total_drift, total_gap, checked = 0, 0, 0
    for fqcn in CLASSES:
        real = javap(real_cp, fqcn)
        ours = javap(OURS, fqcn)
        if real is None:
            print(f"[SKIP] {fqcn}: not in real SDK jar")
            continue
        if ours is None:
            print(f"[MISS] {fqcn}: we do not provide this class")
            total_drift += 1
            continue
        checked += 1
        real_sigs = public_sigs(real)
        our_sigs = public_sigs(ours)
        real_names = {member_name(s) for s in real_sigs}

        # DRIFT = a signature WE declare, for a member name the real SDK also has, that the
        # real SDK does NOT have (an overload/return/param we got wrong). An opmode using it
        # would compile here but fail on a real robot.
        drift = sorted(s for s in our_sigs
                       if s not in real_sigs and member_name(s) in real_names)
        # GAP = real public members we have not mirrored yet (missing overloads/methods).
        gap_sigs = sorted(real_sigs - our_sigs)
        gap = len(gap_sigs)
        total_gap += gap
        if drift:
            total_drift += len(drift)
            print(f"[DRIFT] {fqcn}  ({len(drift)} wrong, {gap} gaps)")
            for d in drift:
                print(f"         ours: {d}  <-- not in real SDK")
        else:
            print(f"[OK]    {fqcn}  ({gap} gaps)")
        if SHOW_GAPS and gap_sigs:
            for g in gap_sigs:
                print(f"         gap: {g}")

    print(f"\nChecked {checked} classes.  Drift (mismatched signatures): {total_drift}.  "
          f"Coverage gaps (not yet mirrored): {total_gap}.")
    if total_drift:
        print("FAIL: our mirror has signatures that disagree with the real SDK.")
        return 1
    print("PASS: every mirrored member matches the real SDK 1:1.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
