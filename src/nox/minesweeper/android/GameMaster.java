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
			s += g.printAll()+"\n";
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
		Game parsed = Game.parseGame(info);
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
}
