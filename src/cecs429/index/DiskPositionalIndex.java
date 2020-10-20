package cecs429.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


public class DiskPositionalIndex implements Index
{
	private BTreeMap<String, Long> mMap;
	private RandomAccessFile mPostingsFile;
	private RandomAccessFile mDocWeightsFile;


	public DiskPositionalIndex(String directoryPath)
	{
		try
		{
			mPostingsFile = new RandomAccessFile(new File(directoryPath + File.separator + "index", "postings.bin"), "r");
			mDocWeightsFile = new RandomAccessFile(new File(directoryPath + File.separator + "index", "docWeights.bin"), "r");
		}
		catch (FileNotFoundException e)
		{
			throw new RuntimeException(e);
		}

		File vocabTableFile = new File(directoryPath + File.separator + "index", "vocab_table.db");

		DB db = DBMaker.fileDB(vocabTableFile).fileMmapEnable().closeOnJvmShutdown().make();
		mMap = db.treeMap("map").keySerializer(Serializer.STRING).valueSerializer(Serializer.LONG).createOrOpen();
	}


	@Override
	public List<Posting> getPostings(String term)
	{
		List<Posting> result = new ArrayList<Posting>();

		Long byteOffset = mMap.get(term);

		if (byteOffset != null)
		{
			byte[] buffer = new byte[4];
			try
			{
				mPostingsFile.seek(byteOffset);

				// Read the 4 bytes for the document frequency
				mPostingsFile.read(buffer, 0, buffer.length);
				int docFreq = ByteBuffer.wrap(buffer).getInt();

				int lastDocId = 0;
				for (int i = 0; i < docFreq; i++)
				{
					// Read the 4 bytes for the gap of docId
					mPostingsFile.read(buffer, 0, buffer.length);
					int docId = ByteBuffer.wrap(buffer).getInt() + lastDocId;

					lastDocId = docId;

					// Read the 4 bytes for the position frequency
					mPostingsFile.read(buffer, 0, buffer.length);
					int posFreq = ByteBuffer.wrap(buffer).getInt();

					List<Integer> positions = new ArrayList<Integer>();

					int lastPos = 0;
					for (int k = 0; k < posFreq; k++)
					{
						// Read the 4 bytes for the gap of position
						mPostingsFile.read(buffer, 0, buffer.length);
						int pos = ByteBuffer.wrap(buffer).getInt() + lastPos;

						positions.add(pos);

						lastPos = pos;
					}

					result.add(new Posting(docId, positions));
				}
			}

			catch (FileNotFoundException e)
			{
				throw new RuntimeException(e);
			}

			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	/*
	public List<Posting> getPostings(List<String> terms)
	{
		List<Posting> result = new ArrayList<Posting>();
		for (String term : terms)
		{
			result = Operator.orMerge(result, getPostings(term));
		}
		return result;
	}
	*/

	
	public Double getDocWeights(int docId)
	{
		try
		{
			byte[] buffer = new byte[8];
			mDocWeightsFile.seek(docId * 32);
			mDocWeightsFile.read(buffer, 0, buffer.length);
            return ByteBuffer.wrap(buffer).getDouble();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
	

	@Override
	public List<String> getVocabulary()
	{
		return new ArrayList<String>(mMap.keySet());
	}


	@Override
	public void buildKGramIndex()
	{
		// TODO Auto-generated method stub
	}


	@Override
	public KGramIndex getKGramIndex()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
