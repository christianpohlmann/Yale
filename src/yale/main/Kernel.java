/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.main;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import yale.eval.Cons;
import yale.eval.Environment;
import yale.eval.Function;
import yale.eval.Let;
import yale.eval.Number;
import yale.eval.Printer;
import yale.eval.SExpr;
import yale.eval.SpecialForm;
import yale.eval.Symbol;
import yale.exception.ParseError;
import yale.parse.Parser;

/**
 * This class contains definitions of the core functionality of Yale. These
 * include special forms, functions and symbols (t, nil) that could not (easily)
 * be implemented in Yale itself.
 *
 */
public class Kernel {

    public static final Symbol QUOTE = new Symbol("quote");
    public static final Symbol LAMBDA = new Symbol("lambda");
    public static final Symbol LET = new Symbol("let");
    public static final Symbol LETREC = new Symbol("letrec");
    public static final Symbol IF = new Symbol("if");
    public static final Symbol DEFINE = new Symbol("define");
    public static final Symbol ASSIGN = new Symbol("assign");

    public static final Symbol CONS = new Symbol("cons");
    public static final Symbol CAR = new Symbol("car");
    public static final Symbol CDR = new Symbol("cdr");

    public static final Symbol T = new Symbol("t");
    public static final Symbol NOT = new Symbol("not");
    public static final Symbol EQL = new Symbol("eql");
    public static final Symbol NULLP = new Symbol("nullp");
    public static final Symbol CONSP = new Symbol("consp");
    public static final Symbol ATOMP = new Symbol("atomp");
    public static final Symbol NUMBERP = new Symbol("numberp");

    public static final Symbol ADD = new Symbol("+");
    public static final Symbol SUB = new Symbol("-");
    public static final Symbol MULT = new Symbol("*");
    public static final Symbol DIV = new Symbol("/");
    public static final Symbol GT = new Symbol(">");
    public static final Symbol MOD = new Symbol("mod");

    public static final Symbol EXIT = new Symbol("exit");
    public static final Symbol PRINT = new Symbol("print");
    public static final Symbol PRINTLN = new Symbol("println");
    public static final Symbol READ = new Symbol("read");

    private Environment root;
    private Parser parser;
    private Scanner stdin;

    public Kernel() {
        this.root = new Environment();
        this.parser = new Parser();
        this.stdin = new Scanner(System.in);

        addSpecialForms();
        addConsFunctions();
        addPredicates();
        addMathFunctions();
        addIOFunctions();

        this.root.defineBinding(Symbol.NIL, Symbol.NIL);
        this.root.defineBinding(Kernel.T, Kernel.T);
    }

    public Environment getRootEnvironment() {
        return this.root;
    }

    /**
     * Helper function to verify the number of parameters passed to a special
     * form or built-in function. User-defined functions can figure this out
     * themselves (due to the parameter description list passed to a lambda
     * form).
     * 
     * @param parameters
     *            List of passed parameters
     * @param expected
     *            Expected number of parameters
     * @param atLeast
     *            If true, the parameter {@code expected} is a lower bound for
     *            the size of the parameter list. If false, the parameter list
     *            must be of the same size as parameter {@code expected}.
     * @param sym
     *            Symbol representing the special form/builtin function (needed
     *            for the exception string)
     * @throws yale.exception.RuntimeError
     *             Thrown if there is a mismatch between number of passed and
     *             expected parameters
     */
    public static void validateParameters(List<SExpr> parameters, int expected, boolean atLeast, Symbol sym) {
        if (!atLeast) {
            if (parameters.size() != expected) {
                throw new RuntimeException(String.format("%s requires %d parameter(s), %d given.", sym.getName(),
                        expected, parameters.size()));
            }
        } else {
            if (parameters.size() < expected) {
                throw new RuntimeException(String.format("%s requires at least %d parameter(s), %d given.",
                        sym.getName(), expected, parameters.size()));
            }
        }
    }

    private void addSpecialForms() {
        addLambda();
        addLet();

        this.root.defineBinding(QUOTE, new SpecialForm() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, QUOTE);
                return parameters.get(0);
            }
        });

        this.root.defineBinding(IF, new SpecialForm() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 3, false, IF);
                SExpr condition = parameters.get(0).eval(env);
                SExpr exprThen = parameters.get(1);
                SExpr exprElse = parameters.get(2);
                if (!condition.eval(env).equals(Symbol.NIL)) {
                    return exprThen.eval(env); // exprElse stays un-evaluated
                } else {
                    return exprElse.eval(env); // exprThen stays un-evaluated
                }

            }
        });

        this.root.defineBinding(DEFINE, new SpecialForm() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, DEFINE);
                SExpr sym = parameters.get(0);
                SExpr sexpr = parameters.get(1);
                if (!(sym instanceof Symbol)) {
                    throw new RuntimeException(String.format("%s is not a symbol.", sym.toString()));
                }
                // introduce root binding
                root.defineBinding((Symbol) sym, sexpr.eval(env));
                return sym;
            }
        });

        this.root.defineBinding(ASSIGN, new SpecialForm() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, ASSIGN);
                SExpr sym = parameters.get(0);
                SExpr sexpr = parameters.get(1);
                if (!(sym instanceof Symbol)) {
                    throw new RuntimeException(String.format("%s is not a symbol.", sym.toString()));
                }
                // update binding
                env.updateBinding((Symbol) sym, sexpr.eval(env));
                return sym;
            }
        });
    }

    private void addConsFunctions() {
        this.root.defineBinding(CONS, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, CONS);
                SExpr car = parameters.get(0);
                SExpr cdr = parameters.get(1);
                Cons cons = new Cons(car, cdr);
                return cons;
            }
        });

        this.root.defineBinding(CAR, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, CAR);
                SExpr cons = parameters.get(0);
                return cons.car();
            }
        });

        this.root.defineBinding(CDR, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, CDR);
                SExpr cons = parameters.get(0);
                return cons.cdr();
            }
        });
    }

    private void addPredicates() {
        this.root.defineBinding(NOT, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, NOT);
                SExpr condition = parameters.get(0);
                if (condition.equals(Symbol.NIL)) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });

        this.root.defineBinding(EQL, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, EQL);
                SExpr first = parameters.get(0);
                SExpr second = parameters.get(1);
                if (first.equals(second)) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });

        this.root.defineBinding(NULLP, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, NULLP);
                if (parameters.get(0).equals(Symbol.NIL)) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });

        this.root.defineBinding(CONSP, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, CONSP);
                if (parameters.get(0).isCons()) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });

        this.root.defineBinding(ATOMP, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, ATOMP);
                if (parameters.get(0).isAtom()) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });

        this.root.defineBinding(NUMBERP, new Function() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, false, NUMBERP);
                if (parameters.get(0).isNumber()) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });
    }

    private void addLambda() {
        this.root.defineBinding(LAMBDA, new SpecialForm() {
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 1, true, LAMBDA);
                List<Symbol> variables = new LinkedList<Symbol>();
                List<SExpr> body = new LinkedList<SExpr>();
                SExpr currCons = parameters.get(0);

                while (!currCons.equals(Symbol.NIL)) {
                    SExpr currCar = currCons.car();
                    if (!(currCar instanceof Symbol)) {
                        throw new RuntimeException(String.format("%s is not a symbol, but appears in parameter list.",
                                currCar.toString()));
                    }
                    variables.add((Symbol) currCar);
                    currCons = currCons.cdr();
                }

                for (int i = 1; i < parameters.size(); ++i) {
                    body.add(parameters.get(i));
                }
                Environment defEnv = env;
                return new Function() {
                    @Override
                    public SExpr apply(List<SExpr> parameters, Environment env) {
                        if (parameters.size() != variables.size()) {
                            throw new RuntimeException(String.format("lambda form requires %d parameter(s), %d given",
                                    variables.size(), parameters.size()));
                        }
                        /*
                         * It is important to use _defEnv_ here and NOT _env_:
                         * _defEnv_ is the environment where lambda was defined
                         * whereas _env_ is the environment where it is
                         * executed. Therefore, by using _defEnv_, bindings are
                         * resolved lexicographically whereas by using _env_
                         * they would be resolved dynamically (cf. lexical vs
                         * dynamic scoping). _env_ is therefore ignored.
                         */
                        Environment subEnv = new Environment(defEnv);
                        for (int i = 0; i < parameters.size(); ++i) {
                            subEnv.defineBinding(variables.get(i), parameters.get(i));
                        }
                        for (int i = 0; i < body.size() - 1; ++i) {
                            /*
                             * Evaluate all but the last expression of the
                             * lambda body. These are evaluated for side-effects
                             * only.
                             */
                            body.get(i).eval(subEnv);
                        }
                        /* return the value of the final expression */
                        return body.get(body.size() - 1).eval(subEnv);
                    }
                };
            }
        });
    }

    private void addLet() {
        this.root.defineBinding(LET, new Let(false, LET));
        this.root.defineBinding(LETREC, new Let(true, LETREC));
    }

    private void addMathFunctions() {
        this.root.defineBinding(ADD, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                BigDecimal sum = new BigDecimal(0);
                for (SExpr param : parameters) {
                    sum = sum.add(param.getNumericValue());
                }
                return new Number(sum);
            }
        });

        this.root.defineBinding(SUB, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                BigDecimal diff = null;
                if (parameters.size() >= 1) {
                    diff = parameters.get(0).getNumericValue();
                    parameters.remove(0); // remove first parameter since its
                                          // the start value of diff
                } else {
                    diff = new BigDecimal(0);
                }
                for (SExpr param : parameters) {
                    diff = diff.subtract(param.getNumericValue());
                }
                return new Number(diff);
            }
        });

        this.root.defineBinding(MULT, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                BigDecimal prod = new BigDecimal(1);
                for (SExpr param : parameters) {
                    prod = prod.multiply(param.getNumericValue());
                }
                return new Number(prod);
            }
        });

        this.root.defineBinding(DIV, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                BigDecimal quot = null;
                if (parameters.size() >= 1) {
                    quot = parameters.get(0).getNumericValue();
                    parameters.remove(0); // remove first parameter since its
                                          // the start value of quot
                } else {
                    quot = new BigDecimal(1);
                }
                for (SExpr param : parameters) {
                    quot = quot.divide(param.getNumericValue());
                }
                return new Number(quot);
            }
        });

        this.root.defineBinding(GT, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, GT);
                BigDecimal first = parameters.get(0).getNumericValue();
                BigDecimal second = parameters.get(1).getNumericValue();
                if (first.compareTo(second) >= 1) {
                    return Kernel.T;
                } else {
                    return Symbol.NIL;
                }
            }
        });
        
        this.root.defineBinding(MOD, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 2, false, MOD);
                BigDecimal first = parameters.get(0).getNumericValue();
                BigDecimal second = parameters.get(1).getNumericValue();
                return new Number(first.remainder(second));
            }
        });
    }

    public Parser getParser() {
        return this.parser;
    }

    private void addIOFunctions() {
        this.root.defineBinding(EXIT, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 0, false, EXIT);
                System.exit(0);
                return Symbol.NIL;
            }
        });

        this.root.defineBinding(PRINT, new Printer(false, PRINT));
        this.root.defineBinding(PRINTLN, new Printer(true, PRINTLN));

        this.root.defineBinding(READ, new Function() {
            @Override
            public SExpr apply(List<SExpr> parameters, Environment env) {
                validateParameters(parameters, 0, false, READ);
                String str = stdin.nextLine();
                try {
                    /*
                     * only parse first s-expression as there is no clean way to
                     * deal with a list of s-expressions
                     */
                    return parser.parse(str).get(0);
                } catch (ParseError e) {
                    throw new RuntimeException(e.getMessage());
                }

            }
        });
    }
}
