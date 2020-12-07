package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.SortedSet;
import java.util.TreeSet;


import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.TokenProcessor;
import cecs429.text.TokenStream;
import cecs429.classification.Author;


public class BayesianClassifier implements Classifier
{
	private final String directoryPath;
	private final HashMap<Author, Index> indexes;
	private final HashMap<Author, List<Double>> termProbs;
	private final List<String> discriminatingTerms;
	
	private int totalDocCount = 0;
	private TokenProcessor processor = new AdvancedTokenProcessor();
	
	/*
	private final List<Double> hamiltonTermProbs;
	private final List<Double> jayTermProbs;
	private final List<Double> madisonTermProbs;
	
	private final double hamiltonDocCount;
	private final double jayDocCount;
	private final double madisonDocCount;
	*/


	public BayesianClassifier(String directoryPath, int termCount)
	{
		this.directoryPath = directoryPath;
		
		this.indexes = new HashMap<Author, Index>();
		
		this.totalDocCount = 0;	
		
		SortedSet<String> trainingTerms = new TreeSet<String>();
		for(Author author: Author.values())
		{
			String path = directoryPath + File.separator + author;
			DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(path).toAbsolutePath());
			Index index = indexCorpus(corpus);
			
			this.indexes.put(author, index);
			this.totalDocCount += index.getCorpusSize();
			trainingTerms.addAll(index.getVocabulary());
		}

		
		// Build the discriminating set of vocabulary terms
		discriminatingTerms = getDiscriminatingTerms(trainingTerms, termCount);
		
		// Calculate the list of probability of each term contained in each class
		termProbs = getTermProbs();
	}


	@Override
	public void classify()
	{
		String disputedPath = directoryPath + File.separator + "DISPUTED";
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadDirectory(Paths.get(disputedPath).toAbsolutePath());
		Index disputedIndex = indexCorpus(disputedCorpus);

		double n = this.totalDocCount;

		for (Document doc : disputedCorpus.getDocuments())
		{
			// Trained probability of paper being in each class
			HashMap<Author, Double> authorProbs = new HashMap<Author, Double>();
			
			for(Author author: Author.values())
			{
				double authorDocCount = indexes.get(author).getCorpusSize();
				double authorProb = Math.log(authorDocCount / n);
				
				for (int i = 0; i < discriminatingTerms.size(); i++)
				{
					List<Posting> postings = disputedIndex.getPostings(discriminatingTerms.get(i), true);
					if (postings.size() > 0)
					{
						for (Posting posting : postings)
						{
							if (posting.getDocumentId() == doc.getId())
							{
								authorProb += Math.log(termProbs.get(author).get(i));
							}
						}
					}
					
					authorProbs.put(author, authorProb);
				}
			}

			Author author = getAuthorWithHighestProb(authorProbs);

			System.out.println(doc.getTitle() + ": " + author);
		}
	}


	private Author getAuthorWithHighestProb(HashMap<Author, Double> authorProbs)
	{
		PriorityQueue<Entry<Author, Double>> priorityQueue = new PriorityQueue<Entry<Author, Double>>((a, b) -> Double.compare(b.getValue(), a.getValue()));
		priorityQueue.addAll(authorProbs.entrySet());
		
		return priorityQueue.peek().getKey();
	}


	private HashMap<Author, List<Double>> getTermProbs()
	{
		HashMap<Author, List<Double>> result = new HashMap<Author, List<Double>>();

		for(Author author: Author.values())
		{
			List<Double> termProbs = new ArrayList<Double>();
			
			Index index = indexes.get(author);
			
			int totalTermFreq = 0;
			for (int i = 0; i < discriminatingTerms.size(); i++)
			{
				double termFreq = 0;

				List<Posting> postings = index.getPostings(discriminatingTerms.get(i), true);
				for (Posting posting : postings)
				{
					termFreq += posting.getTermFreq();
				}

				totalTermFreq += termFreq;

				termProbs.add(termFreq + 1);
			}

			for (int i = 0; i < discriminatingTerms.size(); i++)
			{
				termProbs.set(i, termProbs.get(i) / (totalTermFreq + discriminatingTerms.size()));
			}
			
			result.put(author, termProbs);
		}
		

		return result;
	}


	private List<String> getDiscriminatingTerms(SortedSet<String> trainingTerms, int termCount)
	{
		List<String> result = new ArrayList<String>();

		HashMap<String, Double> termScore = new HashMap<String, Double>();
		// PriorityQueue<MutualInfo> priorityQueue = new PriorityQueue<MutualInfo>((a, b) -> b.compareTo(a));
		
		PriorityQueue<Entry<String, Double>> priorityQueue = new PriorityQueue<Entry<String, Double>>((a, b) -> Double.compare(b.getValue(), a.getValue()));
		
		for(String term: trainingTerms)
		{
			for(Author author: Author.values())
			{
				Index index = indexes.get(author);
				int n = this.totalDocCount;							// total number of documents in whole classes
				int n1x = index.getCorpusSize();						// total number of documents in this class
				int nx1 = getTotalDocContainingTerm(term);			// total number of documents with this term regardless of class
				
				int n11 = index.getPostings(term, false).size();	
				int n01 = nx1 - n11;	
				int n10 = n1x - n11;									// number of documents that contain term and are not in class
				int n00 = n - (n11 + n01 + n10);
				
				double score = calculateScore(n11, n10, n01, n00);
	
				// System.out.println(score);
				
				// priorityQueue.add(new MutualInfo(term, score));
				
				// priorityQueue.add(new AbstractMap.SimpleEntry<String, Double>(term, score));
				
				
			
				if(termScore.get(term) == null)
				{
					termScore.put(term, score);
				}
				else
				{
					termScore.put(term, termScore.get(term) + score);
				}
			
				
			}
		}

		
	
		for(String term: termScore.keySet())
		{
			// priorityQueue.add(new MutualInfo(term, termScore.get(term)));
			priorityQueue.add(new AbstractMap.SimpleEntry<String, Double>(term, termScore.get(term)));
		}
	
		
		

		int count = 0;
		while (priorityQueue.peek() != null && count < termCount)
		{
			System.out.println(priorityQueue.peek().getKey() + " - " + priorityQueue.peek().getValue());
			result.add(priorityQueue.poll().getKey());
			count++;
		}

		return result;
	}
	
	
	private double calculateScore(double n11, double n10, double n01, double n00)
	{
		double n = n11 + n10 + n01 + n00;
		double n1x = n10 + n11;
		double nx1 = n01 + n11;
		double nx0 = n00 + n10;
		double n0x = n01 + n00;

		double operand1 = (n11/n) * (Math.log((n * n11)/(n1x * nx1))/Math.log(2.0));
		double operand2 = (n10/n) * (Math.log((n * n10)/(n1x * nx0))/Math.log(2.0));
		double operand3 = (n01/n) * (Math.log((n * n01)/(n0x * nx1))/Math.log(2.0));
		double operand4 = (n00/n) * (Math.log((n * n00)/(n0x * nx0))/Math.log(2.0));

		double score = 0;
		score += Double.isNaN(operand1)? 0 : operand1;
		score += Double.isNaN(operand2)? 0 : operand2;
		score += Double.isNaN(operand3)? 0 : operand3;
		score += Double.isNaN(operand4)? 0 : operand4;
		
		return score;
	}
	
	
	private int getTotalDocContainingTerm(String term)
	{
		SortedSet<Integer> docIds = new TreeSet<Integer>();

		for(Author author: Author.values())
		{
			List<Posting> postings = indexes.get(author).getPostings(term, false);
			for(Posting posting: postings)
			{
				docIds.add(posting.getDocumentId());
			}
		}
		
		return docIds.size();
	}

	private Index indexCorpus(DocumentCorpus corpus)
	{
		// Constuct an inverted index
		PositionalInvertedIndex index = new PositionalInvertedIndex(corpus.getCorpusSize());

		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> documents = corpus.getDocuments();

		// Iterate through the documents, process the tokens and add processed terms to the index with addPosting.
		for (Document doc : documents)
		{
			try
			{
				TokenStream tokenStream = new EnglishTokenStream(doc.getContent());

				int position = 0;
				for (String token : tokenStream.getTokens())
				{
					List<String> processedTerms = processor.processToken(token);

					for (String term : processedTerms)
					{
						index.addTerm(term, doc.getId(), position);
					}

					position++;
				}

				tokenStream.close();
			}

			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}

		return index;
	}
}


class MutualInfo implements Comparable<MutualInfo>
{
	private String key;
	private double value;


	public MutualInfo(String key, double value)
	{
		this.key = key;
		this.value = value;
	}


	public String getKey()
	{
		return this.key;
	}


	public Double getValue()
	{
		return this.value;
	}


	@Override
	public int compareTo(MutualInfo o)
	{
		return this.getValue().compareTo(o.getValue());
	}
}
