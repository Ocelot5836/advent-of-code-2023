package io.github.ocelot.aoc23;

import com.mojang.logging.LogUtils;
import foundry.veil.fabric.event.VeilRendererEvent;
import foundry.veil.opencl.CLEnvironment;
import foundry.veil.opencl.VeilOpenCL;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class AdventOfCode23 implements ClientModInitializer {

    public static final Logger LOGGER = LogUtils.getLogger();
    public static final CLEnvironment ENVIRONMENT = VeilOpenCL.get().getEnvironment();

    @Override
    public void onInitializeClient() {
        VeilRendererEvent.EVENT.register(renderer -> renderer.getEditorManager().add(new AdventOfCodeEditor()));
    }
}