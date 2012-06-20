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
			
			
			rect = new float[18];
		}
		
		public void updateCenter(float new_coords[])
		{
			
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

	private FloatBuffer green_sphere_vb;
	
    private void initShapes(){
        
        float green_sphere_coords[] = {
            // X, Y, Z
        	
        	-1.0f, 1.0f, 0.0f,
        	-1.0f, -1.0f, 0.0f,
        	1.0f, -1.0f, 0.0f,
        	1.0f, -1.0f, 0.0f,
        	1.0f, 1.0f, 0.0f,
        	-1.0f, 1.0f, 0.0f
        },
        	board_coords[] = {
        		
        		-3.0f, 3.0f, 0.0f,
            	-3.0f, -3.0f, 0.0f,
            	3.0f, -3.0f, 0.0f,
            	3.0f, -3.0f, 0.0f,
            	3.0f, 3.0f, 0.0f,
            	-3.0f, 3.0f, 0.0f	
        		
        };
        
        // initialize vertex Buffer for triangle  
        ByteBuffer vbb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                green_sphere_coords.length * 4); 
        vbb.order(ByteOrder.nativeOrder());// use the device hardware's native byte order
        green_sphere_vb = vbb.asFloatBuffer();  // create a floating point buffer from the ByteBuffer
        green_sphere_vb.put(green_sphere_coords);    // add the coordinates to the FloatBuffer
        green_sphere_vb.position(0);            // set the buffer to read the first coordinate
    
    }
    
    private int mProgram;
    private int maPositionHandle, radiusHandle, centerHandle, ticksHandle;
    
    Sphere phear, smear;
    Sphere spheres[];
	
    private int loadShader(int type, String shaderCode){
        
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type); 
        
        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        
        GLES20.glValidateProgram(shader);
        
        String
        	stat_one = GLES20.glGetShaderInfoLog(shader);
        
        Log.v("shaders", type + " : " + stat_one);
        
        return shader;
    }
    
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
        GLES20.glViewport(0, 0, width, height);
        
        float ratio = (float) width / height;
        
        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        //Matrix.orthoM(mProjMatrix, 0, 0, width, 0, height, -1, 1);
        muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        MVMatrixHandle = GLES20.glGetUniformLocation(mProgram, "MVMatrix");
        
        Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
        
        Log.v("shaders", "surface width = " + width + ", surface height = " + height);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		// Set the background frame color
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        
        // initialize the triangle vertex array
        initShapes();
        
        int vertexShader = rc.createShader("vertex.vs", GLES20.GL_VERTEX_SHADER);
        int fragmentShader = rc.createShader("sphere.fs", GLES20.GL_FRAGMENT_SHADER);
        
        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
       
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                  // creates OpenGL program executables
        
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glEnable(GLES20.GL_BLEND);
        
        // get handle to the vertex shader's vPosition member
        maPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        radiusHandle = GLES20.glGetUniformLocation(mProgram, "radius");
        centerHandle = GLES20.glGetUniformLocation(mProgram, "center");
        ticksHandle = GLES20.glGetUniformLocation(mProgram, "uTicks");
        
        Log.v("shaders", "center handle is " + centerHandle);
        
        phear = new Sphere(-2.0f, 0.0f, 0.0f, 0.5f);
        smear = new Sphere(0.0f, 0.0f, 0.0f, 0.5f);
        
        spheres = new Sphere[6];
        spheres[0] = new Sphere(-1.5f, 1.0f, 0.0f, 0.5f);
        spheres[1] = new Sphere(0.0f, 1.0f, 0.0f, 0.5f);
        spheres[2] = new Sphere(1.5f, 1.0f, 0.0f, 0.5f);
        spheres[3] = new Sphere(-1.5f, -1.0f, 0.0f, 0.5f);
        spheres[4] = new Sphere(0.0f, -1.0f, 0.0f, 0.5f);
        spheres[5] = new Sphere(1.5f, -1.0f, 0.0f, 0.5f);
        
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
	}

}
