package ass2.src;

import com.jogamp.opengl.GL2;

/**
 * The fabled tree class.
 * This class essentially draws a basic as tree as a cylinder and cone.
 *
 * @author malcolmr & Simon Haddad, z5061640
 */
public class Tree {
    private double[] myPos;
    private double h = 1.5;    //height of trunk
    private double r = 0.2;    //radius of trunk
    private final short T_WOOD = 1, T_TRUNK = 2, T_LEAF = 3;    //texture macros

    public Tree(double x, double y, double z) {
        myPos = new double[]{x, y, z};
    }

    public double[] getPosition() {
        return myPos;
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        if (myPos[1] - Math.floor(myPos[1]) > 0) myPos[1] = Math.floor(myPos[1]) + 1;
        gl.glTranslated(myPos[0], myPos[1], myPos[2]);

        //draws trunk, bottom and top and all.
        drawTree(gl, r, h);

        double phi = Math.PI / 16;
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[]{0.65f, 0.25f, 0.35f, 1f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[]{0.1f, 0.1f, 0.1f, 1f}, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_LEAF].getTexID());

        //draws the lower circle of the "leaves". It stays black to lazily emulate shadows.
        gl.glTranslated(0, h, 0);
        gl.glBegin(GL2.GL_POLYGON);
        {
            gl.glNormal3d(0, -1, 0);
            for (int i = 0; i <= 32; ++i) gl.glVertex3d(4 * r * Math.cos(i * phi), 0, 4 * r * Math.sin(i * phi));

        }
        gl.glEnd();

        //draws the "leaves".
        gl.glBegin(GL2.GL_POLYGON);
        {
            gl.glNormal3d(0, 1, 0);
            gl.glTexCoord2d(0.5, 0.5);
            gl.glVertex3d(0, h, 0);
            for (int i = 32; i >= 0; --i) {
                gl.glTexCoord2d(0.5 + 2.25 * Math.cos(i * phi), 0.5 + 2.25 * Math.sin(i * phi));
                gl.glVertex3d(4 * r * Math.cos(i * phi), 0, 4 * r * Math.sin(i * phi));
            }
        }
        gl.glEnd();
        gl.glPopMatrix();
    }

    public void drawTree(GL2 gl, double r, double h) {
        double phi = Math.PI / 16;    //2pi/32 == pi/16
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[]{0.5f, 0.5f, 0.5f, 1f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[]{1f, 1f, 1f, 1f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[]{0.05f, 0.05f, 0.05f, 1f}, 0);

        gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_TRUNK].getTexID());
        //bottom of trunk
        gl.glBegin(GL2.GL_POLYGON);
        {
            gl.glNormal3d(0, -1, 0);
            for (int i = 0; i <= 32; ++i) {
                gl.glTexCoord2d(0.5 + 0.5 * Math.cos(i * phi), 0.5 + 0.5 * Math.sin(i * phi));
                gl.glVertex3d(r * Math.cos(i * phi), 0, r * Math.sin(i * phi));
            }
        }
        gl.glEnd();

        gl.glBegin(GL2.GL_POLYGON);
        {    //top of trunk
            gl.glNormal3d(0, -1, 0);
            for (int i = 0; i <= 32; ++i) {
                gl.glTexCoord2d(0.5 + 0.5 * Math.cos(i * phi), 0.5 + 0.5 * Math.sin(i * phi));
                gl.glVertex3d(r * Math.cos(i * phi), h, r * Math.sin(i * phi));
            }
        }
        gl.glEnd();

        gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_WOOD].getTexID());
        gl.glBegin(GL2.GL_QUADS);
        {    //the skin of the trunk
            for (int i = 0; i <= 32; ++i) {
                double x0 = r * Math.cos(i * phi), z0 = r * Math.sin(i * phi);
                double x1 = r * Math.cos(((i + 1) % 32) * phi), z1 = r * Math.sin(((i + 1) % 32) * phi);

                gl.glNormal3dv(MathUtil.normalise(new double[]{x0, 0, z0}), 0);
                gl.glTexCoord2d(i * phi / 2, 0);
                gl.glVertex3d(x0, h, z0); //upper left
                gl.glTexCoord2d(((i + 1) % 32) * phi / 2, 0);
                gl.glVertex3d(x1, h, z1); //upper right

                gl.glNormal3dv(MathUtil.normalise(new double[]{x1, 0, z1}), 0);
                gl.glTexCoord2d(((i + 1) % 32) * phi / 2, 2);
                gl.glVertex3d(x1, 0, z1); //lower right
                gl.glTexCoord2d(i * phi / 2, 2);
                gl.glVertex3d(x0, 0, z0);    //lower left

            }
        }
        gl.glEnd();
    }

    /**
     * Convenience function for the L-Tree. Grab the texture macro for leaves.
     *
     * @param gl
     */
    public void bindLeadTex(GL2 gl) {
        gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_LEAF].getTexID());
    }
}