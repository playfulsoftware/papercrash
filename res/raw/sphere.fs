precision mediump float;

vec3 specular = vec3 (1.0, 1.0, 1.0);

float shiny = 5.0;

varying vec3 pos, cen, ldir;
varying float rad, ticks;
 
void main()
{        
     
	vec3 color = vec3(0.63671875, 0.76953125, 0.22265625);
	vec3 amb = 0.2 * color;
	vec3 p = pos;
	if (distance(pos.xy, cen.xy) < rad)
	{
		float z = (rad * rad) - ((pos.x - cen.x) * (pos.x - cen.x)) - ((pos.y - cen.y) * (pos.y - cen.y)); 
		z = sqrt (z) + cen.z;
		
		p.z = z;
		vec3 normedP = normalize (p);
		vec3 normal = normalize(p - cen);
				
		vec3 u = normalize (reflect (normedP, normal));
				
		vec3 light_color = vec3(0.5 * (1.0 + sin(ticks / 8.0)), 0.5 * (1.0 + cos(ticks / 8.0)), 0.5 * (1.0 + cos(ticks / 8.0))) * normal;
		
		vec3 l = normalize (ldir);
		vec3 r = normalize (reflect (l, normal));
								
		float ndl = dot (l, normal);
		float intensity = 0.0;
		
		intensity += 0.8 * clamp (ndl, 0.0, 1.0);
		color *= (intensity * light_color);
				
		float rde = max (0.0, dot (r, normedP));
		color += (pow(rde, shiny) * specular);				
				
		gl_FragColor = vec4 (amb + color, 1.0);
		//gl_FragColor = vec4 (amb + (light_color * color), 1.0);
	}												
	else
	{
		gl_FragColor = vec4 (0.0, 0.0, 0.0, 0.0);
	}													

}