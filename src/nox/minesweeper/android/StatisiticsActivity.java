package nox.minesweeper.android;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class StatisiticsActivity extends Activity
{
	private TextView      titleView;
	private ListView      statsView;
	private GamesAdapter  gamesAdapter;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.list_games);

		this.gamesAdapter = new GamesAdapter(this.getLayoutInflater())
		{
			private String print(Statistic stats)
			{
				if (stats==null) return "";

				boolean wonGames = stats.countGamesWon(true)>0;

				String str;
				str = "Games Won:        "+stats.countGamesWon(true) +"\n"
					+ "Games Lost:       "+stats.countGamesWon(false);

				if (!wonGames) return str;
				
				str = "\n"
					+ "Average Time:     "+print(stats.getTime(Statistic.AVERAGE_TIME))+"\n"
					+ "Best Time:        "+print(stats.getTime(Statistic.BEST_TIME))+"\n"
					+ "Seconds per Cell: "+print(stats.getTime(Statistic.CELL_TIME))
					;

				return str;
			}

			private String print(long ms)
			{
				return String.format("%.3f s", (ms*1e-3));
			}


			@Override
			public View getView(int pos, View convertView, ViewGroup group)
			{
				convertView = super.getView(pos, convertView, group);


				Statistic stats;
				Game       game;
				TextView   tView;

				game  = (Game) this.getItem(pos);
				stats = game.getStatistics();

				tView = (TextView) convertView.findViewById(R.id.text0);
				tView.setText("Statistic: "+game);
				tView.setGravity(Gravity.LEFT);

				tView = (TextView) convertView.findViewById(R.id.text1);
				tView.setText(this.print(stats));
				tView.setGravity(Gravity.LEFT);
				return convertView;
			}
		};

		this.titleView = (TextView) findViewById(R.id.title);
		this.statsView = (ListView) findViewById(R.id.list);

		this.titleView.setText(getString(R.string.statistics));
		this.statsView.setAdapter(this.gamesAdapter);

		findViewById(R.id.button).setVisibility(View.GONE);
	}
}
