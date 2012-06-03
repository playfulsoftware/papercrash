package net.playfulsoftware.skunkworks.papercrash;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class PaperCrashActivity extends Activity {
	private GLSurfaceView glView;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}