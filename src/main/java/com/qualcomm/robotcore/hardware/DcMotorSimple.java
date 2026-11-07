package com.qualcomm.robotcore.hardware;

/** Mirror of the FTC SDK DcMotorSimple interface. Direction lives here (not in DcMotor). */
public interface DcMotorSimple extends HardwareDevice {

    enum Direction {
        FORWARD, REVERSE;
        public Direction inverted() { return this == FORWARD ? REVERSE : FORWARD; }
    }

    void setDirection(Direction direction);
    Direction getDirection();
    void setPower(double power);
    double getPower();
}
