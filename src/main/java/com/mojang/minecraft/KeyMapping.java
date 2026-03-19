package com.mojang.minecraft;

import static org.lwjgl.glfw.GLFW.*;

public record KeyMapping(Options options, String name, int key) {
    public static boolean isDown(KeyMapping key) {
        return glfwGetKey(key.options.mc.window, key.key) == GLFW_PRESS;
    }
}
