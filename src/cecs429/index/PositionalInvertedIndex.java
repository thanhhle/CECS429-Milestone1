package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import cecs429.query.Operator;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.Normalizer;
import cecs429.text.TokenProcessor;

public class PositionalInvertedIndex implements Index 
{
	private final HashMap<String, List<Posting>> mMap;
	
	private Normalizer normalizer = new Normalizer();
	private SortedSet<String> tokens;
	private KGramIndex kgramIndex;
	
	/**
	 * Constructs an empty index consists of a HashMap<K, v>
	 * K is the vocabulary appears in the corpus
	 * V is list of document ID where the K appears
	 */
	public PositionalInvertedIndex() 
	{
		mMap = new HashMap<String, List<Posting>>();
		kgramIndex = new KGramIndex(3);
		tokens = new TreeSet<String>();
	}
	
	
	/**
	 * Process the token into term
	 * Associates the given documentId with the given term in the index.
	 */
	public void addToken(String token, TokenProcessor processor, int documentId, int position) 
	{
		// Remove all non-alphanumeric and aspostropes from the token but not stemming it
		String temp = normalizer.removeNonAlphanumeric(token).toLowerCase();
		temp = normalizer.removeApostropes(temp);
		
		// Add the token to a TreeSet to build k-gram index
		tokens.add(temp);
		
		
		// Process the token and add it to the index
		List<String> processedTerms = processor.processToken(token);
		
		for(String term: processedTerms)
		{			
			if(mMap.containsKey(term))
			{
				// If the term is already recorded in the index
				int lastIndex = mMap.get(term).size() - 1;
				
				if(mMap.get(term).get(lastIndex).getDocumentId() != documentId)
				{
					// If posting with the given documentId is not created
					// Construct a new Posting object with given documentId and position
					// And add it the entry associated with given term
					mMap.get(term).add(new Posting(documentId, position));
				}
				else
				{
					// If posting with the given documentId is existed
					// Add the new position to the posting
					mMap.get(term).get(lastIndex).getPositions().add(position);
				}
			}
			
			else
			{	
				// If the term is not added to the index
				// Construct a list of posting 
				List<Posting> postings = new ArrayList<Posting>();
				
				// Add the posting with given documentId and position to the list
				postings.add(new Posting(documentId, position));
				
				// Add the new constructed list to the associated term key
				mMap.put(term, postings);
			}	
		}
	}
	
	
	public void buildKGramIndex()
	{
		for(String token: tokens)
		{
			kgramIndex.addType(token);
		}
	}
	
	
	@Override
	public List<Posting> getPostings(String term) 
	{
		return mMap.get(term) != null ? mMap.get(term) : new ArrayList<Posting>();
	}
	
	
	@Override
	public List<Posting> getPostings(List<String> terms) 
	{
		List<Posting> result = new ArrayList<Posting>();
		if(terms.size() > 0)
		{
			result = mMap.get(terms.get(0)) != null ? mMap.get(terms.get(0)) : result;
			for(int i = 1; i < terms.size(); i++)
			{
				result = Operator.orMerge(result, mMap.get(terms.get(i)));
			}
		}
		
		return result;
	}
	
	
	@Override
	public List<String> getVocabulary() {
		List<String> vocabulary = new ArrayList<String>(mMap.keySet());
		Collections.sort(vocabulary);
		return vocabulary;
	}

	@Override
	public KGramIndex getKGramIndex() 
	{	
		return kgramIndex;
	}
	
}
