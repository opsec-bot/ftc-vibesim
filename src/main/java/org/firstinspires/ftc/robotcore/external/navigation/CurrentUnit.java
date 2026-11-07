package org.firstinspires.ftc.robotcore.external.navigation;

/** Mirror of the FTC SDK CurrentUnit. */
public enum CurrentUnit {
    AMPS, MILLIAMPS;

    public double toAmps(double v) { return this == AMPS ? v : v / 1000.0; }
    public double toMilliAmps(double v) { return this == MILLIAMPS ? v : v * 1000.0; }
    public double convert(double v, CurrentUnit them) {
        return this == AMPS ? them.toAmps(v) : them.toMilliAmps(v);
    }
}
