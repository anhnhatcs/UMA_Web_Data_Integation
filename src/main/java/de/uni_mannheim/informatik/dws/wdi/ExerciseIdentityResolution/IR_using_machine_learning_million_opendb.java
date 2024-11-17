package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution;
import java.io.File;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators.*;
import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.HashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVCorrespondenceFormatter;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.SongXMLReader;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Blocking.SongBlockingKeyByArtistGenerator;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Blocking.SongBlockingKeyByTitleGenerator;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Blocking.SongBlockingKeyByAlbumGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.algorithms.RuleLearner;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.SortedNeighbourhoodBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.WekaMatchingRule;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;


public class IR_using_machine_learning_million_opendb {
	
	/*
	 * Logging Options:
	 * 		default: 	level INFO	- console
	 * 		trace:		level TRACE     - console
	 * 		infoFile:	level INFO	- console/file
	 * 		traceFile:	level TRACE	- console/file
	 *  
	 * To set the log level to trace and write the log to winter.log and console, 
	 * activate the "traceFile" logger as follows:
	 *     private static final Logger logger = WinterLogManager.activateLogger("traceFile");
	 *
	 */

	private static final Logger logger = WinterLogManager.activateLogger("default");
	
    public static void main( String[] args ) throws Exception
    {
		// loading data
		logger.info("*\tLoading datasets\t*");
		HashedDataSet<Song, Attribute> dataMillion = new HashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/input/million.xml"), "/songs/song", dataMillion);
		HashedDataSet<Song, Attribute> dataOpenDB = new HashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/input/opendb.xml"), "/songs/song", dataOpenDB);
		
		// load the training set
		MatchingGoldStandard gsTraining = new MatchingGoldStandard();
		gsTraining.loadFromCSVFile(new File("data/goldstandard/gs_million_opendb.csv"));

		// create a matching rule
		String options[] = new String[] { "-S" };
		String modelType = "SimpleLogistic"; // use a logistic regression
		WekaMatchingRule<Song, Attribute> matchingRule = new WekaMatchingRule<>(0.7, modelType, options);
		matchingRule.activateDebugReport("data/output/debugResultsMatchingRule_million_opendb.csv", 1000, gsTraining);
		
		// add comparators
		matchingRule.addComparator(new SongTitleComparatorLevenshtein());
		// matchingRule.addComparator(new SongTitleComparatorJaccard());
		matchingRule.addComparator(new SongTitleComparatorLowerCaseJaccard());
		// matchingRule.addComparator(new SongTitleComparatorJaroWinkler());
		// matchingRule.addComparator(new SongTitleComparatorSoundex());
		// matchingRule.addComparator(new SongTitleComparatorEqual());
		// matchingRule.addComparator(new SongTitleComparatorJaro());

		// matchingRule.addComparator(new SongArtistComparatorLevenshtein());
		// matchingRule.addComparator(new SongArtistComparatorJaccard());
		// matchingRule.addComparator(new SongArtistComparatorLowerCaseJaccard());
		// matchingRule.addComparator(new SongArtistComparatorJaroWinkler());
		// matchingRule.addComparator(new SongArtistComparatorSoundex());
		// matchingRule.addComparator(new SongArtistComparatorEqual());
		matchingRule.addComparator(new SongArtistComparatorJaro());

		// matchingRule.addComparator(new SongAlbumComparatorLevenshtein());
		// matchingRule.addComparator(new SongAlbumComparatorJaccard());
		// matchingRule.addComparator(new SongAlbumComparatorLowerCaseJaccard());
		// matchingRule.addComparator(new SongAlbumComparatorJaroWinkler());
		// matchingRule.addComparator(new SongAlbumComparatorSoundex());
		// matchingRule.addComparator(new SongAlbumComparatorEqual());
		matchingRule.addComparator(new SongAlbumComparatorJaro());
		
		
		// train the matching rule's model
		logger.info("*\tLearning matching rule\t*");
		RuleLearner<Song, Attribute> learner = new RuleLearner<>();
		learner.learnMatchingRule(dataMillion, dataOpenDB, null, matchingRule, gsTraining);
		logger.info(String.format("Matching rule is:\n%s", matchingRule.getModelDescription()));
		
		// create a blocker (blocking strategy)
		StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByTitleGenerator());
		// StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByArtistGenerator());		
		// StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByAlbumGenerator());		
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByTitleGenerator(), 40);
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByArtistGenerator(), 30);
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByAlbumGenerator(), 30);
		blocker.collectBlockSizeData("data/output/debugResultsBlocking_million_opendb.csv", 10000);
		
		// Initialize Matching Engine
		MatchingEngine<Song, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		logger.info("*\tRunning identity resolution\t*");
		Processable<Correspondence<Song, Attribute>> correspondences = engine.runIdentityResolution(
			dataMillion, dataOpenDB, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("data/output/million_opendb_correspondences.csv"), correspondences);

		// load the gold standard (test set)
		logger.info("*\tLoading gold standard\t*");
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"data/goldstandard/gs_million_opendb.csv"));
		
		// evaluate your result
		logger.info("*\tEvaluating result\t*");
		MatchingEvaluator<Song, Attribute> evaluator = new MatchingEvaluator<Song, Attribute>();
		Performance perfTest = evaluator.evaluateMatching(correspondences,
				gsTest);
		
		// print the evaluation result
		logger.info("Million <-> OpenDB");
		logger.info(String.format(
				"Precision: %.4f",perfTest.getPrecision()));
		logger.info(String.format(
				"Recall: %.4f",	perfTest.getRecall()));
		logger.info(String.format(
				"F1: %.4f",perfTest.getF1()));
    }
}
