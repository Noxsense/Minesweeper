package nox.minesweeper.logic;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * The field.
 * The heart of minesweeper.
 */
public class Field
{
	public final static int   MIN = 1; // Field should have at least one/two position.
	public final static int   DISPLAY_ZERO   = 0;
	public final static int   DISPLAY_CLOSED = 1;
	public final static int   DISPLAY_MINE   = 2;
	public final static int   DISPLAY_MARKED = 3;

	public final static int   VALUE_MINE_ON_POS = Integer.MAX_VALUE;
	public final static int   VALUE_CLOSED      = Integer.MIN_VALUE;
	public final static int   VALUE_MARKED      = 9;

	private final int  height, width;
	private final int  hashCode;

	private Position[] pos;
	private boolean    lost;
	private int[]      minesCnt; // actual #mines, #marked

	private boolean[]  mine;
	private State[]    state; // position is closed, marked or opened

	private       static char[]    display; // how to display the positions.


	/**
	 * Enum State.
	 * Indicator for a positions state (open, closed, marked).
	 */
	public enum State
	{
		OPEN, CLOSED, MARKED;
	}


	/**
	 * Class Position.
	 * Depends on the current field.
	 * Capsulated methods like searching neighbours or counting mines.
	 */
	private class Position
	{
		public final int    index;

		private Position[]  neighbours;

		private int         minesCnt; // in neighbours.


		public Position(int index)
		throws ArrayIndexOutOfBoundsException
		{
			if (index<0 || Field.this.size()<=index) // not in field.
			{
				throw new ArrayIndexOutOfBoundsException("No such index: "+index);
			}

			this.index         = index;
			this.reset();
		}


		@Override public String toString()
		{
			if (this.isOpen()) // show numbers or mine.
			{
				if (this.isMine())
				{
					return Field.display[Field.DISPLAY_MINE] +"";
				}

				return (this.minesCnt()<1)
					?  Field.display[Field.DISPLAY_ZERO] + ""
					:  this.minesCnt()            + "";
			}

			return (this.isMarked())
				?  Field.display[Field.DISPLAY_MARKED] + ""
				:  Field.display[Field.DISPLAY_CLOSED] + "";
			
		}


		/**
		 * Like to String, but only one char.
		 * @return char, representing the Position's state.
		 */
		public char toChar()
		{
			return this.toString().charAt(0);
		}


		/**
		 * Show information about this position.
		 * May be helpful for debugging.
		 * @return 
		 */
		public String show()
		{
			int max  = String.valueOf(Field.this.size()).length();
			String s = String.format("%"+(max)+"s", this.index);

			s += " "+((this.isMine()) ? "X" : this.minesCnt()) + " ";

			if   (this.isMarked()) s+= " ?";
			if   (this.isOpen())   s+= " _";

			for (Position p : neighbours()) s += " " + p.index;

			return s;
		}


		@Override public int hashCode()
		{
			return this.index;
		}


		@Override public boolean equals(Object o)
		{
			return o!=null && o instanceof Position
				&& this.index == ((Position)o).index;
		}


		/**
		 * Get the neighbours.
		 * May initiate them.
		 * @return neighbours as Position array.
		 */
		public Position[] neighbours()
		{
			if (this.neighbours==null)
			{
				Field         f     = Field.this;
				Position      p     = null;
				Position[]    ps    = new Position[8];
				int           psLen = 0;

				boolean isOuterLeft, isOuterRight, isTop, isBottom;
				isOuterLeft  = this.index % f.width == 0;
				isOuterRight = this.index % f.width == f.width-1;
				isTop        = this.index < f.width;
				isBottom     = this.index > f.size() - f.width+1;

				for (int row : new int[]{-1,0,1})
				{
					for (int col : new int[]{-1,0,1})
					{
						/*This is on edge: No neighbours above|below.*/
						if ((isTop && row<0) || (isBottom && row>0))
							continue;

						/*This is on edge: No neighbours left|right.*/
						if ((isOuterLeft && col<0) || (isOuterRight && col>0))
							continue;

						/*Own Index: Not a neighbour.*/
						if (row==0 && col==0)
							continue;

						try
						{
							p = f.get(this.index + row*f.width + col);
							ps[psLen] = p;
							psLen++;
						}
						catch (ArrayIndexOutOfBoundsException e)
						{}
						catch (NullPointerException e)
						{}
					}
				}
				this.neighbours = new Position[psLen];
				System.arraycopy(ps, 0, this.neighbours, 0, psLen);
			}
			return this.neighbours;
		}


		/**
		 * Count the mines in the neighbourhood and self.
		 * @return number of mines near this position (int).
		 */
		public int minesCnt()
		{
			if (this.minesCnt < 0) // initiate counting mines in neighbourhood
			{
				if (this.isMine()) // this is a mine: mines in "neigbourhood" is max.
				{
					this.minesCnt = Field.VALUE_MINE_ON_POS;
				}

				else // this is not a mine: count neighbours.
				{
					this.minesCnt = 0;
					for (Position n : this.neighbours())
					{
						if (n!=null && n.isMine()) this.minesCnt += 1;
					}
				}
			}
			return this.minesCnt;
		}


		/**
		 * Check if this is a mine.
		 * @return 
		 */
		public boolean isMine()
		{
			return Field.this.mine[this.index];
		}


		/**
		 * Set this positions as mine or not.
		 * @param mine if true, this position will contain a mine.
		 */
		public void setMine(boolean mine)
		{
			Field.this.mine[this.index] = mine;
		}


		/**
		 * Check if this Position is already opend.
		 * @return true if open. (marking not possible anymore)
		 */
		public boolean isOpen()
		{
			return Field.this.state[this.index] == State.OPEN;
		}


		/**
		 * Check if this Position is marked.
		 * @return if true, this is marked and can't be opend.
		 */
		public boolean isMarked()
		{
			return Field.this.state[this.index] == State.MARKED;
		}


		/**
		 * Toggle marked state.
		 * If it was marked, then unmark, else mark.
		 * Doesn't mark, if already open.
		 */
		public void toggleMark()
		{
			switch(Field.this.state[this.index])
			{
				case CLOSED: Field.this.state[this.index] = State.MARKED; break;
				case MARKED: Field.this.state[this.index] = State.CLOSED; break;
				default: break;
			}
		}


		/**
		 * Open this Position: Show it's mines count or lose game.
		 * @return true, if this wasn't a mine (opening was safe).
		 */
		public boolean open()
		{
			if (!this.isMarked() || this.isOpen())
			{
				Field.this.state[this.index] = State.OPEN;
			}
			return !this.isMine();
		}


		/**
		 * Resets the Position state.
		 * Close and unmark.
		 * Remove mine, if there was one and reset mines count.
		 */
		public void reset()
		{
			Field.this.mine[this.index]  = false;
			Field.this.state[this.index] = State.CLOSED;
			this.minesCnt  = -1;
		}
	}



	/**
	 * Initiate a new field for mines.
	 * @param height 
	 * @param width 
	 */
	public Field(int height, int width)
	throws ArrayIndexOutOfBoundsException
	{
		if (height<MIN || width<MIN)
			throw new ArrayIndexOutOfBoundsException("Invalid size.");

		this.height    = height;
		this.width     = width;
		this.hashCode  = (new Integer(width*101 + height*17)).hashCode();

		this.minesCnt  = new int[]{0,0}; // 0 minesCnt, 0 undiscovered1
		this.pos       = new Position[this.height*this.width];

		this.mine      = new boolean[this.size()];
		this.state     = new State[this.size()];

		for (int i=0; i<this.size(); i++) // initate positions
			this.pos[i] = this.get(i);

		this.lost     = false;
		Field.display = new char[] {'0', ' ', 'X', '?'};
	}


	@Override
	public String toString()
	{
		return "Field " + this.width + "x" + this.height
			+ " ("+minesCnt[1]+"/"+minesCnt[0]+" mines)"
			+ ((lost) ? " LOST" : "");
	}


	@Override
	public boolean equals(Object o)
	{
		return o!=null && o instanceof Field
			&& ((Field)o).height == this.height
			&& ((Field)o).width  == this.width
			;
	}


	@Override
	public int hashCode()
	{
		return this.hashCode;
	}


	/**
	 * Get the index for the certain row/column comination.
	 * @param row row of position.
	 * @param col column of position.
	 * @return index for position with on given row and column.
	 */
	protected int indexOf(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		if (row<0 || this.getHeight()<=row)
			throw new ArrayIndexOutOfBoundsException("Invalid row");

		if (col<0 || this.getWidth()<=col)
			throw new ArrayIndexOutOfBoundsException("Invalid column");

		int index = row*this.getWidth()+col; 
		return index;
	}


	/**
	 * Get the Position of a certain position.
	 * @param row row of position.
	 * @param col column of position.
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 * @return 
	 */
	private Position get(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.get(this.indexOf(row,col));
	}


	/**
	 * Get the Position of a certain index.
	 * If out of boundary throw an error.
	 * @param  index to get.
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range
	 * @return 
	 */
	private Position get(int index) throws ArrayIndexOutOfBoundsException
	{
		if (index<0 || this.size()<=index) // not in field.
		{
			throw new ArrayIndexOutOfBoundsException("No such index: "+index);
		}

		Position p = this.pos[index];
		if (p == null) // not initiated.
		{
			this.pos[index] = new Position(index);
		}
		return this.pos[index];
	}


	/**
	 * Toggles the mark of the given position.
	 * @param row row of position.
	 * @param col column of position.
	 * @return true, if position is marked now.
	 */
	public boolean toggleMark(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.toggleMark(this.indexOf(row,col));
	}


	/**
	 * Toggles the mark of the given position.
	 * @param p Position to open.
	 * @return true, if position is marked now.
	 */
	public boolean toggleMark(int index) throws ArrayIndexOutOfBoundsException
	{
		if (this.isLost() || this.isWon()) // no updates
		{
			return false; // not marked, because there's nothing.
		}

		Position p = this.get(index);
		p.toggleMark();

		this.minesCnt[1] = this.minesCnt[1] + ((this.pos[index].isMarked() ? 1 : -1));

		return this.pos[index].isMarked();
	}


	/**
	 * Opens all mines and set as this as lost.
	 * @return array of all mines.
	 */
	public int[] reveal()
	{
		this.lost  = true;

		int[] mines, tmp;
		int   cnt;

		tmp = new int[this.size()];
		cnt = 0;

		for (int i=0; i<mine.length; i++) // search for all mines' indices.
		{
			if (mine[i])
			{
				tmp[cnt] = i;
				cnt     += 1;
				this.get(i).open(); // force to open.
			}
		}

		mines = new int[cnt];

		System.arraycopy(tmp,0, mines, 0, cnt);
		return mines;
	}


	/**
	 * Oopen given position and maybe it's zero neighbours.
	 * Proxy for open(intex).
	 * @param row row of position.
	 * @param col column of position.
	 * @return array of newky opened positons.
	 * @throws ArrayIndexOutOfBoundsException if calculated index is not in boundaries.
	 */
	public int[] open(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.open(this.indexOf(row,col));
	}


	/**
	 * Opens given Position and maybe it's zero-neighbours.
	 * @param p Position to open.
	 * @return array of newly opened positions (indices).
	 */
	public int[] open(int index) throws ArrayIndexOutOfBoundsException
	{
		if (this.isLost() || this.isWon() || this.pos[index].isMarked()) // no updates
		{
			return new int[0];
		}

		Position p    = this.get(index);
		boolean  zero = p.minesCnt() < 1;
		this.lost     = !p.open(); // p safly opened.

		if (!zero) // finised opening.
		{
			return new int[]{index};
		}

		List<Integer>  nowOpen    = new ArrayList<Integer>();
		List<Position> neighbours = new ArrayList<Position>();

		nowOpen.add(p.index);

		for (Position n : p.neighbours()) // initial neighbours.
			neighbours.add(n);

		/*Given Index: Position without mines: Open unblocked neighbours.*/
		Set<Position> newDiscovered = new HashSet<Position>();
		while (!neighbours.isEmpty() && !this.lost)
		{
			for (Position n : neighbours)
			{
				if (n==null||n.isMarked()||n.isOpen()||nowOpen.contains(n.index))
					continue;

				this.lost = !n.open(); // open safly?
				nowOpen.add(n.index);

				if (this.lost) // Lost: Stop Opening
					break;

				if (0 < n.minesCnt()) // don't open neighbours.
					continue;

				/*Open also n's neighbours, because n is also "0".*/
				for (Position nn : n.neighbours())
				{
					if (nn==null || nn.isMarked() || nn.isOpen())
						continue;

					if (nowOpen.contains(nn.index)) // skip
						continue;

					newDiscovered.add(nn);
				}
			}
			neighbours.clear(); // remove recently watched.
			neighbours.addAll(newDiscovered);
			newDiscovered.clear();
		}

		int[] tmp = new int[nowOpen.size()];
		for (int i=0; i<nowOpen.size(); i++)
			tmp[i] = nowOpen.get(i);

		return tmp;
	}


	/**
	 * Get the state of the Position.
	 * @param row row of position.
	 * @param col column of position.
	 * @return State (open, closed, marked)
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public State getState(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.getState(this.indexOf(row,col));
	}


	/**
	 * Get the state of the Position.
	 * @param p requested Position's index.
	 * @return State (open, closed, marked)
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public State getState(int index) throws ArrayIndexOutOfBoundsException
	{
		return this.state[index];
	}


	/**
	 * Get the count of the neighbouring mines for the Position.
	 * @param row row of position.
	 * @param col column of position.
	 * @return MAX VALUE (like a mine)  if the position is not opened yet, else the count of neighbours.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public int onPosition(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.onPosition(this.indexOf(row, col));
	}


	/**
	 * Get the count of the neighbouring mines for the Position.
	 * @param p requested Position's index.
	 * @return MAX VALUE (like a mine)  if the position is not opened yet, else the count of neighbours.
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public int onPosition(int index) throws ArrayIndexOutOfBoundsException
	{
		Position p = this.pos[index];
		if (!p.isOpen())
		{
			return (p.isMarked()) ? Field.VALUE_MARKED : Field.VALUE_CLOSED;
		}
		return p.minesCnt();
	}


	/**
	 * Check if this Field has opend a mine.
	 * @return true, if mine is revealled.
	 */
 	public boolean isLost()
	{
		return this.lost;
	}
	

	/**
	 * Check if this field is won.
	 * @return true, if everything except the minesCnt are open.
	 */
	public boolean isWon()
	{
		if (this.isLost())
		{
			return false;
		}

		for (int i=0; i<this.size(); i++)
		{
			if (this.state[i] != State.OPEN && !this.mine[i])
			{
				return false;
			}
		}

		return true;
	}


	/**
	 * Clear and Fill the field with minesCnt.
	 * @param except   index, where no mine is.
	 * @param minesCnt number of new minesCnt.
	 * @throws ArrayIndexOutOfBoundsException except fails or to many mines.
	 */
	public void fillRandomly(int minesCnt, int except) throws ArrayIndexOutOfBoundsException
	{
		this.clear();

		if (this.size() <= minesCnt) // too many mines. => NullPointerException
		{
			throw new ArrayIndexOutOfBoundsException("Too many mines (max."+this.size()+")");
		}

		while (this.minesCnt[0]<minesCnt)
		{
			int index = (int) (Math.random()*this.size());
			
			if (index==except || this.get(index).isMine())
				continue;

			this.get(index).setMine(true);

			this.minesCnt[0]=this.minesCnt[0]+1;
		}
	}


	/**
	 * Clear and Fill the field with minesCnt.
	 * default: No exceptions
	 * @param minesCnt number of new minesCnt.
	 */
	public void fillRandomly(int minesCnt) throws ArrayIndexOutOfBoundsException
	{
		this.fillRandomly(minesCnt, -1);
	}


	/**
	 * Fill this field with mines.
	 * This will clear the current state and set the given indeces as mines.
	 * @param mineIndices positions for mines.
	 * @throws ArrayIndexOutOfBoundsException 
	 * @throws NullPointerException 
	 */
	public void fillMines(int[] mineIndices) throws ArrayIndexOutOfBoundsException, NullPointerException
	{
		this.clear();

		if (mineIndices.length -1 >= this.size())
		{
			throw new ArrayIndexOutOfBoundsException("Too many mines (max."+this.size()+")");
		}

		for (int mine : mineIndices)
		{
			Position pos = this.get(mine);

			if (pos.isMine())
				continue;

			pos.setMine(true);
			this.minesCnt[0] += 1;
		}
	}


	/**
	 * Clear the field, delete all minesCnt (no position with mine).
	 */
	private void clear()
	{
		for (Position p: pos)
		{
			p.reset(); // resets the position
		}
		this.minesCnt[0] = 0;
		this.minesCnt[1] = 0;
		this.lost        = false;
	}


	/**
	 * Get the count of the marked positions.
	 * @return mines count in field as int.
	 */
	public int getMarked()
	{
		return this.minesCnt[1];
	}


	/**
	 * Get the count of the mines.
	 * @return mines count in field as int.
	 */
	public int getMines()
	{
		return this.minesCnt[0];
	}


	/**
	 * Get the indices where mines are placed.
	 * @return indices as int[]
	 */
	public int[] getMineIndices()
	{
		int     len;
		int[]   tmp, requested;

		tmp = new int[this.size()];
		len = 0;

		for (int i=0; i<this.size(); i++)
		{
			if (!this.pos[i].isMine())
				continue;

			tmp[len] = i;
			len++;
		}

		requested = new int[len];
		System.arraycopy(tmp, 0, requested, 0, len);

		return requested;
	}


	/**
	 * Count (new) the positions, which have currently the given state.
	 * @param s   requested State.
	 * @param min if open, then with at least min mines in neighbourhood
	 * @return positions which are opened, -1 if game is lost.
	 */
	public int[] getWithState(State s, int min)
	{
		if (this.isLost())
			return new int[0];

		int     len;
		int[]   tmp, requested;
		boolean useMin = s == State.OPEN && 0<min; // else useless

		tmp = new int[this.size()];
		len = 0;

		for (int i=0; i<this.size(); i++)
		{
			if (this.state[i] == s && (!useMin || this.pos[i].minesCnt()>=min))
			{
				tmp[len] = i;
				len++;
			}
		}

		requested = new int[len];
		System.arraycopy(tmp, 0, requested, 0, len);

		return requested;
	}


	/**
	 * Get the neighbours for a certain position.
	 * @param row row of position.
	 * @param col column of position.
	 * @return i's neighbours' indices as int[].
	 */
	public int[] getNeighbours(int row, int col) throws ArrayIndexOutOfBoundsException
	{
		return this.getNeighbours(this.indexOf(row,col));
	}


	/**
	 * Get the neighbours for a certain position.
	 * @param index position
	 * @return i's neighbours' indices as int[].
	 */
	public int[] getNeighbours(int index) throws ArrayIndexOutOfBoundsException
	{
		Position[] ps = this.pos[index].neighbours();
		int[]      ns = new int[ps.length];
		for (int i=0; i<ps.length; i++)
		{
			ns[i] = ps[i].index;
		}
		return ns;
	}


	/**
	 * Get a copy of the positions states.
	 * @return 
	 */
	protected State[] getStates()
	{
		State[] copy = new State[this.size()];
		System.arraycopy(this.state, 0, copy, 0, this.size());
		return copy;
	}


	/**
	 * Get size as number of positions.
	 * @return size as int.
	 */
	public int size()
	{
		return this.pos.length;
	}


	/**
	 * Get height of this field.
	 * @return height as int.
	 */
	public int getHeight()
	{
		return this.height;
	}


	/**
	 * Get width of this field.
	 * @return width as int.
	 */
	public int getWidth()
	{
		return this.width;
	}


	/**
	 * Set the char which should be displayed when an open positions has no mines.
	 * @param d new char.
	 */
	public static void setDisplay0(char d)
	{
		Field.display[DISPLAY_ZERO] = d;
	}


	/**
	 * Set the char which should be displayed for a closed positions.
	 * @param d new char.
	 */
	public static void setDisplayClosed(char d)
	{
		Field.display[DISPLAY_CLOSED] = d;
	}


	/**
	 * Set the char which should be displayed for a position with mines.
	 * @param d new char.
	 */
	public static void setDisplayMine(char d)
	{
		Field.display[DISPLAY_MINE] = d;
	}


	/**
	 * Set the char which should be displayed for a marked position.
	 * @param d new char.
	 */
	public static void setDisplayMarked(char d)
	{
		Field.display[DISPLAY_MARKED] = d;
	}


	/**
	 * Get the display for the given information.
	 * @param info requested data which should be displayed (zero as default).
	 * @return display as char.
	 */
	public static char getDisplay(int info)
	{
		switch(info)
		{
			case DISPLAY_CLOSED:
			case DISPLAY_MINE:case DISPLAY_MARKED:
				return Field.display[info];
			case DISPLAY_ZERO: default:
				return Field.display[DISPLAY_ZERO];
		}
	}


	/**
	 * Show debugging information of this field.
	 * @return Field as String.
	 */
	public String showAll()
	{
		String s = "Field: "+this.width+" x "+this.height;
		for (int i=0; i<this.size(); i++)
		{
			s += "\nPosition: "+ this.pos[i].show();
		}
		
		s += "\n\n";

		for (int i=0; i<this.size(); i++)
		{
			s += String.format("%3s |", this.pos[i].index);

			if (i%this.height == this.height-1)
				s += "\n";
		}
		return s;
	}


	/**
	 * Create a new Field from the given string.
	 * height width [mines]
	 * @param str String to read.
	 * @return a new Field from the String.
	 * TODO Rewrite or delete
	 */
	public static Field parse(String str)
	throws NullPointerException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		String[] data = str.split("\\s|;");

		if (data.length < 2) throw new NullPointerException("Not enough data");

		Field f = new Field(Integer.parseInt(data[0]),Integer.parseInt(data[1]));

		for (int i=2; i<data.length; i++)
		{
			f.get(Integer.parseInt(data[i])).setMine(true);
			f.minesCnt[0] = f.minesCnt[0]+1;
		}
		return f;
	}


	/**
	 * Pretty print the field.
	 * @return a grid with the game.
	 */
	public String print()
	{
		String s = "";
		for (int i=0; i<pos.length; i++)
		{
			s += this.get(i);
			if (i%this.width == this.width-1)
			{
				s += "\n";
			}
		}
		return s;
	}


	/**
	 * Prints a String representing the current game and field information.
	 * 4 Numbers:  Width Height #Mines #Marked
	 * 4th String: Display for Zero Mines, Closed, Mine and Marked
	 * 5th String: With length: size(), representing the current situation.
	 * @return String with length == size;
	 */
	public String oneLine()
	{
		String s = "";
		for (Position p : pos) s += p.toChar();

		return String.format(
				"%d %d %d %c%c%c%c %s",
				this.width, this.height, this.minesCnt[0],
				display[DISPLAY_ZERO], display[DISPLAY_CLOSED],
				display[DISPLAY_MINE], display[DISPLAY_MARKED],
				s);
	}
}
