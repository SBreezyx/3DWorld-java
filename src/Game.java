package ass2.src;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * The main Game Engine class.
 * This class handles the book-keeping of the textures, VBOs, scene tree, etc.
 * It also draws a pretty (?) background for ambience.
 *
 * @author malcolmr & Simon Haddad, z5061640
 */

@SuppressWarnings("serial")
public class Game extends JFrame implements GLEventListener {
    private Terrain myTerrain;
    private Avatar me;
    public static Texture[] tex = new Texture[9];
    private final int T_NIGHT = 7, T_DAY = 8;
    public static int[] buffIDs;

    /**
     * Load a level file and display it.
     *
     * @param args - The first argument is a level file in JSON format
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        Terrain terrain = LevelIO.load(new File(args[0]));
        Game game = new Game(terrain);
        game.run();
    }

    /**
     * Constructs a Game object.
     * "me" is the avatar that handles the camera and any user I/O.
     * buffIDs is created here to avoid excessive passing of objects around.
     *
     * @param terrain
     */
    public Game(Terrain terrain) {
        super("Assignment 2");
        myTerrain = terrain;
        me = new Avatar(terrain);
        buffIDs = new int[terrain.numCreepers() * 6];    //each creeper has 6 parts, each part has 1 vbo
    }

    /**
     * Run the game.
     */
    public void run() {
        GLProfile glp = GLProfile.getDefault();
        GLCapabilities caps = new GLCapabilities(glp);    //could use this later for rasterisation
        GLJPanel panel = new GLJPanel(caps);
        panel.addGLEventListener(this);

        // Add controller for user I/O.
        panel.addKeyListener(me.getCam());
        panel.addMouseListener(me.getCam());
        panel.addMouseMotionListener(me.getCam());

        // Add an animator to call 'display' at 60fps
        FPSAnimator animator = new FPSAnimator(60);
        animator.add(panel);
        animator.start();

        // Set JFrame properties.
        getContentPane().add(panel);
        setSize(800, 600);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Starts the display chain.
     * The world is a scene tree with the terrain being the parent of all trees, roads, etc.
     * Whereas the avatar is the standalone child of game, who in turn is the parent of the camera.
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        drawBG(gl);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
        me.draw(gl);        //draws avatar/camera
        setUpLighting(gl);    //light0 is a fixed light in the scene.
        myTerrain.draw(gl);
    }

    /**
     * Constructs an orthographic camera and sticks a textured image as the BG.
     *
     * @param gl
     */
    private void drawBG(GL2 gl) {
        if (me.isNight()) gl.glBindTexture(GL2.GL_TEXTURE_2D, tex[T_NIGHT].getTexID());
        else gl.glBindTexture(GL2.GL_TEXTURE_2D, tex[T_DAY].getTexID());
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0, 1, 1, 0, -1, 1);
        gl.glDisable(GL2.GL_DEPTH_TEST);
        gl.glDisable(GL2.GL_LIGHTING);
        gl.glDepthMask(false);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glBegin(GL2.GL_QUADS);
        {
            gl.glTexCoord2d(1, 1);
            gl.glVertex2d(0, 0);
            gl.glTexCoord2d(0, 1);
            gl.glVertex2d(1, 0);
            gl.glTexCoord2d(0, 0);
            gl.glVertex2d(1, 1);
            gl.glTexCoord2d(1, 0);
            gl.glVertex2d(0, 1);
        }
        gl.glEnd();

        gl.glDepthMask(true);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_DEPTH_TEST);

        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glPopMatrix();
    }

    /**
     * Turn on the lighting for the scene.
     * If it is night mode, then different light values are used ot emulate
     * night time. Similarly for day time.
     *
     * @param gl
     */
    private void setUpLighting(GL2 gl) {
        gl.glEnable(GL2.GL_LIGHT0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, myTerrain.getSunlight(), 0);
        if (me.isNight()) {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.1f, 0.1f, 0.1f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[]{0.05f, 0.05f, 0.05f, 1f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[]{0.969f, 0.891f, 1f, 1f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[]{0.117f, 0.119f, 0.161f, 1f}, 0);
        } else {
            gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, new float[]{1f, 1f, 1f, 1f}, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, new float[]{0.15f, 0.15f, 0.15f, 1f}, 0);
        }
    }

    /**
     * Cleanup the graphics window. Pretty much just destroys VBOs.
     */
    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        if (buffIDs.length > 0) gl.glDeleteBuffers(buffIDs.length, buffIDs, 0);
    }

    /**
     * Enables all the specialty things needed such as lighting, texturing, etc.
     * Also all the textures are generated.
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glEnable(GL2.GL_DEPTH_TEST);
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_NORMALIZE);
        gl.glEnable(GL2.GL_TEXTURE_2D);

        tex[0] = new Texture(gl, "media/textures/mc_snow.jpg");
        tex[1] = new Texture(gl, "media/textures/mc_wood.jpg");
        tex[2] = new Texture(gl, "media/textures/mc_trunk.jpg");
        tex[3] = new Texture(gl, "media/textures/mc_leaf.jpg");
        tex[4] = new Texture(gl, "media/textures/mc_road.jpg");
        tex[5] = new Texture(gl, "media/textures/mc_creeper_face.jpg");
        tex[6] = new Texture(gl, "media/textures/mc_creeper_body.jpg");
        tex[7] = new Texture(gl, "media/textures/night.jpg");
        tex[8] = new Texture(gl, "media/textures/day.jpg");
        gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_MODULATE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_COLOR_CONTROL, GL2.GL_SEPARATE_SPECULAR_COLOR);
        if (buffIDs.length != 0) gl.glGenBuffers(buffIDs.length, buffIDs, 0);
    }

    /**
     * Repositions the perspective when the window is resized.
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU glu = new GLU();
        glu.gluPerspective(60.0, (float) w / (float) h, 1.0, 20.0);
    }
}