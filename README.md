# FTC VibeSim

A tiny local FTC simulator for vibecoding bots. Write an opmode against the real FTC SDK
API, run it on your laptop, watch the robot drive on a field, no Control Hub required.

The FTC SDK types live in their real packages (`com.qualcomm.robotcore.*`,
`org.firstinspires.ftc.robotcore.external.*`), so an opmode that runs here is byte-identical
to the one you deploy. No `#ifdef SIM`, no second copy of your code.

## Requirements

Nothing to install. JDK 21 and the bundled Gradle wrapper do everything.

## Run

```bash
./gradlew run        # GUI window, real time — watch the robot move
./gradlew runSim     # headless + fast — prints the trajectory and final pose (good for quick checks)
```

Pick a different opmode or change the pass/fail check:

```bash
./gradlew runSim -Popmode=org.firstinspires.ftc.teamcode.Spin_Turn_90_Buggy -Pexpect=-90
./gradlew runSim -Psnapshot=build/out.png        # also write a PNG of the final pose
```

## What's in here

```
src/main/java/
  com/qualcomm/robotcore/...        fake FTC SDK (DcMotor, HardwareMap, LinearOpMode, @TeleOp)
  org/firstinspires/ftc/robotcore/  fake Telemetry
  com/ftcsim/engine/                the sim: RobotConfig, SimRobot (diff-drive physics),
                                    SwingField (field view), SimMain (launcher)
  org/firstinspires/ftc/teamcode/   your opmodes (Spin_Turn_90_Degrees, _Buggy)
```

## How the physics works

`RobotConfig` holds the TRUE robot (REV-45-2041: HD Hex 28 cpr through UltraPlanetary
20:1 nominal = 18.9:1 actual -> ~529 ticks/rev, 9 cm wheel, 30 cm track width). The engine
turns each motor's encoder ticks into wheel travel, then into pose with standard
differential-drive kinematics.

Your opmode computes its OWN target ticks from its OWN idea of the geometry. If that math is
right, the robot lands where you intended. If it has a bug, the sim shows the wrong motion.
That mismatch is the point: `Spin_Turn_90_Buggy` ends at 0 degrees (integer `90/360 = 0`
zeroes the turn); the fixed version ends at exactly -90 degrees.

## v0 scope / next

- v0: two-motor tank/diff drive, RUN_TO_POSITION + RUN_USING_ENCODER, exact kinematics,
  Swing field view, PNG snapshot.
- Next: dyn4j physics world (already on the classpath) for field walls + collisions;
  mecanum/omni drive; FTC Dashboard browser view; more sensors (IMU, distance).

## Add your own opmode

1. Drop a `LinearOpMode` in `org/firstinspires/ftc/teamcode/`.
2. Make sure the motor names it looks up match the ones registered in `SimMain`
   (currently `SWmotorL`, `SWmotorR`).
3. `./gradlew runSim -Popmode=org.firstinspires.ftc.teamcode.YourOpMode`
