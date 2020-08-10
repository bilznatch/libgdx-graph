package com.gempukku.libgdx.graph.ui.graph;

import com.gempukku.libgdx.graph.renderer.loader.node.PipelineNodeOutput;
import com.google.common.base.Supplier;

public class GraphBoxOutputConnectorImpl implements GraphBoxOutputConnector {
    private Side side;
    private Supplier<Float> offsetSupplier;
    private PipelineNodeOutput pipelineNodeOutput;

    public GraphBoxOutputConnectorImpl(Side side, Supplier<Float> offsetSupplier, PipelineNodeOutput pipelineNodeOutput) {
        this.side = side;
        this.offsetSupplier = offsetSupplier;
        this.pipelineNodeOutput = pipelineNodeOutput;
    }

    @Override
    public Side getSide() {
        return side;
    }

    @Override
    public float getOffset() {
        return offsetSupplier.get();
    }

    @Override
    public PipelineNodeOutput getPipelineNodeOutput() {
        return pipelineNodeOutput;
    }
}
