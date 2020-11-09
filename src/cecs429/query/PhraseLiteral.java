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
public class PhraseLiteral implements Query 
{
	// The list of individual terms in the phrase.
	private List<Query> mChildren = new ArrayList<>();
	
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(Collection<Query> children)
	{	
		mChildren = new ArrayList<>(children);	
	}
	
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) 
	{
		terms = terms.replaceAll("\"", "");
		for(String s: Arrays.asList(terms.split(" ")))
		{
			if(s.contains("*"))
			{
				mChildren.add(new WildcardLiteral(s, true));
			}
			else
			{
				mChildren.add(new TermLiteral(s, true));
			}
		}
	}
	
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{
		List<Posting> result = mChildren.get(0).getPostings(index, processor);
		
		int distance = 1;
		for(int i = 1; i < mChildren.size(); i++)
		{
			result = Operator.positionalMerge(result, mChildren.get(i).getPostings(index, processor), distance);
			distance++;
		}
		
		return result;
	}
	
	
	public int getTermCount()
	{
		return mChildren.size();
	}
	
	
	@Override
	public String toString() 
	{
		String s = "";
		for(Query query: mChildren)
		{
			s += query.toString() + " ";
		}
		return s;
	}
}
