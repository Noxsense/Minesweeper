package nox.minesweeper.desktop;


import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;


import nox.minesweeper.logic.*;



/**
 * Class GameField.
 * Graphical representation for games.
 */
class GameField extends Canvas implements MouseListener, MouseMotionListener
{
	private final static long MARK_TIME_MIN = 10;

	private Minesweeper host;
	private Game game;

	private long markTime;
	private long mouseClickStarted;
	private int  aimedFieldPos;

	private Design  design;
	private int     cellSize;
	private int     cellGap;

	private Graphics buffGraphics;
	private Image    buffImage;

	private int      seenFirstRow;
	private int      seenFirstCol;

	private Dimension paintedFieldSize;


	/**
	 * Class Design.
	 * Contains information of colours, cell text and cell roundness.
	 */
	public static class Design
	{
		private final static int MINE  = 9;
		private final static int MARK  = 10;
		private final static int CLOSE = 11;

		private Color   colText;
		private Color   colClosed;
		private Color[] colOpen;
		private char[]  posChar;
		private double  roundness;

		/**
		 * Iniate a new Design.
		 */
		private Design()
		{
			this.posChar = new char[12]; // 9 open, mine, closed and so on.

			for (int mines=1; mines<=8; mines++) // initate classic mine text.
			{
				this.posChar[mines] = (char) (48+mines);
			}
			this.updateCellLabels();

			this.colOpen = new Color[9];
			this.setMainColour(Color.BLACK);
			this.setTextColour(Color.BLACK);
			this.setClosedColour(Color.GRAY);
			this.setRoundness(.1);
		}

		/**
		 * Create a new Design.
		 * @return newly initated Design.
		 */
		static Design createDesign()
		{
			return new Design();
		}


		/**
		 * Set roundness.
		 * @param a new roundness value.
		 */
		public void setRoundness(double percent)
		{
			this.roundness = (percent<0) ? 0 : percent;
		}


		/**
		 * Update the labels for the cells.
		 * Depends on the Field.displays
		 */
		public void updateCellLabels()
		{
			this.posChar[0]     = Field.getDisplay(Field.DISPLAY_ZERO);
			this.posChar[MINE]  = Field.getDisplay(Field.DISPLAY_MINE);
			this.posChar[MARK]  = Field.getDisplay(Field.DISPLAY_MARKED);
			this.posChar[CLOSE] = Field.getDisplay(Field.DISPLAY_CLOSED);
		}


		/**
		 * Set text colour.
		 * @param textColor new colour for text.
		 */
		private void setTextColour(Color textColor)
		{
			this.colText = (textColor==null) ? Color.BLACK : textColor;
		}


		/**
		 * Set the colour for the opened mines.
		 * The different between them will be their alpha value.
		 * @param colour main colourscheme | base colour.
		 */
		public void setClosedColour(Color colour)
		{
			this.colClosed = (colour==null) ? Color.BLACK : colour;
		}


		/**
		 * Set the colour for the opened mines.
		 * The different between them will be their alpha value.
		 * @param colour main colourscheme | base colour.
		 */
		public void setMainColour(Color colour)
		{
			this.setMainColour((colour==null) ? 0 : colour.getRGB());
		}


		/**
		 * Set the colour for the opened mines.
		 * The different between them will be their alpha value.
		 * @param rgb main colourscheme | base colour.
		 */
		public void setMainColour(int rgb)
		{
			int alpha, colour;

			alpha = 255/9;
			rgb   = (rgb<0) ? 0 : rgb;

			for (int mines=0; mines<this.colOpen.length; mines++)
			{
				colour = ((alpha*mines) << 24)  | rgb;
				this.colOpen[mines] = new Color(colour, true);
			}
		}


		/**
		 * Draw the Cell with the given data.
		 * @param g     graphics where the cell shoule be displayed.
		 * @param point point where to set the cell representation
		 * @param size  size of the cell representation
		 * @param data  open, closed, marked, mine...
		 * @return 
		 */
		public void drawCell(Graphics g, Point point, int size, int data)
		{
			if (g==null)
			{
				return;
			}

			int arc_ = (int) Math.round((size*roundness));

			/*Opened cell.*/
			if (0<=data && data<colOpen.length)
			{
				g.setColor(this.colOpen[data]);
				g.fillRoundRect(point.x, point.y, size, size, arc_, arc_);
			}

			/*Closed Cell*/
			else
			{
				g.setColor(this.colClosed);
				g.drawRoundRect(point.x, point.y, size, size, arc_, arc_);
			}

			/*Cell text.*/
			switch (data)
			{
				case Field.VALUE_MINE_ON_POS: data = MINE; break;
				case Field.VALUE_CLOSED: data = CLOSE; break;
				case Field.VALUE_MARKED: data = MARK; break;
				default: break;
			}

			FontMetrics fm;
			int height, width;

			fm     = g.getFontMetrics();
			height = fm.getAscent();
			width  = fm.charWidth(this.posChar[data]);

			g.setColor(this.colText);
			point.y = point.y+(size+height)/2;       // y center of cell
			point.x = point.x+(size-width)/2; // x center of cell
			g.drawChars(this.posChar, data, 1, point.x, point.y);
		}
	}


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
		this.addMouseMotionListener(this);

		this.showRoot();

		Field.setDisplay0(' ');
		this.setCellSize(30);
		this.setCellGap(-1);
		this.setMarkTime(300);
		this.design = Design.createDesign();
	}


	/**
	 * Get the Design of this GameField.
	 * @return used design.
	 */
	public Design getDesign()
	{
		return this.design;
	}


	/**
	 * Set the current Game for this GameField.
	 * @param g new Game value to set.
	 */
	public void openGame(Game g)
	{
		this.game = g;
		this.showRoot();
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
	 * Get the dimension of currently painted Field.
	 * If there is currently no game and no field,
	 * it will be at least one cell big.
	 * @return preferred Dimension.
	 */
	public Dimension getPaintedFieldSize()
	{
		/*Initate paintedFieldSize*/
		if (this.paintedFieldSize == null)
		{
			this.paintedFieldSize = new Dimension(1,1);
		}

		/*Set preferred size in dependencies of the current game.*/
		if (this.game==null)
		{
			this.paintedFieldSize.setSize(this.getCellSize(),this.getCellSize());
		}
		else
		{
			double size = this.getCellSize();
			double gap  = this.getCellGap();
			double line = .1; // pixel width (border left AND right together)
			this.paintedFieldSize.setSize(
					(this.game.field.getWidth() -1)*(size+gap+line)+ size,
					(this.game.field.getHeight()-1)*(size+gap+line)+ size);
		}
		return this.paintedFieldSize;
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
		if (this.cellSize <= 0)
		{
			this.setCellSize(this.cellSize);
		}

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

		this.getBufferedGraphics();

		if (this.buffGraphics==null || graphics==null)
		{
			return;
		}

		this.clear(this.buffGraphics);
		this.design.updateCellLabels();

		int size = this.getCellSize();
		int gap  = this.getCellGap();

		for (int i=0; i<this.game.field.size(); i++)
		{
			Point p;
			this.design.drawCell(buffGraphics,
					p=this.index2Point(i),
					size,
					this.game.field.onPosition(i));
			buffGraphics.setColor(Color.GREEN);
			buffGraphics.drawString(""+i, p.x,p.y);
		}

		graphics.drawImage(this.buffImage,
				0,//this.seenFirstCol*(size+gap),
				0,//this.seenFirstRow*(size+gap),
				this);
	}


	@Override
	public void update(Graphics graphics)
	{
		this.paint(graphics);
	}


	/**
	 * Get the buffered graphics for double buffering.
	 * @return this buffered grapics.
	 */
	private Graphics getBufferedGraphics()
	{
		if (this.buffGraphics==null
				|| this.buffImage.getWidth(this)  < this.getWidth()
				|| this.buffImage.getHeight(this) < this.getHeight())
		{
			try
			{
				this.buffImage = new BufferedImage(
						super.getWidth(),
						super.getHeight(),
						BufferedImage.TYPE_INT_ARGB);

				this.buffGraphics = this.buffImage.getGraphics();
			}
			catch (IllegalArgumentException e)
			{}
		}
		return this.buffGraphics;
	}


	/**
	 * Clear graphics for this canvas.
	 * @param graphics
	 */
	private void clear(Graphics graphics)
	{
		if (graphics == null)
			return;

		graphics.setColor(this.host.getBackground());
		graphics.fillRect(0,0, super.getWidth(), super.getHeight());
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
		int width  = this.game.field.getWidth();

		int index = ((offset*(width-seenFirstCol)) < p.x)
			? this.point2Index(null)
			: (p.y/(offset)*width) + p.x/(offset);
		System.out.println("Index: "+index);
		return index;
	}


	/**
	 * Reset the current View to the root.
	 */
	public void showRoot()
	{
		this.seenFirstRow = 0;
		this.seenFirstCol = 0;
	}


	/**
	 * Scroll horizontally some columns.
	 * @param cols to skip
	 */
	public void scrollHorizontal(int cols)
	{
		/*There's nothing to scroll.*/
		if (this.game == null)
		{
			return;
		}

		this.seenFirstCol = Math.max(0, seenFirstCol+cols);
		this.seenFirstCol = Math.min(this.game.field.getWidth(), seenFirstCol+cols);
	}


	/**
	 * Scroll vertically some rows.
	 * @param rows to skip
	 */
	public void scrollVertical(int rows)
	{
		/*There's nothing to scroll.*/
		if (this.game == null)
		{
			return;
		}

		this.seenFirstRow = Math.max(0, seenFirstRow+rows);
		this.seenFirstRow = Math.min(this.game.field.getHeight(), seenFirstRow+rows);
	}


	@Override
	public void mouseMoved(MouseEvent e)
	{
		// on hover
	}


	@Override
	public void mouseEntered(MouseEvent e)
	{
		// on hover
	}


	@Override
	public void mouseExited(MouseEvent e)
	{
		// on hover
	}


	@Override
	public void mousePressed(MouseEvent e)
	{
		this.mouseClickStarted = GameField.now();

		if (e==null) return;

		this.aimedFieldPos = this.point2Index(e.getPoint());
	}


	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (this.game==null)
			return;

		int pos, difference, width, v, h;
		pos        = this.point2Index(e.getPoint());
		width      = this.game.field.getWidth();
		difference = pos - this.aimedFieldPos;
		
		v = (difference)/width;
		h = (difference)%width;

		this.scrollVertical((difference)/width);
		this.scrollHorizontal((difference)%width);
		this.repaint();

		this.aimedFieldPos = pos;

		System.out.println("Mouse Dragged: Movement ("+v+","+h+")");
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
			this.resetMouseClick(false);
			return;
		}

		if (this.aimedFieldPos<0 || this.game.field.size()<= this.aimedFieldPos)
		{
			return;
		}

		/*Check toggle...*/
		if (this.isToggleMarkEvent(e, clickTime))
		{
			this.game.toggleMark(this.aimedFieldPos);
		}

		/*... or open.*/
		else
		{
			this.game.open(this.aimedFieldPos);
		}

		this.resetMouseClick(true);
	}


	/**
	 * Reset the mouse click event like time or aimed position.
	 * @param repaint if true, repaint the canvas.
	 */
	private void resetMouseClick(boolean repaint)
	{
		this.host.updateGameLabel();
		this.mouseClickStarted = Long.MAX_VALUE;
		this.aimedFieldPos     = -1;

		if (repaint)
		{
			this.repaint();
		}
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
