package cecs429.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class KGramIndex
{
	private final HashMap<String, List<String>> mMap;
	private final int mKValue; 
	
	/**
	 * Constructs an empty index consists of a HashMap<K, v>
	 * K is the k-gram
	 * V is list of vocabulary types that contain that k-gram
	 *
	 * @param kValue the largest value for k for k-gram to be generated
	 */
	public KGramIndex(int kValue) {
		mMap = new HashMap<String, List<String>>();
		mKValue = kValue;
	}
	
	/**
	 * Generate the k-grams of the given vocabulary type and add them to the index
	 * @param type the vocabulary type
	 */
	public void addType(String type)
	{
		String modifiedType = "$" + type + "$";
		
		for(int k = 1; k <= mKValue; k++)
		{
			// Generate k-grams with maxK as the largest value for k, and add them to the index
			for(int i = 0; i + k <= modifiedType.length(); i++)
			{
				String kgram = modifiedType.substring(i, i + k);
				if(!kgram.equals("$"))
				{
					addKGram(kgram, type);
				}
			}
		}	
	}
	
	/**
	 * Add k-gram to the index
	 * @param kgram the k-gram
	 * @param type the vocabulary type that contains k-gram
	 */
	private void addKGram(String kgram, String type)
	{
		List<String> candidates = mMap.get(kgram) != null ? mMap.get(kgram) : new ArrayList<String>();
		if(!candidates.contains(type))
		{
			candidates.add(type);
			mMap.put(kgram, candidates);
		}
	}
	
	
	public List<String> getKGrams()
	{
		List<String> kgrams = new ArrayList<String>(mMap.keySet());
		Collections.sort(kgrams);
		return kgrams;
	}
	
	
	public List<String> getCandidates(String kgram)
	{
		List<String> candidates = mMap.get(kgram);
		if(candidates != null)
		{
			Collections.sort(candidates);
			return candidates;
		}
		
		return new ArrayList<String>();
	}
	
	
	public int getKValue()
	{
		return mKValue;
	}
	
}
