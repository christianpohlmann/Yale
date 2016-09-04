/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

public class Symbol extends Atom {

    public static final Symbol NIL = new Symbol("nil");

    private String name;

    public Symbol(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }

    @Override
    public SExpr eval(Environment env) throws RuntimeException {
        return env.getBinding(this);
    }
    
    public String toString() {
        return this.name;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Symbol) {
            Symbol sym = (Symbol) obj;
            return sym.getName().equals(this.getName());
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        return this.name.hashCode();
    }
}
