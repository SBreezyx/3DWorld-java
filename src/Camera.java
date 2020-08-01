package ass2.src;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The basic I/O class, the Camera class!
 * The class keeps tabs on the front facing vector, where the camera is, and also things like
 * if we're in third-person or not, and night mode.
 *
 * @author Simon Haddad, z5061640
 */
public class Camera implements KeyListener, MouseListener, MouseMotionListener {
    private double[] camPos = new double[]{0, 0.25, 3};
    private double[] camFront = new double[]{0, 0, -1};
    private double[] camUp = new double[]{0, 1, 0};
    private double pitch = 0, yaw = 0;
    private boolean fpsMode = false, night = false;
    private Terrain land;

    public Camera(Terrain land) {
        this.land = land;
    }

    /**
     * MouseMotionListener section.
     */
    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    /**
     * MouseListener section.
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * KeyListener section.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:    //move in the direction we're facing
                camPos[0] += camFront[0];
                camPos[2] += camFront[2];
                camPos[1] = land.altitude(camPos[0], camPos[2]); //with y-coord being determined by the land.
                break;
            case KeyEvent.VK_DOWN:    //same here, but backwards.
                camPos[0] -= camFront[0];
                camPos[2] -= camFront[2];
                camPos[1] = land.altitude(camPos[0], camPos[2]);
                break;
            case KeyEvent.VK_LEFT:
                yaw -= 30;    //lets turn!
                rotate();
                break;
            case KeyEvent.VK_RIGHT:
                yaw += 30;
                rotate();
                break;
            case KeyEvent.VK_C:
                fpsMode = !fpsMode;
                break;
            case KeyEvent.VK_N:
                night = !night;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public double[] getPos() {
        return new double[]{camPos[0], camPos[1], camPos[2]};
    }

    public double[] getTarget() {
        return new double[]{camPos[0] + camFront[0], camPos[1] + camFront[1], camPos[2] + camFront[2]};
    }

    public double[] getUp() {
        return camUp;
    }

    public boolean isFPS() {
        return fpsMode;
    }

    /**
     * Calculate the coordinates of the new front using basic trig.
     */
    private void rotate() {
        camFront[0] = Math.cos(Math.toRadians(pitch)) * Math.cos(Math.toRadians(yaw));
        camFront[1] = Math.sin(Math.toRadians(pitch));
        camFront[2] = Math.cos(Math.toRadians(pitch)) * Math.sin(Math.toRadians(yaw));
        camFront = MathUtil.normalise(camFront);
    }

    public boolean isNight() {
        return night;
    }
}
