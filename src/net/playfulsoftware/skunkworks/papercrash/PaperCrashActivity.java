package net.playfulsoftware.skunkworks.papercrash;

import android.app.Activity;
import android.os.Bundle;

public class PaperCrashActivity extends Activity {
	
	private GameSurfaceView.Renderer renderer;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        renderer = new GameRenderer();
        
        GameSurfaceView glView = (GameSurfaceView) findViewById(R.id.glSurface);
        
        glView.setRenderer(renderer);
    }
    
    /** Called when paused. */
    @Override
    public void onPause() {
        super.onPause();
    }
    
    /** Called when resumed. */
    @Override
    public void onResume() {
    	super.onResume();
    }
}