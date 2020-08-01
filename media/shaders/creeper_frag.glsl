#version 130

in vec4 pos;
in vec3 N;
in vec2 tex;
uniform sampler2D texUnit1;

void main (void) {
	vec3 lightDir = normalize(gl_LightSource[0].position.xyz - pos.xyz);	//directional/point light, treated the same
	vec3 viewDir = normalize(-pos.xyz);
	vec3 norm = normalize(N);
	
	float lambertDot = max(0, dot(lightDir, norm));
	vec4 globalAmbient = gl_LightModel.ambient * gl_FrontMaterial.ambient;
	vec4 ambient = gl_LightSource[0].ambient * gl_FrontMaterial.ambient;
	vec4 diffuse = lambertDot * gl_LightSource[0].diffuse * gl_FrontMaterial.diffuse;
						
	vec3 h = normalize(viewDir+lightDir);
	vec4 specular = vec4(0.0, 0.0, 0.0, 1.0);
	
	if (lambertDot > 0.0) {
		float base = max(dot(norm, h), 0.0);
		specular = gl_LightSource[0].specular * gl_FrontMaterial.specular*pow(base, gl_FrontMaterial.shininess);
	}	
	gl_FragColor = texture(texUnit1, tex)*(gl_FrontMaterial.emission + globalAmbient + ambient + diffuse) + specular;
}