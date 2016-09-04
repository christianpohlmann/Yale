/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.math.BigDecimal;
import java.util.List;

/**
 * This is the base class for all symbolic expressions in yale. It offers a
 * number of operations shared by all s-expr types and provides meaningful
 * default behaviour.
 *
 */
public abstract class SExpr {

    /**
     * Evaluate this s-expression with variable bindings defined in _env_. Since
     * data is of the same type as code in Yale (and in fact in any Lisp), it
     * also returns an s-expression, i.e. the value of an s-expression is again
     * an s-expression.
     * 
     * @param env
     *            Context in which the expression is evaluated.
     * @return The value of this s-expression that was retrieved in the context
     *         of the passed environment.
     * @throws java.lang.RuntimeException
     *             in case there are any errors (such as unbound symbols, an
     *             operation on this s-expression is not supported etc.)
     */
    public abstract SExpr eval(Environment env);

    /**
     * Checks if this s-expression is a function.
     * 
     * @return true if this s-expression is a function and false otherwise.
     */
    public boolean isFunction() {
        return false;
    }

    /**
     * Checks if this s-expression is a special form.
     * 
     * @return true if this s-expression is a special form and false otherwise.
     */
    public boolean isSpecialForm() {
        return false;
    }

    /**
     * Checks if this s-expression is a cons pair.
     * 
     * @return true if this s-expression is a cons pair and false otherwise.
     */
    public boolean isCons() {
        return false;
    }

    /**
     * Check if this s-expression is a number.
     * 
     * @return true if this s-expression is a number and false otherwise.
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Checks if this s-expression is an atom.
     * 
     * @return true if this s-expression is an atom and false otherwise.
     */
    public boolean isAtom() {
        return false;
    }

    /**
     * The car part (i.e. left subtree) of this s-expression. Only implemented
     * for cons pairs.
     * 
     * @return the car part of a cons pair which is the left subtree of the
     *         conspair.
     */
    public SExpr car() {
        throw new RuntimeException("Object " + this + " does not support car.");
    }

    /**
     * The cdr part (i.e. right subtree) of this s-expression. Only implemented
     * for cons pairs.
     * 
     * @return the cdr part of a cons pair which is the right subtree of the
     *         conspair.
     */
    public SExpr cdr() {
        throw new RuntimeException("Object " + this + " does not support cdr.");
    }

    /**
     * Apply this s-expression to the passed parameters. This method is called
     * on the left subtree of a cons pair when it is evaluated. Applicable are
     * functions and special forms.
     * 
     * @param parameters
     *            A list of parameters. The eval()-implementation of _Cons_
     *            cares about constructing this list.
     * @param env
     *            lexical context in which to apply this s-expr.
     * @return The result of this application which is also an s-expression.
     */
    public SExpr apply(List<SExpr> parameters, Environment env) {
        throw new RuntimeException("Object " + this + " is not callable.");
    }

    /**
     * Get the actual number (i.e. BigDecimal) representing this s-expression.
     * This method is only defined for numbers.
     * 
     * @return The BigDecimal behind this s-expression.
     */
    public BigDecimal getNumericValue() {
        throw new RuntimeException("Object " + this + " is not a number.");
    }
}
