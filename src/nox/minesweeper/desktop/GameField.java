package nox.minesweeper.desktop;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.*;

import nox.minesweeper.logic.Field;


/**
 * Class GameField.
 * Contains field with mines and measures the played time.
 * Shouldn't be public.
 */
class GameField extends JPanel implements MouseListener
{
	// gui attributes and statics.
	protected final static String INDICATOR = "Minesweeper.GameField.";
	private final static Border[] colBorder = new Border[]
	{
		null // noo border, if no mines are near.
			,BorderFactory.createLineBorder(new Color(0, 255, 171), 1)
			,BorderFactory.createLineBorder(new Color(0, 255, 14),  2)
			,BorderFactory.createLineBorder(new Color(143, 255, 0), 3)
			,BorderFactory.createLineBorder(new Color(255, 210, 0), 4)
			,BorderFactory.createLineBorder(new Color(255, 0, 103), 5)
			,BorderFactory.createLineBorder(new Color(250, 0, 255), 6)
			,BorderFactory.createLineBorder(new Color(93, 0, 255),  7)
			,BorderFactory.createLineBorder(new Color(0, 64, 255),  8)
	};

	// key attributes
	private Field         field;
	private JLabel[]      posView;
	private int           hiddenMines;

	// game (history) attributes
	private int           gamesLost;
	private int           gamesWon;
	private int           winStreak;
	private long          avgTime;
	private long          bestTime;

	// current game attributes
	private long          gameTime;
	private boolean       initiated, paused;
	private long          clickStarted;
	private boolean       posLoaded;

	// additional gui attributes
	private Minesweeper   host;


	/**
	 * Initate a new GameField with a matching field.
	 * @param height field dimension
	 * @param width  field dimension
	 * @param n      mines count
	 */
	public GameField(int height, int width, int n) throws ArrayIndexOutOfBoundsException
	{
		this.field = new Field(height, width);
		this.setLayout(new GridLayout(height,width));
		this.setBorder(null);
		this.initiatePositions();

		this.hiddenMines = (n<1||this.field.size()<=n) ? 1 : n;

		this.gameTime = -1; // neigther playerd nor started
		this.setOpaque(false);

		this.clickStarted = Long.MAX_VALUE;
		this.initiated    = false;
		this.paused       = true;

		this.bestTime     = Long.MAX_VALUE;
		this.gamesLost    = 0;
		this.gamesWon     = 0;
	}


	/**
	 * Get the Minesweeper (extending JFrame) where this Field is hosted.
	 * @return host as Minesweeper
	 */
	public Minesweeper getHost()
	{
		return this.host;
	}


	/**
	 * Set the Minesweeper host (may be null?).
	 * @param h new host
	 */
	public void setHost(Minesweeper h)
	{
		this.host = h;
	}


	/**
	 * Initiate (or reset) the buttons.
	 */
	private void initiatePositions()
	{
		Color     colour;
		Border    border;

		colour = new Color(230,230,230);
		border = BorderFactory.createLineBorder(colour);
		colour = new Color(240,240,240);

		if (this.posView == null) // initatiate
		{
			this.posView  = new JLabel[this.field.size()];
		}

		for (int i=0; i<posView.length; i++) // reset views
		{
			if (this.posView[i] == null)
			{
				this.posView[i] = new JLabel("", JLabel.CENTER);
				this.posView[i].addMouseListener(this);
				this.posView[i].revalidate();
				this.posView[i].setName(""+i);
				this.add(this.posView[i]);
			}

			this.posView[i].setBackground(colour);
			this.posView[i].setBorder(border);
			this.posView[i].setEnabled(true);
			this.posView[i].setForeground(new Color(100,100,100));
			this.posView[i].setOpaque(true);
			this.posView[i].setText("");
			this.posView[i].revalidate();
			this.posView[i].repaint();
		}

		this.posLoaded = true; // mark as (re)loaded


		if (host==null) // no host
		{
			return;
		}

		JButton giveUpBtn = host.getGiveUpButton();
		if (giveUpBtn != null)
		{
			giveUpBtn.setText(":(");
			giveUpBtn.setToolTipText("This will end this game automatically.");
		}
	}


	@Override
	public Dimension getPreferredSize()
	{
		Dimension d  = super.getPreferredSize();
		d.width      = Minesweeper.POS_SCALE * this.field.getWidth();
		d.height     = Minesweeper.POS_SCALE * this.field.getHeight();
		return d;
	}


	/**
	 * Get the dimension of a Pos in this GameField.
	 * @return size as Dimension.
	 */
	protected Dimension getPosSize()
	{
		return (posView.length<1) ? new Dimension() : posView[0].getSize();
	}


	/**
	 * Get the relation the Width should have to the height.
	 * @return wanted width/height as double.
	 */
	protected double getWidthToHeight()
	{
		return this.field.getWidth()*1./this.field.getHeight();
	}


	/**
	 * Restart the game on this field.
	 * If currently playing, then this will be lost.
	 */
	public void restart()
	{
		if (initiated)
		{
			this.endGame();
		}

		this.initiatePositions();
		this.initiated = false;
	}


	/**
	 * Check if the field is currently playing.
	 * @return true, if a game was initated and not won/lost yet.
	 */
	public boolean isRunning()
	{
		return this.initiated
			&& (!this.field.isLost() && !this.field.isWon());
	}


	/**
	 * Force to end: Set this game as lost  and reveal all mines.
	 */
	public void endGame()
	{
		for (int index : this.field.reveal()) // show mines
		{
			if (this.field.getState(index) == Field.State.MARKED)
			{
				this.posView[index].setBackground(Color.YELLOW);
				continue;
			}
			this.posView[index].setText("X");
			this.posView[index].setBackground(Color.BLACK);
			this.posView[index].setForeground(Color.WHITE);
		}

		this.gamesLost += 1;
		this.winStreak = 0;
		this.posLoaded = false;
	}


	/**
	 * Get the count of hidden mines or as mine marked mine.
	 * @param  real true: hidden mines, else as mine marked.
	 * @return #mines or #marked
	 */
	public int getMines(boolean real)
	{
		return (real) ? this.field.getMines() : this.field.getMarked();
	}


	/**
	 * Get the current win streak: Sequence of won games.
	 * @return win streak as int.
	 */
	public int winStreak()
	{
		return this.winStreak;
	}


	/**
	 * Get the count of games, which were won with this field.
	 * @param won get #won games (or #lost)
	 * @return count as int.
	 */
	public int countGames(boolean won)
	{
		return (won) ? this.gamesWon : this.gamesLost;
	}


	/**
	 * Get the best time (or average time).
	 * @param  best time (or aerage time if false).
	 * @return best time (or average) as long.
	 */
	public long getTime(boolean best)
	{
		return (best) ? this.bestTime : this.avgTime;
	}


	/**
	 * Pause the game.
	 * Resumable with current state.
	 */
	public void pause()
	{
		if (paused) // already paused.
		{
			return;
		}

		this.gameTime = this.playedTime(); // store play in start.
		this.paused   = true;
	}


	/**
	 * Resume a paused Game.
	 */
	public void resume()
	{
		if (!paused) // is not paused yet/anymore.
		{
			return;
		}

		this.gameTime = this.startTime();
		this.paused   = false;
	}


	/**
	 * Get the time, the current game may have started (to have the given play time).
	 * If paused, gameTime stores played time.
	 * @return time (nanoseconds) as long
	 */
	public long startTime()
	{
		return (paused) ? System.nanoTime()-this.gameTime : this.gameTime;
	}


	/**
	 * Get the time, the current game is currently played.
	 * If paused, played gameTime stores in start time.
	 * @return time (nanoseconds) as long
	 */
	public long playedTime()
	{
		return (paused) ? this.gameTime : System.nanoTime()-this.gameTime;
	}


	/**
	 * Set games count for won or lost.
	 * @param n   new count.
	 * @param won set won or lost.
	 */
	protected void setGames(int n, boolean won)
	{
		if (won) this.gamesWon  = (n<0) ? 0 : n;
		else     this.gamesLost = (n<0) ? 0 : n;
	}


	/**
	 * Set the times for the games, average or best time.
	 * @param t    new time score.
	 * @param best set t as best time (or as average time).
	 */
	protected void setTimes(long t, boolean best)
	{
		if (best) this.bestTime = (t<0) ? 0 : t;
		else      this.avgTime  = (t<0) ? 0 : t;
	}


	/**
	 * Set the streak for wins in sequence.
	 * @param n count of wins.
	 */
	protected void setStreak(int n)
	{
		this.winStreak = (n<0) ? 0 : n;
	}


	/**
	 * Set new best time and average time and add a new win.
	 * @param end time.
	 */
	private void updateWin(long now)
	{
		if (!this.field.isWon() || now < 0) // safety check
		{
			return;
		}

		long played;
		played = now - this.gameTime;   // in nano seconds.
		played = (long) (played*1E-6);  // in milli seconds.


		this.bestTime   = (played<this.bestTime) ? played : this.bestTime;
		this.gamesWon  += 1;
		this.winStreak += 1;
		this.avgTime    = (this.avgTime*(gamesWon-1) + played)/gamesWon;

		this.initiated  = false; // new game hasn't started yet.
		this.gameTime   = -1;

		this.posLoaded  = false;


		if (host==null) // no host => skip
		{
			return;
		}

		JButton giveUpBtn = host.getGiveUpButton();
		if (giveUpBtn != null)
		{
			giveUpBtn.setText(":)");
			giveUpBtn.setToolTipText("Start a new game.");
		}

		host.updateGameLabel(); // update label.
	}


	/**
	 * Graphical representation of opening a field's position.
	 * @param i position to open.
	 */
	private void open(int i)
	{
		long  now    = System.nanoTime();
		int[] opened = this.field.open(i);

		for (int index : opened) // display all opened positions
		{
			this.show(index);
		}

		if (this.field.isLost()) // "force to lost"
		{
			this.endGame();
		}

		else if (this.field.isWon()) // update a new win now
		{
			this.updateWin(System.nanoTime());
		}
	}


	/**
	 * Graphical representation of one position with its posView.
	 * @param i position to show.
	 */
	private void show(int i)
	{
		switch (this.field.getState(i)) // not open: just toggle mark.
		{
			case CLOSED:
				this.posView[i].setText("");
				return; // do nothing

			case MARKED:
				String str = "<html><font color=\"red\"><b>?</b></font></html>";
				this.posView[i].setText(str);
				return;

			default: break;
		}

		int    mines = this.field.onPosition(i);
		String str   = "<html><font color=\"rgb(100,100,100)\">X</font></html>";

		this.posView[i].setOpaque(9 < mines); // if mine: fill black.

		if (0<=mines && mines < 9) // open without mine.
		{
			str = (mines<1) ? "" : str.replaceAll("X", ""+mines);
			this.posView[i].setBorder(GameField.colBorder[mines]);
		}
		else // this is a mine.
		{
			this.posView[i].setBackground(Color.BLACK);
			this.posView[i].setForeground(Color.WHITE);
		}

		this.posView[i].setText(str);
		this.posView[i].revalidate();
		this.posView[i].repaint();
	}


	@Override
	public void mouseClicked(MouseEvent e)
	{
		Object src = e.getSource();

		// as play time
		clickStarted = System.currentTimeMillis() - this.clickStarted;

		// not if game is ended.
		if (e==null || !this.posLoaded || src==null || !(src instanceof JComponent))
		{
			this.clickStarted = Long.MAX_VALUE;
			return;
		}

		boolean mark = host!=null  && host.getMarkTime() <= clickStarted
			|| e.getButton() != MouseEvent.BUTTON1;

		this.clickStarted = Long.MAX_VALUE; // reset

		/*To open position with index as name*/
		int i = Integer.parseInt(((JComponent)src).getName()); // index by name

		if (!this.initiated) // starting game.
		{
			/*Set start time and fill all except current.*/
			this.paused   = false;
			this.gameTime = System.nanoTime();
			this.field.fillRandomly(this.hiddenMines, i);
		}

		this.resume(); // doesn't do anything, if not paused.
		this.initiated = true;
		this.paused    = false;

		if (mark) this.field.toggleMark(i);
		else      this.open(i);

		this.show(i); // display, doesn't have to reveal.

		if (this.host!=null) // update host's game label
		{
			this.host.updateGameLabel();
		}

		this.revalidate();
		this.repaint();
	}


	@Override
	public void mousePressed(MouseEvent e)
	{
		// start measssuring click time, eg. for marking time.
		this.clickStarted = System.currentTimeMillis();
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{}

	@Override
	public void mouseEntered(MouseEvent e) // just hovering
	{}

	@Override
	public void mouseExited(MouseEvent e) // just hovering
	{}


	@Override
	public String toString()
	{
		String f = "GameField: %d:%d (%d)%s";
		return String.format(f
				,  this.field.getWidth(), this.field.getHeight() // dimension
				,  this.hiddenMines                              // minescount
				, ((this.isRunning()) ? " RUNNING" : "")
				);
	}


	@Override
	public int hashCode()
	{
		return this.field.hashCode();
	}


	@Override
	public boolean equals(Object o)
	{
		return o!=null && o instanceof GameField
			&& this.field.equals(((GameField)o).field)
			&& this.hiddenMines == ((GameField)o).hiddenMines
			;
	}


	/**
	 * Get the current field information into a String.
	 * The current game isn't saved here.
	 *   field.width
	 *   field.height
	 *   hiddenMines
	 *   gamesLost
	 *   gamesWon
	 *   winStreak
	 *   avgTime
	 *   bestTime
	 * @return String with dimension, mine count, and other data.
	 */
	public String allInformation()
	{
		return GameField.INDICATOR
			+  this.field.getWidth()
			+":"+  this.field.getHeight()
			+":"+  this.hiddenMines
			+":"+  this.gamesLost
			+":"+  this.gamesWon
			+":"+  this.winStreak
			+":"+  this.avgTime
			+":"+  this.bestTime
			;
	}


	/**
	 * Try to parse a GameField.
	 * with GameField.allInformation() : String
	 * @param str String with GameField data.
	 * @return parsed as GameField
	 * @throws NullPointerException 
	 * @throws NumberFormatException 
	 */
	protected static GameField parseGameField(String str) throws NullPointerException, NumberFormatException
	{
		str = str.trim();
		if (!str.startsWith(GameField.INDICATOR))
		{
			throw new NullPointerException("Invalid Format for GameField.");
		}

		String[] no;
		no = str.substring(GameField.INDICATOR.length(), str.length()).split(":");

		if (no.length != 8)
		{
			throw new NullPointerException("Attribute size invalid.");
		}

		int width, height, mines, lost, won, streak;
		long avgTime, bestTime;

		width  = Integer.parseInt(no[0]);
		height = Integer.parseInt(no[1]);
		mines  = Integer.parseInt(no[2]);
		lost   = Integer.parseInt(no[3]);
		won    = Integer.parseInt(no[4]);
		streak = Integer.parseInt(no[5]);

		avgTime  = Long.parseLong(no[6]);
		bestTime = Long.parseLong(no[7]);

		GameField parsed = new GameField(height, width, mines);

		parsed.setGames(lost,     false);
		parsed.setGames(won,      true);
		parsed.setTimes(bestTime, true);
		parsed.setTimes(avgTime,  true);
		parsed.setStreak(streak);

		//fs.add(0, parsed);
		return parsed;
	}
}
