package ass2.src;

import com.jogamp.opengl.GL2;

/**
 * A cheeky class used for the "others" part of this assignment.
 * A creeper may or may not have been 'borrowed' from the ever popular yet hated game
 * Minecraft. All rights reserved.
 * A creeper is made up of rectangular prisms, 4 for the feet, one for the body, and one
 * for the head.
 *
 * @author Simon Haddad, z5061640
 */
public class Creeper {
    private double[] location;
    private int shader = -1;    //initial dummy value to lazily initialise the shader
    private RectangularPrism[] feet = new RectangularPrism[4];
    private RectangularPrism torso, head;
    private int T_HEAD = 5, T_BODY = 6;    //texture macros

    public Creeper(double x, double y, double z, int vboIX) {
        location = new double[]{x, y, z};// vv vertex information.
        head = new RectangularPrism(0.35f, 0.35f, 0.35f, new double[]{-0.0625, 1.125, -0.0625}, vboIX * 6);
        torso = new RectangularPrism(0.25f, 1f, 0.25f, new double[]{0, 0.125, 0}, vboIX * 6 + 1);
        feet[0] = new RectangularPrism(0.25f, 0.18f, 0.25f, new double[]{0.14, 0, 0.14}, vboIX * 6 + 2);
        feet[1] = new RectangularPrism(0.25f, 0.18f, 0.25f, new double[]{-0.14, 0, 0.14}, vboIX * 6 + 3);
        feet[2] = new RectangularPrism(0.25f, 0.18f, 0.25f, new double[]{-0.14, 0, -0.14}, vboIX * 6 + 4);
        feet[3] = new RectangularPrism(0.25f, 0.18f, 0.25f, new double[]{0.14, 0, -0.14}, vboIX * 6 + 5);
    }

    public void draw(GL2 gl) {
        if (shader == -1) {
            try {
                shader = Shader.initShaders(gl, "media/shaders/creeper_vert.glsl", "media/shaders/creeper_frag.glsl");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        gl.glPushMatrix();
        gl.glTranslated(location[0], location[1], location[2]);
        gl.glScaled(1.25, 1.25, 1.25);
        head.draw(gl, shader, T_HEAD);
        torso.draw(gl, shader, T_BODY);
        for (RectangularPrism foot : feet) foot.draw(gl, shader, T_BODY);
        gl.glPopMatrix();
    }
}
