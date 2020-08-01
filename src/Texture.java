package ass2.src;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.awt.ImageUtil;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * The Texture class.
 * Essentially a convenience class to encapsulate a Texture here for JOGL.
 * Does what you'd expect, reads in textures from an image files, appropriately formats it,
 * and applies MIP-mapping for anti-aliasing.test
 *
 * @author simon
 */
public class Texture {
    private int[] texIDs = new int[1];

    public Texture(GL2 gl, String file) {
        TextureData data = null;
        try {
            BufferedImage img = ImageIO.read(new File(file)); // read file into BufferedImage
            ImageUtil.flipImageVertically(img);
            data = AWTTextureIO.newTextureData(GLProfile.getDefault(), img, false);
        } catch (IOException exc) {
            System.err.println(file);
            exc.printStackTrace();
            System.exit(1);
        }
        gl.glGenTextures(texIDs.length, texIDs, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, texIDs[0]);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0,
                data.getInternalFormat(),
                data.getWidth(),
                data.getHeight(),
                0,
                data.getPixelFormat(),
                data.getPixelType(),
                data.getBuffer());

        setFilters(gl);
    }

    private void setFilters(GL2 gl) {
        gl.glGenerateMipmap(GL2.GL_TEXTURE_2D); //opengl is my slave...make my mipmaps, and a sammich
        //let's use bilinear magnification, and trilinear mipmapping minification
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);

        float fLargest[] = new float[1];    //gotta love the antisotropic filtering.
        gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, fLargest, 0);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, fLargest[0]);
    }

    public int getTexID() {    //could add argument if ever more IDs
        return texIDs[0];
    }

    public void release(GL2 gl) {
        if (texIDs[0] > 0) gl.glDeleteTextures(1, texIDs, 0);
    }
}