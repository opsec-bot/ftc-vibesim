package com.qualcomm.robotcore.hardware;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Desktop mirror of the FTC SDK HardwareMap. Matches the callable API opmodes use:
 * get/tryGet by (Class, name), get by name, iteration, and the dcMotor/servo/crservo
 * typed DeviceMappings.
 *
 * Coverage note: the real class carries an android.content.Context appContext field and
 * ~20 more DeviceMapping fields (sensors, I2C, etc.) plus the real constructor. Those are
 * Android-tainted / rarely called, so they are not mirrored. The sim registers devices
 * with put(name, device).
 */
public class HardwareMap implements Iterable<HardwareDevice> {

    public class DeviceMapping<T extends HardwareDevice> implements Iterable<T> {
        private final Map<String, T> map = new LinkedHashMap<>();
        public T get(String deviceName) {
            T d = map.get(deviceName);
            if (d == null) throw new IllegalArgumentException("no device \"" + deviceName + "\" of this type");
            return d;
        }
        public void put(String deviceName, T device) { map.put(deviceName, device); }
        public int size() { return map.size(); }
        @Override public Iterator<T> iterator() { return map.values().iterator(); }
    }

    public final DeviceMapping<DcMotor> dcMotor = new DeviceMapping<>();
    public final DeviceMapping<Servo> servo = new DeviceMapping<>();
    public final DeviceMapping<CRServo> crservo = new DeviceMapping<>();

    private final Map<String, HardwareDevice> byName = new LinkedHashMap<>();
    private final List<HardwareDevice> all = new ArrayList<>();

    /** SIM ONLY: register a device under a name (also files it in the matching typed mapping). */
    public void put(String deviceName, HardwareDevice device) {
        byName.put(deviceName, device);
        all.add(device);
        if (device instanceof DcMotor)  dcMotor.put(deviceName, (DcMotor) device);
        if (device instanceof Servo)    servo.put(deviceName, (Servo) device);
        if (device instanceof CRServo)  crservo.put(deviceName, (CRServo) device);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<? extends T> classOrInterface, String deviceName) {
        HardwareDevice d = byName.get(deviceName);
        if (d == null) {
            throw new IllegalArgumentException(
                "Sim hardwareMap has no device named \"" + deviceName + "\". Register it in SimMain.");
        }
        if (!classOrInterface.isInstance(d)) {
            throw new IllegalArgumentException(
                "Device \"" + deviceName + "\" is a " + d.getClass().getSimpleName()
                + ", not a " + classOrInterface.getSimpleName());
        }
        return (T) d;
    }

    @SuppressWarnings("unchecked")
    public <T> T tryGet(Class<? extends T> classOrInterface, String deviceName) {
        HardwareDevice d = byName.get(deviceName);
        if (d == null || !classOrInterface.isInstance(d)) return null;
        return (T) d;
    }

    public HardwareDevice get(String deviceName) {
        HardwareDevice d = byName.get(deviceName);
        if (d == null) throw new IllegalArgumentException("no device \"" + deviceName + "\"");
        return d;
    }

    @Override public Iterator<HardwareDevice> iterator() { return all.iterator(); }
}
