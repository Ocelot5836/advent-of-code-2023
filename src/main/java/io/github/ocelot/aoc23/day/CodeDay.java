package io.github.ocelot.aoc23.day;

import org.lwjgl.system.NativeResource;

public interface CodeDay extends NativeResource {

    String run(boolean advanced) throws Exception;

    default boolean canRun() {
        return true;
    }

    void addUiElements();

    String getName();
}
