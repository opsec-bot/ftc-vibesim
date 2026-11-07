package com.qualcomm.robotcore.hardware;

/** Mirror of the FTC SDK PIDFCoefficients. */
public class PIDFCoefficients {
    public double p;
    public double i;
    public double d;
    public double f;
    public MotorControlAlgorithm algorithm = MotorControlAlgorithm.PIDF;

    public PIDFCoefficients() {}
    public PIDFCoefficients(double p, double i, double d, double f) { this.p = p; this.i = i; this.d = d; this.f = f; }
    public PIDFCoefficients(double p, double i, double d, double f, MotorControlAlgorithm algorithm) {
        this(p, i, d, f); this.algorithm = algorithm;
    }
    public PIDFCoefficients(PIDFCoefficients c) { this(c.p, c.i, c.d, c.f, c.algorithm); }
    public PIDFCoefficients(PIDCoefficients c) { this(c.p, c.i, c.d, 0.0); }

    @Override public String toString() {
        return String.format("pidf: %f,%f,%f,%f,%s", p, i, d, f, algorithm);
    }
}
