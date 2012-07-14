/**
 * 
 */
package net.playfulsoftware.skunkworks.papercrash;

import java.util.LinkedList;
import java.util.Queue;

import android.util.Log;
import android.view.MotionEvent;

/**
 * @author cary
 * 
 */
public class GestureProcessor {

	class RawTouchEvent {

		private float x;
		private float y;

		private int action;

		public RawTouchEvent(int action, float x, float y) {
			this.x = x;
			this.y = y;
			this.action = action;
		}

		public boolean equals(RawTouchEvent other) {
			return x == other.getX() && y == other.getY();
		}

		public int getActionType() {
			return action;
		}

		public float getX() {
			return x;
		}

		public float getY() {
			return y;
		}

	}

	class SwipeEvent extends InputEvent {

		class Coords {
			public float x;
			public float y;

			public Coords(float x, float y) {
				this.x = x;
				this.y = y;
			}
		}

		private LinkedList<Coords> coordList;

		public SwipeEvent() {
			coordList = new LinkedList<Coords>();
		}

		public void addCoords(float x, float y) {
			coordList.offer(new Coords(x, y));
		}

		@Override
		public int getType() {
			return InputEvent.SWIPE_EVENT;
		}

		@Override
		public float getX() {
			Coords current = coordList.peek();
			if (current == null) {
				return Float.NaN;
			} else {
				return current.x;
			}
		}

		@Override
		public float getY() {
			Coords current = coordList.peek();
			if (current == null) {
				return Float.NaN;
			} else {
				return current.y;
			}
		}
	}

	class TapEvent extends InputEvent {
		private float x;
		private float y;

		public TapEvent(float x, float y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int getType() {
			return InputEvent.TAP_EVENT;
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

	class GestureNode {
		private RawTouchEvent te;

		public GestureNode next;

		public GestureNode(RawTouchEvent te) {
			this.te = te;

			this.next = null;
		}

		public RawTouchEvent getEvent() {
			return te;
		}
	}

	private Queue<InputEvent> inputQueue;

	private GestureNode head, last;

	public GestureProcessor() {
		head = null;
		last = null;
	}

	public boolean process(MotionEvent e) {
		int action = e.getAction();
		float x = e.getX();
		float y = e.getY();

		RawTouchEvent te = new RawTouchEvent(action, x, y);
		GestureNode ge = new GestureNode(te);

		switch (action) {
		case MotionEvent.ACTION_CANCEL:
			Log.d("input_events", String.format("CANCEL X: %f, Y: %f", x, y));
			head = last = null;
			break;
		case MotionEvent.ACTION_DOWN:
			Log.d("input_events", String.format("DOWN X: %f, Y: %f", x, y));
			head = last = ge;
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d("input_events", String.format("MOVE X: %f, Y: %f", x, y));
			last.next = ge;
			last = ge;
			break;
		case MotionEvent.ACTION_OUTSIDE:
			Log.d("input_events", String.format("OUTSIDE X: %f, Y: %f", x, y));
			break;
		case MotionEvent.ACTION_SCROLL:
			Log.d("input_events", String.format("SCROLL X: %f, Y: %f", x, y));
			break;
		case MotionEvent.ACTION_UP:
			Log.d("input_events", String.format("UP X: %f, Y: %f", x, y));
			last.next = ge;
			last = ge;
			processEvent();
			break;
		}

		return true;
	}

	public void setInputQueue(Queue<InputEvent> queue) {
		inputQueue = queue;
	}

	private void processEvent() {
		GestureNode current = head;

		while (current != null) {
			if (current.te.getActionType() == MotionEvent.ACTION_MOVE) {
				createSwipeEvent();
				return;
			}
			current = current.next;
		}
		createTapEvent();
	}

	private void createSwipeEvent() {
		SwipeEvent se = new SwipeEvent();
		for (GestureNode current = head; current != null; current = current.next) {
			if (current.te.getActionType() == MotionEvent.ACTION_MOVE) {
				se.addCoords(current.te.getX(), current.te.getY());
			}
		}
		Log.d("input_events", "Created SwipeEvent");
		inputQueue.offer(se);

	}

	private void createTapEvent() {
		float x = head.te.getX();
		float y = head.te.getY();

		Log.d("input_events", String.format("Created TapEvent: (%f, %f)", x, y));
		inputQueue.offer(new TapEvent(x, y));
		head = null;
	}

}
