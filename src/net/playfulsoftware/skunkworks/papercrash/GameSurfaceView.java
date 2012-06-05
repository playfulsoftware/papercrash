package net.playfulsoftware.skunkworks.papercrash;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GameSurfaceView extends GLSurfaceView {

	public GameSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		setEGLContextClientVersion(2);
	}

}
