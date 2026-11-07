package com.qualcomm.robotcore.hardware;

/** Mirror of the FTC SDK CRServo interface (continuous-rotation servo). */
public interface CRServo extends DcMotorSimple {
    ServoController getController();
    int getPortNumber();
}
