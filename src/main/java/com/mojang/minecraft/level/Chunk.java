package com.mojang.minecraft.level;

import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;

import java.util.ArrayList;
import java.util.Random;

public class Chunk {
    public static final int TILE_UPDATE_INTERVAL = 400;
    public static final int CHUNK_SIZE = 16;
    public int chunkX;
    public int chunkY;
    public int chunkZ;
    public byte[] blocks;
    private int[] lightDepths;
    private Level level;
    private Random random = new Random();
    int unprocessed = 0;

    public Chunk(Level level, int chunkX, int chunkY, int chunkZ) {
        this.level = level;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.blocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
        this.lightDepths = new int[CHUNK_SIZE * CHUNK_SIZE];
        this.calcLightDepths(0, 0, CHUNK_SIZE, CHUNK_SIZE);
    }

    public void calcLightDepths(int x0, int y0, int x1, int y1) {
        for(int x = x0; x < x0 + x1; ++x) {
            for(int z = y0; z < y0 + y1; ++z) {
                int oldDepth = this.lightDepths[x + z * CHUNK_SIZE];

                int y;
                for(y = CHUNK_SIZE - 1; y > 0 && !this.isLightBlocker(x, y, z); --y) {
                }

                this.lightDepths[x + z * CHUNK_SIZE] = y;
                if (oldDepth != y) {
                    int yl0 = oldDepth < y ? oldDepth : y;
                    int yl1 = oldDepth > y ? oldDepth : y;

                    for(int i = 0; i < this.level.getLevelListeners().size(); ++i) {
                        ((LevelListener)this.level.getLevelListeners().get(i)).lightColumnChanged(this, x, z, yl0, yl1);
                    }
                }
            }
        }

    }

    public boolean isLightBlocker(int x, int y, int z) {
        Tile tile = Tile.tiles[this.getTile(x, y, z)];
        return tile == null ? false : tile.blocksLight();
    }

    public boolean setTile(int x, int y, int z, int type) {
        if (x >= 0 && y >= 0 && z >= 0 && x < CHUNK_SIZE && y < CHUNK_SIZE && z < CHUNK_SIZE) {
            int i1 = (y * CHUNK_SIZE + z) * CHUNK_SIZE + x;
            if (type == this.blocks[i1]) {
                return false;
            } else {
                this.blocks[i1] = (byte)type;
                this.calcLightDepths(x, z, 1, 1);

                for(int i = 0; i < this.level.getLevelListeners().size(); ++i) {
                    ((LevelListener)this.level.getLevelListeners().get(i)).tileChanged(this, x, y, z);
                }

                return true;
            }
        } else {
            return false;
        }
    }

    public boolean isLit(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < CHUNK_SIZE && y < CHUNK_SIZE && z < CHUNK_SIZE) {
            return y >= this.lightDepths[x + z * CHUNK_SIZE];
        } else {
            return true;
        }
    }

    public int getTile(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < CHUNK_SIZE && y < CHUNK_SIZE && z < CHUNK_SIZE ? this.blocks[(y * CHUNK_SIZE + z) * CHUNK_SIZE + x] : 0;
    }

    public boolean isSolidTile(int x, int y, int z) {
        Tile tile = Tile.tiles[this.getTile(x, y, z)];
        return tile == null ? false : tile.isSolid();
    }

    public void tick() {
        this.unprocessed += CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE;
        int ticks = this.unprocessed / TILE_UPDATE_INTERVAL;
        this.unprocessed -= ticks * TILE_UPDATE_INTERVAL;

        for(int i = 0; i < ticks; ++i) {
            int x = this.random.nextInt(CHUNK_SIZE);
            int y = this.random.nextInt(CHUNK_SIZE);
            int z = this.random.nextInt(CHUNK_SIZE);
            Tile tile = Tile.tiles[this.getTile(x, y, z)];
            if (tile != null) {
                tile.tick(this.level, x, y, z, this.random);
            }
        }

    }
}
