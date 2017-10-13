package nox.minesweeper.android;


import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class StatisticsActivity extends Activity implements OnClickListener
{
	private TextView      titleView;
	private ListView      statsView;
	private GamesAdapter  gamesAdapter;

	private OnItemLongClickListener resetStatsListener;
	private Dialog                  resetStatsDialog;
	private Game                    resetStatsGame;


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

				boolean wonGames = 0<stats.countGamesWon(true);

				String str = "", time;

				str += String.format("%15s %d\n%15s %d"
					, "Games won:",  stats.countGamesWon(true)
					, "Games Lost:", stats.countGamesWon(false));

				if (!wonGames)
					return str;

				time = getString(R.string.current_game_time);
				double avg, best, ms;

				ms   = 1e-3;
				avg  = stats.getTime(Statistic.AVERAGE_TIME)*ms;
				best = stats.getTime(Statistic.BEST_TIME)*ms;
				
				str += String.format("\n"+"%15s %s\n"+"%15s %s\n"+"%15s %s"
				,"Average Time:",    String.format(time,avg)
				,"Best Time:",       String.format(time,best)
				,"Cell Time:",       String.format(time,avg/stats.cells())
				);

				return str;
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
				tView.setText(String.valueOf(game));
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

		this.resetStatsListener = new OnItemLongClickListener()
		{
			private GameMaster master = GameMaster.getInstance();

			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
					View view, int pos, long id)
			{
				/*Consumed, but invalid.*/
				if (parent==null || view==null)
				{
					return true;
				}

				StatisticsActivity.this.resetStatsGame = master.get(pos);
				StatisticsActivity.this.getResetStatsDialog().show();

				return true;
			}
		};
		this.statsView.setOnItemLongClickListener(this.resetStatsListener);

		findViewById(R.id.button).setVisibility(View.GONE);
	}


	@Override
	public void onClick(DialogInterface dialog, int button)
	{
		/*No game to reset or canceled.*/
		if (this.resetStatsGame == null || button==Dialog.BUTTON_NEGATIVE)
		{
			this.resetStatsGame = null;
			return;
		}

		this.resetStatsGame.resetStatistics();
		this.gamesAdapter.notifyDataSetChanged();
	}


	/**
	 * Get the dialog to confirm the reset of the selected game's statistic.
	 * @return resetStatsDialog as Dialog.
	 */
	private Dialog getResetStatsDialog()
	{
		if (this.resetStatsDialog != null)
		{
			return this.resetStatsDialog;
		}

		this.resetStatsDialog = new AlertDialog.Builder(this)
			.setTitle(getString(R.string.reset_stats))
			.setMessage(getString(R.string.reset_stats_warning))
			.setPositiveButton(getString(R.string.accept), this)
			.setNegativeButton(getString(R.string.cancel), this)
			.create();

		return this.getResetStatsDialog();
	}
}
