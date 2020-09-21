package cecs429.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A BasicTokenProcessor creates terms from tokens by removing all non-alphanumeric characters from the token, and
 * converting it to all lowercase.
 */
public class BasicTokenProcessor implements TokenProcessor {
	
	@Override
	public List<String> processToken(String token) {
		List<String> result = new ArrayList<String>();
		result.add(token.replaceAll("\\W", "").toLowerCase());
		return result;
	}
}