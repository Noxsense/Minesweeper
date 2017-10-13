package nox.minesweeper.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Class MainActivity.
 * First View while opening the application.
 * Offers an menu with
 * - new game
 * - resume game
 * - open statistics
 */
public class MainActivity extends Activity implements OnClickListener
{
	private Button selectGame;
	private Button recentGame;
	private Button statistics;

	private GameMaster master;
	//private Button about;
	private static String SAVED_MASTER = "minesweeper_game_master.txt";


	/**
	 * Find the buttons in the view.
	 */
	protected void findButtons()
	{
		this.selectGame = (Button) findViewById(R.id.btn_sel_game);
		this.recentGame = (Button) findViewById(R.id.btn_recent_game);
		this.statistics = (Button) findViewById(R.id.btn_statistics);

		this.selectGame.setOnClickListener(this);
		this.statistics.setOnClickListener(this);
		this.recentGame.setOnClickListener(this);
		this.recentGame.setVisibility(View.GONE);
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.home);

		this.master = GameMaster.getInstance();

		this.findButtons();
	}


	@Override
	public void onResume()
	{
		super.onStart();

		try
		{
			this.loadMaster(SAVED_MASTER);

			int v = (0<master.getGames().size()) ? View.VISIBLE : View.GONE;
			this.recentGame.setVisibility(v);
		}
		catch (IOException fnfe) // no such file
		{
		}
		catch (Exception e) // possible not initated.
		{
			this.findButtons();
			this.recentGame.setVisibility(View.GONE);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


	@Override
	public void onPause()
	{
		super.onPause();

		/*Save game stati.*/
		try
		{
			this.saveMaster(SAVED_MASTER);
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}


	/**
	 * Load the GameMaster from an internal file. 
	 * @param filename of internal file
	 * @throws Exception IOException
	 */
	private void loadMaster(String filename) throws Exception
	{
		FileInputStream fis;
		StringBuffer    content;
		byte[]          buf;

		buf     = new byte[1024];
		content = new StringBuffer("");

		fis = openFileInput(SAVED_MASTER);
		while (fis.read(buf) != -1)
		{
			content.append(new String(buf));
		}
		fis.close();

		if (content.length()<1) return; // nothing to load

		for (String line : content.toString().split("\\n"))
		{
			master.loadGame(line);
		}
	}


	/**
	 * Saves the GameMaster into an internal file. 
	 * @param filename of internal file
	 * @throws Exception IOException
	 */
	private void saveMaster(String filename) throws Exception
	{
		String info;
		FileOutputStream fos;

		info = master.printAllGames();
		fos  = openFileOutput(filename, Context.MODE_PRIVATE);
		fos.write(info.getBytes());
		fos.close();
	}


	@Override
	public void onClick(View view)
	{
		try
		{
			Intent intent = null;

			/** Opens the view to select a (new) game.*/
			if (view.equals(selectGame))
			{
				intent = new Intent(this, SelectGameActivity.class);
			}

			/** Opens the game view with the most recent game.  */
			else if (view.equals(recentGame))
			{
				intent = new Intent(this, PlayActivity.class);
			}

			/** Opens the statisitcs view.*/
			else if (view.equals(statistics))
			{
				intent = new Intent(this, StatisticsActivity.class);
			}

			if (intent==null) // nothing to do.
				return;

			this.startActivity(intent);
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
