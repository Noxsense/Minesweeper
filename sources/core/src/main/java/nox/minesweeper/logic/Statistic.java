package nox.minesweeper.logic;



/**
 * Class Statistic.
 */
public class Statistic
{
	public final static int BEST_TIME     = 0;
	public final static int AVERAGE_TIME  = 1;

	private int  gameHeight, gameWidth, gameMines;

	private int  gamesWon;
	private int  gamesLost;

	private int  streak;

	private long timeAverage;
	private long timeBest;

	/**
	 * Initate a new Statistic.
	 * For a certain game.
	 * @param game whose attributes will maintained.
	 */
	public Statistic(Game g) throws NullPointerException
	{
		this(g.field.getHeight(), g.field.getWidth(), g.mines);
	}


	/**
	 * Initate a new Statistic.
	 * For a certain game with the given dimensions.
	 * @param height of game
	 * @param width of game
	 * @param mines of game
	 */
	public Statistic(int height, int width, int mines)
	{
		this.gameHeight = height;
		this.gameWidth  = width;
		this.gameMines  = mines;

		this.reset();
	}


	/**
	 * Copy of the original.
	 * @param original 
	 */
	protected Statistic(Statistic original)
	{
		original = (original==null) ? new Statistic(0,0,0) : original;

		this.gameHeight = original.gameHeight;
		this.gameWidth  = original.gameWidth;
		this.gameMines  = original.gameMines;

		this.gamesWon    = original.gamesWon;
		this.gamesLost   = original.gamesLost;
		this.timeAverage = original.timeAverage;
		this.timeBest    = original.timeBest;
	}


	/**
	 * Add a new game as won.
	 * Recalculates also best time and average time.
	 * @param won  if true, the game is won, else lost.
	 * @param time time of won game.
	 */
	public void addWon(boolean won, long time)
	{
		/*Lost game: Lose streak, add lost game. Return.*/
		if (!won)
		{
			this.gamesLost += 1;
			this.streak     = 0;
			return;
		}

		/*Won game: Calculates new average, best and add to streak.*/
		long sum         = this.timeAverage*this.gamesWon;
		this.timeAverage = (sum+time)/(this.gamesWon+=1);

		//this.sum      += time; // what if time too long?
		//this.gamesWon += 1;
		//this.timeAverage = Math.round(sum / this.gamesWon);


		this.timeBest = (time<this.timeBest) ? time : this.timeBest;
		this.streak += 1;
	}
	//private long sum = 0;


	/**
	 * Add a new game as won.
	 * Recalculates also best time and average time.
	 * @param time time of won game.
	 */
	public void addWon(long time)
	{
		this.addWon(true, time);
	}


	/**
	 * Add a new game as lost.
	 */
	public void addLost()
	{
		this.addWon(false, 0);
	}


	/**
	 * Get the requested time in milli seconds.
	 * @param which type of time.
	 * @return time in milli seconds as long.
	 */
	public long getTime(int which)
	{
		if (this.gamesWon<1)
		{
			return -1;
		}

		switch(which)
		{
			case AVERAGE_TIME: return this.timeAverage;
			case BEST_TIME: return this.timeBest;
			default: return -1;
		}
	}


	/**
	 * Get a count of all won games (or lost games).
	 * @param won if true, count the won games, else the lost games.
	 * @return number of won (or lost) games.
	 */
	public int countGamesWon(boolean won)
	{
		return (won) ? this.gamesWon : this.gamesLost;
	}


	/**
	 * Get streak of the statistics.
	 * @return streak as int.
	 */
	public int getStreak()
	{
		return this.streak;
	}


	/**
	 * Get the count of all games: #lost+ #won.
	 * @return #lost + #won as int.
	 */
	public int allGames()
	{
		return this.gamesLost + this.gamesWon;
	}


	/**
	 * Get the count of cells which are contained in the mathing field.
	 * @return width * height as int.
	 */
	public int cells()
	{
		return this.gameWidth*this.gameHeight;
	}


	/**
	 * Reset the statistics.
	 * This will set all counters to default.
	 */
	public void reset()
	{
		this.gamesWon    = 0;
		this.gamesLost   = 0;
		this.timeAverage = 0;
		this.timeBest    = Long.MAX_VALUE;
	}


	/**
	 * Fill own statistic values with values of other statistic, if they are matching.
	 * @param that other statistic.
	 */
	public void loadFrom(Statistic that)
	{
		if (that==null || !this.match(that))
		{
			return;
		}

		this.gamesWon    = that.gamesWon;
		this.gamesLost   = that.gamesLost;
		this.timeAverage = that.timeAverage;
		this.timeBest    = that.timeBest;
	}


	/**
	 * Check if the other statistic would match the same game like this.
	 * @param that other statistic.
	 * @return true, if the dimenesions are the same.
	 */
	public boolean match(Statistic that)
	{
		return that!=null
			&& that.gameHeight == this.gameHeight
			&& that.gameWidth  == this.gameWidth
			&& that.gameMines  == this.gameMines
			;
	}


	/**
	 * Check if this statistic matches the given game.
	 * @param game game.
	 * @return true, if the dimenesions are the same.
	 */
	public boolean match(Game game)
	{
		return game!=null
			&& game.field.getHeight() == this.gameHeight
			&& game.field.getWidth()  == this.gameWidth
			&& game.mines  == this.gameMines
			;
	}


	/**
	 * Parse value for this stats from string.
	 * @param info String which represents values for a certain game.
	 * @throws NullPointerException 
	 * @throws NumberFormatException 
	 */
	public void parseValues(String info) throws NullPointerException, NumberFormatException
	{
		String[] values = info.split("\\s+");

		try
		{
			this.gamesWon    = Integer.parseInt(values[0].trim());
			this.gamesLost   = Integer.parseInt(values[1].trim());
			this.streak      = Integer.parseInt(values[2].trim());
			this.timeAverage = Long.parseLong(values[3].trim());
			this.timeBest    = Long.parseLong(values[4].trim());
		}
		catch (NumberFormatException e)
		{
			throw new NumberFormatException("Statistic::parseValues() <= "+e.getMessage());
		}
	}


	@Override
	public String toString()
	{
		return   this.gamesWon
			+" "+this.gamesLost
			+" "+this.streak
			+" "+this.timeAverage
			+" "+this.timeBest
			;
	}
}
