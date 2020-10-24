package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An OrQuery composes other Query objects and merges their postings with a union-type operation.
 */
public class OrQuery implements Query 
{
	// The components of the Or query.
	private List<Query> mChildren;
	
	
	public OrQuery(Collection<Query> children) 
	{
		mChildren = new ArrayList<>(children);	
	}
	
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		List<Posting> result = new ArrayList<Posting>();
		
		for(int i = 0; i < mChildren.size(); i++)
		{
			result = Operator.orMerge(result, mChildren.get(i).getPostings(index, processor));
		}
		
		return result;
	}
	
	
	@Override
	public String toString() 
	{
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
}
