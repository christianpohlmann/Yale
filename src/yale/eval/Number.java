/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.eval;

import java.math.BigDecimal;

public class Number extends Atom {

    private BigDecimal number;
    
    public Number(BigDecimal number) {
        this.number = number;
    }
    
    @Override
    public SExpr eval(Environment env) {
        return this;
    }
    
    @Override
    public BigDecimal getNumericValue() {
        return this.number;
    }
    
    @Override
    public boolean isNumber() {
        return true;
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof Number) {
            Number num = (Number) obj;
            return num.getNumericValue().equals(this.getNumericValue());
        } else {
            return false;
        }
    }
    
    public String toString () {
        return this.number.toString();
    }
}
