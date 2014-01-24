package ua.cr2csop.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ua.cr2csop.parser.ConstraintRelationParserTest;
import ua.cr2csop.util.GraphUtilTest;
import ua.cr2csop.weights.BfsWeightAssignerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({ GraphUtilTest.class, ConstraintRelationParserTest.class,
					  BfsWeightAssignerTest.class})
public class AllTests {



}
