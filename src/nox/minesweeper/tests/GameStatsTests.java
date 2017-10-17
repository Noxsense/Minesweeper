package nox.minesweeper.tests;


import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import static java.lang.Thread.sleep;

import nox.minesweeper.logic.*;


/**
 * Class GameStatsTests.
 * Run unit test to proove the game methods.
 *	+discovered()
 *	+getStatistics()
 *	+getTime(boolean start)
 *	+hashCode()
 *	+isPaused()
 *	+isRunning()
 *	+loadStatisticFrom(Statistic statistic)
 *	+now()
 *	+open(int index)
 *	+pause()
 *	+resetStatistics()
 *	+restart()
 *	+resume()
 *	+resumeWith(long playedTime)
 *	+reveal()
 *	+toString()
 *	+toggleMark(int index)
*/
public class GameStatsTests
{
	private int    height;
	private int    width;
	private int    mines;
	private String msg;

	private Game game;

	public GameStatsTests()
	{
		height = 7;
		width  = 8;
		mines  = 9;

		this.game = new Game(height, width, mines);
	}

	@Before
	public void resetField()
	{
		this.game.restart();
	}


	@Test
	public void testBaseGetters()
	{
		assertEquals("Width", width, this.game.field.getWidth());
		assertEquals("Height", height, this.game.field.getHeight());
		assertEquals("Mines", mines, this.game.mines);

		msg = "Check equals";
		assertEquals(msg, this.game, new Game(new Field(height,width),mines));
		assertEquals(msg, this.game, new Game(height,width,mines));
		assertNotEquals(msg, this.game, new Game(height,width,0));
		assertEquals(msg, true, this.game.hasAttributes(height,width,mines));
	}


	@Test
	public void testGameFlow()
	{
		int  index = 0;
		long seconds = 0;

		msg = "Nothing done yet.";
		assertEquals(msg, false, this.game.isRunning());

		msg = "First Move: Starts";
		game.open(index);
		assertEquals(msg+":Running", true, this.game.isRunning() || this.game.field.isWon());

		assertNotEquals(msg+":Discovered", 0, this.game.discovered());

		// try to wait.
		seconds = 5;
		try
		{
			sleep(seconds);
		}
		catch (InterruptedException e)
		{
			seconds = 0;
		}
		msg = msg+":Played Time (at least "+seconds+")";
		assertEquals(msg, true, seconds<=this.game.getTime(Game.PLAYED_TIME));
	}
}
