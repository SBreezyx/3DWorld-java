package ass2.src;

import com.jogamp.opengl.GL2;

import java.util.HashMap;
import java.util.Stack;


/**
 * The class implements a basic rewrite L-System.
 * The formal grammar herein used is as follows:
 * f : create a new branch.
 * l : create a new leaf
 * [ & ]: create a local branch.
 * + & -: rotate the branch right/left in x-axis.
 * ^ & v: rotate the branch up/down in y-axis.
 * < & >: rotate the branch left/right in z-axis.
 * Probably my favourite part, the L-System tree class.
 * This class uses the Lindenmayer system to draw beautifully fractal trees.
 *
 * @author Simon Haddad, z5061640
 * Credit to http://www.geekyblogger.com/2008/04/tree-and-l-system.html for the grammars for the trees
 */
public class LTree extends Tree {
    private int iter;
    private double phi, initRad, bRadRed, initLen;
    private String initSys;
    HashMap<Character, String> prodRules = new HashMap<Character, String>();


    public LTree(int iterations, int type, double x, double y, double z) {
        super(x, y, z);
        iter = iterations;
        switch (type) {        //The following defines the mappings for the production rules.
            case 1:
                prodRules.put('A', "f[++Al][--Al]>>>A");
                prodRules.put('B', "");
                phi = 25;
                initRad = 0.02;
                bRadRed = 0.0015;
                initLen = 0.15;
                initSys = "fffffA";
                break;
            case 2:
                prodRules.put('A', "f[^Bl]>>[^Bl]>>A");
                prodRules.put('B', "f[-Bl]B");
                phi = 30;
                initRad = 0.01;
                bRadRed = 0.001;
                initLen = 0.16;
                initSys = "fA";
                break;
            case 3:
                prodRules.put('A', "^fB>>>B>>>>>B");
                prodRules.put('B', "[^^f>>>>>>A]");
                phi = 15;
                initRad = 0.02;
                bRadRed = 0.0015;
                initLen = 0.15;
                initSys = "fA";
        }
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        double[] pos = getPosition();
        gl.glTranslated(pos[0], pos[1], pos[2]);
        gl.glScaled(4, 4, 4);
        double rad = initRad, len = initLen;
        String sys = initSys;

        for (int i = 0; i < iter; ++i) {    //build the final string
            StringBuilder newSys = new StringBuilder();
            for (char c : sys.toCharArray()) {
                if (prodRules.containsKey(c)) newSys.append(prodRules.get(c));
                else newSys.append(c);
            }
            sys = newSys.toString();
        }

        Stack<Double> oldRads = new Stack<Double>();
        for (char c : sys.toCharArray()) {    //draw the tree!
            switch (c) {
                case 'f':
                    if (rad > 0) {
                        drawTree(gl, rad, len);
                        gl.glTranslated(0, len, 0);
                        rad -= bRadRed;
                    }
                    break;
                case 'l':
                    createLeaf(gl);
                    break;
                case '[':
                    gl.glPushMatrix();
                    oldRads.push(rad);
                    break;
                case ']':
                    gl.glPopMatrix();
                    rad = oldRads.pop();
                    break;
                case '+':
                    gl.glRotated(phi, 0, 0, 1);
                    break;
                case '-':
                    gl.glRotated(-phi, 0, 0, 1);
                    break;
                case '^':
                    gl.glRotated(phi, 1, 0, 0);
                    break;
                case 'v':
                    gl.glRotated(-phi, 1, 0, 0);
                    break;
                case '<':
                    gl.glRotated(phi, 0, 1, 0);
                    break;
                case '>':
                    gl.glRotated(-phi, 0, 1, 0);
                    break;
            }
        }
        gl.glPopMatrix();
    }

    /**
     * Dirtily draw a 'leaf'-- aka a triangle.
     *
     * @param gl
     */
    private void createLeaf(GL2 gl) {
        gl.glPushMatrix();
        bindLeadTex(gl);
        gl.glScaled(0.04, 0.04, 0.04);
        gl.glBegin(GL2.GL_TRIANGLES);
        {
            gl.glTexCoord2d(1, 1);
            gl.glVertex3d(1, 1, 1);
            gl.glTexCoord2d(1, 0);
            gl.glVertex3d(1, 0, 0);
            gl.glTexCoord2d(0, 0);
            gl.glVertex3d(0, 0, 1);
        }
        gl.glEnd();
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glPopMatrix();
    }
}
