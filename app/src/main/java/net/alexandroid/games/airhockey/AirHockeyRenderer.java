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

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.orthoM;


class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private static final int POSITION_COMPONENT_COUNT = 2;

    private static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int aColorLocation;


    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;

    private static final String U_MATRIX = "u_Matrix";
    private final float[] projectionMatrix = new float[16];
    private int uMatrixLocation;

    private final Context context;
    private final FloatBuffer vertexData;

    private int program;

/*    int parsedColor = Color.parseColor("#0099CC");
    float red = Color.red(parsedColor) / 255f;
    float green = Color.green(parsedColor) / 255f;
    float blue = Color.blue(parsedColor) / 255f;*/

    public AirHockeyRenderer(Context context) {
        this.context = context;
        float[] tableVerticesWithTriangles = {
                // Triangle 1 - Border
                -0.55f, -0.53f, 0.3f, 0.3f, 0.3f,
                0.55f, 0.53f, 0.3f, 0.3f, 0.3f,
                -0.55f, 0.53f, 0.3f, 0.3f, 0.3f,
                // Triangle 2 - Border
                -0.55f, -0.83f, 0.3f, 0.3f, 0.3f,
                0.55f, -0.83f, 0.3f, 0.3f, 0.3f,
                0.55f, 0.83f, 0.3f, 0.3f, 0.3f,
                // Triangle Fan
                0f, 0f, 1f, 1f, 1f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, 0.8f, 0.7f, 0.7f, 0.7f,
                -0.5f, -0.8f, 0.7f, 0.7f, 0.7f,
                // Line 1
                -0.5f, 0f, 1.0f, 0.0f, 0.0f,
                0.5f, 0f, 1.0f, 0.0f, 0.0f,
                // Mallets
                0f, -0.25f, 0.0f, 0.0f, 1.0f,
                0f, 0.25f, 1.0f, 0.0f, 0.0f,
                // Puck
                0f, 0f, 0.0f, 0.0f, 0.0f
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

        // COLOR

        // Get the location of our uniform, and we store that location in aColorLocation.
        aColorLocation = glGetAttribLocation(program, A_COLOR);

        // Tell OpenGL where to find data for our attribute aColorLocation
        // When OpenGL starts reading in the color attributes, we want it to start at the first
        // color attribute, not the first position attribute.
        // We need to skip over the first position ourselves by taking the position component size into account,
        vertexData.position(POSITION_COMPONENT_COUNT);

        //Associate our color data with a_Color
        glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);
        // Enable the attribute before we can start drawing
        glEnableVertexAttribArray(aColorLocation);


        // POSITION

        // Get the location of our attribute. With this location,
        // we’ll be able to tell OpenGL where to find the data for this attribute
        aPositionLocation = glGetAttribLocation(program, A_POSITION);


        // Tell OpenGL where to find data for our attribute a_Position
        vertexData.position(0);
        // Associate our color data with a_Position
        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT, false, STRIDE, vertexData);

        // Enable the attribute before we can start drawing
        glEnableVertexAttribArray(aPositionLocation);


        uMatrixLocation = glGetUniformLocation(program, U_MATRIX);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        MyLog.d("");
        // Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);

        //This code will create an orthographic projection matrix that will take the
        //screen’s current orientation into account.
        final float aspectRatio = width > height ?
                (float) width / (float) height :
                (float) height / (float) width;
        if (width > height) {
        // Landscape
            orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f, -1f, 1f);
        } else {
        // Portrait or square
            orthoM(projectionMatrix, 0, -1f, 1f, -aspectRatio, aspectRatio, -1f, 1f);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        MyLog.d("");
        // Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);

        // Send the orthographic projection matrix to the shader
        glUniformMatrix4fv(uMatrixLocation, 1, false, projectionMatrix, 0);

        // Draw border
        glDrawArrays(GL_TRIANGLES, 0, 6);

        // Triangle fan
        glDrawArrays(GL_TRIANGLE_FAN, 6, 6);

        // Line
        glDrawArrays(GL_LINES, 12, 2);

        // Draw the first mallet blue.
        glDrawArrays(GL_POINTS, 14, 1);

        // Draw the second mallet red.
        glDrawArrays(GL_POINTS, 15, 1);

        // Draw puck
        glDrawArrays(GL_POINTS, 16, 1);


    }
}
