package com.gempukku.libgdx.graph.shader.builder;


import com.gempukku.libgdx.graph.shader.UniformRegistry;

public class FragmentShaderBuilder extends CommonShaderBuilder {
    public FragmentShaderBuilder(UniformRegistry uniformRegistry) {
        super(uniformRegistry);
    }

    public String buildProgram(String mainBody) {
        StringBuilder result = new StringBuilder();

        appendStructures(result);
        appendUniformVariables(result);
        appendVaryingVariables(result);
        appendVariables(result);

        appendFunctions(result);

        result.append("void main() {\n");

        result.append(mainBody);

        result.append("}\n");
        return result.toString();
    }
}
