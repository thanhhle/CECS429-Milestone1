package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index 
{
	private final HashMap<String, List<Posting>> mMap;
	private final KGramIndex kgramIndex;
	
	/**
	 * Constructs an empty index consists of a HashMap<K, v>
	 * K is the vocabulary appears in the corpus
	 * V is list of document ID where the K appears
	 */
	public PositionalInvertedIndex() {
		mMap = new HashMap<String, List<Posting>>();
		kgramIndex = new KGramIndex(3);
	}
	
	/**
	 * Associates the given documentId with the given term in the index.
	 */
	public void addTerm(List<String> list, int documentId, int position) 
	{
		for(String term: list)
		{
			// Add term to the k-gram index
			kgramIndex.addType(term);
			
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
	
	@Override
	public List<Posting> getPostings(String term) 
	{
		return mMap.get(term) != null ? mMap.get(term) : new ArrayList<Posting>();
	}
	
	
	@Override
	public List<Posting> getPostings(List<String> terms) {
		List<Posting> result = new ArrayList<Posting>();
		for(String term: terms)
		{
			List<Posting> postings = mMap.get(term);
			
			if(postings != null)
			{
				result.addAll(postings);
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
