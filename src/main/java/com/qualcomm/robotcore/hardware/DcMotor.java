package com.qualcomm.robotcore.hardware;

/**
 * Simulated stand-in for the FTC SDK's DcMotor. The public API (setMode, setPower,
 * setTargetPosition, getCurrentPosition, isBusy, setDirection, the Direction and
 * RunMode enums) matches the real SDK, so an opmode written against this compiles and
 * runs unchanged on a real Control Hub.
 *
 * The fields/methods prefixed with "sim" or marked SIM ONLY are extra hooks the desktop
 * engine uses to step the encoder and read wheel motion. The real SDK does not have them;
 * your opmode never calls them, so portability is unaffected.
 */
public class DcMotor {

    public enum Direction { FORWARD, REVERSE }

    public enum RunMode {
        RUN_WITHOUT_ENCODER,
        RUN_USING_ENCODER,
        RUN_TO_POSITION,
        STOP_AND_RESET_ENCODER
    }

    private final String name;

    // --- real SDK state (volatile: engine thread writes, opmode thread reads) ---
    private volatile Direction direction = Direction.FORWARD;
    private volatile RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private volatile double power = 0.0;
    private volatile int targetPosition = 0;
    private volatile double reportedTicks = 0.0; // what getCurrentPosition() returns

    // --- SIM ONLY: set by the engine from RobotConfig ---
    public volatile int mountSign = 1;             // +1 right side, -1 left side (mirrored)
    public volatile double maxTicksPerSec = 2800.0; // encoder speed at full power
    private static final double POS_TOLERANCE = 5.0;

    public DcMotor(String name) { this.name = name; }

    public String getName() { return name; }

    public void setDirection(Direction d) { this.direction = d; }
    public Direction getDirection() { return direction; }

    public void setMode(RunMode m) {
        this.mode = m;
        if (m == RunMode.STOP_AND_RESET_ENCODER) {
            reportedTicks = 0.0;
            power = 0.0;
        }
    }
    public RunMode getMode() { return mode; }

    public void setPower(double p) { this.power = p; }
    public double getPower() { return power; }

    public void setTargetPosition(int t) { this.targetPosition = t; }
    public int getTargetPosition() { return targetPosition; }

    public int getCurrentPosition() { return (int) Math.round(reportedTicks); }

    public boolean isBusy() {
        return mode == RunMode.RUN_TO_POSITION
            && Math.abs(targetPosition - reportedTicks) > POS_TOLERANCE
            && Math.abs(power) > 1e-6;
    }

    // ===================== SIM ONLY below this line =====================

    /**
     * Advance this motor's encoder by {@code dt} seconds and return the wheel's
     * forward tick delta in the robot frame (already corrected for setDirection and
     * for which side of the robot the motor is mounted on).
     */
    public double simStep(double dt) {
        double before = reportedTicks;
        switch (mode) {
            case RUN_TO_POSITION -> {
                double maxStep = maxTicksPerSec * Math.abs(power) * dt;
                double err = targetPosition - reportedTicks;
                double stepTicks = Math.max(-maxStep, Math.min(maxStep, err));
                reportedTicks = reportedTicks + stepTicks;
            }
            case RUN_USING_ENCODER, RUN_WITHOUT_ENCODER ->
                reportedTicks = reportedTicks + maxTicksPerSec * power * dt;
            case STOP_AND_RESET_ENCODER -> { /* holds at 0 */ }
        }
        double reportedDelta = reportedTicks - before;
        double dirSign = (direction == Direction.REVERSE) ? -1.0 : 1.0;
        // wheel-forward contribution = reported motion * direction convention * mounting side
        return reportedDelta * dirSign * mountSign;
    }
}
