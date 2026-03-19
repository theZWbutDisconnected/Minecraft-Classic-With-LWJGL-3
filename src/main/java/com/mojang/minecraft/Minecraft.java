//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.mojang.minecraft;

import com.mojang.minecraft.character.Zombie;
import com.mojang.minecraft.gui.Font;
import com.mojang.minecraft.level.Chunk;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.level.LevelRenderer;
import com.mojang.minecraft.level.tile.Tile;
import com.mojang.minecraft.particle.ParticleEngine;
import com.mojang.minecraft.phys.AABB;
import com.mojang.minecraft.renderer.Frustum;
import com.mojang.minecraft.renderer.Tesselator;
import com.mojang.minecraft.renderer.Textures;

import java.awt.Canvas;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Minecraft implements Runnable {
    public static final String VERSION_STRING = "0.0.11a";
    private static Minecraft minecraft;
    private final FloatBuffer fogColor0 = BufferUtils.createFloatBuffer(4);
    private final FloatBuffer fogColor1 = BufferUtils.createFloatBuffer(4);
    private final IntBuffer viewportBuffer = BufferUtils.createIntBuffer(16);
    private final IntBuffer selectBuffer = BufferUtils.createIntBuffer(2000);
    private Canvas parent;
    private boolean fullscreen = false;
    private int width;
    private int height;
    private Timer timer = new Timer(20.0F);
    private Level level;
    private LevelRenderer levelRenderer;
    private Player player;
    private int paintTexture = 1;
    private ParticleEngine particleEngine;
    private ArrayList<Entity> entities = new ArrayList();
    private int yMouseAxis = -1;
    private Font font;
    private int editMode = 0;
    private volatile boolean running = false;
    private String fpsString = "";
    private boolean mouseGrabbed = false;
    private HitResult hitResult = null;
    private float xo;
    private float yo;
    public Options options;
    public Textures textures;
    public volatile boolean pause = false;
    long window;
    FloatBuffer lb = BufferUtils.createFloatBuffer(16);

    public static Minecraft getInstance() {
        return minecraft;
    }

    public Minecraft(Canvas parent, int width, int height, boolean fullscreen) {
        this.parent = parent;
        this.width = width;
        this.height = height;
        this.fullscreen = fullscreen;
        this.options = new Options(this);
        this.textures = new Textures();
    }

    public void init() throws IOException {
        int col0 = 16710650;
        int col1 = 920330;
        float fr = 0.5F;
        float fg = 0.8F;
        float fb = 1.0F;
        this.fogColor0.put(new float[]{(float)(col0 >> 16 & 255) / 255.0F, (float)(col0 >> 8 & 255) / 255.0F, (float)(col0 & 255) / 255.0F, 1.0F});
        this.fogColor0.flip();
        this.fogColor1.put(new float[]{(float)(col1 >> 16 & 255) / 255.0F, (float)(col1 >> 8 & 255) / 255.0F, (float)(col1 & 255) / 255.0F, 1.0F});
        this.fogColor1.flip();

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "Hello World!", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");
        handleCallbackSet(window);
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }
        glfwMakeContextCurrent(window);
        glfwSwapInterval(0);
        glfwShowWindow(window);

        GL.createCapabilities();

        this.checkGlError("Pre startup");
        glEnable(3553);
        glShadeModel(7425);
        glClearColor(fr, fg, fb, 1.0f);
        glClearDepth((double)1.0F);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, 0.0F);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glMatrixMode(GL_MODELVIEW);
        this.checkGlError("Startup");
        this.level = new Level(256, 256, 64);
        this.levelRenderer = new LevelRenderer(this.level, this.textures);
        this.player = new Player(this.level);
        this.particleEngine = new ParticleEngine(this.level, this.textures);
        this.font = new Font("/default.gif", this.textures);

        for(int i = 0; i < 10; ++i) {
            Zombie zombie = new Zombie(this.level, this.textures, 128.0F, 0.0F, 128.0F);
            zombie.resetPos();
            this.entities.add(zombie);
        }

        IntBuffer imgData = BufferUtils.createIntBuffer(256);
        imgData.clear().limit(256);
        this.grabMouse();

        this.checkGlError("Post startup");
    }

    private void handleCallbackSet(long handle) {
        glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (key == options.keySave.key() && action == GLFW_PRESS)
                this.level.save();
            if (key == options.keyReset.key() && action == GLFW_PRESS)
                this.player.resetPos();
            if (key > GLFW_KEY_0 && key < GLFW_KEY_7 && action == GLFW_PRESS)
                this.paintTexture = key - GLFW_KEY_0;
            if (key == GLFW_KEY_G && action == GLFW_PRESS)
                this.entities.add(new Zombie(this.level, this.textures, this.player.x, this.player.y, this.player.z));
        });

        glfwSetFramebufferSizeCallback(handle, (window, width, height) -> {
            this.width = width;
            this.height = height;
        });

        glfwSetWindowFocusCallback(window, (windowHandle, focused) -> {
            if (!focused && this.mouseGrabbed) {
                this.releaseMouse();
            } else if (focused && !this.mouseGrabbed) {
            }
        });

        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            float xOffset = (float) xpos - xo;
            float yOffset = (float) ypos - yo;
            this.player.turn(xOffset, yOffset * (float)this.yMouseAxis);
            xo = (float) xpos;
            yo = (float) ypos;
        });

        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            boolean state = action == GLFW_PRESS;
            if (!this.mouseGrabbed && state) {
                this.grabMouse();
            } else {
                if (button == GLFW_MOUSE_BUTTON_1 && state) {
                    this.handleMouseClick();
                }
                if (button == GLFW_MOUSE_BUTTON_2 && state) {
                    this.editMode = (this.editMode + 1) % 2;
                }
            }
        });
    }

    private void checkGlError(String string) {
        int errorCode = glGetError();
        if (errorCode != 0) {
            String errorString = "OpenGL Error: " + errorCode;
            System.out.println("########## GL ERROR ##########");
            System.out.println("@ " + string);
            System.out.println(errorCode + ": " + errorString);
            System.exit(0);
        }

    }

    public void destroy() {
        try {
            this.level.save();
        } catch (Exception ignored) {
        }
    }

    public void run() {
        this.running = true;

        try {
            this.init();
        } catch (Exception e) {
            JOptionPane.showMessageDialog((Component)null, e.toString(), "Failed to start Minecraft", 0);
            return;
        }

        long lastTime = System.currentTimeMillis();
        int frames = 0;

        try {
            while(this.running) {
                if (this.pause) {
                    Thread.sleep(100L);
                } else {
                    if (this.parent == null && glfwWindowShouldClose(window)) {
                        this.stop();
                    }

                    this.timer.advanceTime();

                    for(int i = 0; i < this.timer.ticks; ++i) {
                        this.tick();
                    }

                    this.checkGlError("Pre render");
                    this.render(this.timer.a);
                    this.checkGlError("Post render");
                    ++frames;

                    while(System.currentTimeMillis() >= lastTime + 1000L) {
                        this.fpsString = frames + " fps, " + Chunk.updates + " chunk updates";
                        Chunk.updates = 0;
                        lastTime += 1000L;
                        frames = 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.destroy();
        }

    }

    public void stop() {
        this.running = false;
    }

    public void grabMouse() {
        if (!this.mouseGrabbed) {
            this.mouseGrabbed = true;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
    }

    public void releaseMouse() {
        if (this.mouseGrabbed) {
            this.mouseGrabbed = false;
            glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        }
    }

    private void handleMouseClick() {
        if (this.editMode == 0) {
            if (this.hitResult != null) {
                Tile oldTile = Tile.tiles[this.level.getTile(this.hitResult.x, this.hitResult.y, this.hitResult.z)];
                boolean changed = this.level.setTile(this.hitResult.x, this.hitResult.y, this.hitResult.z, 0);
                if (oldTile != null && changed) {
                    oldTile.destroy(this.level, this.hitResult.x, this.hitResult.y, this.hitResult.z, this.particleEngine);
                }
            }
        } else if (this.hitResult != null) {
            int x = this.hitResult.x;
            int y = this.hitResult.y;
            int z = this.hitResult.z;
            if (this.hitResult.f == 0) {
                --y;
            }

            if (this.hitResult.f == 1) {
                ++y;
            }

            if (this.hitResult.f == 2) {
                --z;
            }

            if (this.hitResult.f == 3) {
                ++z;
            }

            if (this.hitResult.f == 4) {
                --x;
            }

            if (this.hitResult.f == 5) {
                ++x;
            }

            AABB aabb = Tile.tiles[this.paintTexture].getAABB(x, y, z);
            if (aabb == null || this.isFree(aabb)) {
                this.level.setTile(x, y, z, this.paintTexture);
            }
        }

    }

    public void tick() {
        this.level.tick();
        this.particleEngine.tick();

        for(int i = 0; i < this.entities.size(); ++i) {
            ((Entity)this.entities.get(i)).tick();
            if (((Entity)this.entities.get(i)).removed) {
                this.entities.remove(i--);
            }
        }

        this.player.tick();
    }

    private boolean isFree(AABB aabb) {
        if (this.player.bb.intersects(aabb)) {
            return false;
        } else {
            for(int i = 0; i < this.entities.size(); ++i) {
                if (((Entity)this.entities.get(i)).bb.intersects(aabb)) {
                    return false;
                }
            }

            return true;
        }
    }

    private void moveCameraToPlayer(float a) {
        glTranslatef(0.0F, 0.0F, -0.3F);
        glRotatef(this.player.xRot, 1.0F, 0.0F, 0.0F);
        glRotatef(this.player.yRot, 0.0F, 1.0F, 0.0F);
        float x = this.player.xo + (this.player.x - this.player.xo) * a;
        float y = this.player.yo + (this.player.y - this.player.yo) * a;
        float z = this.player.zo + (this.player.z - this.player.zo) * a;
        glTranslatef(-x, -y, -z);
    }

    private void setupCamera(float a) {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        float fov = 70.0F;
        float aspect = (float)this.width / (float)this.height;
        float near = 0.05F;
        float far = 1000.0F;

        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float)Math.toRadians(fov), aspect, near, far);
        applyMatrix4f(projectionMatrix);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        this.moveCameraToPlayer(a);
    }

    private void setupPickCamera(float a, int x, int y) {
        glMatrixMode(5889);
        glLoadIdentity();
        this.viewportBuffer.clear();
        glGetIntegerv(2978, this.viewportBuffer);
        this.viewportBuffer.flip();
        this.viewportBuffer.limit(16);

        float fov = 70.0F;
        float aspect = (float)this.width / (float)this.height;
        float near = 0.05F;
        float far = 1000.0F;

        int[] viewport = new int[4];
        glGetIntegerv(GL_VIEWPORT, viewport);
        Matrix4f pickMatrix = new Matrix4f();
        pickMatrix.pick((float)x, (float)y, 5.0F, 5.0F, viewport);
        applyMatrix4f(pickMatrix);
        Matrix4f projectionMatrix = new Matrix4f();
        projectionMatrix.setPerspective((float)Math.toRadians(fov), aspect, near, far);
        applyMatrix4f(projectionMatrix);
        glMatrixMode(5888);
        glLoadIdentity();
        this.moveCameraToPlayer(a);
    }

    private void pick(float a) {
        this.selectBuffer.clear();
        glSelectBuffer(this.selectBuffer);
        glRenderMode(GL_SELECT);
        this.setupPickCamera(a, this.width / 2, this.height / 2);
        this.levelRenderer.pick(this.player, Frustum.getFrustum());
        int hits = glRenderMode(GL_RENDER);
        this.selectBuffer.flip();
        this.selectBuffer.limit(this.selectBuffer.capacity());
        long closest = 0L;
        int[] names = new int[10];
        int hitNameCount = 0;

        for(int i = 0; i < hits; ++i) {
            int nameCount = this.selectBuffer.get();
            long minZ = (long)this.selectBuffer.get();
            this.selectBuffer.get();
            if (minZ >= closest && i != 0) {
                for(int j = 0; j < nameCount; ++j) {
                    this.selectBuffer.get();
                }
            } else {
                closest = minZ;
                hitNameCount = nameCount;

                for(int j = 0; j < nameCount; ++j) {
                    names[j] = this.selectBuffer.get();
                }
            }
        }

        if (hitNameCount > 0) {
            this.hitResult = new HitResult(names[0], names[1], names[2], names[3], names[4]);
        } else {
            this.hitResult = null;
        }

    }

    public void render(float a) {
        glViewport(0, 0, this.width, this.height);
        this.checkGlError("Set viewport");
        this.pick(a);
        this.checkGlError("Picked");
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        this.setupCamera(a);
        this.checkGlError("Set up camera");
        glEnable(GL_CULL_FACE);
        Frustum frustum = Frustum.getFrustum();
        this.levelRenderer.updateDirtyChunks(this.player);
        this.checkGlError("Update chunks");
        this.setupFog(0);
        glEnable(GL_FOG);
        this.levelRenderer.render(this.player, 0);
        this.checkGlError("Rendered level");

        for(int i = 0; i < this.entities.size(); ++i) {
            Entity entity = (Entity)this.entities.get(i);
            if (entity.isLit() && frustum.isVisible(entity.bb)) {
                ((Entity)this.entities.get(i)).render(a);
            }
        }

        this.checkGlError("Rendered entities");
        this.particleEngine.render(this.player, a, 0);
        this.checkGlError("Rendered particles");
        this.setupFog(1);
        this.levelRenderer.render(this.player, 1);

        for(int i = 0; i < this.entities.size(); ++i) {
            Entity zombie = (Entity)this.entities.get(i);
            if (!zombie.isLit() && frustum.isVisible(zombie.bb)) {
                ((Entity)this.entities.get(i)).render(a);
            }
        }

        this.particleEngine.render(this.player, a, 1);
        glDisable(2896);
        glDisable(3553);
        glDisable(2912);
        this.checkGlError("Rendered rest");
        if (this.hitResult != null) {
            glDisable(3008);
            this.levelRenderer.renderHit(this.hitResult, this.editMode, this.paintTexture);
            glEnable(3008);
        }

        this.checkGlError("Rendered hit");
        this.drawGui(a);
        this.checkGlError("Rendered gui");
        glfwPollEvents();
        glfwSwapBuffers(window);
    }

    private void drawGui(float a) {
        int screenWidth = this.width * 240 / this.height;
        int screenHeight = this.height * 240 / this.height;
        glClear(256);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();

        Matrix4f orthoMatrix = new Matrix4f();
        orthoMatrix.setOrtho(0.0F, (float)screenWidth, (float)screenHeight, 0.0F, 100.0F, 300.0F);
        applyMatrix4f(orthoMatrix);

        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(0.0F, 0.0F, -200.0F);
        this.checkGlError("GUI: Init");
        glPushMatrix();
        glTranslatef((float)(screenWidth - 16), 16.0F, 0.0F);
        Tesselator t = Tesselator.instance;
        glScalef(16.0F, 16.0F, 16.0F);
        glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
        glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
        glTranslatef(-1.5F, 0.5F, -0.5F);
        glScalef(-1.0F, -1.0F, 1.0F);
        int id = this.textures.loadTexture("/terrain.png", 9728);
        glBindTexture(GL_TEXTURE_2D, id);
        glEnable(GL_TEXTURE_2D);
        t.init();
        Tile.tiles[this.paintTexture].render(t, this.level, 0, -2, 0, 0);
        t.flush();
        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
        this.checkGlError("GUI: Draw selected");
        this.font.drawShadow(VERSION_STRING, 2, 2, 16777215);
        this.font.drawShadow(this.fpsString, 2, 12, 16777215);
        this.checkGlError("GUI: Draw text");
        int wc = screenWidth / 2;
        int hc = screenHeight / 2;
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        t.init();
        t.vertex((float)(wc + 1), (float)(hc - 4), 0.0F);
        t.vertex((float)(wc - 0), (float)(hc - 4), 0.0F);
        t.vertex((float)(wc - 0), (float)(hc + 5), 0.0F);
        t.vertex((float)(wc + 1), (float)(hc + 5), 0.0F);
        t.vertex((float)(wc + 5), (float)(hc - 0), 0.0F);
        t.vertex((float)(wc - 4), (float)(hc - 0), 0.0F);
        t.vertex((float)(wc - 4), (float)(hc + 1), 0.0F);
        t.vertex((float)(wc + 5), (float)(hc + 1), 0.0F);
        t.flush();
        this.checkGlError("GUI: Draw crosshair");
    }

    private void setupFog(int i) {
        if (i == 0) {
            glFogi(2917, 2048);
            glFogf(2914, 0.001F);
            glFogfv(2918, this.fogColor0);
            glDisable(2896);
        } else if (i == 1) {
            glFogi(2917, 2048);
            glFogf(2914, 0.01F);
            glFogfv(2918, this.fogColor1);
            glEnable(2896);
            glEnable(2903);
            float br = 0.6F;
            glLightModelfv(2899, this.getBuffer(br, br, br, 1.0F));
        }
    }

    private FloatBuffer getBuffer(float a, float b, float c, float d) {
        this.lb.clear();
        this.lb.put(a).put(b).put(c).put(d);
        this.lb.flip();
        return this.lb;
    }

    public static void checkError() {
        int e = glGetError();
        if (e != 0) {
            throw new IllegalStateException("OpenGL Error: " + e);
        }
    }

    private void applyMatrix4f(Matrix4f matrix) {
        float[] matrixArray = new float[16];
        matrix.get(matrixArray);
        glMultMatrixf(matrixArray);
    }

    public static void main(String[] args) throws IOException {
        Minecraft.minecraft = new Minecraft((Canvas)null, 854, 480, false);
        (new Thread(minecraft)).start();
    }
}