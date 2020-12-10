package cecs429.index;

import java.util.List;


/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting implements Comparable<Posting>
{
	private int mDocumentId;
	private int mTermFreq;
	private List<Integer> mPositions;
	private double mWeight;

	
	public Posting(int documentId, int termFreq)
	{
		mDocumentId = documentId;
		mTermFreq = termFreq;
		mWeight = 0;
	}
	

	public Posting(int documentId, List<Integer> positions)
	{
		mDocumentId = documentId;
		mPositions = positions;
		mTermFreq = positions.size();
		mWeight = 0;
	}


	public int getDocumentId()
	{
		return mDocumentId;
	}


	public int getTermFreq()
	{
		if(mPositions == null)
		{
			return mTermFreq;
		}
		
		return mPositions.size();
	}


	public List<Integer> getPositions()
	{
		return mPositions;
	}
	
	
	public double getWeight()
	{
		return mWeight;
	}
	
	
	public void setWeight(double weight)
	{
		mWeight = weight;
	}


	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}

		if (!(obj instanceof Posting))
		{
			return false;
		}

		Posting posting = (Posting) obj;
		return mDocumentId == posting.getDocumentId();
	}


	@Override
	public int compareTo(Posting p)
	{
		return this.mDocumentId - p.getDocumentId();
	}
}
