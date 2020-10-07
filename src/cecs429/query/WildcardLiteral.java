package cecs429.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class WildcardLiteral implements Query
{
	private String mTerm;
	
	
	public WildcardLiteral(String term) 
	{
		mTerm = term;
	}
	
	
	public String getTerm() 
	{
		return mTerm;
	}
	
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{	
		List<Posting> result = new ArrayList<Posting>();
		
		// Generate the largest k-grams
		List<String> kgrams = generateKGrams(mTerm, index.getKGramIndex().getKValue());

		// Retrieve and intersect the list of vocabulary types for each k-gram
		List<String> candidates = index.getKGramIndex().getCandidates(kgrams.get(0));
		for(int i = 1; i < kgrams.size(); i++)
		{
			candidates = Operator.andMerge(candidates, index.getKGramIndex().getCandidates(kgrams.get(i)));
		}
		
		// Post-filtering to make sure candidates match the wildcard pattern
		if(candidates.size() > 0)
		{
			Iterator<String> iterator = candidates.iterator();
		    while (iterator.hasNext()) 
		    {
		    	if (!iterator.next().matches(mTerm.replace("*", ".*"))) 
		    	{
		    		iterator.remove();
		    	}
		    }
		}
		
		// Print list of final wildcard candidates
		System.out.println("\n" + candidates.size() + " wildcard matches these types:");
		for(String candidate: candidates)
		{
			System.out.println("- " + candidate);
		}
		
		// Or merge the postings for the processed term from each final wildcard candidate
		if(candidates.size() > 0)
		{	
			result = index.getPostings(processor.processToken(candidates.get(0)));
		
	    	for(int i = 1; i < candidates.size(); i++)
	    	{
	    		result = Operator.orMerge(result, index.getPostings(processor.processToken(candidates.get(i))));
	    	}	
			  
		}
		
		return result;
	}
	
	
	@Override
	public String toString() 
	{
		return mTerm;
	}
	
	
	private List<String> generateKGrams(String term, int kValue)
	{
		SortedSet<String> kgrams = new TreeSet<String>();
		
		// Append "$" at the beginning and end of the wildcard
		String modifiedTerm = term;
		if(modifiedTerm.charAt(0) != '*')
		{
			modifiedTerm = "$" + modifiedTerm;
		}
		
		if(modifiedTerm.charAt(modifiedTerm.length() - 1) != '*')
		{
			modifiedTerm = modifiedTerm + "$";
		}
		
		// Split the query at the '*' and generate the largest k-grams
		String[] largestKGrams = modifiedTerm.split("\\*");
		
		for(String element: largestKGrams)
		{
			if(element.length() > kValue)
			{
				for(int i = 0; i + kValue <= element.length(); i++)
				{
					kgrams.add(element.substring(i, i + kValue));
				}
			}
			else if(element.length() > 0)
			{
				kgrams.add(element);
			}
		}
		
		return new ArrayList<String>(kgrams);
	}

}