package nox.minesweeper.android;


import android.os.Parcelable;
import android.os.Parcel;
import nox.minesweeper.logic.Field;


/**
 * Class Statistic.
 */
class Statistic implements Parcelable
{
	public final static int BEST_TIME     = 0;
	public final static int AVERAGE_TIME  = 1;
	public final static int CELL_TIME     = 2;

	private int  gameHeight, gameWidth, gameMines;

	private int  gamesWon;
	private int  gamesLost;

	private int  streak;

	private long timeAverage;
	private long timeBest;
	private long timePerCell;

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

		this.gamesWon    = 0;
		this.gamesLost   = 0;
		this.timeAverage = 0;
		this.timeBest    = Long.MAX_VALUE;
	}


	/**
	 * Copy of the original.
	 * @param original 
	 */
	protected Statistic(Statistic original)
	{
		original = (original==null) ? new Statistic(0,0,0) : original;

		this.gamesWon    = original.gamesWon;
		this.gamesLost   = original.gamesLost;
		this.timeAverage = original.timeAverage;
		this.timeBest    = original.timeBest;
	}


	/**
	 * Initate Statistic from Parcel.
	 * @param in 
	 */
	private Statistic(Parcel in)
	{
		this.gameHeight = in.readInt();
		this.gameWidth  = in.readInt();
		this.gameMines  = in.readInt();

		this.gamesWon    = in.readInt();
		this.gamesLost   = in.readInt();
		this.timeAverage = in.readLong();
		this.timeBest    = in.readLong();
	}


	public final static Parcelable.Creator<Statistic> CREATOR = new Parcelable.Creator<Statistic>()
	{
		@Override
		public Statistic createFromParcel(Parcel in)
		{
			return new Statistic(in);
		}


		@Override
		public Statistic[] newArray(int size)
		{
			return new Statistic[size];
		}
	};


	@Override
	public int describeContents()
	{
		return 0;
	}


	@Override
	public void writeToParcel(Parcel out, int flags)
	{
		out.writeInt(this.gameHeight);
		out.writeInt(this.gameWidth);
		out.writeInt(this.gameMines);

		out.writeInt(this.gamesWon);
		out.writeInt(this.gamesLost);
		out.writeLong(this.timeAverage);
		out.writeLong(this.timeBest);
	}


	/**
	 * Add a new game as won.
	 * Recalculates also best time and average time.
	 * @param time time of won game.
	 */
	public void addWon(long time)
	{
		long sum         = this.timeAverage*this.gamesWon;
		this.timeAverage = (sum+time)/(this.gamesWon+=1);

		this.timeBest = (time<this.timeBest) ? time : this.timeBest;
		this.streak += 1;
	}


	/**
	 * Add a new game as lost.
	 */
	public void addLost()
	{
		this.gamesLost += 1;
		this.streak     = 0;
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
			case BEST_TIME: return this.timeBest;
			case CELL_TIME: return this.timePerCell;
			case AVERAGE_TIME: return this.timeAverage;
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
	 * Get the count of all games: #lost+ #won.
	 * @return #lost + #won as int.
	 */
	public int allGames()
	{
		return this.gamesLost + this.gamesWon;
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


