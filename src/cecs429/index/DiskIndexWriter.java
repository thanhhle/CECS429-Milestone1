package cecs429.index;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;


public class DiskIndexWriter
{
	public BTreeMap<String, Long> writeIndex(Index index, String directoryPath)
	{		
		// Get the folder named "index" inside the corpus path
		File dir = new File(directoryPath + File.separator + "index");

		// Create the folder if it does not exist
		dir.mkdir();

		// Get the file named "postings.bin" which is to store the index's postings
		File postingsFile = new File(dir.getAbsolutePath().toString(), "postings.bin");

		// Delete the file if it exists
		postingsFile.delete();
		
		// Get the file named "postings.bin" which is to store the index's postings
		File vocabTableFile = new File(dir.getAbsolutePath().toString(), "vocab_table.db");

		// Delete the file if it exists
		vocabTableFile.delete();

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
				DataOutputStream outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(postingsFile, true)));

				// Add the byte position where the posting begins
				mMap.put(vocab, byteOffset);

				// Get the list of the posting of each vocabulary term
				List<Posting> postings = index.getPostings(vocab);

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

		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		
		db.close();

		return mMap;
	}
}
