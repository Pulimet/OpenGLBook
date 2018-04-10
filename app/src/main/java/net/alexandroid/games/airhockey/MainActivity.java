package net.alexandroid.games.airhockey;

import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import net.alexandroid.utils.mylog.MyLog;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ZAQ";
    private GLSurfaceView mGlSurfaceView;
    private boolean rendererSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyLog.init(getApplicationContext());
        MyLog.setTag("ZAQ");

        if (!isSystemSupportOpenGLv2()) {
            MyLog.e("onCreate: OpenGL 2.0 not supported");
            Toast.makeText(this, "This device does not support OpenGL ES 2.0.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        createGLSurfaceView();
        setContentView(mGlSurfaceView);

    }

    private boolean isSystemSupportOpenGLv2() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager != null && activityManager.getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

    private void createGLSurfaceView() {
        MyLog.d("");
        mGlSurfaceView = new GLSurfaceView(this);
        mGlSurfaceView.setEGLContextClientVersion(2);
        mGlSurfaceView.setRenderer(new AirHockeyRenderer(this));
        mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        rendererSet = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.d("");
        if (rendererSet) {
            mGlSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.d("");
        if (rendererSet) {
            mGlSurfaceView.onPause();
        }
    }
}
