package nox.minesweeper.android;


import android.os.Parcelable;
import android.os.Parcel;
import nox.minesweeper.logic.Field;


/**
 * Class Game.
 * Contains a size fixed field and fixed number of mines.
 */
class Game implements Parcelable
{
	private final static String SEP0 = "!";
	private final static String SEP1 = ",";

	public final Field field;
	public final int   mines;

	private long       time;
	private Statistic  stats;
	private int        opened;

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
		this.time   = 0;
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
		str = string.split(SEP0, 7);

		int height = Integer.parseInt(str[0]);
		int width  = Integer.parseInt(str[1]);
		int mines  = Integer.parseInt(str[2]);

		Game parsed = new Game(height, width, mines);

		/*Section for known mines.*/
		if (0<str[3].length())
		{
			indices  = str[3].split(SEP1); // mine
			int[] is = new int[indices.length];
			for (int i=0; i<is.length; i++)
			{
				is[i] = Integer.parseInt(indices[i]);
			}
			parsed.field.fillMines(is);
		}

		/*Section for opened mines.*/
		if (0<str[4].length())
		{
			indices = str[4].split(SEP1); // open
			for (String indexS : indices)
			{
				parsed.open(Integer.parseInt(indexS));
			}
		}

		/*Section for marked mines.*/
		if (0<str[5].length())
		{
			indices = str[5].split(SEP1); // marked
			for (String indexS : indices)
			{
				parsed.toggleMark(Integer.parseInt(indexS));
			}
		}

		/*Statistics*/
		parsed.stats.parseValues(str[6].replaceAll(SEP1, " "));

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
			s += i+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = this.field.getWithState(Field.State.OPEN, 0); // opened
		for (int i=0; i<indices.length; i++)
		{
			s += i+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = this.field.getWithState(Field.State.MARKED, 0); // marked
		for (int i=0; i<indices.length; i++)
		{
			s += i+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		/*Statistics*/
		s += this.stats.toString().replaceAll("\\s+",SEP1);

		return s;
	}


	/**
	 * Check if the Game is currently running.
	 * Running: First index is open and neighter won nor lost.
	 * @return true, if the game is currently running.
	 */
	public boolean isRunning()
	{
		return false;
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
	 * @return newly opened indices.
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public int[] open(int index) throws ArrayIndexOutOfBoundsException
	{
		/*Fill field except just clicked index.*/
		if (this.field.getMines() < this.mines)
		{
			this.field.fillRandomly(this.mines, index);
		}

		int[] indices = this.field.open(index);
		this.opened   = this.field.getWithState(Field.State.OPEN, 0).length;

		return indices;
	}


	/**
	 * Shortcut for this.field.toggleMark(index).
	 * @param index index which should be marked (or not).
	 * @return true, if the index is marked, false if just closed.
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public boolean toggleMark(int index) throws ArrayIndexOutOfBoundsException
	{
		return this.field.toggleMark(index);
	}


	/**
	 * Get the statistics for this game.
	 * @return stats as Statistic, where changes have no effect on this stats.
	 */
	public Statistic getStatistics()
	{
		return new Statistic(this.stats);
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
