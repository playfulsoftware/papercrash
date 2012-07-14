package net.playfulsoftware.skunkworks.papercrash;

import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class PaperCrashActivity extends Activity {

	private GameSurfaceView glView;
	private GLSurfaceView.Renderer renderer;

	private Queue<InputEvent> inputEventQueue;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		inputEventQueue = new LinkedList<InputEvent>();

		renderer = new GameRenderer(this);
		
		((GameRenderer)renderer).setInputQueue(inputEventQueue);

		glView = (GameSurfaceView) findViewById(R.id.glSurface);

		glView.setInputQueue(inputEventQueue);

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
