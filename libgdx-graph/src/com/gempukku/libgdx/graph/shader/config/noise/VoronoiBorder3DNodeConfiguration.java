package com.gempukku.libgdx.graph.shader.config.noise;

import com.gempukku.libgdx.graph.NodeConfigurationImpl;
import com.gempukku.libgdx.graph.pipeline.loader.node.GraphNodeInputImpl;
import com.gempukku.libgdx.graph.pipeline.loader.node.GraphNodeOutputImpl;
import com.gempukku.libgdx.graph.shader.ShaderFieldType;

public class VoronoiBorder3DNodeConfiguration extends NodeConfigurationImpl<ShaderFieldType> {
    public VoronoiBorder3DNodeConfiguration() {
        super("VoronoiBorder3D", "Voronoi Border 3D", "Noise");
        addNodeInput(
                new GraphNodeInputImpl<ShaderFieldType>("point", "Point", true, ShaderFieldType.Vector3));
        addNodeInput(
                new GraphNodeInputImpl<ShaderFieldType>("scale", "Scale", false, ShaderFieldType.Float));
        addNodeInput(
                new GraphNodeInputImpl<ShaderFieldType>("progress", "Progress", false, ShaderFieldType.Float));
        addNodeOutput(
                new GraphNodeOutputImpl<ShaderFieldType>("output", "Result", ShaderFieldType.Float));
    }
}