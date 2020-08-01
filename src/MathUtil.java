package ass2.src;

/**
 * A utility class borrowed from Week4 to do Maths-related calculations.
 * Naturally, I made it slightly more memory efficient by removing temp variables.
 * No need to thak me.
 *
 * @author malcolmr & (paritally) Simon Haddad, z5061640
 */
public class MathUtil {

    public static double[] cross(double[] u, double[] v) {
        if (u[0] == v[0] && u[1] == v[1] && u[2] == v[2])
            return new double[]{-u[1], u[0], 0};
        double crossProduct[] = new double[3];
        crossProduct[0] = u[1] * v[2] - u[2] * v[1];
        crossProduct[1] = u[2] * v[0] - u[0] * v[2];
        crossProduct[2] = u[0] * v[1] - u[1] * v[0];
        return crossProduct;
    }

    public static double[] normalise(double[] n) {
        double mag = Math.sqrt(n[0] * n[0] + n[1] * n[1] + n[2] * n[2]);
        return new double[]{n[0] / mag, n[1] / mag, n[2] / mag};
    }

    public static double[] multiply(double[][] m, double[] v) {
        double[] u = new double[v.length];
        for (int i = 0; i < v.length; i++) {
            u[i] = 0;
            for (int j = 0; j < v.length; j++) u[i] += m[i][j] * v[j];
        }
        return u;
    }

    /**
     * COMMENT: matrixMultiply
     *
     * @param t
     * @param r
     * @return
     */
    public static double[][] multiply(double[][] p, double[][] q) {
        double[][] m = new double[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                m[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    m[i][j] += p[i][k] * q[k][j];
                }
            }
        }

        return m;
    }

    public static void print(double[][] m) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.out.print(m[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static void print(double[] n) {
        System.out.println(n[0] + " " + n[1] + " " + n[2]);
    }

    /**
     * Head minus tail vector creation from two points.
     *
     * @param p1
     * @param p2
     * @return
     */
    public static double[] vec3d(double[] p1, double[] p2) {
        return new double[]{p2[0] - p1[0], p2[1] - p1[1], p2[2] - p1[2]};
    }
}
