package net.playfulsoftware.skunkworks.papercrash;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class PaperCrashActivity extends Activity {
	
	private GameSurfaceView glView;
	private GLSurfaceView.Renderer renderer;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        renderer = new GameRenderer();
        
        glView = (GameSurfaceView) findViewById(R.id.glSurface);
        
        glView.setRenderer(renderer);
    }
    
    /** Called when paused. */
    @Override
    public void onPause() {
    	super.onPause();
    	glView.onPause();
    }
    
    /** Called when resumed. */
    @Override
    public void onResume() {
    	super.onResume();
    	glView.onResume();
    }
}