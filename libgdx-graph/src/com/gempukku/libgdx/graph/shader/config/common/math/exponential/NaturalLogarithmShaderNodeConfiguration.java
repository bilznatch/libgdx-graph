package com.gempukku.libgdx.graph.shader.config.common.math.exponential;

import com.gempukku.libgdx.graph.config.SameTypeOutputTypeFunction;
import com.gempukku.libgdx.graph.data.NodeConfigurationImpl;
import com.gempukku.libgdx.graph.pipeline.producer.node.GraphNodeInputImpl;
import com.gempukku.libgdx.graph.pipeline.producer.node.GraphNodeOutputImpl;
import com.gempukku.libgdx.graph.shader.ShaderFieldType;

public class NaturalLogarithmShaderNodeConfiguration extends NodeConfigurationImpl<ShaderFieldType> {
    public NaturalLogarithmShaderNodeConfiguration() {
        super("Log", "Log e", "Math/Exponential");
        addNodeInput(
                new GraphNodeInputImpl<ShaderFieldType>("input", "Input", true, ShaderFieldType.Vector4, ShaderFieldType.Vector3, ShaderFieldType.Vector2, ShaderFieldType.Float));
        addNodeOutput(
                new GraphNodeOutputImpl<ShaderFieldType>("output", "Result",
                        new SameTypeOutputTypeFunction<ShaderFieldType>("input"),
                        ShaderFieldType.Vector4, ShaderFieldType.Vector3, ShaderFieldType.Vector2, ShaderFieldType.Float));
    }
}
