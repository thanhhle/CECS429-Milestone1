package cecs429.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;


public class RankedQuery implements Query
{
	private List<Query> mChildren;
	private int mCorpusSize;


	public RankedQuery(List<Query> children, int corpusSize)
	{
		mChildren = children;
		mCorpusSize = corpusSize;
	}


	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor)
	{
		List<Posting> result = new ArrayList<Posting>();
		HashMap<Posting, Double> accumulator = new HashMap<Posting, Double>();

		for (Query query : mChildren)
		{
			// Get the posting lists of each term		
			List<Posting> postings = query.getPostings(index, processor);

			// Calculate query weight = ln(1 + N/dft)
			int docFreq = postings.size();
			double queryWeight = Math.log10(1 + mCorpusSize / docFreq);

			for (Posting posting : postings)
			{
				// Calculate document weight = 1 + ln(tftd)
				double docWeight = 1 + Math.log10(posting.getTermFreq());

				// Increase accumulator by docWeight * queryWeight
				if (accumulator.get(posting) == null)
				{
					accumulator.put(posting, 0.0);
				}

				accumulator.put(posting, accumulator.get(posting) + (docWeight * queryWeight));
			}
		}

		// Construct a priority queue with capacity of 10 and compare entry by accumulator value
		PriorityQueue<Entry<Posting, Double>> priorityQueue = new PriorityQueue<Entry<Posting, Double>>((a, b) -> b.getValue().compareTo(a.getValue()));

		// For each non-zero accumulator, divide the accumulator by Ld where Ld is read from the docWeights.bin file
		for (Entry<Posting, Double> acc : accumulator.entrySet())
		{
			int docId = acc.getKey().getDocumentId();
			double Ld = ((DiskPositionalIndex) index).getDocWeight(docId);
			acc.setValue(acc.getValue() / Ld);

			priorityQueue.add(acc);
		}

		for (int i = 0; i < 10; i++)
		{
			Entry<Posting, Double> entry = priorityQueue.poll();
			if (entry == null)
			{
				break;
			}
			
			result.add(entry.getKey());
		}

		return result;
	}


	@Override
	public String toString() 
	{
		String s = "";
		for(Query query: mChildren)
		{
			s += query.toString() + " ";
		}
		return s;
	}
	
}
