package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import java.util.List;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.CSVDataSetFormatter;


public class SongCSVFormatter extends CSVDataSetFormatter<Song, Attribute> {

    @Override
    public String[] getHeader(List<Attribute> orderedHeader) {
        return new String[] { "id", "artist", "track", "trackExplicitness", "albumYear", "duration" };
    }

    @Override
    public String[] format(Song record, DataSet<Song, Attribute> dataset, List<Attribute> orderedHeader) {
        return new String[] {
                record.getIdentifier(),
                record.getArtist(),
                record.getTrack(),
                record.getTrackExplicitness(),
                Integer.toString(record.getAlbumYear()),
                Integer.toString(record.getDuration())
        };
    }
}
