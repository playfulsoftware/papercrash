package net.playfulsoftware.skunkworks.papercrash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.util.Log;

public class GameRenderer implements Renderer {

	private Activity parent;
	private ResourceCompiler rc;

	private FloatBuffer boxVB;

	private int mProgram;

	// shader handles.
	private int mMVPMatrixHandle;
	private int mPositionHandle;

	private float boxCoords[] = {
			0.5f, -0.5f, 0,
			0.5f, 0.5f, 0,
			-0.5f, -0.5f, 0,
			-0.5f, 0.5f, 0
	};

	private float[] mMVPMatrix = new float[16];
	private float[] mMMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	private float[] mPMatrix = new float[16];

	private void initBuffers() {
		ByteBuffer vbb = ByteBuffer.allocateDirect(boxCoords.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		boxVB = vbb.asFloatBuffer();
		boxVB.put(boxCoords);
		boxVB.position(0);
	}

	public GameRenderer(Activity parent) {
		this.parent = parent;
		this.rc = new ResourceCompiler(this.parent);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		// clear the screen.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		// use the complied shader.
		GLES20.glUseProgram(mProgram);

		// apply model-view projection transform.
		Matrix.multiplyMM(mMVPMatrix, 0, mPMatrix, 0, mVMatrix, 0);
		GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);

		// pass the triangle vertex data to the shader.
		GLES20.glVertexAttribPointer(mPositionHandle, 3, GLES20.GL_FLOAT, false, 12, boxVB);
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		// TODO Auto-generated method stub
		GLES20.glViewport(0, 0, width, height);

		float ratio = (float) width / height;

		Matrix.frustumM(mPMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
		Matrix.setLookAtM(mVMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// define the clear color
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

		// initialize the vertex buffers.
		initBuffers();

		int vertexShader = rc.createShader(R.raw.ambient, GLES20.GL_VERTEX_SHADER);
		int fragmentShader = rc.createShader(R.raw.flat, GLES20.GL_FRAGMENT_SHADER);

		if (vertexShader == -1 || fragmentShader == -1) {
			Log.d("GameRenderer::onSurfaceCreated", "failed to load shaders");
		}

		mProgram = GLES20.glCreateProgram();
		GLES20.glAttachShader(mProgram, vertexShader);
		GLES20.glAttachShader(mProgram, fragmentShader);
		GLES20.glLinkProgram(mProgram);

		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
		mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
	}

}
