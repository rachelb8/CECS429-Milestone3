package cecs429.classification;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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

public class Classifier {
	
	// ENTER YOUR LOCAL PATH TO THE FEDERALIST PAPERS FOLDER
	static String LOCAL_PATH = "C:\\Users\\Jonathan\\Documents\\GitHub\\CECS429-Milestone3\\FedPapers";
	static String hPath = LOCAL_PATH + "/HAMILTON";
	static String jPath = LOCAL_PATH + "/JAY";
	static String mPath = LOCAL_PATH + "/MADISON";
	static String dPath = LOCAL_PATH + "/DISPUTED";
	DiskPositionalIndex hamiltonIndex;
	DiskPositionalIndex madisonIndex;
	DiskPositionalIndex jayIndex;
	DiskPositionalIndex disputedIndex;
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
	List<String> fullVocabList;
	
	public static enum DocClass {
		HAMILTON,
		MADISON,
		JAY,
		DISPUTED
	}
	

	public void buildNewIndexes(String dirSelection) {
<<<<<<< Updated upstream
		DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/HAMILTON").toAbsolutePath());
		DocumentCorpus madisonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/MADISON").toAbsolutePath());
		DocumentCorpus jayCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/JAY").toAbsolutePath());
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/DISPUTED").toAbsolutePath());
=======
		DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(hPath).toAbsolutePath());
		DocumentCorpus madisonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(mPath).toAbsolutePath());
		DocumentCorpus jayCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(jPath).toAbsolutePath());
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dPath).toAbsolutePath());
>>>>>>> Stashed changes
		
		hamiltonIndex = new DiskPositionalIndex(hPath);
		madisonIndex = new DiskPositionalIndex(mPath);
		jayIndex = new DiskPositionalIndex(jPath);
		disputedIndex = new DiskPositionalIndex(dPath);

		Index hInvertedIndex = DiskIndexWriter.indexCorpus(hamiltonCorpus);
		Index mInvertedIndex = DiskIndexWriter.indexCorpus(madisonCorpus);
		Index jInvertedIndex = DiskIndexWriter.indexCorpus(jayCorpus);
		Index dInvertedIndex = DiskIndexWriter.indexCorpus(disputedCorpus);
<<<<<<< Updated upstream
		
		DiskIndexWriter.writeIndex(hInvertedIndex, dirSelection + "/HAMILTON");
		DiskIndexWriter.writeIndex(mInvertedIndex, dirSelection + "/MADISON");
		DiskIndexWriter.writeIndex(jInvertedIndex, dirSelection + "/JAY");
		DiskIndexWriter.writeIndex(dInvertedIndex, dirSelection + "/DISPUTED");
		
		hamiltonIndex = new DiskPositionalIndex(dirSelection + "/HAMILTON");
		madisonIndex = new DiskPositionalIndex(dirSelection + "/MADISON");
		jayIndex = new DiskPositionalIndex(dirSelection + "/JAY");
		disputedIndex = new DiskPositionalIndex(dirSelection + "/DISPUTED");
	}
	
	public void loadExistingIndexes(String dirSelection) {
		hamiltonIndex = new DiskPositionalIndex(dirSelection + "/HAMILTON");
		madisonIndex = new DiskPositionalIndex(dirSelection + "/MADISON");
		jayIndex = new DiskPositionalIndex(dirSelection + "/JAY");
		disputedIndex = new DiskPositionalIndex(dirSelection + "/DISPUTED");
=======

		
		DiskIndexWriter.writeIndex(hInvertedIndex, hPath);
		DiskIndexWriter.writeIndex(mInvertedIndex, mPath);
		DiskIndexWriter.writeIndex(jInvertedIndex, jPath);
		DiskIndexWriter.writeIndex(dInvertedIndex, dPath);
		
		
	}
	
	public void loadExistingIndexes(String dirSelection) {
		hamiltonIndex = new DiskPositionalIndex(hPath);
		madisonIndex = new DiskPositionalIndex(mPath);
		jayIndex = new DiskPositionalIndex(jPath);
		disputedIndex = new DiskPositionalIndex(dPath);
>>>>>>> Stashed changes
	}
	
	public void initializeFullVocabSet() {
		List<String> hVocab = hamiltonIndex.getVocabulary();
		List<String> mVocab = madisonIndex.getVocabulary();
		List<String> jVocab = jayIndex.getVocabulary();
		List<String> dVocab = disputedIndex.getVocabulary();
		
		List<String> fullVocab = new ArrayList<String>();
		fullVocab.addAll(hVocab);
		fullVocab.addAll(mVocab);
		fullVocab.addAll(jVocab);
		fullVocab.addAll(dVocab);
		
		fullVocabList = new ArrayList<>(new HashSet<>(fullVocab));
		fullVocabList.removeAll(Arrays.asList("", null));
		Collections.sort(fullVocabList);
	}
	
	public List<String> getFullVocabSet()
	{
		return fullVocabList;
	}

	public VectorSpace initializeVectorSpace(DocClass classArg, List<String> vocList){
		String directory = "";
		DiskPositionalIndex lIndex;
		switch (classArg) {
			case HAMILTON:
				directory = hPath;
				lIndex = hamiltonIndex;
				break;

			case JAY:
				directory = jPath;
				lIndex = jayIndex;
				break;
			
			case MADISON:
				directory = mPath;
				lIndex = madisonIndex;
				break;

			case DISPUTED:
				directory = dPath;
				lIndex = disputedIndex;
				break;
		
			default:
				directory = "";
				lIndex = null;
				break;
		}
		
		VectorSpace resultSpace = new VectorSpace(vocList);
		String dbName = directory + ".db";
		DB db = DBMaker.fileDB(dbName).make();
		BTreeMap<String, Integer> map = db.treeMap("map").keySerializer(Serializer.STRING)
					.valueSerializer(Serializer.INTEGER).createOrOpen();
		for (String lString : vocList){		
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
					updateVector(lIndex, resultSpace, classArg, docId, lString, docScore);


				}
			}
			

		}
		db.close();	
		return resultSpace;
	}
	
	public static void updateVector(DiskPositionalIndex indexArg, VectorSpace spaceArg, DocClass classArg, int idArg, String termArg, double scoreArg){
		DocVectorModel lModel = spaceArg.getVectorModel(classArg, idArg);
		if (lModel == null) {
			lModel = new DocVectorModel(classArg);
			lModel.setDocId(idArg);
			lModel.setWeight(indexArg.getDocWeight(idArg));
			spaceArg.addVector(lModel);
		}
		lModel.addComponent(termArg, scoreArg);
	}
	public static void main(String[] args) {
		Classifier c = new Classifier();
//		c.buildNewIndexes(LOCAL_PATH);
		c.loadExistingIndexes(LOCAL_PATH);
		c.initializeFullVocabSet();
		VectorSpace fullSpace = new VectorSpace(c.getFullVocabSet());
		VectorSpace hSpace = c.initializeVectorSpace(DocClass.HAMILTON, c.getFullVocabSet());
		VectorSpace jSpace = c.initializeVectorSpace(DocClass.JAY, c.getFullVocabSet());
		VectorSpace mSpace = c.initializeVectorSpace(DocClass.MADISON, c.getFullVocabSet());
		VectorSpace uSpace = c.initializeVectorSpace(DocClass.DISPUTED, c.getFullVocabSet());

		for (String s: c.getFullVocabSet()) {
			System.out.print(s + " ");
		}
		
		System.out.println();
		System.out.println(c.getFullVocabSet().size());
	}
}
