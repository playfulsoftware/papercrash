uniform mat4 uMVPMatrix;
uniform mat4 MVMatrix;

uniform vec4 center;
uniform vec4 light_pos;

uniform float radius;
uniform float uTicks;

attribute vec4 vPosition;

varying vec3 pos, cen, ldir;
varying float rad, ticks;
 
void main()
{

	ticks = uTicks;
	//ldir = (MVMatrix * light_pos).xyz - pos; 
	ldir = light_pos.xyz - center.xyz; 
	//ldir = light_pos.xyz; 
	pos = (MVMatrix * vPosition).xyz;
	rad = radius;
	cen = (MVMatrix * center).xyz;

	gl_Position = uMVPMatrix * vPosition;
}