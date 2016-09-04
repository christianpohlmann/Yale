/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.exception;

public class ParseError extends Exception {
    
    public ParseError() {}
    
    public ParseError(String msg) {
        super(msg);
    }
}
