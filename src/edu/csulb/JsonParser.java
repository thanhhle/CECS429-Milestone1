package edu.csulb;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import cecs429.models.Article;
import cecs429.models.Dataset;

public class JsonParser {

	public static void main(String[] args) 
	{
		try 
		{
		    // Create Gson instance
		    Gson gson = new Gson();

		    // Create a reader
		    Reader reader = Files.newBufferedReader(Paths.get("all-nps-sites.json"));

		    // Convert JSON file to dataset
		    Dataset dataset = gson.fromJson(reader, Dataset.class);
		    
		    // Get the list of articles
		    Article[] articles = dataset.getDocuments();
		    
		    // Convert each article to JSON file
		    for(int i = 0; i < articles.length; i++)
		    {
		    	String fileName = "article" + (i+1) + ".json";	    	
		    	try 
		    	{
		    		// Create a writer
		    		Writer writer = new FileWriter(fileName);
		    		
		    		// Convert article to JSON file
		    		gson.toJson(articles[i], writer);
		    		
		    		// Close the writer
		    		writer.close();
		    		
		    	} 
		    	catch (Exception e) 
		    	{
		    		throw new RuntimeException(e);
		    	}
		    }

		    // Close the reader
		    reader.close();

		} 
		catch (IOException e) 
		{
			throw new RuntimeException(e);
		}

	}
}
