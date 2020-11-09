package cecs429.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cecs429.index.Index;
import cecs429.query.Operator;
import cecs429.query.Query;
import cecs429.query.TermLiteral;
import cecs429.text.Normalizer;
import cecs429.text.TokenProcessor;


public class SpellingCorrector
{
	private static final double JACCARD_THRESHOLD = 0.12;
	private static final int DOCFREQ_THRESHOLD = 5;
	private static final String BOOLEAN_OPEPATOR_REGEX = "^[near/\\d+]+$";

	private final Index mIndex;
	private final TokenProcessor mProcessor;

	private String mQuery;
	private Map<String, List<Integer>> mOperators;
	
	
	public SpellingCorrector(String query, Index index, TokenProcessor processor)
	{
		mIndex = index;
		mProcessor = processor;
		mQuery = query;
		mOperators = new HashMap<String, List<Integer>>();
		
		mOperators.put("hyphen", new ArrayList<Integer>());
		mOperators.put("openingBracket", new ArrayList<Integer>());
		mOperators.put("closingBracket", new ArrayList<Integer>());
		mOperators.put("openingQuotation", new ArrayList<Integer>());
		mOperators.put("closingQuotation", new ArrayList<Integer>());
	}

	
	/**
	 * Handle the spelling correction for the given query and return the most-likely correction of the query
	 * @param query the input query
	 * @return the most-likely correction of the query
	 */
	public String getSpellingCorrection()
	{
		// Ignore the query which contains less than 2 characters
		if(mQuery.length() < 2)
		{
			return mQuery;
		}

		String result = new String();
		
		// Get the list of query terms
		List<String> queryTerms = Arrays.asList(mQuery.toLowerCase().split(" "));
		
		// Get the list of indexes of the misspelled term 
		List<Integer> indexes = getMisspelledTermIndexes(queryTerms);
		
		// Get and replace the spelling correction for each misspelled term
		if(indexes.size() != 0)
		{
			System.out.println("\nHandling spelling correction...");
			
			for(Integer index: indexes)
			{
				String correction = getCorrection(normalizeTerm(queryTerms.get(index)));
				if (correction != null && !correction.equals(queryTerms.get(index)))
				{
					queryTerms.set(index, correction);
				}
			}
		}
		
		// Reform the query	
		for(int i = 0; i < queryTerms.size(); i++)
		{
			if(mOperators.get("hyphen").contains(i))
			{
				result += "-";
			}
			
			if(mOperators.get("openingBracket").contains(i))
			{
				result += "[";
			}
			
			if(mOperators.get("openingQuotation").contains(i))
			{
				result += "\"";
			}
			
			result += queryTerms.get(i);
			
			if(mOperators.get("closingQuotation").contains(i))
			{
				result += "\"";
			}
			
			if(mOperators.get("closingBracket").contains(i))
			{
				result += "]";
			}
			
			if(i < queryTerms.size() - 1)
			{
				result += " ";
			}
		}
		
		return result;
	}
	
	
	/**
	 * Split the query into a list of terms
	 * @param query the input query
	 * @return list of query terms
	 */
	private String normalizeTerm(String term)
	{
		// Remove non-alphanumeric characters from the term
		String result = Normalizer.removeNonAlphanumeric(term);
		
		// Remove apostropes from the term
		result = Normalizer.removeApostropes(term);

		return result;
	}
	
	
	/**
	 * Get a list of indexes of query terms where the spelling correction is needed
	 * @param queryTerms list of query terms
	 * @return list of indexes
	 */
	private List<Integer> getMisspelledTermIndexes(List<String> queryTerms)
	{
		List<Integer> result = new ArrayList<Integer>();

		for (int i = 0; i < queryTerms.size(); i++)
		{
			if (!queryTerms.get(i).matches(BOOLEAN_OPEPATOR_REGEX) && !queryTerms.get(i).contains("*"))
			{
				if (queryTerms.get(i).charAt(0) == '-')
				{
					queryTerms.set(i, queryTerms.get(i).substring(1));
				
					List<Integer> indexes = mOperators.get("hyphen");
					indexes.add(i);
					mOperators.put("hyphen", indexes);
				}
				
				if (queryTerms.get(i).charAt(0) == '[')
				{
					queryTerms.set(i, queryTerms.get(i).substring(1));
				
					List<Integer> indexes = mOperators.get("openingBracket");
					indexes.add(i);
					mOperators.put("openingBracket", indexes);
				}
				
				if (queryTerms.get(i).charAt(queryTerms.get(i).length() - 1) == ']')
				{
					queryTerms.set(i, queryTerms.get(i).substring(0, queryTerms.get(i).length() - 1));
				
					List<Integer> indexes = mOperators.get("closingBracket");
					indexes.add(i);
					mOperators.put("closingBracket", indexes);
				}
				
				if (queryTerms.get(i).charAt(0) == '"')
				{
					queryTerms.set(i, queryTerms.get(i).substring(1));
				
					List<Integer> indexes = mOperators.get("openingQuotation");
					indexes.add(i);
					mOperators.put("openingQuotation", indexes);
				}
				
				if (queryTerms.get(i).charAt(queryTerms.get(i).length() - 1) == '"')
				{
					queryTerms.set(i, queryTerms.get(i).substring(0, queryTerms.get(i).length() - 1));
				
					List<Integer> indexes = mOperators.get("closingQuotation");
					indexes.add(i);
					mOperators.put("closingQuotation", indexes);
				}
				
					
				Query literal = new TermLiteral(queryTerms.get(i), false);

				// Add the index of query term whose document frequency is below the threshold to the return list
				if (literal.getPostings(mIndex, mProcessor).size() < DOCFREQ_THRESHOLD)
				{
					result.add(i);
				}
			}
		}

		return result;
	}


	/**
	 * Generate the spelling correction for the given term
	 * @param term misspelled term
	 * @return spelling correction of the input term
	 */
	private String getCorrection(String term)
	{
		String result = null;
        
		// Generate kgrams for the term
		List<String> termKGrams = generateKGrams(term);

		// Retrieve and union the list of candidates that have kgrams in common with term
		List<String> candidates = new ArrayList<String>();
		for (String kgram : termKGrams)
		{
			candidates = Operator.orMerge(candidates, mIndex.getKGramIndex().getCandidates(kgram));
		}
		
		// Assume the first letter is entered correctly
		String first2Gram = "$" + Character.toString(term.charAt(0));
		List<String> first2GramCandidates = mIndex.getKGramIndex().getCandidates(first2Gram);
		if(first2GramCandidates.size() != 0)
		{
			candidates = Operator.andMerge(candidates, first2GramCandidates);
		}
		
		// List of candidates to calculate the edit distannce
		List<String> editDistanceCandidates = new ArrayList<String>();

		// Calculate the Jaccard coefficient for each candidate
		for (String candidate : candidates)
		{
			// Get the k-grams for the candidate
			List<String> candidateKGrams = generateKGrams(candidate);

			// Calculate the Jaccard coefficient
			int intersectionSize = Operator.andMerge(termKGrams, candidateKGrams).size();
			int unionSize = termKGrams.size() + candidateKGrams.size() - intersectionSize;
			double jaccard = (double) intersectionSize / unionSize;

			// Add candidate whose coefficient exceeds the threshold to a list to calculate the edit distance
			if (jaccard > JACCARD_THRESHOLD)
			{
				editDistanceCandidates.add(candidate);
			}
		}

		// Return null if no candidate is added to the list
		if (editDistanceCandidates.size() == 0)
		{
			return result;
		}

		// Construct a list to store candidates with lowest edit distance
		List<String> lowestEDCandidates = new ArrayList<String>();
		
		// Set the first candidate as the one with lowest edit distance
		lowestEDCandidates.add(editDistanceCandidates.get(0));

		// Set the edit distance of the first candidate as the lowest edit distance
		int lowestEditDistance = calculateEditDistance(term, editDistanceCandidates.get(0));

		// Loop through the list to look for the candidates with lowest edit distance
		for (int i = 1; i < editDistanceCandidates.size(); i++)
		{
			int editDistance = calculateEditDistance(term, editDistanceCandidates.get(i));
			if (editDistance < lowestEditDistance)
			{
				// Update the lowest edit distance
				lowestEditDistance = editDistance;
				
				// Add the candidate with lowest edit distance
				lowestEDCandidates.clear();
				lowestEDCandidates.add(editDistanceCandidates.get(i));
			}
			else if (editDistance == lowestEditDistance)
			{
				// Add the candidate to the current list if it has the same lowest edit distance
				lowestEDCandidates.add(editDistanceCandidates.get(i));
			}
		}
	
		// If multiple candidates tie, select the candidate with highest document frequency 
		if (lowestEDCandidates.size() > 1)
		{
			// Set the first candidate as the one with highest document frequency
			int highestDocFreq = mIndex.getPostings(Normalizer.stemToken(lowestEDCandidates.get(0)), false).size();

			// Compare the document frequency with the remaining
			for (int i = 1; i < lowestEDCandidates.size(); i++)
			{
				int docFreq = mIndex.getPostings(Normalizer.stemToken(lowestEDCandidates.get(i)), false).size();
				if (docFreq > highestDocFreq)
				{
					// Update the highest document frequency
					highestDocFreq = docFreq;
					
					// Update the candidate with highest document frequency to be returned
					result = lowestEDCandidates.get(i);
				}
			}
		}
		else
		{
			result = lowestEDCandidates.get(0);
		}

		return result;
	}


	/**
	 * Generate kgrams for the given term
	 * @param term
	 * @return list of kgrams
	 */
	private List<String> generateKGrams(String term)
	{
		List<String> result = new ArrayList<String>();

		String modifiedTerm = "$" + term + "$";
		for (int k = 1; k <= mIndex.getKGramIndex().getKValue(); k++)
		{
			for (int i = 0; i + k <= modifiedTerm.length(); i++)
			{
				String kgram = modifiedTerm.substring(i, i + k);
				if (!kgram.equals("$"))
				{
					result.add(kgram);
				}
			}
		}

		return result;
	}

	
	/**
	 * Measure the number of "edit operations" to turn string s1 into string s2 by Levenshtein Distance algorithm
	 * @param s1
	 * @param s2
	 * @return edit distance between s1 and s2
	 */
	private int calculateEditDistance(String s1, String s2)
	{
		if (s1.length() == 0)		return s2.length();
		if (s2.length() == 0)		return s1.length();

		int deletion = calculateEditDistance(s1.substring(1), s2) + 1;
		int insertion = calculateEditDistance(s1, s2.substring(1)) + 1;
		int substitution = calculateEditDistance(s1.substring(1), s2.substring(1)) + costOfSubstitution(s1.charAt(0), s2.charAt(0));
		
		return Math.min(deletion, Math.min(insertion, substitution));
	}


	/**
	 * Measure the number of "edit operations" to substitute character a with character b
	 * @param c1
	 * @param c2
	 * @return cost to substitute c1 with c2
	 */
	private int costOfSubstitution(char c1, char c2)
	{
		return c1 == c2 ? 0 : 1;
	}
}
