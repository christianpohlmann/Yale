/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

public abstract class Function extends SExpr {

    @Override
    public SExpr eval(Environment env) {
        return this;
    }
    
    @Override
    public boolean isFunction() {
        return true;
    }
    
    public String toString() {
        return String.format("<function %d>", System.identityHashCode(this));
    }
}
