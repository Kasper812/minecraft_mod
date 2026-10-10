#version 330

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

float luminance(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

void main() {
    vec3 col = texture(InSampler, texCoord).rgb;
    float l = luminance(col);
    if (l > 0.6) {
        fragColor = vec4(1.0);
        return;
    }
    fragColor = vec4(vec3(l), 1.0);
}
