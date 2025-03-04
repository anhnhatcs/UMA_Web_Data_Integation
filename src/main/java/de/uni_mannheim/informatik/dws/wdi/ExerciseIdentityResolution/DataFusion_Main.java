package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.evaluation.*;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.fusers.*;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.SongXMLFormatter;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.SongXMLReader;
import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEngine;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionEvaluator;
import de.uni_mannheim.informatik.dws.winter.datafusion.DataFusionStrategy;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleHashedDataSet;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroupFactory;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.utils.WinterLogManager;
import org.slf4j.Logger;

public class DataFusion_Main 
{
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
		// Load the Data into FusibleDataSet
		logger.info("*\tLoading datasets\t*");
		FusibleDataSet<Song, Attribute> ds1 = new FusibleHashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/input/apple.xml"), "/songs/song", ds1);
		ds1.printDataSetDensityReport();

		FusibleDataSet<Song, Attribute> ds2 = new FusibleHashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/input/opendb.xml"), "/songs/song", ds2);
		ds2.printDataSetDensityReport();

		FusibleDataSet<Song, Attribute> ds3 = new FusibleHashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/input/million.xml"), "/songs/song", ds3);
		ds3.printDataSetDensityReport();

		// Maintain Provenance
		// Scores (e.g. from rating)
		ds1.setScore(3.0);
		ds2.setScore(1.0);
		ds3.setScore(2.0);

		// Date (e.g. last update)
		DateTimeFormatter formatter = new DateTimeFormatterBuilder()
		        .appendPattern("yyyy-MM-dd")
		        .parseDefaulting(ChronoField.CLOCK_HOUR_OF_DAY, 0)
		        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
		        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
		        .toFormatter(Locale.ENGLISH);
		
		ds1.setDate(LocalDateTime.parse("2024-01-01", formatter));
		ds2.setDate(LocalDateTime.parse("2024-09-01", formatter));
		ds3.setDate(LocalDateTime.parse("2023-01-01", formatter));

		// load correspondences
		logger.info("*\tLoading correspondences\t*");
		CorrespondenceSet<Song, Attribute> correspondences = new CorrespondenceSet<>();
		correspondences.loadCorrespondences(new File("data/correspondences/apple_opendb_correspondences.csv"),ds1, ds2);
		correspondences.loadCorrespondences(new File("data/correspondences/million_opendb_correspondences.csv"),ds3, ds2);

		// // write group size distribution
		correspondences.printGroupSizeDistribution();

		// // load the gold standard
		logger.info("*\tEvaluating results\t*");
		DataSet<Song, Attribute> gs = new FusibleHashedDataSet<>();
		new SongXMLReader().loadFromXML(new File("data/goldstandard/gold.xml"), "/songs/song", gs);

		for(Song m : gs.get()) {
			logger.info(String.format("gs: %s", m.getIdentifier()));
		}

		// define the fusion strategy
		DataFusionStrategy<Song, Attribute> strategy = new DataFusionStrategy<>(new SongXMLReader());
		// write debug results to file
		strategy.activateDebugReport("data/output/debugResultsDatafusion.csv", -1, gs);
		
		// add attribute fusers
		strategy.addAttributeFuser(Song.ALBUM, new AlbumFuserLongestString(),new TitleEvaluationRule());
		strategy.addAttributeFuser(Song.ALBUM_YEAR,new AlbumYearFuserVoting(), new AlbumYearEvaluationRule());
		strategy.addAttributeFuser(Song.ARTIST, new ArtistFuserLongestString(),new ArtistEvaluationRule());
		strategy.addAttributeFuser(Song.DURATION,new DurationFuserFavourSource(),new DurationEvaluationRule());
		strategy.addAttributeFuser(Song.TRACK_EXPLICITNESS,new ExplicitnessFavourSource(),new ExplicitnessEvaluationRule());
		strategy.addAttributeFuser(Song.TRACK,new TitleFuserLongestString(),new TitleEvaluationRule());

		// create the fusion engine
		DataFusionEngine<Song, Attribute> engine = new DataFusionEngine<>(strategy);

		// print consistency report
		engine.printClusterConsistencyReport(correspondences, null);
		
		// print record groups sorted by consistency
		engine.writeRecordGroupsByConsistency(new File("data/output/recordGroupConsistencies.csv"), correspondences, null);

		// run the fusion
		logger.info("*\tRunning data fusion\t*");
		FusibleDataSet<Song, Attribute> fusedDataSet = engine.run(correspondences, null);

		// write the result
		new SongXMLFormatter().writeXML(new File("data/output/fused.xml"), fusedDataSet);

		// evaluate
		DataFusionEvaluator<Song, Attribute> evaluator = new DataFusionEvaluator<>(strategy, new RecordGroupFactory<Song, Attribute>());
		
		double accuracy = evaluator.evaluate(fusedDataSet, gs, null);

		logger.info(String.format("*\tAccuracy: %.2f", accuracy));
    }
}
