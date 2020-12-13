package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenStream;


public class RocchioClassifier
{
	private final SortedSet<String> trainingSet;
	private final HashMap<Author, Index> trainingIndexes;
	private final HashMap<Author, HashMap<String, Double>> centroids;
	
	private final DocumentCorpus disputedCorpus;
	private final Index disputedIndex;
	

	public RocchioClassifier(String directoryPath)
	{
		trainingIndexes = new HashMap<Author, Index>();
		trainingSet = new TreeSet<String>();
		for(Author author: Author.values())
		{
			String path = directoryPath + File.separator + author;
			DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(path).toAbsolutePath());
			Index index = indexCorpus(corpus);
			
			trainingIndexes.put(author, index);
			trainingSet.addAll(index.getVocabulary());
		}
		
		String disputedPath = directoryPath + File.separator + "DISPUTED";
		disputedCorpus = DirectoryCorpus.loadDirectory(Paths.get(disputedPath).toAbsolutePath());
		disputedIndex = indexCorpus(disputedCorpus);
		
		trainingSet.addAll(disputedIndex.getVocabulary());
		
		centroids = calculateCentroids();
	}


	public void classify()
	{
		System.out.println("\nClassifying...\n");
		
		for (Document doc : disputedCorpus.getDocuments())
		{
			HashMap<Author, Double> distances = new HashMap<Author, Double>();
			
			for(Author author: Author.values())
			{
				double distance = 0.0;
				
				HashMap<String, Double> centroid = centroids.get(author);
				
				for(String term: trainingSet)
				{
					List<Posting> postings = disputedIndex.getPostings(term, true);
					
					double termVector = 0.0;
					for(Posting posting: postings)
					{
						if(posting.getDocumentId() == doc.getId())
						{
							double docWeight = 1 + Math.log(posting.getTermFreq());
							double docLength = disputedIndex.getDocLength(posting.getDocumentId());
							termVector += docWeight / docLength;
						}
					}
					
					distance += Math.pow(centroid.get(term) - termVector, 2);
					
					/*
					if (centroid.get(term) != null)
					{
						distance += Math.pow(centroid.get(term) - termVector, 2);
					}
					else
					{
						distance += Math.pow(termVector, 2);
					}
					*/
				}
				
				distances.put(author, Math.sqrt(distance));
			}
			
			for(Author author: Author.values()) 
			{
				System.out.println("Distance to " + author + " for \"" + doc.getTitle() + "\": " + String.format("%.6f  ", distances.get(author)));
			}
	
			System.out.println("Classify \"" + doc.getTitle() + "\" as " + getAuthorWithLowestDistance(distances) + "\n");
		}
	}
	
	
	public void classify(String documentName)
	{
		Document document = null;
		for (Document doc : disputedCorpus.getDocuments())
		{
			if(doc.getTitle().equals(documentName))
			{
				document = doc;
			}
		}
		
		if(document == null)
		{
			System.out.println("Document not found");
			return;
		}
		
		HashMap<Author, Double> distances = new HashMap<Author, Double>();
		
		for(Author author: Author.values())
		{
			double distance = 0.0;
			
			HashMap<String, Double> centroid = centroids.get(author);
			
			for(String term: trainingSet)
			{
				List<Posting> postings = disputedIndex.getPostings(term, true);
				
				double termVector = 0.0;
				for(Posting posting: postings)
				{
					if(posting.getDocumentId() == document.getId())
					{
						double docWeight = 1 + Math.log(posting.getTermFreq());
						double docLength = disputedIndex.getDocLength(posting.getDocumentId());
						termVector += docWeight / docLength;
					}
				}
				
				if (centroid.get(term) != null)
				{
					distance += Math.pow(centroid.get(term) - termVector, 2);
				}
				else
				{
					distance += Math.pow(termVector, 2);
				}
			}
			
			distances.put(author, Math.sqrt(distance));
		}
		
		for(Author author: Author.values()) 
		{
			System.out.println("Distance to " + author + " for \"" + documentName + "\": " + String.format("%.6f  ", distances.get(author)));
		}

		System.out.println("Classify \"" + documentName + "\" as " + getAuthorWithLowestDistance(distances) + "\n");	
	}
	
	
	public void printCentroids()
	{
		printCentroids(trainingSet.size());
	}
	
	
	public void printCentroids(int count)
	{
		for(Author author: Author.values())
		{
			System.out.println("The first " + count + " components of centroid vector for " + author);
			
			HashMap<String, Double> centroid = centroids.get(author);
			SortedSet<String> terms = new TreeSet<String>(centroid.keySet());

			for(int i = 0; i < count; i++)
			{
				String term = (String) terms.toArray()[i];
				System.out.print(String.format("%.9f  ", centroid.get(term)));
			}
			
			System.out.println("\n");
		}
	}
	
	
	public void printNormalizedVectors(int count, String documentName)
	{
		System.out.println("The first " + count + " components (alphabetically) of the normalized vector for " + documentName);
		
		Document document = null;
		for (Document doc : disputedCorpus.getDocuments())
		{
			if(doc.getTitle().equals(documentName))
			{
				document = doc;
			}
		}
		
		if(document == null)
		{
			System.out.println("Document not found!");
			return;
		}
		
		int i = 0;
		while(i < count)
		{
			String term = (String) trainingSet.toArray()[i];
			List<Posting> postings = disputedIndex.getPostings(term, true);
			
			double termVector = 0.0;
			for(Posting posting: postings)
			{
				if(posting.getDocumentId() == document.getId())
				{
					double docWeight = 1 + Math.log(posting.getTermFreq());
					double docLength = disputedIndex.getDocLength(posting.getDocumentId());
					termVector += docWeight / docLength;
				}
			}
			
			System.out.print(String.format("%.9f  ", termVector));
			
			i++;
		}
		
		System.out.println("\n");
	}
	
	
	public void printTrainingSet()
	{
		printTrainingSet(trainingSet.size());
	}
	
	
	public void printTrainingSet(int count)
	{
		for(int i = 0; i < count; i++)
		{
			System.out.print(trainingSet.toArray()[i] + "  ");
		}
		
		System.out.println("\n");
	}
	
	
	private Author getAuthorWithLowestDistance(HashMap<Author, Double> distances)
	{
		PriorityQueue<Entry<Author, Double>> priorityQueue = new PriorityQueue<Entry<Author, Double>>((a, b) -> Double.compare(a.getValue(), b.getValue()));
		priorityQueue.addAll(distances.entrySet());
		
		return priorityQueue.peek().getKey();
	}
	
	
	private HashMap<Author, HashMap<String, Double>> calculateCentroids()
	{
		HashMap<Author, HashMap<String, Double>> centroids = new HashMap<Author, HashMap<String, Double>>();
		
		for(Author author: Author.values())
		{
			HashMap<String, Double> centroid = new HashMap<String, Double>();
			
			Index index = trainingIndexes.get(author);
			
			for(String term: trainingSet)
			{
				double termVector = 0.0;
				
				List<Posting> postings = index.getPostings(term, true);
				for(Posting posting: postings)
				{
					// Calculate document weight wdt = 1 + ln(tftd)
					double docWeight = 1 + Math.log(posting.getTermFreq());
					double docLength = index.getDocLength(posting.getDocumentId());
					
					termVector += docWeight / docLength;
				}
				
				centroid.put(term, termVector / (double)index.getCorpusSize());
			}
			
			centroids.put(author, centroid);
		}
		
		return centroids;
	}
	
	
	private Index indexCorpus(DocumentCorpus corpus)
	{
		// Construct an inverted index
		PositionalInvertedIndex index = new PositionalInvertedIndex(corpus.getCorpusSize());

		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> documents = corpus.getDocuments();

		// Iterate through the documents, process the tokens and add processed terms to the index with addPosting.
		for (Document doc : documents)
		{
			try
			{
				TokenStream tokenStream = new EnglishTokenStream(doc.getContent());

				HashMap<String, Integer> termFreq = new HashMap<String, Integer>();

				int position = 0;
				for (String token : tokenStream.getTokens())
				{
					List<String> processedTerms = new AdvancedTokenProcessor().processToken(token);

					for (String term : processedTerms)
					{
						index.addTerm(term, doc.getId(), position);

						int freq = termFreq.get(term) != null ? termFreq.get(term) + 1 : 1;
						termFreq.put(term, freq);
					}

					position++;

					index.addToken(token);
				}

				tokenStream.close();

				// Calculate document weight
				double totalWeightSquared = 0;
				for (String term : termFreq.keySet())
				{
					// wdt = 1 + ln(tftd)
					totalWeightSquared += Math.pow(1 + Math.log(termFreq.get(term)), 2);
				}
				double length = Math.sqrt(totalWeightSquared);
				
				index.addDocLength(doc.getId(), length);
			}

			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		// Build a k-gram index
		// index.buildKGramIndex(directoryPath);

		return index;
	}
}