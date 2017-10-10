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
			s += g+"\n";
		}
		return s.trim();
	}


	/**
	 * Add a new Game into the store.
	 * @param game new Game to remember.
	 */
	private void add(Game game)
	{
		if (game==null || this.games.contains(game))
		{
			return;
		}
		this.games.add(game);
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
	 * Get a game with the the given dimensions and mines.
	 * It may initate a new game.
	 * @param height 
	 * @param width 
	 * @param mines 
	 * @return Game with requested attributes.
	 */
	public Game getGameWith(int height, int width, int mines)
	{
		Game game = null;

		/*Find exisitng game.*/
		if (this.containsGameWith(height,width,mines))
		{
			/*Find game*/
			for (Game g : this.games)
			{
				if (!g.equals(height,width,mines))
					continue;

				game = g;
				break;
			}
		}

		/*Initiate a new Game and store it.*/
		else
		{
			game = new Game(height, width, mines);
			GameMaster.this.add(game);
		}

		return game;
	}


	/**
	 * Check if there is a game known with the given attribtues.
	 * @param height of field
	 * @param width of field
	 * @param mines in field
	 * @return true, if there is a game store with those parameters.
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
}
