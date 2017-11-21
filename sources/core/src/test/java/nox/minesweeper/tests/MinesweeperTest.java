package nox.minesweeper.tests;

import static org.junit.Assert.assertTrue;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;

/**
 * Class Test.
 * Run unit test to proove the logic methods and classes.
 */
public class MinesweeperTest
{
	public static void main(String[] args)
	{
		Result result = JUnitCore.runClasses(
				FieldTests.class
				, GameStatsTests.class
				);
      for (Failure failure : result.getFailures())
	  {
         System.out.printf("---\n - %s\n", failure.toString());
      }
      System.out.printf(
      		"-----\nRuntime:\t%d ms\nResult:\t\t%s\n-----\n",
      		result.getRunTime(), result.wasSuccessful());
	}


	protected static void assertMax(String msg, long max, long value)
	{
		MinesweeperTest.assertMax(msg, max, value, true);
	}

	protected static void assertMax(String msg, long max, long value, boolean inclusive)
	{
		msg = msg +": "+max+" is maximum "
			+ ((inclusive) ? "(inclusive)" : "(exclusive)")
			+ ", got <"+value+">"
			;

		assertTrue(msg, (inclusive) && value<=max || value<max);
	}


	protected static void assertMin(String msg, long min, long value)
	{
		MinesweeperTest.assertMin(msg, min, value, true);
	}

	protected static void assertMin(String msg, long min, long value, boolean inclusive)
	{
		msg = msg +": "+min+" is minimum "
			+ ((inclusive) ? "(inclusive)" : "(exclusive)")
			+ ", got <"+value+">"
			;

		assertTrue(msg, (inclusive) && min<=value || min<value);
	}


	protected static void assertInRange(String msg, long min, long max, long value)
	{
		MinesweeperTest.assertMin(msg, min, max, value, true);
	}

	protected static void assertMin(String msg, long min, long max, long value, boolean endInclusive)
	{
		String f = (endInclusive) ? ": [%d,%d]" : ": [%d,%d[";

		msg = msg + String.format(f+", but <%d>", min, max, value);

		assertTrue(msg, min<=value
				&& ((endInclusive) && value <= max || value < max));
	}
}
