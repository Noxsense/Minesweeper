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
 *	+resetStatistics()
 *	+resumeWith(long playedTime)
 *	+reveal()
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
		long seconds = 0, playedTime = 0, startTime;

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
		MinesweeperTest.assertMin(msg, seconds, this.game.getTime(Game.PLAYED_TIME));

		msg = "Pause game";
		startTime  = this.game.getTime(Game.START_TIME);
		playedTime = this.game.getTime(Game.PLAYED_TIME);
		this.game.pause();
		assertEquals(msg, true, this.game.isPaused());
		assertEquals(msg, playedTime/10, this.game.getTime(Game.PLAYED_TIME)/10); // round

		// try to wait.
		seconds = 100;
		try
		{
			sleep(seconds);
		}
		catch (InterruptedException e)
		{
			seconds = 0;
		}

		msg = "Wait. Game's still paused";
		assertEquals(msg, true, this.game.isPaused());
		assertEquals(msg, playedTime/10, this.game.getTime(Game.PLAYED_TIME)/10); // round

		// try to wait.
		seconds = 1000;
		try
		{
			sleep(seconds);
		}
		catch (InterruptedException e)
		{
			seconds = 0;
		}

		msg = "Resume Game";
		this.game.resume();
		assertEquals(msg, false, this.game.isPaused());
		assertEquals(msg, playedTime/10, this.game.getTime(Game.PLAYED_TIME)/10); // round
		assertEquals(msg, true, this.game.isRunning() || this.game.field.isWon());

		msg = "New calculated start time (depends on played time)";
		assertNotEquals(msg, startTime/10, this.game.getTime(Game.START_TIME)/10); // round
	}


	@Test
	public void testStatistics()
	{
		Statistic stats;
		long time, sum, timeMin, statsAVG, statsBEST;
		double offset;
		int won;

		msg = "Own stats are matching.";
		assertEquals(msg, true, this.game.getStatistics().match(this.game));

		msg = "Matching stats are matching.";
		assertEquals(msg, true, (stats=new Statistic(this.game)).match(this.game));
		assertEquals(msg, true, (stats=new Statistic(height, width, mines)).match(this.game));

		msg = "Lost Game";
		stats.addWon(false, 0); // lost game
		assertEquals(msg, 1, stats.countGamesWon(false));

		msg = "Test calculation of Won Games";
		sum = 0;
		offset = .05;
		timeMin = Long.MAX_VALUE;

		for (won=1; won<=1000; won++)
		{
			time = Math.round(Math.random()*1000*3600*10); // until 10 min
			sum += time;
			timeMin = (time<timeMin) ? time : timeMin;
			stats.addWon(true, time); // won game

			statsAVG = stats.getTime(Statistic.AVERAGE_TIME);
			statsBEST = stats.getTime(Statistic.BEST_TIME);

			assertEquals(msg+" Lost Games", 1, stats.countGamesWon(false));
			assertEquals(msg+" New Won game", won, stats.countGamesWon(true));
			MinesweeperTest.assertInRange(
					msg+" Average time with "+won,
					Math.round(sum/won*(1-offset)),
					Math.round(sum/won*(1+offset)),
					statsAVG);
			assertEquals(msg+" Best time", timeMin, statsBEST);
		}
	}
}
