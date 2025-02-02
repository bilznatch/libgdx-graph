package com.gempukku.libgdx.graph.plugin.lighting3d.producer;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import com.gempukku.libgdx.graph.plugin.lighting3d.Directional3DLight;
import com.gempukku.libgdx.graph.plugin.lighting3d.LightColor;
import com.gempukku.libgdx.graph.plugin.lighting3d.Lighting3DEnvironment;
import com.gempukku.libgdx.graph.plugin.lighting3d.Lighting3DPrivateData;
import com.gempukku.libgdx.graph.plugin.lighting3d.Point3DLight;
import com.gempukku.libgdx.graph.plugin.lighting3d.Spot3DLight;
import com.gempukku.libgdx.graph.shader.BasicShader;
import com.gempukku.libgdx.graph.shader.GraphShader;
import com.gempukku.libgdx.graph.shader.GraphShaderContext;
import com.gempukku.libgdx.graph.shader.ShaderContext;
import com.gempukku.libgdx.graph.shader.ShaderFieldType;
import com.gempukku.libgdx.graph.shader.UniformRegistry;
import com.gempukku.libgdx.graph.shader.UniformSetters;
import com.gempukku.libgdx.graph.shader.builder.FragmentShaderBuilder;
import com.gempukku.libgdx.graph.shader.builder.GLSLFragmentReader;
import com.gempukku.libgdx.graph.shader.builder.VertexShaderBuilder;
import com.gempukku.libgdx.graph.shader.node.ConfigurationShaderNodeBuilder;
import com.gempukku.libgdx.graph.shader.node.DefaultFieldOutput;
import com.gempukku.libgdx.graph.util.LibGDXCollections;

public class BlinnPhongLightingShaderNodeBuilder extends ConfigurationShaderNodeBuilder {
    private int maxNumberOfDirectionalLights;
    private int maxNumberOfPointLights;
    private int maxNumberOfSpotlights;

    public BlinnPhongLightingShaderNodeBuilder(int maxNumberOfDirectionalLights, int maxNumberOfPointLights, int maxNumberOfSpotlights) {
        super(new BlinnPhongLightingShaderNodeConfiguration());
        this.maxNumberOfDirectionalLights = maxNumberOfDirectionalLights;
        this.maxNumberOfPointLights = maxNumberOfPointLights;
        this.maxNumberOfSpotlights = maxNumberOfSpotlights;
    }

    @Override
    public ObjectMap<String, ? extends FieldOutput> buildVertexNodeSingleInputs(boolean designTime, String nodeId, JsonValue data, ObjectMap<String, FieldOutput> inputs, ObjectSet<String> producedOutputs, VertexShaderBuilder vertexShaderBuilder, GraphShaderContext graphShaderContext, GraphShader graphShader) {
        throw new UnsupportedOperationException("At the moment light calculation is not available in vertex shader");
    }

    @Override
    public ObjectMap<String, ? extends FieldOutput> buildFragmentNodeSingleInputs(boolean designTime, String nodeId, final JsonValue data, ObjectMap<String, FieldOutput> inputs, ObjectSet<String> producedOutputs, VertexShaderBuilder vertexShaderBuilder, FragmentShaderBuilder fragmentShaderBuilder, final GraphShaderContext graphShaderContext, GraphShader graphShader) {
        fragmentShaderBuilder.addStructure("Lighting",
                "  vec3 diffuse;\n" +
                        "  vec3 specular;\n");

        fragmentShaderBuilder.addUniformVariable("u_ambientLight", "vec3", true,
                new UniformRegistry.UniformSetter() {
                    @Override
                    public void set(BasicShader shader, int location, ShaderContext shaderContext) {
                        Lighting3DEnvironment environment = shaderContext.getPrivatePluginData(Lighting3DPrivateData.class).getEnvironment(data.getString("id", ""));
                        if (environment != null && environment.getAmbientColor() != null) {
                            LightColor ambientColor = environment.getAmbientColor();
                            shader.setUniform(location, ambientColor.getRed(), ambientColor.getGreen(), ambientColor.getBlue());
                        } else {
                            shader.setUniform(location, 0f, 0f, 0f);
                        }
                    }
                });

        final int numDirectionalLights = maxNumberOfDirectionalLights;
        passDirectionalLights(data, fragmentShaderBuilder, numDirectionalLights);
        final int numPointLights = maxNumberOfPointLights;
        passPointLights(data, fragmentShaderBuilder, numPointLights);
        final int numSpotLights = maxNumberOfSpotlights;
        passSpotLights(data, fragmentShaderBuilder, numSpotLights);

        FieldOutput positionValue = inputs.get("position");
        FieldOutput normalValue = inputs.get("normal");
        FieldOutput albedoValue = inputs.get("albedo");
        FieldOutput emissionValue = inputs.get("emission");
        FieldOutput specularValue = inputs.get("specular");
        FieldOutput ambientOcclusionValue = inputs.get("ambientOcclusion");
        FieldOutput shininessValue = inputs.get("shininess");

        String position = positionValue.getRepresentation();
        String normal = normalValue.getRepresentation();
        String albedo = albedoValue != null ? albedoValue.getRepresentation() : "vec3(0.0)";
        String emission = emissionValue != null ? emissionValue.getRepresentation() : "vec3(0.0)";
        String specular = specularValue != null ? specularValue.getRepresentation() : "vec3(1.0)";
        String ambientOcclusion = ambientOcclusionValue != null ? ambientOcclusionValue.getRepresentation() : "1.0";
        String shininess = shininessValue != null ? shininessValue.getRepresentation() : "32.0";

        fragmentShaderBuilder.addMainLine("// Blinn-Phong Lighting node");
        String lightingVariable = "lighting_" + nodeId;
        String normalVariable = "normal_" + nodeId;
        fragmentShaderBuilder.addMainLine("Lighting " + lightingVariable + " = Lighting(vec3(0.0), vec3(0.0));");
        fragmentShaderBuilder.addMainLine("vec3 " + normalVariable + " = normalize(" + normal + ");");
        if (numDirectionalLights > 0)
            fragmentShaderBuilder.addMainLine(lightingVariable + " = getDirectionalBlinnPhongLightContribution(" + position + ", " + normalVariable + ", " + shininess + ", " + lightingVariable + ");");
        if (numPointLights > 0)
            fragmentShaderBuilder.addMainLine(lightingVariable + " = getPointBlinnPhongLightContribution(" + position + ", " + normalVariable + ", " + shininess + ", " + lightingVariable + ");");
        if (numSpotLights > 0)
            fragmentShaderBuilder.addMainLine(lightingVariable + " = getSpotBlinnPhongLightContribution(" + position + ", " + normalVariable + ", " + shininess + ", " + lightingVariable + ");");

        ShaderFieldType resultType = ShaderFieldType.Vector3;
        ObjectMap<String, DefaultFieldOutput> result = new ObjectMap<>();
        if (producedOutputs.contains("output")) {
            String name = "color_" + nodeId;
            fragmentShaderBuilder.addMainLine(resultType.getShaderType() + " " + name + " = " + emission + ".rgb + " + ambientOcclusion + " * u_ambientLight * " + albedo + ".rgb;");
            fragmentShaderBuilder.addMainLine(name + " += " + lightingVariable + ".diffuse * " + albedo + ".rgb + " + lightingVariable + ".specular * " + specular + ".rgb;");
            result.put("output", new DefaultFieldOutput(resultType, name));
        }
        if (producedOutputs.contains("diffuse")) {
            result.put("diffuse", new DefaultFieldOutput(resultType, lightingVariable + ".diffuse"));
        }
        if (producedOutputs.contains("specularOut")) {
            result.put("specularOut", new DefaultFieldOutput(resultType, lightingVariable + ".specular"));
        }

        return result;
    }

    private void passSpotLights(final JsonValue data, FragmentShaderBuilder fragmentShaderBuilder, final int numSpotLights) {
        if (numSpotLights > 0) {
            fragmentShaderBuilder.addUniformVariable("u_cameraPosition", "vec4", true, UniformSetters.cameraPosition);
            fragmentShaderBuilder.addStructure("SpotLight",
                    "  vec3 color;\n" +
                            "  vec3 position;\n" +
                            "  vec3 direction;\n" +
                            "  float cutoffAngle;\n" +
                            "  float exponent;\n");
            fragmentShaderBuilder.addStructArrayUniformVariable("u_spotLights", new String[]{"color", "position", "direction", "cutoffAngle", "exponent"}, numSpotLights, "SpotLight", true,
                    new UniformRegistry.StructArrayUniformSetter() {
                        @Override
                        public void set(BasicShader shader, int startingLocation, int[] fieldOffsets, int structSize, ShaderContext shaderContext) {
                            Array<Spot3DLight> spots = null;
                            Lighting3DEnvironment environment = shaderContext.getPrivatePluginData(Lighting3DPrivateData.class).getEnvironment(data.getString("id", ""));
                            if (environment != null) {
                                spots = environment.getSpotLights();
                            }

                            for (int i = 0; i < numSpotLights; i++) {
                                int location = startingLocation + i * structSize;
                                if (spots != null && i < spots.size) {
                                    Spot3DLight spotLight = spots.get(i);
                                    LightColor color = spotLight.getColor();
                                    float intensity = spotLight.getIntensity();
                                    Vector3 position = spotLight.getPosition();

                                    shader.setUniform(location, color.getRed() * intensity,
                                            color.getGreen() * intensity, color.getBlue() * intensity);
                                    shader.setUniform(location + fieldOffsets[1], position.x, position.y, position.z);
                                    shader.setUniform(location + fieldOffsets[2], spotLight.getDirectionX(), spotLight.getDirectionY(),
                                            spotLight.getDirectionZ());
                                    shader.setUniform(location + fieldOffsets[3], spotLight.getCutoffAngle());
                                    shader.setUniform(location + fieldOffsets[4], spotLight.getExponent());
                                } else {
                                    shader.setUniform(location, 0f, 0f, 0f);
                                    shader.setUniform(location + fieldOffsets[1], 0f, 0f, 0f);
                                    shader.setUniform(location + fieldOffsets[2], 0f, 0f, 0f);
                                    shader.setUniform(location + fieldOffsets[3], 0f);
                                    shader.setUniform(location + fieldOffsets[4], 0f);
                                }
                            }
                        }
                    });
            if (!fragmentShaderBuilder.containsFunction("getSpotBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getSpotBlinnPhongLightContribution",
                        GLSLFragmentReader.getFragment("blinn-phong/spotLightContribution",
                                LibGDXCollections.singletonMap("NUM_SPOT_LIGHTS", String.valueOf(numSpotLights))));
            }
        } else {
            if (!fragmentShaderBuilder.containsFunction("getSpotBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getSpotBlinnPhongLightContribution",
                        "Lighting getSpotLightContribution(vec4 pos, vec3 normal, float shininess, Lighting lighting) {\n" +
                                "  return lighting;\n" +
                                "}\n");
            }
        }
    }

    private void passPointLights(final JsonValue data, FragmentShaderBuilder fragmentShaderBuilder, final int numPointLights) {
        if (numPointLights > 0) {
            fragmentShaderBuilder.addUniformVariable("u_cameraPosition", "vec4", true, UniformSetters.cameraPosition);
            fragmentShaderBuilder.addStructure("PointLight",
                    "  vec3 color;\n" +
                            "  vec3 position;\n");
            fragmentShaderBuilder.addStructArrayUniformVariable("u_pointLights", new String[]{"color", "position"}, numPointLights, "PointLight", true,
                    new UniformRegistry.StructArrayUniformSetter() {
                        @Override
                        public void set(BasicShader shader, int startingLocation, int[] fieldOffsets, int structSize, ShaderContext shaderContext) {
                            Array<Point3DLight> points = null;
                            Lighting3DEnvironment environment = shaderContext.getPrivatePluginData(Lighting3DPrivateData.class).getEnvironment(data.getString("id", ""));
                            if (environment != null) {
                                points = environment.getPointLights();
                            }

                            for (int i = 0; i < numPointLights; i++) {
                                int location = startingLocation + i * structSize;
                                if (points != null && i < points.size) {
                                    Point3DLight pointLight = points.get(i);
                                    LightColor color = pointLight.getColor();
                                    float intensity = pointLight.getIntensity();
                                    Vector3 position = pointLight.getPosition();

                                    shader.setUniform(location, color.getRed() * intensity,
                                            color.getGreen() * intensity, color.getBlue() * intensity);
                                    shader.setUniform(location + fieldOffsets[1], position.x, position.y, position.z);
                                } else {
                                    shader.setUniform(location, 0f, 0f, 0f);
                                    shader.setUniform(location + fieldOffsets[1], 0f, 0f, 0f);
                                }
                            }
                        }
                    });
            if (!fragmentShaderBuilder.containsFunction("getPointBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getPointBlinnPhongLightContribution",
                        GLSLFragmentReader.getFragment("blinn-phong/pointLightContribution",
                                LibGDXCollections.singletonMap("NUM_POINT_LIGHTS", String.valueOf(numPointLights))));
            }
        } else {
            if (!fragmentShaderBuilder.containsFunction("getPointBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getPointBlinnPhongLightContribution",
                        "Lighting getPointLightContribution(vec4 pos, vec3 normal, float shininess,  lighting) {\n" +
                                "  return lighting;\n" +
                                "}\n");
            }
        }
    }

    private void passDirectionalLights(final JsonValue data, FragmentShaderBuilder fragmentShaderBuilder, final int numDirectionalLights) {
        if (numDirectionalLights > 0) {
            fragmentShaderBuilder.addUniformVariable("u_cameraPosition", "vec4", true, UniformSetters.cameraPosition);
            fragmentShaderBuilder.addStructure("DirectionalLight",
                    "  vec3 color;\n" +
                            "  vec3 direction;\n");
            fragmentShaderBuilder.addStructArrayUniformVariable("u_dirLights", new String[]{"color", "direction"}, numDirectionalLights, "DirectionalLight", true,
                    new UniformRegistry.StructArrayUniformSetter() {
                        @Override
                        public void set(BasicShader shader, int startingLocation, int[] fieldOffsets, int structSize, ShaderContext shaderContext) {
                            Array<Directional3DLight> dirs = null;
                            Lighting3DEnvironment environment = shaderContext.getPrivatePluginData(Lighting3DPrivateData.class).getEnvironment(data.getString("id", ""));
                            if (environment != null) {
                                dirs = environment.getDirectionalLights();
                            }

                            for (int i = 0; i < numDirectionalLights; i++) {
                                int location = startingLocation + i * structSize;
                                if (dirs != null && i < dirs.size) {
                                    Directional3DLight directionalLight = dirs.get(i);
                                    LightColor color = directionalLight.getColor();
                                    float intensity = directionalLight.getIntensity();

                                    shader.setUniform(location, color.getRed() * intensity, color.getGreen() * intensity,
                                            color.getBlue() * intensity);
                                    shader.setUniform(location + fieldOffsets[1], directionalLight.getDirectionX(),
                                            directionalLight.getDirectionY(), directionalLight.getDirectionZ());
                                } else {
                                    shader.setUniform(location, 0f, 0f, 0f);
                                    shader.setUniform(location + fieldOffsets[1], 0f, 0f, 0f);
                                }
                            }
                        }
                    });
            if (!fragmentShaderBuilder.containsFunction("getDirectionalBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getDirectionalBlinnPhongLightContribution",
                        GLSLFragmentReader.getFragment("blinn-phong/directionalLightContribution",
                                LibGDXCollections.singletonMap("NUM_DIRECTIONAL_LIGHTS", String.valueOf(numDirectionalLights))));
            }
        } else {
            if (!fragmentShaderBuilder.containsFunction("getDirectionalBlinnPhongLightContribution")) {
                fragmentShaderBuilder.addFunction("getDirectionalBlinnPhongLightContribution",
                        "Lighting getDirectionalLightContribution(vec4 pos, vec3 normal, float shininess, Lighting lighting) {\n" +
                                "  return lighting;\n" +
                                "}\n");
            }
        }
    }
}
