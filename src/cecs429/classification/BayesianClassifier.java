package cecs429.classification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import cecs429.text.TokenStream;


public class BayesianClassifier
{
	private final HashMap<Author, Index> trainingIndexes;
	private final HashMap<String, Double> discriminatingTerms;
	private final HashMap<Author, HashMap<String, Double>> termProbs;
	
	private final DocumentCorpus disputedCorpus;
	private final Index disputedIndex;

	private int totalDocCount = 0;


	public BayesianClassifier(String directoryPath, int discriminatingTermsSize)
	{
		trainingIndexes = new HashMap<Author, Index>();

		SortedSet<String> trainingSet = new TreeSet<String>();

		for (Author author : Author.values())
		{
			String path = directoryPath + File.separator + author;
			DirectoryCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(path).toAbsolutePath());
			Index index = indexCorpus(corpus);

			trainingIndexes.put(author, index);
			totalDocCount += index.getCorpusSize();
			trainingSet.addAll(index.getVocabulary());
		}
		
		String disputedPath = directoryPath + File.separator + "DISPUTED";
		disputedCorpus = DirectoryCorpus.loadDirectory(Paths.get(disputedPath).toAbsolutePath());
		disputedIndex = indexCorpus(disputedCorpus);

		// Build the discriminating set of vocabulary terms
		discriminatingTerms = getDiscriminatingTerms(trainingSet, discriminatingTermsSize);

		// Calculate the list of probability of each term contained in each class
		termProbs = getTermProbs();
	}


	public void classify()
	{
		System.out.println("\nClassifying...\n");
		
		double n = totalDocCount;
		for (Document doc : disputedCorpus.getDocuments())
		{
			// Trained probability of paper being in each class
			HashMap<Author, Double> probs = new HashMap<Author, Double>();

			for (Author author : Author.values())
			{
				double docCount = trainingIndexes.get(author).getCorpusSize();
				double prob = Math.log10(docCount / n);

				for (String term : disputedIndex.getVocabulary())
				{
					Double termProb = termProbs.get(author).get(term);

					if (termProb != null)
					{
						List<Posting> postings = disputedIndex.getPostings(term, true);
						for (Posting posting: postings)
						{
							if (posting.getDocumentId() == doc.getId())
							{
								prob += Math.log10(termProb);
							}
						}
					}

					probs.put(author, prob);
				}
			}
	
			for(Author author: Author.values()) 
			{
				System.out.println("Score of " + author + " for \"" + doc.getTitle() + "\": " + String.format("%.6f  ", probs.get(author)));
			}
			
			System.out.println("Classify \"" + doc.getTitle() + "\" as " + getAuthorWithHighestProb(probs) + "\n");
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
		
		double n = totalDocCount;
		
		// Trained probability of paper being in each class
		HashMap<Author, Double> probs = new HashMap<Author, Double>();

		for (Author author : Author.values())
		{
			double docCount = trainingIndexes.get(author).getCorpusSize();
			double prob = Math.log10(docCount / n);

			for (String term : disputedIndex.getVocabulary())
			{
				Double termProb = termProbs.get(author).get(term);

				if (termProb != null)
				{
					List<Posting> postings = disputedIndex.getPostings(term, true);
					for (Posting posting: postings)
					{
						if (posting.getDocumentId() == document.getId())
						{
							prob += Math.log10(termProb);
						}
					}
				}

				probs.put(author, prob);
			}
		}

		for(Author author: Author.values()) 
		{
			System.out.println("Score of " + author + " for \"" + documentName + "\": " + String.format("%.6f  ", probs.get(author)));
		}
		
		System.out.println("Classify \"" + documentName + "\" as " + getAuthorWithHighestProb(probs) + "\n");
	}
	
	
	public void printDiscriminatingTerms()
	{
		printDiscriminatingTerms(discriminatingTerms.size());
	}
	
	
	public void printDiscriminatingTerms(int count)
	{
		System.out.println("Top " + count + " terms by I(T,C), and giving a score of 0 to any I(T,C) that is NaN:");
		for (int i = 0; i < count; i++)
		{
			String term = (String) discriminatingTerms.keySet().toArray()[i];
			System.out.println(term + " - " + discriminatingTerms.get(term));
		}
		
		System.out.println();
	}
	

	private Author getAuthorWithHighestProb(HashMap<Author, Double> probs)
	{
		PriorityQueue<Entry<Author, Double>> priorityQueue = new PriorityQueue<Entry<Author, Double>>((a, b) -> Double.compare(b.getValue(), a.getValue()));
		priorityQueue.addAll(probs.entrySet());

		return priorityQueue.peek().getKey();
	}


	private HashMap<Author, HashMap<String, Double>> getTermProbs()
	{
		HashMap<Author, HashMap<String, Double>> result = new HashMap<Author, HashMap<String, Double>>();

		for (Author author : Author.values())
		{
			HashMap<String, Double> termProbs = new HashMap<String, Double>();

			Index index = trainingIndexes.get(author);

			int totalTermFreq = 0;
			for (String term : discriminatingTerms.keySet())
			{
				int termFreq = 0;

				List<Posting> postings = index.getPostings(term, true);
				for (Posting posting : postings)
				{
					termFreq += posting.getTermFreq();
				}

				termProbs.put(term, (double)termFreq + 1);

				totalTermFreq += termFreq;
			}

			for (String term : discriminatingTerms.keySet())
			{
				termProbs.put(term, termProbs.get(term) / (totalTermFreq + discriminatingTerms.size()));
			}

			result.put(author, termProbs);
		}

		return result;
	}


	private HashMap<String, Double> getDiscriminatingTerms(SortedSet<String> trainingSet, int discriminatingTermsSize)
	{
		HashMap<String, Double> discriminatingTerms = new LinkedHashMap<String, Double>();

		PriorityQueue<Entry<String, Double>> priorityQueue = new PriorityQueue<Entry<String, Double>>(
				(a, b) -> Double.compare(b.getValue(), a.getValue()));

		for (String term : trainingSet)
		{
			for (Author author : Author.values())
			{
				Index index = trainingIndexes.get(author);

				int n = totalDocCount; 								// total number of documents in whole classes
				int n1x = getTotalDocContainingTerm(term); 			// total number of documents with this term regardless of class
				int nx1 = index.getCorpusSize(); 					// total number of documents in this class regardless of term containing

				int n11 = index.getPostings(term, true).size(); 	// number of documents contain the term and in the class
				int n01 = nx1 - n11; 								// number of documents does not contain the term and in the class
				int n10 = n1x - n11; 								// number of documents that contain term and not in the class
				int n00 = n - (n11 + n01 + n10); 					// number of documents does not contain the term and not in the class

				double score = calculateScore(n11, n10, n01, n00);

				// priorityQueue.add(new MutualInfo(term, score));

				priorityQueue.add(new AbstractMap.SimpleEntry<String, Double>(term, score));

				// termScore.put(term, termScore.get(term) + score);
			}
		}

		/*
		 * for(String term: trainingSet) { // priorityQueue.add(new MutualInfo(term,
		 * termScore.get(term))); priorityQueue.add(new AbstractMap.SimpleEntry<String,
		 * Double>(term, termScore.get(term))); }
		 */

		while (priorityQueue.peek() != null && discriminatingTerms.size() < discriminatingTermsSize)
		{
			Entry<String, Double> termScore = priorityQueue.poll();			
			
			if(!discriminatingTerms.keySet().contains(termScore.getKey()))
			{
				discriminatingTerms.put(termScore.getKey(), termScore.getValue());
			}
		}

		return discriminatingTerms;
	}


	private double calculateScore(double n11, double n10, double n01, double n00)
	{
		double n = n11 + n10 + n01 + n00;
		double n1x = n10 + n11;
		double nx1 = n01 + n11;
		double nx0 = n10 + n00;
		double n0x = n01 + n00;

		double op1 = (n11 / n) * (Math.log((n * n11) / (n1x * nx1)) / Math.log(2));
		double op2 = (n10 / n) * (Math.log((n * n10) / (n1x * nx0)) / Math.log(2));
		double op3 = (n01 / n) * (Math.log((n * n01) / (n0x * nx1)) / Math.log(2));
		double op4 = (n00 / n) * (Math.log((n * n00) / (n0x * nx0)) / Math.log(2));

		op1 = Double.isNaN(op1) ? 0 : op1;
		op2 = Double.isNaN(op2) ? 0 : op2;
		op3 = Double.isNaN(op3) ? 0 : op3;
		op4 = Double.isNaN(op4) ? 0 : op4;

		return op1 + op2 + op3 + op4;
	}


	private int getTotalDocContainingTerm(String term)
	{
		int docCount = 0;
		for (Author Author : Author.values())
		{
			docCount += trainingIndexes.get(Author).getPostings(term, true).size();
		}

		return docCount;
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


	public boolean equals(Object o)
	{
		MutualInfo info = (MutualInfo) o;
		return this.getKey().equals(info.getKey()) && this.getValue() == info.getValue();
	}
}
