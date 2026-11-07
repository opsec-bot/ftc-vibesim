package com.ftcsim.engine;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import javax.swing.JFrame;

/**
 * Desktop launcher. Builds a simulated robot, injects a fake hardwareMap + telemetry into
 * an opmode, runs the opmode on its own thread and steps the physics world.
 *
 *   ./gradlew run            GUI window, real time
 *   ./gradlew runSim         headless + fast, prints trajectory and final pose
 *
 * To sim a different opmode, swap the `new Spin_Turn_90_Degrees()` line and make sure the
 * motor names below match what that opmode looks up in hardwareMap.
 */
public class SimMain {

    static final double DT = 0.01;          // 100 Hz physics
    static final double MAX_SIM_SECONDS = 15.0;

    public static void main(String[] args) throws Exception {
        boolean headless = Boolean.parseBoolean(System.getProperty("sim.headless", "false"));
        boolean fast     = Boolean.parseBoolean(System.getProperty("sim.fast", "false"));
        Double expectHeading = parseOrNull(System.getProperty("sim.expectHeadingDeg"));

        // --- build the robot / hardware ---
        RobotConfig cfg = new RobotConfig();
        DcMotor motorL = new DcMotor("SWmotorL");
        DcMotor motorR = new DcMotor("SWmotorR");
        SimRobot robot = new SimRobot(cfg, motorL, motorR);

        HardwareMap hw = new HardwareMap();
        hw.put("SWmotorL", motorL);
        hw.put("SWmotorR", motorR);

        Telemetry telemetry = new Telemetry();

        // --- the opmode under test (byte-identical to the deploy version) ---
        // Default is the fixed spin turn; override with -Dsim.opmode=<fully.qualified.ClassName>
        String opmodeClass = System.getProperty("sim.opmode",
                "org.firstinspires.ftc.teamcode.Spin_Turn_90_Degrees");
        LinearOpMode opmode = (LinearOpMode) Class.forName(opmodeClass)
                .getDeclaredConstructor().newInstance();
        opmode.hardwareMap = hw;
        opmode.telemetry = telemetry;

        System.out.println("=== FTC VibeSim ===");
        System.out.printf("Robot: ticks/rev=%.1f  wheel=%.1fcm  track=%.1fcm  ticks/cm=%.2f%n",
                cfg.ticksPerRev, cfg.wheelDiameterCm, cfg.trackWidthCm, cfg.ticksPerCm());
        System.out.println("OpMode: " + opmode.getClass().getSimpleName());
        System.out.println();

        // --- GUI (optional) ---
        SwingField panel = null;
        if (!headless) {
            JFrame frame = SwingField.show(robot, telemetry);
            panel = (SwingField) frame.getContentPane();
        }

        // --- run the opmode on its own thread ---
        final Throwable[] opErr = new Throwable[1];
        Thread opThread = new Thread(() -> {
            try { opmode.runOpMode(); }
            catch (Throwable t) { opErr[0] = t; }
        }, "opmode");
        opThread.setDaemon(true);
        opThread.start();

        // let init run, then "press start"
        stepFor(robot, panel, 0.0, 0.25, DT, fast);
        opmode.simStart();
        System.out.println("[START] match started\n");

        // --- main sim loop ---
        double t = 0.25;
        double nextLog = 0.0;
        while (opThread.isAlive() && t < MAX_SIM_SECONDS) {
            robot.step(DT);
            t += DT;
            if (panel != null) {
                panel.setSimTime(t);
                panel.pushTrail();
                panel.repaint();
            }
            if (t >= nextLog) {
                System.out.printf("t=%4.2fs  x=%6.1f  y=%6.1f  heading=%7.1f°  encL=%5d encR=%5d  busy=%b/%b%n",
                        t, robot.x, robot.y, robot.headingDeg(),
                        motorL.getCurrentPosition(), motorR.getCurrentPosition(),
                        motorL.isBusy(), motorR.isBusy());
                nextLog += 0.05;
            }
            if (!fast) sleep((long) (DT * 1000));
        }

        opmode.simRequestStop();
        opThread.join(500);

        System.out.println();
        if (opErr[0] != null) {
            System.out.println("[OPMODE THREW]");
            opErr[0].printStackTrace(System.out);
        }
        System.out.println("[DONE] opmode ended at t=" + String.format("%.2f", t) + "s");
        System.out.printf("Final pose: x=%.2fcm  y=%.2fcm  heading=%.2f°%n",
                robot.x, robot.y, robot.headingDeg());
        System.out.printf("Encoders:   L=%d  R=%d%n",
                motorL.getCurrentPosition(), motorR.getCurrentPosition());
        System.out.println("Last telemetry:");
        for (String s : telemetry.getRendered().split("\n")) System.out.println("   " + s);

        boolean expectationFailed = false;
        if (expectHeading != null) {
            double err = Math.abs(robot.headingDeg() - expectHeading);
            boolean pass = err < 2.0;
            expectationFailed = !pass;
            System.out.printf("%nRESULT: %s  (measured %.2f° vs expected %.1f°, err %.2f°)%n",
                    pass ? "PASS" : "FAIL", robot.headingDeg(), expectHeading, err);
        }

        String snap = System.getProperty("sim.snapshot");
        if (snap != null && !snap.isBlank()) {
            try {
                SwingField sp = new SwingField(robot, telemetry);
                sp.setSize(680, 720);
                sp.setSimTime(t);
                java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
                        680, 720, java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = img.createGraphics();
                sp.paint(g);
                g.dispose();
                java.io.File out = new java.io.File(snap);
                javax.imageio.ImageIO.write(img, "png", out);
                System.out.println("Snapshot written: " + out.getAbsolutePath());
            } catch (Exception e) {
                System.out.println("Snapshot failed: " + e.getMessage());
            }
        }

        boolean ok = opErr[0] == null && !expectationFailed;
        if (headless) System.exit(ok ? 0 : 1);
    }

    private static void stepFor(SimRobot robot, SwingField panel,
                                double tStart, double tEnd, double dt, boolean fast) {
        double t = tStart;
        while (t < tEnd) {
            robot.step(dt);
            t += dt;
            if (panel != null) { panel.setSimTime(t); panel.repaint(); }
            if (!fast) sleep((long) (dt * 1000));
        }
    }

    private static Double parseOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
