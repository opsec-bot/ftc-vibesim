package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * The ORIGINAL opmode, bugs intact, kept to show what the sim catches.
 * Run it with:  ./gradlew runSim -Dsim.opmode=org.firstinspires.ftc.teamcode.Spin_Turn_90_Buggy
 * It should end at ~0 degrees: the robot never turns, because (90/360) is integer 0.
 */
@TeleOp(name = "Spin_Turn_90_Degrees (BUGGY original)")
public class Spin_Turn_90_Buggy extends LinearOpMode {

  private DcMotor SWmotorL;
  private DcMotor SWmotorR;

  int targetTicksLeft;
  int targetTicksRight;

  @Override
  public void runOpMode() {
    int trackWidth;
    int wheelDiameter;
    int turnAngleDegrees;
    int ticksPerRev;
    double ticksPerCm;
    double turnDistanceCm;
    double circumference;
    double turnTicks;

    SWmotorL = hardwareMap.get(DcMotor.class, "SWmotorL");
    SWmotorR = hardwareMap.get(DcMotor.class, "SWmotorR");

    trackWidth = 30;
    wheelDiameter = 9;
    turnAngleDegrees = 90;
    SWmotorL.setDirection(DcMotor.Direction.REVERSE);
    SWmotorR.setDirection(DcMotor.Direction.FORWARD);
    SWmotorL.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
    SWmotorR.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

    ticksPerRev = 28 * 5 * 4 * 1;                 // 560 (nominal, slightly high)
    circumference = Math.PI * wheelDiameter;
    ticksPerCm = ticksPerRev / circumference;
    turnDistanceCm = Math.PI * trackWidth * (turnAngleDegrees / 360);  // BUG: int / int = 0
    turnTicks = Math.floor(Math.round(turnDistanceCm * ticksPerCm));
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
