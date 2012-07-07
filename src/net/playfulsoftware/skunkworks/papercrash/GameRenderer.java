package net.playfulsoftware.skunkworks.papercrash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.app.Activity;
import android.util.Log;

// TODO: OpenGL 1.0 is LAME
public class GameRenderer implements Renderer {
	
	public class Sphere {
		
		public Sphere(float cx, float cy, float cz, float rad)
		{
			radius = rad;
			center = new float[3];
			center[0] = cx; center[1] = cy; center[2] = cz;
			
			Log.v("shaders", "center_x = " + (new Float(cx)).toString() + 
							 ", center_y = " + (new Float(cy)).toString() +
							 ", center_z = " + (new Float(cz)).toString() 
			);
			rect = new float[18];
		}
		
		public void updateCenter(float new_coords[])
		{
			center[0] = new_coords[0]; center[1] = new_coords[1];
		}
		
		// They shoot free functions in Java, don't they
		public float[] boundingBox(float center[], float radius)
		{	
			float shamus = (float) Math.sqrt(Math.pow(radius, 2) + Math.pow(radius, 2));
			
			rect[0] = center[0] - shamus;
			rect[1] = center[1] - shamus;
			rect[2] = center[2];
			
			rect[3] = center[0] + shamus;
			rect[4] = center[1] - shamus;
			rect[5] = center[2];
			
			rect[6] = center[0] - shamus;
			rect[7] = center[1] + shamus;
			rect[8] = center[2];
			
			rect[9] = center[0] - shamus;
			rect[10] = center[1] + shamus;
			rect[11] = center[2];
			
			rect[12] = center[0] + shamus;
			rect[13] = center[1] - shamus;
			rect[14] = center[2];
			
			rect[15] = center[0] + shamus;
			rect[16] = center[1] + shamus;
			rect[17] = center[2];
			
						
			return rect;
		}
		
		public FloatBuffer getBuffer() {
			
			rect = boundingBox(center, radius);
			
			// TODO: should probably try to avoid recreating these every frame, 
			// and there's probably a more direct path from center -> rect -> buffer
			
			vbb = ByteBuffer.allocateDirect(
	                // (# of coordinate values * 4 bytes per float)
	                rect.length * 4
	        ); 
			
	        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
	        sphere_vb = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
	        sphere_vb.put(rect);    // add the coordinates to the FloatBuffer
	        sphere_vb.position(0);            // set the buffer to read the first coordinate
	        
	        return sphere_vb;
		}
		
		public FloatBuffer sphere_vb;
		public float center[];
		public float rect[];
		public float radius;
		
        public ByteBuffer vbb;

	}
	
	
	float dist(float[] start, float[] end)
	{
		return (float) Math.sqrt((end[0] - start[0]) * (end[0] - start[0]) +
						 (end[1] - start[1]) * (end[1] - start[1]));
	}
	
	class SphereGoal {
		
		SphereGoal(float[] _start, float f, float g)
		{
			start = new float[2];
			start[0] = _start[0]; start[1] = _start[1];
			end = randomEnd(f, g);
			done = false;
			Log.v("shaders", "start: " + start[0] + ", " + start[1]);
			Log.v("shaders", "end: " + end[0] + ", " + end[1]);
		}
		
		float[] randomEnd(float width, float height)
		{
			float rend[] = new float[2];
			
			rend[0] = (float) (Math.random() * width);
			rend[1] = (float) (Math.random() * height);
			
			return rend;
		}
		
		void newGoal(float width, float height)
		{
			done = false;
			ticks = -1;
			end = randomEnd(width, height);
		}
		
		float length() {
			
			return dist(start, end);
		}
		
		float[] tick()
		{
			ticks++;
			
			float interp[] = new float[2];
			
			float t = Math.min((ticks * vel) / length(), 1.0f);
			
			if (t == 1.0f)
			{
				done = true;
				interp = end;
			}
			else
			{
				interp[0] = start[0] + (t * ((end[0] - start[0])));
				interp[1] = start[1] + (t * ((end[1] - start[1])));
			}
			
			//Log.v("shaders", start[0] + ", " + start[1] + ", t= " + t + ", " + done);
			Log.v("shaders", interp[0] + ", " + interp[1] + ", t= " + t + ", " + done);
			return interp;
		}
		
		boolean done = false;
		int ticks = -1;
		float vel = 5;
		float[] start, end;
	}

	private ByteBuffer background_bb;
	private FloatBuffer background_vb;
	
    private int mProgram;
    private int maPositionHandle, radiusHandle, centerHandle, ticksHandle;
    
    Sphere spheres[];
    SphereGoal sphere_goals[];
    
    private int muMVPMatrixHandle, MVMatrixHandle;

    private float[] mProjMatrix = new float[16];
    
    private Random rng;
    private Timer timer;
    private int ticks;
    private float scale;
        
        
	private Activity parent;
	private ResourceCompiler rc;

	private FloatBuffer boxVB;

	// shader handles.
	private int mMVPMatrixHandle;
	private int mPositionHandle, mLightHandle;

	private float[] mMVPMatrix = new float[16];
	private float[] mMMatrix = new float[16];
	private float[] mVMatrix = new float[16];
	private float[] mPMatrix = new float[16];
	
	private float[] light_center = new float[4];

	private int surface_width, surface_height;
	
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

		Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
		
		// Prepare the triangle data
		for(int i = 0; i < spheres.length; i++)
		{
			GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, mVMatrix, 0);
			GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
			GLES20.glUniform1f(ticksHandle, ticks);
			GLES20.glUniform4f(centerHandle, spheres[i].center[0], spheres[i].center[1], spheres[i].center[2], 1.0f);
			GLES20.glUniform4f(mLightHandle, light_center[0], light_center[1], light_center[2], 1.0f);
			GLES20.glUniform1f(radiusHandle, spheres[i].radius);
			GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, spheres[i].getBuffer());
			GLES20.glEnableVertexAttribArray(maPositionHandle);
			// Draw the triangles
			GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        
        float ratio = (float) width / height;
        
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        //Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.orthoM(mProjMatrix, 0, 0, width, 0, height, -1, 1);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "MVMatrix");
        
        Matrix.setIdentityM(mVMatrix, 0);
        //Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        
        surface_width = width; surface_height = height;
        Log.v("shaders", "surface width = " + width + ", surface height = " + height);
        
        spheres = new Sphere[1];
        spheres[0] = new Sphere(surface_width / 2.0f, surface_height / 2.0f, 0.0f, surface_width / 16.0f);
        
        sphere_goals = new SphereGoal[1];
        sphere_goals[0] = new SphereGoal(spheres[0].center, surface_width - (2 * spheres[0].radius), 
        								 surface_height - (2 * spheres[0].radius));
        
        /*
        light_center[0] = surface_width / 2.0f + 50.0f;
        light_center[1] = surface_height / 2.0f - 50.0f;
        light_center[2] = -1.0f;
        light_center[3] = 1.0f;
        */
        light_center[0] = 0.0f;
        light_center[1] = 100.0f;
        light_center[2] = -1.0f;
        light_center[3] = 1.0f;
        
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		// Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        
        int vertexShader = rc.createShader(R.raw.vertex, GLES20.GL_VERTEX_SHADER);
        int fragmentShader = rc.createShader(R.raw.sphere, GLES20.GL_FRAGMENT_SHADER);
        
        //int vertexShader = rc.createShader(R.raw.flat, GLES20.GL_VERTEX_SHADER);
        //int fragmentShader = rc.createShader(R.raw.ambient, GLES20.GL_FRAGMENT_SHADER);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
       
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL program executables
        
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        
        // get handle to the vertex shader's vPosition member
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        radiusHandle = GLES20.glGetUniformLocation(mProgram, "radius");
        mLightHandle = GLES20.glGetUniformLocation(mProgram, "light_pos");
        centerHandle = GLES20.glGetUniformLocation(mProgram, "center");
        ticksHandle = GLES20.glGetUniformLocation(mProgram, "uTicks");
        
        rng = new Random();
        
        scale = 1.0f;
        ticks = 0;
        timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				tick();
			}

		}, 0, 1000 / 30);
	}
	
	public void tick()
	{
		scale = Math.min( 
				0.5f * (1.0f + (float) (Math.cos(ticks * Math.PI / 180.0f))), 
				1.0f);
		ticks++;
		
		if (spheres != null)
		{
			for(int i = 0; i < spheres.length; i++)
			{
				if (spheres[i] == null)
				{
					continue;
				}
				
				spheres[i].updateCenter(sphere_goals[i].tick());
				
				if (sphere_goals[i].done)
				{
					Log.v("shaders", "resetting!");
					
					sphere_goals[i].start[0] = spheres[i].center[0];
					sphere_goals[i].start[1] = spheres[i].center[1];
					sphere_goals[i].newGoal(surface_width - (2.0f * spheres[i].radius), 
							surface_height - (2.0f * spheres[i].radius));
				}
			}
		}
	}

}
