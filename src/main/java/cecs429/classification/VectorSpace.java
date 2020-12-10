package cecs429.classification;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import cecs429.query.RankedRetrieval.DocumentScore;

public class VectorSpace {
    DocumentCorpus repCorpus;
    DiskPositionalIndex repIndex; 
    HashMap <Integer, DocVectorModel> vectors;
    List<String> vocab;
    
    public VectorSpace(String directory, boolean existingIndex){
        repCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(directory).toAbsolutePath());
        repCorpus.getDocuments();
        if (existingIndex){
            repIndex = new DiskPositionalIndex(directory);            
        }
        else{
            repIndex = new DiskPositionalIndex(directory);
            Index repInvertedIndex = DiskIndexWriter.indexCorpus(repCorpus);
            DiskIndexWriter.writeIndex(repInvertedIndex, directory);
        }
        vectors = new HashMap <Integer, DocVectorModel> ();
        vocab = repIndex.getVocabulary();
        initializeVectorSpace(directory);
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
            lVector.setWeight(weight);
        }
        for (int id: vectors.keySet()){
            // System.out.println(repCorpus.getDocument(id).getTitle());
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
