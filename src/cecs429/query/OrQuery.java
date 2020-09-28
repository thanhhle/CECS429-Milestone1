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
public class OrQuery implements Query {
	// The components of the Or query.
	private List<Query> mChildren;
	
	public OrQuery(Collection<Query> children) {
		mChildren = new ArrayList<>(children);	
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) {
		List<Posting> result = mChildren.get(0).getPostings(index, processor);
		
		// TODO: program the merge for an OrQuery, by gathering the postings of the composed Query children and
		// unioning the resulting postings.
		for(int i = 1; i < mChildren.size(); i++)
		{
			result = orMerge(result, mChildren.get(i).getPostings(index, processor));
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mChildren.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
	
	private List<Posting> orMerge(List<Posting> list1, List<Posting> list2)
	{	
		if(list1 == null && list2 == null)
		{
			return null;
		}
		else if(list1 == null)
		{
			return list2;
		}
		else if(list2 == null)
		{
			return list1;
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
        		result.add(list1.get(i));
        		i++;
        	}
        	else
        	{
        		result.add(list2.get(j));
        		j++;
        	}
        }
        
        // Append the longer list to the results
        while (i < list1.size())
        {
        	result.add(list1.get(i));
            i++;
        }
        
        while (j < list2.size())
        {
        	result.add(list2.get(j));
        	j++;
        } 

        return result;
	}
}
