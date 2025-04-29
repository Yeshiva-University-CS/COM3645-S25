package edu.yu.compilers.backend.compiler;

/**
 * Enum representing x86-64 registers used in the code generator.
 */
public enum X86_64Register {
    // General purpose registers (64-bit)
    RAX("%rax"),
    RBX("%rbx"),
    RCX("%rcx"),
    RDX("%rdx"),
    RSI("%rsi"),
    RDI("%rdi"),
    RBP("%rbp"),
    RSP("%rsp"),
    R8("%r8"),
    R9("%r9"),
    R10("%r10"),
    R11("%r11"),
    R12("%r12"),
    R13("%r13"),
    R14("%r14"),
    R15("%r15"),
    
    // General purpose registers (32-bit)
    EAX("%eax"),
    EBX("%ebx"),
    ECX("%ecx"),
    EDX("%edx"),
    ESI("%esi"),
    EDI("%edi"),
    EBP("%ebp"),
    ESP("%esp"),
    R8D("%r8d"),
    R9D("%r9d"),
    R10D("%r10d"),
    R11D("%r11d"),
    R12D("%r12d"),
    R13D("%r13d"),
    R14D("%r14d"),
    R15D("%r15d"),
    
    // General purpose registers (8-bit)
    AL("%al"),
    BL("%bl"),
    CL("%cl"),
    DL("%dl"),
    
    // SSE registers (used for floating point)
    XMM0("%xmm0"),
    XMM1("%xmm1"),
    XMM2("%xmm2"),
    XMM3("%xmm3"),
    XMM4("%xmm4"),
    XMM5("%xmm5"),
    XMM6("%xmm6"),
    XMM7("%xmm7");
    
    private final String name;
    
    X86_64Register(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * Get a register by its name.
     * 
     * @param name the register name (with or without % prefix)
     * @return the register enum value
     * @throws IllegalArgumentException if the register name is invalid
     */
    public static X86_64Register fromString(String name) {
        String normalizedName = name.startsWith("%") ? name : "%" + name;
        
        for (X86_64Register reg : values()) {
            if (reg.name.equals(normalizedName)) {
                return reg;
            }
        }
        
        throw new IllegalArgumentException("Invalid register name: " + name);
    }
}
