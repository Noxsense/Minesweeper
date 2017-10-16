package nox.minesweeper.tests;


import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.ArrayList;

import nox.minesweeper.logic.*;


/**
 * Class FieldTest.
 * Run unit test to proove the field methods.
 *
 *	+fillMines(int[] mineIndices)
 *	+fillRandomly(int minesCnt)
 *	+fillRandomly(int minesCnt, int except)
 *	+getDisplay(int info)
 *	+getHeight()
 *	+getMarked()
 *	+getMineIndices()
 *	+getMines()
 *	+getNeighbours(int index)
 *	+getNeighbours(int row, int col)
 *	+getState(int index)
 *	+getState(int row, int col)
 *	+getWidth()
 *	+getWithState(State s, int min)
 *	+isLost()
 *	+isWon()
 *	+onPosition(int index)
 *	+onPosition(int row, int col)
 *	+oneLine()
 *	+open(int index)
 *	+open(int row, int col)
 *	+parse(String str)
 *	+print()
 *	+reveal()
 *	+setDisplay0(char d)
 *	+setDisplayClosed(char d)
 *	+setDisplayMarked(char d)
 *	+setDisplayMine(char d)
 *	+showAll()
 *	+size()
 *	+toggleMark(int index)
 *	+toggleMark(int row, int col)
 *	-clear()
 *	
*/
public class FieldTests
{
	private int    height;
	private int    width;
	private int    mines;
	private String msg;

	private Field field;

	public FieldTests()
	{
		height = 7;
		width  = 8;
		mines  = 0;

		this.field = new Field(height, width);
	}

	@Before
	public void resetField()
	{
		this.field.fillRandomly(0); // reset field.
	}

	@Test
	public void testBaseGetters()
	{
		assertEquals("Wanted Height of Field.",height, field.getHeight());
		assertEquals("Wanted width of Field", width, field.getWidth());
		assertEquals("Resulting #position of field", width*height, field.size());
	}


	@Test
	public void testInsertedMines()
	{
		int[] indices;

		msg = "Fill with no mines: randomly";
		mines = 0;
		this.field.fillRandomly(mines);
		assertEquals(msg, mines, field.getMines());

		msg = "Fill with no mines: indices array";
		mines = 0;
		field.fillMines(new int[0]);
		assertEquals(msg, mines, field.getMines());

		msg = "Fill with mines: randomly";
		mines = 7;
		field.fillRandomly(mines);
		assertEquals(msg, mines, field.getMines());

		msg = "Fill with mines: indices array";
		mines = 9;
		Collection<Integer> mineIndices;
		mineIndices = new ArrayList<Integer>();
		indices     = new int[mines];
		for (int i=0; i<mines; i=mineIndices.size())
		{
			int index = (int) (Math.random()*field.size());

			if (mineIndices.contains(index)) continue;
			mineIndices.add(index);
			indices[i] = index;
		}

		field.fillMines(indices);
		assertEquals(msg, mines, field.getMines());
		assertEquals(msg+" (actual indices)", mineIndices.size(), field.getMines());

		msg = "Fill mines: Index array with always the same index";
		field.fillMines(new int[3]); // only 0 as mine
		assertEquals(msg, 1, field.getMines());

		msg = "Fill mines with less than 0 mines";
		mines = -1;
		field.fillRandomly(mines); // 0 mines.
		assertEquals(msg, 0, field.getMines());

		msg = "Fill almost all field positions with mines";
		mines = field.size()-1;
		field.fillRandomly(mines);
		assertEquals(msg, mines, field.getMines());

	}


	@Test
	public void testNeighbours()
	{
		msg = "Get neighbours of top left position.";
		assertEquals(msg, true, field.getNeighbours(0,0).length <= 3);

		msg = "Get neighbours of bottom right position";
		assertEquals(msg, true, field.getNeighbours(height-1,width-1).length <= 3);

		msg = "Get neighbours of another edge position";
		assertEquals(msg, true, field.getNeighbours(0,1).length <= 5);

		msg = "Get neighbours of a non-edge position";
		if (height>2 && width > 2)
		{
			assertEquals(msg, true, field.getNeighbours(1,1).length == 8);
		}
	}


	@Test
	public void testOpenPositions()
	{
		int[] indices;
		int   savePos, value;

		mines = field.size()/9;
		savePos = 0;
		field.fillRandomly(mines, savePos);
		assertEquals(msg, mines, field.getMines());
		assertEquals(msg, mines, field.getMineIndices().length);

		msg = "Open save Position: Not lost";
		field.open(savePos);
		assertEquals(msg, false, field.isLost());

		msg = "Open mine Position: Lost";
		savePos = 0;
		field.fillMines(new int[]{savePos});
		field.open(savePos);
		assertEquals(msg, true, field.isLost());

		savePos = 0;
		for (;savePos<field.size(); savePos++)
		{
			msg = "Test pos: "+savePos;

			msg  = "Test pos \""+savePos+"\"";
			msg += ": CLOSED value";
			this.resetField();
			assertEquals(msg, Field.VALUE_CLOSED, field.onPosition(savePos));

			msg  = "Test pos \""+savePos+"\"";
			msg += ": MINE value";
			field.fillMines(new int[]{savePos});
			field.open(savePos);
			assertEquals(msg, Field.VALUE_MINE_ON_POS, field.onPosition(savePos));

			msg  = "Test pos \""+savePos+"\"";
			msg += ": MARKED value";
			this.resetField();
			field.toggleMark(savePos);
			assertEquals(msg, Field.VALUE_MARKED, field.onPosition(savePos));

			msg  = "Test pos \""+savePos+"\"";
			msg += ": No mines in neighbourhood (value)";
			this.resetField();
			field.open(savePos);
			assertEquals(msg, 0, field.onPosition(savePos));

			msg  = "Test pos \""+savePos+"\"";
			msg += ": All neighbours are mines";
			indices = field.getNeighbours(savePos);
			field.fillMines(indices);
			field.open(savePos);
			assertEquals(msg, indices.length, field.onPosition(savePos));

			msg  = "Test pos \""+savePos+"\"";
			msg += ": Some neighbours";
			field.fillRandomly(mines, savePos);
			field.open(savePos);
			value = field.onPosition(savePos);
			assertEquals(msg+" might be mines", true, 0<=value);
			assertEquals(msg+", but not more", true, value<=indices.length);
		}
	}


	@Test
	public void testToggleMarks()
	{
		msg = "Add a new Marked Positions";
		for (int i=0; i<this.field.size(); i++)
		{
			this.field.toggleMark(i); // toggle index i
			assertEquals(msg, i+1, this.field.getMarked());
			assertEquals(msg, Field.State.MARKED, this.field.getState(i));
		}

		msg = "Nothing can be opened";
		for (int i=0; i<this.field.size(); i++)
		{
			this.field.open(i);
			assertEquals(msg, 0, this.field.getWithState(Field.State.OPEN,0).length);
		}


	}


	@Test
	public void testInvalidIndices()
	{
		msg = "Fill all positions with mines";
		try
		{
			field.fillRandomly(field.size());
			fail(msg);

		}
		catch (ArrayIndexOutOfBoundsException e)
		{}

		msg = "Get Value from position index: -1";
		try
		{
			field.onPosition(-1);
			fail(msg);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{}

		msg = "Get Value from position index: field.size()"
			+ ", actually [0,field.size()-1]";
		try
		{
			field.onPosition(field.size());
			fail(msg);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{}

		msg = "Get value form position with row, height out of bounds.";
		try
		{
			field.open(width, height);
			fail(msg);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{}

	}
}

