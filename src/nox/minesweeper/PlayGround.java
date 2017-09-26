package nox.minesweeper;



/**
 * Class PlayGround.
 * A Testing area with main.
 * With a lot of terminal output.
 */
public class PlayGround
{
	private static void customField()
	{
		System.out.println("==========================\nCustomized Game Field.\n");

		field = new Field(8,3);
		field.setDisplay0('~');
		field.setDisplayClosed('.');
		field.setDisplayMarked('*');
		
		field.fillMines(new int[]{0,3,6,9,12,15,18,21, 8,14,20});
		field.open(4);
		field.open(5);
		field.open(10);
		field.open(11);
		field.open(16);
		field.open(17);
		field.open(22);
		field.open(23);

		System.out.println(field.print());

		lim = 2;

		for (int tries=0; tries<lim; tries ++)
		{
			System.out.println("########################################################\nNew Try:");
			System.out.println("\n View all open Indices.");
			indices = field.getWithState(Field.State.OPEN,1);

			for (int index : indices)
			{
				System.out.println("\n View Index: "+index+"\n========================================================\nNew State:");
				Solver.INSTANCE.checkPosition(field, index, 0<tries);
				System.out.println(field.print());
			}
		}
	}

	public static void main(String[] args)
	{
		int[] indices, closed;
		int   except; // first move
		int   last  = -1;
		int   n     = 0;
		int   lim   = Integer.MAX_VALUE;
		int[] f     = {16, 30, 99};
		int   still = 0;
		long  start = 0;
		long  avg   = 0;

		Field field;
		field = new Field(f[0], f[1]);
		field.setDisplay0('~');
		field.setDisplayClosed('.');
		field.setDisplayMarked('*');

		indices = new int[0];

		lim = field.size()*3;
		lim = 0;

		while (indices.length < 4) // get a useable game.
		{
			field.fillRandomly(f[2], (except = Solver.random(field)));
			field.open(except);
			indices = field.getWithState(Field.State.OPEN,0);
			
		}
		System.out.println("Field: "+field+" size: "+field.size()+", mines: "+field.getMines());
		System.out.println("\n field.oneLine():\n"+field.oneLine());

		for (;still<5 && n<lim && !(field.isWon()||field.isLost()); n++)
		{
			//indices = Solver.INSTANCE.possibleUnsatisfied(field);
			indices = field.getWithState(Field.State.OPEN,1);
			closed  = field.getWithState(Field.State.CLOSED,0);

			System.out.println("\n==========================================================================================================");
			System.out.println("\n"+field.print());
			//System.out.println("Step: "+n+"   Now Open: "+java.util.Arrays.toString(indices));


			still = (last==closed.length) ? still + 1 : 0;
			last  = closed.length;
			avg   = 0;

			for (int index : indices)
			{
				start = System.nanoTime();
				Solver.INSTANCE.checkPosition(field, index, 1<still);
				avg += System.nanoTime()-start;
			}

			if (indices.length==0)
				continue;
			System.out.println(String.format("Average Time: %.3f", (avg/indices.length*1e-9)));

		}

		System.out.println("\n Loops: "+n);
		System.out.println(field.print());
		System.out.println("\n Game State: "+(field.isWon()?"WON":(field.isLost()?"LOST": "...")));

		System.out.println("\n field.oneLine():\n"+field.oneLine());
	}
}
