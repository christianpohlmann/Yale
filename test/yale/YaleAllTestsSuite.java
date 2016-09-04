/**
 * Copyright (C) 2016 Christian Pohlmann
 * 
 * Licensed under The MIT License (see LICENSE.md)
 */
package yale;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import yale.main.YaleTest;
import yale.parse.ParserTest;

/**
 * Container for all test classes in Yale.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ParserTest.class, YaleTest.class })
public class YaleAllTestsSuite {
}
