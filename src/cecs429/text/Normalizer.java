package cecs429.text;

import java.util.ArrayList;
import java.util.List;

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
	 * For hyphen in words, remove the hyphens from the token 
	 *  And split the original hyphenated token into multiple tokens without a hyphen
	 *	And put these modified into a list of string
	 * @param token the string
	 * @return list of token with hyphens removed and tokens which are split by hyphens
	 */
	public List<String> splitHyphen(String token)
	{
		String[] splitTokens = token.split("-");
		List<String> list = new ArrayList<String>();
		
		if(splitTokens.length > 1)
		{
			StringBuffer sb = new StringBuffer();
			
			for(String s: splitTokens)
			{
				if(!s.equals(""))
				{
					s = removeNonAlphanumeric(s);
					s = removeApostropes(s);
					list.add(s);
					sb.append(s);
				}	
			}
		
			list.add(sb.toString());
		}
		else
		{
			String s = removeNonAlphanumeric(token);
			s = removeApostropes(s);
			list.add(s);
		}
		
		return list;
	}
	
	/**
	 * Stem the token using an implementation of the Porter2 stemmer
	 * @param token the string
	 * @return
	 */
	public String stemToken(String token) {
		Stemmer stemmer = new Stemmer();
		stemmer.add(token.toCharArray(), token.length());
		stemmer.stem();
		return stemmer.toString();
	}

	
}
