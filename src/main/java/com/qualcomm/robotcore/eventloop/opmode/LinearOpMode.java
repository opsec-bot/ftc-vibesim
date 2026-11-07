package com.qualcomm.robotcore.eventloop.opmode;

/**
 * Desktop mirror of the FTC SDK LinearOpMode. Same lifecycle: the engine injects
 * hardwareMap/telemetry/gamepads (on OpMode), starts runOpMode() on its own thread,
 * releases waitForStart() when the match starts, and keeps opModeIsActive() true until stop.
 */
public abstract class LinearOpMode extends OpMode {

    private volatile boolean started = false;
    private volatile boolean stopRequested = false;

    public abstract void runOpMode() throws InterruptedException;

    public void waitForStart() {
        while (!started && !stopRequested) {
            try { Thread.sleep(5); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
        }
    }

    public boolean opModeIsActive() { return started && !stopRequested; }
    public boolean opModeInInit()   { return !started && !stopRequested; }
    public boolean isStarted()      { return started; }
    public boolean isStopRequested() { return stopRequested; }

    public void idle() {
        try { Thread.sleep(1); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void sleep(long milliseconds) {
        try { Thread.sleep(milliseconds); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // OpMode's abstract hooks are driven by runOpMode() in a LinearOpMode; make them final no-ops.
    @Override public final void init() {}
    @Override public final void loop() {}

    // ===================== SIM ONLY =====================
    public void simStart()       { started = true; }
    public void simRequestStop() { stopRequested = true; }
}
