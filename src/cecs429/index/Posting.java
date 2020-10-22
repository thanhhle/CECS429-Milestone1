package cecs429.index;

import java.util.List;


/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting implements Comparable<Posting>
{
	private int mDocumentId;
	private List<Integer> mPositions;
	private int mTermFreq;

	
	public Posting(int documentId, int termFreq)
	{
		mDocumentId = documentId;
		mTermFreq = termFreq;
	}
	

	public Posting(int documentId, List<Integer> positions)
	{
		mDocumentId = documentId;
		mPositions = positions;
		mTermFreq = positions.size();
	}


	public int getDocumentId()
	{
		return mDocumentId;
	}


	public int getTermFreq()
	{
		return mTermFreq;
	}


	public List<Integer> getPositions()
	{
		return mPositions;
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
