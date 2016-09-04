/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

public class SpecialForm extends SExpr {

    @Override
    public SExpr eval(Environment env) throws RuntimeException {
        return null;
    }
    
    @Override
    public boolean isSpecialForm() {
        return true;
    }
    
    public String toString() {
        return String.format("<special form %d>", System.identityHashCode(this));
    }
}
