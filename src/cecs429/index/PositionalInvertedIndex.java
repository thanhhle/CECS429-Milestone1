package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import cecs429.text.Normalizer;


public class PositionalInvertedIndex implements Index
{
	private final HashMap<String, List<Posting>> mMap;

	private SortedSet<String> tokens;
	private KGramIndex mKGramIndex;

	/**
	 * Constructs an empty index consists of a HashMap<K, v> K is the vocabulary
	 * appears in the corpus V is list of document ID where the K appears
	 */
	public PositionalInvertedIndex()
	{
		mMap = new HashMap<String, List<Posting>>();
		mKGramIndex = new KGramIndex(3);
		tokens = new TreeSet<String>();
	}


	/**
	 * Process the token into term Associates the given documentId with the given
	 * term in the index.
	 */
	public void addTerm(String term, int documentId, int position)
	{
		if (mMap.containsKey(term))
		{
			// If the term is already recorded in the index
			int lastIndex = mMap.get(term).size() - 1;

			// Construct a new Posting object if posting with the given documentId is not created
			if (mMap.get(term).get(lastIndex).getDocumentId() != documentId)
			{
				Posting posting = new Posting(documentId, new ArrayList<Integer>());
				posting.getPositions().add(position);
				
				mMap.get(term).add(posting);
			}
			else
			{
				// Add the new position to the posting
				mMap.get(term).get(lastIndex).getPositions().add(position);
			}
		}

		else
		{
			// Construct a list of posting if the term is not added to the index
			List<Posting> postings = new ArrayList<Posting>();

			// Construct a posting with given documentId and position
			Posting posting = new Posting(documentId, new ArrayList<Integer>());
			posting.getPositions().add(position);
			
			// Add the posting to the list
			postings.add(posting);
			
			// Add the new constructed list to the associated term key
			mMap.put(term, postings);
		}
	}


	public void addToken(String token)
	{
		// Remove all non-alphanumeric and aspostropes from the token but not stemming it
		String temp = Normalizer.removeNonAlphanumeric(token).toLowerCase();
		temp = Normalizer.removeApostropes(temp);

		// Add the token to a TreeSet to build k-gram index
		tokens.add(temp);
	}
	
	
	@Override
	public void buildKGramIndex(String directoryPath)
	{
		for (String token : tokens)
		{
			mKGramIndex.addType(token);
		}
	}


	@Override
	public List<Posting> getPostings(String term, boolean withPosition)
	{
		return mMap.get(term) != null ? mMap.get(term) : new ArrayList<Posting>();
	}
	
	
	@Override
	public List<String> getVocabulary()
	{
		List<String> vocabulary = new ArrayList<String>(mMap.keySet());
		Collections.sort(vocabulary);
		return vocabulary;
	}


	@Override
	public KGramIndex getKGramIndex()
	{
		return mKGramIndex;
	}	
}
