package org.jruby.compiler.ir.instructions;

import org.jruby.compiler.ir.Operation;
import org.jruby.compiler.ir.operands.SelfVariable;
import org.jruby.compiler.ir.operands.Variable;
import org.jruby.compiler.ir.representations.InlinerInfo;
import org.jruby.interpreter.InterpreterContext;
import org.jruby.runtime.builtin.IRubyObject;

/*
 * Assign Argument passed into scope/method to destination Variable
 */
public class ReceiveArgumentInstruction extends NoOperandInstr {
    int argIndex;
    boolean restOfArgArray; // If true, the argument sub-array starting at this index is passed in via this instruction.

    public ReceiveArgumentInstruction(Variable destination, int argIndex,
            boolean restOfArgArray) {
        super(Operation.RECV_ARG, destination);
        
        this.argIndex = argIndex;
        this.restOfArgArray = restOfArgArray;
    }

    public ReceiveArgumentInstruction(Variable destination, int index) {
        this(destination, index, false);
    }

    public Instr cloneForInlining(InlinerInfo ii) {
        return new CopyInstr(ii.getRenamedVariable(result), ii.getCallArg(argIndex, restOfArgArray));
    }

    @Override
    public String toString() {
        return super.toString() + "(" + argIndex + (restOfArgArray ? ", ALL" : "") + ")";
    }

    @Override
    public void interpret(InterpreterContext interp, IRubyObject self) {
        // All interpretation already has self so we have no need to receive it.
        if (getResult() instanceof SelfVariable) return;

        Object value = interp.getParameter(argIndex);

        System.out.println("PARM INDEX: " + argIndex + " : " + value + " -> " + getResult());
        getResult().store(interp, value);
    }
}
