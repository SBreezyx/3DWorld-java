package ass2.src;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.gl2.GLUT;

/**
 * The Avatar.
 * Sorry James Cmaeron, but this class isn' as good as your movie.
 * Still, this class aims to encapsulate the player, though not as cleanly
 * as actual player classes.
 * This sets where to look at, and if we are in third-person draws "us", along with
 * our nifty little torch if it's night time.
 *
 * @author Simon Haddad, z5061640
 */
public class Avatar {
    private Camera cam;
    private double rad = 0.25;
    double cDist = 5, cRot = 35;

    public Avatar(Terrain land) {
        cam = new Camera(land);
    }

    public void draw(GL2 gl) {
        double[] pos = cam.getPos(), targ = cam.getTarget(), up = cam.getUp();
        if (!cam.isFPS()) {    //we set up a nice top-down view at the avatar.
            gl.glTranslated(0, 0, -cDist);
            gl.glRotated(cRot, 1, 0, 0);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[3].getTexID());
            new GLUT().glutSolidSphere(0.25, 20, 20);
            if (cam.isNight()) drawTorch(gl);    //and draw in our torch if its night.
            gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
            pos[1] -= 1.5 * rad;
        }
        if (cam.isNight()) enableTorch(gl);    //put on the lights!
        else gl.glDisable(GL2.GL_LIGHT1);    //...lest its daytime, in which case turn it off
        new GLU().gluLookAt(pos[0], pos[1] + 2.5 * rad, pos[2], targ[0], targ[1] + 2.5 * rad, targ[2], up[0], up[1], up[2]);
    }

    private void drawTorch(GL2 gl) {
        gl.glPushMatrix();
        gl.glTranslated(0.4, 0, -0.1);
        GLUT glut = new GLUT();
        glut.glutSolidCylinder(0.08, 0.3, 20, 20);
        gl.glTranslated(0, 0, -0.1);
        glut.glutSolidCone(0.1, 0.1, 20, 20);
        enableTorch(gl);
        gl.glPopMatrix();
    }

    private void enableTorch(GL2 gl) {
        gl.glPushMatrix();
        gl.glEnable(GL2.GL_LIGHT1);
        gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_CUTOFF, 30);
        gl.glLightf(GL2.GL_LIGHT1, GL2.GL_SPOT_EXPONENT, 4);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, new float[]{0, 0, 0, 1}, 0);
        double[] tmp = cam.getTarget();
        gl.glRotated(90, 0, 1, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPOT_DIRECTION, new float[]{(float) tmp[0], (float) tmp[1], (float) tmp[2]}, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1f}, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[]{1f, 1f, 0.1f, 1f}, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[]{0.2f, 0.2f, 0.2f, 1f}, 0);
        gl.glPopMatrix();
    }

    public Camera getCam() {
        return cam;
    }

    public boolean isNight() {
        return cam.isNight();
    }
}
