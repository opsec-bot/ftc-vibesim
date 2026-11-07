package com.ftcsim.sim;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

/**
 * Simulated motor implementing the real DcMotorEx interface. Holds the encoder state the
 * engine steps each tick and exposes the forward-tick delta (corrected for direction and
 * mounting side) via simStep().
 */
public class SimDcMotor implements DcMotorEx {

    private final String name;

    private volatile Direction direction = Direction.FORWARD;
    private volatile RunMode mode = RunMode.RUN_WITHOUT_ENCODER;
    private volatile ZeroPowerBehavior zeroPower = ZeroPowerBehavior.BRAKE;
    private volatile double power = 0.0;
    private volatile int targetPosition = 0;
    private volatile double reportedTicks = 0.0;
    private volatile boolean enabled = true;
    private volatile int targetTolerance = 5;

    private volatile boolean velocityCommanded = false;
    private volatile double commandedVelTicksPerSec = 0.0;
    private volatile double lastVelTicksPerSec = 0.0;

    // SIM ONLY: set by the engine from RobotConfig
    public volatile int mountSign = 1;
    public volatile double maxTicksPerSec = 2800.0;

    public SimDcMotor(String name) { this.name = name; }

    // ---- DcMotorSimple ----
    @Override public void setDirection(Direction d) { this.direction = d; }
    @Override public Direction getDirection() { return direction; }
    @Override public void setPower(double p) { this.power = p; this.velocityCommanded = false; }
    @Override public double getPower() { return power; }

    // ---- DcMotor ----
    @Override public int getPortNumber() { return 0; }
    @Override public void setZeroPowerBehavior(ZeroPowerBehavior z) { this.zeroPower = z; }
    @Override public ZeroPowerBehavior getZeroPowerBehavior() { return zeroPower; }
    @Override public void setPowerFloat() { this.power = 0.0; this.zeroPower = ZeroPowerBehavior.FLOAT; }
    @Override public boolean getPowerFloat() { return zeroPower == ZeroPowerBehavior.FLOAT && power == 0.0; }
    @Override public void setTargetPosition(int position) { this.targetPosition = position; }
    @Override public int getTargetPosition() { return targetPosition; }
    @Override public boolean isBusy() {
        return mode == RunMode.RUN_TO_POSITION
            && Math.abs(targetPosition - reportedTicks) > targetTolerance
            && Math.abs(power) > 1e-6;
    }
    @Override public int getCurrentPosition() { return (int) Math.round(reportedTicks); }
    @Override public void setMode(RunMode m) {
        this.mode = m;
        if (m == RunMode.STOP_AND_RESET_ENCODER) { reportedTicks = 0.0; power = 0.0; lastVelTicksPerSec = 0.0; }
    }
    @Override public RunMode getMode() { return mode; }

    // ---- DcMotorEx ----
    @Override public void setMotorEnable() { enabled = true; }
    @Override public void setMotorDisable() { enabled = false; }
    @Override public boolean isMotorEnabled() { return enabled; }
    @Override public void setVelocity(double ticksPerSecond) { this.commandedVelTicksPerSec = ticksPerSecond; this.velocityCommanded = true; }
    @Override public void setVelocity(double angularRate, AngleUnit unit) { setVelocity(angularRate); }
    @Override public double getVelocity() { return lastVelTicksPerSec; }
    @Override public double getVelocity(AngleUnit unit) { return unit.fromDegrees(lastVelTicksPerSec); }
    @Override public void setPIDCoefficients(RunMode mode, PIDCoefficients pidCoefficients) {}
    @Override public void setPIDFCoefficients(RunMode mode, PIDFCoefficients pidfCoefficients) {}
    @Override public void setVelocityPIDFCoefficients(double p, double i, double d, double f) {}
    @Override public void setPositionPIDFCoefficients(double p) {}
    @Override public PIDCoefficients getPIDCoefficients(RunMode mode) { return new PIDCoefficients(); }
    @Override public PIDFCoefficients getPIDFCoefficients(RunMode mode) { return new PIDFCoefficients(); }
    @Override public void setTargetPositionTolerance(int tolerance) { this.targetTolerance = tolerance; }
    @Override public int getTargetPositionTolerance() { return targetTolerance; }
    @Override public double getCurrent(CurrentUnit unit) { return unit.toAmps(Math.abs(power) * 2.0); }
    @Override public double getCurrentAlert(CurrentUnit unit) { return unit.toAmps(9.2); }
    @Override public void setCurrentAlert(double current, CurrentUnit unit) {}
    @Override public boolean isOverCurrent() { return false; }

    // ---- HardwareDevice ----
    @Override public Manufacturer getManufacturer() { return Manufacturer.Lynx; }
    @Override public String getDeviceName() { return "Sim DC Motor"; }
    @Override public String getConnectionInfo() { return "sim; motor " + name; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}

    // ===================== SIM engine hook =====================
    public String getName() { return name; }

    /** Advance the encoder by dt seconds; return wheel-forward tick delta (dir + mount corrected). */
    public double simStep(double dt) {
        if (!enabled || dt <= 0) { lastVelTicksPerSec = 0; return 0; }
        double before = reportedTicks;
        switch (mode) {
            case RUN_TO_POSITION: {
                double maxStep = maxTicksPerSec * Math.abs(power) * dt;
                double err = targetPosition - reportedTicks;
                reportedTicks += Math.max(-maxStep, Math.min(maxStep, err));
                break;
            }
            case RUN_USING_ENCODER: {
                double rate = velocityCommanded ? commandedVelTicksPerSec : power * maxTicksPerSec;
                reportedTicks += rate * dt;
                break;
            }
            case RUN_WITHOUT_ENCODER:
                reportedTicks += power * maxTicksPerSec * dt;
                break;
            case STOP_AND_RESET_ENCODER:
            default:
                break;
        }
        double delta = reportedTicks - before;
        lastVelTicksPerSec = delta / dt;
        double dirSign = (direction == Direction.REVERSE) ? -1.0 : 1.0;
        return delta * dirSign * mountSign;
    }
}
