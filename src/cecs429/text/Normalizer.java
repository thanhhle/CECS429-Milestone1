package cecs429.text;

import java.util.ArrayList;
import java.util.List;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

public class Normalizer {
	
	/**
	 * Remove all non-alphanumeric characters from the begin and end of the token, but not the middle
	 * @param token the string
	 * @return the string with the non-alphanumeric characters removed from the begin and end
	 */
	public String removeNonAlphanumeric(String token)
	{
		/*
		int i = 0;
		int j = token.length() - 1;
		
		while(!isAlphaNumeric(String.valueOf(token.charAt(i)))) {
			i++;
		}
		
		while(!isAlphaNumeric(String.valueOf(token.charAt(j)))) {
			j--;
		}
		
		return token.substring(i, j+1);
		*/
		
		return token.replaceAll("^\\W*", "").replaceAll("\\W*$", "");
	}
	
	/**
	 * Remove all apostropes or quotation marks (single or double quotes) from anywhere in the string
	 * @param token the string
	 * @return the string with appstropes or quotation marks removed
	 */
	public String removeApostropes(String token)
	{
		return token.replaceAll("'", "").replaceAll("\"", "");
	}
	
	
	/**
	 * Stem the token using an implementation of the Porter2 stemmer
	 * @param token the string
	 * @return
	 */
	public String stemToken(String token){
		 SnowballStemmer stemmer = new englishStemmer();
		 stemmer.setCurrent(token);
		 stemmer.stem();
		 return stemmer.getCurrent();  
	}

	
}