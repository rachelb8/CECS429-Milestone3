package cecs429.classification;

import java.util.ArrayList;
import java.util.List;

public class VectorSpace {
    List<DocVectorModel> vectors;
    List<String> vocab;
    public VectorSpace(List<String> vocabArg){
        vectors = new ArrayList<DocVectorModel>();
        vocab = vocabArg;
    }

    public void addVector(DocVectorModel vectorArg){
        vectors.add(vectorArg);
    }

    public void addVectorSpace(VectorSpace spaceArg){
        for (DocVectorModel lVectorModel : spaceArg.vectors){
            vectors.add(lVectorModel);
        }        
    }

    public DocVectorModel getVectorModel(Classifier.DocClass classificationArg, int docId){
        DocVectorModel resultVectorModel = null;
        for (DocVectorModel lVector : vectors){
            if (!lVector.classification.equals(classificationArg)){
                continue;
            }
            if (lVector.docId != docId){
                continue;
            }
            resultVectorModel = lVector;
        }
        return resultVectorModel;

    }

    public List<String> getVocab(){
        return vocab;
    }
}
