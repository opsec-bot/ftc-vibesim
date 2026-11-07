package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Simulated FTC LinearOpMode. Same lifecycle as the real one: the engine injects
 * hardwareMap + telemetry, starts your opmode on its own thread, releases waitForStart()
 * when the match "starts", and keeps opModeIsActive() true until the opmode ends.
 */
public abstract class LinearOpMode {

    public HardwareMap hardwareMap;
    public Telemetry telemetry;

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
    public boolean opModeInInit()  { return !started && !stopRequested; }
    public boolean isStarted()     { return started; }
    public boolean isStopRequested() { return stopRequested; }

    public void idle() {
        try { Thread.sleep(1); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public void sleep(long milliseconds) {
        try { Thread.sleep(milliseconds); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ===================== SIM ONLY =====================
    public void simStart()       { started = true; }
    public void simRequestStop() { stopRequested = true; }
}
