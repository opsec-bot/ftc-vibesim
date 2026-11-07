package org.firstinspires.ftc.robotcore.external.navigation;

/**
 * Mirror of the FTC SDK AngleUnit (commonly used conversion methods).
 * Coverage note: the real enum also has float overloads, bVal, and getUnnormalized(); not mirrored yet.
 */
public enum AngleUnit {
    DEGREES, RADIANS;

    public double toDegrees(double v) { return this == DEGREES ? v : Math.toDegrees(v); }
    public double toRadians(double v) { return this == RADIANS ? v : Math.toRadians(v); }
    public double fromDegrees(double degrees) { return this == DEGREES ? degrees : Math.toRadians(degrees); }
    public double fromRadians(double radians) { return this == RADIANS ? radians : Math.toDegrees(radians); }
    public double fromUnit(AngleUnit them, double v) { return this == DEGREES ? them.toDegrees(v) : them.toRadians(v); }

    public double normalize(double v) { return this == DEGREES ? normalizeDegrees(v) : normalizeRadians(v); }

    public static double normalizeDegrees(double degrees) {
        while (degrees >= 180.0) degrees -= 360.0;
        while (degrees < -180.0) degrees += 360.0;
        return degrees;
    }

    public static double normalizeRadians(double radians) {
        while (radians >= Math.PI) radians -= 2.0 * Math.PI;
        while (radians < -Math.PI) radians += 2.0 * Math.PI;
        return radians;
    }
}
