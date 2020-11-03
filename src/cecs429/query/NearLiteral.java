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
		query = query.substring(1, query.length() - 1);
		String[] tokens = query.split(" ");
		mQuery1 = new TermLiteral(tokens[0], true);
		mQuery2 = new TermLiteral(tokens[2], true);
		mDistance = Integer.parseInt(tokens[1].substring(5));
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
