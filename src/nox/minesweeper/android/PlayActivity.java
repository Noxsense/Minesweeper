package nox.minesweeper.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.ArrayList;

import nox.minesweeper.logic.Field;


public class PlayActivity extends Activity
{
	private View     gameView;
	private TextView infoText;
	private TextView timeText;

	private Game     game;

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

				return false;
			}

			float x,y;
			int action, pointer, cell;
			Game game;

			game = PlayActivity.this.game;

			action  = e.getAction();
			pointer = action >> MotionEvent.ACTION_POINTER_ID_SHIFT;

			x    = e.getX(pointer);
			y    = e.getY(pointer);
			cell = this.pos2Index(x,y,game);

			/*Invalid position; event consumed.*/
			if (cell < 0)
			{
				return true;
			}

			/*Handle event actions.*/
			switch (e.getActionMasked())
			{
				/* Initial meant position to edit.*/
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:

					// add pseudo aims to override
					while (this.aimedPos.size()<=pointer)
					{
						this.aimedPos.add(-1);
					}

					this.aimedPos.set(pointer, cell);

					return true;


				/* Check if the pointer is still referring the aimed position.
				 * If not, cancel the edit.*/
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:

					/*Nothing aimed => Nothing to do.*/
					if (this.aimedPos.isEmpty())
					{
						return true;
					}

					int aim;
					aim  = this.aimedPos.remove(pointer);

					/*Cancel, it's not the aimed position anymore.*/
					if (cell < 0 || cell != aim)
					{
						return true;
					}

					/*Finish handling for move.*/
					if (action == MotionEvent.ACTION_MOVE)
					{
						this.aimedPos.add(pointer, aim); // readd
						return true;
					}

					// do sth with cell
					game.open(cell);

					this.invalidate();
					PlayActivity.this.showInfo();

					return true;

				default:
					break;
			}

			return true;
		}


		/**
		 * Get the gap between cells.
		 * @return 
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
		 * @param index 
		 * @return array of coords.
		 */
		private int[] translateIndex(Game game, int index) throws ArrayIndexOutOfBoundsException
		{
			if (game==null || index<0  || index>= game.field.size())
			{
				throw new ArrayIndexOutOfBoundsException("Index or game invalid");
			}

			int[] coords = new int[2];

			coords[ROW]    = index/game.field.getHeight(); // row
			coords[COL]    = index%game.field.getHeight(); // column

			return coords;
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
		layout.addView(this.gameView,
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
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
		}

		this.showInfo();
	}


	/**
	 * Display the info of the current game.
	 */
	private void showInfo()
	{
		if (this.game==null)
			return;

		String str;
		str = "Cells: "
			+ this.game.discovered()+"/"+this.game.field.size() + ", "
			+ "Mines: "
			+ this.game.field.getMarked()+"/"+this.game.mines;

		this.infoText.setText(str);
		this.timeText.setText("Time: "+this.game.getTime(Game.PLAYED_TIME));
	}
}
