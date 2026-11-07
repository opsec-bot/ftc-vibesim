package com.qualcomm.robotcore.hardware;

/**
 * Mirror of the FTC SDK DcMotor interface (callable public API).
 *
 * Coverage note: the real DcMotor also has getMotorType/setMotorType (MotorConfigurationType)
 * and getController (DcMotorController). Those pull in device-driver types that opmodes rarely
 * touch, so they are intentionally not mirrored yet. The signature checker reports them as gaps.
 */
public interface DcMotor extends DcMotorSimple {

    enum RunMode {
        RUN_WITHOUT_ENCODER, RUN_USING_ENCODER, RUN_TO_POSITION, STOP_AND_RESET_ENCODER,
        RUN_WITHOUT_ENCODERS, RUN_USING_ENCODERS, RESET_ENCODERS;

        public RunMode migrate() {
            switch (this) {
                case RUN_WITHOUT_ENCODERS: return RUN_WITHOUT_ENCODER;
                case RUN_USING_ENCODERS:   return RUN_USING_ENCODER;
                case RESET_ENCODERS:       return STOP_AND_RESET_ENCODER;
                default:                   return this;
            }
        }

        public boolean isPIDMode() {
            return this == RUN_USING_ENCODER || this == RUN_TO_POSITION;
        }
    }

    enum ZeroPowerBehavior { UNKNOWN, BRAKE, FLOAT }

    int getPortNumber();
    void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior);
    ZeroPowerBehavior getZeroPowerBehavior();
    void setPowerFloat();
    boolean getPowerFloat();
    void setTargetPosition(int position);
    int getTargetPosition();
    boolean isBusy();
    int getCurrentPosition();
    void setMode(RunMode mode);
    RunMode getMode();
}
