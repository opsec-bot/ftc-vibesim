package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Desktop mirror of the FTC SDK OpMode base class: the fields and lifecycle methods opmodes
 * and the runtime use. The sim injects hardwareMap / telemetry / gamepad1 / gamepad2.
 *
 * Coverage note: the real OpMode extends an internal OpModeInternal and has msStuckDetect*,
 * blackboard, and internal* methods tied to the Android runtime; not mirrored.
 */
public abstract class OpMode {

    public HardwareMap hardwareMap;
    public Telemetry telemetry;
    public Gamepad gamepad1;
    public Gamepad gamepad2;

    public volatile double time = 0.0;

    private long startTimeNanos = System.nanoTime();

    public abstract void init();
    public void init_loop() {}
    public void start() {}
    public abstract void loop();
    public void stop() {}

    public double getRuntime() { return (System.nanoTime() - startTimeNanos) / 1.0e9; }
    public void resetRuntime() { startTimeNanos = System.nanoTime(); }

    public void updateTelemetry(Telemetry telemetry) { telemetry.update(); }
}
