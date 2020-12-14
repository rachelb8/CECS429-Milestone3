package cecs429.classification;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.ByteUtils;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;

public class VectorSpace {
    DocumentCorpus repCorpus;
    DiskPositionalIndex repIndex; 
    HashMap <Integer, DocVectorModel> vectors;
    List<String> vocab;
    
    public VectorSpace(DiskPositionalIndex dIndexArg, List<String> vocabularyArg, boolean existsBool){
        repCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dIndexArg.getCorpusPath()).toAbsolutePath());
        repCorpus.getDocuments();
        repIndex = dIndexArg;
        if (!existsBool){
            Index lIndex = DiskIndexWriter.indexCorpus(repCorpus);
            DiskIndexWriter.writeIndex(lIndex, dIndexArg.getCorpusPath());
        }
        vectors = new HashMap <Integer, DocVectorModel> ();
        vocab = vocabularyArg;
        initializeVectorSpace(dIndexArg.getCorpusPath());
    }
    
    public void initializeVectorSpace(String directoryArg){
        String directory = directoryArg;
        String dbName = directory + ".db";
		DB db = DBMaker.fileDB(dbName).make();
		BTreeMap<String, Integer> map = db.treeMap("map").keySerializer(Serializer.STRING)
					.valueSerializer(Serializer.INTEGER).createOrOpen();
		for (String lString : vocab){		
            Integer postingStartOffset = map.get(lString);
			DataInputStream dataInStrm = null;
			if(postingStartOffset != null) {
				try {
					dataInStrm = new DataInputStream(new FileInputStream(directory + "\\Postings.bin"));
		
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					dataInStrm.skipBytes(postingStartOffset);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Integer docFrequency = ByteUtils.DecodeNextInt(dataInStrm);
				Integer lastDocIDSum = 0;            
				for (int j = 0; j < docFrequency; j++){
					List<Integer> docGapEncode = ByteUtils.GetNextVariableBytes(dataInStrm);
					Integer docGapInt = ByteUtils.DecodeVariableByte(docGapEncode);
					lastDocIDSum = lastDocIDSum + docGapInt;
					Integer docId = lastDocIDSum;
					Double docScore = ByteUtils.DecodeNextDouble(dataInStrm);
					Integer termFrequency = ByteUtils.DecodeNextInt(dataInStrm);            
					for (int k = 0; k < termFrequency; k++){
						
						List<Integer> VBEncode = ByteUtils.GetNextVariableBytes(dataInStrm);
						@SuppressWarnings("unused")
						Integer posGapInt = ByteUtils.DecodeVariableByte(VBEncode);
                    }
                    // System.out.println("Adding " + lString + " to Vector " + docId + "(" 
                    //     + repCorpus.getDocument(docId).getTitle()+ ") with score" + docScore);
                    updateVector(docId, lString, docScore);
				}
            }
        }
        for (int lInt : vectors.keySet()){
            DocVectorModel lVector = vectors.get(lInt);
            double weight = repIndex.getDocWeight(lInt);
            lVector.setTitle(repCorpus.getDocument(lInt).getTitle());
            lVector.setWeight(weight);
            lVector.normalize();
        }
        // System.out.println(".");
    }

    public void updateVector(int idArg, String termArg, double docScore) {
        DocVectorModel lModel = vectors.get(idArg);
        if (lModel == null){
            lModel = new DocVectorModel(vocab);
            vectors.put(idArg, lModel);
        }
        lModel.addComponent(termArg, docScore);

    }
    public void updateVectorWeight(int idArg, double weight) {
        DocVectorModel lModel = vectors.get(idArg);
        if (lModel == null){
            lModel = new DocVectorModel(vocab);
            vectors.put(idArg, lModel);
        }
        lModel.setWeight(weight);

    }

    public void setClassifications(Classifier.DocClass classArg){
        for (DocVectorModel lModel: vectors.values()){
            lModel.setClassification(classArg);
        }
    }

    public List<String> getVocab(){
        return vocab;
    }


}
