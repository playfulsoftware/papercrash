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
    
    private final String boardVS = 
    	"varying vec3 position;" +
		"void main()" +
		"{" +
		"	position = gl_Vertex.xyz;" +
		"	gl_Position = ftransform();" +
		"	gl_TexCoord[0] = -gl_MultiTexCoord0;" +
    	"}";
    
    private final String vertexShaderCode = 
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;   		\n" +
            "uniform vec4 center;				\n" +
            "uniform mat4 MVMatrix;   		\n" +
            "uniform float radius;				\n" +
            "uniform float uTicks;				\n" +
            
            "attribute vec4 vPosition;  		\n" +
            
            "vec4 lPos = vec4 (1.0, 2.0, -6.5, 1.0); \n" +
            
			"varying vec3 pos, cen, ldir;	\n" +
			"varying float rad, ticks; \n" + 
            "void main(){               		\n" +
            
				"ticks = uTicks;" +
				"pos = (MVMatrix * vPosition).xyz; " +
				"rad = radius; " +
				"cen = (MVMatrix * center).xyz; " +
				"ldir = (MVMatrix * lPos).xyz - pos; " +
            	// the matrix must be included as a modifier of gl_Position
            	"gl_Position = uMVPMatrix * vPosition; \n" +
            
            	
            //	"position = (MVMatrix * vPosition).xyz;				\n" +
            
            "}  \n";
        
    private final String fragmentShaderCode = 
           // "uniform float radius;				\n" +
	        "precision mediump float;  \n" +
	        "vec3 Specular = vec3 (1.0, 1.0, 1.0); " +
	        "vec3 lDir = vec3 (3.0, -3.0, 8.0); " +
	        "vec3 lCol = vec3 (0.3, 0.2, 0.8); " +
    		"float shiny = 5.0; " +
	        "varying vec3 pos, cen, ldir;	\n" +
	        "varying float rad, ticks; \n" +
	        "void main(){              \n" +
	        //"	gl_FragColor = vec4(position.x, position.y, position.z, 1.0); " +
	        "	vec3 color = vec3(0.63671875, 0.76953125, 0.22265625); " +
	        "   vec3 amb = 0.2 * color; " +
	        "	vec3 p = pos; " +
	        "	if (distance(pos.xy, cen.xy) < rad) " +
	        "	{ " +
	        //" gl_FragColor = vec4 (0.63671875, 0.76953125, 0.22265625, 1.0); \n" +
	//" float z = (radius * radius) - ((pos.x - cen.x) * (pos.x - cen.x)) - ((pos.y - cen.y) * (pos.y - cen.y)); " +
	        		" float z = (rad * rad) - ((pos.x - cen.x) * (pos.x - cen.x)) - ((pos.y - cen.y) * (pos.y - cen.y)); " + 
    				" z = sqrt (z) + cen.z; " +

					" p.z = z; " + 
					"vec3 normedP = normalize (p); " +
    				"vec3 normal = normalize(p - cen); " +
    				
    				"vec3 u = normalize (reflect (normedP, normal)); " +				
    				
    				//"lCol.rgb *= vec3(0.5 * (1.0 + sin(ticks)), 0.5 * (1.0 + cos(ticks)), sin(ticks) * cos(ticks));" +
    				"vec3 light_color = vec3(0.5 * (1.0 + sin(ticks / 8.0)), 0.5 * (1.0 + cos(ticks / 8.0)), sin(ticks / 8.0) * cos(ticks / 8.0)) * normal;" +
    				"vec3 l = normalize (ldir); " +
    				"vec3 r = normalize (reflect (l, normal)); " + 						
    				"float ndl = dot (l, normal); " +
    				"float intensity = 0.0; " +
    				"intensity += 0.8 * clamp (ndl, 0.0, 1.0); " +
    				"color *= (intensity * light_color); " +
    				
    				"float rde = max (0.0, dot (r, normedP)); " +
    				"color += (pow(rde, shiny) * Specular); " +				
    				
    				"gl_FragColor = vec4 (amb + color, 1.0); " +
	        "	} "													+
	        "	else "												+
	        "	{ "													+
	        "		gl_FragColor = vec4 (0.0, 0.0, 0.0, 0.0); \n" +
	        "	}													" +
	        
	        "}                         \n";
	        
    
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
    private float[] mMVPMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    
    private Random rng;
    private Timer timer;
    private int ticks;
    private float scale;
    
	@Override
	public void onDrawFrame(GL10 gl) {
        
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        
        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);
        
        Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);
                 
        // Prepare the triangle data
        
        for(int i = 0; i < spheres.length; i++)
        {
        	GLES20.glUniformMatrix4fv(MVMatrixHandle, 1, false, mVMatrix, 0);
            GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0);
            
            //GLES20.glUniform1f(radiusHandle, 0.4f + (0.6f * scale));
            
            GLES20.glUniform1f(ticksHandle, ticks);
        	GLES20.glUniform4f(centerHandle, spheres[i].center[0], spheres[i].center[1], spheres[i].center[2], 1.0f);
        	GLES20.glUniform1f(radiusHandle, spheres[i].radius * scale);
        	GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false, 12, spheres[i].getBuffer());
        	GLES20.glEnableVertexAttribArray(maPositionHandle);
        	
        	// Draw the triangle
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 6);
        }
        
        
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
        
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        
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
