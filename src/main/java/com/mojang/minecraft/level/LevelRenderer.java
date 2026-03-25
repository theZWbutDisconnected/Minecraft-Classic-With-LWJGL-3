//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft.level;

import com.mojang.minecraft.HitResult;
import com.mojang.minecraft.Player;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Frustum;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.lwjgl.opengl.GL11;

import static com.mojang.minecraft.level.Chunk.CHUNK_SIZE;

public class LevelRenderer implements LevelListener {
    public static final int MAX_REBUILDS_PER_FRAME = 8;
    private Level level;
    private ChunkRenderer[] chunks;
    private int xChunks;
    private int yChunks;
    private int zChunks;
    private Textures textures;

    public LevelRenderer(Level level, Textures textures) {
        this.level = level;
        this.textures = textures;
        level.addListener(this);
        this.xChunks = level.width / CHUNK_SIZE;
        this.yChunks = level.depth / CHUNK_SIZE;
        this.zChunks = level.height / CHUNK_SIZE;
        this.chunks = new ChunkRenderer[this.xChunks * this.yChunks * this.zChunks];

        for(int x = 0; x < this.xChunks; ++x) {
            for(int y = 0; y < this.yChunks; ++y) {
                for(int z = 0; z < this.zChunks; ++z) {
                    int x0 = x * CHUNK_SIZE;
                    int y0 = y * CHUNK_SIZE;
                    int z0 = z * CHUNK_SIZE;
                    int x1 = (x + 1) * CHUNK_SIZE;
                    int y1 = (y + 1) * CHUNK_SIZE;
                    int z1 = (z + 1) * CHUNK_SIZE;
                    if (x1 > level.width) {
                        x1 = level.width;
                    }

                    if (y1 > level.depth) {
                        y1 = level.depth;
                    }

                    if (z1 > level.height) {
                        z1 = level.height;
                    }

                    this.chunks[(x + y * this.xChunks) * this.zChunks + z] = new ChunkRenderer(level, x0, y0, z0, x1, y1, z1);
                }
            }
        }

    }

    public List<ChunkRenderer> getAllDirtyChunks() {
        ArrayList<ChunkRenderer> dirty = null;

        for(int i = 0; i < this.chunks.length; ++i) {
            ChunkRenderer chunk = this.chunks[i];
            if (chunk.isDirty()) {
                if (dirty == null) {
                    dirty = new ArrayList();
                }

                dirty.add(chunk);
            }
        }

        return dirty;
    }

    public void render(Player player, int layer) {
        GL11.glEnable(3553);
        int id = this.textures.loadTexture("/terrain.png", 9728);
        GL11.glBindTexture(3553, id);
        Frustum frustum = Frustum.getFrustum();

        for(int i = 0; i < this.chunks.length; ++i) {
            if (frustum.isVisible(this.chunks[i].aabb)) {
                this.chunks[i].render(layer);
            }
        }

        GL11.glDisable(3553);
    }

    public void updateDirtyChunks(Player player) {
        List<ChunkRenderer> dirty = this.getAllDirtyChunks();
        if (dirty != null) {
            Collections.sort(dirty, new DirtyChunkSorter(player, Frustum.getFrustum()));

            for(int i = 0; i < 8 && i < dirty.size(); ++i) {
                ((ChunkRenderer)dirty.get(i)).rebuild();
            }

        }
    }

    public void pick(Player player, Frustum frustum) {
        Tesselator t = Tesselator.instance;
        float r = 3.0F;
        AABB box = player.bb.grow(r, r, r);
        int x0 = (int)box.x0;
        int x1 = (int)(box.x1 + 1.0F);
        int y0 = (int)box.y0;
        int y1 = (int)(box.y1 + 1.0F);
        int z0 = (int)box.z0;
        int z1 = (int)(box.z1 + 1.0F);
        GL11.glInitNames();
        GL11.glPushName(0);
        GL11.glPushName(0);

        for(int x = x0; x < x1; ++x) {
            GL11.glLoadName(x);
            GL11.glPushName(0);

            for(int y = y0; y < y1; ++y) {
                GL11.glLoadName(y);
                GL11.glPushName(0);

                for(int z = z0; z < z1; ++z) {
                    Tile tile = Tile.tiles[this.level.getTile(x, y, z)];
                    if (tile != null && frustum.isVisible(tile.getTileAABB(x, y, z))) {
                        GL11.glLoadName(z);
                        GL11.glPushName(0);

                        for(int i = 0; i < 6; ++i) {
                            GL11.glLoadName(i);
                            t.init();
                            tile.renderFaceNoTexture(t, x, y, z, i);
                            t.flush();
                        }

                        GL11.glPopName();
                    }
                }

                GL11.glPopName();
            }

            GL11.glPopName();
        }

        GL11.glPopName();
        GL11.glPopName();
    }

    public void renderHit(HitResult h, int mode, int tileType) {
        Tesselator t = Tesselator.instance;
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, ((float)Math.sin((double)System.currentTimeMillis() / (double)100.0F) * 0.2F + 0.4F) * 0.5F);
        if (mode == 0) {
            t.init();

            for(int i = 0; i < 6; ++i) {
                Tile.rock.renderFaceNoTexture(t, h.x(), h.y(), h.z(), i);
            }

            t.flush();
        } else {
            GL11.glBlendFunc(770, 771);
            float br = (float)Math.sin((double)System.currentTimeMillis() / (double)100.0F) * 0.2F + 0.8F;
            GL11.glColor4f(br, br, br, (float)Math.sin((double)System.currentTimeMillis() / (double)200.0F) * 0.2F + 0.5F);
            GL11.glEnable(3553);
            int id = this.textures.loadTexture("/terrain.png", 9728);
            GL11.glBindTexture(3553, id);
            int x = h.x();
            int y = h.y();
            int z = h.z();
            if (h.face() == 0) {
                --y;
            }

            if (h.face() == 1) {
                ++y;
            }

            if (h.face() == 2) {
                --z;
            }

            if (h.face() == 3) {
                ++z;
            }

            if (h.face() == 4) {
                --x;
            }

            if (h.face() == 5) {
                ++x;
            }

            t.init();
            t.noColor();
            Tile.tiles[tileType].render(t, this.level, 0, x, y, z);
            Tile.tiles[tileType].render(t, this.level, 1, x, y, z);
            t.flush();
            GL11.glDisable(3553);
        }

        GL11.glDisable(3042);
    }

    public void setDirty(int x0, int y0, int z0, int x1, int y1, int z1) {
        x0 /= CHUNK_SIZE;
        x1 /= CHUNK_SIZE;
        y0 /= CHUNK_SIZE;
        y1 /= CHUNK_SIZE;
        z0 /= CHUNK_SIZE;
        z1 /= CHUNK_SIZE;
        if (x0 < 0) {
            x0 = 0;
        }

        if (y0 < 0) {
            y0 = 0;
        }

        if (z0 < 0) {
            z0 = 0;
        }

        if (x1 >= this.xChunks) {
            x1 = this.xChunks - 1;
        }

        if (y1 >= this.yChunks) {
            y1 = this.yChunks - 1;
        }

        if (z1 >= this.zChunks) {
            z1 = this.zChunks - 1;
        }

        for(int x = x0; x <= x1; ++x) {
            for(int y = y0; y <= y1; ++y) {
                for(int z = z0; z <= z1; ++z) {
                    this.chunks[(x + y * this.xChunks) * this.zChunks + z].setDirty();
                }
            }
        }

    }

    public void tileChanged(Chunk chunk, int x, int y, int z) {
        int x0 = chunk.chunkX * CHUNK_SIZE;
        int y0 = chunk.chunkY * CHUNK_SIZE;
        int z0 = chunk.chunkZ * CHUNK_SIZE;
        this.setDirty(x0 + x - 1, y0 + y - 1, z0 + z - 1, x0 + x + 1, y0 + y + 1, z0 + z + 1);
    }

    public void lightColumnChanged(Chunk chunk, int x, int z, int y0, int y1) {
        int xBase = chunk.chunkX * CHUNK_SIZE;
        int yBase = chunk.chunkY * CHUNK_SIZE;
        int zBase = chunk.chunkZ * CHUNK_SIZE;
        this.setDirty(xBase + x - 1, yBase + y0 - 1, zBase + z - 1, xBase + x + 1, yBase + y1 + 1, zBase + z + 1);
    }

    public void allChanged() {
        this.setDirty(0, 0, 0, this.level.width, this.level.depth, this.level.height);
    }
}
