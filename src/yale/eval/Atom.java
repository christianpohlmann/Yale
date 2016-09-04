/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

public abstract class Atom extends SExpr {
    @Override
    public boolean isAtom() {
        return true;
    }
}
