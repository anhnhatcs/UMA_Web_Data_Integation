package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.fusers;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.winter.datafusion.AttributeValueFuser;
import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.string.LongestString;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;

/**
 * {@link AttributeValueFuser} for the titles of {@link Song}s.
 */
public class AlbumFuserLongestString extends
		AttributeValueFuser<String, Song, Attribute> {

	public AlbumFuserLongestString() {
		super(new LongestString<Song, Attribute>());
	}

	@Override
	public void fuse(RecordGroup<Song, Attribute> group, Song fusedRecord, Processable<Correspondence<Attribute, Matchable>> schemaCorrespondences, Attribute schemaElement) {

		// get the fused value
		FusedValue<String, Song, Attribute> fused = getFusedValue(group, schemaCorrespondences, schemaElement);

		// set the value for the fused record
		fusedRecord.setAlbum(fused.getValue());

		// add provenance info
		fusedRecord.setAttributeProvenance(Song.ALBUM, fused.getOriginalIds());
	}

	@Override
	public boolean hasValue(Song record, Correspondence<Attribute, Matchable> correspondence) {
		return record.hasValue(Song.ALBUM);
	}

	@Override
	public String getValue(Song record, Correspondence<Attribute, Matchable> correspondence) {
		return record.getAlbum();
	}

}

