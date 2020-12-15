package cecs429.classification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;

public class Classifier {
	
	static Path LOCAL_PATH = Paths.get("FedPapers");
	
	static String hPath = LOCAL_PATH + "/HAMILTON";
	static String jPath = LOCAL_PATH + "/JAY";
	static String mPath = LOCAL_PATH + "/MADISON";
	static String dPath = LOCAL_PATH + "/DISPUTED";
	static String aPath = LOCAL_PATH + "/ALL";

	DiskPositionalIndex allIndex;
	List<String> fullVocabList;
	
	static List<DocVectorModel> fullSpaceVectors;
	static List<DocVectorModel> disputedVectors;
	static List<DocVectorModel> trainingSetVectors;
	static VectorSpace[] trainingSets;
	
	static int BENCHMARK_K = 3;
	static int PRERECORD_K = 5;
	static int LIVEDEMO_K = 5;
	
	static int BENCHMARK_COMPONENTS_NUM = 10;
	static int PRERECORD_COMPONENTS_NUM = 30;
	
	public static enum DocClass {
		HAMILTON,
		MADISON,
		JAY,
		DISPUTED
	}
	
	public VectorSpace initializeFull(boolean existsBool){
		DocumentCorpus allCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(aPath).toAbsolutePath());

		allIndex = new DiskPositionalIndex(aPath);

		Index aInvertedIndex = DiskIndexWriter.indexCorpus(allCorpus);

		DiskIndexWriter.writeIndex(aInvertedIndex, aPath);

		return new VectorSpace(allIndex, aInvertedIndex.getVocabulary(), existsBool);
	}
	
	public List<String> getFullVocabSet()
	{
		return allIndex.getVocabulary();
	}

	public void runBenchmarkChecks() {
		/*
		 * KNN BENCHMARKS:
		 * kNN with k=3 and Euclidian distance:
		 * 
		 * "paper_50.txt" nearest to: 1: "paper_48.txt" (0.821775) 2: "paper_58.txt"
		 * (0.844832) 3: "paper_59.txt" (0.857783)
		 * 
		 * "paper_51.txt" nearest to: 1: "paper_10.txt" (0.794238) 2: "paper_43.txt"
		 * (0.799546) 3: "paper_78.txt" (0.811363
		 * 
		 * "paper_53.txt" First 10 components of vector: 0 0 0 0.0222767 0 0 0 0 0 0
		 * Nearest to: 1: "paper_36.txt" (0.798794) 2: "paper_84.txt" (0.800874) 3:
		 * "paper_58.txt" (0.812979)
		 */
		
		System.out.println("kNN Classification: ");
		DocVectorModel vector50 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_50.txt")).findAny().get();
		printFirstNVectorComponents(vector50, BENCHMARK_COMPONENTS_NUM);
		List<DocVectorModel> disputedVectors50 = new ArrayList<DocVectorModel>();
		disputedVectors50.add(vector50);
		KNNClassification.applyKNN(disputedVectors50, trainingSetVectors, BENCHMARK_K);
		
		DocVectorModel vector51 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_51.txt")).findAny().get();
		printFirstNVectorComponents(vector51, BENCHMARK_COMPONENTS_NUM);
		List<DocVectorModel> disputedVectors51 = new ArrayList<DocVectorModel>();
		disputedVectors51.add(vector51);
		KNNClassification.applyKNN(disputedVectors51, trainingSetVectors, BENCHMARK_K);
		
		DocVectorModel vector53 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_53.txt")).findAny().get();
		printFirstNVectorComponents(vector53, BENCHMARK_COMPONENTS_NUM);
		List<DocVectorModel> disputedVectors53 = new ArrayList<DocVectorModel>();
		disputedVectors53.add(vector53);
		KNNClassification.applyKNN(disputedVectors53, trainingSetVectors, BENCHMARK_K);
		System.out.println();
		
		
		/*
		 * ROCHIO BENCHMARKS:
		 * 
		 * First 10 components of centroid vector for /hamilton: 0.014753 0.000891329
		 * 0.001399 0 0.000401945 0.000607439 0.000339432 0.000394253 0.00126622 0
		 * 
		 * First 10 components of centroid vector for /madison: 0.0092167 0 0.0013554 0
		 * 0 0 0 0 0 0.00124324
		 * 
		 * First 10 components of centroid vector for /jay: 0 0 0 0 0 0 0 0 0 0
		 * 
		 * Dist to /hamilton for doc "paper_49.txt" is 0.603563 Dist to /madison for doc
		 * "paper_49.txt" is 0.602587 Dist to /jay for doc "paper_49.txt" is 0.69839 Low
		 * distance for "paper_49.txt" is /madison
		 */
		
		System.out.println("Rocchio Classification: ");
		DocVectorModel hCentroidVector = RocchioClassification.findCentroid(trainingSets[0]); // Hamilton
		DocVectorModel jCentroidVector = RocchioClassification.findCentroid(trainingSets[1]); // Jay
		DocVectorModel mCentroidVector = RocchioClassification.findCentroid(trainingSets[2]); // Madison
		
		System.out.println("Centroid vector for /hamilton: ");
		printFirstNVectorComponents(hCentroidVector, BENCHMARK_COMPONENTS_NUM);
		
		System.out.println("Centroid vector for /madison: ");
		printFirstNVectorComponents(mCentroidVector, BENCHMARK_COMPONENTS_NUM);
		
		System.out.println("Centroid vector for /jay: ");
		printFirstNVectorComponents(jCentroidVector, BENCHMARK_COMPONENTS_NUM);
		
		DocVectorModel vector49 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_49.txt")).findAny().get();
		List<DocVectorModel> disputedVectors49 = new ArrayList<DocVectorModel>();
		disputedVectors49.add(vector49);
		RocchioClassification.applyRocchio(disputedVectors49, trainingSets);
		
	}
	
	public void runPrerecordedData() {
		/*
		 * kNN classification:
		 * For the document "paper_53.txt":
		 * 	- the first 30 components (alphabetically) of the normalized vector for the document
		 *	- the 5 closest (k=5) vectors in the training set, and their Euclidian distances
		 */
		System.out.println("kNN Classification: ");
		DocVectorModel vector53 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_53.txt")).findAny().get();
		printFirstNVectorComponents(vector53, PRERECORD_COMPONENTS_NUM);
		List<DocVectorModel> disputedVectors53 = new ArrayList<DocVectorModel>();
		disputedVectors53.add(vector53);
		KNNClassification.applyKNN(disputedVectors53, trainingSetVectors, PRERECORD_K);
		System.out.println();
		
		/*
		 * Rocchio classification:
		 * For the document "paper_52.txt":
		 * 	- the first 30 components (alphabetically) of the normalized vector for the document
		 *	- the exact Euclidian distance between the normalized vector for the document and each of the 3 class centroids
		 */
		System.out.println("Rocchio Classification: ");
		DocVectorModel vector52 = disputedVectors.stream().filter(vector -> vector.getTitle().equals("paper_52.txt")).findAny().get();
		printFirstNVectorComponents(vector52, PRERECORD_COMPONENTS_NUM);
		List<DocVectorModel> disputedVectors52 = new ArrayList<DocVectorModel>();
		disputedVectors52.add(vector52);
		RocchioClassification.applyRocchio(disputedVectors52, trainingSets);
		
		
	}
	
	public void runLiveDemo() {
		/*
		 * kNN classification:
		 * The kNN classification decision for all disputed documents, using k=5
		 */
		System.out.println("kNN Classification: ");
		KNNClassification.applyKNN(disputedVectors, trainingSetVectors, LIVEDEMO_K);
		System.out.println();
		
		/*
		 * Rocchio classification:
		 * The Rocchio classification decision for all disputed documents
		 */
		System.out.println("Rocchio Classification: ");
		RocchioClassification.applyRocchio(disputedVectors, trainingSets);	
	}
	
	public static void printFirstNVectorComponents(DocVectorModel vector, int n) {
		System.out.println("\"" + vector.getTitle() + "\"");
		System.out.println("First " + n + " components of the normalized vector:");
		System.out.print("<");
		vector.vectorComponents.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Double>comparingByKey())
			.limit(n)
			.forEach(result -> System.out.print(result.getValue() + ", "));	
		System.out.println(">");
		System.out.println();
	}

	public static void main(String[] args) {
		Classifier c = new Classifier();
		boolean existsBool = true;
		VectorSpace fullSpace = c.initializeFull(existsBool);
		
		List<String> fullVocab = fullSpace.getVocab();
		
		VectorSpace hSpace = new VectorSpace(new DiskPositionalIndex(hPath), fullVocab, existsBool);
		hSpace.setClassifications(DocClass.HAMILTON);
		
		VectorSpace jSpace = new VectorSpace(new DiskPositionalIndex(jPath), fullVocab, existsBool);
		jSpace.setClassifications(DocClass.JAY);

		VectorSpace mSpace = new VectorSpace(new DiskPositionalIndex(mPath), fullVocab, existsBool);
		mSpace.setClassifications(DocClass.MADISON);

		VectorSpace dSpace = new VectorSpace(new DiskPositionalIndex(dPath), fullVocab, existsBool);
		dSpace.setClassifications(DocClass.DISPUTED);

		trainingSets = new VectorSpace[]{
			hSpace,
			jSpace,
			mSpace
		};
		
		fullSpaceVectors = new ArrayList<DocVectorModel>();
		for (DocVectorModel lVector : fullSpace.vectors.values()){
			fullSpaceVectors.add(lVector);
		}
		
		disputedVectors = new ArrayList<DocVectorModel>();
		for (DocVectorModel lVector : dSpace.vectors.values()){
			disputedVectors.add(lVector);
		}			
		
		trainingSetVectors = new ArrayList<DocVectorModel>();
		for (VectorSpace lSpace : trainingSets){
			for (DocVectorModel lTraining : lSpace.vectors.values()){
				trainingSetVectors.add(lTraining);
				for (DocVectorModel lVector : fullSpaceVectors){
					if (lTraining.getTitle().equals(lVector.DocTitle)){
						lVector.setClassification(lTraining.classification);
					}
				}
			}
		}	
//		c.runBenchmarkChecks();
//		c.runPrerecordedData();
		c.runLiveDemo();
	}
}
//========================== Code Graveyard ==============
//Here there be monsters
/*
 // public void buildNewIndexes(String dirSelection) {

	// 	DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(hPath).toAbsolutePath());
	// 	DocumentCorpus madisonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(mPath).toAbsolutePath());
	// 	DocumentCorpus jayCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(jPath).toAbsolutePath());
	// 	DocumentCorpus disputedCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dPath).toAbsolutePath());
		
	// 	hamiltonIndex = new DiskPositionalIndex(hPath);
	// 	madisonIndex = new DiskPositionalIndex(mPath);
	// 	jayIndex = new DiskPositionalIndex(jPath);
	// 	disputedIndex = new DiskPositionalIndex(dPath);

	// 	Index hInvertedIndex = DiskIndexWriter.indexCorpus(hamiltonCorpus);
	// 	Index mInvertedIndex = DiskIndexWriter.indexCorpus(madisonCorpus);
	// 	Index jInvertedIndex = DiskIndexWriter.indexCorpus(jayCorpus);
	// 	Index dInvertedIndex = DiskIndexWriter.indexCorpus(disputedCorpus);

		
	// 	DiskIndexWriter.writeIndex(hInvertedIndex, hPath);
	// 	DiskIndexWriter.writeIndex(mInvertedIndex, mPath);
	// 	DiskIndexWriter.writeIndex(jInvertedIndex, jPath);
	// 	DiskIndexWriter.writeIndex(dInvertedIndex, dPath);
		
		
	// }
	
	// public void loadExistingIndexes(String dirSelection) {
	// 	hamiltonIndex = new DiskPositionalIndex(hPath);
	// 	madisonIndex = new DiskPositionalIndex(mPath);
	// 	jayIndex = new DiskPositionalIndex(jPath);
	// 	disputedIndex = new DiskPositionalIndex(dPath);
	// }
	
	// public void initializeFullVocabSet() {
	// 	List<String> hVocab = hamiltonIndex.getVocabulary();
	// 	List<String> mVocab = madisonIndex.getVocabulary();
	// 	List<String> jVocab = jayIndex.getVocabulary();
	// 	List<String> dVocab = disputedIndex.getVocabulary();
		
	// 	List<String> fullVocab = new ArrayList<String>();
	// 	fullVocab.addAll(hVocab);
	// 	fullVocab.addAll(mVocab);
	// 	fullVocab.addAll(jVocab);
	// 	fullVocab.addAll(dVocab);
		
	// 	fullVocabList = new ArrayList<>(new HashSet<>(fullVocab));
	// 	fullVocabList.removeAll(Arrays.asList("", null));
	// 	Collections.sort(fullVocabList);
	// }
	 // public VectorSpace initializeVectorSpace(DocClass classArg, List<String> vocList){
	// 	String directory = "";
	// 	DiskPositionalIndex lIndex;
	// 	switch (classArg) {
	// 		case HAMILTON:
	// 			directory = hPath;
	// 			lIndex = hamiltonIndex;
	// 			break;

	// 		case JAY:
	// 			directory = jPath;
	// 			lIndex = jayIndex;
	// 			break;
			
	// 		case MADISON:
	// 			directory = mPath;
	// 			lIndex = madisonIndex;
	// 			break;

	// 		case DISPUTED:
	// 			directory = dPath;
	// 			lIndex = disputedIndex;
	// 			break;
		
	// 		default:
	// 			directory = "";
	// 			lIndex = null;
	// 			break;
	// 	}
		
	// 	VectorSpace resultSpace = new VectorSpace(vocList);
	// 	String dbName = directory + ".db";
	// 	DB db = DBMaker.fileDB(dbName).make();
	// 	BTreeMap<String, Integer> map = db.treeMap("map").keySerializer(Serializer.STRING)
	// 				.valueSerializer(Serializer.INTEGER).createOrOpen();
	// 	for (String lString : vocList){		
	// 		Integer postingStartOffset = map.get(lString);
	// 		DataInputStream dataInStrm = null;
	// 		if(postingStartOffset != null) {
	// 			try {
	// 				dataInStrm = new DataInputStream(new FileInputStream(directory + "\\Postings.bin"));
		
	// 			} catch (FileNotFoundException e) {
	// 				// TODO Auto-generated catch block
	// 				e.printStackTrace();
	// 			}
	// 			try {
	// 				dataInStrm.skipBytes(postingStartOffset);
	// 			} catch (IOException e) {
	// 				// TODO Auto-generated catch block
	// 				e.printStackTrace();
	// 			}
	// 			Integer docFrequency = ByteUtils.DecodeNextInt(dataInStrm);
	// 			Integer lastDocIDSum = 0;            
	// 			for (int j = 0; j < docFrequency; j++){
	// 				List<Integer> docGapEncode = ByteUtils.GetNextVariableBytes(dataInStrm);
	// 				Integer docGapInt = ByteUtils.DecodeVariableByte(docGapEncode);
	// 				lastDocIDSum = lastDocIDSum + docGapInt;
	// 				Integer docId = lastDocIDSum;
	// 				Double docScore = ByteUtils.DecodeNextDouble(dataInStrm);
	// 				Integer termFrequency = ByteUtils.DecodeNextInt(dataInStrm);            
	// 				for (int k = 0; k < termFrequency; k++){
						
	// 					List<Integer> VBEncode = ByteUtils.GetNextVariableBytes(dataInStrm);
	// 					@SuppressWarnings("unused")
	// 					Integer posGapInt = ByteUtils.DecodeVariableByte(VBEncode);
	// 				}
	// 				updateVector(lIndex, resultSpace, classArg, docId, lString, docScore);


	// 			}
	// 		}
			

	// 	}
	// 	db.close();	
	// 	return resultSpace;
	// }
	
	// public static void updateVector(DiskPositionalIndex indexArg, VectorSpace spaceArg, DocClass classArg, int idArg, String termArg, double scoreArg){
	// 	DocVectorModel lModel = spaceArg.getVectorModel(classArg, idArg);
	// 	if (lModel == null) {
	// 		lModel = new DocVectorModel(classArg);
	// 		lModel.setDocId(idArg);
	// 		lModel.setWeight(indexArg.getDocWeight(idArg));
	// 		spaceArg.addVector(lModel);
	// 	}
	// 	lModel.addComponent(termArg, scoreArg);
	// }

	//Construct indexs for each collection
	//Construct 
				//Test for benchmark check
//			if(lVector.DocTitle.equals("paper_53.txt")) {
				
//				lVector.vectorComponents.entrySet()
//				  .stream()
//				  .sorted(Map.Entry.<String, Double>comparingByKey())
//				  .forEach(System.out::println);
//			}
 * //		 for (DocVectorModel lVectorModel: fullSpace.vectors.values()) {
//		 	System.out.println(lVectorModel.DocTitle + " - " + lVectorModel.classification);
//		 }
		
//		for (String lString : fullSpace.vocab){
//			System.out.print(lString + " ");
//		}	
 */