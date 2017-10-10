package nox.minesweeper.android;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class SelectGameActivity extends Activity
{
	private ListView      gamesView;
	private GamesAdapter  gamesAdapter;


	/**
	 * Class GamesAdapter,
	 */
	private class GamesAdapter extends BaseAdapter
	{
		@Override
		public View getView(int pos, View convertView, ViewGroup group)
		{
			if (convertView==null)
			{
				convertView = getLayoutInflater()
					.inflate(R.layout.list_item, group, false);
			}

			Game gameOnPos = (Game) this.getItem(pos);

			TextView textV;
			textV = (TextView) convertView.findViewById(R.id.text0);
			if (textV!=null) textV.setText(""+gameOnPos);

			textV = (TextView) convertView.findViewById(R.id.text1);
			if (textV!=null) textV.setText("Running: "+gameOnPos.isRunning());

			return convertView;
		}


		@Override
		public int getCount()
		{
			return GameMaster.getInstance().size();
		}


		@Override
		public Object getItem(int pos)
		{
			return GameMaster.getInstance().get(pos);
		}


		@Override
		public long getItemId(int pos)
		{
			if (pos<0 || pos>=this.getCount()) return -1;
			return (long) pos;
		}
	}


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.select_game);

		this.gamesView = (ListView) findViewById(R.id.list_games);
		this.gamesView.setAdapter(this.gamesAdapter = new GamesAdapter());
	}
}
