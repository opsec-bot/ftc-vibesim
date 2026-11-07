package com.qualcomm.robotcore.hardware;

import java.util.HashMap;
import java.util.Map;

/**
 * Simulated FTC HardwareMap. The engine pre-populates it with named devices before the
 * opmode runs; the opmode looks them up with get(Class, name) exactly like on a robot.
 */
public class HardwareMap {

    private final Map<String, Object> devices = new HashMap<>();

    /** SIM ONLY: register a device under a name. */
    public void put(String name, Object device) { devices.put(name, device); }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> classOrInterface, String deviceName) {
        Object d = devices.get(deviceName);
        if (d == null) {
            throw new IllegalArgumentException(
                "Sim hardwareMap has no device named \"" + deviceName + "\". "
                + "Add it in SimMain before running.");
        }
        return (T) d;
    }
}
