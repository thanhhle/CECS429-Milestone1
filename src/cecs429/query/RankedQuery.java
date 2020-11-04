package cecs429.query;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.Posting;
import cecs429.text.TokenProcessor;


public class RankedQuery implements Query
{
	private static final int NUM_DOC_TO_RETURN = 10;
	
	private List<Query> mChildren;

	public RankedQuery(List<Query> children)
	{
		mChildren = children;
	}


	@Override
	public List<Posting> getPostings(Index index, TokenProcessor processor)
	{
		List<Posting> result = new ArrayList<Posting>();
		List<Posting> accumulator = new ArrayList<Posting>();

		for (Query query : mChildren)
		{
			// Get the posting lists of each term		
			List<Posting> postings = query.getPostings(index, processor);

			// Calculate query weight = ln(1 + N/dft)
			int docFreq = postings.size();
			double queryWeight = docFreq == 0 ? 0 : Math.log(1 + ((double)index.getCorpusSize())/((double)docFreq)) ;

			for (Posting posting : postings)
			{
				// Calculate document weight = 1 + ln(tftd)
				double docWeight = 1 + Math.log(posting.getTermFreq());

				// Set the posting weight to docWeight * queryWeight
				posting.setWeight(docWeight * queryWeight);
				
				// Get index of the posting in the accumulator list
				int i = accumulator.indexOf(posting);
				
				// Increase the accumulator
				if(i < 0)
				{
					accumulator.add(posting);
				}
				else
				{
					Posting p = accumulator.get(i);
					p.setWeight(p.getWeight() + posting.getWeight());
				}
			}
		}

		// Construct a priority queue and compare entry by accumulator value
		PriorityQueue<Posting> priorityQueue = new PriorityQueue<Posting>((a, b) -> Double.compare(b.getWeight(), a.getWeight()));
		
		// For each non-zero accumulator, divide the accumulator by Ld where Ld is read from the docWeights.bin file
		for (Posting p : accumulator)
		{
			if(p.getWeight() > 0.0)
			{
				double Ld = ((DiskPositionalIndex) index).getDocWeight(p.getDocumentId());
				p.setWeight(p.getWeight() / Ld);		
				priorityQueue.add(p);
			}
		}

		// Return 10 postings in the top of the priority queue
		int count = 0;
		while(priorityQueue.peek() != null && count < NUM_DOC_TO_RETURN)
		{
			result.add(priorityQueue.poll());
			count++;
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
