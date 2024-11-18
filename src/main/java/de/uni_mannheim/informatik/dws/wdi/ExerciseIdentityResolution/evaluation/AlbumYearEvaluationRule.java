package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.evaluation;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.winter.datafusion.EvaluationRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

/**
 * {@link EvaluationRule} for the date of {@link Song}s. The rule simply
 * compares the year of the dates of two {@link Song}s.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 */
public class AlbumYearEvaluationRule extends EvaluationRule<Song, Attribute> {

	@Override
	public boolean isEqual(Song record1, Song record2, Attribute schemaElement) {

		if(record1.getAlbumYear() != 0 && record2.getAlbumYear() != 0){
			double lowerBound = record1.getAlbumYear() - 1;
			double upperBound = record1.getAlbumYear() + 1;

			return lowerBound <= record2.getAlbumYear() && record2.getAlbumYear() <= upperBound;
		}

		return false;
	}

	@Override
	public boolean isEqual(Song record1, Song record2,
			Correspondence<Attribute, Matchable> schemaCorrespondence) {
		return isEqual(record1, record2, (Attribute)null);
	}
	
}
