//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft.level;

import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.phys.AABB;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static com.mojang.minecraft.level.Chunk.CHUNK_SIZE;

public class Level {
    public final int width;
    public final int height;
    public final int depth;
    private Chunk[] chunks;
    private ArrayList<LevelListener> levelListeners = new ArrayList();

    public Level(int w, int h, int d) {
        this.width = w;
        this.height = h;
        this.depth = d;
        int xs = this.width / CHUNK_SIZE;
        int ys = this.height / CHUNK_SIZE;
        int zs = this.depth / CHUNK_SIZE;
        this.chunks = new Chunk[xs * ys * zs];
        for(int x = 0; x < xs; x++) {
            for(int y = 0; y < zs; y++) {
                for(int z = 0; z < ys; z++) {
                    int i = (y * ys + z) * xs + x;
                    this.chunks[i] = new Chunk(this, x, y, z);
                }
            }
        }
        boolean mapLoaded = this.load();
        byte[] blocks;
        if (!mapLoaded) {
            blocks = (new LevelGen(w, h, d)).generateMap();
            for (int x = 0; x < xs; x++) {
                for (int y = 0; y < zs; y++) {
                    for (int z = 0; z < ys; z++) {
                        int i = (y * ys + z) * xs + x;
                        byte[] chunkBlocks = new byte[CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE];
                        for (int bx = 0; bx < CHUNK_SIZE; bx++) {
                            for (int by = 0; by < CHUNK_SIZE; by++) {
                                for (int bz = 0; bz < CHUNK_SIZE; bz++) {
                                    int worldX = x * CHUNK_SIZE + bx;
                                    int worldY = y * CHUNK_SIZE + by;
                                    int worldZ = z * CHUNK_SIZE + bz;
                                    int worldIndex = (worldY * this.height + worldZ) * this.width + worldX;
                                    int chunkIndex = (by * CHUNK_SIZE + bz) * CHUNK_SIZE + bx;
                                    chunkBlocks[chunkIndex] = blocks[worldIndex];
                                }
                            }
                        }
                        this.chunks[i].blocks = chunkBlocks;
                    }
                }
            }
        }

        this.calcLightDepths(0, 0, w, h);
    }

    public boolean load() {
        try {
            File file = new File("level.dat");
            if (!file.exists()) {
                return false;
            }
            DataInputStream dis = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
            int savedWidth = dis.readInt();
            int savedHeight = dis.readInt();
            int savedDepth = dis.readInt();
            if (savedWidth != this.width || savedHeight != this.height || savedDepth != this.depth) {
                dis.close();
                return false;
            }
            for (Chunk chunk : this.chunks) {
                dis.readFully(chunk.blocks);
            }
            this.calcLightDepths(0, 0, this.width, this.height);
            for (int i = 0; i < this.levelListeners.size(); ++i) {
                ((LevelListener)this.levelListeners.get(i)).allChanged();
            }
            dis.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void save() {
        try {
            DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(new File("level.dat"))));
            dos.writeInt(this.width);
            dos.writeInt(this.height);
            dos.writeInt(this.depth);
            for (Chunk chunk : this.chunks) {
                dos.write(chunk.blocks);
            }
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void calcLightDepths(int x0, int y0, int x1, int y1) {
        System.out.printf("Calculating light depths for chunk %d %d %d %d\n", x0, y0, x1, y1);
        int coordsX0 = x0 / CHUNK_SIZE;
        int coordsY0 = y0 / CHUNK_SIZE;
        int coordsX1 = x1 / CHUNK_SIZE;
        int coordsY1 = y1 / CHUNK_SIZE;
        List<Chunk> list = Arrays.stream(this.chunks).toList();
        list = list.stream().filter(chunk -> chunk.chunkX >= coordsX0 && chunk.chunkX <= coordsX1 && chunk.chunkZ >= coordsY0 && chunk.chunkZ <= coordsY1).toList();
        for (Chunk chunk : list) {
            chunk.calcLightDepths(x0, y0, CHUNK_SIZE, CHUNK_SIZE);
        }
    }

    public void addListener(LevelListener levelListener) {
        this.levelListeners.add(levelListener);
    }

    public void removeListener(LevelListener levelListener) {
        this.levelListeners.remove(levelListener);
    }

    public ArrayList<AABB> getCubes(AABB aABB) {
        ArrayList<AABB> aABBs = new ArrayList();

        int x0 = (int)aABB.x0;
        int x1 = (int)(aABB.x1 + 1.0F);
        int y0 = (int)aABB.y0;
        int y1 = (int)(aABB.y1 + 1.0F);
        int z0 = (int)aABB.z0;
        int z1 = (int)(aABB.z1 + 1.0F);
        if (x0 < 0) {
            x0 = 0;
        }

        if (y0 < 0) {
            y0 = 0;
        }

        if (z0 < 0) {
            z0 = 0;
        }

        if (x1 > this.width) {
            x1 = this.width;
        }

        if (y1 > this.depth) {
            y1 = this.depth;
        }

        if (z1 > this.height) {
            z1 = this.height;
        }

        for(int x = x0; x < x1; ++x) {
            for(int y = y0; y < y1; ++y) {
                for(int z = z0; z < z1; ++z) {
                    Tile tile = Tile.tiles[this.getTile(x, y, z)];
                    if (tile != null) {
                        AABB aabb = tile.getAABB(x, y, z);
                        if (aabb != null) {
                            aABBs.add(aabb);
                        }
                    }
                }
            }
        }
        return aABBs;
    }

    public boolean setTile(int x, int y, int z, int type) {
        if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
            int chunkX = x / CHUNK_SIZE;
            int chunkY = y / CHUNK_SIZE;
            int chunkZ = z / CHUNK_SIZE;
            int xs = width / CHUNK_SIZE;
            int ys = height / CHUNK_SIZE;
            int chunkIndex = (chunkY * ys + chunkZ) * xs + chunkX;
            Chunk chunk = this.chunks[chunkIndex];
            return chunk.setTile(x - chunkX * CHUNK_SIZE, y - chunkY * CHUNK_SIZE, z - chunkZ * CHUNK_SIZE, type);
        }
        return false;
    }

    public boolean isLit(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
            int chunkX = x / CHUNK_SIZE;
            int chunkY = y / CHUNK_SIZE;
            int chunkZ = z / CHUNK_SIZE;
            int xs = width / CHUNK_SIZE;
            int zs = height / CHUNK_SIZE;
            int chunkIndex = (chunkY * zs + chunkZ) * xs + chunkX;
            Chunk chunk = this.chunks[chunkIndex];
            return chunk.isLit(x - chunkX * CHUNK_SIZE, y - chunkY * CHUNK_SIZE, z - chunkZ * CHUNK_SIZE);
        }
        return true;
    }

    public int getTile(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
            int chunkX = x / CHUNK_SIZE;
            int chunkY = y / CHUNK_SIZE;
            int chunkZ = z / CHUNK_SIZE;
            int xs = width / CHUNK_SIZE;
            int zs = height / CHUNK_SIZE;
            int chunkIndex = (chunkY * zs + chunkZ) * xs + chunkX;
            Chunk chunk = this.chunks[chunkIndex];
            return chunk.getTile(x - chunkX * CHUNK_SIZE, y - chunkY * CHUNK_SIZE, z - chunkZ * CHUNK_SIZE);
        }
        return 0;
    }

    public boolean isSolidTile(int x, int y, int z) {
        if (x >= 0 && y >= 0 && z >= 0 && x < this.width && y < this.depth && z < this.height) {
            int chunkX = x / CHUNK_SIZE;
            int chunkY = y / CHUNK_SIZE;
            int chunkZ = z / CHUNK_SIZE;
            int xs = width / CHUNK_SIZE;
            int zs = height / CHUNK_SIZE;
            int chunkIndex = (chunkY * zs + chunkZ) * xs + chunkX;
            Chunk chunk = this.chunks[chunkIndex];
            return chunk.isSolidTile(x - chunkX * CHUNK_SIZE, y - chunkY * CHUNK_SIZE, z - chunkZ * CHUNK_SIZE);
        }
        return false;
    }

    public void tick() {
        for (Chunk chunk : this.chunks) {
            chunk.tick();
        }
    }

    public List<LevelListener> getLevelListeners() {
        return this.levelListeners;
    }
}