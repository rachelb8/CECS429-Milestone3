package cecs429.classification;

import java.util.HashMap;
import java.util.List;

public class DocVectorModel {
    String DocTitle; 
    Classifier.DocClass classification = null;
    HashMap<String,Double> vectorComponents = new HashMap<String, Double>();
    double docWeight;
    boolean normalizedScores = false;

    public DocVectorModel(List<String> vocab) {
        for (String lString : vocab ){
            vectorComponents.put(lString, 0.0);
        }
    }

    public void normalize(){        
        if (!normalizedScores){
            for (String lKey : vectorComponents.keySet()){
                double originalScore = vectorComponents.get(lKey);
                vectorComponents.put(lKey, originalScore/docWeight);
            }
            normalizedScores = true;
        }
    }

    public void addComponent(String stringArg, double scoreArg){
        vectorComponents.put(stringArg, scoreArg);
    }

    public void setTitle(String titleArg){
        DocTitle = titleArg;
    }

    public String getTitle(){
        return DocTitle;
    }

    public void setWeight(double weightArg){
        docWeight = weightArg;
    }

    public double getWeight(){
        return docWeight;
    }
    
    public void setClassification(Classifier.DocClass classArg){
        classification = classArg;
    }

    public Classifier.DocClass getClassification(){
        return classification;
    }
    
    public static double euclidDistance(DocVectorModel vector1, DocVectorModel vector2) {
    	double total = 0;
    	double diff;
    	
    	for (String lKey : vector1.vectorComponents.keySet()){
    		diff = vector2.vectorComponents.get(lKey) - vector1.vectorComponents.get(lKey);
    		total += diff * diff;
    	}
    	return Math.sqrt(total);
    }
}
