package net.playfulsoftware.skunkworks.papercrash;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class PaperCrashActivity extends Activity {
	
	private GLSurfaceView.Renderer renderer;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        renderer = new GameRenderer();
        
        GLSurfaceView glView = (GLSurfaceView) findViewById(R.id.glSurface);
        
        glView.setRenderer(renderer);
    }
}