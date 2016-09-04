/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import yale.eval.Environment;
import yale.eval.SExpr;
import yale.exception.ParseError;
import yale.parse.Parser;

/**
 * Main class which implements a REPL and allows the user to execute expressions
 * from a file.
 *
 */
public class Yale {

    private static final String PROMPT = "[%d]> ";
    /**
     * The stdandard library is written in Yale itself and provides some useful
     * functions.
     */
    private static final String STDLIB_LOCATION = "/stdlib.yl";

    private int exprCnt;
    private Kernel kernel;
    private BufferedReader reader;

    public Yale() throws IOException, URISyntaxException, ParseError {
        this.exprCnt = 1;
        kernel = new Kernel();
        reader = new BufferedReader(new InputStreamReader(System.in));
        initStandardLibrary();
    }

    private void printPrompt() {
        System.out.print(String.format(Yale.PROMPT, this.exprCnt));
        System.out.flush();
        this.exprCnt++;
    }

    public void feed(SExpr expr) {
        expr.eval(this.kernel.getRootEnvironment());
    }

    public void feed(String str) throws ParseError {
        List<SExpr> expressions = this.kernel.getParser().parse(str);
        for (SExpr expr : expressions) {
            expr.eval(this.kernel.getRootEnvironment());
        }
    }

    private List<SExpr> read() throws IOException, ParseError {
        String expression = reader.readLine();
        return this.kernel.getParser().parse(expression);
    }

    /**
     * Needed by unit tests.
     * 
     * @return The parser used in this yale instance.
     */
    public Parser getParser() {
        return this.kernel.getParser();
    }

    /**
     * Needed by unit tests.
     * 
     * @return Root environment of the kernel
     */
    public Environment getRootEnvironment() {
        return this.kernel.getRootEnvironment();
    }

    public void repl() throws IOException {
        while (true) {
            printPrompt();
            List<SExpr> parseResults;
            try {
                parseResults = read();
                for (SExpr res : parseResults) {
                    try {
                        System.out.println(res.eval(this.kernel.getRootEnvironment()));
                    } catch (RuntimeException e) {
                        System.err.println("Runtime exception: " + e.getMessage());
                        System.err.flush();
                    }
                }
            } catch (ParseError e) {
                System.err.println("Parse error: " + e.getMessage());
            }
        }
    }

    public void runFile(String filename) throws IOException, ParseError {
        String code = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
        // execute every expression from the file
        for (SExpr expr : this.kernel.getParser().parse(code)) {
            expr.eval(this.kernel.getRootEnvironment());
        }
        System.exit(0);
    }

    private void initStandardLibrary() throws IOException, URISyntaxException, ParseError {
        InputStream is = Yale.class.getResourceAsStream(STDLIB_LOCATION);
        /* The famous "stupid scanner trick" */
        Scanner scanner = new Scanner(is, "UTF-8");
        scanner.useDelimiter("\\A");
        String str = scanner.next();
        scanner.close();
        feed(str);
    }

    private static void printIntro() {
        /*
         * The following attribute (Implementation-Version) is unset if running
         * Yale from anything else than an ANT-built jar. In that case it won't
         * be printed.
         */
        String versionNumber = Yale.class.getPackage().getImplementationVersion();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("This is yale");
        if (versionNumber != null) {
            stringBuffer.append(" " + versionNumber);
        }
        stringBuffer.append(" (yet another lisp evaluator)");
        System.out.println(stringBuffer);
    }

    public static void main(String[] args) throws IOException, ParseError, URISyntaxException {
        if (args.length != 0 && args.length != 2) {
            printIntro();
            System.out.println("Usage:");
            System.out.println("\t<>: Start interactive REPL");
            System.out.println("\t<filename>: Run specified file");
            System.exit(1);
        }
        Yale yale = new Yale();
        if (args.length == 0) {
            printIntro();
            yale.repl();
        } else {
            yale.runFile(args[0]);
        }
    }
}
