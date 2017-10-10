package nox.minesweeper.android;


import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.TextView;



/**
 * Class GamesAdapter,
 */
class GamesAdapter extends BaseAdapter
{
	private LayoutInflater layoutInflater;


	public GamesAdapter(LayoutInflater inflater) throws NullPointerException
	{
		super();
		if (inflater==null)
		{
			throw new NullPointerException("No LayoutInflater given");
		}

		this.layoutInflater = inflater;
	}


	@Override
	public View getView(int pos, View convertView, ViewGroup group)
	{
		if (convertView==null)
		{
			convertView = this.layoutInflater.inflate(
					R.layout.list_item,
					group,
					false);
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

