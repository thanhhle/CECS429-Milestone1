package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class KNNClassifier
{
	private final SortedSet<String> trainingSet;
	private final HashMap<Author, Index> trainingIndexes;
	private final HashMap<Author, DocumentCorpus> trainingCorpuses;
	private final HashMap<String, HashMap<String, Double>> documentVectors;
	
	private final DocumentCorpus disputedCorpus;
	private final Index disputedIndex;
	

	public KNNClassifier(String directoryPath)
	{
		trainingIndexes = new HashMap<Author, Index>();
		trainingCorpuses = new HashMap<Author, DocumentCorpus>();
		trainingSet = new TreeSet<String>();
		
		for(Author author: Author.values())
		{
			String path = directoryPath + File.separator + author;
			DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(path).toAbsolutePath());
			Index index = indexCorpus(corpus);
			
			trainingIndexes.put(author, index);
			trainingCorpuses.put(author, corpus);
			trainingSet.addAll(index.getVocabulary());
		}
		
		String disputedPath = directoryPath + File.separator + "DISPUTED";
		disputedCorpus = DirectoryCorpus.loadDirectory(Paths.get(disputedPath).toAbsolutePath());
		disputedIndex = indexCorpus(disputedCorpus);
		
		trainingSet.addAll(disputedIndex.getVocabulary());
		
		documentVectors = calculateDocumentVectors();
	}


	public void classify(int nearestDocCount)
	{
		System.out.println("\nClassifying...\n");
		
		for (Document doc : disputedCorpus.getDocuments())
		{
			HashMap<String, Double> documentDistances = new HashMap<String, Double>();
			
			for(String documentTitle: documentVectors.keySet())
			{
				// System.out.println(documentTitle);
				double distance = 0.0;

				HashMap<String, Double> documentVector = documentVectors.get(documentTitle);
				
				int count = 0;
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
					
					if(count < 10)
					{
						System.out.print(String.format("%.9f  ", termVector));
					}
						
					if(count < 10)
					{
						// System.out.print(String.format("%.9f  ", documentVector.get(term)));
					}
					
					count++;
					distance += Math.pow(documentVector.get(term) - termVector, 2);
				}
				// System.out.println("\n");
				
				documentDistances.put(documentTitle, Math.sqrt(distance));
			}
			
			System.out.println();
			
			KNNResult result = getNearestDocuments(nearestDocCount, documentDistances);
			
			HashMap<String, Double> nearestDocuments = result.getNearestDocuments();
			Author author = result.getAuthor();
			
			System.out.println("\"" + doc.getTitle() + "\" nearest to:");
			
			int i = 1;
			for(String documentTitle: nearestDocuments.keySet())
			{
				System.out.println(i + ": \"" + documentTitle + "\" (" + String.format("%.6f", nearestDocuments.get(documentTitle)) + ")");	
				i++;
			}
			
			System.out.println("Classify \"" + doc.getTitle() + "\" as " + author + "\n");
		}
	}
	
	
	/*
	public void classify(String documentTitle)
	{
		Document document = null;
		for (Document doc : disputedCorpus.getDocuments())
		{
			if(doc.getTitle().equals(documentTitle))
			{
				document = doc;
			}
		}
		
		if(document == null)
		{
			System.out.println("Document not found!\n");
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
				
				distance += Math.pow(centroid.get(term) - termVector, 2);
			}
			
			distances.put(author, Math.sqrt(distance));
		}
		
		for(Author author: Author.values()) 
		{
			System.out.println("Distance to " + author + " for \"" + documentTitle + "\": " + String.format("%.6f  ", distances.get(author)));
		}

		System.out.println("Classify \"" + documentTitle + "\" as " + getAuthorWithLowestDistance(distances) + "\n");	
	}
	*/
	
	
	public void printNormalizedVectors(String documentTitle)
	{
		printNormalizedVectors(trainingSet.size(), documentTitle);
	}
	
	
	public void printNormalizedVectors(int count, String documentTitle)
	{
		System.out.println("First " + count + " components of the normalized vector for " + documentTitle);
		HashMap<String, Double> documentVector = documentVectors.get(documentTitle);
			
		int i = 0;
		while(i < count)
		{
			String term = (String) trainingSet.toArray()[i];
			System.out.print(String.format("%.9f  ", documentVector.get(term)));
			
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
	
	
	private KNNResult getNearestDocuments(int nearestDocCount, HashMap<String, Double> documentDistances)
	{
		PriorityQueue<Entry<String, Double>> priorityQueue = new PriorityQueue<Entry<String, Double>>((a, b) -> Double.compare(a.getValue(), b.getValue()));
		priorityQueue.addAll(documentDistances.entrySet());
		
		HashMap<Author, Integer> authorVotes = new HashMap<Author, Integer>();
		for(Author author: Author.values())
		{
			authorVotes.put(author, 0);
		}
		
		HashMap<String, Double> nearestDocuments = new HashMap<String, Double>();
		
		// Return 10 postings in the top of the priority queue
		int count = 0;
		while (priorityQueue.peek() != null && count < nearestDocCount)
		{
			Entry<String, Double> entry = priorityQueue.poll();
			nearestDocuments.put(entry.getKey(), entry.getValue());
			count++;
		}

		// Count vote for each author
		for(String documentTitle: nearestDocuments.keySet())
		{
			Author author = getDocumentAuthor(documentTitle);
			authorVotes.put(author, authorVotes.get(author) + 1);
		}
		
		
		Author highestVoteAuthor = Author.HAMILTON;
		int highestVote = authorVotes.get(highestVoteAuthor);
		
		// Get author with most vote
		for(Author author: Author.values())
		{
			int vote = authorVotes.get(author);
			if(vote > highestVote && author != highestVoteAuthor)
			{
				highestVote = vote;
				highestVoteAuthor = author;
			}
			else if(vote == highestVote && author != highestVoteAuthor)
			{
				HashMap<Author, Double> temp = new HashMap<Author, Double>();
				for(Author au: Author.values())
				{
					temp.put(au, 0.0);
				}
	
				for(String documentTitle : nearestDocuments.keySet())
				{
					Author au = getDocumentAuthor(documentTitle);
					double distance = nearestDocuments.get(documentTitle);
					temp.put(au, temp.get(au) + distance);
				}
				
				Author higherVoteAuthor = Collections.max(temp.entrySet(), Map.Entry.comparingByValue()).getKey();
				highestVoteAuthor = higherVoteAuthor;
			}
		}
		
		return new KNNResult(highestVoteAuthor, nearestDocuments);
	}
	
	
	private Author getDocumentAuthor(String documentTitle)
	{
		for(Author author: Author.values())
		{
			DocumentCorpus corpus = trainingCorpuses.get(author);
			for(Document doc: corpus.getDocuments())
			{
				if(doc.getTitle().equals(documentTitle))
				{
					return author;
				}
			}
		}
		
		return null;
	}
	
	
	private HashMap<String, HashMap<String, Double>> calculateDocumentVectors()
	{
		HashMap<String, HashMap<String, Double>> documentVectors = new HashMap<String, HashMap<String, Double>>();
		
		for(Author author: Author.values())
		{		
			DocumentCorpus corpus = trainingCorpuses.get(author);
			Index index = trainingIndexes.get(author);
			
			for(Document doc: corpus.getDocuments())
			{
				HashMap<String, Double> termVectors = new HashMap<String, Double>();
				for(String term: trainingSet)
				{
					double termVector = 0.0;
					
					List<Posting> postings = index.getPostings(term, true);
					for(Posting posting: postings)
					{
						double docWeight = 1 + Math.log(posting.getTermFreq());
						double docLength = index.getDocLength(posting.getDocumentId());
						
						termVector += docWeight / docLength;
					}
					
					termVectors.put(term, termVector);
				}
				
				documentVectors.put(doc.getTitle(), termVectors);
			}
		}
		
		return documentVectors;
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


class KNNResult
{
	private Author author;
	private HashMap<String, Double> nearestDocuments;


	public KNNResult(Author author,  HashMap<String, Double> nearestDocuments)
	{
		this.author = author;
		this.nearestDocuments = nearestDocuments;
	}


	public Author getAuthor()
	{
		return this.author;
	}


	public HashMap<String, Double> getNearestDocuments()
	{
		return this.nearestDocuments;
	}
}