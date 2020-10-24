package cecs429.query;

import java.util.ArrayList;
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
		
		List<Query> children = new ArrayList<Query>();
		for(String term: terms)
		{
			if(term.contains("*"))
			{
				children.add(new WildcardLiteral(term));
			}
			else
			{
				children.add(new TermLiteral(term));
			}
		}
		return new RankedQuery(children, corpusSize);
	}
}
