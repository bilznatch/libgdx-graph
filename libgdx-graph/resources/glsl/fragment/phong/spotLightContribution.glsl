Lighting getSpotPhongLightContribution(vec3 pos, vec3 N, float shininess, Lighting lighting) {
    vec3 V = normalize(u_cameraPosition.xyz - pos.xyz);
    for (int i = 0; i < NUM_SPOT_LIGHTS; i++) {
        vec3 L = u_spotLights[i].position - pos.xyz;
        float distanceSquared = dot(L, L);
        float attenuation = 1.0 / (1.0 + distanceSquared);

        L = normalize(L);

        float cd = dot(u_spotLights[i].direction, -L);
        float angularAttenuation = 0.0;
        if (cd > cos(u_spotLights[i].cutoffAngle)) {
            angularAttenuation = clamp(cd * u_spotLights[i].exponent, 0.0, 1.0);
            angularAttenuation *= angularAttenuation;
        }

        vec3 R = reflect(-L, N);

        float diffuse = clamp(dot(N, L), 0.0, 1.0);
        float specular = pow(clamp(dot(V, R), 0.0, 1.0), shininess);

        lighting.diffuse += u_spotLights[i].color * attenuation * angularAttenuation * diffuse;
        lighting.specular += (diffuse > 0.0 ? 1.0 : 0.0) * u_spotLights[i].color * attenuation * angularAttenuation * specular;
    }
    return lighting;
}
