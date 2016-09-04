/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.parse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import yale.eval.Cons;
import yale.eval.SExpr;
import yale.eval.Symbol;
import yale.exception.ParseError;
import yale.parse.Token.TokenType;

/**
 * Instances of this class can read s-expressions which are given as strings.
 * When a string is read, it is converted into a list of tokens first (lexing)
 * which is subsequently converted to an s-expression representing the original
 * string. Since the s-expression is representing data only, it exclusively
 * consists of cons pairs and atoms (i.e. numbers and symbols).
 */
public class Parser {

    private static final int IDX_SUBEXPR = 0;
    private static final int IDX_REST = 1;

    public List<SExpr> parse(String expr) throws ParseError {
        LinkedList<LinkedList<Token>> tokSExprs = extractTokSExprs(lex(expr));
        Iterator<LinkedList<Token>> iter = tokSExprs.iterator();
        List<SExpr> results = new LinkedList<SExpr>();
        while (iter.hasNext()) {
            LinkedList<Token> tokSExpr = iter.next();
            SExpr currResult = parseSingleExpr(tokSExpr);
            results.add(currResult);
        }
        return results;
    }

    private SExpr parseSingleExpr(List<Token> tokSExpr) throws ParseError {
        Token currToken = tokSExpr.get(0);
        if (currToken.getType() == TokenType.PARENS_OPEN) {
            SExpr car = null;
            SExpr cdr = null;
            Token nextToken = tokSExpr.get(1);
            if (nextToken.getType() == TokenType.PARENS_CLOSE) {
                return Symbol.NIL;
            } else if (nextToken.getType() == TokenType.PARENS_OPEN || nextToken.getType() == TokenType.LITERAL) {
                Map<Integer, List<Token>> subExprAndRest = extractSubExpression(tokSExpr);
                List<Token> subExpr = subExprAndRest.get(IDX_SUBEXPR);
                List<Token> rest = subExprAndRest.get(IDX_REST);
                car = parseSingleExpr(subExpr);
                cdr = parseSingleExpr(rest);
                return new Cons(car, cdr);
            } else if (nextToken.getType() == TokenType.QUOTE) {
                return parseQuoteAt(tokSExpr, 1);
            } else {
                throw new ParseError(String.format("Unexpected token %s.", nextToken.toString()));
            }

        } else if (currToken.getType() == TokenType.LITERAL) {
            return parseLiteral(currToken);
        } else if (currToken.getType() == TokenType.QUOTE) {
            return parseQuoteAt(tokSExpr, 0);
        }
        throw new ParseError(String.format("Unexpected token %s.", currToken.toString()));
    }

    /**
     * Parse an s-expression with a quote token at index _idx_
     * 
     * @param tokSExpr
     *            List of tokens representing a single s-expression
     * @param idx
     *            Index where the quote token is located (usually 0 or 1)
     * @return Parsed tokSExpr by calling _parseSingleExpr_
     * @throws ParseError
     *             In case a parse error arises
     */
    private SExpr parseQuoteAt(List<Token> tokSExpr, int idx) throws ParseError {
        tokSExpr.set(idx, new Token(TokenType.PARENS_OPEN)); // ' is replaced by
                                                             // (
        tokSExpr.add(++idx, new Token(TokenType.LITERAL, "quote"));
        /*
         * Find position where to insert closing parens, i.e. identify quoted
         * sub-expression
         */
        int bal = 0;
        ++idx; // shift index one to the right, because a quote was added
        TokenType currType = null;
        do {
            currType = tokSExpr.get(idx).getType();
            ++idx;
            if (currType == TokenType.PARENS_OPEN) {
                ++bal;
            } else if (currType == TokenType.PARENS_CLOSE) {
                --bal;
            }
            /*
             * completely ignore quotes here, because they belong to the
             * subsequent sub-expression
             */
        } while (bal != 0 || currType == TokenType.QUOTE);
        tokSExpr.add(idx, new Token(TokenType.PARENS_CLOSE));
        return parseSingleExpr(tokSExpr);
    }

    private SExpr parseLiteral(Token token) {
        String literal = token.getLiteral();
        /*
         * If the literal starts with a digit or with a minus sign, the parser
         * tries to treat it as number
         */
        boolean startsWithMinus = literal.charAt(0) == '-';
        if (Character.isDigit(literal.charAt(0)) || startsWithMinus) {
            try {
                yale.eval.Number num = new yale.eval.Number(new BigDecimal(literal));
                return num;
            } catch (NumberFormatException e) {
                /*
                 * Literal is not a number afterall, i.e. a symbol will be
                 * returned
                 */
                return new Symbol(literal);
            }
        } else {
            return new Symbol(literal);
        }
    }

    /**
     * Extracts the first sub-expression from a list of tokens. This
     * sub-expression can be a literal or a list of tokens (delimited by '(' and
     * ')')
     * 
     * @param tokens
     *            A list of tokens representing a complete s-expression.
     * @return Map with two entries: The extracted first sub-expression and the
     *         original expression with the sub-expression removed, e.g. for (a
     *         b) the return value is <a, (b)>
     * @throws ParseError
     *             In case no sub-expression can be extracted.
     */
    private Map<Integer, List<Token>> extractSubExpression(List<Token> tokens) throws ParseError {
        Map<Integer, List<Token>> result = new HashMap<Integer, List<Token>>();
        List<Token> subExpr = new LinkedList<Token>();
        Iterator<Token> iter = tokens.iterator();
        if (!iter.hasNext()) {
            throw new ParseError("Unexpected end of input.");
        }
        Token first = iter.next();
        if (first.getType() != TokenType.PARENS_OPEN) {
            throw new ParseError("Unexpected token " + first);
        }
        /*
         * first token is '(', the second token will determine if the
         * sub-expression is a literal or a nested s-expression, i.e. a list of
         * tokens.
         */
        if (!iter.hasNext()) {
            throw new ParseError("Unexpected end of input.");
        }
        Token second = iter.next();
        subExpr.add(second);
        if (second.getType() == TokenType.LITERAL) { // sub-expression is a
                                                     // literal
            result.put(IDX_SUBEXPR, subExpr);
            tokens.remove(1); // remove extracted sub-expression from list of
                              // tokens
            result.put(IDX_REST, tokens);
        } else if (second.getType() == TokenType.PARENS_OPEN) { // sub-expression
                                                                // is a nested
                                                                // s-expression
            int bal = 1;
            while (iter.hasNext() && bal > 0) {
                Token curr = iter.next();
                if (curr.getType() == TokenType.PARENS_OPEN) {
                    bal++;
                } else if (curr.getType() == Token.TokenType.PARENS_CLOSE) {
                    bal--;
                }
                subExpr.add(curr);
            }
            List<Token> rest = new LinkedList<Token>();
            /*
             * add initial '(' to retain syntactically correct original message
             * with just the extracted sub-expression removed
             */
            rest.add(tokens.get(0));
            while (iter.hasNext()) { // add remaining tokens to rest list
                rest.add(iter.next());
            }
            result.put(IDX_SUBEXPR, subExpr);
            result.put(IDX_REST, rest);
        } else {
            throw new ParseError("Unexpected token " + second);
        }
        return result;
    }

    public List<SExpr> parseFromFile(String filename) {
        return null;
    }

    /**
     * Partitions given list of tokens into a list of lists of tokens. Each list
     * of tokens covers exactly one s-expression.
     * 
     * @param tokens
     *            list of tokens
     * @return List of lists of tokens or null if excess tokens remain.
     * @throws Exception
     */
    LinkedList<LinkedList<Token>> extractTokSExprs(List<Token> tokens) throws ParseError {
        LinkedList<LinkedList<Token>> tokSExprs = new LinkedList<LinkedList<Token>>();
        Iterator<Token> iter = tokens.iterator();
        LinkedList<Token> currTokSexpr = new LinkedList<Token>();
        int bal = 0;
        while (iter.hasNext()) {
            Token currToken = iter.next();
            if (currToken.getType() == TokenType.PARENS_OPEN) {
                bal += 1;
            } else if (currToken.getType() == TokenType.PARENS_CLOSE) {
                bal -= 1;
            }
            currTokSexpr.add(currToken);
            if (bal == 0 && currToken.getType() != TokenType.QUOTE) {
                tokSExprs.add(currTokSexpr);
                currTokSexpr = new LinkedList<Token>();
            } else if (bal < 0) { // more ) than ( observed, i.e. expression
                                  // must be malformed
                throw new ParseError(String.format("Malformed s-expression", tokSExprs.size() + 1));
            }
        }
        if (bal == 0 && currTokSexpr.size() == 0) { // rules out possibility of
                                                    // excess tokens
            return tokSExprs;
        } else {
            throw new ParseError("Malformed s-expression.");
        }
    }

    List<Token> lex(String expr) {
        List<Token> tokens = new LinkedList<Token>();
        Token nextToken = null;
        StringBuffer currLiteral = new StringBuffer();
        for (int i = 0; i < expr.length(); ++i) {
            char ch = expr.charAt(i);
            boolean isSeparator = true;
            if (ch == Token.PARENS_OPEN) {
                nextToken = new Token(TokenType.PARENS_OPEN);
            } else if (ch == Token.PARENS_CLOSE) {
                nextToken = new Token(TokenType.PARENS_CLOSE);
            } else if (ch == Token.QUOTE) {
                nextToken = new Token(TokenType.QUOTE);
            } else if (!Character.isWhitespace(ch)) {
                isSeparator = false;
                currLiteral.append(ch);
            }
            if (isSeparator || i == expr.length() - 1) {
                if (currLiteral.length() > 0) {
                    tokens.add(new Token(TokenType.LITERAL, currLiteral.toString()));
                    currLiteral = new StringBuffer();
                }
                if (nextToken != null) {
                    tokens.add(nextToken);
                    nextToken = null;
                }
            }
        }
        return tokens;
    }
}
