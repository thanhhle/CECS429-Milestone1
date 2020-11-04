package cecs429.index;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


public class DiskPositionalIndex implements Index
{
	private BTreeMap<String, Long> mVocabTable;
	private RandomAccessFile mPostingsFile;
	private RandomAccessFile mDocWeightsFile;
	
	private KGramIndex mKGramIndex;
	private final int mCorpusSize;
	
	public DiskPositionalIndex(String directoryPath, int corpusSize)
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
		DB vocabTableDB = DBMaker.fileDB(vocabTableFile).fileMmapEnable().closeOnJvmShutdown().make();
		mVocabTable = vocabTableDB.treeMap("map").keySerializer(Serializer.STRING).valueSerializer(Serializer.LONG).createOrOpen();
		
		mKGramIndex = new KGramIndex(3);
		this.buildKGramIndex(directoryPath);
		
		mCorpusSize = corpusSize;
	}

	
	public List<Posting> getPostings(String term, boolean withPosition)
	{
		List<Posting> result = new ArrayList<Posting>();

		Long byteOffset = mVocabTable.get(term);

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
					int termFreq = ByteBuffer.wrap(buffer).getInt();
					
					if(withPosition)
					{
						List<Integer> positions = new ArrayList<Integer>();

						int lastPos = 0;
						for (int k = 0; k < termFreq; k++)
						{
							// Read the 4 bytes for the gap of position
							mPostingsFile.read(buffer, 0, buffer.length);
							int pos = ByteBuffer.wrap(buffer).getInt() + lastPos;

							positions.add(pos);

							lastPos = pos;
						}

						// Add new created posting with docId and list of postitions
						result.add(new Posting(docId, positions));
					}
					else
					{
						// Add new created posting with docId and term freqeuncy
						result.add(new Posting(docId, termFreq));
						
						// Skip over the positions
						mPostingsFile.skipBytes(4 * termFreq);
					}		
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

	
	public Double getDocWeight(int docId)
	{
		try
		{
			byte[] buffer = new byte[8];
			mDocWeightsFile.seek(docId * 8);
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
		return new ArrayList<String>(mVocabTable.keySet());
	}


	@Override
	public void buildKGramIndex(String directoryPath)
	{
		System.out.println("\nLoading the kgram index...");
		try
		{
			RandomAccessFile candidatesFile = new RandomAccessFile(new File(directoryPath + File.separator + "index", "candidates.bin"), "r");
			
			File kgramTableFile = new File(directoryPath + File.separator + "index", "kgram_table.db");
			DB kgramTableDB = DBMaker.fileDB(kgramTableFile).fileMmapEnable().closeOnJvmShutdown().make();
			BTreeMap<String, Long> kgramTable = kgramTableDB.treeMap("treemap")
												.keySerializer(Serializer.STRING)
												.valueSerializer(Serializer.LONG).createOrOpen();
			
			for(String kgram: kgramTable.keySet())
			{
				Long byteOffset = kgramTable.get(kgram);
				if (byteOffset != null)
				{
					byte[] buffer = new byte[4];
					
					candidatesFile.seek(byteOffset);
					
					// Read the 4 bytes for the candidate frequency
					candidatesFile.read(buffer, 0, buffer.length);
					int candidateFreq = ByteBuffer.wrap(buffer).getInt();

					for (int i = 0; i < candidateFreq; i++)
					{
						// Read the 4 bytes for candidate size
						candidatesFile.read(buffer, 0, buffer.length);
						int candidateSize = ByteBuffer.wrap(buffer).getInt();

						byte[] candidateBuffer = new byte[candidateSize];
						
						// Read the bytes for the candidate
						candidatesFile.read(candidateBuffer, 0, candidateBuffer.length);
						
						String candidate = new String(candidateBuffer, StandardCharsets.UTF_8);
						
						mKGramIndex.addKGram(kgram, candidate);
					}
				}
				
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


	@Override
	public KGramIndex getKGramIndex()
	{
		return mKGramIndex;
	}
	
	
	public int getCorpusSize()
	{
		return mCorpusSize;
	}
}
