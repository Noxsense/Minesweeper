package nox.minesweeper.android;


import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


public class SelectGameActivity extends Activity implements OnClickListener
{
	private TextView      titleView;
	private ListView      gamesView;
	private GamesAdapter  gamesAdapter;

	private Button        createGame;
	private Dialog        dialogCreateGame;
	private SeekBar       inGameHeight;
	private SeekBar       inGameWidth;
	private SeekBar       inGameMines;

	private static int    HEIGHT_MIN = 1;
	private static int    MINES_MIN  = 1;
	private static int    WIDTH_MIN  = 2;

	private final static int HEIGHT = 0;
	private final static int WIDTH  = 1;
	private final static int MINES  = 2;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.list_games);

		this.titleView  = (TextView) findViewById(R.id.title);
		this.gamesView  = (ListView) findViewById(R.id.list);
		this.createGame = (Button)   findViewById(R.id.button);

		this.titleView.setText(getString(R.string.select_game));

		this.gamesView.setAdapter(
				this.gamesAdapter = new GamesAdapter(this.getLayoutInflater()));

		this.createGame.setText(getString(R.string.custom_game));
		this.createGame.setVisibility(View.VISIBLE);
		this.createGame.setOnClickListener(this);
	}


	@Override
	public void onStart()
	{
		super.onStart();

		/*Create the first game.*/
		if (GameMaster.getInstance().size()<1)
		{
			this.createGame.performClick();
		}
	}


	@Override
	public void onClick(View view)
	{
		try
		{
			if (view.equals(createGame))
			{
				this.getCreateGameDialog().show();
				this.initInputs();
			}
		}
		catch (Exception e)
		{
			Toast.makeText(this,e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * Create the custom Game with readed inputput from the dialog.
	 */
	private void createCustomGame()
	{
		try
		{
			GameMaster master = GameMaster.getInstance();
			int[]      in     = this.getInputs();

			Game game = master.getGameWith(in[HEIGHT],in[WIDTH],in[MINES]);
			Toast.makeText(this, "New Game created: "+game, Toast.LENGTH_LONG).show();
			this.gamesAdapter.notifyDataSetChanged();
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * Get the sliders' progress (input for game's width, height and mines).
	 * @return inputs as int array.
	 */
	private int[] getInputs()
	{
		int[] input = new int[3];
		input[HEIGHT] = HEIGHT_MIN;
		input[WIDTH]  = WIDTH_MIN;
		input[MINES]  = MINES_MIN;

		try
		{
			input[HEIGHT] += this.inGameHeight.getProgress();
			input[WIDTH]  += this.inGameWidth.getProgress();
			input[MINES]  += this.inGameMines.getProgress();
		}
		catch (NullPointerException e)
		{}
		return input;
	}


	/**
	 * Initate the dialog inpus: Game height, width and mines.
	 */
	private void initInputs()
	{
		if (this.dialogCreateGame==null || this.inGameHeight!=null)
			return;

		this.inGameHeight = (SeekBar) this.dialogCreateGame.findViewById(R.id.in_game_height);
		this.inGameWidth  = (SeekBar) this.dialogCreateGame.findViewById(R.id.in_game_width);
		this.inGameMines  = (SeekBar) this.dialogCreateGame.findViewById(R.id.in_game_mines);

		final String BUTTON_TXT
			= "Create Game with %d:%d with %d mines";

		OnSeekBarChangeListener l = new OnSeekBarChangeListener()
		{
			private final static int HEIGHT = 0;
			private final static int WIDTH  = 1;
			private final static int MINES  = 2;


			@Override public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser)
			{
				int[] in = SelectGameActivity.this.getInputs();

				String str;
				str = String.format(BUTTON_TXT, in[HEIGHT],in[WIDTH],in[MINES]);

				((AlertDialog)SelectGameActivity.this.dialogCreateGame)
					.getButton(Dialog.BUTTON1) //BUTTON1 is positive button
					.setText(str);

				/*Do nothing special.*/
				if (seekbar.equals(SelectGameActivity.this.inGameMines))
					return;

				SelectGameActivity.this.inGameMines.setMax(in[HEIGHT]+in[WIDTH]-1);
			}

			@Override public void onStartTrackingTouch(SeekBar seekbar)
			{
			}

			@Override public void onStopTrackingTouch(SeekBar seekbar)
			{
			}
		};

		this.inGameHeight.setOnSeekBarChangeListener(l);
		this.inGameWidth.setOnSeekBarChangeListener(l);
		this.inGameMines.setOnSeekBarChangeListener(l);

		/*Imitate the first move to set min of mines and button text.*/
		this.inGameWidth.incrementProgressBy(1);
		this.inGameWidth.incrementProgressBy(-1);
	}


	/**
	 * Get the dialog to create a new game format.
	 * @return 
	 */
	public Dialog getCreateGameDialog()
	{
		if (this.dialogCreateGame != null)
		{
			return this.dialogCreateGame;
		}

		DialogInterface.OnClickListener l = new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int button)
			{
				if (button == Dialog.BUTTON_NEGATIVE)
					return;
				SelectGameActivity.this.createCustomGame();
			}
		};

		this.dialogCreateGame = new AlertDialog.Builder(this)
			.setView(getLayoutInflater().inflate(R.layout.create_custom_game, null))
			.setPositiveButton(getString(R.string.accept), l)
			.create()
			;
		return this.dialogCreateGame;
	}
}
