//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft;

import com.mojang.minecraft.level.Level;

public class Player extends Entity {
    public Player(Level level) {
        super(level);
        this.heightOffset = 1.62F;
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        float xa = 0.0F;
        float ya = 0.0F;
        Minecraft mc = Minecraft.getInstance();
        if (KeyMapping.isDown(mc.options.keyReset)) {
            this.resetPos();
        }

        if (KeyMapping.isDown(mc.options.keys[0])) {
            --ya;
        }

        if (KeyMapping.isDown(mc.options.keys[1])) {
            ++ya;
        }

        if (KeyMapping.isDown(mc.options.keys[2])) {
            --xa;
        }

        if (KeyMapping.isDown(mc.options.keys[3])) {
            ++xa;
        }

        if (KeyMapping.isDown(mc.options.keys[4]) && this.onGround) {
            this.yd = 0.5F;
        }

        this.moveRelative(xa, ya, this.onGround ? 0.1F : 0.02F);
        this.yd = (float)((double)this.yd - 0.08);
        this.move(this.xd, this.yd, this.zd);
        this.xd *= 0.91F;
        this.yd *= 0.98F;
        this.zd *= 0.91F;
        if (this.onGround) {
            this.xd *= 0.7F;
            this.zd *= 0.7F;
        }

    }
}
