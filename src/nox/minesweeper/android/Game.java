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
	public final Field field;
	public final int   mines;

	private long       time;
	private Statistic  stats;

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
	 * @param str String with information about the Game.
	 * @return game with parsed attribtues
	 * @throws NullPointerException if the string is null.
	 * @throws NumberFormatException if the numbers are invalid.
	 * @throws ArrayIndexOutOfBoundsException if the parsed indices don't fit.
	 */
	protected static Game parseGame(String string) throws NullPointerException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		String[] str, indices;
		str = string.split("\\s+", 7);

		int height = Integer.parseInt(str[0]);
		int width  = Integer.parseInt(str[1]);
		int mines  = Integer.parseInt(str[2]);

		Game parsed = new Game(height, width, mines);

		indices  = str[3].split(","); // mine
		int[] is = new int[indices.length];
		for (int i=0; i<is.length; i++)
		{
			is[i] = Integer.parseInt(indices[i]);
		}
		parsed.field.fillMines(is);

		indices = str[4].split(","); // open
		for (String indexS : indices)
		{
			parsed.field.open(Integer.parseInt(indexS));
		}

		indices = str[5].split(","); // marked
		for (String indexS : indices)
		{
			parsed.field.toggleMark(Integer.parseInt(indexS));
		}

		/*Statistics*/
		parsed.stats.parseValues(str[6].replaceAll(",", " "));

		return parsed;
	}


	/**
	 * Get a String representation of the current state.
	 * @return this game with all mines, marks and open as String.
	 */
	protected String printAll()
	{
		/*Base information.*/
		String s;
		s  = this.field.getHeight()+" "+this.field.getWidth()+" "+this.mines;
		s += " ";


		/*Position information.*/
		int[] indices;

		indices = this.field.getMineIndices(); // mines
		for (int i=0; i<indices.length; i++)
		{
			s += i+ ((i<indices.length-1) ? "," : "");
		}
		s += " ";

		indices = this.field.getWithState(Field.State.OPEN, 0); // opened
		for (int i=0; i<indices.length; i++)
		{
			s += i+ ((i<indices.length-1) ? "," : "");
		}
		s += " ";

		indices = this.field.getWithState(Field.State.MARKED, 0); // marked
		for (int i=0; i<indices.length; i++)
		{
			s += i+ ((i<indices.length-1) ? "," : "");
		}
		s += " ";

		/*Statistics*/
		s += this.stats.toString().replaceAll("\\s+",",");

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
		return "Game"
			+ " "+ this.field.getHeight()
			+ " "+ this.field.getWidth()
			+ " "+ this.mines
			+ (this.isRunning()?" Running":"")
			;
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
