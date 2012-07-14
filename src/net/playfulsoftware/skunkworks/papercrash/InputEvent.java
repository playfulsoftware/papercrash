/**
 *
 */
package net.playfulsoftware.skunkworks.papercrash;

/**
 * @author Cary Haynie <cary.haynie@gmail.com>
 * 
 */
public abstract class InputEvent {
	
	public static final int INVALID_EVENT = 0;
	public static final int TAP_EVENT = 1;
	public static final int SWIPE_EVENT = 2;
	
	public abstract int getType();

	public abstract float getX();

	public abstract float getY();

}
