/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.util.HashMap;

/**
 * This class represents a set of bindings between symbols and s-expresions. An
 * environment can have a parent environment, thus enabling the implementation
 * of lexical scoping in Yale.
 *
 */
public class Environment {

    private Environment parent;
    private HashMap<Symbol, SExpr> bindings;

    public Environment() {
        this(null);
    }

    public Environment(Environment parent) {
        this.parent = parent;
        this.bindings = new HashMap<Symbol, SExpr>();
    }

    /**
     * Create a new binding for _sym_ in the current environment (cf. special
     * form _define_).
     * 
     * @param sym
     *            The symbol for which a binding should be introduced.
     * @param sexpr
     *            S-expression the symbol is bound to.
     * @throws RuntimeException
     *             If a binding for _sym_ already exists in this environment.
     */
    public void defineBinding(Symbol sym, SExpr sexpr) {
        if (this.bindings.containsKey(sym)) {
            throw new RuntimeException("Binding for " + sym + " already exists.");
        }
        this.bindings.put(sym, sexpr);
    }

    /**
     * Update an existing binding for _sym_. If _sym_ is unbound, update binding
     * in the parent environment. If no binding can be retrieved and the parent
     * environment is unset, raise a RuntimeException.
     * 
     * @param sym
     *            The symbol for which a binding should be updated.
     * @param sexpr
     *            S-expression the symbol is bound to.
     * @throws RuntimeException
     *             If no existing binding for _sym_ can be retrieved.
     */
    public void updateBinding(Symbol sym, SExpr sexpr) {
        if (this.bindings.containsKey(sym)) {
            this.bindings.put(sym, sexpr);
        } else if (this.parent != null) {
            parent.updateBinding(sym, sexpr);
        } else {
            throw new RuntimeException("Cannot assign to " + sym + " as it is unbound.");
        }
    }

    /**
     * Get value of binding identified by _sym_ and look in parent environment
     * if _sym_ is unbound in the current environment.
     * 
     * @param sym
     *            Symbol for which a binding should be retrieved.
     * @return The value that _sym_ is bound to.
     * @throws RuntimeException
     *             If _sym_ is unbound and the parent environment is unset.
     */
    public SExpr getBinding(Symbol sym) {
        SExpr result = this.bindings.get(sym);
        if (result == null && parent == null) {
            throw new RuntimeException("Symbol " + sym + " is unbound.");
        } else if (result == null && parent != null) {
            return parent.getBinding(sym);
        } else {
            return result;
        }
    }
}
