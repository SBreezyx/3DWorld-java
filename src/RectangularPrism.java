package ass2.src;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import java.nio.FloatBuffer;

/**
 * ---------------------------
 * /d						  /|
 * /-------------------------/ |
 * |						|  |
 * |						|h /
 * |						| /
 * --------------------------/
 * w
 *
 * @author simon
 * Little abstraction class to make drawing a creeper easier. Since a Creeper is
 * a culmination of basically the same shape, this should aid efficiency/readability.
 * This class is drawn using VBOs, so it branches away from the deprecated immediate mode code seen
 * thus far.
 */
public class RectangularPrism {
    private double[] location;
    private float[] vert, norm, tex;
    private float[] diffAndSpec = new float[]{0f, 0.5f, 0f, 1f}, amb = new float[]{0.3f, 0.3f, 0.3f, 1f};
    private int bIX;    //buffer index
    private FloatBuffer pData = null, nData = null, tData = null;

    public RectangularPrism(float w, float h, float d, double[] loc, int vIX) {
        //Data here is duplicated as OpenGL defines a vertex to be more than simply a position.
        //A vertex encapsulates all things needed to draw it i.e. position, colour, normals, texture coords, etc.
        //As such if even a single attribute is different, that vertex is unique.
        //Naturally, that makes indexing a pain since there is no benefit indexing a cube with 3 different normals
        //for a given position.
        vert = new float[]{
                w, 0, 0, w, h, 0, w, h, d, w, 0, d, //x
                0, h, 0, 0, h, d, w, h, d, w, h, 0,  //y
                0, 0, 0, w, 0, 0, w, 0, d, 0, 0, d,    //-y
                0, 0, d, 0, h, d, 0, h, 0, 0, 0, 0,//-x
                0, 0, 0, 0, h, 0, w, h, 0, w, 0, 0, //-z
                w, 0, d, w, h, d, 0, h, d, 0, 0, d //z
        };

        norm = new float[]{
                1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,  //x
                0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0,    //y
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,  //-y
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,  //-x
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,   //-z
                0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1  //z
        };

        tex = new float[]{1, 0, 1, 1, 0, 1, 0, 0,
                0, 0, 2, 0, 2, 2, 0, 2,
                0, 0, 2, 0, 2, 2, 0, 2,
                0, 0, 2, 0, 2, 2, 0, 2,
                0, 0, 2, 0, 2, 2, 0, 2,
                0, 0, 2, 0, 2, 2, 0, 2};

        location = loc;
        this.bIX = vIX;
    }

    public void draw(GL2 gl, int shad, int texID) {
        if (pData == null || nData == null || tData == null) genBuffers(gl);
        gl.glPushMatrix();
        gl.glTranslated(location[0], location[1], location[2]);    //move to my centre for ease.
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, amb, 0);    //set material properties for this shape.
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, diffAndSpec, 0);
        gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, diffAndSpec, 0);

        gl.glUseProgram(shad);
        gl.glUniform1i(gl.glGetUniformLocation(shad, "texUnit1"), 0);
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, Game.buffIDs[bIX]);

        //lets grab a pointer to my GPU variables...
        int vpos = gl.glGetAttribLocation(shad, "vpos");
        gl.glEnableVertexAttribArray(vpos);
        int vN = gl.glGetAttribLocation(shad, "vN");
        gl.glEnableVertexAttribArray(vN);
        int vtex = gl.glGetAttribLocation(shad, "vtex");
        gl.glEnableVertexAttribArray(vtex);

        //give opengl them...
        gl.glVertexAttribPointer(vpos, 3, GL.GL_FLOAT, false, 0, 0);
        gl.glVertexAttribPointer(vN, 3, GL.GL_FLOAT, false, 0, Float.BYTES * vert.length);
        gl.glVertexAttribPointer(vtex, 2, GL.GL_FLOAT, false, 0, Float.BYTES * (vert.length + norm.length));

        //and then draw them! We draw the head differently to the body
        //we draw the head seperately from the body to avoid the face repeating on the texture.
        if (texID == 5) {
            gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[texID++].getTexID());
            gl.glDrawArrays(GL2.GL_QUADS, 0, 4);
            gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[texID].getTexID());
            gl.glDrawArrays(GL2.GL_QUADS, 4, 20);
        } else {
            gl.glBindTexture(GL2.GL_TEXTURE_2D, Game.tex[texID].getTexID());
            gl.glDrawArrays(GL2.GL_QUADS, 0, 24);
        }

        //cleanup
        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
        gl.glBindTexture(GL2.GL_TEXTURE_2D, 0);
        gl.glUseProgram(0);
        gl.glPopMatrix();
    }

    private void genBuffers(GL2 gl) {
        pData = Buffers.newDirectFloatBuffer(vert);    //vert data
        nData = Buffers.newDirectFloatBuffer(norm);    //normal data
        tData = Buffers.newDirectFloatBuffer(tex);        //texture data

        gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, Game.buffIDs[bIX]);
        gl.glBufferData(GL2.GL_ARRAY_BUFFER,
                Float.BYTES * (vert.length + norm.length + tex.length),
                null,
                GL2.GL_STATIC_DRAW);
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, 0, vert.length * Float.BYTES, pData);
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, Float.BYTES * vert.length, norm.length * Float.BYTES, nData);
        gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, Float.BYTES * (vert.length + norm.length), tex.length * Float.BYTES, tData);
    }
}
