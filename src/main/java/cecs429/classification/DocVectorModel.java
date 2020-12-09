package cecs429.classification;
import cecs429.documents.*;


import java.util.HashMap;

public class DocVectorModel {
    int docId;
    HashMap vectorComponents = new HashMap<String, Double>();
    Classifier.DocClass classification;
    double docWeight;

    public DocVectorModel(Classifier.DocClass classArg, List<String> listArg) {
        classification = classArg;
    }

    public void addComponent(String stringArg, double scoreArg){
        vectorComponents.put(stringArg, scoreArg);
    }

    public void setWeight(double weightArg){
        docWeight = weightArg;
    }

    public void setDocId(int idArg){
        docId = idArg;
    }

    public int getDocId() {
        return docId;
    }
    
    public void setClassification(Classifier.DocClass classArg){
        classification = classArg;
    }

    public Classifier.DocClass getClassification(){
        return classification;
    }
    
}
