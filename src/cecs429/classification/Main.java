package cecs429.classification;

public class Main
{

	public static void main(String[] args)
	{
		String directoryPath = "/Users/thanhle/Downloads/federalist-papers";
		
		System.out.println("Bayesian Classifer");
		// Classifier bayesianClassifer = new BayesianClassifier(directoryPath, 1000);
		// bayesianClassifer.classify();
		
		System.out.println();
		
		System.out.println("Rocchio Classifer");
		Classifier rocchioClassifier = new RocchioClassifier(directoryPath);
		rocchioClassifier.classify();
	}
}
