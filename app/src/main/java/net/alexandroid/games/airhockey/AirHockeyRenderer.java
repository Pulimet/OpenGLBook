package net.alexandroid.games.airhockey;

import android.content.Context;
import android.opengl.GLSurfaceView;

import net.alexandroid.games.airhockey.util.ShaderHelper;
import net.alexandroid.games.airhockey.util.TextResourceReader;
import net.alexandroid.utils.mylog.MyLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.*;


class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final String U_COLOR = "u_Color";
    private int uColorLocation;

    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private final Context context;
    private final FloatBuffer vertexData;

    private int program;

    public AirHockeyRenderer(Context context) {
        this.context = context;
        float[] tableVerticesWithTriangles = {
                // Triangle 1 - Border
                -0.55f, -0.53f,
                0.55f, 0.53f,
                -0.55f, 0.53f,
                // Triangle 2 - Border
                -0.55f, -0.53f,
                0.55f, -0.53f,
                0.55f, 0.53f,
                // Triangle 1
                -0.5f, -0.5f,
                0.5f, 0.5f,
                -0.5f, 0.5f,
                // Triangle 2
                -0.5f, -0.5f,
                0.5f, -0.5f,
                0.5f, 0.5f,
                // Line 1
                -0.5f, 0f,
                0.5f, 0f,
                // Mallets
                0f, -0.25f,
                0f, 0.25f,
                // Puck
                0f, 0f
        };

        vertexData = ByteBuffer
                .allocateDirect(tableVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        vertexData.put(tableVerticesWithTriangles);

    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        MyLog.d("");
        // The screen will become red when cleared
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);

        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (BuildConfig.DEBUG) {
            ShaderHelper.validateProgram(program);
        }

        glUseProgram(program);

        // Get the location of our uniform, and we store that location in uColorLocation.
        // We’ll use that when we want to update the value of this uniform later on.
        uColorLocation = glGetUniformLocation(program, U_COLOR);

        // Get the location of our attribute. With this location,
        // we’ll be able to tell OpenGL where to find the data for this attribute
        aPositionLocation = glGetAttribLocation(program, A_POSITION);


        // Tell OpenGL where to find data for our attribute a_Position
        vertexData.position(0);
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, 0, vertexData);

        // Enable the attribute before we can start drawing
        glEnableVertexAttribArray(aPositionLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        MyLog.d("");
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        MyLog.d("");
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Draw border
        glUniform4f(uColorLocation, 0.3f, 0.3f, 0.3f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        // 2 Triangles
        glUniform4f(uColorLocation, 1.0f, 1.0f, 1.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 6, 6);

        // Line
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_LINES, 12, 2);

        // Draw the first mallet blue.
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f);
        glDrawArrays(GL_POINTS, 14, 1);

        // Draw the second mallet red.
        glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 15, 1);

        // Draw puck
        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_POINTS, 16, 1);



    }
}
