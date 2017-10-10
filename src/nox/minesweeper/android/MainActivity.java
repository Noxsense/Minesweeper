package nox.minesweeper.android;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


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

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.home);

		this.master = GameMaster.getInstance();

		this.findButtons();
	}


	@Override
	public void onStart()
	{
		super.onStart();

		try
		{
			int v = (0<master.getGames().size()) ? View.VISIBLE : View.GONE;
			this.recentGame.setVisibility(v);
		}
		catch (NullPointerException e) // possible not initated.
		{
			this.findButtons();
		}
	}


	/**
	 * Find the buttons in the view.
	 */
	protected void findButtons()
	{
		this.selectGame = (Button) findViewById(R.id.btn_sel_game);
		this.recentGame = (Button) findViewById(R.id.btn_recent_game);
		this.statistics = (Button) findViewById(R.id.btn_statistics);

		this.selectGame.setOnClickListener(this);
		this.recentGame.setOnClickListener(this);
		this.statistics.setOnClickListener(this);
	}


	@Override
	public void onPause()
	{
		super.onPause();
		
		/*Save game stati.*/
		String info = master.printAllGames();
	}


	@Override
	public void onClick(View view)
	{
		try
		{
			Intent intent = null;

			if (view.equals(selectGame))
			{
				intent = new Intent(this, SelectGameActivity.class);
			}

			if (intent==null) // nothing to do.
				return;
			startActivity(intent);
		}
		catch (Exception e)
		{
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}
}
