package cecs429.classification;

public class Main
{
	public static void main(String[] args)
	{
		String directoryPath = "/Users/thanhle/Downloads/federalist-papers";
		
		System.out.println("BAYESIAN CLASSIFIER");
		Classifier bayesianClassifer = new BayesianClassifier(directoryPath, 10);
		bayesianClassifer.classify();
		
		System.out.println();
		
		/*
		System.out.println("Rocchio Classifer");
		Classifier rocchioClassifier = new RocchioClassifier(directoryPath);
		rocchioClassifier.classify();
		*/
	}
}
