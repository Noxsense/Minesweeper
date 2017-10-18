package nox.minesweeper.desktop;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


import nox.minesweeper.logic.*;



/**
 * Class GameField.
 * Graphical representation for games.
 */
class GameField extends Canvas implements MouseListener
{
	private final static long MARK_TIME_MIN = 10;

	private final static int CHAR_MINE  = 9;
	private final static int CHAR_MARK  = 10;
	private final static int CHAR_CLOSE = 11;

	private Minesweeper host;
	private Game game;

	private long markTime;
	private long mouseClickStarted;
	private int  aimedFieldPos;

	private char[]  posChar;
	private Color[] colours;
	private Color   colourClosed;
	private int     cellSize;
	private int     cellGap;

	/**
	 * Initate a new GameField.
	 */
	public GameField(Minesweeper host) throws NullPointerException
	{
		super();
		this.host = host;
		if (this.host==null)
		{
			throw new NullPointerException("Host is null");
		}

		this.setBackground(null);
		this.addMouseListener(this);

		this.setCellSize(30);
		this.setCellGap(-1);
		this.setMarkTime(200);
		
		this.posChar = new char[12];
		this.posChar[0]         = Field.getDisplay(Field.DISPLAY_ZERO);
		this.posChar[CHAR_MINE] = Field.getDisplay(Field.DISPLAY_MINE);
		this.posChar[CHAR_MARK] = Field.getDisplay(Field.DISPLAY_MARKED);
		this.posChar[CHAR_CLOSE] = Field.getDisplay(Field.DISPLAY_CLOSED);

		this.colourClosed = new Color(0,0,0);

		this.colours = new Color[9];

		for (int mines=0; mines<colours.length; mines++)
		{
			this.colours[mines] = new Color(0, 0, 0, 255/colours.length*mines);
			
			if (mines==0) continue; // skip zero

			this.posChar[mines] = (char) (48+mines);
		}
	}


	/**
	 * Set the current Game for this GameField.
	 * @param g new Game value to set.
	 */
	public void openGame(Game g)
	{
		this.game = g;
	}


	/**
	 * Get the current Game of this GameField.
	 * @return current game as Game.
	 */
	public Game getGame()
	{
		return this.game;
	}


	/**
	 * Set the mark time (for toggle).
	 * @param markTime new mark time, at least minimum.
	 */
	public void setMarkTime(long markTime)
	{
		this.markTime = (markTime < MARK_TIME_MIN) ? MARK_TIME_MIN : markTime;
	}


	/**
	 * Set new size of one cell (square).
	 * @param size new size value, at least 1.
	 */
	public void setCellSize(int size)
	{
		this.cellSize = (size<1) ? 1 : size;
	}


	/**
	 * Get the size of one cell (square).
	 * @return side length of once cell.
	 */
	public int getCellSize()
	{
		return this.cellSize;
	}


	/**
	 * Set Gap value between cells.
	 * @param gap new gap.
	 */
	public void setCellGap(int gap)
	{
		this.cellGap = (gap<0) ? -1 : cellGap;
	}


	
	/**
	 * Get the Gap between to cells.
	 * If gap less than 0, the gap will be depend on the cell size.
	 * @return gap between as int.
	 */
	public int getCellGap()
	{
		return (this.cellGap < 0)
			? this.getCellSize()/5
			: this.cellGap;
	}


	@Override
	public void paint(Graphics graphics)
	{
		if (this.game == null)
		{
			super.paint(graphics);
			return;
		}

		if (graphics == null)
		{
			return;
		}

		FontMetrics fm;
		int         size, arc, gap, textHeight, textWidth;

		size       = this.getCellSize();
		gap        = this.getCellGap();
		arc        = 2;
		fm         = graphics.getFontMetrics();
		textHeight = fm.getAscent();

		int[] cellTextWidth = new int[this.posChar.length];

		for (int i=1; i<posChar.length; i++)
		{
			cellTextWidth[i] = fm.charWidth(this.posChar[i]);
		}

		for (int i=0; i<this.game.field.size(); i++)
		{
			Point point = this.index2Point(i);
			int   mines = this.game.field.onPosition(i);

			graphics.setColor(this.colourClosed);

			switch (mines)
			{
				case Field.VALUE_CLOSED:
					mines = CHAR_CLOSE;
					break;

				case Field.VALUE_MINE_ON_POS:
					mines = CHAR_MINE;
					break;

				case Field.VALUE_MARKED:
					mines = CHAR_MARK;
					break;

				case 0:
				case 1:case 2:case 3:case 4:
				case 5:case 6:case 7:case 8:
					graphics.setColor(this.colours[mines]);
					break;

				default: // error?
					continue;
			}

			graphics.drawRoundRect(point.x, point.y, size, size, arc, arc);

			point.y = point.y+(size+textHeight)/2;       // y center of cell
			point.x = point.x+(size-cellTextWidth[mines])/2; // x center of cell
			graphics.drawChars(this.posChar, mines, 1, point.x, point.y);
		}
	}


	/**
	 * Translate the given index in this game.field to 2D Point.
	 * @param index    in field.
	 * @param cellSize cell size.
	 * @param gap      gap between cells.
	 * @return         point with coordinates for cell root.
	 */
	private Point index2Point(int index)
	{
		if (this.game == null)
			return null;

		int size = this.getCellSize();
		int gap  = this.getCellGap();

		int y = (index/this.game.field.getWidth())*(size + gap); // row
		int x = (index%this.game.field.getWidth())*(size + gap); // column

		return new Point(x, y);
	}


	/**
	 * Get the game field index for the given point.
	 * @param p point.
	 * @return index if field.position was hit, else -1
	 */
	private int point2Index(Point p)
	{
		if (p==null || this.game==null)
		{
			return -1;
		}

		int offset = this.getCellSize()+this.getCellGap();

		return p.y/(offset)*this.game.field.getWidth() + p.x/(offset);
	}


	@Override
	public void mouseEntered(MouseEvent e)
	{
	}


	@Override
	public void mouseExited(MouseEvent e)
	{
	}



	@Override
	public void mousePressed(MouseEvent e)
	{
		this.mouseClickStarted = GameField.now();

		if (e==null) return;

		this.aimedFieldPos = this.point2Index(e.getPoint());
	}


	@Override
	public void mouseReleased(MouseEvent e)
	{
	}


	@Override
	public void mouseClicked(MouseEvent e)
	{
		long clickTime  = GameField.now() - this.mouseClickStarted;

		/*Invalid input.*/
		if (clickTime<0 || e == null || this.game == null)
		{
			this.resetMouseClick();
			return;
		}

		if (this.aimedFieldPos<0 || this.game.field.size()<= this.aimedFieldPos)
		{
			return;
		}

		int  currentAim = this.point2Index(e.getPoint());

		/*Goal changed.*/
		if (this.aimedFieldPos != currentAim)
		{
			this.resetMouseClick();
			return;
		}

		/*Check toggle...*/
		if (this.isToggleMarkEvent(e, clickTime))
		{
			this.game.toggleMark(this.aimedFieldPos);
			this.resetMouseClick();
			this.repaint();
			return;
		}

		/*... or open.*/
		this.game.open(this.aimedFieldPos);


		this.resetMouseClick();
		this.repaint();
	}


	/**
	 * Reset the mouse click event like time or aimed position.
	 */
	private void resetMouseClick()
	{
		this.host.updateGameLabel();
		this.mouseClickStarted = Long.MAX_VALUE;
		this.aimedFieldPos     = -1;
	}


	/**
	 * Check if the current imput is fitting the conditions of toggling marks.
	 * @param e MouseEvent.
	 * @param clickTime Time the mouse was pressedn.
	 * @return true, if mousetime or buttons are ok.
	 */
	private boolean isToggleMarkEvent(MouseEvent e, long clickTime)
	{
		return e!=null
			&& (
					this.markTime <= clickTime ||
					e.getButton() != MouseEvent.BUTTON1
			   );
	}


	/**
	 * Keep uniform time unit for this representation.
	 * @return current time (milli seconds) in long.
	 */
	protected static long now()
	{
		return System.currentTimeMillis();
	}
}
