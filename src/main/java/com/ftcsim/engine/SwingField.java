package com.ftcsim.engine;

import com.ftcsim.sim.SimTelemetry;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A plain-JDK (Swing) field view: draws a 360 cm FTC-ish field, the robot as an oriented
 * rectangle with a heading nose, a fading trail, and the latest telemetry text. No web
 * server, no external deps. Reads the live SimRobot pose on every repaint.
 */
public class SwingField extends JPanel {

    private static final int FIELD_CM = 360;
    private final SimRobot robot;
    private final SimTelemetry telemetry;
    private final Deque<double[]> trail = new ArrayDeque<>();
    private volatile double simTime = 0.0;

    public SwingField(SimRobot robot, SimTelemetry telemetry) {
        this.robot = robot;
        this.telemetry = telemetry;
        setPreferredSize(new Dimension(680, 720));
        setBackground(new Color(18, 20, 24));
    }

    public void setSimTime(double t) { simTime = t; }

    public void pushTrail() {
        trail.addLast(new double[]{robot.x, robot.y});
        while (trail.size() > 600) trail.removeFirst();
    }

    public static JFrame show(SimRobot robot, SimTelemetry telemetry) {
        final JFrame[] holder = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame f = new JFrame("FTC VibeSim");
                SwingField panel = new SwingField(robot, telemetry);
                f.setContentPane(panel);
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
                holder[0] = f;
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return holder[0];
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int fieldPx = Math.min(getWidth(), getHeight() - 110) - 24;
        int ox = (getWidth() - fieldPx) / 2;
        int oy = 12;
        double scale = (double) fieldPx / FIELD_CM;

        // field
        g.setColor(new Color(30, 34, 40));
        g.fillRect(ox, oy, fieldPx, fieldPx);
        g.setColor(new Color(60, 66, 76));
        for (int i = 0; i <= 6; i++) {
            int p = ox + (int) (i * fieldPx / 6.0);
            int q = oy + (int) (i * fieldPx / 6.0);
            g.drawLine(p, oy, p, oy + fieldPx);
            g.drawLine(ox, q, ox + fieldPx, q);
        }
        g.setColor(new Color(90, 98, 110));
        g.drawRect(ox, oy, fieldPx, fieldPx);

        // field-cm -> screen-px (center origin, y up)
        double cx = ox + fieldPx / 2.0;
        double cy = oy + fieldPx / 2.0;

        // trail
        g.setColor(new Color(80, 170, 255, 120));
        double[] prev = null;
        for (double[] p : trail) {
            if (prev != null) {
                g.drawLine((int) (cx + prev[0] * scale), (int) (cy - prev[1] * scale),
                           (int) (cx + p[0] * scale),   (int) (cy - p[1] * scale));
            }
            prev = p;
        }

        // robot
        double rx = cx + robot.x * scale;
        double ry = cy - robot.y * scale;
        double w = robot.cfg.bodyWidthCm * scale;
        double h = robot.cfg.bodyLengthCm * scale;

        AffineTransform old = g.getTransform();
        g.translate(rx, ry);
        g.rotate(-robot.thetaRad); // screen y is down, so negate CCW angle
        g.setColor(new Color(64, 200, 140));
        g.fillRoundRect((int) (-h / 2), (int) (-w / 2), (int) h, (int) w, 8, 8);
        g.setColor(new Color(20, 24, 28));
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect((int) (-h / 2), (int) (-w / 2), (int) h, (int) w, 8, 8);
        // nose (forward = +x in robot frame)
        g.setColor(new Color(255, 220, 80));
        g.setStroke(new BasicStroke(3f));
        g.drawLine(0, 0, (int) (h / 2), 0);
        g.setTransform(old);

        // HUD
        int ty = oy + fieldPx + 20;
        g.setColor(new Color(220, 226, 232));
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
        g.drawString(String.format("t=%.2fs   x=%.1fcm  y=%.1fcm  heading=%.1f°",
                simTime, robot.x, robot.y, robot.headingDeg()), ox, ty);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        String tel = telemetry.getRendered();
        int line = ty + 20;
        for (String s : tel.split("\n")) {
            if (line > getHeight() - 6) break;
            g.setColor(new Color(150, 200, 170));
            g.drawString(s, ox, line);
            line += 16;
        }
    }
}
