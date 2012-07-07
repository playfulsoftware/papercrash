package net.playfulsoftware.skunkworks.papercrash;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

public class ResourceCompiler implements Closeable {

	private Activity parent;

	public ResourceCompiler(Activity parent) {
		this.parent = parent;
	}

	/**
	 * Builds a shader from the supplied shader source code
	 *
	 * @param source
	 *            Source string for the shader
	 * @param type
	 *            Shader type
	 * @return handle to the compiled shader
	 */
	private int buildShader(String source, int type) {
		// create the shader
		int shader = GLES20.glCreateShader(type);
		// load and compile the shader source.
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);

		return shader;
	}

	private int createTextureId() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		return textures[0];
	}

	public int createShader(int shaderId, int shaderType) {
		try {
			Log.d("createShader", "Trying to load raw shader file");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					parent.getResources().openRawResource(shaderId)));
			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append("\n");
				line = reader.readLine();
			}

			return buildShader(buffer.toString(), shaderType);
		} catch (IOException e) {
			Log.e("GameStart", "Unable to load shader file");
			return -1;
		}
	}

	/**
	 * Takes a resource and returns a texture.
	 *
	 * @param id
	 * @return
	 */
	public int createTexture(int id) {
		int texId = createTextureId();

		// need to flip the textures vertically.
		// NOTE: using the non-OpenGl matrix class since we're not doing a 3d
		// transform on the image (yet).
		android.graphics.Matrix flip = new android.graphics.Matrix();
		flip.postScale(1f, -1f);

		// don't scale bitmaps by default.
		BitmapFactory.Options bmpOpts = new BitmapFactory.Options();
		bmpOpts.inScaled = false;

		// TODO:: Probably need to do an initial scale to a power of two size to
		// make everyone happy.
		// Apparently, not everyone appreciates NPOT textures.
		Bitmap temp = BitmapFactory.decodeResource(parent.getResources(), id,
				bmpOpts);
		Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(),
				temp.getHeight(), flip, true);
		temp.recycle();

		// setup the OpenGL texture object.
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
				GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR_MIPMAP_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
				GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
				GLES20.GL_REPEAT);

		// generate mipmaps
		for (int level = 0, width = bmp.getWidth(), height = bmp.getHeight(); true; level++) {
			// load the texture at the specified mipmap level.
			GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, level, bmp, 0);

			// don't need to go lower than a single pixel mipmap
			if (width == 1 && height == 1)
				break;

			// scale the bitmap down for the next pass.
			width >>= 1;
			height >>= 1;
			width = (width < 1) ? 1 : width;
			height = (height < 1) ? 1 : height;

			Bitmap bmp2 = Bitmap.createScaledBitmap(bmp, width, height, true);
			bmp.recycle();
			bmp = bmp2;
		}

		bmp.recycle();

		return texId;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
