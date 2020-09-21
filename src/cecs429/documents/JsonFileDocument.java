package cecs429.documents;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;

import cecs429.models.Article;

public class JsonFileDocument implements FileDocument {
	private int mDocumentId;
	private Path mFilePath;
	private String mTitle;
	
	/**
	 * Constructs a TextFileDocument with the given document ID representing the file at the given
	 * absolute file path.
	 */
	public JsonFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
		mTitle = getArticle().getTitle();
	}
	
	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	@Override
	public int getId() {
		return mDocumentId;
	}
	
	@Override
	public Reader getContent() {
		try {
			return Files.newBufferedReader(mFilePath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public String getTitle() {
		return mTitle;
	}
	
	public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
		return new JsonFileDocument(documentId, absolutePath);
	}
	
	private Article getArticle()
	{
		try {
			// Create Gson instance
		    Gson gson = new Gson();

		    // Create a reader
		    Reader reader = Files.newBufferedReader(mFilePath);

		    // Convert JSON file to Article object
		    Article article = gson.fromJson(reader, Article.class);
		    
		    // Close the reader
		    reader.close();
		    
			return article;
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
