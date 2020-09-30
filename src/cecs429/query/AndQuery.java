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
public class AndQuery implements Query {
	private List<Query> mChildren;
	
	public AndQuery(Collection<Query> children) {
		mChildren = new ArrayList<>(children);
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		// TODO: program the merge for an AndQuery, by gathering the postings of the composed QueryComponents and
		// intersecting the resulting postings.
		List<Posting> result = mChildren.get(0).getPostings(index, processor);;
		
		if(mChildren.get(0).isNegative())
		{
			// If the first literal is negative
			result = notMerge(mChildren.get(1).getPostings(index, processor), result);
		}
		else if(mChildren.get(1).isNegative())
		{
			// If the second literal is negative
			result = notMerge(result, mChildren.get(1).getPostings(index, processor));
		}
		else
		{
			// If both of 2 first literals are positive
			result = andMerge(result, mChildren.get(1).getPostings(index, processor));
		}
		
		for(int i = 2; i < mChildren.size(); i++)
		{
			if(mChildren.get(i).isNegative())
			{
				result = notMerge(result, mChildren.get(i).getPostings(index, processor));
			}
			else
			{
				result = andMerge(result, mChildren.get(i).getPostings(index, processor));
			}
		}

		return result;
	}
	
	@Override
	public String toString() {
		return
		 String.join(" ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}

	
	private List<Posting> andMerge(List<Posting> list1, List<Posting> list2)
	{
		if(list1 == null || list2 == null)
		{
			return null;
		}

		List<Posting> result = new ArrayList<Posting>();
		
		int i = 0;
        int j = 0;
        
        while (i < list1.size() && j < list2.size()) 
        {
        	if(list1.get(i).getDocumentId() == list2.get(j).getDocumentId())
        	{
        		result.add(list1.get(i));
        		i++;
        		j++;
        	}
        	else if(list1.get(i).getDocumentId() < list2.get(j).getDocumentId())
        	{
        		i++;
        	}
        	else
        	{
        		j++;
        	}
        }
        
        return result;
	}
	

	private List<Posting> notMerge(List<Posting> list, List<Posting> notList)
	{	
		if(list == null || notList == null)
		{
			return null;
		}

		List<Posting> result = new ArrayList<Posting>();
		
		int i = 0;
        int j = 0;
        
        while (i < list.size() && j < notList.size()) 
        {
        	if(list.get(i).getDocumentId() == notList.get(j).getDocumentId())
        	{
        		i++;
        		j++;
        	}
        	else if(list.get(i).getDocumentId() < notList.get(j).getDocumentId())
        	{
        		
        		result.add(list.get(i));
        		i++;
        	}
        	else
        	{
        		j++;
        	}
        }
        
        // Append the longer list to the results
        while (i < list.size())
        {
        	result.add(list.get(i));
            i++;
        }
        
        return result;
	}
}
