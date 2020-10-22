package cecs429.index;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import cecs429.text.TokenProcessor;


public class DiskIndexWriter
{
	public void writeIndex(Index index, String directoryPath, TokenProcessor processor)
	{		
		// Get the file named "postings.bin" which is to store the index's postings
		File postingsFile = new File(directoryPath, "postings.bin");

		// Get the file named "vocab_table.bin" which is to store mapping from vocabulary term to byte position in postings.bin
		File vocabTableFile = new File(directoryPath, "vocab_table.db");

		List<String> vocabulary = index.getVocabulary();

		DB db = DBMaker.fileDB(vocabTableFile).fileMmapEnable().make();
		BTreeMap<String, Long> mMap = db.treeMap("map")
		        .keySerializer(Serializer.STRING)
		        .valueSerializer(Serializer.LONG)
		        .create();
		
		try
		{
			long byteOffset = 0;
			for (String vocab : vocabulary)
			{
				for (String term : processor.processToken(vocab))
				{
					DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(postingsFile, true)));

					// Add the byte position where the posting begins
					mMap.put(term, byteOffset);

					// Get the list of the posting of each vocabulary term
					List<Posting> postings = index.getPostingsWithPositions(term);

					int docFreq = postings.size();

					// Write the dft to the file
					outputStream.writeInt(docFreq);

					int lastDocId = 0;
					for (Posting posting : postings)
					{
						int docId = posting.getDocumentId();

						// Write the docId using gap to the file
						outputStream.writeInt(docId - lastDocId);

						// Update the last doc Id
						lastDocId = docId;

						List<Integer> positions = posting.getPositions();

						int posFreq = positions.size();

						// Write the tftd to the file
						outputStream.writeInt(posFreq);

						int lastPos = 0;
						for (Integer pos : positions)
						{
							// Write the position using gap to the file
							outputStream.writeInt(pos - lastPos);

							// Update the last position
							lastPos = pos;
						}
					}

					// Update the byte position of where the next posting begins
					byteOffset += outputStream.size();

					outputStream.close();
				}
			}
				
		}

		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		db.close();
	}
	
	
	public void writeDocWeights(HashMap<String, Integer> termFreq, String directoryPath)
	{		
		// Get the file named "docWeights.bin" which is to store the index's postings
		File docWeightsFile = new File(directoryPath, "docWeights.bin");
		
		// Calculate document weight
		double weight = 0;
		for(String term: termFreq.keySet())
		{
			// wdt = 1 + ln(tftd)
			weight += Math.pow(1 + Math.log10(termFreq.get(term)), 2);
		}
		weight = Math.sqrt(weight);
		
		// Write the doc weight to the file
		try
		{
			DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(docWeightsFile, true)));
			outputStream.writeDouble(weight);
			outputStream.close();
		}

		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
