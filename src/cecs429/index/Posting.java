package cecs429.index;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting implements Comparable<Posting>
{
	private int mDocumentId;
	private List<Integer> mPositions;
	
	
	public Posting(int documentId, int position) 
	{
		mDocumentId = documentId;
		mPositions = new ArrayList<Integer>();
		mPositions.add(position);
	}
	
	
	public Posting(int documentId, List<Integer> positions)
	{
		mDocumentId = documentId;
		mPositions = positions;
	}
	
	
	public int getDocumentId()
	{
		return mDocumentId;
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
        return mDocumentId == posting.getDocumentId() &&
                mPositions.equals(posting.getPositions());
	}

	
	@Override
	public int compareTo(Posting p)
	{
		return this.mDocumentId - p.getDocumentId();
	}
}
