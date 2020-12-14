package cecs429.classification;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import static java.util.stream.Collectors.*;
import java.util.ArrayList;
import static java.util.Map.Entry.*;

public class KNNClassification {
	
	static List<DocVectorModel> disputedDocs;
	static List<DocVectorModel> trainingSetVectors;
	static Map<String, Double> sortedDistances;
	static Map<String, Classifier.DocClass> trainingClassifications;
	static int hCount = 0;
	static int mCount = 0;	
	
	public static void applyKNN(List<DocVectorModel> disputedDocsArg, List<DocVectorModel> trainingSetVectorsArg, int k) {
		disputedDocs = disputedDocsArg;
		trainingSetVectors = trainingSetVectorsArg;
		
		disputedDocs.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
		for(DocVectorModel disputedDoc: disputedDocs) {
			Map<String, Double> distances = new HashMap<String, Double>();
			for (int i = 0; i < trainingSetVectors.size(); i++) {
				distances.put(trainingSetVectors.get(i).DocTitle, DocVectorModel.euclidDistance(disputedDoc, trainingSetVectors.get(i)));
			}
			
			sortedDistances = distances.entrySet()
					.stream()
					.sorted(comparingByValue())
					.limit(k)
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		
			hCount = 0;
			mCount = 0;
			trainingClassifications = new HashMap<String, Classifier.DocClass>();
			for(String docTitle: sortedDistances.keySet()) {
				for (DocVectorModel lVector : trainingSetVectors){
					
					if (docTitle.equals(lVector.DocTitle)){
						trainingClassifications.put(docTitle, lVector.getClassification());
						switch(lVector.classification) {
							case HAMILTON:
								hCount++;
								break;
							case MADISON:
								mCount++;
								break;
							default:
								break;
						}
					}
				}
			}
			
			if (hCount > mCount) {
				disputedDoc.setClassification(Classifier.DocClass.HAMILTON);
				printClassificationResults(disputedDoc);
			} else if (mCount > hCount) {
				disputedDoc.setClassification(Classifier.DocClass.MADISON);
				printClassificationResults(disputedDoc);
			} else if (hCount == mCount){
				// Choose a tie-breaking strategy
				tieBreakLowerK(disputedDoc, k);
//				tieBreakWeightByDistance(disputedDoc, sortedDistances);
//				tieBreakRandomSelection(disputedDoc);
				
			}	
		}	
	}
	
	public static void tieBreakLowerK(DocVectorModel disputedDoc, int k) {
		if (k >= 1){
			List<DocVectorModel> tieVector = new ArrayList<DocVectorModel>();
			tieVector.add(disputedDoc);
			KNNClassification.applyKNN(tieVector, trainingSetVectors, k - 1);
		}
	}
	
	public static void tieBreakWeightByDistance(DocVectorModel disputedDoc, Map<String, Double> sortedDistances) {
		double hScore = 0;
		double mScore = 0;
		
		for(String docTitle: sortedDistances.keySet()) {
			for (DocVectorModel lVector : trainingSetVectors){
				if (docTitle.equals(lVector.DocTitle)){
					switch(lVector.classification) {
						case HAMILTON:
							hScore = hScore + Math.cos(sortedDistances.get(docTitle));
							break;
						case MADISON:
							mScore = mScore + Math.cos(sortedDistances.get(docTitle));
							break;
						default:
							break;
					}
				}
			}
		}
		
		if (hScore > mScore) {
			disputedDoc.setClassification(Classifier.DocClass.HAMILTON);
		} else if (mScore > hScore) {
			disputedDoc.setClassification(Classifier.DocClass.MADISON);
		}
		printClassificationResultsWithScore(disputedDoc, hScore, mScore);
		
	}
	
	public static void tieBreakRandomSelection(DocVectorModel disputedDoc) {
		Random rand = new Random();
		int randomInt = rand.nextInt(2);
		if (randomInt == 0) {
			disputedDoc.setClassification(Classifier.DocClass.HAMILTON);
		} else if (randomInt == 1) {
			disputedDoc.setClassification(Classifier.DocClass.MADISON);
		}
		printClassificationResults(disputedDoc);
		
	}
	
	public static void printClassificationResults(DocVectorModel disputedDoc) {
		System.out.println(disputedDoc.DocTitle + " classification: " + disputedDoc.getClassification());
		System.out.println(disputedDoc.DocTitle + " nearest to:");
		int count = 1;
		for(String docTitle: sortedDistances.keySet()) {
			System.out.printf(count + ": " + docTitle + "(%.6f)" + " - " + trainingClassifications.get(docTitle) + "\n\n", sortedDistances.get(docTitle));
			count++;
		}
		System.out.println("Hamilton Count: " + hCount);
		System.out.println("Madison Count: " + mCount);
		System.out.println();
	}
	
	public static void printClassificationResultsWithScore(DocVectorModel disputedDoc, double hScore, double mScore) {
		System.out.println(disputedDoc.DocTitle + " classification: " + disputedDoc.getClassification());
		System.out.println(disputedDoc.DocTitle + " nearest to:");
		int count = 1;
		for(String docTitle: sortedDistances.keySet()) {
			System.out.printf(count + ": " + docTitle + "(%.6f)" + " - " + trainingClassifications.get(docTitle) + "\n\n", sortedDistances.get(docTitle));
			count++;
		}
		System.out.println("Hamilton Count: " + hCount);
		System.out.println("Madison Count: " + mCount);
		System.out.println("Hamilton Score: " + hScore);
		System.out.println("Madison Score: " + mScore);
		System.out.println();
	}
}
