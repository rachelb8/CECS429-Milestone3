package cecs429.classification;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import cecs429.classification.Classifier.DocClass;
import ch.qos.logback.core.net.SyslogOutputStream;
import static java.util.stream.Collectors.*;
import java.util.ArrayList;
import static java.util.Map.Entry.*;

public class RocchioClassification {

	public static DocVectorModel findCentroid(VectorSpace vs) {
		List<DocVectorModel> classVectors = new ArrayList<DocVectorModel>();
		for (DocVectorModel lVector : vs.vectors.values()) {
			classVectors.add(lVector);
		}
		DocVectorModel centroid = classVectors.get(0);
		for (int i = 1; i < classVectors.size(); i++) {
			for (Map.Entry<String, Double> entry : classVectors.get(i).vectorComponents.entrySet()) {
				centroid.vectorComponents.put(entry.getKey(), centroid.vectorComponents.get(entry.getKey()) + entry.getValue());
			}
		}

		for (Map.Entry<String, Double> entry : centroid.vectorComponents.entrySet()) {
			centroid.vectorComponents.put(entry.getKey(), centroid.vectorComponents.get(entry.getKey()) / classVectors.size());
		}

		// centroid.vectorComponents.entrySet().stream()
		//  								    .sorted(Map.Entry.<String, Double>comparingByKey())
		// 									.forEach(System.out::println);
		return centroid;
	}

    public static void applyRocchio(List<DocVectorModel> disputedDocs, VectorSpace[] trainingSets) {

        DocVectorModel hCentroidVector = findCentroid(trainingSets[0]); // Hamilton
		DocVectorModel jCentroidVector = findCentroid(trainingSets[1]); // Jay
		DocVectorModel mCentroidVector = findCentroid(trainingSets[2]); // Madison

		for (DocVectorModel disputedDoc : disputedDocs) {
			
			Double hDistance = DocVectorModel.euclidDistance(disputedDoc, hCentroidVector); // Distance to Hamilton centroid
			Double mDistance = DocVectorModel.euclidDistance(disputedDoc, mCentroidVector); // Distance to Madison centroid
			Double jDistance = DocVectorModel.euclidDistance(disputedDoc, jCentroidVector); // Distance to Jay centroid

			if (hDistance < mDistance && hDistance < jDistance) {
				disputedDoc.setClassification(DocClass.HAMILTON);
			} else if (mDistance < hDistance && mDistance < jDistance) {
				disputedDoc.setClassification(DocClass.MADISON);
			} else {
				disputedDoc.setClassification(DocClass.JAY);
			}

			System.out.printf("Dist to /hamilton for doc " + disputedDoc.DocTitle + " is %.6f" + "\n" ,hDistance);
			System.out.printf("Dist to /madison for doc " + disputedDoc.DocTitle + " is %.6f" + "\n" ,mDistance);
			System.out.printf("Dist to /jay for doc " + disputedDoc.DocTitle + " is %.6f" + "\n" ,jDistance);
			System.out.println("Lowest distance for " + disputedDoc.DocTitle + " is /" + disputedDoc.getClassification().toString().toLowerCase());
			System.out.println();
		}
    }
}