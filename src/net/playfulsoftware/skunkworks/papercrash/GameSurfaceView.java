package net.playfulsoftware.skunkworks.papercrash;

import java.util.Queue;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class GameSurfaceView extends GLSurfaceView {

	private Queue<InputEvent> inputQueue;
	private GestureProcessor gestProc;

	public GameSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEGLContextClientVersion(2);
		
		gestProc = new GestureProcessor();
	}

	public void setInputQueue(Queue<InputEvent> queue) {
		inputQueue = queue;
		if (gestProc != null) {
			gestProc.setInputQueue(inputQueue);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		return gestProc.process(e);
	}

}
