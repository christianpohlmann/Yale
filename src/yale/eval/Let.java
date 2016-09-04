/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.util.List;

import yale.main.Kernel;

/**
 * S-expression which introduces a local lexical context with bindings and a
 * body in which s-expressions can be executed with access to the local lexical
 * context.
 */
public class Let extends SpecialForm {

    private boolean recursive;
    private Symbol symbol;

    /**
     * Create a new Let form
     * 
     * @param recursive
     *            Set to true if inner functions should be able to call
     *            themselves (letrec semantics). See source documentation of
     *            method _apply_ for details.
     * @param sym
     *            Top-level symbol that this form is bound to (used in parameter
     *            validation).
     */
    public Let(boolean recursive, Symbol sym) {
        this.recursive = recursive;
        this.symbol = sym;
    }

    @Override
    public SExpr apply(List<SExpr> parameters, Environment env) {
        Kernel.validateParameters(parameters, 1, true, this.symbol);
        Environment subEnv = new Environment(env);
        SExpr bindings = parameters.get(0);
        SExpr curr = bindings;
        while (!curr.equals(Symbol.NIL)) {
            SExpr currTuple = curr.car();
            SExpr left = currTuple.car();
            SExpr right = currTuple.cdr().car();
            if (!(left instanceof Symbol)) {
                throw new RuntimeException(
                        String.format("%s is not a symbol, but appears as first element in a binding", left));
            }
            /*
             * The next line of code differentiates between letrec and let
             * semantics:
             * 
             * If the bindings introduced by this form should be immediately
             * available in the right-hand-side expressions of the bindings, the
             * right-hand side expressions must be evaluated in the same
             * environment in which the bindings are defined. This allows the
             * definition of recursive functions inside a _let_ form (letrec
             * semantics).
             * 
             * However, if the bindings are evaluated in the same environment in
             * which the surrounding _let_ form is applied, access to the
             * bindings in the right-hand side expressions is not possible (let
             * semantics).
             */
            Environment evaluateIn = this.recursive ? subEnv : env;
            subEnv.defineBinding((Symbol) left, right.eval(evaluateIn));
            curr = curr.cdr();
        }
        for (int i = 1; i < parameters.size() - 1; ++i) {
            /*
             * Evaluate first n-1 parameters for their side-effects only
             */
            parameters.get(i).eval(subEnv);
        }
        /* Evaluate final parameter and return its value. */
        return parameters.get(parameters.size() - 1).eval(subEnv);
    }
}
