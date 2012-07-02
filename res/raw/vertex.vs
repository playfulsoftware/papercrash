uniform mat4 uMVPMatrix;
uniform vec4 center;
uniform mat4 MVMatrix;
uniform float radius;
uniform float uTicks;

attribute vec4 vPosition;

vec4 lPos = vec4 (1.0, 0.0, -6.5, 1.0);

varying vec3 pos, cen, ldir;
varying float rad, ticks;
 
void main()
{

	ticks = uTicks;
	pos = (MVMatrix * vPosition).xyz;
	rad = radius;
	cen = (MVMatrix * center).xyz;
	ldir = (MVMatrix * lPos).xyz - pos; 

	gl_Position = uMVPMatrix * vPosition;
}