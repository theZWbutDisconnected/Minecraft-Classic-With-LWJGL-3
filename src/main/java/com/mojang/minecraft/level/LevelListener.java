//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft.level;

public interface LevelListener {
    void tileChanged(Chunk chunk, int var1, int var2, int var3);

    void lightColumnChanged(Chunk chunk, int var1, int var2, int var3, int var4);

    void allChanged();
}
