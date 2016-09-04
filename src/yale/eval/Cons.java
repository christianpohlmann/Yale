/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.util.LinkedList;
import java.util.List;

import yale.parse.Token;

/**
 * Representing a cons pair which is basically a binary tree. The left subtree
 * is identified by car and the right subtree is identified by cdr.
 */
public class Cons extends SExpr {

    private SExpr car;
    private SExpr cdr;

    public Cons(SExpr car, SExpr cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    /**
     * When a cons pair is evaluated, it applies the value of the left subtree
     * to a list of parameters which is retrieved by iterating successively over
     * the the first values of the right subtrees.
     */
    @Override
    public SExpr eval(Environment env) {
        SExpr form = this.car.eval(env);
        List<SExpr> params = null;
        if (form.isFunction()) { // If the form is a function, evaluate all of
                                 // its parameters.
            params = getParameters(true, env);
        }
        /*
         * The parameters passed to a special form are not evaluated. If some
         * need to be evaluated, the special form itself needs to take care of
         * it.
         */
        else if (form.isSpecialForm()) {
            params = getParameters(false, env);
        }
        return form.apply(params, env);
    }

    @Override
    public SExpr car() {
        return this.car;
    }

    @Override
    public SExpr cdr() {
        return this.cdr;
    }

    @Override
    public boolean isCons() {
        return true;
    }

    private List<SExpr> getParameters(boolean evaluate, Environment env) {
        SExpr currCdr = cdr;
        List<SExpr> parameters = new LinkedList<SExpr>();
        while (!currCdr.equals(Symbol.NIL)) {
            parameters.add(evaluate ? currCdr.car().eval(env) : currCdr.car());
            currCdr = currCdr.cdr();
        }
        return parameters;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        SExpr iter = this;
        sb.append(Token.PARENS_OPEN);
        while (!iter.equals(Symbol.NIL)) {
            sb.append(iter.car().toString() + " ");
            iter = iter.cdr();
        }
        sb.delete(sb.length() - 1, sb.length());
        sb.append(Token.PARENS_CLOSE);
        return sb.toString();
    }
}
