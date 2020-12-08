package cecs429.classification;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import cecs429.documents.DirectoryCorpus;
import cecs429.documents.DocumentCorpus;
import cecs429.index.DiskIndexWriter;
import cecs429.index.DiskPositionalIndex;
import cecs429.index.Index;

public class Classifier {
	
	// ENTER YOUR LOCAL PATH TO THE FEDERALIST PAPERS FOLDER
	static String LOCAL_PATH = "ENTER PATH HERE PLZ";
	DiskPositionalIndex hamiltonIndex;
	DiskPositionalIndex madisonIndex;
	DiskPositionalIndex jayIndex;
	DiskPositionalIndex disputedIndex;
	List<String> fullVocabList;
	

	public void buildNewIndexes(String dirSelection) {
		DocumentCorpus hamiltonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/HAMILTON").toAbsolutePath());
		DocumentCorpus madisonCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/MADISON").toAbsolutePath());
		DocumentCorpus jayCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/JAY").toAbsolutePath());
		DocumentCorpus disputedCorpus = DirectoryCorpus.loadMilestone1Directory(Paths.get(dirSelection + "/DISPUTED").toAbsolutePath());
		
		Index hInvertedIndex = DiskIndexWriter.indexCorpus(hamiltonCorpus);
		Index mInvertedIndex = DiskIndexWriter.indexCorpus(madisonCorpus);
		Index jInvertedIndex = DiskIndexWriter.indexCorpus(jayCorpus);
		Index dInvertedIndex = DiskIndexWriter.indexCorpus(disputedCorpus);
		
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
	
	public static void main(String[] args) {
		Classifier c = new Classifier();
		c.buildNewIndexes(LOCAL_PATH);
//		c.loadExistingIndexes(LOCAL_PATH);
		c.initializeFullVocabSet();
		for (String s: c.getFullVocabSet()) {
			System.out.print(s + " ");
		}
		
		System.out.println();
		System.out.println(c.getFullVocabSet().size());
	}
}
