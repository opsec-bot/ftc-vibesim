# Coverage / gaps

What the mirror + sim cover today, and what's missing. Regenerate the member-level list with:

```bash
py scripts/check_sdk_signatures.py --gaps     # python3 on Linux/CI
```

As of SDK 12.0.0: **0 drift, 121 member gaps**, plus the whole-class gaps below.

---

## A. Member gaps inside classes we already mirror (121)

These classes exist and match 1:1 for what we provide; these specific members are not mirrored yet.

**DcMotor (3)** — device-driver internals, rarely called in opmodes:
- `getController()`, `getMotorType()`, `setMotorType()`

**DcMotorEx** — none (fully mirrored).

**Gamepad (94)** — the big one. Mostly:
- edge helpers: `aWasPressed()` / `aWasReleased()` and the same for b, x, y, dpad_*, bumpers, stick buttons, triggers, start/back/guide, circle/cross/triangle/square/share/options/touchpad/ps (~56 methods)
- rumble + LED: `rumble()`, `rumbleBlips()`, `stopRumble()`, `isRumbling()`, `runRumbleEffect()`, `runLedEffect()`, `setLedColor()`
- touchpad fields: `touchpad_finger_1/2`, `touchpad_finger_1_x/y`, etc.
- plumbing: `type`, `id`, `timestamp`, `getGamepadId()`, `toByteArray()`, `copy()`, `reset()`, `resetEdgeDetection()`, `getUser()`

**OpMode (10)** — Android/runtime internals, opmodes don't call these:
- `msStuckDetectInit/InitLoop/Start/Loop`, `blackboard`, `internalPreInit()`, `internalPostInitLoop()`, `internalPostLoop()`, `internalUpdateTelemetryNow()`, `terminateOpModeNow()`

**LinearOpMode (3)** — `init_loop()`, `start()`, `stop()` final re-overrides. We inherit OpMode's; harmless, behavior identical.

**AngleUnit (11)** — `float` overloads of every method, plus `bVal`, `Pif`, `getUnnormalized()`. The `double` versions are mirrored.

---

## B. Whole SDK classes NOT mirrored at all (bigger functional gaps)

These block real opmodes from compiling in the sim until added.

**Timing / util (P0 — extremely common):**
- `com.qualcomm.robotcore.util.ElapsedTime` — timers, used in almost every auto/teleop
- `com.qualcomm.robotcore.util.Range` — `Range.clip()`, `Range.scale()`

**IMU / heading (P0):**
- `com.qualcomm.robotcore.hardware.IMU`
- navigation geometry: `YawPitchRollAngles`, `Orientation`, `AngularVelocity`, `Quaternion`, `RevHubOrientationOnRobot`, `UnnormalizedAngleUnit`

**Sensors (P1):**
- `ColorSensor`, `NormalizedColorSensor`, `DistanceSensor`, `TouchSensor`, `VoltageSensor`, `AnalogInput`, `DigitalChannel`
- HardwareMap typed mappings for them (`colorSensor`, `touchSensor`, `voltageSensor`, ... ~20 fields we dropped)

**Controllers (P2):**
- `DcMotorController` (so `DcMotor.getController()` can return real), full `ServoController` behavior
- `MotorConfigurationType` (so `getMotorType()` works)

**Vision (P2 — big subsystem):**
- `VisionPortal`, `AprilTagProcessor` + `AprilTagDetection`, TensorFlow — the whole `org.firstinspires.ftc.vision` package (separate `Vision` artifact)

**Misc devices (P3):**
- `RevBlinkinLedDriver`, `LED`, `IrSeekerSensor`, legacy sensors

---

## C. Simulation / physics gaps (not SDK-API — the engine itself)

Even where the API exists, the sim doesn't model the physics yet.

- **Servos / CRServo don't move anything.** `SimServo` stores a position; no arm/lift/mechanism is simulated.
- **No IMU data source.** The engine tracks `thetaRad` already, so feeding a Sim IMU is easy once the interface exists.
- **Sensors return fake/empty data.**
- **Drive is diff/tank only.** No mecanum / holonomic / strafe.
- **No field elements or collisions.** dyn4j is on the classpath but unused.
- **Motor model is simplified** — moves toward target at max rate; no acceleration, stall, PID response, or battery sag.
- **No live gamepad input.** Teleop opmodes run but gamepads stay at rest; need a keyboard/controller input source.
- **Single hardcoded robot.** One `RobotConfig`, motor names fixed to `SWmotorL` / `SWmotorR` in `SimMain`.

---

## Suggested next order

1. **ElapsedTime + Range** (tiny, unblocks tons of opmodes)
2. **IMU + gyro turns** (interface + feed `thetaRad`) — biggest behavior win
3. **Gamepad edge helpers** + a **live input source** (keyboard) for teleop
4. **Servo/arm motion** in the physics model
5. **Basic sensors** (distance/touch/voltage/color) returning sim values
6. **Mecanum drive**
7. Vision (AprilTag) — large, do last
