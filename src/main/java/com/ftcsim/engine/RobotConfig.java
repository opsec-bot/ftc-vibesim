package com.ftcsim.engine;

/**
 * The TRUE physical robot, used by the engine to turn encoder ticks into real motion.
 * Your opmode encodes its own idea of the geometry to compute target ticks; the sim
 * decodes those ticks with the numbers here. If your opmode's math is right, the robot
 * ends where you intended. If it has a bug (e.g. integer division zeroing the turn),
 * the sim shows the robot doing the wrong thing. That mismatch is the whole point.
 *
 * Numbers below are for the REV-45-2041 drivetrain: HD Hex motor (28 counts/rev) through
 * an UltraPlanetary 20:1 nominal gearbox, whose ACTUAL ratio is 18.9:1.
 */
public class RobotConfig {

    /** Encoder counts per output-shaft revolution: 28 * 18.9 (actual, not nominal 20:1). */
    public double ticksPerRev = 28 * 18.9;      // ~= 529

    public double wheelDiameterCm = 9.0;         // measure outer diameter of your wheel
    public double trackWidthCm    = 30.0;        // distance between left & right wheel centers

    /** Encoder speed at full power. HD Hex ~6000 rpm / 18.9 = ~317 rpm output -> ~2800 ticks/s. */
    public double maxTicksPerSec  = 2800.0;

    /** Robot body size for drawing (cm). 18 in = ~45.7 cm max legal; use your real footprint. */
    public double bodyLengthCm = 40.0;
    public double bodyWidthCm  = 40.0;

    public double ticksPerCm() { return ticksPerRev / (Math.PI * wheelDiameterCm); }
}
