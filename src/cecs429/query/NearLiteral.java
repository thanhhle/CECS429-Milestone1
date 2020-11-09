package cecs429.query;

import java.util.ArrayList;
import java.util.List;

import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;


public class NearLiteral implements Query
{
	private Query mQuery1;
	private Query mQuery2;
	private int mDistance;


	public NearLiteral(String query)
	{
		query = query.substring(1, query.length()-1); 
		int index = query.toLowerCase().indexOf("near/");
		int i = index + 5;
		
		while(Character.isDigit(query.charAt(i)))
		{
			i++;
		}
		
		mDistance = Integer.parseInt(query.substring(index + 5, i));

		mQuery1 = parseLiteral(query.substring(0, index).trim());
		if(mQuery1 instanceof PhraseLiteral)
		{
			mDistance += ((PhraseLiteral)mQuery1).getTermCount() - 1;
		}
		
		mQuery2 = parseLiteral(query.substring(i).trim());
	}


	private Query parseLiteral(String literal)
	{
		if(literal.charAt(0) == '"')
		{ 
			return new PhraseLiteral(literal);
		}
		else if(literal.contains("*"))
		{
			return new WildcardLiteral(literal, true);
		}
		else
		{
			return new TermLiteral(literal, true);
		}	
	}
	
	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor)
	{
		List<Posting> result = new ArrayList<Posting>();

		for (int i = 1; i <= mDistance; i++)
		{
			List<Posting> temp = Operator.positionalMerge(mQuery1.getPostings(index, processor),
					mQuery2.getPostings(index, processor), i);
			result = Operator.orMerge(result, temp);
		}

		return result;
	}
}
