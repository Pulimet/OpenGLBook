package net.alexandroid.games.airhockey.util;


import net.alexandroid.utils.mylog.MyLog;

import static android.opengl.GLES20.GL_COMPILE_STATUS;
import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_LINK_STATUS;
import static android.opengl.GLES20.GL_VALIDATE_STATUS;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetProgramiv;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetShaderiv;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glValidateProgram;

public class ShaderHelper {
    public static int compileVertexShader(String shaderCode) {
        return compileShader(GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        // Create an object. Returned integer is the reference to our OpenGL object.
        final int shaderObjectId = glCreateShader(type);
        if (shaderObjectId == 0) {
            MyLog.d("Could not create new shader.");
            return 0;
        }

        //Once we have a valid shader object, we call glShaderSource(shaderObjectId, shaderCode)
        //to upload the source code. This call tells OpenGL to read in the source code
        //defined in the String shaderCode and associate it with the shader object referred to
        //by shaderObjectId.
        glShaderSource(shaderObjectId, shaderCode);

        //This tells OpenGL to compile the source code that was previously uploaded to shaderObjectId.
        glCompileShader(shaderObjectId);

        // Following code added to check if OpenGL was able to successfully compile the shader
        final int[] compileStatus = new int[1];
        glGetShaderiv(shaderObjectId, GL_COMPILE_STATUS, compileStatus, 0);


        if (compileStatus[0] == 0) { // If it failed, delete the shader object.
            MyLog.d("Compilation of shader failed.");
            MyLog.d("Results of compiling source:" + "\n" + shaderCode + "\n:" + glGetShaderInfoLog(shaderObjectId));
            glDeleteShader(shaderObjectId);
            return 0;
        }

        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        // Create a new program object
        final int programObjectId = glCreateProgram();
        if (programObjectId == 0) {
            MyLog.d("Could not create new program");
            return 0;
        }

        // Attach both our vertex shader and our fragment shader to the program object
        glAttachShader(programObjectId, vertexShaderId);
        glAttachShader(programObjectId, fragmentShaderId);

        // Join our shaders together
        glLinkProgram(programObjectId);

        // Check whether the link failed or succeeded
        final int[] linkStatus = new int[1];
        glGetProgramiv(programObjectId, GL_LINK_STATUS, linkStatus, 0);


        if (linkStatus[0] == 0) {
            MyLog.d("Results of linking program:\n" + glGetProgramInfoLog(programObjectId));
            // If it failed, delete the program object.
            glDeleteProgram(programObjectId);
            MyLog.d("Linking of program failed.");
            return 0;
        }

        return programObjectId;
    }

    public static void validateProgram(int programObjectId) {
        glValidateProgram(programObjectId);
        final int[] validateStatus = new int[1];
        glGetProgramiv(programObjectId, GL_VALIDATE_STATUS, validateStatus, 0);
        MyLog.d("Results of validating program: " + validateStatus[0]
                + "\nLog:" + glGetProgramInfoLog(programObjectId));
        //return validateStatus[0] != 0;
    }
}
