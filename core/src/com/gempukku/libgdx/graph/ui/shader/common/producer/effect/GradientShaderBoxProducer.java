package com.gempukku.libgdx.graph.ui.shader.common.producer.effect;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonValue;
import com.gempukku.libgdx.graph.shader.ShaderFieldType;
import com.gempukku.libgdx.graph.shader.config.common.effect.GradientShaderNodeConfiguration;
import com.gempukku.libgdx.graph.ui.graph.GraphBox;
import com.gempukku.libgdx.graph.ui.graph.GraphBoxImpl;
import com.gempukku.libgdx.graph.ui.graph.GraphBoxPartImpl;
import com.gempukku.libgdx.graph.ui.graph.GraphChangedEvent;
import com.gempukku.libgdx.graph.ui.producer.GraphBoxProducerImpl;
import com.gempukku.libgdx.graph.util.SimpleNumberFormatter;
import com.kotcrab.vis.ui.widget.color.ColorPicker;
import com.kotcrab.vis.ui.widget.color.ColorPickerAdapter;
import com.talosvfx.talos.editor.widgets.GradientWidget;
import com.talosvfx.talos.runtime.values.ColorPoint;

public class GradientShaderBoxProducer extends GraphBoxProducerImpl<ShaderFieldType> {
    public GradientShaderBoxProducer() {
        super(new GradientShaderNodeConfiguration());
    }

    @Override
    public GraphBox<ShaderFieldType> createPipelineGraphBox(Skin skin, String id, JsonValue data) {
        GraphBoxImpl<ShaderFieldType> result = createGraphBox(skin, id);
        addConfigurationInputsAndOutputs(skin, result);

        final GradientWidget gradientWidget = new GradientWidget();
        gradientWidget.setSize(300, 80);

        if (data != null) {
            JsonValue points = data.get("points");
            for (String point : points.asStringArray()) {
                String[] split = point.split(",");
                gradientWidget.createPoint(Color.valueOf(split[0]), Float.parseFloat(split[1]));
            }
        } else {
            gradientWidget.createPoint(Color.WHITE, 0);
        }

        gradientWidget.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        gradientWidget.fire(new GraphChangedEvent(false, true));
                    }
                });

        gradientWidget.setListener(
                new GradientWidget.GradientWidgetListener() {
                    @Override
                    public void colorPickerShow(final ColorPoint point, final Runnable onSuccess) {
                        final ColorPicker picker = new ColorPicker();
                        picker.setColor(point.color);
                        picker.setListener(new ColorPickerAdapter() {
                            @Override
                            public void finished(Color newColor) {
                                point.color.set(newColor);
                                picker.dispose();
                                onSuccess.run();
                            }

                            @Override
                            public void canceled(Color oldColor) {
                                picker.dispose();
                            }
                        });

                        gradientWidget.getStage().addActor(picker.fadeIn());
                    }
                });

        result.addGraphBoxPart(
                new GraphBoxPartImpl<ShaderFieldType>(
                        gradientWidget,
                        new GraphBoxPartImpl.Callback() {
                            @Override
                            public void serialize(JsonValue object) {
                                Array<ColorPoint> points = gradientWidget.getPoints();
                                JsonValue pointsValue = new JsonValue(JsonValue.ValueType.array);
                                for (ColorPoint point : points) {
                                    pointsValue.addChild(new JsonValue(point.color.toString() + "," + SimpleNumberFormatter.format(point.pos)));
                                }
                                object.addChild("points", pointsValue);
                            }
                        }
                ));

        return result;
    }
}