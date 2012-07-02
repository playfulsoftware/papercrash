package net.playfulsoftware.skunkworks.papercrash;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.res.AssetManager;
import android.opengl.GLES20;
import android.util.Log;

public class ResourceCompiler implements Closeable {

	private Activity parent;

	public ResourceCompiler(Activity parent) {
		this.parent = parent;
	}

	private int buildShader(String source, int type) {
		// create the shader
		int shader = GLES20.glCreateShader(type);
		// load and compile the shader source.
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);

		return shader; 
	}

	public int createShader(int shaderId, int shaderType) {
		try {
			Log.d("createShader", "Trying to load raw shader file");
			BufferedReader reader = new BufferedReader(new InputStreamReader(parent.getResources().openRawResource(shaderId)));
			StringBuffer buffer = new StringBuffer(); 
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append("\n");
				line = reader.readLine();
			}

			return buildShader(buffer.toString(), shaderType);
		} catch (IOException e)
		{
			Log.e("GameStart", "Unable to load shader file");
			return -1;
		}
	}

	public int createShader(String fileName, int shaderType) {
		AssetManager am = parent.getAssets();

		try {
			Log.d("createShader", "Trying to load raw shader file");
			BufferedReader reader = new BufferedReader(new InputStreamReader(am.open(fileName, AssetManager.ACCESS_BUFFER)));
			StringBuffer buffer = new StringBuffer();
			String line = reader.readLine();
			while (line != null) {
				buffer.append(line);
				buffer.append("\n");
				line = reader.readLine();
			}

			return buildShader(buffer.toString(), shaderType);

		} catch (IOException e) {
			Log.e("GameStart", "Unable to load shader file: " + fileName);
			return -1;
		}
	}


	/** Take a drawable resource id and returns a texture id. */
	public int createTexture(int id) {
		return 0;
	}

	public int createTexture(String fileName) {
		return 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

}
