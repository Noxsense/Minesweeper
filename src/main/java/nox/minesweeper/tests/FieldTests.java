package nox.minesweeper.tests;


import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.util.Collection;
import java.util.ArrayList;

import nox.minesweeper.logic.*;


/**
 * Class FieldTest.
 * Run unit test to proove the field methods.
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
		MinesweeperTest.assertMax(msg, 3, field.getNeighbours(0,0).length);

		msg = "Get neighbours of bottom right position";
		MinesweeperTest.assertMax(msg, 3, field.getNeighbours(height-1,width-1).length );

		msg = "Get neighbours of another edge position";
		MinesweeperTest.assertMax(msg, 5, field.getNeighbours(0,1).length );

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

		int[] pos = new int[]
		{
			// corners
			0, this.field.size()-1, width,

			// edges
			1, width -1,

			// mid positions
			(width+1)%field.size()
		};

		for (int p : pos)
		{
			msg = "Test pos: "+p;

			msg  = "Test pos \""+p+"\"";
			msg += ": CLOSED value";
			this.resetField();
			assertEquals(msg, Field.VALUE_CLOSED, field.onPosition(p));

			msg  = "Test pos \""+p+"\"";
			msg += ": MINE value";
			field.fillMines(new int[]{p});
			field.open(p);
			assertEquals(msg, Field.VALUE_MINE_ON_POS, field.onPosition(p));

			msg  = "Test pos \""+p+"\"";
			msg += ": MARKED value";
			this.resetField();
			field.toggleMark(p);
			assertEquals(msg, Field.VALUE_MARKED, field.onPosition(p));

			msg  = "Test pos \""+p+"\"";
			msg += ": No mines in neighbourhood (value)";
			this.resetField();
			field.open(p);
			assertEquals(msg, 0, field.onPosition(p));

			msg  = "Test pos \""+p+"\"";
			msg += ": All neighbours are mines";
			indices = field.getNeighbours(p);
			field.fillMines(indices);
			field.open(p);
			assertEquals(msg, indices.length, field.onPosition(p));

			msg  = "Test pos \""+p+"\"";
			msg += ": Some neighbours";
			field.fillRandomly(mines, p);
			field.open(p);
			value = field.onPosition(p);
			MinesweeperTest.assertMin(msg+" might be mines", 0, value);
			MinesweeperTest.assertMax(msg+", but not more",  indices.length, value);
		}

		int opened = 0;
		field.fillRandomly(mines);

		opened = field.getWithState(Field.State.OPEN, 0).length;
		field.reveal();

		msg = "Game:reveal() => Lost => Not won";
		assertEquals(msg, true, this.field.isLost());
		assertEquals(msg, false, this.field.isWon());

		msg = "All and only mines are opend now.";
		opened = opened + mines;
		assertEquals(msg+":OPENED", opened, this.field.getWithState(Field.State.OPEN,0).length);
		assertEquals(msg+":CLOSED", field.size()-opened, this.field.getWithState(Field.State.CLOSED, 0).length);

		for (int index : this.field.getWithState(Field.State.OPEN,0))
		{
			assertNotEquals("Correct State.", Field.State.CLOSED, field.getState(index));
		}
	}


	@Test
	public void testToggleMarks()
	{
		int[] positions = new int[]
		{
			// corners
			0, this.field.size()-1, width,

			// edges
			1, width -1,

			// mid positions
			(width+1)%field.size()
		};
		int count = 0;

		msg = "Add a new Marked Positions";
		for (int i : positions)
		{
			this.field.toggleMark(i); // toggle index i
			count += 1;
			assertEquals(msg, count, this.field.getMarked());
			assertEquals(msg, Field.State.MARKED, this.field.getState(i));
		}

		msg = "Nothing can be opened";
		for (int i : positions)
		{
			this.field.open(i);
			assertEquals(msg, 0, this.field.getWithState(Field.State.OPEN,0).length);
			assertEquals(msg, Field.State.MARKED, this.field.getState(i));
		}

		msg = "Field is neither won nor lost";
		assertEquals(msg+":WON", false, field.isWon());
		assertEquals(msg+":Lost", false, field.isLost());

		msg = "Unmark all";
		for (int i : positions)
		{
			this.field.toggleMark(i);
			count += -1;
			assertEquals(msg, count, this.field.getWithState(Field.State.MARKED,0).length);
			assertEquals(msg, Field.State.CLOSED, this.field.getState(i));
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
