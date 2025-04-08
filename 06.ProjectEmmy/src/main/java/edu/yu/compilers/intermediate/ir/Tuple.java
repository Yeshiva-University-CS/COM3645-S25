package edu.yu.compilers.intermediate.ir;

import java.util.ArrayList;
import java.util.List;

/**
 * The Tuple class represents a basic instruction in the IR.
 * Each tuple consists of an operator and operands.
 */
public class Tuple {
    private Operator operator;
    private ArrayList<Operand> operands;

    /**
     * Construct a tuple with the given operator.
     * 
     * @param operator the tuple operator
     */
    public Tuple(Operator operator) {
        this.operator = operator;
        this.operands = new ArrayList<>();
    }

    /**
     * Construct a tuple with the given operator and operands.
     * 
     * @param operator the tuple operator
     * @param operands the tuple operands
     */
    public Tuple(Operator operator, Operand... operands) {
        this.operator = operator;
        this.operands = new ArrayList<>();
        for (Operand operand : operands) {
            this.operands.add(operand);
        }
    }

    /**
     * Add an operand to this tuple.
     * 
     * @param operand the operand to add
     */
    public void addOperand(Operand operand) {
        operands.add(operand);
    }

    /**
     * Get the operator of this tuple.
     * 
     * @return the operator
     */
    public Operator getOperator() {
        return operator;
    }

    /**
     * Get the operands of this tuple.
     * 
     * @return the list of operands
     */
    public List<Operand> getOperands() {
        return operands;
    }

    /**
     * Return a string representation of this tuple.
     * 
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(operator);

        if (!operands.isEmpty()) {
            sb.append(" ");
            for (int i = 0; i < operands.size(); i++) {
                if (i > 0)
                    sb.append(", ");
                sb.append(operands.get(i));
            }
        }

        return sb.toString();
    }
}
