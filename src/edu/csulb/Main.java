package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.helper.SpellingCorrector;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.query.QueryParser;
import cecs429.query.RankedQueryParser;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Normalizer;
import cecs429.text.TokenProcessor;
import cecs429.text.TokenStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.apache.commons.io.FileUtils;


public class Main
{
	private static String directoryPath;
	private static TokenProcessor processor;
	private static DocumentCorpus corpus;
	private static QueryParser queryParser;
	private static Index index;
	private static Scanner scan = new Scanner(System.in);;


	public static void main(String[] args)
	{
		if (directoryPath == null)
		{
			// Allow user to select a directory that they would like to index
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = fileChooser.showDialog(new JFrame(), "Choose Directory");

			if (option == JFileChooser.APPROVE_OPTION)
			{
				directoryPath = fileChooser.getSelectedFile().getPath();
			}
			else
			{
				System.out.println("Invalid selection. Program exist.");
				System.exit(0);
			}
		}

		// Print the selected directory path 
		System.out.println("Directory path: " + directoryPath);

		// Construct a corpus from the selected directory
		corpus = DirectoryCorpus.loadDirectory(Paths.get(directoryPath).toAbsolutePath());

		// Prompt user to choose a token processor
		processor = getTokenProcessor();

		// Prompt user to choose program's mode
		System.out.println("\n1 - Build a disk index");
		System.out.println("2 - Process queries over an existing disk index");

		int option = 0;
		while (true)
		{
			System.out.println("Please enter the number associated with the program's mode to be run:");
			String input = scan.nextLine();

			if (isNumeric(input))
			{
				option = Integer.parseInt(input);

				if (option == 1 || option == 2)
				{
					break;
				}
			}
		}

		if (option == 1)
		{
			buildDiskIndex();
		}
		else
		{
			queryOverExistingIndex();
		}

		scan.close();

		System.exit(0);
	}


	private static TokenProcessor getTokenProcessor()
	{
		System.out.println("\n1 - Basic token processor");
		System.out.println("2 - Advanced token processor");

		int processorId = 0;
		while (true)
		{
			System.out.println("Please enter the number associated with the token processor to be used:");
			String input = scan.nextLine();

			if (isNumeric(input))
			{
				processorId = Integer.parseInt(input);

				if (processorId == 1 || processorId == 2)
				{
					break;
				}
			}
		}

		if (processorId == 1)
		{
			return new BasicTokenProcessor();
		}
		else
		{
			return new AdvancedTokenProcessor();
		}
	}


	private static void buildDiskIndex()
	{
		// Start counting time to index the corpus 
		long startTime = System.currentTimeMillis();

		// Build a positional inverted index 
		System.out.println("\nIndexing \"" + directoryPath + "\"...");
		indexCorpus(corpus);

		// Stop counting time to index the corpus
		long endTime = System.currentTimeMillis();

		// Print indexing time
		System.out.println("\nTime to index = " + (endTime - startTime) / 1000 + " seconds");
	}


	private static void queryOverExistingIndex()
	{
		index = new DiskPositionalIndex(directoryPath);
		queryParser = getQueryParser();

		// Handle some "special" queries that do not represent information needs.
		// If the user query starts with any of these strings, perform specified operation instead of doing a postings retrieval
		// 		:q - exit the program
		//  	:stem token - take the token string and stem it, then print the stemmed term
		//		:index directoryname - index the folder specified by directoryname and then begin querying it, effectively restarting the program
		//		:vocab - print the first 1000 terms in the vocabulary of the corpus sorted alphabetically and the count of the total number of vocabulary terms
		while (true)
		{
			System.out.println("\n\nPlease enter a term to search: ");
			String term = scan.nextLine();

			if (term.length() == 0)
			{
				// Do nothing if user didn't input a query
			}

			// If the input term starts with :q
			// Exit the program
			else if (term.startsWith(":q"))
			{
				System.out.println("\nProgram exits.");
				break;
			}

			// If the input term starts with :stem
			// Take the token string and stem it, then print the stemmed term
			else if (term.startsWith(":stem"))
			{
				term = term.substring(6, term.length());
				System.out.println(term + " -> " + Normalizer.stemToken(term));
			}

			// If the input term starts with :index
			// Index the folder specified by directoryname and then begin querying it, effectively restarting the program
			else if (term.startsWith(":index"))
			{
				directoryPath = term.substring(7, term.length());
				main(new String[]{});
			}

			// If the input term starts with :vocab
			// Print the first 1000 terms in the vocabulary of the corpus sorted alphabetically and the count of the total number of vocabulary terms
			else if (term.startsWith(":vocab"))
			{
				List<String> vocab = index.getVocabulary();
				for (int i = 0; i < vocab.size(); i++)
				{
					System.out.println(vocab.get(i));
				}
				System.out.println("\nTotal number of vocabulary terms: " + vocab.size());
			}

			// If the input term starts with :processor
			// Allow user to chnage the TokenProcessor at runtime
			else if (term.startsWith(":processor"))
			{
				processor = getTokenProcessor();
			}

			// If the input term starts with :process
			// Process the term and print out the processed terms
			else if (term.startsWith(":process"))
			{
				term = term.substring(9, term.length());

				for (String processedTerm : processor.processToken(term))
				{
					System.out.println(processedTerm);
				}
			}

			// If the input term starts with :mode
			// Allow user to change the querying mode
			else if (term.startsWith(":mode"))
			{
				queryParser = getQueryParser();
			}

			// Run the query
			else
			{
				runQuery(term);
				
				SpellingCorrector spellingCorrector = new SpellingCorrector(term, index, processor);
				String correctionQuery = spellingCorrector.getSpellingCorrection();
				if(!correctionQuery.equals(term) && !Normalizer.stemToken(correctionQuery).equals(term))
				{
					runSpellingCorrectionQuery(correctionQuery);
				}
			}
		}
	}


	private static Index indexCorpus(DocumentCorpus corpus)
	{
		// Constuct an inverted index
		PositionalInvertedIndex index = new PositionalInvertedIndex();

		// Create an on-disk representation of the inverted index
		DiskIndexWriter indexWriter = new DiskIndexWriter();

		// Create the folder named "index" inside the corpus path
		File indexDirectory = new File(directoryPath + File.separator + "index");
		try
		{
			FileUtils.deleteDirectory(indexDirectory);
			indexDirectory.mkdir();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

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
					List<String> processedTerms = processor.processToken(token);

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

				// Write the document weights of each document to disk
				indexWriter.writeDocWeights(termFreq, indexDirectory.getAbsolutePath());
			}

			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		
		System.out.println("Done");

		// Build a k-gram index
		System.out.println("\nBuilding kgram index...");
		index.buildKGramIndex(directoryPath);
		System.out.println(index.getKGramIndex().getKGrams().size() + " distinct kgrams in the index");

		// Write the postings to disk
		indexWriter.writeIndex(index, indexDirectory.getAbsolutePath(), processor);
		
		// Write the kgram index to disk
		indexWriter.writeKGramIndex(index.getKGramIndex(), indexDirectory.getAbsolutePath());
	
		return index;
	}


	private static QueryParser getQueryParser()
	{
		// Prompt user to choose processing queries' mode
		System.out.println("\n1 - Boolean retrieval mode");
		System.out.println("2 - Ranked retrieval mode");

		int option = 0;
		while (true)
		{
			System.out.println("Please enter the number associated with the querying style to be run:");
			String input = scan.nextLine();

			if (isNumeric(input))
			{
				option = Integer.parseInt(input);

				if (option == 1 || option == 2)
				{
					break;
				}
			}
		}

		if (option == 1)
		{
			return new BooleanQueryParser();
		}
		else
		{
			return new RankedQueryParser();
		}
	}

	
	private static void runQuery(String term)
	{
		// Parse the input into appropriate Query object
		Query query = queryParser.parseQuery(term, corpus.getCorpusSize());

		// Get a list of postings for the documents that match the query
		List<Posting> postings = query.getPostings(index, processor);

		// Construct list of string to record file names returned from the query
		List<FileDocument> files = new ArrayList<FileDocument>();

		// Output the names of the documents returned from the query, one per line
		System.out.println("\nDocuments contain the query:");

		int count = 1;
		for (Posting p : postings)
		{
			FileDocument file = (FileDocument) corpus.getDocument(p.getDocumentId());
			System.out.println(count + " - " + file.getTitle() 
											 + " (\"" + file.getFilePath().getFileName() + "\")"
											 + " -- " + String.format("%.5f", p.getWeight()));
			files.add(file);
			count++;
		}

		// Output the number of documents returned from the query
		System.out.println("\nNumber of documents returned from the query: " + postings.size());

		// If the user selects a document to view, print the entire content of the document to the screen
		if (files.size() > 0)
		{
			viewDocumentsMatchTheQuery(files);
		}
	}

	
	private static void viewDocumentsMatchTheQuery(List<FileDocument> files)
	{
		System.out.println("\nPlease enter the document ID of the document to be viewed or anything else to continue:");
		String input = scan.nextLine();

		if (isNumeric(input))
		{
			int documentId = Integer.parseInt(input);

			if (documentId > 0 && documentId <= files.size())
			{
				BufferedReader bufferedReader = new BufferedReader(files.get(documentId - 1).getContent());

				try
				{
					String buffer = "\n";
					do
					{
						System.out.println(buffer);
						buffer = bufferedReader.readLine();
					} while (buffer != null);
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	
	private static void runSpellingCorrectionQuery(String correctionQuery)
	{
		System.out.println("\nSuggested correction: " + correctionQuery);
		System.out.println("Do you want to run this query? Please enter Y for yes and N for no");
		String input = scan.nextLine().toLowerCase();
		if (input.equals("y"))
		{
			runQuery(correctionQuery);
		}
	}
	
	
	private static boolean isNumeric(final String str)
	{
		if (str == null || str.length() == 0)
		{
			return false;
		}

		return str.chars().allMatch(Character::isDigit);
	}
}
