package com.mojang.minecraft;

import static org.lwjgl.glfw.GLFW.*;

public class KeyMapping {
    public static final KeyMapping keyForward = new KeyMapping("Forward", GLFW_KEY_W);
    public static final KeyMapping keyBack = new KeyMapping("Back", GLFW_KEY_S);
    public static final KeyMapping keyLeft = new KeyMapping("Left", GLFW_KEY_A);
    public static final KeyMapping keyRight = new KeyMapping("Right", GLFW_KEY_D);

    private String name;
    private int key;

    public KeyMapping(String name, int key) {
        this.name = name;
        this.key = key;
    }
}
