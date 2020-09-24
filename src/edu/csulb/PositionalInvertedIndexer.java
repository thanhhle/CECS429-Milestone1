package edu.csulb;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.Document;
import cecs429.documents.DocumentCorpus;
import cecs429.index.Index;
import cecs429.index.PositionalInvertedIndex;
import cecs429.index.Posting;
import cecs429.query.BooleanQueryParser;
import cecs429.query.Query;
import cecs429.text.AdvancedTokenProcessor;
import cecs429.text.EnglishTokenStream;
import cecs429.text.Normalizer;
import cecs429.text.TokenProcessor;
import cecs429.text.TokenStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;


public class PositionalInvertedIndexer 
{	
	public static void main(String[] args)
	{
		String directoryPath = null;
		// directoryPath = "/Users/thanhle/Downloads/MobyDick10Chapters";
		if(args.length > 0)
		{
			directoryPath = args[0];
		}
		else
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
		// DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directoryPath).toAbsolutePath(), ".txt");
		DocumentCorpus corpus = DirectoryCorpus.loadJsonDirectory(Paths.get(directoryPath).toAbsolutePath(), ".json");
			
		// Build a positional inverted index and print indexing time
		long startTime = System.currentTimeMillis();
		Index index = indexCorpus(corpus);
		long endTime = System.currentTimeMillis();
		System.out.println("Indexing time: " + (endTime - startTime)/1000 + " seconds");
		
		
		// Handle some "special" queries that do not represent information needs.
		// If the user query starts with any of these strings, perform specified operation instead of doing a postings retrieval
		// 		:q - exit the program
		//  	:stem token - take the token string and stem it, then print the stemmed term
		//		:index directoryname - index the folder specified by directoryname and then begin querying it, effectively restarting the program
		//		:vocab - print the first 1000 terms in the vocabulary of the corpus sorted alphabetically and the count of the total number of vocabulary terms
		Scanner scan = new Scanner(System.in);
		while(true)
		{
			System.out.println("\nPlease enter a term to search: ");
			String term = scan.nextLine();
			
			if(term.startsWith(":q"))
			{
				System.out.println("\nProgram exits.");
				break;
			}
			else if(term.startsWith(":stem"))
			{
				Normalizer normalizer = new Normalizer();
				System.out.println(normalizer.stemToken(term.substring(6, term.length())));
			}
			else if(term.startsWith(":index"))
			{
				directoryPath = term.substring(7, term.length());
				main(new String[]{ directoryPath });
			}
			else if(term.startsWith(":vocab"))
			{
				List<String> vobabulary = index.getVocabulary();
				for(int i = 0; i < 1000; i++)
				{
					System.out.println(vobabulary.get(i));
				}
				System.out.println("\nTotal number of vocabulary terms: " + vobabulary.size());
			}
			else
			{
				// Parse the input into appropriate Query object
				BooleanQueryParser queryParser = new BooleanQueryParser();
				Query query = queryParser.parseQuery(term);
					
				// Get a list of postings for the documents that match the query
				System.out.println("\nDocuments contain the query:");
				List<Posting> postings = query.getPostings(index);
				
				if(postings != null)
				{
					// Construct list of string to record file names returned from the query
					List<String> fileNames = new ArrayList<String>();
					
					// Output the names of the documents returned from the query, one per line
					for (Posting p: postings) 
					{
						String fileName = corpus.getDocument(p.getDocumentId()).getTitle();
						System.out.println("Document " + fileName);
						fileNames.add(fileName);
					}
					
					// Output the number of documents returned from the query
					System.out.println("\nNumber of documents returned from the query: " + postings.size());
					
					// Ask the user if they would like to select a document to view
					System.out.println("\nDo you want to view a document? Enter Y for YES or anything else for NO");
					String input = scan.nextLine().toLowerCase();
					
					// If the user selects a document to view, print the entire content of the document to the screen
					if(input.equals("y"))
					{	
						viewDocumentsMatchTheQuery(fileNames, directoryPath);
					}
				}
				else
				{
					System.out.println("\nNo document that match the query was found.");
				}	
			}
		}
		scan.close();	
	}
	
	
	/*
	 * Index the corpus
	 */
	private static Index indexCorpus(DocumentCorpus corpus) 
	{
		// Construct a token processor
		TokenProcessor processor = new AdvancedTokenProcessor();
		
		// Constuct an inverted index
		PositionalInvertedIndex index = new PositionalInvertedIndex();
		
		// Get all the documents in the corpus by calling GetDocuments().
		Iterable<Document> documents = corpus.getDocuments();
				
		// Iterate through the documents, tokenize the terms and add terms to the index with addPosting.
		for(Document doc: documents)
		{	
			try {
				TokenStream tokenStream = new EnglishTokenStream(doc.getContent());
				int position = 0;
				for(String token: tokenStream.getTokens())
				{
					index.addTerm(processor.processToken(token), doc.getId(), position);
					position++;
				}
				tokenStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
		}
			
		return index;
	}
	
	/*
	 *  Allow user to select a file and print out its content
	 */
	private static void viewDocumentsMatchTheQuery(List<String> fileNames, String directoryPath)
	{
		// Allow user to select a file from the directoryPath which has name that is in the fileName
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setCurrentDirectory(new File(directoryPath));
		
		fileChooser.setFileFilter(new FileFilter() 
			{
				@Override
				public boolean accept(File file) {
			        return fileNames.contains(file.getName());
			    }

				@Override
				public String getDescription() {
					return "Files match the query";
				}
				
			});


		int option = fileChooser.showDialog(new JFrame(), "View File");
			
	    if(option == JFileChooser.APPROVE_OPTION)
	    {
	    	// Get the selected file and print its content
	       	File file = fileChooser.getSelectedFile();
			try {
				FileReader fr = new FileReader(file);
				BufferedReader br = new BufferedReader(fr);
				String buffer = br.readLine();

		        while (buffer != null)
		        {
			       	System.out.println(buffer);
			       	buffer = br.readLine();
		        }
			       					        
			    br.close();
			    fr.close();
					
			} catch (FileNotFoundException e) {	
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			           
        }
	    else
	    {
	        System.out.println("Invalid selection. Program exist.");
	       	System.exit(0);
	    }
	}
}
