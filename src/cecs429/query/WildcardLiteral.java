package cecs429.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

public class WildcardLiteral implements Query{
	private String mTerm;
	
	public WildcardLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor) 
	{	
		// Generate the largest k-grams
		List<String> kgrams = generateKGrams(mTerm, index.getKGramIndex().getKValue());
		
		
		// Retrieve the list of vocabulary types for each k-gram
		HashMap<String, List<String>> mMap = new HashMap<String, List<String>>();

		for(String kgram: kgrams)
		{
			mMap.put(kgram, index.getKGramIndex().getCandidates(kgram));
		}
				
		
		// Intersect the list of vocabulary types for each k-gram
		List<String> candidates = mMap.get(kgrams.get(0));
		for(int i = 1; i < kgrams.size(); i++)
		{
			candidates = Operator.andMerge(candidates, mMap.get(kgrams.get(i)));
		}
		
		
		// Post-filtering to make sure candidates match the wildcard pattern
		Iterator<String> iter = candidates.iterator();
	    while (iter.hasNext()) {
	      if (!iter.next().matches(mTerm.replace("*", ".*"))) {
	        iter.remove();
	      }
	    }
	    

	    System.out.println("Candidates to be queried:");
		for(String s: candidates)
		{
			System.out.println(s);
		}
			
	    
		List<Posting> result = null;
		
	    // Or merge the postings for the processed term from each final wildcard candidate
	    if(candidates.size() != 0)
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
	public String toString() {
		return mTerm;
	}
	
	private List<String> generateKGrams(String term, int maxK)
	{
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
		
		SortedSet<String> kgrams = new TreeSet<String>();
		
		for(String element: largestKGrams)
		{
			if(element.length() > maxK)
			{
				for(int i = 0; i + maxK <= element.length(); i++)
				{
					kgrams.add(element.substring(i, i + maxK));
				}
			}
			else
			{
				kgrams.add(element);
			}
		}
		
		return new ArrayList<String>(kgrams);
	}

}