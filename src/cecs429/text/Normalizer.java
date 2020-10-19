package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Normalizer {
	
	/**
	 * Remove all non-alphanumeric characters from the begin and end of the token, but not the middle
	 * @param token the string
	 * @return the string with the non-alphanumeric characters removed from the begin and end
	 */
	public static String removeNonAlphanumeric(String token)
	{
		return token.replaceAll("^\\W*", "").replaceAll("\\W*$", "");
	}
	

	/**
	 * Remove all apostropes or quotation marks (single or double quotes) from anywhere in the string
	 * @param token the string
	 * @return the string with appstropes or quotation marks removed
	 */
	public static String removeApostropes(String token)
	{
		return token.replaceAll("'", "")
				.replaceAll("\"", "")
				.replaceAll("‘", "")
				.replaceAll("’", "")
				.replaceAll("“", "")
				.replaceAll("”", "");
	}
	
	
	/**
	 * Stem the token using an implementation of the Porter2 stemmer
	 * @param token the string
	 * @return
	 */
	public static String stemToken(String token)
	{
		 SnowballStemmer stemmer = new englishStemmer();
		 stemmer.setCurrent(token);
		 stemmer.stem();
		 return stemmer.getCurrent();  
	}
	
	
	private boolean isAlphaNumeric(String s) 
	{
		return s != null && s.matches("^[a-zA-Z0-9]*$");
	}

}