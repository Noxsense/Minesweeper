package nox.minesweeper.tests;

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
				//, GameStatsTests.class
				);
      for (Failure failure : result.getFailures())
	  {
         System.out.printf("---\n - %s\n", failure.toString());
      }
      System.out.printf(
      		"-----\nRuntime:\t%d ms\nResult:\t\t%s\n-----\n",
      		result.getRunTime(), result.wasSuccessful());
	}
}
