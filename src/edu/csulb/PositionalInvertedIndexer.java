package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.documents.FileDocument;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Normalizer;
import cecs429.text.TokenProcessor;
import cecs429.text.TokenStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;


public class PositionalInvertedIndexer 
{	
	private static String directoryPath;
	private static TokenProcessor processor;
	private static Scanner scan = new Scanner(System.in);;
	
	public static void main(String[] args)
	{
		// directoryPath = "/Users/thanhle/Downloads/MobyDick10Chapters";
		if(directoryPath == null)
		{
			// Allow user to select a directory that they would like to index
			JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int option = fileChooser.showDialog(new JFrame(), "Choose Directory");
			
	        if(option == JFileChooser.APPROVE_OPTION)
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
		System.out.println("Directory path: " + directoryPath + "\n");
		
		// Construct a corpus from the selected directory
		DocumentCorpus corpus = DirectoryCorpus.loadDirectory(Paths.get(directoryPath).toAbsolutePath());
		
		// Prompt user to choose a token processor
		processor = getTokenProcessor();
		
		// Build a positional inverted index and print indexing time
		System.out.println("\nIndexing...");
		long startTime = System.currentTimeMillis();
		Index index = indexCorpus(corpus);
		long endTime = System.currentTimeMillis();
		System.out.println("\nIndexing time: " + (endTime - startTime)/1000 + " seconds");
		
		
		// Handle some "special" queries that do not represent information needs.
		// If the user query starts with any of these strings, perform specified operation instead of doing a postings retrieval
		// 		:q - exit the program
		//  	:stem token - take the token string and stem it, then print the stemmed term
		//		:index directoryname - index the folder specified by directoryname and then begin querying it, effectively restarting the program
		//		:vocab - print the first 1000 terms in the vocabulary of the corpus sorted alphabetically and the count of the total number of vocabulary terms
		while(true)
		{
			System.out.println("\nPlease enter a term to search: ");
			String term = scan.nextLine();
				
			// If the input term starts with :q
			// Exit the program
			if(term.startsWith(":q"))
			{
				System.out.println("\nProgram exits.");
				break;
			}
			
			// If the input term starts with :stem
			// Take the token string and stem it, then print the stemmed term
			else if(term.startsWith(":stem"))
			{
				Normalizer normalizer = new Normalizer();
				term = term.substring(6, term.length());
				System.out.println(term + " -> " + normalizer.stemToken(term));
			}
			
			// If the input term starts with :index
			// Index the folder specified by directoryname and then begin querying it, effectively restarting the program
			else if(term.startsWith(":index"))
			{
				directoryPath = term.substring(7, term.length());
				main(new String[] {});
			}
			
			// If the input term starts with :vocab
			// Print the first 1000 terms in the vocabulary of the corpus sorted alphabetically and the count of the total number of vocabulary terms
			else if(term.startsWith(":vocab"))
			{
				List<String> vocab = index.getVocabulary();
				for(int i = 0; i < vocab.size(); i++)
				{
					System.out.println(vocab.get(i));
				}
				System.out.println("\nTotal number of vocabulary terms: " + vocab.size());
			}

			else
			{
				// Parse the input into appropriate Query object
				BooleanQueryParser queryParser = new BooleanQueryParser();
				Query query = queryParser.parseQuery(term);
					
				// Get a list of postings for the documents that match the query
				System.out.println("\nDocuments contain the query:");
				List<Posting> postings = query.getPostings(index, processor);
				
				// Construct list of string to record file names returned from the query
				List<FileDocument> files = new ArrayList<FileDocument>();
				
				// Output the names of the documents returned from the query, one per line
				int count = 0;
				for (Posting p: postings) 
				{
					FileDocument file = (FileDocument)corpus.getDocument(p.getDocumentId());
					System.out.println(count++ + " - " + file.getTitle());
					
					files.add(file);
				}
				
				// Output the number of documents returned from the query
				System.out.println("\nNumber of documents returned from the query: " + postings.size());
				
				// Ask the user if they would like to select a document to view
				System.out.println("\nDo you want to view a document? Enter Y for YES or anything else for NO");
				String input = scan.nextLine().toLowerCase();
				
				// If the user selects a document to view, print the entire content of the document to the screen
				if(input.equals("y"))
				{	
					viewDocumentsMatchTheQuery(files);
				}
			}
		}
		
		scan.close();	
	}
	
	
	/*
	 * Choose the token process to be used
	 */
	private static TokenProcessor getTokenProcessor()
	{
		System.out.println("1 - Basic Token Processor");
		System.out.println("2 - Advanced Token Processor");
		
		int processorId = 0;
		
		while(true) 
		{
			System.out.println("Please enter the number associated with the token processor to be used:");
			processorId = Integer.parseInt(scan.nextLine());
			
			if(processorId == 1 || processorId == 2)
			{
				break;
			}
			
		}
		
		if(processorId == 1)
		{
			return new BasicTokenProcessor();
		}
		else
		{
			return new AdvancedTokenProcessor();
		}
	}
	
	
	/*
	 * Index the corpus
	 */
	private static Index indexCorpus(DocumentCorpus corpus) 
	{
		// Constuct an inverted index
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		
		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> documents = corpus.getDocuments();
				
		// Iterate through the documents, tokenize the terms and add terms to the index with addPosting.
		for(Document doc: documents)
		{	
			try 
			{
				TokenStream tokenStream = new EnglishTokenStream(doc.getContent());
				
				int position = 0;
				for(String token: tokenStream.getTokens())
				{
					index.addTerm(processor.processToken(token), doc.getId(), position);
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
	
	
	/*
	 *  Allow user to select a file and print out its content
	 */
	private static void viewDocumentsMatchTheQuery(List<FileDocument> files)
	{	
		boolean validChoice = false;
		
		do {
			System.out.println("\nPlease enter the document ID of the document to be viewed:");
			int documentId = Integer.parseInt(scan.nextLine());
			
			if(documentId > -1 && documentId < files.size())
			{
				validChoice = true;
				
				BufferedReader bufferedReader = new BufferedReader(files.get(documentId).getContent());

		        try 
		        {
		        	String buffer = "\n";
		        	do {
		        		System.out.println(buffer);
		        		buffer = bufferedReader.readLine();
		        	}
		        	while(buffer != null);
				} 
		        catch (IOException e) 
		        {				
					throw new RuntimeException(e);
				}

			}
			
		} while(!validChoice);
	}
}
