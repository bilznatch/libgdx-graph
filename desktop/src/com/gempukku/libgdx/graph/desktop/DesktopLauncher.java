package com.gempukku.libgdx.graph.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.gempukku.libgdx.graph.plugin.ui.design.UIDesignInitializer;
import com.gempukku.libgdx.graph.ui.LibgdxGraphApplication;

public class DesktopLauncher {
	public static void main(String[] arg) {
        UIDesignInitializer uiDesignInitializer = new UIDesignInitializer();
        uiDesignInitializer.initialize();

        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.width = 1440;
        config.height = 810;
        new LwjglApplication(new LibgdxGraphApplication(), config);
    }
}
