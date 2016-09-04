/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.main;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import yale.eval.SExpr;
import yale.exception.ParseError;

/**
 * Testing Yale's evaluation capabilities, covering both the core and the standard library.
 */
public class YaleTest {

    private Yale yale;
    
    /**
     * Checks if the evaluation result of the expressions given as first
     * parameter is equivalent to the second parameter. This method is currently only used for testing purposes.
     * 
     * @param str
     *            Any number of s-expressions
     * @param expected
     *            The expected result of evaluating the expressions given as
     *            _str_. _expected_ is only checked against the value of the
     *            final s-expression. All others are evaluated for side effects
     *            only. Note that _expected_ itself is *not* evaluated.
     * @return True if the expected value and the result of the evaluation are
     *         equal.
     * @throws ParseError
     */
    public boolean validateResult(String str, String expected) throws ParseError {
        List<SExpr> expressions = this.yale.getParser().parse(str);
        SExpr expectedValue = this.yale.getParser().parse(expected).get(0); // expected should contain only one expression
        for (int i = 0; i < expressions.size() - 1; ++i) {
            expressions.get(i).eval(this.yale.getRootEnvironment());
        }
        // check value of last expression
        SExpr finalValue = expressions.get(expressions.size() - 1).eval(this.yale.getRootEnvironment());
        return finalValue.equals(expectedValue);
    }
    
    @Test
    public void test001() throws ParseError {
        assertTrue(this.validateResult("(+ 1 -2)", "-1"));
    }
    
    @Test
    public void test002() throws ParseError {
        assertTrue(this.validateResult("(+ 1 (- 2 3) (* 2 4))", "8"));
    }
    
    @Test
    public void test003() throws ParseError {
        assertTrue(this.validateResult("(/ 16 2 2 2)", "2"));
    }
    
    @Test
    public void test004() throws ParseError {
        assertTrue(this.validateResult("(* 6 (+ 2 (* 3 (+ 4 (* 5 6667)))))", "600114"));
    }
    
    @Test
    public void test005() throws ParseError {
        assertTrue(this.validateResult("(*)", "1"));
    }
    
    @Test
    public void test006() throws ParseError {
        assertTrue(this.validateResult("(/)", "1"));
    }
    
    @Test
    public void test007() throws ParseError {
        assertTrue(this.validateResult("(+)", "0"));
    }
    
    @Test
    public void test008() throws ParseError {
        assertTrue(this.validateResult("(> 1 2)", "nil"));
    }
    
    @Test
    public void test009() throws ParseError {
        assertTrue(this.validateResult("(> 5555 5555)", "nil"));
    }
    
    @Test
    public void test010() throws ParseError {
        assertTrue(this.validateResult("(> 222 221)", "t"));
    }
    
    @Test
    public void test011() throws ParseError {
        assertTrue(this.validateResult("(>= 7777 7777)", "t"));
    }
    
    @Test
    public void test012() throws ParseError {
        assertTrue(this.validateResult("(<= 7777 7777)", "t"));
    }
    
    @Test
    public void test013() throws ParseError {
        assertTrue(this.validateResult("(< 7777 7777)", "nil"));
    }
    
    @Test
    public void test014() throws ParseError {
        assertTrue(this.validateResult("(< 7776 7777)", "t"));
    }
    
    @Test
    public void test015() throws ParseError {
        assertTrue(this.validateResult("(eql 7776 7777)", "nil"));
    }

    @Test
    public void test016() throws ParseError {
        assertTrue(this.validateResult("(eql 7777 7777)", "t"));
    }
    
    @Test
    public void test017() throws ParseError {
        assertTrue(this.validateResult("(eql 'a 'a)", "t"));
    }
    
    @Test
    public void test018() throws ParseError {
        this.yale.feed("(define x 'a)");
        this.yale.feed("(define y (car (cons 'a (cons 'b nil))))");
        assertTrue(this.validateResult("(eql x y)", "t"));
    }
    
    @Test
    public void test019() throws ParseError {
        this.yale.feed("(define x 'a)");
        this.yale.feed("(define y (car (cdr (cons 'a (cons 'b (cons 'c nil))))))");
        assertTrue(this.validateResult("(eql x y)", "nil"));
    }
    
    @Test
    public void test020() throws ParseError {
        assertTrue(this.validateResult("(car '(a b))", "a"));
    }
    
    @Test
    public void test021() throws ParseError {
        assertTrue(this.validateResult("(car (cdr '(a b)))", "b"));
    }
    
    @Test
    public void test022() throws ParseError {
        assertTrue(this.validateResult("(tree-equal (cons 'a (cons (cons 'b (cons 'c nil)) (cons 'd nil))) '(a (b c) d))", "t"));
    }
    
    @Test
    public void test023() throws ParseError {
        this.yale.feed("(define x 'a)");
        this.yale.feed("(define y 'b)");
        assertTrue(this.validateResult("(tree-equal '(a b) (cons x (cons y nil)))", "t"));
    }
    
    @Test
    public void test024() throws ParseError {
        this.yale.feed("(define x (cons 'a (cons 'b nil)))");
        assertTrue(this.validateResult("(car x)", "a"));
    }
    
    @Test
    public void test025() throws ParseError {
        this.yale.feed("(define x (cons 'a (cons 'b nil)))");
        assertTrue(this.validateResult("(tree-equal (cdr x) '(b))", "t"));
    }
    
    @Test
    public void test0026() throws ParseError {
        assertTrue(this.validateResult("(tree-equal '(a '(b c) d) '(a (quote (b c)) d))", "t"));
    }
    
    @Test
    public void test0027() throws ParseError {
        this.yale.feed("(define x 'a)");
        this.yale.feed("(define y x)");
        assertTrue(this.validateResult("(eql x y)", "t"));
    }
    
    @Test
    public void test0028() throws ParseError {
        assertTrue(this.validateResult("(not nil)", "t"));
    }
    
    @Test
    public void test0029() throws ParseError {
        assertTrue(this.validateResult("(not t)", "nil"));
    }
    
    @Test
    public void test0030() throws ParseError {
        assertTrue(this.validateResult("(not 'a)", "nil"));
    }
    
    @Test
    public void test0031() throws ParseError {
        this.yale.feed("(define x 9)");
        assertTrue(this.validateResult("(not x)", "nil"));
    }
    
    @Test
    public void test0032() throws ParseError {
        assertTrue(this.validateResult("(define x 8)", "x"));
    }
    
    @Test
    public void test0033() throws ParseError {
        assertTrue(this.validateResult("(not (cdr '(x)))", "t"));
    }
    
    @Test
    public void test0034() throws ParseError {
        assertTrue(this.validateResult("(if nil 'a (car '(x)))", "x"));
    }
    
    @Test
    public void test0035() throws ParseError {
        this.yale.feed("(define z '(a b))");
        assertTrue(this.validateResult("(if (cdr (cdr z)) (/ 1 0) (- 7 1 1))", "5"));
    }
    
    @Test
    public void test0036() throws ParseError {
        assertTrue(this.validateResult("(not ''abc)", "nil"));
    }
    
    @Test
    public void test0037() throws ParseError {
        assertTrue(this.validateResult("(tree-equal '''''qux '(quote (quote (quote (quote qux)))))", "t"));
    }
    
    @Test
    public void test0038() throws ParseError {
        assertTrue(this.validateResult("(car ''(one two))", "quote"));
    }
    
    @Test
    public void test0039() throws ParseError {
        this.yale.feed("(define var (if (> 1 2) 'three 'four))");
        assertTrue(this.validateResult("var", "four"));
    }
    
    @Test
    public void test0040() throws ParseError {
        assertTrue(this.validateResult("t", "t"));
    }
    
    @Test
    public void test0041() throws ParseError {
        assertTrue(this.validateResult("nil", "nil"));
    }
    
    @Test
    public void test0042() throws ParseError {
        assertTrue(this.validateResult("(eql nil ())", "t"));
    }
    
    @Test
    public void test0043() throws ParseError {
        assertTrue(this.validateResult("(eql '(one two) '(one two))", "nil"));
    }
    
    @Test
    public void test0044() throws ParseError {
    	assertTrue(this.validateResult("(define x 4) (assign x 3) x", "3"));
    }
    
    @Test
    public void test0045() throws ParseError {
    	this.yale.feed("(define x 7)");
    	this.yale.feed("(assign x 2)");
    	this.yale.feed("(assign x 'qux)");
    	assertTrue(this.validateResult("x", "qux"));
    }
    
    @Test
    public void test0046() throws ParseError {
    	this.yale.feed("(define x 7)");
    	this.yale.feed("(assign x 2)");
    	assertTrue(this.validateResult("(assign x 'y)", "x"));
    }
    
    @Test
    public void test0047() throws ParseError {
    	assertTrue(this.validateResult("(nullp nil)", "t"));
    }
    
    @Test
    public void test0048() throws ParseError {
    	assertTrue(this.validateResult("(eql '() ())", "t"));
    }
    
    @Test
    public void test0049() throws ParseError {
    	assertTrue(this.validateResult("(nullp +)", "nil"));
    }
    
    @Test
    public void test0050() throws ParseError {
    	this.yale.feed("(define qux '(a b c d))");
    	assertTrue(this.validateResult("(nullp qux)", "nil"));
    }
    
    @Test
    public void test0051() throws ParseError {
    	this.yale.feed("(define qux '(a b c d))");
    	assertTrue(this.validateResult("(nullp (cdr (cdr (cdr (cdr qux)))))", "t"));
    }
    
    @Test
    public void test0052() throws ParseError {
    	assertTrue(this.validateResult("(and nil nil)", "nil"));
    }

    @Test
    public void test0053() throws ParseError {
    	assertTrue(this.validateResult("(and nil t)", "nil"));
    }
    
    @Test
    public void test0054() throws ParseError {
    	assertTrue(this.validateResult("(and t nil)", "nil"));
    }
    
    @Test
    public void test0055() throws ParseError {
    	assertTrue(this.validateResult("(and t t)", "t"));
    }
    
    @Test
    public void test0056() throws ParseError {
    	String or1 = "(or nil nil)";
    	String or2 = "(or t nil)";
    	String or3 = "(or nil t)";
    	String or4 = "(or t t)";
    	assertTrue(this.validateResult(String.format("(and (and (not %s) %s) (and %s %s))", or1, or2, or3, or4), "t"));
    }
    
    @Test
    public void test0057() throws ParseError {
    	String c1 = "(consp '(a b))";
    	String c2 = "(consp (cons 'a nil))";
    	String c3 = "(define c3 '(a b))";
    	String c4 = "(not (consp (cdr (cdr c3))))";
    	String c5 = "(not (consp 'qux))";
    	assertTrue(this.validateResult(String.format("%s (and (and %s %s) (and %s %s))", c3, c1, c2, c4, c5), "t"));
    }
    
    @Test
    public void test0058() throws ParseError {
    	String c1 = "(not (atomp '(a b)))";
    	String c2 = "(not (atomp (cons 'a nil)))";
    	String c3 = "(define c3 '(a b))";
    	String c4 = "(atomp (cdr (cdr c3)))";
    	String c5 = "(atomp 'qux)";
    	assertTrue(this.validateResult(String.format("%s (and (and %s %s) (and %s %s))", c3, c1, c2, c4, c5), "t"));
    }
    
    @Test
    public void test0059() throws ParseError {
    	String c1 = "(numberp -7)";
    	String c2 = "(numberp 2.123)";
    	String c3 = "(not (numberp 'x))";
    	String c4 = "(not (numberp ()))";
    	String c5 = "(not (numberp '(a b c)))";
    	String c6 = "(not (numberp xyz))";
    	this.yale.feed("(define xyz '(1))");
    	assertTrue(this.validateResult(String.format("(and (and %s %s) (and (and %s %s) (and %s %s)))", c1, c2, c3, c4, c5, c6), "t"));
    }
    
    @Test
    public void test0060() throws ParseError {
    	assertTrue(this.validateResult("(let ((x 1) (y 2)) (+ x y))", "3"));
    }
    
    @Test
    public void test0061() throws ParseError {
    	assertTrue(this.validateResult("(let ())", "nil"));
    }
    
    @Test
    public void test0062() throws ParseError {
    	this.yale.feed("(let ((qux 'abc)) (define xyz qux))");
    	assertTrue(this.validateResult("xyz", "abc"));
    }
    
    @Test
    public void test0063() throws ParseError {
    	this.yale.feed("(define x 1)");
    	String c1 = "(let ((x 2)) (assign x 3) x)";
    	String c2 = "x";
    	assertTrue(this.validateResult(String.format("(and (eql %s 3) (eql %s 1))", c1, c2), "t"));
    }
    
    @Test
    public void test0064() throws ParseError {
        this.yale.feed("(define f (lambda (x) (+ x 1)))");
        assertTrue(this.validateResult("(and (eql (f 4) 5) (eql (f -1) 0))", "t"));
    }
    
    @Test
    public void test0065() throws ParseError {
        this.yale.feed("(define double (lambda (x) (+ x x)))");
        this.yale.feed("(define quadruple (lambda (x) (double (double x))))");
        assertTrue(this.validateResult("(and (eql (quadruple 8) 32) (eql (quadruple 1.1) 4.4))", "t"));
    }
    
    @Test
    public void test0066() throws ParseError {
        this.yale.feed("(define evenp (lambda (x) (if (eql x 0) t (oddp (- x 1)))))");
        this.yale.feed("(define oddp (lambda (x) (if (eql x 0) nil (evenp (- x 1)))))");
        assertTrue(this.validateResult("(and (oddp 15) (evenp 18))", "t"));
    }
    
    @Test
    public void test0067() throws ParseError {
        this.yale.feed("(define acc-gen (lambda () (let ((acc 0)) (lambda (x) (assign acc (+ acc x)) acc))))");
        this.yale.feed("(define acc1 (acc-gen))");
        this.yale.feed("(define acc2 (acc-gen))");
        this.yale.feed("(acc1 5)");
        this.yale.feed("(acc2 7)");
        this.yale.feed("(acc1 3)");
        assertTrue(this.validateResult("(and (eql (acc1 1) 9) (eql (acc2 1) 8))", "t"));
    }
    
    @Test
    public void test0068() throws ParseError {
        assertTrue(this.validateResult("(reduce + 0 (map length '((a b c d) (d e f) (g h i j k))))", "12"));
    }
    
    @Test
    public void test0069() throws ParseError {
        assertTrue(this.validateResult("(tree-equal (reverse '(qux quux)) '(quux qux))", "t"));
    }
    
    @Test
    public void test0070() throws ParseError {
        assertTrue(this.validateResult("(length (append (range 1 5) (range 1 3)))", "8"));
    }
    
    @Test
    public void test0071() throws ParseError {
        assertTrue(this.validateResult("(tree-equal (append '(qux quux) '(1 2 3)) '(qux quux 1 2 3))", "t"));
    }
    
    @Test
    public void test0072() throws ParseError {
        this.yale.feed("(define f (lambda (x) 1))");
        assertTrue(this.validateResult("(let ((f (lambda (n) (if (eql n 0) 1 (* n (f (- n 1))))))) (f 7))", "7"));
    }
    
    @Test
    public void test0073() throws ParseError {
        this.yale.feed("(define f (lambda (x) 1))");
        assertTrue(this.validateResult("(letrec ((f (lambda (n) (if (eql n 0) 1 (* n (f (- n 1))))))) (f 7))", "5040"));
    }
    
    @Test
    public void test0074() throws ParseError {
        assertTrue(this.validateResult("(and (eql (mod 17 4) 1) (eql (mod 4 8) 4))", "t"));
    }
    
    @Before
    public void setUp() throws IOException, URISyntaxException, ParseError {
        this.yale = new Yale();
    }
}
