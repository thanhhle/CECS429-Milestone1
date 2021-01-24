package cecs429.classification;


public class Main
{
	public static void main(String[] args)
	{
		String directoryPath = "/Users/thanhle/Documents/CSULB/Classes/Fall 2020/CECS 429/Input/federalist-papers";
		// runBayesianClassifier(directoryPath);
		// runRocchioClassifier(directoryPath);
		runKNNClassifier(directoryPath);
	}
	
	
	private static void runBayesianClassifier(String directoryPath)
	{
		System.out.println("\n------------------------- BAYESIAN CLASSIFIER -------------------------");
		BayesianClassifier bayesianClassifier = new BayesianClassifier(directoryPath, 50);
		
		// Print top 10 terms by I(T, C) and giving a score of 0 to any I(T, C) that is NaN:
		bayesianClassifier.printDiscriminatingTerms(15);
		
		// bayesianClassifier.classify("paper_49.txt");
		bayesianClassifier.classify();
		
	}
	
	
	private static void runRocchioClassifier(String directoryPath)
	{
		System.out.println("\n------------------------- ROCCHIO CLASSIFIER -------------------------");
		RocchioClassifier rocchioClassifier = new RocchioClassifier(directoryPath);
		
		rocchioClassifier.printTrainingSet(30);
		rocchioClassifier.printCentroids(30);
		
		// rocchioClassifier.printNormalizedVectors(30, "paper_52.txt");
		
		// rocchioClassifier.classify("paper_52.txt");
		rocchioClassifier.classify();
	}
	
	
	private static void runKNNClassifier(String directoryPath)
	{
		System.out.println("\n------------------------- KNN CLASSIFIER -------------------------");
		KNNClassifier kNNClassifier = new KNNClassifier(directoryPath);

		// kNNClassifier.printNormalizedVectors(30, "paper_53.txt");
		kNNClassifier.classify(5);
	}
}
