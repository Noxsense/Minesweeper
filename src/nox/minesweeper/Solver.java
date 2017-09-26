package nox.minesweeper;

import java.util.Collection;
import java.util.ArrayList;


/**
 * Class Solver.
 * Should be able to solve different fields (height,width, mines).
 */
public enum Solver
{
	INSTANCE;

	private final static Field.State OPEN   = Field.State.OPEN;
	private final static Field.State CLOSED = Field.State.CLOSED;
	private final static Field.State MARKED = Field.State.MARKED;


	/**
	 * Class Todo.
	 * List of indeces and what to do with them.
	 * Indices are for a certain Field.
	 */
	private class Todo
	{
		public  final  Field  field;

		private int[]         indices = new int[8];
		private Field.State[] nxtStep = new Field.State[8];
		private int           length  = 0;

		public Todo(Field field)
		{
			this.field = field;
		}

		public void set(int[] all, Field.State s )
		{
			for (int index: all)
			{
				this.set(index,s);
			}
		}

		public void set(int i, Field.State s)
		{
			if (indices.length <= length) // extend.
			{
				int[]         tmp  = new int[length+8];
				Field.State[] tmpS = new Field.State[length+8];
				System.arraycopy(indices, 0, tmp, 0, indices.length);
				System.arraycopy(nxtStep, 0, tmpS, 0, indices.length);

				this.indices = tmp;
				this.nxtStep = tmpS;
			}

			for (int l=0; l<indices.length; l++)
			{
				if (indices[l] == i) // already knwon.
				{
					nxtStep[l] = s;
					if (length == 0) // because i was 0
					{
						length ++;
					}
					return;
				}
			}
			indices[length] = i;
			nxtStep[length] = s;
			length ++;
		}

		public Field.State[] states()
		{
			Field.State[] tmp = new Field.State[length];
			System.arraycopy(nxtStep,0, tmp,0, length);
			return tmp;
		}

		public int[] indices()
		{
			int[] tmp = new int[length];
			System.arraycopy(indices,0, tmp,0, length);
			return tmp;
		}

		public int length()
		{
			return length;
		}

		@Override public String toString()
		{
			String s = "";
			for (int i=0; i<length; i++) s += " ["+indices[i]+": "+nxtStep[i]+"]";
			return "Solver.Todo:"+s;
		}
	}


	/**
	 * Class PositionInfo.
	 * Initatate with PositionInfo.about(Field,Index).
	 * Collection information about a certain Position in a certain field.
	 */
	private static class PositionInfo
	{
		public final Field         field;
		public final int           position;
		public final Field.State   state;

		public final int           stillNeeded;
		//neighbours
		public final int[] open;
		public final int[] marked;
		public final int[] closed;

		private PositionInfo(Field field, int p, int n, int[] os, int[] ms, int[] cs)
		{
			this.field       = field;
			this.position    = p;
			this.stillNeeded = n;

			this.closed      = cs;
			this.marked      = ms;
			this.open        = os;

			this.state       = field.getState(this.position);
		}


		/**
		 * Initiate/Get PositionInfo about a Position in a certain field.
		 * @param f 
		 * @param pos 
		 * @return 
		 * @throws ArrayIndexOutOfBoundsException 
		 * @throws NullPointerException 
		 */
		public static PositionInfo about(Field f, int pos) throws ArrayIndexOutOfBoundsException,NullPointerException
		{
			int[]       ns, cs, os, ms, tmp;  // neighbours, decided, closed
			int         csLen, osLen, msLen;
			int         needed, marked;
			Field.State nState;

			needed = f.onPosition(pos);

			ns = f.getNeighbours(pos);
			cs = new int[ns.length];
			os = new int[ns.length];
			ms = new int[ns.length];

			csLen  = 0;
			osLen  = 0;
			msLen  = 0;

			/*Count marked, search for closed neighbours.*/
			for (int n : ns)
			{
				switch (f.getState(n))
				{
					case MARKED:
						needed  -= 1; // one less needed.
						ms[msLen] = n;
						msLen ++;
						continue;

					case OPEN: 
						os[osLen] = n;
						osLen ++;
						continue;

					case CLOSED:
						cs[csLen] = n;
						csLen++;
						continue;
				}
			}

			/*Avoid wrong sorted neighbours.*/
			if (ns.length != csLen + osLen + msLen)
			{
				throw new ArrayIndexOutOfBoundsException("Neighbours are wrong sorted.");
			}

			// seize os array.
			ns = new int[osLen];
			System.arraycopy(os, 0, ns, 0, osLen);
			os = ns;

			// seize ms array.
			ns = new int[msLen];
			System.arraycopy(ms, 0, ns, 0, msLen);
			ms = ns;

			// seize cs array.
			ns = new int[csLen];
			System.arraycopy(cs, 0, ns, 0, csLen);
			cs = ns;

			return new PositionInfo(f, pos, needed, os, ms, cs);
		}


		/**
		 * Check if that PositionInfo shares some closed with this.
		 * @param that other PositionInfo.
		 * @param all
		 * @return true, if at least one (or all) is the same.
		 */
		public boolean sharesClosed(PositionInfo that, boolean all)
		{
			if (that==null)
				return false;

			for (int c : this.closed)
			{
				boolean match = false;

				for (int c_ : that.closed)
				{
					if (c==c_)
					{
						if (!all) return true;
						match = true;
					}
				}
				if (all && !match) return false;
			}

			return all; // if not all: then the first matching had returned true.
		}


		/**
		 * Get all closed positions which are not shared between that and this.
		 * @param that 
		 * @return 
		 */
		public int[] unsharedClosed(PositionInfo that)
		{
			if (that == null)
				return this.closed;

			return unsharedClosed(that.closed);
		}


		/**
		 * Get all closed positions which are not shared between this and the given positions.
		 * @param closed other positions 
		 * @return 
		 */
		public int[] unsharedClosed(int[] closed)
		{
			if (closed.length == 0)
				return this.closed;

			int   len;
			int[] tmp, unshared;

			len = 0;
			tmp = new int[this.closed.length];

			for (int c : this.closed)
			{
				boolean shared = false;
				for (int c_ : closed)
				{
					if (c==c_)
					{
						shared = true;
						break;
					}
				}
				
				if (shared) continue;
				tmp[len] = c;
				len ++;
			}

			unshared = new int[len];
			System.arraycopy(tmp, 0, unshared, 0, len);

			return unshared;
		}


		/**
		 * Check if the position is satisfied, if the given marks would exist.
		 * @param  marks
		 * @return number of satisfied positions.
		 */
		public int satisfaction(int[] marks)
		{
			if (marks==null || marks.length < 1 || this.state != Solver.OPEN)
				return 0;

			int satisfaction = 0;

			for (int mark : marks)
				for (int closed : this.closed)
					satisfaction += (mark==closed) ? 1 : 0;

			return satisfaction;
		}


		@Override public String toString()
		{
			return " Pos: "+position
				+  " \""+stillNeeded+"\""
				+  " Closed: "+java.util.Arrays.toString(closed)
				+  " ("+closed.length+")"
				+  " Open: "+java.util.Arrays.toString(open)
				+  " Marked: "+java.util.Arrays.toString(marked)
				+  " Field: "+field
				;
		}

		@Override public boolean equals(Object o)
		{
			return o!=null && o instanceof PositionInfo
				&& o.toString().equals(this.toString());
		}

		@Override public int hashCode()
		{
			return this.toString().hashCode();
		}
	}


	/**
	 * Try to parse a Solver readable Field.
	 * This is to try to make the Solver also useable outside this package.
	 * @param string with Field information.
	 * @return a nox.minesweeper.Field
	 */
	private Field parseFieldInfo(String string) throws NullPointerException
	{
		return null;
	}


	/**
	 * Print the field scoping the given Index and it's neighbours.
	 * @param field 
	 * @param i 
	 * @param level
	 * @return 
	 */
	public String printNear(Field field, int index, int level)
	{
		try
		{
			int      r,c;
			String[] lines = field.print().split("\\n");
			String   str = "";

			r = index / field.getWidth();
			c = index % field.getWidth();

			for (int i=r-level; i<=r+level && i<lines.length; i++)
			{
				if (i<0) continue;
				int len   = lines[i].length();
				int start = (c-level <  0)   ? 0     : c-level;
				int end   = (c+level >= len) ? len-1 : c+level;
				str      += lines[i].substring(start,end)+"\n";
			}

			return str;
		}
		catch (NullPointerException e)
		{
			return "";
		}
	}


	/**
	 * Get the indices of the positions which are unsatisfied, but open.
	 * @param field 
	 * @return 
	 */
	public int[] possibleUnsatisfied(Field field)
	{
		if (field==null)
			return new int[0];

		int[] open, unsatisfied, ns;
		int   mines = 1;
		int   marks = 0;
		int   len   = 0;
		open        = field.getWithState(OPEN, 1);
		unsatisfied = new int[open.length];

		for (int o: open)
		{
			marks = 0;
			mines = field.onPosition(o);
			ns    = field.getNeighbours(o);

			for (int n : ns)
			{
				marks += (field.getState(n) == MARKED) ? 1 : 0;
			}

			if (mines <= marks) // as satisfied marked.
				continue;

			unsatisfied[len] = o;
			len++;
		}

		open = new int[len];
		System.arraycopy(unsatisfied, 0, open, 0, len);
		return open;
	}

	/**
	 * Check if the Position is statisfied (all mines are marked).
	 * Proxy for checkPosition(field,index,false).
	 * @param field 
	 * @param index 
	 */
	public void checkPosition(Field field, int index) throws NullPointerException, ArrayIndexOutOfBoundsException
	{
		this.checkPosition(field, index, false);
	}

	/**
	 * Check if the Position is statisfied (all mines are marked).
	 * @param field 
	 * @param index 
	 * @param withTry will also check with combinatoric moves.
	 */
	public void checkPosition(Field field, int index, boolean withTry) throws NullPointerException, ArrayIndexOutOfBoundsException
	{
		if (field.isWon() || field.isLost())
			return;

		/*Invalid index.*/
		if (field.getState(index) != OPEN)
			return;

		PositionInfo pos   = PositionInfo.about(field, index);
		Todo         todo  = new Todo(field);
		Field.State  state = CLOSED;

		todo = this.recognizePattern(pos);

		//todo.set(pos.closed, state);

		if (state == CLOSED && withTry)
			this.checkWithCombinatorics(pos, todo);

		this.nextStep(todo);
	}


	/**
	 * Try with some combinatoric moves.
	 * @param pos 
	 */
	private void checkWithCombinatorics(PositionInfo pos, Todo todo)
	{
		if (pos==null)
			return;

		PositionInfo[] info; // information from linked neighbours.
		int[] cs, ns;
		int[] possCnt;
		int   needed;
		int   csLen; // reduced array length: really valuable positions

		cs            = new int[pos.closed.length];
		possCnt       = new int[cs.length];
		needed        = pos.stillNeeded;
		info          = new PositionInfo[cs.length];
		csLen         = 0;

		for (int i=cs.length-1; 0<=i; i--) // select usable closed neighbours
		{
			PositionInfo tmp = PositionInfo.about(pos.field, pos.closed[i]);

			if (tmp.open.length<2) // unusable 
			{
				continue;
			}

			info[csLen] = tmp;
			cs[csLen]   = pos.closed[i];
			csLen++;
		}

		if (csLen<1) // no information to evaluate.
		{
			System.out.println("Sorry! :(");
			todo.set(pos.closed, CLOSED);
			this.nextStep(todo);
			return;
		}

		Collection<PositionInfo> openUnsatisfied;
		int[][]  marks    = nChooseK(csLen, pos.stillNeeded);
		double[] approval = new double[csLen];

		openUnsatisfied  = new ArrayList<PositionInfo>();
		openUnsatisfied.add(pos);

		for (int i=0; i<csLen; i++) // search for open positions near the undecided
		{
			for (int d : info[i].open)
			{
				PositionInfo dInfo = PositionInfo.about(pos.field, d);

				if (dInfo.closed.length<1)
					continue;

				if (openUnsatisfied.contains(dInfo))
					continue;

				openUnsatisfied.add(dInfo);
			}
		}

		for (int i=0; i<marks.length; i++)
		{
			/*Actual tupel instead of indices.*/
			int[] version = new int[pos.stillNeeded];

			for (int x=0; x<version.length; x++)
				version[x] = cs[marks[i][x]];

			System.out.print("Try with "+java.util.Arrays.toString(marks[i]));
			System.out.print(String.format(" == %"+((1+pos.stillNeeded)*3)+"s  ",java.util.Arrays.toString(version)));

			/*Check if unsatisfied neighbour is a bit satisfied with this.*/
			for (PositionInfo open : openUnsatisfied)
			{
				int satisfaction = open.satisfaction(version);
				double value     = 0;
				
				System.out.print(String.format("(%d ~ %d/\"%d\")", open.position,satisfaction,open.stillNeeded));
				

				if (satisfaction == open.stillNeeded)
					value = 1;
				else if (satisfaction >= open.stillNeeded) // to much!
					value = -1;
				else if (satisfaction <= open.stillNeeded) // not enough
					value = 1.*satisfaction/open.stillNeeded;

				for (int x=0; x<version.length; x++) // add values.
				{
					approval[marks[i][x]] += value;
				}
			}
			System.out.println();
		}

		int[]  notApproved    = new int[csLen];
		int    notApprovedLen = 0;
		double min            = Double.POSITIVE_INFINITY;

		for (int i=0; i<csLen; i++)
		{
			System.out.println("Position "+cs[i]+ " MARK approval: "+approval[i]);
			if (min < approval[i])
				continue;

			/*New min or extend.*/
			boolean newMin = Math.round(approval[i]*100)<Math.round(min*100);
			notApprovedLen = (newMin) ? 0 : notApprovedLen;
			min            = (newMin) ? approval[i] : min;

			notApproved[notApprovedLen] = cs[i];
			notApprovedLen++;
		}
		System.out.println();

		/*Canditates to OPEN found: Some. but not all are minimal:
		 *Check if there will be any chance to be satisfied somewhere else.
		 */
		if (0<notApprovedLen && notApprovedLen < csLen)
		{
			boolean ok  = true;
			int[]   tmp = new int[notApprovedLen];
			System.arraycopy(notApproved, 0, tmp, 0, notApprovedLen);

			/*Check if the unsatisfied have other chances to be satisfied.*/
			for (PositionInfo open : openUnsatisfied)
			{
				if (open.unsharedClosed(tmp).length <= 0)
				{
					ok = false;
					break;
				}
			}

			if (ok)
			{
				todo.set(tmp, OPEN);
			}
		}
	}


	/**
	 * Choose in the for the given number n k indices to compose.
	 * @param n 
	 * @param k 
	 * @return array with all different possibilities to choose k numbers from 0 to n-1
	 */
	private static int[][] nChooseK(int n, int k)
	{
		if (8<n || n<k || k<=0)
			return new int[0][0];

		int foo=1, bar=1;

		for (int i=1; i<=k; i++)
		{
			foo *= (n+1-i);
			bar *= i;
		}

		foo /= bar;

		int[][] chosen = new int[foo][k];
		int[]   max    = new int[k];
		int     last   = k-1;

		for (int version=0; version<foo; version++)
		{
			boolean overflow = false;

			for (int i=last; 0<=i; i--) // last to first
			{
				if (version==0) // first row: fill with actual Numbers
				{
					chosen[version][i] = i;
					max[i]             = n -(1 + last-i); // init max
					continue;
				}

				overflow = overflow || i==last;

				int val  = chosen[version-1][i] + ((overflow) ? 1 : 0);
				overflow = max[i] < val;

				if (overflow && 0<i)
				{
					int val_;
					val_ = chosen[version-1][i-1]+2;
					val  = (val_ < max[i]) ? val_ : max[i];
				}
				chosen[version][i] = val;
			}
		}

		return chosen;
	}


	/**
	 * Try to recognize a pattern and handle it.
	 * @param field 
	 * @param index 
	 * @return a Todo what can be done.
	 */
	private Todo recognizePattern(PositionInfo pos) throws NullPointerException
	{
		Todo todo = new Todo(pos.field);

		int[] open   = pos.open;
		int[] closed = pos.closed;

		/*All are mines.*/
		if (pos.stillNeeded==pos.closed.length)
		{
			todo.set(closed, MARKED);
			return todo;
		}

		/*No Mines.*/
		if (pos.stillNeeded == 0)
		{
			todo.set(closed, OPEN);
			return todo;
		}
		int length;
		PositionInfo[] opInfo; // neighbours which are also linked to closed.
		opInfo = new PositionInfo[open.length];
		length = 0;

		for (int o : open) // search for neighbours which share closed
		{
			PositionInfo tmp = PositionInfo.about(pos.field, o);
			if (tmp.sharesClosed(pos, false))
			{
				opInfo[length] = tmp;
				length++;
			}
		}

		for (int i=0; i<length; i++)
		{
			/*Pos doen't share all closed neighbours with another open.*/
			if (!pos.sharesClosed(opInfo[i], true))
				continue;

			int[] unshared = opInfo[i].unsharedClosed(pos);

			/*Needing the same: The share their mines in shared closed.
			 *Not shared are save.*/
			if (opInfo[i].stillNeeded == pos.stillNeeded)
			{
				System.out.println("Shared Mines: ");
				System.out.println(" > "+pos);
				System.out.println(" > "+opInfo[i]);
				todo.set(unshared, OPEN);
				continue;
			}

			/*Pos can satisfy neighbour with it's needed mines.
			 *The unshared postions are matching with the still needed mines.*/
			if (opInfo[i].stillNeeded == unshared.length + pos.stillNeeded)
			{
				System.out.println("Left closed are Mines:");
				System.out.println(" > "+pos);
				System.out.println(" > "+opInfo[i]);
				System.out.println(" > Mark those: "+java.util.Arrays.toString(unshared));
				todo.set(unshared, MARKED);
			}
		}

		if (true)
			return todo;

		switch (pos.stillNeeded)
		{
			case 7: break;
			case 6: break;
			case 5: break;
			case 4: break;
			case 3: break;
			case 2:
			{
				if (length<3)
				{
					for (int i=0; i<length; i++)
					{
						if (1 < opInfo[i].stillNeeded)
							continue;

						int[] unshared = pos.unsharedClosed(opInfo[i]);
						if (unshared.length==1)
						{
							System.out.println("Pattern: 12@");
							todo.set(unshared[0], MARKED);
						}
					}
				}
				break;
			}
			case 1:
			{
				if (pos.closed.length==2)
				{
					/* The others will be satisfied by one
					 * of the two closed of this.
					 */
					for (int i=0; i<length; i++)
					{
						if (!pos.sharesClosed(opInfo[i], true))
							continue;

						if (opInfo[i].stillNeeded == 4)
						{
							todo.set(opInfo[i].unsharedClosed(pos), MARKED);
						}
					}
					todo.set(pos.closed, CLOSED); // set own closed as closed
				}
				break;
			}
			default: break;
		}

		return todo;
	}


	/**
	 * Follow the instructions of the todo in the given field.
	 * @param field
	 * @param todo
	 */
	private void nextStep(Todo todo)
	{
		if (todo==null)
			return;

		Field f = todo.field;

		for (int i=0; i<todo.length(); i++)
		{
			switch(todo.nxtStep[i])
			{
				case CLOSED: break; // nothing to do.
				case MARKED: f.toggleMark(todo.indices[i]); break;
				case OPEN:   f.open(todo.indices[i]);       break;
			}
		}
	}


	/**
	 * Get a "random" number until max.
	 * @param max maximum, won't ever be reached.
	 * @return random number in [0,max[ as int.
	 */
	protected static int random(int max)
	{
		return (int) (Math.random()*max);
	}
}
