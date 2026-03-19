package com.mojang.minecraft;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Options {
    private final KeyMapping keyForward;
    private final KeyMapping keyBack;
    private final KeyMapping keyLeft;
    private final KeyMapping keyRight;
    private final KeyMapping keyJump;
    public final KeyMapping keyReset;
    public final KeyMapping keySave;
    public KeyMapping[] keys;
    public Minecraft mc;

    public Options(Minecraft mc) {
        keyForward = new KeyMapping(this, "Forward", GLFW_KEY_W);
        keyBack = new KeyMapping(this, "Back", GLFW_KEY_S);
        keyLeft = new KeyMapping(this, "Left", GLFW_KEY_A);
        keyRight = new KeyMapping(this, "Right", GLFW_KEY_D);
        keyJump = new KeyMapping(this, "Jump", GLFW_KEY_SPACE);
        keyReset = new KeyMapping(this, "Reset", GLFW_KEY_R);
        keySave = new KeyMapping(this, "Save", GLFW_KEY_ENTER);
        keys = new KeyMapping[] {
                keyForward,
                keyBack,
                keyLeft,
                keyRight,
                keyJump
        };
        this.mc = mc;
    }
}
