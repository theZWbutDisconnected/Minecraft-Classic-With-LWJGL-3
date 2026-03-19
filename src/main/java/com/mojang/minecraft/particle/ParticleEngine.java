//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft.particle;

import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.opengl.GL11;

public class ParticleEngine {
    protected Level level;
    private List<Particle> particles = new ArrayList();
    private Textures textures;

    public ParticleEngine(Level level, Textures textures) {
        this.level = level;
        this.textures = textures;
    }

    public void add(Particle p) {
        this.particles.add(p);
    }

    public void tick() {
        for(int i = 0; i < this.particles.size(); ++i) {
            Particle p = (Particle)this.particles.get(i);
            p.tick();
            if (p.removed) {
                this.particles.remove(i--);
            }
        }

    }

    public void render(Player player, float a, int layer) {
        if (this.particles.size() != 0) {
            GL11.glEnable(3553);
            int id = this.textures.loadTexture("/terrain.png", 9728);
            GL11.glBindTexture(3553, id);
            float xa = -((float)Math.cos((double)player.yRot * Math.PI / (double)180.0F));
            float za = -((float)Math.sin((double)player.yRot * Math.PI / (double)180.0F));
            float xa2 = -za * (float)Math.sin((double)player.xRot * Math.PI / (double)180.0F);
            float za2 = xa * (float)Math.sin((double)player.xRot * Math.PI / (double)180.0F);
            float ya = (float)Math.cos((double)player.xRot * Math.PI / (double)180.0F);
            Tesselator t = Tesselator.instance;
            GL11.glColor4f(0.8F, 0.8F, 0.8F, 1.0F);
            t.init();

            for(int i = 0; i < this.particles.size(); ++i) {
                Particle p = (Particle)this.particles.get(i);
                if (p.isLit() ^ layer == 1) {
                    p.render(t, a, xa, ya, za, xa2, za2);
                }
            }

            t.flush();
            GL11.glDisable(3553);
        }
    }
}
