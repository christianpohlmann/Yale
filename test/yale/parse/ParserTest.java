/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.parse;

import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import yale.exception.ParseError;
import yale.parse.Token.TokenType;

/**
 * Testing Yale's lexer and parser.
 */
public class ParserTest {

    @Test
    public void testLexer() {
        Parser p = new Parser();
        List<Token> tokens = p.lex("( + 12  abc (+ 3))");
        assertTrue(tokens.size() == 9);
    }
    
    @Test
    public void testLexerIncomplete() {
        Parser p = new Parser();
        List<Token> tokens = p.lex("(+ 1 foo");
        assertTrue(tokens.size() == 4);
    }
    
    @Test
    public void testLexerAtom() {
        Parser p = new Parser();
        List<Token> tokens = p.lex(" 123.456  ");
        assertTrue(tokens.size() == 1 && tokens.get(0).getType() == TokenType.LITERAL);
    }
    
    @Test
    public void testExtractTokSExprs1SE1() throws ParseError{
        Parser p = new Parser();
        List<Token> tokens = p.lex("(* 12 14 (- aaa qux))");
        assertTrue(p.extractTokSExprs(tokens).size() == 1);
    }
    
    @Test
    public void testExtractTokSExprs1SE2() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("qux");
        assertTrue(p.extractTokSExprs(tokens).size() == 1);
    }
    
    @Test
    public void testExtractTokSExprs1SE3() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("()");
        assertTrue(p.extractTokSExprs(tokens).size() == 1);
    }
    
    @Test
    public void testExtractTokSExprs2SE() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("abc xyz");
        assertTrue(p.extractTokSExprs(tokens).size() == 2);
    }
    
    @Test
    public void testExtractTokSExprs3SE() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("(*) q (bar qux)");
        assertTrue(p.extractTokSExprs(tokens).size() == 3);
    }
    
    @Test
    public void testExtractTokSExprsEmpty() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("   ");
        assertTrue(p.extractTokSExprs(tokens).size() == 0);
    }

    @Test
    public void testExtractTokSExprsMalformedSExpr() {
        Parser p = new Parser();
        List<Token> tokens = p.lex(")a(");
        boolean parseErrorRaised = false;
        try {
            p.extractTokSExprs(tokens);
        } catch (ParseError e) {
            parseErrorRaised = true;
        }
        assertTrue(parseErrorRaised);
    }
    
    @Test
    public void testExtractTokSExprQuote1() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("'xy");
        LinkedList<LinkedList<Token>> tokSexprs = p.extractTokSExprs(tokens);
        assertTrue(tokSexprs.size() == 1);
        assertTrue(tokSexprs.get(0).get(0).getType() == TokenType.QUOTE);
    }
    
    @Test
    public void testExtractTokSExprQuote2() throws ParseError {
        Parser p = new Parser();
        List<Token> tokens = p.lex("'(a b) 3 'q");
        assertTrue(p.extractTokSExprs(tokens).size() == 3);
    }
    
    @Test
    public void testExtractTokSExprMalformedQuote() {
        Parser p = new Parser();
        List<Token> tokens = p.lex("'");
        boolean parseErrorRaised = false;
        try {
            p.extractTokSExprs(tokens);
        } catch (ParseError e) {
            parseErrorRaised = true;
        }
        assertTrue(parseErrorRaised);
    }
}
