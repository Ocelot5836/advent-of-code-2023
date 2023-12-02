package io.github.ocelot.aoc23;

import foundry.veil.editor.SingleWindowEditor;
import imgui.ImGui;
import imgui.type.ImBoolean;
import io.github.ocelot.aoc23.day.CodeDay;
import io.github.ocelot.aoc23.day.CodeDay1;
import org.jetbrains.annotations.Nullable;

public class AdventOfCodeEditor extends SingleWindowEditor {

    private static final CodeDay[] DAYS = new CodeDay[]{
            new CodeDay1()
    };

    private final String[] results;
    private final ImBoolean advanced;
    private int selectedIndex;

    public AdventOfCodeEditor() {
        this.selectedIndex = 0;
        this.advanced = new ImBoolean();
        this.results = new String[DAYS.length];
    }

    @Override
    protected void renderComponents() {
        CodeDay selectedDay = this.getDay();

        ImGui.beginDisabled(selectedDay == null || !selectedDay.canRun());
        if (ImGui.button("Run")) {
            if (selectedDay != null) {
                try {
                    this.results[this.selectedIndex] = selectedDay.run(this.advanced.get());
                } catch (Exception e) {
                    AdventOfCode23.LOGGER.error("Failed to run {}", selectedDay.getName(), e);
                }
            }
        }
        ImGui.endDisabled();

        ImGui.sameLine();
        ImGui.checkbox("Advanced", this.advanced);

        String result = this.getResult();
        ImGui.separator();
        ImGui.text("Results:");
        ImGui.textColored(result != null ? -1 : 0xFF0000FF, result != null ? result : "N/A");

        if (ImGui.beginTabBar("##days")) {
            for (int i = 0; i < DAYS.length; i++) {
                CodeDay day = DAYS[i];
                if (ImGui.tabItemButton(day.getName()) && selectedDay != day) {
                    this.free();
                    this.selectedIndex = i;
                }
            }
            ImGui.endTabBar();
        }

        if (selectedDay != null) {
            selectedDay.addUiElements();
        }
    }

    private @Nullable String getResult() {
        return this.selectedIndex >= 0 && this.selectedIndex < DAYS.length ? this.results[this.selectedIndex] : null;
    }

    private @Nullable CodeDay getDay() {
        return this.selectedIndex >= 0 && this.selectedIndex < DAYS.length ? DAYS[this.selectedIndex] : null;
    }

    @Override
    public void onHide() {
        super.onHide();
        this.free();
    }

    @Override
    public void free() {
        CodeDay selectedDay = this.getDay();
        if (selectedDay != null) {
            selectedDay.free();
        }
        this.selectedIndex = 0;
    }

    @Override
    public String getDisplayName() {
        return "Advent of Code 2023";
    }
}
