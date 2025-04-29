package edu.yu.compilers.backend.compiler;

/**
 * Enum representing x86-64 assembly instructions used in the code generator.
 */
public enum X86_64Instruction {
    // Data Movement
    MOVQ("movq"),
    MOVL("movl"),
    MOVZBQ("movzbq"),
    LEAQ("leaq"),
    LEAVE("leave"),

    // Stack Operations
    PUSHQ("pushq"),
    POPQ("popq"),
    SUBQ("subq"),
    ADDQ("addq"),

    // Arithmetic Operations
    IMULQ("imulq"),
    IDIVQ("idivq"),
    MULSD("mulsd"),
    DIVSD("divsd"),
    ADDSD("addsd"),
    SUBSD("subsd"),

    // Logical Operations
    ANDQ("andq"),
    ORQ("orq"),
    NOTQ("notq"),
    TESTQ("testq"),
    CMPQ("cmpq"),

    // Control Flow
    JMP("jmp"),
    JE("je"),
    JNZ("jnz"),
    CALL("call"),
    RET("ret"),

    // Condition Code Operations
    SETE("sete"),
    SETNE("setne"),
    SETG("setg"),
    SETGE("setge"),
    SETL("setl"),
    SETLE("setle"),

    // Special Operations
    CQTO("cqto"),
    XORQ("xorq");

    private final String mnemonic;

    X86_64Instruction(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public String toString() {
        return mnemonic;
    }

    /**
     * Get the instruction enum value from a string mnemonic.
     *
     * @param mnemonic the instruction mnemonic
     * @return the corresponding X86_64Instruction enum value
     * @throws IllegalArgumentException if the mnemonic is not recognized
     */
    public static X86_64Instruction fromString(String mnemonic) {
        for (X86_64Instruction instruction : values()) {
            if (instruction.getMnemonic().equalsIgnoreCase(mnemonic)) {
                return instruction;
            }
        }
        throw new IllegalArgumentException("Unknown instruction mnemonic: " + mnemonic);
    }
}
