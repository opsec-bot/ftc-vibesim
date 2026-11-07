package com.ftcsim.sim;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Desktop implementation of the real Telemetry interface. Renders to a String the sim shows. */
public class SimTelemetry implements Telemetry {

    private static final class Entry {
        String caption;            // null for a bare line
        Supplier<String> value;
        boolean retained;
    }

    private final List<Entry> entries = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private volatile String rendered = "";
    private boolean autoClear = true;
    private int msInterval = 250;
    private String itemSeparator = " | ";
    private String captionValueSeparator = " : ";

    private Entry add(String caption, Supplier<String> value) {
        Entry e = new Entry();
        e.caption = caption;
        e.value = value;
        entries.add(e);
        return e;
    }

    private final class ItemImpl implements Item {
        final Entry e;
        ItemImpl(Entry e) { this.e = e; }
        @Override public String getCaption() { return e.caption; }
        @Override public Item setCaption(String caption) { e.caption = caption; return this; }
        @Override public Item setValue(String format, Object... args) { e.value = () -> String.format(format, args); return this; }
        @Override public Item setValue(Object value) { e.value = () -> String.valueOf(value); return this; }
        @Override public <T> Item setValue(Func<T> vp) { e.value = () -> String.valueOf(vp.value()); return this; }
        @Override public <T> Item setValue(String format, Func<T> vp) { e.value = () -> String.format(format, vp.value()); return this; }
        @Override public Item setRetained(Boolean retained) { e.retained = Boolean.TRUE.equals(retained); return this; }
        @Override public boolean isRetained() { return e.retained; }
        @Override public Item addData(String c, String f, Object... a) { return new ItemImpl(add(c, () -> String.format(f, a))); }
        @Override public Item addData(String c, Object v) { return new ItemImpl(add(c, () -> String.valueOf(v))); }
        @Override public <T> Item addData(String c, Func<T> vp) { return new ItemImpl(add(c, () -> String.valueOf(vp.value()))); }
        @Override public <T> Item addData(String c, String f, Func<T> vp) { return new ItemImpl(add(c, () -> String.format(f, vp.value()))); }
    }

    private final class LineImpl implements Line {
        @Override public Item addData(String c, String f, Object... a) { return new ItemImpl(add(c, () -> String.format(f, a))); }
        @Override public Item addData(String c, Object v) { return new ItemImpl(add(c, () -> String.valueOf(v))); }
        @Override public <T> Item addData(String c, Func<T> vp) { return new ItemImpl(add(c, () -> String.valueOf(vp.value()))); }
        @Override public <T> Item addData(String c, String f, Func<T> vp) { return new ItemImpl(add(c, () -> String.format(f, vp.value()))); }
    }

    private final class LogImpl implements Log {
        private final List<String> lines = new ArrayList<>();
        private int capacity = 9;
        private DisplayOrder order = DisplayOrder.OLDEST_FIRST;
        @Override public int getCapacity() { return capacity; }
        @Override public void setCapacity(int capacity) { this.capacity = capacity; }
        @Override public DisplayOrder getDisplayOrder() { return order; }
        @Override public void setDisplayOrder(DisplayOrder displayOrder) { this.order = displayOrder; }
        @Override public void add(String entry) { lines.add(entry); while (lines.size() > capacity) lines.remove(0); }
        @Override public void add(String format, Object... args) { add(String.format(format, args)); }
        @Override public void clear() { lines.clear(); }
    }

    private final LogImpl logImpl = new LogImpl();

    @Override public Item addData(String c, String f, Object... a) { return new ItemImpl(add(c, () -> String.format(f, a))); }
    @Override public Item addData(String c, Object v) { return new ItemImpl(add(c, () -> String.valueOf(v))); }
    @Override public <T> Item addData(String c, Func<T> vp) { return new ItemImpl(add(c, () -> String.valueOf(vp.value()))); }
    @Override public <T> Item addData(String c, String f, Func<T> vp) { return new ItemImpl(add(c, () -> String.format(f, vp.value()))); }

    @Override public boolean removeItem(Item item) { return item instanceof ItemImpl && entries.remove(((ItemImpl) item).e); }
    @Override public void clear() { entries.removeIf(e -> !e.retained); }
    @Override public void clearAll() { entries.clear(); }
    @Override public Object addAction(Runnable action) { actions.add(action); return action; }
    @Override public boolean removeAction(Object token) { return actions.remove(token); }
    @Override public void speak(String text) {}
    @Override public void speak(String text, String languageCode, String countryCode) {}

    @Override public boolean update() {
        for (Runnable a : actions) a.run();
        StringBuilder sb = new StringBuilder();
        for (Entry e : entries) {
            String v = e.value == null ? "" : e.value.get();
            if (e.caption == null || e.caption.isEmpty()) sb.append(v);
            else sb.append(e.caption).append(captionValueSeparator).append(v);
            sb.append('\n');
        }
        rendered = sb.toString();
        if (autoClear) clear();
        return true;
    }

    @Override public Line addLine() { add(null, () -> ""); return new LineImpl(); }
    @Override public Line addLine(String lineCaption) { add(null, () -> lineCaption); return new LineImpl(); }
    @Override public boolean removeLine(Line line) { return false; }
    @Override public boolean isAutoClear() { return autoClear; }
    @Override public void setAutoClear(boolean autoClear) { this.autoClear = autoClear; }
    @Override public int getMsTransmissionInterval() { return msInterval; }
    @Override public void setMsTransmissionInterval(int ms) { this.msInterval = ms; }
    @Override public String getItemSeparator() { return itemSeparator; }
    @Override public void setItemSeparator(String s) { this.itemSeparator = s; }
    @Override public String getCaptionValueSeparator() { return captionValueSeparator; }
    @Override public void setCaptionValueSeparator(String s) { this.captionValueSeparator = s; }
    @Override public void setDisplayFormat(DisplayFormat displayFormat) {}
    @Override public Log log() { return logImpl; }

    /** SIM ONLY: latest rendered telemetry block. */
    public String getRendered() { return rendered; }
}
