package cecs429.classification;

public class Main
{

	public static void main(String[] args)
	{
		String directoryPath = "/Users/thanhle/Downloads/federalist-papers";
		Classifier classifer = new BayesianClassifier(directoryPath, 10);

		classifer.classify();
	}

}
