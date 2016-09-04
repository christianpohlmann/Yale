/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.parse;

public class Token {
    
    public static final Character PARENS_OPEN = '(';
    public static final Character PARENS_CLOSE = ')';
    static final Character QUOTE = '\'';
    
    enum TokenType {
        PARENS_OPEN, PARENS_CLOSE, QUOTE, LITERAL
    }
    
    private TokenType type;
    private String literal;
    
    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }
    
    public Token(TokenType type) {
        this.type = type;
        this.literal = null;
    }
    
    public TokenType getType() {
        return this.type;
    }
    
    public String getLiteral() {
        return this.literal;
    }
    
    public String toString() {
        return type.toString() + ((literal != null) ? " " + literal : "");
    }
}
