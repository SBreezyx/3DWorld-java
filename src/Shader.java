package ass2.src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;

import com.jogamp.opengl.GL2;

/**
 * Shader Utility class borrowed from COMP3421's Week 6 code.
 *
 * @author Angela Finlayson
 * ^ (I think??)
 */
public class Shader {
    private String[] mySource;
    private int myType, myID;

    public static int initShaders(GL2 gl, String vs, String fs) throws Exception {
        Shader vertexShader = new Shader(GL2.GL_VERTEX_SHADER, new File(vs));
        vertexShader.compile(gl);
        Shader fragmentShader = new Shader(GL2.GL_FRAGMENT_SHADER, new File(fs));
        fragmentShader.compile(gl);

        //Each shaderProgram must have one vertex shader and one fragment shader.
        int shaderprogram = gl.glCreateProgram();
        gl.glAttachShader(shaderprogram, vertexShader.getID());
        gl.glAttachShader(shaderprogram, fragmentShader.getID());

        int[] error = new int[2];
        gl.glLinkProgram(shaderprogram);    //if we couldnt link the error status is logged here.
        gl.glGetProgramiv(shaderprogram, GL2.GL_LINK_STATUS, error, 0);
        if (error[0] != GL2.GL_TRUE) {
            int[] logLength = new int[1];
            gl.glGetProgramiv(shaderprogram, GL2.GL_INFO_LOG_LENGTH, logLength, 0);
            byte[] log = new byte[logLength[0]];
            gl.glGetProgramInfoLog(shaderprogram, logLength[0], (int[]) null, 0, log, 0);
            System.out.printf("Failed to link shader! %s\n", new String(log));
            throw new RuntimeException("Error linking the shader: " + new String(log));
        }

        gl.glValidateProgram(shaderprogram);    //if the program could not be validated, the error is logged here.
        gl.glGetProgramiv(shaderprogram, GL2.GL_VALIDATE_STATUS, error, 0);
        if (error[0] != GL2.GL_TRUE) {
            System.out.printf("Failed to validate shader!\n");
            throw new Exception("program failed to validate");
        }
        return shaderprogram;    //the shader ID
    }

    /**
     * Reads the external GLSL shader program into a single String, ready for compilation
     * and linking.
     *
     * @param type       Either GL2.GL_VERTEX_SHADER or GL2.GL_FRAGMENT_SHADER
     * @param sourceFile Java File containing the shader program.
     * @throws IOException
     */
    public Shader(int type, File sourceFile) throws IOException {
        myType = type;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceFile)));
            StringWriter writer = new StringWriter();
            mySource = new String[1];
            for (String line = reader.readLine(); line != null; line = reader.readLine())
                writer.write(line + "\n");
            reader.close();
            mySource[0] = writer.getBuffer().toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void compile(GL2 gl) {
        myID = gl.glCreateShader(myType);
        gl.glShaderSource(myID, 1, mySource, new int[]{mySource[0].length()}, 0);
        gl.glCompileShader(myID);

        //Check compile status.
        int[] compiled = new int[1];
        gl.glGetShaderiv(myID, GL2.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            int[] logLength = new int[1];
            gl.glGetShaderiv(myID, GL2.GL_INFO_LOG_LENGTH, logLength, 0);

            byte[] log = new byte[logLength[0]];
            gl.glGetShaderInfoLog(myID, logLength[0], (int[]) null, 0, log, 0);

            throw new RuntimeException("Error compiling the shader: " + new String(log));
        }

    }

    public int getID() {
        return myID;
    }
}
