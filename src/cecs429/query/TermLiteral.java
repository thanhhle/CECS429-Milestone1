package cecs429.query;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements Query 
{
	private String mTerm;
	
	
	public TermLiteral(String term) 
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
		// return index.getPostings(processor.processToken(mTerm));
		
		List<Posting> result = new ArrayList<Posting>();
		for (String term: processor.processToken(mTerm))
		{
			result = Operator.orMerge(result, index.getPostingsWithPositions(term));
		}

		for(Posting posting: result)
		{
			System.out.println(posting.getDocumentId());
		}
		
		return result;
	}
	
	
	@Override
	public String toString() 
	{
		return mTerm;
	}
}
