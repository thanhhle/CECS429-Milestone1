package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements Query {
	// The list of individual terms in the phrase.
	private List<Query> mChildren = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(Collection<Query> children) {	
		mChildren = new ArrayList<>(children);	
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		for(String s: Arrays.asList(terms.split(" ")))
		{
			mChildren.add(new TermLiteral(s));
		}
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		List<Posting> result = mChildren.get(0).getPostings(index, processor);
		
		// TODO: program this method. Retrieve the postings for the individual terms in the phrase,
		// and positional merge them together.
		int distance = 1;
		for(int i = 1; i < mChildren.size(); i++)
		{
			result = positionalMerge(result, mChildren.get(i).getPostings(index, processor), distance);
			distance++;
		}
		
		return result;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Query query: mChildren)
		{
			s += query.toString() + " ";
		}
		return s;
	}
	
	private List<Posting> positionalMerge(List<Posting> list1, List<Posting> list2, int distance)
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
				 List<Integer> positions1 = list1.get(i).getPositions();
				 List<Integer> positions2 = list2.get(j).getPositions();
				 List<Integer> temp = new ArrayList<Integer>();
				 
				 int m = 0;
				 int n = 0;
				
				 while(m < positions1.size() && n < positions2.size())
				 {
					 int dis = positions2.get(n) - positions1.get(m);
					 
					 if(dis == distance)
					 {
						 temp.add(positions1.get(m));
						 m++;
						 n++;
					 }	
					 else if(dis > distance)
					 {
						 m++;
					 }
					 else
					 {
						 n++;
					 }
				 }
				 
				 if(temp.size() > 0)
				 {
					 result.add(new Posting(list1.get(i).getDocumentId(), temp));
				 }
				 
				 i++;
				 j++;
			}
			else if (list1.get(i).getDocumentId() < list2.get(j).getDocumentId())
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
}
