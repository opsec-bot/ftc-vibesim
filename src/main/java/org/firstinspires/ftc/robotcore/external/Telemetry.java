package org.firstinspires.ftc.robotcore.external;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Minimal stand-in for the FTC SDK Telemetry. addData/addLine queue key-value lines,
 * update() renders them (and clears for the next cycle), just like on the Driver Station.
 * The engine reads getRendered() to show the latest telemetry in the sim window / console.
 */
public class Telemetry {

    private final LinkedHashMap<String, String> data = new LinkedHashMap<>();
    private volatile String rendered = "";
    private int lineCounter = 0;

    public Object addData(String caption, Object value) {
        data.put(caption, String.valueOf(value));
        return null;
    }

    public Object addData(String caption, String format, Object... args) {
        data.put(caption, String.format(format, args));
        return null;
    }

    public Object addLine(String line) {
        data.put("\u0000line" + (lineCounter++), line);
        return null;
    }

    public boolean update() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : data.entrySet()) {
            if (e.getKey().startsWith("\u0000line")) {
                sb.append(e.getValue()).append('\n');
            } else {
                sb.append(e.getKey()).append(" : ").append(e.getValue()).append('\n');
            }
        }
        rendered = sb.toString();
        data.clear();
        lineCounter = 0;
        return true;
    }

    /** SIM ONLY: latest rendered telemetry block. */
    public String getRendered() { return rendered; }
}
