package nox.minesweeper;


import java.util.Map;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


/**
 * Class Labels.
 * Manager for labels and strings and a lot of output.
 * Manager for translations and so on.
 * SINGLETON!
 */
class Labels
{
	public  final static String            LANG_DEFAULT = "ENGLISH";
	private       static Labels            INSTANCE;

	private String                         language;      // currently set language
	private Map<String,Map<String,String>> loadedStrings; // Key:  Language:Value


	/**
	 * Initiate labels.
	 * Initiate it's attributes.
	 */
	private Labels()
	{
		this.loadedStrings = new HashMap<>();
		this.setLanguage(LANG_DEFAULT);
	}


	/**
	 * Try to complete the known strings with data from file.
	 * Stringformat:
	 * @param file filename/path where to find information.
	 * @throws NullPointerException if filename is null
	 * @throws IOException
	 */
	public void loadFromFile(String file) throws NullPointerException
	{
		file = file.trim();

		try (BufferedReader buffR = new BufferedReader(new FileReader(file)))
		{
			for (String l=buffR.readLine(); l != null; l=buffR.readLine())
			{
				String[] s = l.split("\\s", 3);
				this.put(s[0],s[1],s[2]);
			}
		}
		catch (IOException e)
		{
			//e.printStackTrace();
		}
	}


	/**
	 * Try to save the labels to an external file.
	 * @param file filename/path to save information
	 * @throws NullPointerException 
	 */
	public void saveToFile(String file) throws NullPointerException
	{
		Map<String,String> langs;
		String             data;

		for (String key : this.loadedStrings.keySet())
		{
			langs = this.loadedStrings.get(key);
			for (String lang : langs.keySet())
			{
				data = key+"\t"+lang+"\t"+langs.get(lang);

				System.out.println(data); // save here.
			}
		}
	}


	/**
	 * Set the current Language.
	 * If null or empty: Resets to "ENGLISH"
	 * @param language new language.
	 */
	public void setLanguage(String language)
	{
		try
		{
			this.language = Labels.keyFormat(language);
		}
		catch (NullPointerException e)
		{
			this.setLanguage(LANG_DEFAULT);
		}
	}


	/**
	 * Get the current Language.
	 * @return language as String (CAPS).
	 */
	public String getLanguage()
	{
		return this.language;
	}


	/**
	 * Read and a String and save with key and language.
	 * @param key Singal word to find this String.
	 * @param lan Language for this word.
	 * @param str Actual String to save.
	 */
	public void put(String key, String lan, String str) throws NullPointerException
	{
		key = Labels.keyFormat(key);
		lan = Labels.keyFormat(lan);
		str = str.trim();

		if (!this.loadedStrings.containsKey(key))
		{
			this.loadedStrings.put(key, new HashMap<String,String>());
		}
		this.loadedStrings.get(key).put(lan, str);
	}


	/**
	 * Get the string which is saved with given key and language.
	 * If key or lan are unknown, it will return an empty string.
	 * @param key Singal word to find this String.
	 * @param lan Language for this word.
	 * @return saved string.
	 */
	public String get(String key, String lan)
	{
		try // format key and language, on Failure it will return "".
		{
			key = Labels.keyFormat(key);
			lan = Labels.keyFormat(lan);
		}
		catch (NullPointerException e)
		{
			return "";
		}

		if (!this.loadedStrings.containsKey(key))
		{
			return "";
		}
		
		Map<String,String> languages;
		languages = this.loadedStrings.get(key);

		if (languages.keySet().isEmpty() || !languages.containsKey(LANG_DEFAULT))
		{
			return "";
		}

		else if (!languages.containsKey(lan)) // fallback: use default
		{
			return languages.get(LANG_DEFAULT);
		}

		return languages.get(lan);
	}


	/**
	 * Get the string which is saved with given key for current language.
	 * Proxy for get(key, this.language)
	 * @param key Singal word to find this String.
	 * @param lan Language for this word.
	 * @return saved string.
	 */
	public String get(String key)
	{
		return this.get(key, this.language);
	}


	/**
	 * Get the Labels instance.
	 * @return instance as Labels
	 */
	public static Labels getInstance()
	{
		if (Labels.INSTANCE == null)
		{
			Labels.INSTANCE = new Labels();
		}
		return Labels.INSTANCE;
	}


	/**
	 * Print the given String in a colourful environment.
	 * Proxy for Labels.env("span", str, "style=\"color:col;\"").
	 * @param str  string in tags.
	 * @param col  color for the environment
	 * @return formated String
	 */
	public static String colour(String str, String col)
	{
		return Labels.env("span", str, "style=\"color:"+col+";\"");
	}


	/**
	 * Print the given String in html environment.
	 * Proxy for Labels.env("html", str, "").
	 * @param str  string in tags.
	 * @return formated String
	 */
	public static String html(String str)
	{
		return Labels.env("html", str, "");
	}


	/**
	 * Print the given String in header environment.
	 * Proxy for Labels.env("h"+i, str, "").
	 * @param n    header level
	 * @param str  string in tags.
	 * @param attr  string in tags.
	 * @return formated String
	 */
	public static String h(int n, String str, String attr)
	{
		return Labels.env("h"+Math.abs(n), str, attr);
	}


	/**
	 * Print the given String in html-div environment.
	 * Proxy for Labels.env("div", str, attr).
	 * @param str  string in tags.
	 * @param attr attribute of environment.
	 * @return formated String
	 */
	public static String div(String str, String attr)
	{
		return Labels.env("div", str, attr);
	}


	/**
	 * Print the given String in html-environment tags.
	 * @param env  new environment
	 * @param str  string which have to be enclosed.
	 * @param attr environment attributes.
	 * @return str in a new environment as String.
	 */
	public static String env(String env, String str, String attr)
	{
		if (env==null || (env = env.trim()).equals(""))
		{
			return str;
		}

		if (attr==null || (attr = attr.trim()).equals(""))
		{
			return String.format("<%s>%s</%s>", env, str, env);
		}
		return String.format("<%s %s>%s</%s>", env, attr, str, env);
	}


	/**
	 * Given trimed String to uppercase, all whitespaces to unterscore.
	 * @param st String which have to be formatted.
	 * @return formatted String.
	 * @throws NullPointerException 
	 */
	private static String keyFormat(String st) throws NullPointerException
	{
		st = st.trim().toUpperCase().replaceAll("\\s+", "_");

		if (st.equals(""))
		{
			throw new NullPointerException("Useless as Key!");
		}

		return st;
	}
}
