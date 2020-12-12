package cecs429.classification;

import java.util.HashMap;

import cecs429.text.Normalizer;

public class Main
{
	public static void main(String[] args)
	{
		String directoryPath = "/Users/thanhle/Downloads/federalist-papers";
		// runBayesianClassifier(directoryPath);
		runRocchioClassifier(directoryPath);
		
		String term = "[1]";
		System.out.println(Normalizer.removeNonAlphanumeric(term));
	}
	
	
	private static void runBayesianClassifier(String directoryPath)
	{
		System.out.println("--------------- BAYESIAN CLASSIFIER ---------------");
		BayesianClassifier bayesianClassifer = new BayesianClassifier(directoryPath, 50);
		
		// Print top 10 terms by I(T, C) and giving a score of 0 to any I(T, C) that is NaN:
		HashMap<String, Double> discriminatingTerms = bayesianClassifer.getDiscriminatingTerms();
		System.out.println("Top 10 terms by I(T,C), and giving a score of 0 to any I(T,C) that is NaN:");
		for (int i = 0; i < 12; i++)
		{
			String term = (String) discriminatingTerms.keySet().toArray()[i];
			System.out.println(term + " - " + discriminatingTerms.get(term));
		}
		
		System.out.println();
		
		bayesianClassifer.classify();
	}
	
	
	private static void runRocchioClassifier(String directoryPath)
	{
		System.out.println("--------------- ROCCHIO CLASSIFIER ---------------");
		RocchioClassifier rocchioClassifier = new RocchioClassifier(directoryPath);
		
		rocchioClassifier.printCentroids(30);
			
		rocchioClassifier.classify();
	}
}
