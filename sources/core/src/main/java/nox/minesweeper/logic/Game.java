package nox.minesweeper.logic;



/**
 * Class Game.
 * Contains a size fixed field and fixed number of mines.
 */
public class Game
{
	public final static boolean START_TIME  = true;
	public final static boolean PLAYED_TIME = false;

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
	 * @param width initiate with Field.width.
	 * @param height initiate with Field.height.
	 * @param mines initial mines of the Game.
	 * @throws ArrayIndexOutOfBoundsException if there are too many (cannot be filled).
	 */
	public Game(int height, int width, int mines) throws ArrayIndexOutOfBoundsException
	{
		this(new Field(height,width), mines);
	}


	/**
	 * Initate a new Game with the given attributes.
	 * @param field Base field of the Game.
	 * @param mines initial mines of the Game.
	 * @throws ArrayIndexOutOfBoundsException if there are too many (cannot be filled).
	 */
	public Game(Field field, int mines) throws NullPointerException, ArrayIndexOutOfBoundsException
	{
		this.field = field;

		if (field.size() <= mines)
			throw new ArrayIndexOutOfBoundsException("Too many mines");

		this.mines = mines;
		this.stats = new Statistic(this);

		this.restart();
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
	 * @throws ArrayIndexOutOfBoundsException  thrown by field.open()
	 */
	public int[] open(int index) throws ArrayIndexOutOfBoundsException
	{
		/*Nothing to do.*/
		if (this.field.isLost() || this.field.isWon())
		{
			return new int[0];
		}

		/*First Move: Fill field except just clicked index.*/
		if (this.field.getMines()<this.mines)
		//if (this.discovered() < 1 && this.field.getMines()<this.mines)
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
		/*Game is not finished yet: Try to reveal mines and restart.*/
		if (this.isRunning())
		{
			this.reveal();
		}

		this.field.fillMines(new int[0]); // fill with no mine == clear
		this.opened = 0;
		this.time   = -1;
		this.paused = true;
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

		this.field.reveal();
		this.stats.addLost();
	}


	/**
	 * Shortcut for this.field.toggleMark(index).
	 * @param index index which should be marked (or not).
	 * @return true, if the index is marked, false if just closed.
	 * @throws ArrayIndexOutOfBoundsException  thrown by field.toggleMark()
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
	 * If this.isPaused() resume the current game with the played time.
	 * Else do nothing.
	 * @param playedTime time the game was played until it was paused, do nothing if smaller than 0.
	 */
	public void resumeWith(long playedTime)
	{
		if (!this.isPaused() || playedTime < 0)
		{
			return;
		}
		
		/*Restore new start time.*/
		this.time   = Game.now() - playedTime;
		this.paused = false;
	}


	/**
	 * If this.isPaused() resume the current game.
	 * Else do nothing.
	 */
	public void resume()
	{
		this.resumeWith(this.getTime(PLAYED_TIME));
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


	/**
	 * Load statistic from the given statistic.
	 * @param statistic other statistic to load information from.
	 */
	public void loadStatisticFrom(Statistic statistic)
	{
		this.stats.loadFrom(statistic);
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
	 * @param g other Game to compare with.
	 * @return true, if height, width and mines are equal.
	 */
	public boolean equals(Game g)
	{
		return g!=null && this.hasAttributes(
				g.field.getHeight(),
				g.field.getWidth(),
				g.mines);
	}


	/**
	 * Compare with dimensions.
	 * @param height check, if Game.height == height.
	 * @param width check, if Game.width == width.
	 * @param mines check, if Game.mines == mines.
	 * @return true, if the attributes are equal.
	 */
	public boolean hasAttributes(int height, int width, int mines)
	{
		return this.field.getHeight()==height
			&& this.field.getWidth()==width
			&& this.mines == mines;
	}
}
