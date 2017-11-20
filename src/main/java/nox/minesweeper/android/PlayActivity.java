package nox.minesweeper.android;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import nox.minesweeper.logic.Field;
import nox.minesweeper.logic.Game;
import nox.minesweeper.logic.Statistic;


public class PlayActivity extends Activity implements DialogInterface.OnClickListener
{
	private View     gameView;
	private TextView infoText;
	private TextView timeText;

	private Game     game;

	private Dialog   restartGame;

	private CustomScollView scroller;


	/**
	 * Class GameView.
	 * Represents the games.
	 */
	private class GameView extends View implements View.OnTouchListener
	{
		private Paint   paintText;
		private Paint   paintClosed;
		private Paint[] paintOpened;

		private float   cellSize;
		private float   cellGap;
		public final static float STANDARD_GAP = -1;

		private final static int ROW = 0;
		private final static int COL = 1;

		private List<Integer> aimedPos; // instead of int[]


		/**
		 * Create a new Gameview.
		 * Initate the colours and anything.
		 * @param context
		 */
		public GameView(Context context)
		{
			super(context);

			this.setOnTouchListener(this);
			this.aimedPos = new ArrayList<Integer>();

			this.cellSize = 20;
			this.cellGap  = GameView.STANDARD_GAP;

			this.paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.paintText.setARGB(255, 255, 255, 255);
			this.paintText.setTextAlign(Paint.Align.CENTER);

			this.paintClosed = new Paint(Paint.ANTI_ALIAS_FLAG);
			this.paintClosed.setARGB(255, 122, 122, 122);
			this.paintClosed.setStyle(Paint.Style.STROKE);

			this.paintOpened = new Paint[9];

			for (int i=0; i < this.paintOpened.length; i++)
			{
				this.paintOpened[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
				this.paintOpened[i].setARGB((31*i)%255, 255, 255, 255);
			}
		}


		@Override
		public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			Game game = PlayActivity.this.game;
			if (game==null)
			{
				super.onMeasure(widthMeasureSpec, heightMeasureSpec);
				return;
			}

			double width, height, gap;
			gap    = this.getCellGap();
			width  = game.field.getWidth()*(this.cellSize+gap) - gap;
			height = game.field.getHeight()*(this.cellSize+gap)- gap;

			this.setMeasuredDimension(
					(int)Math.round(width),
					(int)Math.round(height));
		}


		@Override
		public void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);

			Game game = PlayActivity.this.game;
			if (game==null)
				return;

			float[] coords; // coordinations where to paint.
			float   gap;      // gap between  cells
			int     mines;    // known mines for the field position
			Paint   paintI;   // how to paint.
			String  text;

			gap = this.getCellGap();

			// TODO offset and padding, scaling and so on.
			for (int i=0; i<game.field.size(); i++)
			{
				coords = this.coords2Pos(this.translateIndex(game, i), gap);

				mines  = game.field.onPosition(i);

				paintI = this.paintClosed;
				text   = null;

				switch (mines)
				{
					case Field.VALUE_CLOSED: // do nothing else
						break;

					case Field.VALUE_MINE_ON_POS: // mine display
						text = ""+Field.getDisplay(Field.DISPLAY_MINE);
						break;

					case Field.VALUE_MARKED: // mark display
						text = ""+Field.getDisplay(Field.DISPLAY_MARKED);
						break;

					default: // open and safe display.
						paintI = this.paintOpened[mines];
						text   = (mines==0) ? "" : String.valueOf(mines);
				}

				canvas.drawRect(coords[COL], coords[ROW],
						coords[COL]+cellSize, coords[ROW]+cellSize,
						paintI);

				if (text==null)
					continue;

				/*Center Text in Rectangle.*/
				float w, h;

				w = coords[COL]+this.cellSize/2;
				h = coords[ROW]+(this.cellSize+this.paintText.getTextSize())/2;

				canvas.drawText(text, w, h, this.paintText);
			}
		}


		@Override
		public boolean onTouch(View view, MotionEvent e)
		{
			if (view==null || e==null)
			{
				this.aimedPos.clear();

				return true; // consumed
			}

			float x,y;
			int action, pointer, cell, aimed;
			Game game;
			boolean scrolling;

			game = PlayActivity.this.game;
			scrolling = true;

			/*Start a new game option, else nothing to do?*/
			if (game.field.isLost() || game.field.isWon())
			{
				PlayActivity.this.getRestartDialog().show();
				return true; // game is done, maybe scrolling.
			}

			action  = e.getAction();
			pointer = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;
			action  = action & MotionEvent.ACTION_MASK;

			x    = e.getX(pointer);
			y    = e.getY(pointer);
			cell = this.pos2Index(x,y,game);

			/*Invalid position; event consumed.*/
			if (cell < 0)
			{
				return false; // invalid position, but maybe scrolling.
			}

			/*Start: Aim for certain pointer selected.*/
			if (this.isActionDown(action))
			{
				while (this.aimedPos.size()<=pointer)
				{
					this.aimedPos.add(-1);
				}

				this.aimedPos.set(pointer, cell);
				Toast.makeText(this.getContext(), System.currentTimeMillis()+": Aim "+cell+"!", Toast.LENGTH_SHORT).show();
				return false;
			}

			/*Aim for certain pointer changed => Scrolled view.*/
			if (cell != this.aimedPos.get(pointer)) // aim changed => scrolling
			{
				Toast.makeText(this.getContext(), "Changed. Scroll!", Toast.LENGTH_SHORT).show();
				return false;
			}

			/*End: Toggle mark or open aimed position.*/
			if (this.isActionUp(action))
			{
				cell = this.aimedPos.remove(pointer);

				if (this.isToggleMarkEvent(e)) // toggle
				{
					game.toggleMark(cell);
				}
				else // open
				{
					game.open(cell);
				}

				this.invalidate();
				PlayActivity.this.showInfo();

				return true;
			}

			return true;
		}


		/**
		 * Check if the MotionEvent action is down.
		 * @param action MotionEvent action.
		 * @return true, if action is down or pointer down.
		 */
		private boolean isActionDown(int action)
		{
			return action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_POINTER_DOWN;
		}


		/**
		 * Check if the MotionEvent action is up.
		 * @param action MotionEvent action.
		 * @return true, if action is up or pointer up.
		 */
		private boolean isActionUp(int action)
		{
			return action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_POINTER_UP;
		}


		/**
		 * Check if the given mouse event was a toggle action.
		 * @param e MouseEvent
		 * @return true, if the action should be toggle.
		 */
		private boolean isToggleMarkEvent(MotionEvent e)
		{
			return e != null
				&& false;
		}


		/**
		 * Get the gap between cells.
		 * @return the gap between cells
		 */
		private float getCellGap()
		{
			return (this.cellGap<0) ? this.cellSize/4 : this.cellGap;
		}


		/**
		 * Translate the given field coordinates to a canvas position.
		 * @param coords cooridinates to translate.
		 * @param gap    gap between cells.
		 * @return canvas position as float array.
		 */
		private float[] coords2Pos(int[] coords, float gap) throws NullPointerException
		{
			float[] pos = new float[coords.length];
			pos[ROW] = coords[ROW]*(cellSize + gap);
			pos[COL] = coords[COL]*(cellSize + gap);
			return pos;
		}


		/**
		 * Get the index of the positon which may be on the given coordinates.
		 * @param x canvas coordinate: Aiming row.
		 * @param y canvas coordinate: Aiming column.
		 * @param game game with field.
		 * @return index of position, -1 if there is no position on the given point.
		 */
		private int pos2Index(float x, float y, Game game)
		{
			if (game==null || x<0 || y< 0)
				return -1;

			//TODO
			int   index, row, col;
			float gap = this.getCellGap();

			row = (int) (y / (cellSize + gap));
			col = (int) (x / (cellSize + gap));

			index = row*game.field.getWidth() + col;

			return (index<game.field.size()) ? index : -1;
		}


		/**
		 * Get the row and column of the given Index in the given field.
		 * @param index of a position in the game field.
		 * @return array of coords.
		 */
		private int[] translateIndex(Game game, int index) throws ArrayIndexOutOfBoundsException
		{
			if (game==null || index<0  || index>= game.field.size())
			{
				throw new ArrayIndexOutOfBoundsException("Index or game invalid");
			}

			int[] coords = new int[2];

			coords[ROW]    = index/game.field.getWidth(); // row
			coords[COL]    = index%game.field.getWidth(); // column

			return coords;
		}
	}


	/**
	 * Class CustomScollView.
	 * Nesting of horizontal and vertical scroll with custom MotionEvent handling.
	 */
	private static class CustomScollView extends HorizontalScrollView
	{
		private ScrollView innerScroll;;

		private float x;
		private float y;

		/**
		 * Initiate a new CustomScollView.
		 * @param context
		 * @throws NullPointerException
		 */
		private CustomScollView(Context context) throws NullPointerException
		{
			super(context);

			this.innerScroll = new ScrollView(context)
			{
				@Override
				public boolean onTouchEvent(MotionEvent e)
				{
					return CustomScollView.this.onTouchEvent(e);
				}
			};

			if (this.innerScroll == null)
			{
				throw new NullPointerException("No vertical scroll");
			}

			super.addView(this.innerScroll);
		}


		/**
		 * Initiate a new CustomScollView.
		 * @param context
		 * @param child
		 * @throws NullPointerException
		 */
		public CustomScollView(Context context, View child) throws NullPointerException
		{
			this(context);
			//this.innerScroll.addView(child);
			this.addView(child);

			if (child == null || context == null)
			{
				throw new NullPointerException();
			}
		}


		@Override
		public boolean onTouchEvent(MotionEvent e)
		{
			if (e==null)
			{
				return true;
			}

			/*local x,y coordinates.*/
			float x = e.getX();
			float y = e.getY();

			int action = e.getAction();

			/*Scroll if its moving.*/
			if (action == MotionEvent.ACTION_MOVE)
			{
				this.scrollBy(
						(int) Math.round(this.x-x),
						(int) Math.round(this.y-y));
			}

			/*Store current Position if pointer is still down.*/
			this.x = x;
			this.y = y;

			return true;
		}


		//@Override
		//public void scrollTo(int x, int y)
		//{
			//super.scrollTo(x,y);
			//this.innerScroll.scrollTo(x,y);
		//}


		@Override
		public void scrollBy(int x, int y)
		{
			super.scrollBy(x,y);
			this.innerScroll.scrollBy(x,y);
		}


		@Override
		public void addView(View child, int index, ViewGroup.LayoutParams params)
		{
			if (child == null || child.equals(this.innerScroll))
			{
				super.addView(this.innerScroll, index, params);
				return;
			}
			this.innerScroll.addView(child, index, params);
		}


		@Override
		public void addView(View child, ViewGroup.LayoutParams params)
		{
			this.addView(child, 0, params);
		}


		@Override
		public void addView(View child, int index)
		{
			this.addView(child, index, this.generateDefaultLayoutParams());
		}


		@Override
		public void addView(View child)
		{
			this.addView(child, 0);
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//if (true) return; // skip old part.

		LinearLayout layout = (LinearLayout) this.getLayoutInflater()
			.inflate(R.layout.view_game, null);

		this.infoText = (TextView) layout.findViewById(R.id.txt_game_info);
		this.timeText = (TextView) layout.findViewById(R.id.txt_time);

		this.gameView = new GameView(this);
		this.gameView.invalidate();

		/*Custom scroll views for horizontal and vertical scrolling.*/
		layout.addView(scroller = new CustomScollView(this, this.gameView),
		//layout.addView(this.gameView,
				new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, // width
					LayoutParams.FILL_PARENT, // height
					1 // weight
					));

		this.setContentView(layout);
	}


	@Override
	public void onResume()
	{
		super.onResume();

		GameMaster master = GameMaster.getInstance();
		try
		{
			this.game = master.get(0);

			/*Resume if already started.*/
			this.game.resume();
		}
		catch (Exception e)
		{}

		this.showInfo();
	}


	@Override
	public void onPause()
	{
		super.onPause();

		if (this.game==null)
			return;

		this.game.pause(); // pause the current game.

		try
		{
			MainActivity.saveMaster(this,MainActivity.SAVED_MASTER);
		}
		catch (Exception e)
		{}
	}


	/**
	 * Display the info of the current game.
	 */
	private void showInfo()
	{
		if (this.game==null)
			return;

		String str;
		str = String.format(getString(R.string.current_game_info
					, this.game.discovered(),this.game.field.size()
					, this.game.field.getMarked(),this.game.mines
					));
		this.infoText.setText(str);

		this.showTimeInfo();

		if (this.game.field.isWon())
		{
			Toast.makeText(this, getString(R.string.WON), Toast.LENGTH_SHORT).show();
		}
		if (this.game.field.isLost())
		{
			Toast.makeText(this, getString(R.string.LOST), Toast.LENGTH_SHORT).show();
		}
	}


	/**
	 * Display the current time used to play this game.
	 */
	private void showTimeInfo()
	{
		String str = getString(R.string.current_game_time);

		str = String.format(str, this.game.getTime(Game.PLAYED_TIME)*1e-3);

		this.timeText.setText(str);
	}


	/**
	 * Get the dialog to restart the game.
	 * @return dialog with accept button.
	 */
	private Dialog getRestartDialog()
	{
		if (this.restartGame != null)
		{
			return this.restartGame;
		}

		/*Initate Dialog.*/
		this.restartGame = new AlertDialog.Builder(this)
			.setTitle(R.string.restart_game)
			//.setMessage(R.string.restart_game_warning)
			.setPositiveButton(R.string.accept, this)
			.create();

		return this.getRestartDialog();
	}


	@Override
	public void onClick(DialogInterface dialog, int button)
	{
		if (button != Dialog.BUTTON_POSITIVE || this.game == null)
		{
			return;
		}

		/*Restart the game and redraw the game view.*/
		this.game.restart();
		this.gameView.invalidate();
		this.showInfo();
	}
}
