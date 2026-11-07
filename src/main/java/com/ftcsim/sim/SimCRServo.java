package com.ftcsim.sim;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ServoController;

/** Simulated continuous-rotation servo implementing the real CRServo interface. */
public class SimCRServo implements CRServo {

    private final String name;
    private volatile Direction direction = Direction.FORWARD;
    private volatile double power = 0.0;

    public SimCRServo(String name) { this.name = name; }

    @Override public ServoController getController() { return null; }
    @Override public int getPortNumber() { return 0; }

    @Override public void setDirection(Direction d) { this.direction = d; }
    @Override public Direction getDirection() { return direction; }
    @Override public void setPower(double power) { this.power = power; }
    @Override public double getPower() { return power; }

    @Override public Manufacturer getManufacturer() { return Manufacturer.Lynx; }
    @Override public String getDeviceName() { return "Sim CRServo"; }
    @Override public String getConnectionInfo() { return "sim; crservo " + name; }
    @Override public int getVersion() { return 1; }
    @Override public void resetDeviceConfigurationForOpMode() {}
    @Override public void close() {}

    /** SIM: effective signed power (direction applied). */
    public double effectivePower() { return direction == Direction.REVERSE ? -power : power; }
    public String getName() { return name; }
}
