package nox.minesweeper;

class ParsedField extends Field
{
	private static String sepeator = " ";

	private final static int VALUE_CLOSED = Integer.MIN_VALUE;

	private int[]   valueParsed;
	private State[] stateParsed;
	private boolean lostParsed;

	private ParsedField(int height, int width)
	{
		super(height,width);
		this.valueParsed = new int[this.size()];
		this.stateParsed      = new State[this.size()];
		this.lostParsed       = false;
	}


	/**
	 * Parse a new Field from given String.
	 * @param str string with information.
	 * @return a new Field from the given information.
	 * @throws NullPointerException 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public static ParsedField parseField(String str) throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException
	{
		String[] s = str.split(sepeator);

		int  width, height, mines, size;
		char zero, closed, mine, marked;
		ParsedField field;

		width  = Integer.parseInt(s[0]);
		height = Integer.parseInt(s[1]);
		mines  = Integer.parseInt(s[2]);

		field = new ParsedField(width, height);
		field.parseState(s[4]);

		return field;
	}


	/**
	 * Parse the current state from the string for this field.
	 * @param str 
	 * @throws NullPointerException 
	 * @throws ArrayIndexOutOfBoundsException 
	 */
	public void parseState(String str) throws NullPointerException, ArrayIndexOutOfBoundsException
	{
		if (str.length()!=this.size())
		{
			throw new ArrayIndexOutOfBoundsException("Given indices and contained indeces are not equal.");
		}

		for (int i=0; i<this.size(); i++)
		{
			this.setPosition(i, str.charAt(i));
		}
	}


	/**
	 * Fill position information with data.
	 * @param index Index
	 * @param data may contain: Open|Closed|Marked 
	 * @throws ArrayIndexOutOfBoundsException if index is out ouf boundaries.
	 */
	protected void setPosition(int index, char data) throws ArrayIndexOutOfBoundsException
	{
		//TODO (A) 2017-10-09  @implement
		int value = -1;
		State state = Field.State.CLOSED;

		if (data == Field.getDisplay(Field.DISPLAY_MARKED))
		{
			value = Field.VALUE_MARKED;
			state = State.MARKED;
		}
		else if (data == Field.getDisplay(Field.DISPLAY_CLOSED))
		{
			value = VALUE_CLOSED;
		}
		else if (data==Field.getDisplay(DISPLAY_MINE))
		{
			this.lostParsed = true;
			this.valueParsed[index] = Integer.MAX_VALUE;
			state = State.OPEN;
		}
		else if (data==Field.getDisplay(DISPLAY_ZERO))
		{
			value = 0;
			state = State.OPEN;
		}
		else // parse mines count.
		{
			value = (int) data - 49;
			state = State.OPEN;
		}
		this.valueParsed[index] = value;
		this.stateParsed[index]      = state;
	}


	@Override
	public State getState(int index) throws ArrayIndexOutOfBoundsException
	{
		return this.stateParsed[index];
	}


	@Override
	public int onPosition(int index) throws ArrayIndexOutOfBoundsException
	{
		return this.valueParsed[index];
	}


	@Override
	public boolean isLost()
	{
		return this.lostParsed;
	}


	@Override
	public int[] open(int index) throws ArrayIndexOutOfBoundsException
	{
		System.out.println("REQUEST: Open on '"+index+"'.");
		//return super.open(index);
		return new int[0];
	}


	@Override
	public boolean toggleMark(int index) throws ArrayIndexOutOfBoundsException
	{
		if (this.stateParsed[index] == State.CLOSED)
		{
			this.stateParsed[index] = State.MARKED;
			this.valueParsed[index] = Field.VALUE_MARKED;
			return true;
		}
		else if (this.stateParsed[index] == State.MARKED)
		{
			this.stateParsed[index] = State.CLOSED;
			this.valueParsed[index] = Field.VALUE_CLOSED;
		}
		return false;
	}
}
