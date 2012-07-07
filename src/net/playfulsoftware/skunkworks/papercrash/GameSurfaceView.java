package net.playfulsoftware.skunkworks.papercrash;

import java.util.Queue;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class GameSurfaceView extends GLSurfaceView {

	class TouchEvent extends InputEvent {

		private float x;
		private float y;

		public TouchEvent(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public float getX() {
			return x;
		}

		@Override
		public float getY() {
			return y;
		}

	}

	private Queue<InputEvent> inputQueue;

	public GameSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setEGLContextClientVersion(2);
	}

	public void setInputQueue(Queue<InputEvent> queue) {
		inputQueue = queue;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		float x = e.getX();
		float y = e.getY();

		TouchEvent te = new TouchEvent(x, y);

		switch (e.getAction()) {
		case MotionEvent.ACTION_CANCEL:
			break;
		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_MOVE:
			inputQueue.offer(te);
			break;
		case MotionEvent.ACTION_UP:
			break;
		default:
			break;
		}
		return true;
	}

}
