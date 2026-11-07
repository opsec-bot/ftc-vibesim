package com.qualcomm.robotcore.hardware;

/** Mirror of the FTC SDK ServoController interface. */
public interface ServoController extends HardwareDevice {

    enum PwmStatus { ENABLED, DISABLED }

    void pwmEnable();
    void pwmDisable();
    PwmStatus getPwmStatus();
    void setServoPosition(int servo, double position);
    double getServoPosition(int servo);
    void forgetLastKnownPosition(int servo);
}
