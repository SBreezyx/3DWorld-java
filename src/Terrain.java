package ass2.src;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import com.jogamp.opengl.GL2;

/**
 * The terrain class, containing roads, trees, and creepers.
 * A m x n grid of altitudes, with any non-integer point bilinearly interpolated.
 * Such fun.
 *
 * @author malcolmr & Simon Haddad, z5061640
 */
public class Terrain {
    private Dimension mySize;
    private double[][] myAltitude;
    private List<Tree> myTrees;
    private List<Road> myRoads;
    private List<Creeper> myCreeps;
    private float[] mySunlight;
    private final short T_TERR = 0;    //texture macro

    /**
     * Create a new terrain
     *
     * @param width The number of vertices in the x-direction
     * @param depth The number of vertices in the z-direction
     */
    public Terrain(int width, int depth) {
        mySize = new Dimension(width, depth);
        myAltitude = new double[width][depth];
        myTrees = new ArrayList<Tree>();
        myRoads = new ArrayList<Road>();
        myCreeps = new ArrayList<Creeper>();
        mySunlight = new float[4];
    }

    public Terrain(Dimension size) {
        this(size.width, size.height);
    }

    void draw(GL2 gl) {
        gl.glPushMatrix();

        for (int z = 0; z < mySize.height - 1; ++z) {
            for (int x = 0; x < mySize.width - 1; ++x) {
                //set material properties for the terrain.
                gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, new float[]{0.738f, 0.769f, 0.8f, 1f}, 0);
                gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, new float[]{0.738f, 0.769f, 0.8f, 0.8f}, 0);
                gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, new float[]{0.2f, 0.2f, 0.2f, 1f}, 0);

                gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[T_TERR].getTexID());
                gl.glBegin(GL2.GL_TRIANGLES);
                {
                    //To draw the terrain itself, each square encapsulated by x, x+1, z, and z+1 is split into
                    //2 triangles, with each triangle having its own normal specified. The reduced weird lighting glitches
                    //and let's people get practice reading near duplicated code.

                    double[] v1 = {x, myAltitude[x][z + 1], z + 1};
                    double[] v2 = {x + 1, myAltitude[x + 1][z + 1], z + 1};
                    double[] v3 = {x + 1, myAltitude[x + 1][z], z};

                    gl.glNormal3dv(MathUtil.normalise(MathUtil.cross(MathUtil.vec3d(v1, v2), MathUtil.vec3d(v1, v3))), 0);
                    gl.glTexCoord2d(0, 0);
                    gl.glVertex3dv(v1, 0);
                    gl.glTexCoord2d(1, 0);
                    gl.glVertex3dv(v2, 0);
                    gl.glTexCoord2d(1, 1);
                    gl.glVertex3dv(v3, 0);

                    v1[0] = x + 1;
                    v1[1] = myAltitude[x + 1][z];
                    v1[2] = z;
                    v2[0] = x;
                    v2[1] = myAltitude[x][z];
                    v2[2] = z;
                    v3[0] = x;
                    v3[1] = myAltitude[x][z + 1];
                    v3[2] = z + 1;

                    gl.glNormal3dv(MathUtil.normalise(MathUtil.cross(MathUtil.vec3d(v1, v2), MathUtil.vec3d(v1, v3))), 0);
                    gl.glTexCoord2d(1, 1);
                    gl.glVertex3dv(v1, 0);
                    gl.glTexCoord2d(0, 1);
                    gl.glVertex3dv(v2, 0);
                    gl.glTexCoord2d(0, 0);
                    gl.glVertex3dv(v3, 0);
                }
                gl.glEnd();
            }
        }
        for (Tree t : myTrees) t.draw(gl);    //We draw each child of the terrain
        for (Road r : myRoads) r.draw(gl);    //seperately, restoring the modelview matrix
        for (Creeper c : myCreeps) c.draw(gl);    //at each stage.
        gl.glPopMatrix();
    }

    public Dimension size() {
        return mySize;
    }

    public List<Tree> trees() {
        return myTrees;
    }

    public List<Road> roads() {
        return myRoads;
    }

    public float[] getSunlight() {
        return mySunlight;
    }

    /**
     * Set the sunlight direction.
     * <p>
     * Note: the sun should be treated as a directional light, without a position
     *
     * @param dx
     * @param dy
     * @param dz
     */
    public void setSunlightDir(float dx, float dy, float dz) {
        mySunlight[0] = dx;
        mySunlight[1] = dy;
        mySunlight[2] = dz;
        mySunlight[3] = 0;    //because its a vector
    }

    /**
     * Resize the terrain, copying any old altitudes.
     *
     * @param width
     * @param height
     */
    public void setSize(int width, int height) {
        mySize = new Dimension(width, height);
        double[][] oldAlt = myAltitude;
        myAltitude = new double[width][height];

        for (int i = 0; i < width && i < oldAlt.length; i++) {
            for (int j = 0; j < height && j < oldAlt[i].length; j++) {
                myAltitude[i][j] = oldAlt[i][j];
            }
        }
    }

    /**
     * Get the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public double getGridAltitude(int x, int z) {
        return myAltitude[x][z];
    }

    /**
     * Set the altitude at a grid point
     *
     * @param x
     * @param z
     * @return
     */
    public void setGridAltitude(int x, int z, double h) {
        myAltitude[x][z] = h;
    }

    public double altitude(double[] p) {
        return altitude(p[0], p[1]);
    }

    /**
     * Get the altitude at an arbitrary point.
     * Non-integer points should be interpolated from neighbouring grid points
     *
     * @param x
     * @param z
     * @return
     */
    public double altitude(double x, double z) {
        int ix = (int) x, iz = (int) z;    //truncation
        double tx = x - ix, tz = z - iz; //how much to move in each direction

        if (ix == myAltitude.length - 1 && iz == myAltitude[ix].length - 1) {    //if as far down and right we can go.
            return myAltitude[ix][iz];
        } else if (ix == myAltitude.length - 1) {     //if we are as far right as we can go.
            return myAltitude[ix][iz] + (myAltitude[ix][iz + 1] - myAltitude[ix][iz]) * tz;
        } else if (iz == myAltitude[ix].length - 1) {    //if as far down we can go
            return myAltitude[ix][iz] + (myAltitude[ix + 1][iz] - myAltitude[ix][iz]) * tz;
        } else {    //find the same level points on the z axis, then lerp between them to the desired point along the x-axis.
            double a1 = myAltitude[ix][iz] + (myAltitude[ix][iz + 1] - myAltitude[ix][iz]) * tz;
            double a2 = myAltitude[ix + 1][iz] + (myAltitude[ix + 1][iz + 1] - myAltitude[ix + 1][iz]) * tz;
            return a1 + (a2 - a1) * tx;
        }
    }

    public void addCreeper(double x, double z) {
        myCreeps.add(new Creeper(x, altitude(x, z), z, myCreeps.size()));
    }

    public int numCreepers() {
        return myCreeps.size();
    }

    /**
     * Add a tree at the specified (x,z) point.
     * The tree's y coordinate is calculated from the altitude of the terrain at that point.
     *
     * @param x
     * @param z
     */
    public void addTree(double x, double z) {
        myTrees.add(new Tree(x, altitude(x, z), z));
    }

    /**
     * Add a road.
     *
     * @param x
     * @param z
     */
    public void addRoad(double width, double[] spine) {
        myRoads.add(new Road(width, spine, this));
    }

    public void addLTree(int iter, int type, double x, double z) {
        myTrees.add(new LTree(iter, type, x, altitude(x, z), z));
    }
}