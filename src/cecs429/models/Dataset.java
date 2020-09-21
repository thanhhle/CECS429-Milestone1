package cecs429.models;

public class Dataset {
	public Article[] documents;
	
	public Dataset(Article[] documents)
	{
		this.documents = documents;
	}
	
	public Article[] getDocuments()
	{
		return documents;
	}
}
