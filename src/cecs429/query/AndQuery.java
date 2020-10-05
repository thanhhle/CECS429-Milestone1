package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An AndQuery composes other Query objects and merges their postings in an intersection-like operation.
 */
public class AndQuery implements Query 
{
	private List<Query> mChildren;
	
	
	public AndQuery(Collection<Query> children) 
	{
		mChildren = new ArrayList<>(children);
	}
	
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		List<Posting> result = mChildren.get(0).getPostings(index, processor);;
		
		if(mChildren.get(0).isNegative())
		{
			// If the first literal is negative
			result = Operator.notMerge(mChildren.get(1).getPostings(index, processor), result);
		}
		else if(mChildren.get(1).isNegative())
		{
			// If the second literal is negative
			result = Operator.notMerge(result, mChildren.get(1).getPostings(index, processor));
		}
		else
		{
			// If both of 2 first literals are positive
			result = Operator.andMerge(result, mChildren.get(1).getPostings(index, processor));
		}
		
		
		for(int i = 2; i < mChildren.size(); i++)
		{
			if(mChildren.get(i).isNegative())
			{
				result = Operator.notMerge(result, mChildren.get(i).getPostings(index, processor));
			}
			else
			{
				result = Operator.andMerge(result, mChildren.get(i).getPostings(index, processor));
			}
		}

		return result;
	}
	
	
	@Override
	public String toString() 
	{
		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
