package com.ftcsim.sim;

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;

/** Simulated servo implementing the real Servo interface. */
public class SimServo implements Servo {

    private final String name;
    private volatile double position = 0.0;
    private volatile Direction direction = Direction.FORWARD;
    private volatile double scaleMin = 0.0;
    private volatile double scaleMax = 1.0;

    public SimServo(String name) { this.name = name; }

    @Override public ServoController getController() { return null; }
    @Override public int getPortNumber() { return 0; }
    @Override public void setDirection(Direction d) { this.direction = d; }
    @Override public Direction getDirection() { return direction; }
    @Override public void setPosition(double position) {
        double p = Math.max(0.0, Math.min(1.0, position));
        this.position = scaleMin + p * (scaleMax - scaleMin);
    }
    @Override public double getPosition() { return position; }
    @Override public void scaleRange(double min, double max) { this.scaleMin = min; this.scaleMax = max; }

    @Override public Manufacturer getManufacturer() { return Manufacturer.Lynx; }
    @Override public String getDeviceName() { return "Sim Servo"; }
    @Override public String getConnectionInfo() { return "sim; servo " + name; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}

    public String getName() { return name; }
}
