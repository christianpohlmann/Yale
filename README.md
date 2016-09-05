# yale
## About
yale (yet another lisp evaluator) is a simple Lisp interpreter written
in Java. Its functionality is rather limited:
* symbols
* numbers
* cons pairs
* first-class functions
* first-class special forms

`if`, `lambda` and all other special forms are first-class objects and
can be passed as function parameters. 

yale was written for educational purposes in order to better understand Lisp systems.

## Requirements
* Java 8 (Java 7 will probably work, too)
* Apache Ant (to build yale easily)

## Installation
Clone this repository and run `ant deploy`. This will create a runnable `yale.jar`.

Alternatively, you can download a pre-built jar [here](http://nconc.de/yale/).

yale can be run in two different ways:

1. Interactively by invoking the jar file without any parameters:
`java -jar yale.jar`. This will start a REPL.  Please note that the
REPL does not yet support input that spreads over multiple lines.

2. Running code stored in a file: `java -jar yale.jar <filename>`.

## Examples

### Factorial
```
(define factorial
  (lambda (n)
    (reduce * 1 (range 1 n))))
```

`(factorial 5)` => `120`

### Accumulator generator
```
(define acc-gen
  (lambda (x)
    (lambda (y)
      (assign x (+ x y)) x)))
```

### Second Project Euler task
This is also included in the *examples* directory. The following code
sums all fibonacci numbers which are smaller or equal to 4000000 and
are even.

```
(define evenp
  (lambda (n)
    (eql (mod n 2) 0)))

(define fibonacci-sequence
  (lambda ()
    (let ((f1 0)
	  (f2 1))
      (lambda ()
	(let ((tmp f1))
	  (assign f1 f2)
	  (assign f2 (+ f2 tmp)))
	f2))))

(define euler2
  (lambda ()
    (let ((fib-seq (fibonacci-sequence)))
      (letrec ((loop
		(lambda (acc)
		  (let ((curr (fib-seq)))
		    (if (> curr 4000000) acc
		      (if (evenp curr)
			  (loop (+ acc curr))
			(loop acc)))))))
	(loop 0)))))

(let ((e (euler2)))
  (println e))
```

To run this code, type: `java -jar yale.jar examples/euler2.yl`

For more examples, have a look at the implementation of the
standard library in _resources/stdlib.yl_.

## Functionality
### Core
yale's core include the following functions, special forms and symbols:
`quote`, `lambda`, `let`, `letrec`, `if`, `define`, `assign` (like `set!` in Scheme), `cons`,
`car`, `cdr`, `t`, `not`, `eql`, `nullp`, `consp`, `atomp`, `numberp`, `+`, `-`, `*`, `/`, `>`, `mod`,
`exit`, `print`, `println`, `read`

### Standard library
yale's standard library is written in yale itself and defines the following functions:
`and`, `or`, `>=`, `<=`, `<`, `length`, `append`, `reverse`, `map`, `reduce`, `range`, `tree-equal` (equality for cons pairs)

## Todo
* Introduce macros
* Introduce strings
* Intern symbols only once by using a global symbol table (i.e. symbols with the same name should always refer to identical objects)
* Allow multi-line input from REPL.
* Exit gracefully when detecting CTRL+D inside the REPL.
