package cecs429.text;

import java.util.List;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class AdvancedTokenProcessor implements TokenProcessor {
	Normalizer normalizer = new Normalizer();
	
	@Override
	public List<String> processToken(String token) {
		// Remove all non-alphanumeric characters from the beginning and end of the token
		token = normalizer.removeNonAlphanumeric(token);
		
		// Remove all apostropes or quotation marks from the token
		token = normalizer.removeApostropes(token);
		
		// For hyphen in words, remove the hyphens from the token 
		// And split the original hyphenated token into multiple tokens without a hyphen
		// And put these modified into a list of string
		List<String> tokens = normalizer.splitHyphen(token);
		
		for(String s: tokens)
		{
			// Convert the token to lowercase
			s = s.toLowerCase();
			
			// Stem the token using an implementation of the Porter2 stemmer
			s = normalizer.stemToken(s);
		}
		
		return tokens;
	}

}
