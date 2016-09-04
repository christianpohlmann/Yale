/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.util.List;

import yale.main.Kernel;

public class Printer extends Function {

    private boolean newline;
    private Symbol sym;

    /**
     * Creates a new printer.
     * 
     * @param newline
     *            If true, a newline is attached to the printed object.
     * @param sym
     *            Symbol under which this printer is available (needed in case
     *            an incorrect number of parameters is passed).
     */
    public Printer(boolean newline, Symbol sym) {
        this.newline = newline;
    }

    @Override
    public SExpr apply(List<SExpr> parameters, Environment env) {
        Kernel.validateParameters(parameters, 1, false, this.sym);
        if (!this.newline) {
            System.out.print(parameters.get(0));
        } else {
            System.out.println(parameters.get(0));
        }
        return Symbol.NIL;
    }
}
