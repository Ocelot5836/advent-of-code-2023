package io.github.ocelot.aoc23;

import foundry.veil.opencl.CLEnvironment;
import foundry.veil.opencl.VeilOpenCL;
import foundry.veil.render.pipeline.VeilRenderSystem;
import net.fabricmc.api.ClientModInitializer;

public class AdventOfCode23 implements ClientModInitializer {

    public static final CLEnvironment ENVIRONMENT = VeilOpenCL.get().getEnvironment();

    @Override
    public void onInitializeClient() {
    }
}