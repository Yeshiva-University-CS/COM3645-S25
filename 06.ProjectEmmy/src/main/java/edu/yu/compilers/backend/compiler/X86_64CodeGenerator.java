package edu.yu.compilers.backend.compiler;

import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.Operand.OperandType;
import edu.yu.compilers.intermediate.ir.Operand.Constant;
import edu.yu.compilers.intermediate.ir.Operand.Function;
import edu.yu.compilers.intermediate.ir.Operand.Variable;
import edu.yu.compilers.intermediate.ir.Operand.Temporary;
import edu.yu.compilers.intermediate.ir.Operand.Label;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;
import edu.yu.compilers.intermediate.ir.TupleIR.VariableInfo;

import java.util.*;

/**
 * X86_64 code generator for the Emmy compiler.
 * Generates x86_64 assembly code from the intermediate representation.
 */
public class X86_64CodeGenerator extends CodeGenerator {

    // Indentation for assembly code
    private String indent = "\t";

    // Counters for unique labels
    private int stringCounter = 0;

    // Maps to track variables and temporaries
    private final Map<String, Integer> variableOffsets = new HashMap<>();
    private final Map<Integer, Integer> tempOffsets = new HashMap<>();

    // Current function being processed
    private String currentFunction = "";

    // Map to track stack sizes for each function
    private final Map<String, Integer> functionStackSizes = new HashMap<>();

    // Constants, global variables, and functions
    private final Map<String, String> stringConstants = new HashMap<>();
    private final Map<Double, String> floatConstants = new HashMap<>();
    private final Set<String> globalVariables = new HashSet<>();
    private final Set<String> functionNames = new HashSet<>();
    private int floatCounter = 0;

    /**
     * Constructor for the X86_64CodeGenerator.
     *
     * @param ir the intermediate representation
     */
    public X86_64CodeGenerator(TupleIR ir) {
        super(ir);
    }

    // Removed unused indent/dedent methods

    /**
     * Emit a line of assembly code.
     * Used for section directives and labels (left-aligned).
     *
     * @param code the code to emit
     */
    private void emit(String code) {
        output.append(code).append("\n");
    }

    /**
     * Emit a line of assembly code with indentation.
     * Used for instructions (indented with a tab).
     *
     * @param code the code to emit
     */
    private void emitIndented(String code) {
        output.append(indent).append(code).append("\n");
    }

    /**
     * Generate a unique label for string constants.
     *
     * @return a unique string label
     */
    private String generateStringLabel() {
        return ".LCS" + (stringCounter++);
    }

    // Removed unused generateLabel method

    /**
     * Add a string constant to the data section.
     *
     * @param value the string value
     * @return the label for the string
     */
    private String addStringConstant(String value) {
        if (stringConstants.containsKey(value)) {
            return stringConstants.get(value);
        }

        String label = generateStringLabel();
        stringConstants.put(value, label);
        return label;
    }

    /**
     * Get the format specifier for printf based on operand type.
     *
     * @param operand the operand
     * @return the format specifier
     */
    private String getFormatSpecifier(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            Object value = constant.getValue();

            if (value instanceof Integer || value instanceof Long) {
                return "%ld";
            } else if (value instanceof Float || value instanceof Double) {
                return "%f";
            } else if (value instanceof Boolean) {
                return "%d";
            } else if (value instanceof String) {
                return "%s";
            }
        } else if (operand instanceof Variable || operand instanceof Temporary) {
            OperandType type = (operand instanceof Variable)
                    ? ((Variable) operand).getType()
                    : ((Temporary) operand).getType();

            switch (type) {
                case INTEGER:
                    return "%ld";
                case FLOAT:
                    return "%f";
                case BOOLEAN:
                    return "%d";
                case STRING:
                    return "%s";
                default:
                    return "%ld";
            }
        }

        return "%ld"; // Default to long integer
    }

    /**
     * Check if an operand is a string constant.
     *
     * @param operand the operand to check
     * @return true if the operand is a string constant
     */
    private boolean isStringConstant(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            return constant.getValue() instanceof String;
        }
        return false;
    }

    /**
     * Get the assembly representation of an operand.
     *
     * @param operand the operand
     * @return the assembly representation
     */
    private String getOperandReference(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            Object value = constant.getValue();

            if (value instanceof String) {
                String strValue = (String) value;
                String label = addStringConstant(strValue);
                return label;
            } else if (value instanceof Boolean) {
                return ((Boolean) value) ? "$1" : "$0";
            } else if (value instanceof Float || value instanceof Double) {
                double doubleValue = value instanceof Float ? ((Float) value).doubleValue() : (Double) value;
                String label = addFloatConstant(doubleValue);
                return label;
            } else {
                return "$" + value;
            }
        } else if (operand instanceof Variable) {
            Variable variable = (Variable) operand;
            String varName = variable.getName();

            if (variableOffsets.containsKey(varName)) {
                int offset = variableOffsets.get(varName);
                return offset + "(%rbp)";
            } else {
                // If we're in a function context, allocate space for the local variable
                if (!currentFunction.equals("") && !currentFunction.equals("main")) {
                    // Allocate space for the local variable - use the next available slot
                    // We already allocated space for parameters and some locals in
                    // emitFunctionStart
                    // So we need to find the next available slot
                    int nextSlot = 1;
                    for (int offset : variableOffsets.values()) {
                        if (offset < 0) { // Only consider negative offsets (stack variables)
                            nextSlot = Math.max(nextSlot, Math.abs(offset) / 8 + 1);
                        }
                    }
                    int offset = -8 * nextSlot;
                    variableOffsets.put(varName, offset);
                    return offset + "(%rbp)";
                } else {
                    // Global variable - add to the set of global variables
                    globalVariables.add(varName);
                    // Use RIP-relative addressing for global variables
                    return varName + "(%rip)";
                }
            }
        } else if (operand instanceof Temporary) {
            Temporary temp = (Temporary) operand;
            int tempNum = temp.getNumber();

            if (tempOffsets.containsKey(tempNum)) {
                int offset = tempOffsets.get(tempNum);
                return offset + "(%rbp)";
            } else {
                // Allocate space for the temporary - use the next available slot
                // We need to find the next available slot considering both variables and
                // temporaries
                int nextSlot = 1;

                // Check variable offsets
                for (int offset : variableOffsets.values()) {
                    if (offset < 0) { // Only consider negative offsets (stack variables)
                        nextSlot = Math.max(nextSlot, Math.abs(offset) / 8 + 1);
                    }
                }

                // Check temporary offsets
                for (int offset : tempOffsets.values()) {
                    if (offset < 0) { // Only consider negative offsets (stack variables)
                        nextSlot = Math.max(nextSlot, Math.abs(offset) / 8 + 1);
                    }
                }

                int offset = -8 * nextSlot;
                tempOffsets.put(tempNum, offset);

                // Calculate the stack size needed for this temporary
                int newStackSize = 8 * nextSlot;

                // Determine which function we're currently in
                String funcKey = currentFunction.equals("") ? "main" : currentFunction;
                if (funcKey.endsWith("_func")) {
                    funcKey = funcKey.substring(0, funcKey.length() - 5);
                }

                // Update the function's stack size in our map if this temporary requires more
                // space
                int currentFuncSize = functionStackSizes.getOrDefault(funcKey, 0);
                if (newStackSize > currentFuncSize) {
                    functionStackSizes.put(funcKey, newStackSize);
                    // Log for debugging
                    System.out.println("Updated stack size for '" + funcKey + "' to " + newStackSize +
                            " bytes due to temporary #" + tempNum);
                }

                return offset + "(%rbp)";
            }
        } else if (operand instanceof Label) {
            return operand.toString();
        }

        return operand.toString();
    }

    /**
     * Generate a unique label for float constants.
     *
     * @return a unique float label
     */
    private String generateFloatLabel() {
        return ".LCF" + (floatCounter++);
    }

    /**
     * Add a float constant to the data section.
     *
     * @param value the float value
     * @return the label for the float
     */
    private String addFloatConstant(double value) {
        if (floatConstants.containsKey(value)) {
            return floatConstants.get(value);
        }

        String label = generateFloatLabel();
        floatConstants.put(value, label);
        return label;
    }

    /**
     * Convert a double to its IEEE 754 representation as two 32-bit integers.
     *
     * @param value the double value
     * @return array of two longs representing the double in memory
     */
    private long[] doubleToIEEE754(double value) {
        long bits = Double.doubleToRawLongBits(value);
        return new long[] {
                bits & 0xFFFFFFFFL, // Lower 32 bits
                (bits >> 32) & 0xFFFFFFFFL // Upper 32 bits
        };
    }

    /**
     * Escape special characters in a string for assembly.
     *
     * @param str the string to escape
     * @return the escaped string
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
    }

    /**
     * Emit the program prologue.
     */
    private void emitPrologue() {
        // The .text section and .globl directives will be handled in emitProgramEnd
        // to ensure correct section ordering
    }

    @Override
    public void emitProgramStart() {
        emitPrologue();
    }

    @Override
    public void emitProgramEnd() {
        // Get the final stack size for the main function from our map
        int finalMainStackSize = functionStackSizes.getOrDefault("main", 16);

        // Make sure stack size is aligned to 16 bytes (ABI requirement)
        int alignedStackSize = (finalMainStackSize + 15) & ~15;

        // Replace the stack size placeholder in the output
        String placeholder = "subq\t$##STACKSIZE##, %rsp";
        String replacement = "subq\t$" + alignedStackSize + ", %rsp";
        output = new StringBuilder(output.toString().replace(placeholder, replacement));

        // Add a label for the main function epilogue
        emit(".main_epilogue:");

        // Add a proper return value (0) and exit sequence for the main function
        emitIndented("movl\t$0, %eax"); // Return 0 from main
        emitIndented("movq\t%rbp, %rsp");
        emitIndented("popq\t%rbp");
        emitIndented("ret");
        emit(""); // Empty line for readability

        // Now we need to reorder the sections in the output
        // First, save the current output which contains all the function definitions
        String currentOutput = output.toString();

        // Clear the output and start building it with the correct section order
        output = new StringBuilder();

        // 1. Start with the .text section directive
        emit(".text");

        // 2. Emit function declarations
        for (String funcName : functionNames) {
            emit(".globl " + funcName);
        }
        emit(""); // Empty line for readability

        // 3. Append the current output which already has the definition for main
        output.append(currentOutput);

        // 4. Emit the .rodata section with string constants
        emit(".section .rodata");

        // Emit string constants
        for (Map.Entry<String, String> entry : stringConstants.entrySet()) {
            String value = entry.getKey();
            String label = entry.getValue();

            emit(label + ":");
            emitIndented(".string \"" + escapeString(value) + "\"");
        }

        // Emit float constants
        for (Map.Entry<Double, String> entry : floatConstants.entrySet()) {
            double value = entry.getKey();
            String label = entry.getValue();
            long[] bits = doubleToIEEE754(value);

            emit(""); // Empty line for readability
            emitIndented(".align 8");
            emit(label + ":");
            emitIndented(".long " + bits[0]); // Lower 32 bits
            emitIndented(".long " + bits[1]); // Upper 32 bits
        }

        // 5. Emit the .data section with global variables (if any)
        if (!globalVariables.isEmpty()) {
            emit(".section .data");

            for (String varName : globalVariables) {
                emit(varName + ":");
                emitIndented(".quad 0"); // Initialize to 0
            }
        }

        // 6. Add GNU stack note
        emit(".section .note.GNU-stack,\"\",@progbits");
    }

    @Override
    public void emitFunctionStart(Tuple functionTuple, FunctionInfo info) {
        // Reset function-specific state
        variableOffsets.clear();
        tempOffsets.clear();

        // Get function name
        Label funcLabel = (Label) functionTuple.getOperands().get(0);
        currentFunction = funcLabel.toString();

        // Extract the base function name without any suffixes
        String baseFuncName = currentFunction;
        if (baseFuncName.endsWith("_func")) {
            baseFuncName = baseFuncName.substring(0, baseFuncName.length() - 5);
        }

        // Add function name to the set of functions
        functionNames.add(baseFuncName);

        // Emit function label using the base name
        emit(baseFuncName + ":");

        // Function prologue
        emitIndented("pushq\t%rbp");
        emitIndented("movq\t%rsp, %rbp");

        // Process parameters - store register parameters on the stack
        int paramCount = info.getParameters().size();
        int varCount = info.getVariables().size();

        // Initialize stack size based on parameters and local variables
        // We need at least 24 bytes (3 * 8) for a simple function with 2 params and 1
        // result
        int initialStackSize = Math.max(24, (paramCount + varCount + 1) * 8);

        // Initialize the function stack size in our map
        functionStackSizes.put(baseFuncName, initialStackSize);

        // Use a placeholder for stack size that will be replaced in emitFunctionEnd
        emitIndented("subq\t$##STACKSIZE##, %rsp");

        // Register mapping for parameters (x86_64 calling convention)
        String[] paramRegisters = { "%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9" };

        // Save parameters from registers to stack
        for (VariableInfo var : info.getParameters()) {
            if (var.getParamIndex() < 6) {
                int paramIndex = var.getParamIndex();
                int offset = -8 * (paramIndex + 1); // First param at -8(%rbp), second at -16(%rbp), etc.
                variableOffsets.put(var.getName(), offset);

                // Save parameter from register to stack
                emitIndented("movq\t" + paramRegisters[paramIndex] + ", " + offset + "(%rbp)");
            }
        }
    }

    @Override
    public void emitFunctionEnd(Tuple endFunctionTuple, FunctionInfo info) {
        // Extract the base function name without any suffixes
        String baseFuncName = currentFunction;
        if (baseFuncName.endsWith("_func")) {
            baseFuncName = baseFuncName.substring(0, baseFuncName.length() - 5);
        }

        // Get the final stack size for this function
        int finalStackSize = functionStackSizes.getOrDefault(baseFuncName, 24);

        // Ensure stack is aligned to 16 bytes (ABI requirement)
        int alignedStackSize = (finalStackSize + 15) & ~15;

        // Replace the stack size placeholder in the output
        String placeholder = "subq\t$##STACKSIZE##, %rsp";
        String replacement = "subq\t$" + alignedStackSize + ", %rsp";
        output = new StringBuilder(output.toString().replace(placeholder, replacement));

        // Add a label for the function epilogue that return statements can jump to
        emit("." + baseFuncName + "_epilogue:");

        // Function epilogue
        emitIndented("movq\t%rbp, %rsp");
        emitIndented("popq\t%rbp");
        emitIndented("ret");
        emit(""); // Empty line for readability
    }

    @Override
    protected void emitProgram(Tuple tuple) {
        // The main program is treated as a function named "main"
        emit("main:");
        emitIndented("pushq\t%rbp");
        emitIndented("movq\t%rsp, %rbp");

        // Reset stack tracking variables
        currentFunction = "main";
        tempOffsets.clear();
        variableOffsets.clear();

        // Initialize stack size for main to 16 bytes (minimum aligned size)
        functionStackSizes.put("main", 16);

        // Use a placeholder for stack size that will be replaced in emitProgramEnd
        emitIndented("subq\t$##STACKSIZE##, %rsp");
    }

    @Override
    protected void emitFunction(Tuple tuple) {
        // Handled by emitFunctionStart
    }

    @Override
    protected void emitParam(Tuple tuple) {
        // Parameters are handled in emitFunctionStart
    }

    @Override
    protected void emitEndFunction(Tuple tuple) {
        // Handled by emitFunctionEnd
    }

    @Override
    protected void emitAssign(Tuple tuple) {
        Operand target = tuple.getOperands().get(0);
        Operand source = tuple.getOperands().get(1);

        String sourceRef = getOperandReference(source);
        String targetRef = getOperandReference(target);

        // Load source into register, then store to target
        emitIndented("movq\t" + sourceRef + ", %rax");
        emitIndented("movq\t%rax, " + targetRef);
    }

    @Override
    protected void emitAdd(Tuple tuple) {
        emitBinaryOp(tuple, "addq", "addsd");
    }

    @Override
    protected void emitSub(Tuple tuple) {
        emitBinaryOp(tuple, "subq", "subsd");
    }

    @Override
    protected void emitMul(Tuple tuple) {
        emitBinaryOp(tuple, "imulq", "mulsd");
    }

    @Override
    protected void emitDiv(Tuple tuple) {
        List<Operand> ops = tuple.getOperands();
        Operand result = ops.get(0);
        Operand left = ops.get(1);
        Operand right = ops.get(2);

        // Check if any operand is floating point
        boolean isFloatingPoint = isFloatingPoint(left) || isFloatingPoint(right);

        if (isFloatingPoint) {
            // Floating point division
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            if (isFloatingPointConstant(left)) {
                // For floating point constants, we need to use the RIP-relative addressing
                emitIndented("movq\t" + leftRef + "(%rip), %rax");
                emitIndented("movq\t%rax, %xmm0");
            } else {
                emitIndented("movq\t" + leftRef + ", %xmm0");
            }

            if (isFloatingPointConstant(right)) {
                // For floating point constants, we need to use the RIP-relative addressing
                emitIndented("movq\t" + rightRef + "(%rip), %rax");
                emitIndented("movq\t%rax, %xmm1");
            } else {
                emitIndented("movq\t" + rightRef + ", %xmm1");
            }

            emitIndented("divsd\t%xmm1, %xmm0");
            emitIndented("movq\t%xmm0, " + resultRef);
        } else {
            // Integer division
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            // x86_64 division is special - it uses rdx:rax as dividend
            emitIndented("movq\t" + leftRef + ", %rax");
            emitIndented("cqto"); // Sign-extend rax into rdx
            emitIndented("movq\t" + rightRef + ", %rcx");
            emitIndented("idivq\t%rcx"); // Divide rdx:rax by rcx, result in rax
            emitIndented("movq\t%rax, " + resultRef);
        }
    }

    /**
     * Check if an operand is a floating point value.
     *
     * @param operand the operand to check
     * @return true if the operand is a floating point value
     */
    private boolean isFloatingPoint(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            Object value = constant.getValue();
            return value instanceof Float || value instanceof Double;
        } else if (operand instanceof Variable) {
            Variable variable = (Variable) operand;
            return variable.getType() == OperandType.FLOAT;
        } else if (operand instanceof Temporary) {
            Temporary temp = (Temporary) operand;
            return temp.getType() == OperandType.FLOAT;
        }
        return false;
    }

    /**
     * Check if an operand is a floating point constant.
     *
     * @param operand the operand to check
     * @return true if the operand is a floating point constant
     */
    private boolean isFloatingPointConstant(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            Object value = constant.getValue();
            return value instanceof Float || value instanceof Double;
        }
        return false;
    }

    /**
     * Helper method to emit binary operations.
     *
     * @param tuple            the tuple
     * @param intInstruction   the assembly instruction for integers
     * @param floatInstruction the assembly instruction for floating point
     */
    private void emitBinaryOp(Tuple tuple, String intInstruction, String floatInstruction) {
        List<Operand> ops = tuple.getOperands();
        Operand result = ops.get(0);
        Operand left = ops.get(1);
        Operand right = ops.get(2);

        // Check if any operand is floating point
        boolean isFloatingPoint = isFloatingPoint(left) || isFloatingPoint(right);

        if (isFloatingPoint) {
            // Floating point operation
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            if (isFloatingPointConstant(left)) {
                // For floating point constants, we need to use the RIP-relative addressing
                emitIndented("movq\t" + leftRef + "(%rip), %rax");
                emitIndented("movq\t%rax, %xmm0");
            } else {
                emitIndented("movq\t" + leftRef + ", %xmm0");
            }

            if (isFloatingPointConstant(right)) {
                // For floating point constants, we need to use the RIP-relative addressing
                emitIndented("movq\t" + rightRef + "(%rip), %rax");
                emitIndented("movq\t%rax, %xmm1");
            } else {
                emitIndented("movq\t" + rightRef + ", %xmm1");
            }

            emitIndented(floatInstruction + "\t%xmm1, %xmm0");
            emitIndented("movq\t%xmm0, " + resultRef);
        } else {
            // Integer operation
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            emitIndented("movq\t" + leftRef + ", %rax");
            emitIndented(intInstruction + "\t" + rightRef + ", %rax");
            emitIndented("movq\t%rax, " + resultRef);
        }
    }

    /**
     * Helper method to emit binary operations (integer only).
     *
     * @param tuple       the tuple
     * @param instruction the assembly instruction
     */
    private void emitBinaryOp(Tuple tuple, String instruction) {
        emitBinaryOp(tuple, instruction, instruction);
    }

    @Override
    protected void emitAnd(Tuple tuple) {
        emitBinaryOp(tuple, "andq");
    }

    @Override
    protected void emitOr(Tuple tuple) {
        emitBinaryOp(tuple, "orq");
    }

    @Override
    protected void emitNot(Tuple tuple) {
        Operand result = tuple.getOperands().get(0);
        Operand operand = tuple.getOperands().get(1);

        String operandRef = getOperandReference(operand);
        String resultRef = getOperandReference(result);

        emitIndented("movq\t" + operandRef + ", %rax");
        emitIndented("notq\t%rax");
        emitIndented("andq\t$1, %rax"); // Ensure boolean result (0 or 1)
        emitIndented("movq\t%rax, " + resultRef);
    }

    @Override
    protected void emitEq(Tuple tuple) {
        emitComparison(tuple, "sete");
    }

    @Override
    protected void emitNeq(Tuple tuple) {
        emitComparison(tuple, "setne");
    }

    @Override
    protected void emitGt(Tuple tuple) {
        emitComparison(tuple, "setg");
    }

    @Override
    protected void emitGte(Tuple tuple) {
        emitComparison(tuple, "setge");
    }

    @Override
    protected void emitLt(Tuple tuple) {
        emitComparison(tuple, "setl");
    }

    @Override
    protected void emitLte(Tuple tuple) {
        emitComparison(tuple, "setle");
    }

    /**
     * Helper method to emit comparison operations.
     *
     * @param tuple          the tuple
     * @param setInstruction the set instruction (sete, setne, etc.)
     */
    private void emitComparison(Tuple tuple, String setInstruction) {
        List<Operand> ops = tuple.getOperands();
        Operand result = ops.get(0);
        Operand left = ops.get(1);
        Operand right = ops.get(2);

        String leftRef = getOperandReference(left);
        String rightRef = getOperandReference(right);
        String resultRef = getOperandReference(result);

        emitIndented("movq\t" + leftRef + ", %rax");
        emitIndented("cmpq\t" + rightRef + ", %rax");
        emitIndented(setInstruction + "\t%al");
        emitIndented("movzbq\t%al, %rax"); // Zero-extend byte to quad
        emitIndented("movq\t%rax, " + resultRef);
    }

    @Override
    protected void emitIf(Tuple tuple) {
        Operand condition = tuple.getOperands().get(0);
        Label label = (Label) tuple.getOperands().get(1);

        String conditionRef = getOperandReference(condition);

        emitIndented("movq\t" + conditionRef + ", %rax");
        emitIndented("testq\t%rax, %rax");
        emitIndented("je\t" + label);
    }

    @Override
    protected void emitGoto(Tuple tuple) {
        Label label = (Label) tuple.getOperands().get(0);
        emitIndented("jmp\t" + label);
    }

    @Override
    protected void emitLabel(Tuple tuple) {
        Label label = (Label) tuple.getOperands().get(0);
        emit(label + ":");
    }

    @Override
    protected void emitReturn(Tuple tuple) {
        // Only set the return value if there is one
        if (!tuple.getOperands().isEmpty()) {
            Operand returnValue = tuple.getOperands().get(0);
            String returnRef = getOperandReference(returnValue);

            emitIndented("movq\t" + returnRef + ", %rax");
        }

        // Jump to the function epilogue
        // The actual return sequence (movq %rbp, %rsp; popq %rbp; ret) is handled by
        // emitFunctionEnd
        String baseFuncName = currentFunction;
        if (baseFuncName.endsWith("_func")) {
            baseFuncName = baseFuncName.substring(0, baseFuncName.length() - 5);
        }
        emitIndented("jmp\t." + baseFuncName + "_epilogue");
    }

    @Override
    protected void emitPrint(Tuple tuple) {
        Operand operand = tuple.getOperands().get(0);

        if (isStringConstant(operand)) {
            // Direct string constant
            Constant constant = (Constant) operand;
            String strValue = (String) constant.getValue();

            // Create a format string with the string and a newline
            String formatLabel = addStringConstant("%s\n");
            String stringLabel = addStringConstant(strValue);

            // Print using printf with format specifier
            emitIndented("leaq\t" + formatLabel + "(%rip), %rdi");
            emitIndented("leaq\t" + stringLabel + "(%rip), %rsi");
            emitIndented("xorq\t%rax, %rax"); // Clear AL for varargs printf
            emitIndented("call\t*printf@GOTPCREL(%rip)");
        } else {
            // Value to print with format specifier
            String formatSpecifier = getFormatSpecifier(operand);
            String formatLabel = addStringConstant(formatSpecifier + "\n");
            String operandRef = getOperandReference(operand);

            // Handle floating point values differently
            if (operand instanceof Constant) {
                Constant constant = (Constant) operand;
                Object value = constant.getValue();

                if (value instanceof Float || value instanceof Double) {
                    // For floating point, we need to use XMM registers
                    emitIndented("leaq\t" + formatLabel + "(%rip), %rdi");
                    emitIndented("movq\t" + operandRef + "(%rip), %rax"); // Load from RIP-relative address
                    emitIndented("movq\t%rax, %xmm0");
                    emitIndented("movl\t$1, %eax"); // Set AL to 1 for one XMM register
                    emitIndented("call\t*printf@GOTPCREL(%rip)");
                    return;
                }
            }

            // For non-floating point values
            emitIndented("leaq\t" + formatLabel + "(%rip), %rdi");
            emitIndented("movq\t" + operandRef + ", %rsi");
            emitIndented("xorq\t%rax, %rax"); // Clear AL for varargs printf
            emitIndented("call\t*printf@GOTPCREL(%rip)");
        }
    }

    @Override
    protected void emitCall(Tuple tuple) {
        List<Operand> ops = tuple.getOperands();

        // Handle arguments (x86_64 calling convention)
        // First 6 args go in registers: rdi, rsi, rdx, rcx, r8, r9
        // Additional args go on the stack
        String[] argRegisters = { "%rdi", "%rsi", "%rdx", "%rcx", "%r8", "%r9" };

        // Push any arguments beyond the first 6 onto the stack (in reverse order)
        for (int i = ops.size() - 1; i >= 8; i--) {
            Operand arg = ops.get(i);
            String argRef = getOperandReference(arg);

            emitIndented("movq\t" + argRef + ", %rax");
            emitIndented("pushq\t%rax");
        }

        // Load the first 6 arguments into registers
        for (int i = 2; i < Math.min(ops.size(), 8); i++) {
            Operand arg = ops.get(i);
            String argRef = getOperandReference(arg);

            emitIndented("movq\t" + argRef + ", " + argRegisters[i - 2]);
        }

        // Call the function
        // Get the function name from the function operand
        Function function = (Function) ops.get(1);
        String funcName = function.getName();

        // Add the function to our set of known functions
        // Extract the base function name without any suffixes
        String baseFuncName = funcName;
        if (baseFuncName.endsWith("_func")) {
            baseFuncName = baseFuncName.substring(0, baseFuncName.length() - 5);
        }
        functionNames.add(baseFuncName);

        // Call the function using the base name
        emitIndented("call\t" + baseFuncName);

        // Clean up stack if we pushed arguments
        if (ops.size() > 8) {
            int stackArgs = ops.size() - 8;
            emitIndented("addq\t$" + (stackArgs * 8) + ", %rsp");
        }

        // Store the return value if needed
        if (ops.size() > 0) {
            Operand result = ops.get(0);
            String resultRef = getOperandReference(result);

            emitIndented("movq\t%rax, " + resultRef);
        }
    }

    @Override
    protected void emitTemp(Tuple tuple) {
        // Temporary assignment is handled like a regular assignment
        emitAssign(tuple);
    }

    @Override
    protected void emitUnknown(Tuple tuple) {
        emitIndented("# Unknown operation: " + tuple.getOperator());
    }
}
