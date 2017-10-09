package nox.minesweeper.desktop;


enum LabelsFiller
{
	INSTANCE;

	public void fillLabels()
	{
		Labels l = Labels.getInstance();
		l.put("BTN_BACK", "ENGLISH", "<html><div style=\"font-size:xx-small;\">Home</div></html>");
		l.put("BTN_CUSTOM", "ENGLISH", "Custom %d:%d with %d mines");
		l.put("BTN_NEW_GAME", "ENGLISH", "New Field");
		l.put("BTN_RESUME_GAME", "ENGLISH", "Resume most recent Game");
		l.put("BTN_STATS", "ENGLISH", "Show Statistics");
		l.put("INFO_NO_GAMES", "ENGLISH", "No games played yet");
		l.put("LBL_MINES", "ENGLISH", "Hidden Marks");
		l.put("LBL_TIME", "ENGLISH", "Time");
		l.put("MENU_EXPORT", "ENGLISH", "Export Fields");
		l.put("MENU_IMPORT", "ENGLISH", "Import Fields");
		l.put("MENU_PREFERENCES", "ENGLISH", "Preferences");
		l.put("SLIDER_HEIGHT", "ENGLISH", "Field Height");
		l.put("SLIDER_MINES", "ENGLISH", "Mines");
		l.put("SLIDER_WIDTH", "ENGLISH", "Field Width");
		l.put("TABLE_STATS", "ENGLISH", "<hr> <h1>%s</h1> <table> <tr><td>Lost:</td><td>%d</td></tr> <tr><td>Won:</td><td>%d</td></tr> <tr><td>Current Streak:</td><td>%d</td></tr> <tr><td>Avg Win Time:</td><td>%s</td></tr> <tr><td>Best Time:</td><td>%s</td></tr> </table>");
		l.put("TITLE_CUSTOM", "ENGLISH", "<html><h4 style=\"color:#bbbbbb;\">Create a Custom Field</h4></html>");
		l.put("TITLE_HOME", "ENGLISH", "<html><h1>Minesweeper</h1></html>");
		l.put("TITLE_SEL", "ENGLISH", "<html><h3>Select or create a new</h3></html>");
		l.put("TITLE_SELECT", "ENGLISH", "<html><h4 style=\"color:#bbbbbb;\">or Open an existing field</h4></html>");
	}
}
