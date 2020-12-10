package cecs429.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class AdvancedTokenProcessor implements TokenProcessor 
{

	@Override
	public List<String> processToken(String token) 
	{
		// Convert the token to lowercase
		token = token.toLowerCase();

		// For hyphen in words, remove the hyphens from the token 
		// Split the original hyphenated token into multiple tokens without a hyphen, and proceed with all split tokens 
		String[] splitTokens = token.split("-");
		
		List<String> result = new ArrayList<String>();
		
		// If the token can be splitted by the hyphens into more than one token
		if(splitTokens.length > 1)
		{
			StringBuffer sb = new StringBuffer();
			
			for(String s: splitTokens)
			{
				if(!s.equals(""))
				{
					// Remove all non-alphanumeric characters from the beginning and end
					s = Normalizer.removeNonAlphanumeric(s);
					
					// Remove all apostropes or quotation marks
					s = Normalizer.removeApostropes(s);
					
					// Append it to a string
					sb.append(s);
					
					// Stem using an implementation of the Porter2 stemmer
					s = Normalizer.stemToken(s);
					
					// Add to the return list
					if(!s.isBlank())
					{
						result.add(s);
					}	
				}	
			}
		
			// Stem the appended string and add to the return list
			result.add(Normalizer.stemToken(sb.toString()));
		}
		
		// If the token does not contain hyphen
		else
		{
			// Remove all non-alphanumeric characters from the beginning and end
			String s = Normalizer.removeNonAlphanumeric(token);
			
			// Remove all apostropes or quotation marks
			s = Normalizer.removeApostropes(s);
			
			// Stem using an implementation of the Porter2 stemmer
			s = Normalizer.stemToken(s);
			
			// Add to the return list
			if(!s.isBlank())
			{
				result.add(s);
			}
		}
		
		return result;
	}

}