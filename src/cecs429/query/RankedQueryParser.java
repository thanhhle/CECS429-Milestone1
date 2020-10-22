package cecs429.query;

import java.util.Arrays;
import java.util.List;

public class RankedQueryParser implements QueryParser
{
	/**
	 * Given a ranked query, parses and returns a tree of Query objects representing the query.
	 */
	public Query parseQuery(String query, int corpusSize) 
	{
		List<String> terms = Arrays.asList(query.trim().split("\\s+"));
		return new RankedQuery(terms, corpusSize);
	}
}
