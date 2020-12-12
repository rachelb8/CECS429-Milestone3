package cecs429.classification;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.*;
import static java.util.Map.Entry.*;

public class KNNClassification {
	
	public static void applyKNN(List<DocVectorModel> disputedDocs, List<DocVectorModel> trainingSetVectors, int k) {
		for(DocVectorModel disputedDoc: disputedDocs) {
			Map<String, Double> distances = new HashMap<String, Double>();
			for (int i = 0; i < trainingSetVectors.size(); i++) {
				distances.put(trainingSetVectors.get(i).DocTitle, DocVectorModel.euclidDistance(disputedDoc, trainingSetVectors.get(i)));
			}
			
			Map<String, Double> sortedDistances = distances.entrySet()
					.stream()
					.sorted(comparingByValue())
					.limit(k)
					.collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));
		
			int hCount = 0;
			int jCount = 0;
			int mCount = 0;
			Map<String, Classifier.DocClass> trainingClassifications = new HashMap<String, Classifier.DocClass>();
			for(String docTitle: sortedDistances.keySet()) {
				for (DocVectorModel lVector : trainingSetVectors){
					
					if (docTitle.equals(lVector.DocTitle)){
						trainingClassifications.put(docTitle, lVector.getClassification());
						switch(lVector.classification) {
							case HAMILTON:
								hCount++;
								break;
							case JAY:
								jCount++;
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
			
			if (hCount > jCount && hCount > mCount) {
				disputedDoc.setClassification(Classifier.DocClass.HAMILTON);
			} else if(jCount > hCount && jCount > mCount) {
				disputedDoc.setClassification(Classifier.DocClass.JAY);
			} else if(mCount > hCount && mCount > jCount) {
				disputedDoc.setClassification(Classifier.DocClass.MADISON);
			} else {
				// Tie Logic
			}
			
			System.out.println(disputedDoc.DocTitle + " classification: " + disputedDoc.getClassification());
			System.out.println(disputedDoc.DocTitle + " nearest to:");
			int count = 1;
			for(String docTitle: sortedDistances.keySet()) {
				System.out.printf(count + ": " + docTitle + "(%.6f)" + " - " + trainingClassifications.get(docTitle) + "\n\n", sortedDistances.get(docTitle));
				count++;
			}
		}
		
	}
}
