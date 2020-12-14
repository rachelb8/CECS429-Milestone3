package cecs429.classification;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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

	public void ClassifyVectors(){

	}

	public static void main(String[] args) {
		Classifier c = new Classifier();
		boolean existsBool = true;
		VectorSpace fullSpace = c.initializeFull(existsBool);
		
		List<String> fullVocab = fullSpace.getVocab();
		System.out.println("Done");
		
		VectorSpace hSpace = new VectorSpace(new DiskPositionalIndex(hPath), fullVocab, existsBool);
		hSpace.setClassifications(DocClass.HAMILTON);
		
		VectorSpace jSpace = new VectorSpace(new DiskPositionalIndex(jPath), fullVocab, existsBool);
		jSpace.setClassifications(DocClass.JAY);

		VectorSpace mSpace = new VectorSpace(new DiskPositionalIndex(mPath), fullVocab, existsBool);
		mSpace.setClassifications(DocClass.MADISON);

		VectorSpace dSpace = new VectorSpace(new DiskPositionalIndex(dPath), fullVocab, existsBool);
		dSpace.setClassifications(DocClass.DISPUTED);

		VectorSpace[] trainingSets = new VectorSpace[]{
			hSpace,
			jSpace,
			mSpace//,dSpace
		};
		List<DocVectorModel> fullSpaceVectors = new ArrayList<DocVectorModel>();
		for (DocVectorModel lVector : fullSpace.vectors.values()){
			fullSpaceVectors.add(lVector);
		}

		List<DocVectorModel> disputedVectors = new ArrayList<DocVectorModel>();
		for (DocVectorModel lVector : dSpace.vectors.values()){
			disputedVectors.add(lVector);
		}
		
		for (VectorSpace lSpace : trainingSets){
			for (DocVectorModel lTraining : lSpace.vectors.values()){
				for (DocVectorModel lVector : fullSpaceVectors){
					if (lTraining.getTitle().equals(lVector.DocTitle)){
						lVector.setClassification(lTraining.classification);
						//System.out.println(lVector.DocTitle + " - " + lVector.getClassification());
					}
				}
			}
		}

		// Rocchio Classification
		RocchioClassification.applyRocchio(disputedVectors, trainingSets);
		
		// for (String lString : fullSpace.vocab){
		// 	System.out.print(lString + " ");
		// }

		// System.out.println();
		// System.out.println(c.getFullVocabSet().size());
	}
}
