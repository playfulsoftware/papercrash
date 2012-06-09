package net.playfulsoftware.skunkworks.papercrash;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import android.content.res.AssetManager;

public class ResourceCompiler implements Closeable {
	
	private AssetManager assets;
	
	public ResourceCompiler(AssetManager assets) {
		this.assets = assets;
	}
	
	/** Takes a string list resource id representing a shader, compiles it, and returns the program. */
	public int createShader(String path) {
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(path, AssetManager.ACCESS_BUFFER)));
		} catch (IOException e) {
			
		}
		
		return 0;
	}
	
	/** Take a drawable resource id and returns a texture id. */
	public int createTexture(int id) {
		return 0;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

}
