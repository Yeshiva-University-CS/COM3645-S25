package edu.yu.compilers.backend.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.yu.compilers.backend.compiler.X86_64Operand.Immediate;
import edu.yu.compilers.backend.compiler.X86_64Operand.Memory;
import edu.yu.compilers.backend.compiler.X86_64Operand.Register;
import edu.yu.compilers.intermediate.ir.Operand;
import edu.yu.compilers.intermediate.ir.Operand.Constant;
import edu.yu.compilers.intermediate.ir.Operand.Function;
import edu.yu.compilers.intermediate.ir.Operand.Label;
import edu.yu.compilers.intermediate.ir.Operand.OperandType;
import edu.yu.compilers.intermediate.ir.Operand.Temporary;
import edu.yu.compilers.intermediate.ir.Operand.Variable;
import edu.yu.compilers.intermediate.ir.Tuple;
import edu.yu.compilers.intermediate.ir.TupleIR;
import edu.yu.compilers.intermediate.ir.TupleIR.FunctionInfo;
import edu.yu.compilers.intermediate.ir.TupleIR.VariableInfo;

/**
 * X86_64 code generator for the Emmy compiler.
 * Generates x86_64 assembly code from the intermediate representation.
 */
public class X86_64CodeGenerator extends CodeGenerator {

    // Output buffer for assembly code
    private StringBuilder output; // assigned to the current target builder
    private final StringBuilder masterOutput = new StringBuilder();

    // Indentation for assembly code
    private final String indent = "\t";

    private final String stackSizePlaceHolder = "N";

    // Counters for unique labels
    private int stringCounter = 0;

    // Maps to track variables and temporaries
    private final Map<String, Integer> variableOffsets = new HashMap<>();
    private final Map<Integer, Integer> tempOffsets = new HashMap<>();

    // Current function being processed
    private String currentFunction = "";

    // Current function's stack size
    private int currentFunctionStackSize = 0;

    // Constants, global variables, and functions
    private final Map<String, String> stringConstants = new HashMap<>();
    private final Map<Double, String> floatConstants = new HashMap<>();
    private final Map<String, GlobalVarInfo> globalVariables = new HashMap<>();
    private int floatCounter = 0;

    /**
     * Class to store information about global variables.
     */
    private static class GlobalVarInfo {
        private final Operand.OperandType type;
        private final Object value;

        public GlobalVarInfo(Operand.OperandType type, Object value) {
            this.type = type;
            this.value = value;
        }
    }

    /**
     * Constructor for the X86_64CodeGenerator.
     */
    public X86_64CodeGenerator(TupleIR ir) {
        super(ir);
    }

    @Override
    public String getOutput() {
        return masterOutput.toString();
    }

    @Override
    public void emitProgramStart() {
        masterOutput.setLength(0);
        stringCounter = 0;
        floatCounter = 0;
        stringConstants.clear();
        floatConstants.clear();
        currentFunctionStackSize = 0;

        output = masterOutput;
    }

    @Override
    public void emitProgramEnd() {
        output = masterOutput;

        // 1. Emit the .rodata section with string constants
        if (!stringConstants.isEmpty()) {
            emit(".section .rodata");

            for (Map.Entry<String, String> entry : stringConstants.entrySet()) {
                String value = entry.getKey();
                String label = entry.getValue();

                emit(label + ":");
                emitIndented(".string \"" + escapeString(value) + "\"");
            }
            emit("");
        }

        // 2. Emit the .rodata section with float constants
        if (!floatConstants.isEmpty()) {
            emit(".section .rodata");

            for (Map.Entry<Double, String> entry : floatConstants.entrySet()) {
                double value = entry.getKey();
                String label = entry.getValue();
                long[] bits = doubleToIEEE754(value);

                emit(".align 8");
                emit(label + ":");
                emitIndented(".long " + bits[0]); // Lower 32 bits
                emitIndented(".long " + bits[1]); // Upper
            }
            emit("");
        }

        // 6. Emit the .data section with global variables (if any)
        if (!globalVariables.isEmpty()) {
            emit(".section .data");
            emitGlobalVariables();
        }

        // 6. Add GNU stack note
        emit(".section .note.GNU-stack,\"\",@progbits");
    }

    /**
     * Emit global variables with appropriate type-specific initialization and
     * alignment.
     */
    private void emitGlobalVariables() {
        for (Map.Entry<String, GlobalVarInfo> entry : globalVariables.entrySet()) {
            String varName = entry.getKey();
            GlobalVarInfo info = entry.getValue();
            Operand.OperandType type = info.type;
            Object value = info.value;

            switch (type) {
                case INTEGER:
                    emit(".align 8"); // 8-byte alignment for 64-bit integers
                    emit(varName + ":");
                    if (value != null) {
                        // Use the numeric value directly
                        emitIndented(".quad " + value); // 64-bit integer with actual value
                    } else {
                        emitIndented(".quad 0"); // 64-bit integer initialized to 0
                    }
                    break;
                case FLOAT:
                    emit(".align 8"); // 8-byte alignment for 64-bit doubles
                    emit(varName + ":");
                    if (value != null) {
                        // For floating point values, we need to handle special cases
                        if (value instanceof Double) {
                            double doubleValue = (Double) value;
                            if (Double.isNaN(doubleValue) || Double.isInfinite(doubleValue)) {
                                // Special values need special handling
                                long bits = Double.doubleToRawLongBits(doubleValue);
                                emitIndented(".quad " + bits); // Use the bit pattern directly
                            } else {
                                emitIndented(".double " + doubleValue); // Normal double value
                            }
                        } else if (value instanceof Float) {
                            float floatValue = (Float) value;
                            emitIndented(".double " + floatValue); // Convert float to double
                        } else {
                            emitIndented(".double 0.0"); // Fallback
                        }
                    } else {
                        emitIndented(".double 0.0"); // 64-bit double initialized to 0.0
                    }
                    break;
                case BOOLEAN:
                    emit(".align 1"); // 1-byte alignment for booleans
                    emit(varName + ":");
                    if (value instanceof Boolean) {
                        boolean boolValue = (Boolean) value;
                        emitIndented(".byte " + (boolValue ? "1" : "0")); // 8-bit boolean with actual value
                    } else {
                        emitIndented(".byte 0"); // 8-bit boolean initialized to false (0)
                    }
                    break;
                case STRING:
                    emit(".align 8"); // 8-byte alignment for pointers
                    emit(varName + ":");
                    if (value instanceof String strValue) {
                        String stringLabel = addStringConstant(strValue);
                        // We need to use the address of the string constant
                        emitIndented(".quad " + stringLabel + "(%rip)"); // 64-bit pointer to the string constant
                    } else {
                        emitIndented(".quad 0"); // 64-bit pointer initialized to null (0)
                    }
                    break;
                default:
                    emit(".align 8"); // Default to 8-byte alignment
                    emit(varName + ":");
                    emitIndented(".quad 0"); // Default to 64-bit integer
                    break;
            }
            emit(""); // Add empty line for readability
        }
    }

    @Override
    public void emitFunctionStart(Tuple functionTuple, FunctionInfo info) {
        String functionName = getFunctionName(functionTuple);
        currentFunction = functionName;
        handleFunctionPrologue(functionName);

        // Process parameters - store register parameters on the stack
        int paramCount = info.getParameters().size();
        int varCount = info.getVariables().size();

        // Allocate space for local variables and parameters
        // 8 bytes initial space for return address
        // Shortcut: 8 bytes per parameter/local

        currentFunctionStackSize = 8 + (8 * (paramCount + varCount));

        // Save parameters from registers to stack
        // First 6 parameters are in registers
        for (VariableInfo var : info.getParameters()) {
            if (var.getParamIndex() < 6) {
                int paramIndex = var.getParamIndex();
                int offset = -8 * (paramIndex + 1); // First param at -8(%rbp), second at -16(%rbp), etc.
                variableOffsets.put(var.getName(), offset);

                // Save parameter from register to stack
                X86_64Operand paramReg;
                switch (paramIndex) {
                    case 0:
                        paramReg = new Register(X86_64Register.RDI);
                        break;
                    case 1:
                        paramReg = new Register(X86_64Register.RSI);
                        break;
                    case 2:
                        paramReg = new Register(X86_64Register.RDX);
                        break;
                    case 3:
                        paramReg = new Register(X86_64Register.RCX);
                        break;
                    case 4:
                        paramReg = new Register(X86_64Register.R8);
                        break;
                    case 5:
                        paramReg = new Register(X86_64Register.R9);
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid parameter index: " + paramIndex);
                }
                X86_64Operand paramMem = new Memory(offset + "(%rbp)");
                emitAssembly(X86_64Instruction.MOVQ, paramReg, paramMem);
            }
        }
    }

    private String getFunctionName(Tuple functionTuple) {
        Label funcLabel = (Label) functionTuple.getOperands().get(0);
        return funcLabel.toString();
    }

    private void handleFunctionPrologue(String functionName) {
        output = new StringBuilder();

        emit(".text");
        emit(".globl " + functionName);
        emit(".type  " + functionName + ", @function");
        emit(functionName + ":");

        X86_64Operand rbpReg = new Register(X86_64Register.RBP);
        X86_64Operand rspReg = new Register(X86_64Register.RSP);
        X86_64Operand placeholder = new Immediate(stackSizePlaceHolder);

        emitAssembly(X86_64Instruction.PUSHQ, rbpReg);
        emitAssembly(X86_64Instruction.MOVQ, rspReg, rbpReg);
        emitAssembly(X86_64Instruction.SUBQ, placeholder, rspReg);

        // Reset stack tracking variables
        currentFunction = functionName;
        tempOffsets.clear();
        variableOffsets.clear();
    }

    private void handleFunctionEnd(String functionName) {
        // Add a label that return statements can jump to
        emit(functionName + "_epilogue:");

        X86_64Operand rbpReg = new Register(X86_64Register.RBP);
        X86_64Operand rspReg = new Register(X86_64Register.RSP);

        emitAssembly(X86_64Instruction.MOVQ, rbpReg, rspReg); // restore stack pointer to saved frame pointer
        emitAssembly(X86_64Instruction.POPQ, rbpReg); // restore caller's base pointer
        emitAssembly(X86_64Instruction.RET); // return to caller
        emit(""); // Empty line for readability

        // Ensure stack is aligned to 16 bytes (ABI requirement)
        int alignedStackSize = (currentFunctionStackSize + 15) & ~15;

        // Replace the stack size placeholder in the current function's output

        String currentOutput = output.toString();
        String newOutput = currentOutput.replace("$" + stackSizePlaceHolder, "$" + alignedStackSize);

        masterOutput.append(newOutput);
        output.setLength(0);
    }

    // ==================
    // Tuples processing
    // ==================

    @Override
    protected void emitProgram(Tuple tuple) {
        currentFunction = "main";
        handleFunctionPrologue(currentFunction);

        // For now, our main will not have parameters
        // and will return an integer

        currentFunctionStackSize = 8; // 8 bytes for return address
    }

    @Override
    protected void emitEndProgram(Tuple tuple) {
        handleFunctionEnd(currentFunction);
    }

    @Override
    protected void emitEndFunction(Tuple tuple) {
        handleFunctionEnd(currentFunction);
    }

    @Override
    protected void emitAssign(Tuple tuple) {

        Operand target = tuple.getOperands().get(0);
        Operand source = tuple.getOperands().get(1);

        String sourceRef = getOperandReference(source);
        String targetRef = getOperandReference(target);

        // Convert string operands to X86_64Operand objects
        X86_64Operand raxReg = new Register(X86_64Register.RAX);
        X86_64Operand targetMem = new Memory(targetRef);

        // Special case for immediate values (including constants from global section)
        if (sourceRef.startsWith("$")) {
            // Direct immediate value
            X86_64Operand immValue = new Immediate(sourceRef.substring(1));
            emitAssembly(X86_64Instruction.MOVQ, immValue, raxReg);
        } else if (sourceRef.contains("__const_") && sourceRef.endsWith("(%rip)")) {
            // Check if it's a constant 0 from the global section
            if (source instanceof Constant) {
                Constant constant = (Constant) source;
                Object value = constant.getValue();

                // If it's a numeric constant with value 0, use immediate value directly
                if ((value instanceof Integer && (Integer) value == 0) ||
                        (value instanceof Long && (Long) value == 0)) {
                    X86_64Operand zeroImm = new Immediate(0);
                    emitAssembly(X86_64Instruction.MOVQ, zeroImm, raxReg);
                } else {
                    // Otherwise, load from memory
                    X86_64Operand sourceMem = new Memory(sourceRef);
                    emitAssembly(X86_64Instruction.MOVQ, sourceMem, raxReg);
                }
            } else {
                // Not a constant, load from memory
                X86_64Operand sourceMem = new Memory(sourceRef);
                emitAssembly(X86_64Instruction.MOVQ, sourceMem, raxReg);
            }
        } else {
            // Regular memory operand
            X86_64Operand sourceMem = new Memory(sourceRef);
            emitAssembly(X86_64Instruction.MOVQ, sourceMem, raxReg);
        }

        // Store to target
        emitAssembly(X86_64Instruction.MOVQ, raxReg, targetMem);
    }

    @Override
    protected void emitAdd(Tuple tuple) {
        emitBinaryOp(tuple, X86_64Instruction.ADDQ, X86_64Instruction.ADDSD);
    }

    @Override
    protected void emitSub(Tuple tuple) {
        emitBinaryOp(tuple, X86_64Instruction.SUBQ, X86_64Instruction.SUBSD);
    }

    @Override
    protected void emitMul(Tuple tuple) {
        emitBinaryOp(tuple, X86_64Instruction.IMULQ, X86_64Instruction.MULSD);
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

            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand xmm0Reg = new Register(X86_64Register.XMM0);
            X86_64Operand xmm1Reg = new Register(X86_64Register.XMM1);
            X86_64Operand resultMem = new Memory(resultRef);

            if (isFloatingPointConstant(left)) {
                // For floating point constants, we need to use the RIP-relative addressing
                X86_64Operand leftMem = new Memory(leftRef + "(%rip)");
                emitAssembly(X86_64Instruction.MOVQ, leftMem, raxReg);
                emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm0Reg);
            } else {
                X86_64Operand leftMem = new Memory(leftRef);
                emitAssembly(X86_64Instruction.MOVQ, leftMem, xmm0Reg);
            }

            if (isFloatingPointConstant(right)) {
                // For floating point constants, we need to use the RIP-relative addressing
                X86_64Operand rightMem = new Memory(rightRef + "(%rip)");
                emitAssembly(X86_64Instruction.MOVQ, rightMem, raxReg);
                emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm1Reg);
            } else {
                X86_64Operand rightMem = new Memory(rightRef);
                emitAssembly(X86_64Instruction.MOVQ, rightMem, xmm1Reg);
            }

            emitAssembly(X86_64Instruction.DIVSD, xmm1Reg, xmm0Reg);
            emitAssembly(X86_64Instruction.MOVQ, xmm0Reg, resultMem);
        } else {
            // Integer division
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand rcxReg = new Register(X86_64Register.RCX);
            X86_64Operand leftMem = new Memory(leftRef);
            X86_64Operand rightMem = new Memory(rightRef);
            X86_64Operand resultMem = new Memory(resultRef);

            // x86_64 division is special - it uses rdx:rax as dividend
            emitAssembly(X86_64Instruction.MOVQ, leftMem, raxReg);
            emitAssembly(X86_64Instruction.CQTO); // Sign-extend rax into rdx
            emitAssembly(X86_64Instruction.MOVQ, rightMem, rcxReg);
            emitAssembly(X86_64Instruction.IDIVQ, rcxReg); // Divide rdx:rax by rcx, result in rax
            emitAssembly(X86_64Instruction.MOVQ, raxReg, resultMem);
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
    private void emitBinaryOp(Tuple tuple, X86_64Instruction intInstruction, X86_64Instruction floatInstruction) {
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

            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand xmm0Reg = new Register(X86_64Register.XMM0);
            X86_64Operand xmm1Reg = new Register(X86_64Register.XMM1);
            X86_64Operand resultMem = new Memory(resultRef);

            if (isFloatingPointConstant(left)) {
                // For floating point constants, we need to use the RIP-relative addressing
                X86_64Operand leftMem = new Memory(leftRef + "(%rip)");
                emitAssembly(X86_64Instruction.MOVQ, leftMem, raxReg);
                emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm0Reg);
            } else {
                X86_64Operand leftMem = new Memory(leftRef);
                emitAssembly(X86_64Instruction.MOVQ, leftMem, xmm0Reg);
            }

            if (isFloatingPointConstant(right)) {
                // For floating point constants, we need to use the RIP-relative addressing
                X86_64Operand rightMem = new Memory(rightRef + "(%rip)");
                emitAssembly(X86_64Instruction.MOVQ, rightMem, raxReg);
                emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm1Reg);
            } else {
                X86_64Operand rightMem = new Memory(rightRef);
                emitAssembly(X86_64Instruction.MOVQ, rightMem, xmm1Reg);
            }

            emitAssembly(floatInstruction, xmm1Reg, xmm0Reg);
            emitAssembly(X86_64Instruction.MOVQ, xmm0Reg, resultMem);
        } else {
            // Integer operation
            String leftRef = getOperandReference(left);
            String rightRef = getOperandReference(right);
            String resultRef = getOperandReference(result);

            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand leftMem = new Memory(leftRef);
            X86_64Operand rightMem = new Memory(rightRef);
            X86_64Operand resultMem = new Memory(resultRef);

            emitAssembly(X86_64Instruction.MOVQ, leftMem, raxReg);
            emitAssembly(intInstruction, rightMem, raxReg);
            emitAssembly(X86_64Instruction.MOVQ, raxReg, resultMem);
        }
    }

    /**
     * Helper method to emit binary operations (integer only).
     *
     * @param tuple       the tuple
     * @param instruction the assembly instruction
     */
    private void emitBinaryOp(Tuple tuple, X86_64Instruction instruction) {
        emitBinaryOp(tuple, instruction, instruction);
    }

    @Override
    protected void emitAnd(Tuple tuple) {
        emitBinaryOp(tuple, X86_64Instruction.ANDQ);
    }

    @Override
    protected void emitOr(Tuple tuple) {
        emitBinaryOp(tuple, X86_64Instruction.ORQ);
    }

    @Override
    protected void emitNot(Tuple tuple) {

        Operand result = tuple.getOperands().get(0);
        Operand operand = tuple.getOperands().get(1);

        String operandRef = getOperandReference(operand);
        String resultRef = getOperandReference(result);

        // Convert string operands to X86_64Operand objects
        X86_64Operand raxReg = new Register(X86_64Register.RAX);
        X86_64Operand operandMem = new Memory(operandRef);
        X86_64Operand resultMem = new Memory(resultRef);
        X86_64Operand boolMask = new Immediate(1);

        // Use the new emitAssembly method with X86_64Instruction and X86_64Operand
        emitAssembly(X86_64Instruction.MOVQ, operandMem, raxReg);
        emitAssembly(X86_64Instruction.NOTQ, raxReg);
        emitAssembly(X86_64Instruction.ANDQ, boolMask, raxReg); // Ensure boolean result (0 or 1)
        emitAssembly(X86_64Instruction.MOVQ, raxReg, resultMem);
    }

    @Override
    protected void emitEq(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETE);
    }

    @Override
    protected void emitNeq(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETNE);
    }

    @Override
    protected void emitGt(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETG);
    }

    @Override
    protected void emitGte(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETGE);
    }

    @Override
    protected void emitLt(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETL);
    }

    @Override
    protected void emitLte(Tuple tuple) {
        emitComparison(tuple, X86_64Instruction.SETLE);
    }

    /**
     * Helper method to emit comparison operations.
     *
     * @param tuple          the tuple
     * @param setInstruction the set instruction (SETE, SETNE, etc.)
     */
    private void emitComparison(Tuple tuple, X86_64Instruction setInstruction) {
        List<Operand> ops = tuple.getOperands();
        Operand result = ops.get(0);
        Operand left = ops.get(1);
        Operand right = ops.get(2);

        String leftRef = getOperandReference(left);
        String rightRef = getOperandReference(right);
        String resultRef = getOperandReference(result);

        // Convert string operands to X86_64Operand objects
        X86_64Operand raxReg = new Register(X86_64Register.RAX);
        X86_64Operand alReg = new Register(X86_64Register.AL);
        X86_64Operand leftMem = new Memory(leftRef);
        X86_64Operand rightMem = new Memory(rightRef);
        X86_64Operand resultMem = new Memory(resultRef);

        // Use the new emitAssembly method with X86_64Instruction and X86_64Operand
        emitAssembly(X86_64Instruction.MOVQ, leftMem, raxReg);
        emitAssembly(X86_64Instruction.CMPQ, rightMem, raxReg);
        emitAssembly(setInstruction, alReg);
        emitAssembly(X86_64Instruction.MOVZBQ, alReg, raxReg); // Zero-extend byte to quad
        emitAssembly(X86_64Instruction.MOVQ, raxReg, resultMem);
    }

    @Override
    protected void emitIf(Tuple tuple) {
        Operand condition = tuple.getOperands().get(0);
        Label label = (Label) tuple.getOperands().get(1);

        String conditionRef = getOperandReference(condition);

        // Convert string operands to X86_64Operand objects
        X86_64Operand raxReg = new Register(X86_64Register.RAX);
        X86_64Operand conditionMem = new Memory(conditionRef);
        X86_64Operand labelOp = new X86_64Operand.Label(label.toString());

        // Use the new emitAssembly method with X86_64Instruction and X86_64Operand
        emitAssembly(X86_64Instruction.MOVQ, conditionMem, raxReg);
        emitAssembly(X86_64Instruction.TESTQ, raxReg, raxReg);
        emitAssembly(X86_64Instruction.JNZ, labelOp);    // Changed to JNZ - jump when condition is TRUE (non-zero)
    }

    @Override
    protected void emitGoto(Tuple tuple) {
        Label label = (Label) tuple.getOperands().get(0);
        X86_64Operand labelOp = new X86_64Operand.Label(label.toString());
        emitAssembly(X86_64Instruction.JMP, labelOp);
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

            // Check if the return value is a floating point value
            boolean isFloatingPoint = isFloatingPoint(returnValue);

            if (isFloatingPoint) {
                // For floating point returns, use XMM0 register
                X86_64Operand xmm0Reg = new Register(X86_64Register.XMM0);
                X86_64Operand returnMem = new Memory(returnRef);

                // Handle floating point constants differently
                if (isFloatingPointConstant(returnValue)) {
                    X86_64Operand raxReg = new Register(X86_64Register.RAX);
                    // For floating point constants, we need to use the RIP-relative addressing
                    X86_64Operand ripRelativeMem = new Memory(returnRef + "(%rip)");
                    emitAssembly(X86_64Instruction.MOVQ, ripRelativeMem, raxReg);
                    emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm0Reg);
                } else {
                    emitAssembly(X86_64Instruction.MOVQ, returnMem, xmm0Reg);
                }
            } else {
                // For integer/pointer/boolean returns, use RAX register
                X86_64Operand raxReg = new Register(X86_64Register.RAX);
                X86_64Operand returnMem = new Memory(returnRef);

                emitAssembly(X86_64Instruction.MOVQ, returnMem, raxReg);
            }
        }

        // Jump to the function epilogue
        X86_64Operand epilogueLabel = new X86_64Operand.Label(currentFunction + "_epilogue");
        emitAssembly(X86_64Instruction.JMP, epilogueLabel);
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
            X86_64Operand rdiReg = new Register(X86_64Register.RDI);
            X86_64Operand rsiReg = new Register(X86_64Register.RSI);
            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand formatLabelMem = new Memory(formatLabel + "(%rip)");
            X86_64Operand stringLabelMem = new Memory(stringLabel + "(%rip)");
            X86_64Operand printfCall = new X86_64Operand.Symbol("printf@GOTPCREL(%rip)");

            emitAssembly(X86_64Instruction.LEAQ, formatLabelMem, rdiReg);
            emitAssembly(X86_64Instruction.LEAQ, stringLabelMem, rsiReg);
            emitAssembly(X86_64Instruction.XORQ, raxReg, raxReg); // Clear AL for varargs printf
            emitAssembly(X86_64Instruction.CALL, printfCall);
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
                    X86_64Operand rdiReg = new Register(X86_64Register.RDI);
                    X86_64Operand raxReg = new Register(X86_64Register.RAX);
                    X86_64Operand xmm0Reg = new Register(X86_64Register.XMM0);
                    X86_64Operand eaxReg = new Register(X86_64Register.EAX);
                    X86_64Operand formatLabelMem = new Memory(formatLabel + "(%rip)");
                    X86_64Operand operandMem = new Memory(operandRef + "(%rip)");
                    X86_64Operand oneImm = new Immediate(1);
                    X86_64Operand printfCall = new X86_64Operand.Symbol("printf@GOTPCREL(%rip)");

                    emitAssembly(X86_64Instruction.LEAQ, formatLabelMem, rdiReg);
                    emitAssembly(X86_64Instruction.MOVQ, operandMem, raxReg);
                    emitAssembly(X86_64Instruction.MOVQ, raxReg, xmm0Reg);
                    emitAssembly(X86_64Instruction.MOVL, oneImm, eaxReg); // Set AL to 1 for one XMM register
                    emitAssembly(X86_64Instruction.CALL, printfCall);
                    return;
                }
            }

            // For non-floating point values
            X86_64Operand rdiReg = new Register(X86_64Register.RDI);
            X86_64Operand rsiReg = new Register(X86_64Register.RSI);
            X86_64Operand raxReg = new Register(X86_64Register.RAX);
            X86_64Operand formatLabelMem = new Memory(formatLabel + "(%rip)");
            X86_64Operand operandMem = new Memory(operandRef);
            X86_64Operand printfCall = new X86_64Operand.Symbol("printf@GOTPCREL(%rip)");

            emitAssembly(X86_64Instruction.LEAQ, formatLabelMem, rdiReg);
            emitAssembly(X86_64Instruction.MOVQ, operandMem, rsiReg);
            emitAssembly(X86_64Instruction.XORQ, raxReg, raxReg); // Clear AL for varargs printf
            emitAssembly(X86_64Instruction.CALL, printfCall);
        }
    }

    @Override
    protected void emitCall(Tuple tuple) {
        List<Operand> ops = tuple.getOperands();

        // Handle arguments (x86_64 calling convention)
        // First 6 args go in registers: rdi, rsi, rdx, rcx, r8, r9
        // Additional args go on the stack
        X86_64Operand[] argRegisters = {
            new Register(X86_64Register.RDI),
            new Register(X86_64Register.RSI),
            new Register(X86_64Register.RDX),
            new Register(X86_64Register.RCX),
            new Register(X86_64Register.R8),
            new Register(X86_64Register.R9)
        };
        X86_64Operand raxReg = new Register(X86_64Register.RAX);
        X86_64Operand rspReg = new Register(X86_64Register.RSP);

        // Push any arguments beyond the first 6 onto the stack (in reverse order)
        for (int i = ops.size() - 1; i >= 8; i--) {
            Operand arg = ops.get(i);
            String argRef = getOperandReference(arg);
            X86_64Operand argMem = new Memory(argRef);

            emitAssembly(X86_64Instruction.MOVQ, argMem, raxReg);
            emitAssembly(X86_64Instruction.PUSHQ, raxReg);
        }

        // Load the first 6 arguments into registers
        for (int i = 2; i < Math.min(ops.size(), 8); i++) {
            Operand arg = ops.get(i);
            String argRef = getOperandReference(arg);
            X86_64Operand argMem = new Memory(argRef);

            emitAssembly(X86_64Instruction.MOVQ, argMem, argRegisters[i - 2]);
        }

        // Call the function
        // Get the function name from the function operand
        Function function = (Function) ops.get(1);
        String funcName = function.getName();

        // Call the function using the base name
        X86_64Operand funcCall = new X86_64Operand.Symbol(funcName);
        emitAssembly(X86_64Instruction.CALL, funcCall);

        // Clean up stack if we pushed arguments
        if (ops.size() > 8) {
            int stackArgs = ops.size() - 8;
            X86_64Operand stackSizeImm = new Immediate(stackArgs * 8);
            emitAssembly(X86_64Instruction.ADDQ, stackSizeImm, rspReg);
        }

        // Store the return value if needed
        if (!ops.isEmpty()) {
            Operand result = ops.get(0);
            String resultRef = getOperandReference(result);
            X86_64Operand resultMem = new Memory(resultRef);

            emitAssembly(X86_64Instruction.MOVQ, raxReg, resultMem);
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

    /**
     * Override the hook method to emit a comment with the tuple's string
     * representation.
     */
    @Override
    protected void onEmitTuple(Tuple tuple) {
        String output = "# " + tuple.toString();

        switch (tuple.getOperator()) {
            case PROGRAM, LABEL, END_FUNCTION, END_PROGRAM -> {
                emit("");
                emit(output);
            }
            case FUNCTION -> {
            }
            default -> {
                emit("");
                emitIndented(output);
            }
        }
    }

    // ==================
    // private methods
    // ==================

    /**
     * Emit an assembly instruction with operands.
     *
     * @param instruction the instruction to emit
     * @param operands    the operands for the instruction
     */
    private void emitAssembly(X86_64Instruction instruction, X86_64Operand... operands) {
        StringBuilder sb = new StringBuilder(indent);
        sb.append(instruction.getMnemonic());

        if (operands.length > 0) {
            sb.append("\t").append(operands[0]);
        }

        if (operands.length > 1) {
            sb.append(", ").append(operands[1]);
        }

        output.append(sb).append("\n");
    }

    /**
     * Emit a line of assembly code.
     * Used for section directives and labels (left-aligned).
     */
    private void emit(String code) {
        output.append(code).append("\n");
    }

    /**
     * Emit a line of assembly code with indentation.
     * Used for instructions (indented with a tab).
     */
    private void emitIndented(String code) {
        output.append(indent).append(code).append("\n");
    }

    /**
     * Generate a unique label for string constants.
     */
    private String generateStringLabel() {
        return ".LCS" + (stringCounter++);
    }

    // Removed unused generateLabel method

    /**
     * Add a string constant to the data section.
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
     */
    private String getOperandReference(Operand operand) {
        if (operand instanceof Constant) {
            Constant constant = (Constant) operand;
            Object value = constant.getValue();

            // If we're in a global context, we might need to create a global variable for
            // this constant
            if (!currentFunction.equals("") && !currentFunction.equals("main")) {
                // We're in a function context, handle normally
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
            } else {
                // For integer constants, use immediate values directly
                if (value instanceof Integer || value instanceof Long) {
                    return "$" + value;
                } else if (value instanceof Boolean) {
                    return ((Boolean) value) ? "$1" : "$0";
                } else {
                    // For other types (String, Float, Double), we still need to create a global
                    // variable
                    // Generate a unique name for this constant
                    String varName = "__const_" + globalVariables.size();

                    // Add to the map of global variables with its type and value
                    Operand.OperandType type;
                    if (value instanceof String) {
                        type = Operand.OperandType.STRING;
                    } else if (value instanceof Float || value instanceof Double) {
                        type = Operand.OperandType.FLOAT;
                    } else {
                        type = Operand.OperandType.INTEGER;
                    }

                    globalVariables.put(varName, new GlobalVarInfo(type, value));

                    // Use RIP-relative addressing for global variables
                    return varName + "(%rip)";
                }
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
                    // Global variable - add to the map of global variables with its type and value
                    Object value = null;

                    // If the variable is a constant, get its value
                    if (variable.getEntry() != null && variable.getEntry().getValue() != null) {
                        value = variable.getEntry().getValue();
                    }

                    globalVariables.put(varName, new GlobalVarInfo(variable.getType(), value));
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

                // Update the current function's stack size if this temporary requires more
                // space
                if (newStackSize > currentFunctionStackSize) {
                    currentFunctionStackSize = newStackSize;
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
     */
    private String generateFloatLabel() {
        return ".LCF" + (floatCounter++);
    }

    /**
     * Add a float constant to the data section.
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
     */
    private String escapeString(String str) {
        return str.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\"", "\\\"");
    }

}


