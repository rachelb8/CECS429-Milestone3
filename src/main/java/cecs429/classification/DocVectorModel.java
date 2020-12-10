package cecs429.classification;

import cecs429.documents.*;

import java.util.HashMap;
import java.util.List;

public class DocVectorModel {
    Classifier.DocClass classification = null;
    HashMap vectorComponents = new HashMap<String, Double>();
    double docWeight;

    public DocVectorModel(List<String> vocab) {
        for (String lString : vocab ){
            vectorComponents.put(lString, 0.0);
        }
    }

    public void addComponent(String stringArg, double scoreArg){
        vectorComponents.put(stringArg, scoreArg);
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
    
}
