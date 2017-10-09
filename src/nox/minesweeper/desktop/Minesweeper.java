package nox.minesweeper.desktop;


import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import nox.minesweeper.logic.Field;


/**
 * Minesweeper class.
 * Starting the GUI.
 */
public class Minesweeper extends JFrame implements ActionListener
{
	protected static int              POS_SCALE = 10;
	protected static Labels           ls        = Labels.getInstance();

	private CardLayout                layout;
	private JMenu                     prefsMenu;
	private String                    currView;

	private JPanel                    homeView;

	private JPanel                    selectorView;
	private JSlider                   hSlider, wSlider, mSlider;
	private JList<GameField>          fieldSelector;

	private JPanel                    gameView;
	private JLabel                    timeLabel;
	private JLabel                    gameLabel;
	private GameField                 currGame;
	private JButton                   giveUpBtn;
	private Timer                     gameTimer;

	private JComponent                statsView;
	private JComponent                prefView;

	private ListModel<GameField>      fieldsModel; // known fields: size x mines
	private long                      markTime;
	private int                       max;


	/**
	 * Start Minesweeper.
	 * Create a frame where minesweeper could be played with a (maybe usable) gui.
	 * @param width 
	 * @param height 
	 */
	public Minesweeper(int width, int height)
	{
		super("Minesweeper");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.getContentPane().setBackground(Color.WHITE);

		try // load Strings
		{
			Minesweeper.ls.loadFromFile("res/labels.csv");
		}
		catch (NullPointerException e)
		{
			System.out.println("Loading file went wrong, use Standard Labels.");
			LabelsFiller.INSTANCE.fillLabels();
		}

		this.fieldsModel = new DefaultListModel<GameField>();

		this.setLayout(this.layout = new CardLayout()); // card layout.

		this.initiateMenu(); // home button and other buttons.
		this.showHome();

		this.setMarkTime(200);
		this.setMax(50);

		int x,y;
		Point p;
		p = MouseInfo.getPointerInfo().getLocation();
		x = p.x-width/2;
		y = p.y-height/2;
		this.setBounds((x<0) ? 0 : x, (y<0) ? 0 : y, width,height);
		this.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e==null || e.getSource()==null || !(e.getSource() instanceof JComponent))
			return;

		/*Try to handle by activated component's name., name can be null.*/
		String name = ((JComponent) e.getSource()).getName();

		if (name == null)
			return;

		switch (name)
		{
			case "GAME_NEW":
				this.showFieldSelector(false);
				break;

			case "CREATE_GAME":
				this.openGame(new GameField(
							hSlider.getValue(),
							wSlider.getValue(),
							mSlider.getValue()));
				break;

			case "GAME_LIST":
				int i;
				if ((i = this.fieldSelector.getSelectedIndex()) >= 0)
				{
					this.openGame(this.fieldsModel.getElementAt(i));
				}
				break;

			case "GAME_OLD":
				this.showFieldSelector(true);
				break;

			case "GO_HOME":
				this.showHome();
				break;

			case "GAME_RESTART":
				this.restartCurrent();
				break;

			case "GAME_STATS":
				this.showStatistics();
				break;

			case "FIELDS_EXPORT":
				for (i=0; i<this.fieldsModel.getSize(); i++)
				{
					GameField f = this.fieldsModel.getElementAt(i);
					System.out.println(f.allInformation());
				}
				break;

			case "FIELDS_IMPORT":
				break;

			default:
				this.currGame.pause(); // pause game if running.
				break;
		}
	}


	/**
	 * Set a new maxium limit for one side (width, height).
	 * Prefer not over 100 per side because of performance.
	 * @param m new Maxium.
	 */
	public void setMax(int m)
	{
		this.max = (m < Field.MIN) ? Field.MIN + 1 : m;
	}


	/**
	 * Get the time the mouse have to be pressed to toggle the mark.
	 * @return marktime in milliseconds as long
	 */
	public long getMarkTime()
	{
		return this.markTime;
	}


	/**
	 * Set the time the mouse have to be pressed to toggle the mark.
	 * @param ms milliseconds to press at least to toggle mark.
	 */
	public void setMarkTime(long ms)
	{
		this.markTime = (ms < 10) ? 10 : ms;
	}


	/**
	 * Show home.
	 * Initatate home.
	 * Card: "HOME"
	 */
	private void showHome()
	{
		if (this.homeView == null)
		{
			this.homeView = new JPanel();
			this.homeView.setOpaque(false);
			this.homeView.setLayout(new BorderLayout());
			
			JPanel  panel;
			JLabel  lbl;
			String  str;

			panel = new JPanel();
			panel.setOpaque(false);
			this.homeView.add(panel, BorderLayout.CENTER);

			panel.add(this.createBtn(ls.get("BTN_NEW_GAME"), this, "GAME_NEW"));
			panel.add(this.createBtn(ls.get("BTN_RESUME_GAME"), this ,"GAME_OLD"));
			panel.add(this.createBtn(ls.get("BTN_STATS"), this ,"GAME_STATS"));

			lbl = new JLabel(ls.get("TITLE_HOME"), JLabel.CENTER);
			this.homeView.add(lbl, BorderLayout.NORTH);

			str = Labels.html(Labels.colour("Made by Nox", "#aaaaaa"));
			lbl = new JLabel(str, JLabel.CENTER);
			this.homeView.add(lbl, BorderLayout.SOUTH);

			this.homeView.setName("VIEW_HOME");
			this.add(this.homeView, this.homeView.getName());
		}

		JComponent recent;
		recent = (JPanel) this.homeView.getComponent(0); // Button panel
		recent = (JComponent) recent.getComponent(1);    // GAME_OLD btn
		recent.setVisible(this.fieldsModel.getSize()>0);
	
		this.layout.show(this.getContentPane(), this.homeView.getName());
		this.currView = this.homeView.getName();
		this.getJMenuBar().setVisible(false);
	}


	/**
	 * Shwo the Game Panel.
	 */
	private void showGame()
	{
		if (this.gameView == null)
		{
			ActionListener timerAL = new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Minesweeper.this.showGameTime();
				}
			};

			ComponentListener cl = new ComponentListener()
			{
				public void componentHidden(ComponentEvent e)
				{
					String name = e.getComponent().getName();
					if (name != null && name.equals(Minesweeper.this.gameView.getName()))
					{
						GameField curr = Minesweeper.this.currGame;
						if (curr != null)
						{
							curr.pause();
						}
					}
				}

				public void componentMoved(ComponentEvent e)
				{}

				public void componentResized(ComponentEvent e)
				{
					Minesweeper.this.matchSizes();
				}

				public void componentShown(ComponentEvent e)
				{}
			};

			Color fg = Color.BLUE;
			Color bg = Color.WHITE;

			this.gameTimer = new Timer(100, timerAL);
			this.gameLabel = new JLabel(ls.get("LBL_MINES"), JLabel.LEFT);
			this.timeLabel = new JLabel(ls.get("LBL_TIME"), JLabel.RIGHT);

			this.giveUpBtn = this.createBtn(":(",this,"GAME_RESTART");
			this.giveUpBtn.setToolTipText("This will end this game automatically.");

			this.gameLabel.setForeground(fg);
			this.timeLabel.setForeground(fg);
			this.giveUpBtn.setForeground(fg);

			JPanel topPanel = new JPanel();
			topPanel.setBackground(bg);
			topPanel.setLayout(new GridLayout(1,3));
			topPanel.add(this.gameLabel);
			topPanel.add(this.giveUpBtn);
			topPanel.add(this.timeLabel);

			this.gameView = new JPanel(new BorderLayout());
			this.gameView.setOpaque(false); // transparent.
			this.gameView.add(topPanel, BorderLayout.NORTH);
			this.gameView.addComponentListener(cl);
			this.gameView.setName("VIEW_GAME");
			this.add(this.gameView, this.gameView.getName());
		}

		this.layout.show(this.getContentPane(), this.gameView.getName());
		this.currView = this.gameView.getName();
		this.getJMenuBar().setVisible(true);
		this.prefsMenu.setVisible(false);
	}


	/**
	 * Select a game.
	 * @param existing select an existing game, else start a new game.
	 */
	private void showFieldSelector(boolean existing)
	{
		if (this.selectorView == null)
		{
			this.selectorView = new JPanel();
			this.selectorView.setOpaque(false);
			this.selectorView.setName("VIEW_SELECTOR");
			this.add(this.selectorView, this.selectorView.getName());
			
			this.selectorView.setLayout(new BorderLayout());

			JPanel  pnl;
			JLabel  lbl;

			lbl = new JLabel(ls.get("TITLE_SEL"), JLabel.CENTER);
			lbl.setAlignmentX(Component.CENTER_ALIGNMENT);

			pnl = new JPanel(new GridLayout(1,2));
			pnl.setOpaque(false);
			pnl.add(this.createCreator()); //initated sliders
			//pnl.add(this.createSelector()); // iniate list

			this.selectorView.add(lbl, BorderLayout.NORTH);
			this.selectorView.add(pnl);
		}

		if (existing && 0<this.fieldsModel.getSize()) // resume the most recent game.
		{
			this.openGame(this.fieldsModel.getElementAt(0));
			return;
		}

		this.layout.show(this.getContentPane(), this.selectorView.getName());
		this.currView = this.selectorView.getName();
		this.getJMenuBar().setVisible(true);
		this.prefsMenu.setVisible(true);
	}


	/**
	 * Show the statistics.
	 */
	private void showStatistics()
	{
		if (this.statsView == null)
		{
			JEditorPane text;  // scrollable text output for stats.
			int         vsPol; // vertical bar policy

			vsPol = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED;

			text = new JEditorPane("text/html", ls.get("INFO_NO_GAMES"));
			text.setEditable(false);

			this.statsView = new JScrollPane(text);
			((JScrollPane)statsView).setVerticalScrollBarPolicy(vsPol);

			this.statsView.setName("VIEW_STATISTICS");
			this.add(this.statsView, this.statsView.getName());
		}

		GameField g;
		String s     = "";
		String t     ="%.3f s";
		double milli = 1E-3;
		int    won   = 0;

		for (int i=0; i<this.fieldsModel.getSize(); i++)
		{
			g       = this.fieldsModel.getElementAt(i);

			s += String.format(
					ls.get("TABLE_STATS"),
					g.toString(),
					g.countGames(false),
					(won=g.countGames(true)),
					g.winStreak(),
					(won< 1) ? "Nothing won!" : String.format(t, (g.getTime(false)*milli)),
					(won< 1) ? "Nothing won!" : String.format(t, (g.getTime(true)*milli))
					);
		}

		JComponent c0 = (JComponent) this.statsView.getComponent(0);
		((JEditorPane)c0.getComponent(0)).setText(Labels.html(s));

		this.layout.show(this.getContentPane(),this.statsView.getName());
		this.currView =this.statsView.getName();
		this.getJMenuBar().setVisible(true);
		this.prefsMenu.setVisible(true);
	}


	/**
	 * Get the Give Up Button.
	 * @return giveUpBtn as JButton.
	 */
	protected JButton getGiveUpButton()
	{
		return this.giveUpBtn;
	}


	/**
	 * Display information about currently played game.
	 */
	protected void updateGameLabel()
	{
		if (this.gameLabel == null)
		{
			this.gameLabel = new JLabel();
			this.gameView.add(this.gameLabel, BorderLayout.NORTH);
		}

		if (this.currGame == null)
		{
			this.gameLabel.setText("");
			return;
		}

		String state;
		state = this.currGame.getMines(false)+"/"+this.currGame.getMines(true);
		
		this.gameLabel.setText(state);
		this.showGameTime();
	}


	/**
	 * If a game is currently open, then show the the player needs to solve this game.
	 */
	private void showGameTime()
	{
		if (this.currGame == null || !this.currGame.isRunning()
				|| !this.currView.equals(this.gameView.getName()))
		{
			return;
		}


		String f = "%.2f s";
		double played = this.currGame.playedTime() * 1E-9;
		this.timeLabel.setText(String.format(f, played));
	}


	/**
	 * End the current game and start a new game with in previous field.
	 */
	private void restartCurrent()
	{
		if (this.currGame == null)
		{
			return;
		}

		this.currGame.restart();
	}


	/**
	 * Open a game as currGame.
	 * @param game 
	 */
	private void openGame(GameField game)
	{
		if (game == null) // back to home view.
		{
			this.showHome();
			return;
		}

		this.showGame();

		try // try to reset the current game view.
		{
			this.gameView.remove(this.currGame);
		}
		catch (NullPointerException e)
		{}

		DefaultListModel<GameField> fieldsM;
		fieldsM = (DefaultListModel<GameField>) this.fieldsModel;

		if (fieldsM.contains(game)) // use known field.
		{
			game = fieldsM.remove(fieldsM.indexOf(game));
		}

		fieldsM.add(0, game); // append to start (most recent).

		if (this.fieldSelector==null && this.selectorView!=null) // first game
		{
			((JPanel)this.selectorView.getComponent(1))
				.add(this.createSelector());
		}

		this.currGame = game;
		this.currGame.setHost(this);
		this.gameView.add(this.currGame, BorderLayout.CENTER);

		this.updateGameLabel();
		this.gameTimer.start();

		if (this.currGame.isRunning()) // don't wait for click to resume.
		{
			this.currGame.resume();
		}

		this.revalidate();
		this.repaint();
	}


	/**
	 * If a game is showed, try to match this frame to game field.
	 * The positions have to be squares.
	 */
	private void matchSizes()
	{
		if (this.currGame==null || this.gameView==null || !this.gameView.getName().equals(this.currView))
		{
			return;
		}

		Dimension posSize = this.currGame.getPosSize();

		if (posSize.width == posSize.height)
		{
			return;
		}

		GridLayout layout = (GridLayout) ((JPanel)this.currGame).getLayout();

		int pos = (posSize.width + posSize.height)/2;
		int w   = layout.getColumns()*pos - this.currGame.getWidth();
		int h   = layout.getRows()   *pos - this.currGame.getHeight();

		this.setBounds(this.getX(), this.getY(), this.getWidth()+w, this.getHeight()+h);
	}


	/**
	 * Create a white button (not like the default).
	 * @param t text
	 * @param l ActionListener
	 * @param n JComponent.setName()
	 * @return JButton with attributes
	 */
	private JButton createBtn(String t, ActionListener l, String n)
	{
		JButton btn;
		btn = new JButton(t);
		btn.setOpaque(false);
		btn.setContentAreaFilled(false);
		btn.setName(n);
		btn.addActionListener(l);
		return btn;
	}


	/**
	 * Capsulated slider creator.
	 * @param title Border title
	 * @param name  JComponent.setName()
	 * @return 
	 */
	private JSlider createSlider(String title, String name)
	{
		JSlider slider = new JSlider(Field.MIN, this.max, 1);
		slider.setMajorTickSpacing(10);
		slider.setMinorTickSpacing(1);
		slider.setName(name);
		slider.setOpaque(false);
		slider.setPaintTicks(true);
		slider.setBorder(BorderFactory.createTitledBorder(title));
		return slider;
	}


	/**
	 * Creates the panel to create a new game (with custom dimension and mines count).
	 * @return JPanel with three sliders and a create button.
	 */
	private JPanel createCreator()
	{
		JPanel  pnl, top;
		String  str = null;

		pnl = new JPanel();
		pnl.setOpaque(false);
		pnl.setAlignmentX(Component.CENTER_ALIGNMENT);

		wSlider = this.createSlider(ls.get("SLIDER_WIDTH"), "SLIDER_WIDTH");
		wSlider.setMinimum(2);
		wSlider.setValue(9);
		pnl.add(wSlider);

		hSlider = this.createSlider(ls.get("SLIDER_HEIGHT"), "SLIDER_HEIGHT");
		hSlider.setValue(9);
		pnl.add(hSlider);

		mSlider = this.createSlider(ls.get("SLIDER_MINES"), "SLIDER_MINES");
		mSlider.setValue(10);
		pnl.add(mSlider);

		str = String.format(ls.get("BTN_CUSTOM"),
				wSlider.getValue(), hSlider.getValue(), mSlider.getValue());

		final JButton btn = this.createBtn(str, this, "CREATE_GAME");

		top = new JPanel(new BorderLayout());
		top.setOpaque(false);
		top.add(pnl);
		top.add(btn, BorderLayout.SOUTH);
		top.setBorder(BorderFactory.createTitledBorder(ls.get("TITLE_CUSTOM")));

		ChangeListener forMines = new ChangeListener()
		{
			@Override public void stateChanged(ChangeEvent e)
			{
				int max, h,w,m;
				h = Minesweeper.this.wSlider.getValue();
				w = Minesweeper.this.hSlider.getValue();
				m = Minesweeper.this.mSlider.getValue();

				max = h*w;
				max = (2<max) ? max-1 : 1;

				Minesweeper.this.mSlider.setMaximum(max);

				String str;
				str = ls.get("BTN_CUSTOM");
				str = String.format(str, h, w, m);
				btn.setText(str);
			}
		};

		hSlider.addChangeListener(forMines);
		wSlider.addChangeListener(forMines);
		mSlider.addChangeListener(forMines);

		return top;
	}


	/**
	 * Create the selector and panel view for the known fields.
	 * @return JPanel with JList (with knwon lists) and an open button.
	 */
	private JPanel createSelector()
	{
		this.fieldSelector = new JList<GameField>(this.fieldsModel);
		this.fieldSelector.setLayoutOrientation(JList.VERTICAL);

		JScrollPane lstScoller = new JScrollPane(this.fieldSelector);
		lstScoller.setOpaque(false);
		lstScoller.setBorder(null);

		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.setBorder(BorderFactory.createTitledBorder(ls.get("TITLE_SELECT")));
		panel.add(lstScoller);
		panel.add(this.createBtn("Open this Field", this, "GAME_LIST"),
				BorderLayout.SOUTH);
		return panel;
	}
	

	/**
	 * Initate the Menubar and its items.
	 * Home.
	 * (Almost always hidden) Giveup.
	 */
	private void initiateMenu()
	{
		if (this.getJMenuBar() != null)
		{
			return;
		}

		JMenuBar  menuBar;
		JMenuItem menuItem;
		String    str;

		menuBar = new JMenuBar();
		menuBar.setOpaque(false);

		/*Return to home view in other views.*/
		menuItem = this.createJMenuItem(ls.get("BTN_BACK"), this, "GO_HOME");
		menuBar.add(menuItem);

		this.prefsMenu = new JMenu(ls.get("MENU_PREFERENCES"));
		this.prefsMenu.setMnemonic(KeyEvent.VK_P);

		menuItem = this.createJMenuItem(ls.get("MENU_EXPORT"), this, "FIELDS_EXPORT");
		this.prefsMenu.add(menuItem);

		menuItem = this.createJMenuItem(ls.get("MENU_IMPORT"), this, "FIELDS_IMPORT");
		this.prefsMenu.add(menuItem);

		menuBar.add(this.prefsMenu);

		this.setJMenuBar(menuBar);
	}


	/**
	 * Create a new JMenuItem with given title, Action Listener, and name.
	 * @param t    title
	 * @param l    ActionListener
	 * @param name JComponent name
	 * @return     JMenuItem
	 */
	private JMenuItem createJMenuItem(String t, ActionListener l, String name)
	{
		JMenuItem menuItem = new JMenuItem(t);
		menuItem.setName(name);
		menuItem.addActionListener(l);
		return menuItem;
	}


	public static void main(String[] args)
	{
		Minesweeper gui = new Minesweeper(600,400);
	}
}
