package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.fusers;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.meta.MostRecent;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * {@link AttributeValueFuser} for the titles of {@link Song}s.
 */
public class DurationFuserMostRecent extends AttributeValueFuser<Integer, Song, Attribute> {

	public DurationFuserMostRecent() {
		super(new MostRecent<Integer, Song, Attribute>());
	}

	@Override
	public boolean hasValue(Song record, Correspondence<Attribute, Matchable> correspondence) {
		return record.hasValue(Song.DURATION);
	}

	@Override
	public Integer getValue(Song record, Correspondence<Attribute, Matchable> correspondence) {
		return record.getDuration();
	}

	@Override
	public void fuse(RecordGroup<Song, Attribute> group, Song fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute schemaElement) {
		FusedValue<Integer, Song, Attribute> fused = getFusedValue(group, schemaCorrespondences, schemaElement);
		fusedRecord.setDuration(fused.getValue());
		fusedRecord.setAttributeProvenance(Song.DURATION, fused.getOriginalIds());
	}

}