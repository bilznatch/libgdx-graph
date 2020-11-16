package com.gempukku.libgdx.graph.test;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.gempukku.libgdx.graph.GraphLoader;
import com.gempukku.libgdx.graph.pipeline.PipelineLoaderCallback;
import com.gempukku.libgdx.graph.pipeline.PipelineRenderer;
import com.gempukku.libgdx.graph.pipeline.RenderOutputs;
import com.gempukku.libgdx.graph.shader.environment.GraphShaderEnvironment;
import com.gempukku.libgdx.graph.shader.models.GraphShaderModels;
import com.gempukku.libgdx.graph.shader.models.Transforms;

import java.io.IOException;
import java.io.InputStream;

public class TestScene implements LibgdxGraphTestScene {
    private Array<Disposable> disposables = new Array<>();
    private PipelineRenderer pipelineRenderer;

    private Camera camera;
    private Stage stage;
    private GraphShaderEnvironment lights;
    private Model starfield;
    private Model blackHole;

    private float cameraPositionAngle;
    private float cameraAngle;
    private String blackHoleInstance;
    private Model star;
    private Model starCorona;
    private Vector3 blackHolePosition = new Vector3(0, 0, 0);
    private Vector3 starPosition = new Vector3(-10, 0, -10);
    private String starInstance;
    private String starCoronaInstance;

    @Override
    public void initializeScene() {
        WhitePixel.initialize();

        lights = createLights();
        stage = createStage();
        disposables.add(stage);

        camera = createCamera();

        pipelineRenderer = loadPipelineRenderer();
        createModels(pipelineRenderer.getGraphShaderModels());

        Gdx.input.setInputProcessor(stage);

        updateCamera();
    }

    private GraphShaderEnvironment createLights() {
        float ambientBrightness = 0.3f;
        float directionalBrightness = 0.8f;
        GraphShaderEnvironment lights = new GraphShaderEnvironment();
        lights.setAmbientColor(new Color(ambientBrightness, ambientBrightness, ambientBrightness, 1f));
        DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setColor(directionalBrightness, directionalBrightness, directionalBrightness, 1f);
        directionalLight.setDirection(-0.3f, -0.4f, -1);
        lights.addDirectionalLight(directionalLight);
        return lights;
    }

    private Camera createCamera() {
        PerspectiveCamera camera = new PerspectiveCamera();
        camera.near = 0.5f;
        camera.far = 100f;

        return camera;
    }

    private void createModels(GraphShaderModels models) {
        ModelBuilder modelBuilder = new ModelBuilder();

        starfield = modelBuilder.createSphere(100, 100, 100, 50, 50,
                new Material(), VertexAttributes.Usage.Position);
        disposables.add(starfield);

        float blackHoleSize = 10f;
        blackHole = modelBuilder.createSphere(blackHoleSize, blackHoleSize, blackHoleSize, 50, 50,
                new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(blackHole);

        float starSize = 5f;
        star = modelBuilder.createSphere(starSize, starSize, starSize, 50, 50,
                new Material(), VertexAttributes.Usage.Position);
        disposables.add(star);

        float coronaMultiplier = 1.4f;
        starCorona = modelBuilder.createSphere(starSize * coronaMultiplier, starSize * coronaMultiplier, starSize * coronaMultiplier, 50, 50,
                new Material(), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        disposables.add(starCorona);

        registerModels(models);
    }

    private void registerModels(GraphShaderModels models) {
        String starfieldId = models.registerModel(starfield);
        models.addModelDefaultTag(starfieldId, "starfield");
        models.createModelInstance(starfieldId);

        String blackHoleId = models.registerModel(blackHole);
        models.addModelDefaultTag(blackHoleId, "black-hole");
        blackHoleInstance = models.createModelInstance(blackHoleId);

        String starId = models.registerModel(star);
        starInstance = models.createModelInstance(starId);
        models.updateTransform(starInstance, Transforms.create().idt().translate(starPosition.x, starPosition.y, starPosition.z));

        String starCoronaId = models.registerModel(starCorona);
        starCoronaInstance = models.createModelInstance(starCoronaId);
        models.updateTransform(starCoronaInstance, Transforms.create().idt().translate(starPosition.x, starPosition.y, starPosition.z));
    }

    private Stage createStage() {
        Skin skin = new Skin(Gdx.files.classpath("skin/default/uiskin.json"));
        disposables.add(skin);

        Stage stage = new Stage(new ScreenViewport());

        Table tbl = new Table(skin);

        tbl.setFillParent(true);
        tbl.align(Align.topLeft);

        final Slider positionAngle = new Slider(0, MathUtils.PI2, 0.001f, false, skin);
        positionAngle.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        cameraPositionAngle = positionAngle.getValue();
                        updateCamera();
                    }
                });

        final Slider angle = new Slider(0, MathUtils.PI2, 0.001f, false, skin);
        angle.addListener(
                new ChangeListener() {
                    @Override
                    public void changed(ChangeEvent event, Actor actor) {
                        cameraAngle = angle.getValue();
                        updateCamera();
                    }
                });

        tbl.add("Camera orbit");
        tbl.add(positionAngle).width(500);
        tbl.row();

        tbl.add("Camera rotation");
        tbl.add(angle).width(500);
        tbl.row();

        stage.addActor(tbl);
        return stage;
    }

    private void updateCamera() {
        float cameraDistance = 30f;

        camera.position.set(cameraDistance * MathUtils.cos(cameraPositionAngle), 0, cameraDistance * MathUtils.sin(cameraPositionAngle));
        camera.up.set(0f, 1f, 0f);
        camera.lookAt(0f, 0f, 0f);
        camera.rotate(MathUtils.radDeg * cameraAngle, 0, 1, 0);
        //camera.direction.set(MathUtils.cos(cameraAngle), 0, MathUtils.sin(cameraAngle));
        camera.update();

        GraphShaderModels models = pipelineRenderer.getGraphShaderModels();
        Vector3 blackHoleScreenPosition = camera.project(new Vector3(0, 0, 0));
        Vector2 screenPos = new Vector2(blackHoleScreenPosition.x / camera.viewportWidth, blackHoleScreenPosition.y / camera.viewportHeight);
        models.setProperty(blackHoleInstance, "Center Screen Position", screenPos);

        float distanceToBlackHole = blackHolePosition.dst2(camera.position);
        float distanceToStar = starPosition.dst2(camera.position);
        if (distanceToBlackHole < distanceToStar) {
            models.addTag(starInstance, "star-surface-behind");
            models.addTag(starCoronaInstance, "star-corona-behind");
            models.removeTag(starInstance, "star-surface-in-front");
            models.removeTag(starCoronaInstance, "star-corona-in-front");
        } else {
            models.removeTag(starInstance, "star-surface-behind");
            models.removeTag(starCoronaInstance, "star-corona-behind");
            models.addTag(starInstance, "star-surface-in-front");
            models.addTag(starCoronaInstance, "star-corona-in-front");
        }
    }

    @Override
    public void resizeScene(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void renderScene() {
        float delta = Gdx.graphics.getDeltaTime();
//        cameraPositionAngle += delta * 0.1f;
//        updateCamera();

        stage.act(delta);

        pipelineRenderer.render(delta, RenderOutputs.drawToScreen);
    }

    @Override
    public void disposeScene() {
        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
        pipelineRenderer.dispose();
        WhitePixel.dispose();
    }

    private PipelineRenderer loadPipelineRenderer() {
        try {
            InputStream stream = Gdx.files.local("test.json").read();
            try {
                PipelineRenderer pipelineRenderer = GraphLoader.loadGraph(stream, new PipelineLoaderCallback());
                setupPipeline(pipelineRenderer);
                return pipelineRenderer;
            } finally {
                stream.close();
            }
        } catch (IOException exp) {
            throw new RuntimeException("Unable to load pipeline", exp);
        }
    }

    private void setupPipeline(PipelineRenderer pipelineRenderer) {
        pipelineRenderer.setPipelineProperty("Camera", camera);
        pipelineRenderer.setPipelineProperty("Lights", lights);
        pipelineRenderer.setPipelineProperty("Stage", stage);
    }
}