package nox.minesweeper.android;


import android.os.Parcelable;
import android.os.Parcel;
import nox.minesweeper.logic.Field;


/**
 * Class Game.
 * Contains a size fixed field and fixed number of mines.
 */
class Game //implements Parcelable
{
	public final static boolean START_TIME  = true;
	public final static boolean PLAYED_TIME = false;

	private final static String SEP0 = "\t";
	private final static String SEP1 = ",";

	public final Field field;
	public final int   mines;

	private boolean    paused;
	private long       time;
	private Statistic  stats;
	private int        opened;


	/**
	 * Class NotStartedException.
	 * An exception which may be thrown if there are some
	 * accesses but the game has not started yet.
	 */
	public static class NotStartedException extends NullPointerException
	{
		public NotStartedException(Game game)
		{
			super("Game \""+game+"\" hasn't started yet.");
		}
	}


	/**
	 * Initate a new Game with the given attributes.
	 * @param width 
	 * @param height 
	 * @param mines 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public Game(int height, int width, int mines) throws ArrayIndexOutOfBoundsException
	{
		this.field = new Field(height, width);

		if (field.size() <= mines)
			throw new ArrayIndexOutOfBoundsException("Too many mines");

		this.mines = mines;
		this.stats = new Statistic(this);

		this.opened = 0;
		this.time   = -1;
		this.paused = true;
	}


	public final static Parcelable.Creator<Game> CREATOR = new Parcelable.Creator<Game>()
	{
		@Override
		public Game createFromParcel(Parcel in)
		{
			return Game.parseGame(in.readString());
		}


		@Override
		public Game[] newArray(int size)
		{
			return new Game[size];
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
		/*Dimensions.*/
		out.writeString(this.printAll());
		//out.writeInt(this.field.getHeight());
		//out.writeInt(this.field.getWidth());
		//out.writeInt(this.mines);

		//[>Positions' status.<]
		//for (int i=0; i<this.field.size(); i++);
	}


	/**
	 * Recreate the Game from the String.
	 * Format: height|width|mines|mines|opened|marked|stats
	 * @param str String with information about the Game.
	 * @return game with parsed attribtues
	 * @throws NullPointerException if the string is null.
	 * @throws NumberFormatException if the numbers are invalid.
	 * @throws ArrayIndexOutOfBoundsException if the parsed indices don't fit.
	 */
	protected static Game parseGame(String string) throws NullPointerException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		String[] str, indices;
		str = string.split(SEP0, 8);

		int height = Integer.parseInt(str[0]);
		int width  = Integer.parseInt(str[1]);
		int mines  = Integer.parseInt(str[2]);

		Game parsed = new Game(height, width, mines);

		/*Section for known mines.*/
		if (0<str[3].length())
		{
			indices  = str[3].split(SEP1); // mine
			int[] is = new int[indices.length];
			for (int i=0; i<is.length; i++) // translate string array to int array.
			{
				is[i] = Integer.parseInt(indices[i]);
			}
			parsed.field.fillMines(is);
		}

		/*Section for opened mines.*/
		if (0<str[4].length())
		{
			indices = str[4].split(SEP1); // open
			for (String pos : indices)
			{
				parsed.field.open(Integer.parseInt(pos));
			}
			parsed.opened = indices.length;
		}

		/*Section for marked mines.*/
		if (0<str[5].length())
		{
			indices = str[5].split(SEP1); // marked
			for (String pos : indices)
			{
				parsed.field.toggleMark(Integer.parseInt(pos));
			}
		}

		/*Currently played time.*/
		parsed.time = Long.parseLong(str[6]); // played time
		parsed.paused = true;

		/*Statistics*/
		parsed.stats.parseValues(str[7].replaceAll(SEP1, " "));

		return parsed;
	}


	/**
	 * Get a String representation of the current state.
	 * Format: height|width|mines|mines|opened|marked|stats
	 * @return this game with all mines, marks and open as String.
	 */
	protected String printAll()
	{
		/*Base information.*/
		String s = String.format("%d"+SEP0+"%d"+SEP0+"%d"+SEP0
				,this.field.getHeight()
				,this.field.getWidth()
				,this.mines);

		/*Position information.*/
		int[] indices;

		indices = this.field.getMineIndices(); // mines
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = this.field.getWithState(Field.State.OPEN, 0); // opened
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = this.field.getWithState(Field.State.MARKED, 0); // marked
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		/*Currently played time.*/
		s += (this.time<0) ? this.time : this.getTime(Game.PLAYED_TIME);
		s += SEP0;

		/*Statistics*/
		s += this.stats.toString().replaceAll("\\s+",SEP1);

		return s;
	}


	/**
	 * Check if the Game is currently paused.
	 * The game is running and paused!
	 * @return true, if the game is currently running and paused.
	 */
	public boolean isPaused()
	{
		return this.isRunning() && this.paused;
	}


	/**
	 * Check if the Game is currently running.
	 * Running: First move is done and field is neighter won nor lost.
	 * @return true, if the game is currently running.
	 */
	public boolean isRunning()
	{
		return this.discovered()>0
			&& !this.field.isLost()
			&& !this.field.isWon();
	}


	/**
	 * Get the count of opened and marked mines.
	 * @return count as int.
	 */
	public int discovered()
	{
		return this.opened + this.field.getMarked();
	}


	/**
	 * Proxy and handler for this.field.open(index).
	 * @param index  which should be opened.
	 * @return ALL opened indices.
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public int[] open(int index) throws ArrayIndexOutOfBoundsException
	{
		/*Nothing to do.*/
		if (this.field.isLost() || this.field.isWon())
		{
			return new int[0];
		}

		/*Fill field except just clicked index.*/
		if (this.field.getMines() < this.mines)
		{
			this.field.fillRandomly(this.mines, index);
		}

		long startTime = this.setTimeStart();

		int[] indices = this.field.open(index);

		/*Handle end of game & stores played time in time.*/
		if (this.field.isLost() || this.field.isWon())
		{
			this.time = Game.now() - startTime; // played time
			this.handleEndGame();
		}

		indices     = this.field.getWithState(Field.State.OPEN, 0);
		this.opened = indices.length;

		return indices;
	}


	/**
	 * Handle the end of the game.
	 */
	private void handleEndGame() throws NotStartedException
	{
		if (this.field.isWon())
			this.stats.addWon(this.getTime(Game.PLAYED_TIME));

		if (this.field.isLost())
			this.reveal();
	}


	/**
	 * Empties the field and start a new party.
	 * Only possible, if there is no current game.
	 */
	public void restart()
	{
		/*Nothing to do: Game is not finished yet.*/
		if (!(this.field.isLost() || this.field.isWon()))
		{
			return;
		}

		this.field.fillMines(new int[0]); // fill with no mine == clear
		this.time = -1;
	}


	/**
	 * Set the Game as Lost and open all mines.
	 * If the game hasn't started yet: Nothing opened, accidently marked..
	 * it's not lost.
	 */
	public void reveal()
	{
		/*Not started yet: Nothing to do.*/
		if (this.discovered()<1)
		{
			return;
		}

		/*There are no mines, but it's already marked.*/
		if (this.field.getMines()<1 && 0<this.field.getMarked())
		{
			this.field.fillMines(new int[0]);
			return;
		}

		for (int i : this.field.reveal())
		{
			this.field.open(i);
		}
		this.stats.addLost();
	}


	/**
	 * Shortcut for this.field.toggleMark(index).
	 * @param index index which should be marked (or not).
	 * @return true, if the index is marked, false if just closed.
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public boolean toggleMark(int index) throws ArrayIndexOutOfBoundsException
	{
		/*First move: Initate the game.*/
		this.setTimeStart();

		return this.field.toggleMark(index);
	}


	/**
	 * Start the game now.
	 * If the game is already running, do nothing.
	 * @return the start time.
	 */
	private long setTimeStart()
	{
		/*Game hasn't started yet: Initate time*/
		if (!this.isRunning() || this.time<0)
		{
			this.time = Game.now();
		}

		/*Game is started, but currently paused: Resume.*/
		else if (this.isPaused())
		{
			/*Get Start time by currntly played time.*/
			this.resume();
		}

		this.paused = false;
		return this.getTime(Game.START_TIME);
	}


	/**
	 * Pause the game.
	 * Save played time and set to pause.
	 */
	public void pause()
	{
		this.time   = (this.time<0) ? this.time : this.getTime(Game.PLAYED_TIME);
		this.paused = true;
	}


	/**
	 * If this.isPaused() resume the current game.
	 * Else do nothing.
	 */
	public void resume()
	{
		if (!this.isPaused())
		{
			return;
		}
		
		/*Restore new start time.*/
		this.time   = this.getTime(START_TIME);
		this.paused = false;
	}


	/**
	 * Get time of the currently played game on this field.
	 * @param start if true: Request Start time; false Request: Played Time.
	 * @return time as long.
	 * @throws NotStartedException if start is requested but game hasn't started yet.
	 */
	public long getTime(boolean start) throws NotStartedException
	{
		if (start) // REQUEST: Start time
		{
			if (time<0) // not started yet: Exception
			{
				throw new NotStartedException(this);
			}

			/*On Pause or end: Time stores played time.*/
			return (this.isPaused() || !this.isRunning())
				? Game.now() - this.time
				: this.time;
		}
		else // REQUEST: Played Time
		{
			return (this.time<0)
				? 0
				/*On Pause or end: Time stores played time.*/
				: ((this.isPaused() || !this.isRunning())
						? this.time
						: Game.now() - this.time
				  );
		}
	}


	/**
	 * Uniform the current time.
	 * Avoid using different time sources.
	 * @return current time as long.
	 */
	public static long now()
	{
		return System.currentTimeMillis();
	}


	/**
	 * Get the statistics for this game.
	 * @return stats as Statistic, where changes have no effect on this stats.
	 */
	public Statistic getStatistics()
	{
		return new Statistic(this.stats);
	}


	/**
	 * Reset the statistics for this game.
	 * This will set all counters to default.
	 */
	public void resetStatistics()
	{
		this.stats.reset();
	}


	@Override
	public String toString()
	{
		return String.format("Game %d:%d with %d mines"
				,this.field.getHeight()
				,this.field.getWidth()
				,this.mines);
	}


	@Override
	public int hashCode()
	{
		return (this.getClass().getName()+this.toString()).hashCode();
	}


	@Override
	public boolean equals(Object o)
	{
		return o!=null && o instanceof Game && this.equals((Game) o);
	}


	/**
	 * Compare this dimensions with other dimensions.
	 * @param g 
	 * @return 
	 */
	public boolean equals(Game g)
	{
		return g!=null && this.equals(
				g.field.getHeight(),
				g.field.getWidth(),
				g.mines);
	}


	/**
	 * Compare with dimensions.
	 * @param height 
	 * @param width 
	 * @param mines 
	 * @return 
	 */
	public boolean equals(int height, int width, int mines)
	{
		return this.field.getHeight()==height
			&& this.field.getWidth()==width
			&& this.mines == mines;
	}
}
