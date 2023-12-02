package io.github.ocelot.aoc23.day;

import foundry.veil.opencl.CLBuffer;
import foundry.veil.opencl.CLEnvironment;
import foundry.veil.opencl.CLException;
import foundry.veil.opencl.CLKernel;
import imgui.ImGui;
import imgui.type.ImString;
import io.github.ocelot.aoc23.AdventOfCode23;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static foundry.veil.lib.opencl.CL10.CL_MEM_READ_ONLY;
import static foundry.veil.lib.opencl.CL10.CL_MEM_READ_WRITE;

public class CodeDay1 implements CodeDay {

    private final ImString input;
    private static final String CHECK_METHOD = generateCheckMethod("one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine");

    private CLKernel basicKernel;
    private CLKernel advancedKernel;

    public CodeDay1() {
        this.input = new ImString(Short.MAX_VALUE);
    }

    private static String generateCheckMethod(String... tokens) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            code.append("if(maxIndex - index >= ").append(token.length());
            for (int j = 0; j < token.length(); j++) {
                code.append(" && Data[index + ").append(j).append("] == ").append((int) token.charAt(j));
            }
            code.append(") return ").append(i + 1).append(";\n");
        }
        return """
                int getNumber(int index, int maxIndex, global const unsigned int* Data) {
                %s
                return -1;
                }""".formatted(code);
    }

    @Override
    public String run(boolean advanced) throws CLException {
        CLEnvironment environment = AdventOfCode23.ENVIRONMENT;
        if (environment == null) {
            throw new IllegalStateException("No OpenCL Environment");
        }

        if (this.basicKernel == null) {
            ResourceLocation name = new ResourceLocation("aoc23", "day0_basic");
            environment.loadProgram(name, """                    
                    void kernel part1(global unsigned int* Counter, global const int* Size, global const unsigned int* Data) {
                        int id = get_global_id(0);
                        int start = Size[id];
                        int end = Size[id + 1];
                        
                        int firstNum = -1;
                        int lastNum = -1;
                        for(int i = start; i < end; i++) {
                            if(firstNum != -1 && lastNum != -1) {
                                break;
                            }
                            
                            if(firstNum == -1) {
                                int firstChar = Data[i] - 48;
                                if(firstChar >= 0 && firstChar < 10) {
                                    firstNum = firstChar;
                                }
                            }
                            
                            if(lastNum == -1) {
                                int lastChar = Data[end - i + start - 1] - 48;
                                if(lastChar >= 0 && lastChar < 10) {
                                    lastNum = lastChar;
                                }
                            }
                        }
                        
                        if(firstNum == -1 || lastNum == -1) {
                            return;
                        }
                        
                        atomic_add(Counter, firstNum * 10 + lastNum);
                    }
                    """);
            this.basicKernel = environment.createKernel(name, "part1");
        }
        if (this.advancedKernel == null) {
            ResourceLocation name = new ResourceLocation("aoc23", "day0_advanced");
            environment.loadProgram(name, CHECK_METHOD + """                    
                    void kernel part1(global unsigned int* Counter, global const int* Size, global const unsigned int* Data) {
                        int id = get_global_id(0);
                        int start = Size[id];
                        int end = Size[id + 1];
                        
                        int firstNum = -1;
                        int lastNum = -1;
                        for(int i = start; i < end; i++) {
                            if(firstNum != -1 && lastNum != -1) {
                                break;
                            }
                            
                            if(firstNum == -1) {
                                int firstChar = Data[i] - 48;
                                if(firstChar >= 0 && firstChar < 10) {
                                    firstNum = firstChar;
                                } else {
                                    int wordNum = getNumber(i, end, Data);
                                    if (wordNum != -1) {
                                        firstNum = wordNum;
                                    }
                                }
                            }
                            
                            if(lastNum == -1) {
                                int lastChar = Data[end - i + start - 1] - 48;
                                if(lastChar >= 0 && lastChar < 10) {
                                    lastNum = lastChar;
                                } else {
                                    int wordNum = getNumber(end - i + start - 1, end, Data);
                                    if (wordNum != -1) {
                                        lastNum = wordNum;
                                    }
                                }
                            }
                        }
                        
                        if(firstNum == -1 || lastNum == -1) {
                            return;
                        }
                        
                        atomic_add(Counter, firstNum * 10 + lastNum);
                    }
                    """);
            this.advancedKernel = environment.createKernel(name, "part1");
        }

        CLKernel kernel = advanced ? this.advancedKernel : this.basicKernel;

        String[] parts = this.input.get().split("\n");
        String cleanInput = String.join("", parts);
        IntBuffer data = MemoryUtil.memAllocInt(cleanInput.length());

        try (MemoryStack stack = MemoryStack.stackPush();
             CLBuffer counterBuffer = kernel.createBuffer(CL_MEM_READ_WRITE, Integer.BYTES);
             CLBuffer sizeBuffer = kernel.createBuffer(CL_MEM_READ_ONLY, (long) Integer.BYTES * (parts.length + 1));
             CLBuffer dataBuffer = kernel.createBuffer(CL_MEM_READ_ONLY, (long) Integer.BYTES * cleanInput.length())) {

            IntBuffer counter = stack.callocInt(1);
            counterBuffer.writeAsync(0, counter, null);

            IntBuffer size = stack.mallocInt(parts.length + 1);
            int pointer = 0;
            for (String part : parts) {
                size.put(pointer);
                data.put(part.chars().toArray()); // This guarantees all chars 1 byte
                pointer += part.length();
            }
            size.put(pointer);

            size.rewind();
            data.rewind();

            sizeBuffer.writeAsync(0, size, null);
            dataBuffer.writeAsync(0, data, null);

            kernel.setPointers(0, counterBuffer);
            kernel.setPointers(1, sizeBuffer);
            kernel.setPointers(2, dataBuffer);
            kernel.execute(parts.length, 1);

            counterBuffer.read(0, counter);

            environment.finish();

            return "Calibration Number: " + counter.get(0);
        } finally {
            MemoryUtil.memFree(data);
        }
    }

    @Override
    public boolean canRun() {
        return AdventOfCode23.ENVIRONMENT != null && !this.input.isEmpty();
    }

    @Override
    public void addUiElements() {
        ImGui.inputTextMultiline("Input", this.input);
    }

    @Override
    public String getName() {
        return "Day 1";
    }

    @Override
    public void free() {
        if (this.basicKernel != null) {
            this.basicKernel.free();
            this.basicKernel = null;
        }
        if (this.advancedKernel != null) {
            this.advancedKernel.free();
            this.advancedKernel = null;
        }
    }
}
