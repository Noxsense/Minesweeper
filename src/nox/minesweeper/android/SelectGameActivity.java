package nox.minesweeper.android;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


public class SelectGameActivity extends Activity implements View.OnClickListener
{
	private TextView      titleView;
	private ListView      gamesView;
	private GamesAdapter  gamesAdapter;

	private Button        createGame;


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
	public void onClick(View view)
	{
		return;
	}
}
