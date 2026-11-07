package com.ftcsim.engine;

import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * A two-motor differential (tank) drive. Each engine tick, both motors advance their
 * encoders; the wheel tick-deltas become wheel travel in cm, which become a change in
 * robot pose via standard differential-drive kinematics.
 *
 * Field frame: x to the right, y up, theta measured CCW from +x. Units: cm and radians.
 */
public class SimRobot {

    public final RobotConfig cfg;
    public final DcMotor left;
    public final DcMotor right;

    public volatile double x = 0.0;
    public volatile double y = 0.0;
    public volatile double thetaRad = 0.0;

    public SimRobot(RobotConfig cfg, DcMotor left, DcMotor right) {
        this.cfg = cfg;
        this.left = left;
        this.right = right;
        // Left motor is mounted mirrored relative to the right; teams encode this with
        // setDirection. The sim tracks the physical mounting separately so setDirection
        // behaves faithfully.
        left.mountSign  = -1;
        right.mountSign = +1;
        left.maxTicksPerSec  = cfg.maxTicksPerSec;
        right.maxTicksPerSec = cfg.maxTicksPerSec;
    }

    public void step(double dt) {
        double fwdTicksL = left.simStep(dt);
        double fwdTicksR = right.simStep(dt);

        double tpc = cfg.ticksPerCm();
        double dL = fwdTicksL / tpc;   // left wheel forward travel (cm)
        double dR = fwdTicksR / tpc;   // right wheel forward travel (cm)

        double dCenter = (dL + dR) / 2.0;
        double dTheta  = (dR - dL) / cfg.trackWidthCm;

        // integrate at the midpoint heading for a bit more accuracy on arcs
        double midTheta = thetaRad + dTheta / 2.0;
        x += dCenter * Math.cos(midTheta);
        y += dCenter * Math.sin(midTheta);
        thetaRad += dTheta;
    }

    public double headingDeg() { return Math.toDegrees(thetaRad); }
}
