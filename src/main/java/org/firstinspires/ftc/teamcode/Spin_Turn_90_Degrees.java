package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "Spin_Turn_90_Degrees (fixed)")
public class Spin_Turn_90_Degrees extends LinearOpMode {

  private DcMotor SWmotorL;
  private DcMotor SWmotorR;

  int targetTicksLeft;
  int targetTicksRight;

  @Override
  public void runOpMode() {
    int trackWidth;
    int wheelDiameter;
    int turnAngleDegrees;
    double ticksPerRev;
    double ticksPerCm;
    double turnDistanceCm;
    double circumference;
    double turnTicks;

    SWmotorL = hardwareMap.get(DcMotor.class, "SWmotorL");
    SWmotorR = hardwareMap.get(DcMotor.class, "SWmotorR");

    trackWidth = 30;
    wheelDiameter = 9;
    turnAngleDegrees = 90;

    // Reverse the left motor so positive power moves the robot forward
    SWmotorL.setDirection(DcMotor.Direction.REVERSE);
    SWmotorR.setDirection(DcMotor.Direction.FORWARD);
    SWmotorL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    SWmotorR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

    // ==========================================
    // MATH & CALCULATIONS
    // ==========================================
    // FIX 2: ticks/rev uses the ACTUAL UltraPlanetary ratio (18.9:1), not nominal 20:1.
    // HD Hex motor = 28 counts/rev at the motor.  28 * 18.9 = ~529 (was 28*5*4 = 560).
    ticksPerRev = 28 * 18.9;

    // Ticks per cm = ticksPerRev / wheel circumference
    circumference = Math.PI * wheelDiameter;     // ~28.27 cm
    ticksPerCm = ticksPerRev / circumference;    // ~18.72

    // FIX 1 (the killer): 360.0 not 360. With int 360, (90/360)=0 and the robot never turned.
    // A full 360 spin travels Pi * trackWidth at each wheel; 90 deg is 1/4 of that.
    turnDistanceCm = Math.PI * trackWidth * (turnAngleDegrees / 360.0);  // ~23.562 cm

    // FIX 3: Math.round already returns a whole number; the old Math.floor(Math.round(...)) did nothing.
    turnTicks = Math.round(turnDistanceCm * ticksPerCm);                 // ~441

    // For a RIGHT spin turn: left wheel forward (+), right wheel backward (-)
    targetTicksLeft = (int) turnTicks;
    targetTicksRight = (int) -turnTicks;

    telemetry.addData("Calculated Turn Dist (cm)", turnDistanceCm);
    telemetry.addData("Calculated Ticks/Cm", ticksPerCm);
    telemetry.addData("Target Ticks", turnTicks);
    telemetry.update();

    waitForStart();

    if (opModeIsActive()) {
      movement();
      while (opModeIsActive() && (SWmotorL.isBusy() || SWmotorR.isBusy())) {
        telemetry2();
      }
      SWmotorL.setPower(0);
      SWmotorR.setPower(0);
    }
  }

  private void movement() {
    SWmotorL.setTargetPosition(targetTicksLeft);
    SWmotorR.setTargetPosition(targetTicksRight);
    SWmotorL.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    SWmotorR.setMode(DcMotor.RunMode.RUN_TO_POSITION);
    SWmotorL.setPower(0.5);
    SWmotorR.setPower(0.5);
  }

  private void telemetry2() {
    telemetry.addData("Target Left", targetTicksLeft);
    telemetry.addData("Target Right", targetTicksRight);
    telemetry.addData("Current Pos Left", SWmotorL.getCurrentPosition());
    telemetry.addData("Current Pos Right", SWmotorR.getCurrentPosition());
    telemetry.update();
  }
}
