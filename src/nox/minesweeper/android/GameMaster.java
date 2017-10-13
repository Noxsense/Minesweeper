package nox.minesweeper.android;


import java.util.List;
import java.util.ArrayList;

import nox.minesweeper.logic.*;


/**
 * Class GameMaster.
 * Collects, saves and restores games and statistics.
 * The applications core.
 */
public class GameMaster
{
	private final static String SEP0 = "\t";
	private final static String SEP1 = ",";

	private static GameMaster instance;

	private List<Game> games;


	/**
	 * Initate the GameMaster.
	 */
	private GameMaster()
	{
		this.games = new ArrayList<Game>();
	}


	/**
	 * Get the Instance for GameMaster.
	 * @return instance as GameMaster.
	 */
	public static GameMaster getInstance()
	{
		if (GameMaster.instance == null)
		{
			GameMaster.instance = new GameMaster();
		}
		return GameMaster.instance;
	}


	/**
	 * Get a copy of the stored games list.
	 * @return List which will not affect the GameMaster's List.
	 */
	public List<Game> getGames()
	{
		return new ArrayList<Game>(this.games);
	}


	/**
	 * Get a String representation of all games.
	 * Where every game represents one line.
	 * @return string with all games' data.
	 */
	public String printAllGames()
	{
		String s = "";

		for (Game g : this.games)
		{
			s += GameMaster.printAll(g)+"\n";
		}
		return s.trim();
	}


	/**
	 * Try to load the game into the GameMaster store.
	 * @param info with game data.
	 * @throws NullPointerException 
	 * @throws ArrayIndexOutOfBoundsException 
	 * @throws NumberFormatException 
	 */
	public void loadGame(String info) throws NullPointerException, ArrayIndexOutOfBoundsException, NumberFormatException
	{
		try
		{
			Game parsed = GameMaster.parseGame(info);
			this.add(parsed, false);
		}
		catch (NumberFormatException e)
		{
			throw new NumberFormatException(e.getMessage()+": \""+info+"\"");
		}
	}


	/**
	 * Add a new Game into the store.
	 * @param game new Game to remember.
	 * @param asHead if true, set the game as head, else append.
	 */
	private void add(Game game, boolean asHead)
	{
		if (game==null)
		{
			return;
		}

		/*Remove from previous Position to put it on top.*/
		if (this.games.contains(game))
		{
			/*Find game*/
			for (Game g : this.games)
			{
				if (!g.equals(game))
					continue;

				game = g;
				break;
			}
			this.games.remove(game);
		}

		if (asHead) this.games.add(0, game);
		else        this.games.add(game);
	}


	/**
	 * Add a new Game into the store.
	 * Add the game as new head (most recently called).
	 * @param game new Game to remember.
	 */
	private void add(Game game)
	{
		this.add(game, true);
	}


	/**
	 * Get size/count of stored games.
	 * @return #games as int.
	 */
	public int size()
	{
		return this.games.size();
	}


	/**
	 * Get the game from the given index.
	 * @param index 
	 * @return stored Game on the index.
	 * @throws IndexOutOfBoundsException of the index is invalid.
	 */
	public Game get(int index) throws IndexOutOfBoundsException
	{
		return this.games.get(index);
	}


	/**
	 * Get a game with the attributes of the given game.
	 * Proxy for this.getGameWith(int,int,int).
	 * @param game which should be in the store and should be returned.
	 * @return game with the same attributes, but stored.
	 * @throws NullPointerException the given game is null.
	 */
	public Game get(Game game) throws NullPointerException, ArrayIndexOutOfBoundsException
	{
		return this.getGameWith(
				game.field.getHeight(),
				game.field.getWidth(),
				game.mines);
	}


	/**
	 * Get a game with the the given dimensions and mines.
	 * It may initate a new game.
	 * @param height 
	 * @param width 
	 * @param mines 
	 * @return Game with requested attributes.
	 */
	public Game getGameWith(int height, int width, int mines) throws ArrayIndexOutOfBoundsException
	{
		/* Put the recently called game on head.
		 * May use an already initated game.
		 */
		this.add(new Game(height, width, mines));
		return this.get(0);
	}


	/**
	 * Check if there is a game known with the given attribtues.
	 * @param height of field
	 * @param width of field
	 * @param mines in field
	 * @return true, if there is a game stored with those parameters.
	 */
	public boolean containsGameWith(int height,int width,int mines)
	{
		for (Game g : this.games)
		{
			if (!g.equals(height,width,mines))
				continue;
			return true;
		}
		return false;
	}


	/**
	 * Check if there is a game known like the given game.
	 * @param game which is seached.
	 * @return true, if there is a game stored like the given one.
	 */
	public boolean containsGame(Game game)
	{
		return game!=null && this.games.contains(game);
	}


	/**
	 * Get a String representation of the current state for the given game.
	 * Format: height|width|mines|mines|opened|marked|played time|stats
	 * @return game with all mines, marks and open as String.
	 */
	private static String printAll(Game game)
	{
		if (game==null)
			return "";

		/*Base information.*/
		String s = String.format("%d"+SEP0+"%d"+SEP0+"%d"+SEP0
				,game.field.getHeight()
				,game.field.getWidth()
				,game.mines);

		/*Position information.*/
		int[] indices;

		indices = game.field.getMineIndices(); // mines
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = game.field.getWithState(Field.State.OPEN, 0); // opened
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		indices = game.field.getWithState(Field.State.MARKED, 0); // marked
		for (int i=0; i<indices.length; i++)
		{
			s += indices[i]+ ((i<indices.length-1) ? SEP1 : "");
		}
		s += SEP0;

		/*Currently played time.*/
		s += game.getTime(Game.PLAYED_TIME);
		s += SEP0;

		/*Statistics*/
		s += game.getStatistics().toString().replaceAll("\\s+",SEP1);

		return s;
	}


	/**
	 * Recreate the Game from the String.
	 * Format: height|width|mines|mines|opened|marked|stats
	 * @param str String with information about the Game.
	 * @return game with parsed attribtues
	 * @throws NullPointerException if the string is null.
	 * @throws NumberFormatException if the numbers are invalid.
	 * @throws ArrayIndexOutOfBoundsException if the parsed indices don't fit.
	 */
	protected static Game parseGame(String string) throws NullPointerException, NumberFormatException, ArrayIndexOutOfBoundsException
	{
		String[] str, indices;
		str = string.split(SEP0, 8);

		int height = Integer.parseInt(str[0]);
		int width  = Integer.parseInt(str[1]);
		int mines  = Integer.parseInt(str[2]);

		Game parsed = new Game(height, width, mines);

		/*Section for known mines.*/
		if (0<str[3].length())
		{
			indices  = str[3].split(SEP1); // mine
			int[] is = new int[indices.length];
			for (int i=0; i<is.length; i++) // translate string array to int array.
			{
				is[i] = Integer.parseInt(indices[i]);
			}
			parsed.field.fillMines(is);
		}

		/*Section for opened mines.*/
		if (0<str[4].length())
		{
			indices = str[4].split(SEP1); // open
			for (String pos : indices)
			{
				parsed.open(Integer.parseInt(pos));
			}
		}

		/*Section for marked mines.*/
		if (0<str[5].length())
		{
			indices = str[5].split(SEP1); // marked
			for (String pos : indices)
			{
				parsed.toggleMark(Integer.parseInt(pos));
			}
		}

		/*Currently played time.*/
		parsed.resumeWith(Long.parseLong(str[6])); // played time
		parsed.pause();

		/*Statistics*/
		Statistic stats = new Statistic(parsed);
		stats.parseValues(str[7].replaceAll(SEP1, " "));
		parsed.loadStatisticFrom(stats);

		return parsed;
	}
}
