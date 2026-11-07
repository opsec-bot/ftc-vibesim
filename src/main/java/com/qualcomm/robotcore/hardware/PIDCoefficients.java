package com.qualcomm.robotcore.hardware;

/** Mirror of the FTC SDK PIDCoefficients. */
public class PIDCoefficients {
    public double p;
    public double i;
    public double d;

    public PIDCoefficients() {}
    public PIDCoefficients(double p, double i, double d) { this.p = p; this.i = i; this.d = d; }

    @Override public String toString() {
        return String.format("pid: %f,%f,%f", p, i, d);
    }
}
