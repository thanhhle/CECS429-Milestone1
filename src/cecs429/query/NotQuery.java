package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.List;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class NotQuery implements Query 
{
	private Query mQuery;
	
	
	public NotQuery(Query query) 
	{
		mQuery = query;
	}
	
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		return mQuery.getPostings(index, processor);
	}
	
	
	@Override
	public String toString() 
	{
		return mQuery.toString();
	}
	
	
	public boolean isNegative()
	{
		return true;
	}
}
