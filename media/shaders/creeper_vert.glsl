#version 130

in vec4 vpos;
in vec3 vN;
in vec2 vtex; 

out vec4 pos;	/*the position to the frag shader */
out vec3 N;	 	/*the vertex normal to be interpolated in the frag shader*/
out vec2 tex;

void main(void) {
	pos = gl_ModelViewMatrix * vpos;
	N = vec3(normalize(gl_NormalMatrix * normalize(vN)));
	tex = vtex;
	gl_Position = gl_ModelViewProjectionMatrix * vpos;
}