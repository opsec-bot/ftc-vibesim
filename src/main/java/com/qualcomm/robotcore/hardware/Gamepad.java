package com.qualcomm.robotcore.hardware;

/**
 * Mirror of the FTC SDK Gamepad: the public fields opmodes read directly.
 * Coverage note: the real class also has xWasPressed()/xWasReleased() edge helpers and
 * rumble APIs; not mirrored yet (they need rising-edge state tracking). The sim sets these
 * fields each loop (SimMain / a future input source).
 */
public class Gamepad {

    // sticks and triggers
    public volatile float left_stick_x = 0f;
    public volatile float left_stick_y = 0f;
    public volatile float right_stick_x = 0f;
    public volatile float right_stick_y = 0f;
    public volatile float left_trigger = 0f;
    public volatile float right_trigger = 0f;

    // face + dpad + bumpers + sticks + system buttons
    public volatile boolean a = false;
    public volatile boolean b = false;
    public volatile boolean x = false;
    public volatile boolean y = false;
    public volatile boolean dpad_up = false;
    public volatile boolean dpad_down = false;
    public volatile boolean dpad_left = false;
    public volatile boolean dpad_right = false;
    public volatile boolean left_bumper = false;
    public volatile boolean right_bumper = false;
    public volatile boolean left_stick_button = false;
    public volatile boolean right_stick_button = false;
    public volatile boolean back = false;
    public volatile boolean start = false;
    public volatile boolean guide = false;

    // PlayStation-style aliases
    public volatile boolean circle = false;
    public volatile boolean cross = false;
    public volatile boolean triangle = false;
    public volatile boolean square = false;
    public volatile boolean share = false;
    public volatile boolean options = false;
    public volatile boolean touchpad = false;
    public volatile boolean ps = false;

    public boolean atRest() {
        return left_stick_x == 0f && left_stick_y == 0f
            && right_stick_x == 0f && right_stick_y == 0f
            && left_trigger == 0f && right_trigger == 0f;
    }
}
