package ass2.src;

import com.jogamp.opengl.GL2;

import java.util.ArrayList;
import java.util.List;

/**
 * The road class.
 * This class makes use of a Bezier curve to model a spine of a road, which I use
 * to approximate tangent surfaces to the curve in order to draw an even (hopefully smooth) road.
 * It should be noted that this class had the y-coordinates of points forcefully inserted for convenience.
 *
 * @author malcolmr & Simon Haddad, z5061640.
 */
public class Road {
    private List<Double> myPoints;    //control points [x, y, z]
    private double myWidth;
    double step = 0.05;                //the difference in parameter values
    private Terrain myLand;            //needed to interpolate altitudes.
    private final short T_ROAD = 4;    //texture macro

    /**
     * Create a new road starting at the specified point
     */
    public Road(double width, double x0, double z0, Terrain land) {
        myWidth = width;
        myPoints = new ArrayList<>();
        myLand = land;
        myPoints.add(x0);
        myPoints.add(land.altitude(x0, z0));
        myPoints.add(z0);
    }

    /**
     * Create a new road with the specified spine
     *
     * @param width
     * @param spine
     */
    public Road(double width, double[] spine, Terrain land) {
        myWidth = width;
        myPoints = new ArrayList<>();
        myLand = land;
        for (int i = 0; i < spine.length; i += 2) {
            myPoints.add(spine[i]);    //x
            myPoints.add(land.altitude(spine[i], spine[i + 1])); //y
            myPoints.add(spine[i + 1]); //z
        }
    }

    public void draw(GL2 gl) {
        gl.glPushMatrix();
        //sets the properties for the road colour.
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[]{0.25f, 0.25f, 0.25f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[]{0.843f, 0.844f, 0.812f, 1f}, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[]{0.4f, 0.4f, 0.4f, 1f}, 0);

        gl.glEnable(GL2.GL_POLYGON_OFFSET_LINE);
        gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);    //the road z-fights with flat ground.
        gl.glPolygonOffset(-0.3f, -0.3f);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_ROAD].getTexID());

        double[][] oldPoints = null;
        double d = myWidth / 2;
        for (double t = 0; t < size(); t += step) {
            double[] p = point(t, 0);

            //Now we grab the unit tangent vector at t and rotate it to grab the other tangent to the spine. Once this is obtained,
            //the second perpendicular spans the correct wind for the road to avoid the curve crossing over itself. So we just normalise it
            //and move width/2 units towards and away from it, and stick a vertex there.
            double[] tangent = MathUtil.normalise(point(t, 1)), n = MathUtil.normalise(new double[]{-tangent[2], 0, tangent[0]});
            double[][] newPoints = {{p[0] + d * n[0], p[1] + d * n[1], p[2] + d * n[2]}, {p[0] - d * n[0], p[1] - d * n[1], p[2] - d * n[2]}};
            if (oldPoints == null) oldPoints = newPoints;
            gl.glBegin(GL2.GL_TRIANGLES);
            {
                gl.glNormal3dv(MathUtil.normalise(MathUtil.cross(MathUtil.vec3d(oldPoints[1], oldPoints[0]),
                        MathUtil.vec3d(oldPoints[1], newPoints[1]))), 0);
                gl.glTexCoord2d(0, 0);
                gl.glVertex3dv(oldPoints[0], 0);
                gl.glTexCoord2d(1, 1);
                gl.glVertex3dv(newPoints[1], 0);
                gl.glTexCoord2d(1, 0);
                gl.glVertex3dv(oldPoints[1], 0);

                gl.glNormal3dv(MathUtil.normalise(MathUtil.cross(MathUtil.vec3d(oldPoints[0], newPoints[0]),
                        MathUtil.vec3d(oldPoints[0], newPoints[1]))), 0);
                gl.glTexCoord2d(0, 0);
                gl.glVertex3dv(oldPoints[0], 0);
                gl.glTexCoord2d(0, 1);
                gl.glVertex3dv(newPoints[0], 0);
                gl.glTexCoord2d(1, 1);
                gl.glVertex3dv(newPoints[1], 0);
            }
            gl.glEnd();
            oldPoints = newPoints;
        }
        gl.glDisable(GL2.GL_POLYGON_OFFSET_LINE);
        gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
        gl.glPopMatrix();
    }

    /**
     * The width of the road.
     *
     * @return
     */
    public double width() {
        return myWidth;
    }

    /**
     * Add a new segment of road, beginning at the last point added and ending at (x3, z3).
     * (x1, z1) and (x2, z2) are interpolated as bezier control points.
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     */
    public void addSegment(double x1, double z1, double x2, double z2, double x3, double z3) {
        myPoints.add(x1);
        myPoints.add(myLand.altitude(x1, z1));
        myPoints.add(z1);
        myPoints.add(x2);
        myPoints.add(myLand.altitude(x2, z2));
        myPoints.add(z2);
        myPoints.add(x3);
        myPoints.add(myLand.altitude(x2, z2));
        myPoints.add(z3);
    }

    /**
     * Get the number of segments in the curve
     *
     * @return
     */
    public int size() {
        return myPoints.size() / 9;
    }

    /**
     * Get the specified control point.
     *
     * @param i
     * @return
     */
    public double[] controlPoint(int i) {
        return new double[]{myPoints.get(i * 3), myPoints.get(i * 3 + 1), myPoints.get(i * 3 + 2)};
    }

    /**
     * Get a point on the spine. The parameter t may vary from 0 to size().
     * Points on the kth segment take have parameters in the range (k, k+1).
     * The derivative parameter says which derivative to grab. 1-1 correspondence to mathematical derivative.
     *
     * @param t
     * @return
     */
    public double[] point(double t, int derivative) {
        int i = (int) Math.floor(t);
        t = t - i;
        i *= 9;
        double x0 = myPoints.get(i++);
        double y0 = myPoints.get(i++);
        double z0 = myPoints.get(i++);
        double x1 = myPoints.get(i++);
        double y1 = myPoints.get(i++);
        double z1 = myPoints.get(i++);
        double x2 = myPoints.get(i++);
        double y2 = myPoints.get(i++);
        double z2 = myPoints.get(i++);
        double x3 = myPoints.get(i++);
        double y3 = myPoints.get(i++);
        double z3 = myPoints.get(i++);
        switch (derivative) {
            case 0:    //0th derivative
                return new double[]{b(0, t) * x0 + b(1, t) * x1 + b(2, t) * x2 + b(3, t) * x3,
                        b(0, t) * y0 + b(1, t) * y1 + b(2, t) * y2 + b(3, t) * y3,
                        b(0, t) * z0 + b(1, t) * z1 + b(2, t) * z2 + b(3, t) * z3};
            case 1:    //1st derivative
                return new double[]{3 * (b_(0, t) * (x1 - x0) + b_(1, t) * (x2 - x1) + b_(2, t) * (x3 - x2)),
                        3 * (b_(0, t) * (y1 - y0) + b_(1, t) * (y2 - y1) + b_(2, t) * (y3 - y2)),
                        3 * (b_(0, t) * (z1 - z0) + b_(1, t) * (z2 - z1) + b_(2, t) * (z3 - z2))};
            case 2: //2nd derivative
                return new double[]{6 * (x0 * (-t + 1) + x1 * (3 * t - 2) + x2 * (-3 * t + 1) + x3 * (t)),
                        6 * (y0 * (-t + 1) + y1 * (3 * t - 2) + y2 * (-3 * t + 1) + y3 * (t)),
                        6 * (z0 * (-t + 1) + z1 * (3 * t - 2) + z2 * (-3 * t + 1) + z3 * (t))};
        }
        throw new IllegalArgumentException("" + derivative);
    }

    /**
     * Calculate the Bezier coefficients
     *
     * @param degree
     * @param t
     * @return
     */
    private double b(int degree, double t) {
        switch (degree) {
            case 0:
                return (1 - t) * (1 - t) * (1 - t);
            case 1:
                return 3 * (1 - t) * (1 - t) * t;
            case 2:
                return 3 * (1 - t) * t * t;
            case 3:
                return t * t * t;
        } // this should never happen
        throw new IllegalArgumentException("" + degree);
    }

    /**
     * b'(t) for a t in [0, size()].
     *
     * @param degree
     * @param t
     * @return
     */
    private double b_(int degree, double t) {
        switch (degree) {
            case 0:
                return (1 - t) * (1 - t);
            case 1:
                return 2 * t * (1 - t);
            case 2:
                return t * t;
        }
        throw new IllegalArgumentException("" + degree);
    }
}