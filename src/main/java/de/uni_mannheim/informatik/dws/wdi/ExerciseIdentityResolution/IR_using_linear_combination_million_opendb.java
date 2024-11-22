package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution;
import java.io.File;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators.*;
import org.slf4j.Logger;

import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.StandardRecordBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.SortedNeighbourhoodBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
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


public class IR_using_linear_combination_million_opendb {
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

		// load the gold standard (test set)
		logger.info("*\tLoading gold standard\t*");
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(
				"data/goldstandard/gs_million_opendb.csv"));

		// create a matching rule
		// set the finalThreshold as our data requires
		LinearCombinationMatchingRule<Song, Attribute> matchingRule = new LinearCombinationMatchingRule<>(
				0.8);
		matchingRule.activateDebugReport("data/output/debugResultsMatchingRule.csv", 1000, gsTest);
		
		// add comparators
		// here defines the weight of matching attributes
		// matchingRule.addComparator(new SongTitleComparatorEqual(), 0.2);
		// matchingRule.addComparator(new SongTitleComparatorJaccard(), 0.2);
		// matchingRule.addComparator(new SongTitleComparatorJaro(), 0.3);
		// matchingRule.addComparator(new SongTitleComparatorJaroWinkler(), 0.4);
		// matchingRule.addComparator(new SongTitleComparatorLevenshtein(), 0.4);
		// matchingRule.addComparator(new SongTitleComparatorSoundex(), 0.4);

		matchingRule.addComparator(new SongArtistComparatorEqual(), 0.25);
		// matchingRule.addComparator(new SongArtistComparatorJaccard(), 0.2);
		// matchingRule.addComparator(new SongArtistComparatorJaro(), 0.2);
		matchingRule.addComparator(new SongTitleComparatorLowerCaseJaccard(), 0.6);
		// matchingRule.addComparator(new SongArtistComparatorLowerCaseJaccard(), 0.4);
		matchingRule.addComparator(new SongAlbumYearComparator2Years(), 0.15);

		// matchingRule.addComparator(new SongArtistComparatorLevenshtein(), 0.2);
		// matchingRule.addComparator(new SongArtistComparatorLowerCaseJaccard(), 0.2);
		// matchingRule.addComparator(new SongArtistComparatorSoundex(), 0.2);
		
		// matchingRule.addComparator(new SongAlbumComparatorEqual(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorJaccard(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorJaro(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorJaroWinkler(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorLevenshtein(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorLowerCaseJaccard(), 0.2);
		// matchingRule.addComparator(new SongAlbumComparatorSoundex(), 0.2);

		// matchingRule.addComparator(new SongDurationComparator2Seconds(), 0.2);
		// matchingRule.addComparator(new SongDurationComparator10Seconds(), 0.2);
		// create a blocker (blocking strategy)
		StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByTitleGenerator());
		// StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByArtistGenerator());		
		// StandardRecordBlocker<Song, Attribute> blocker = new StandardRecordBlocker<Song, Attribute>(new SongBlockingKeyByAlbumGenerator());		
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByTitleGenerator(), 60);
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByArtistGenerator(), 30);
		// SortedNeighbourhoodBlocker<Song, Attribute, Attribute> blocker = new SortedNeighbourhoodBlocker<>(new SongBlockingKeyByAlbumGenerator(), 30);
		blocker.setMeasureBlockSizes(true);
		//Write debug results to file:
		blocker.collectBlockSizeData("data/output/debugResultsBlocking.csv", 100);
		
		// Initialize Matching Engine
		MatchingEngine<Song, Attribute> engine = new MatchingEngine<>();

		// Execute the matching
		logger.info("*\tRunning identity resolution\t*");
		Processable<Correspondence<Song, Attribute>> correspondences = engine.runIdentityResolution(
				// this order should match the column order in the golden standard
				dataMillion, dataOpenDB, null, matchingRule,
				blocker);

		// write the correspondences to the output file
		new CSVCorrespondenceFormatter().writeCSV(new File("data/output/million_opendb_correspondences.csv"), correspondences);		
		
		logger.info("*\tEvaluating result\t*");
		// evaluate your result
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
