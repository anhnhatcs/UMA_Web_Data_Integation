package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Node;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleFactory;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.model.io.XMLMatchableReader;

/**
 * A {@link XMLMatchableReader} for {@link Song}s.
 */
public class SongXMLReader extends XMLMatchableReader<Song, Attribute> implements
FusibleFactory<Song, Attribute> {

    @Override
    protected void initialiseDataset(DataSet<Song, Attribute> dataset) {
        super.initialiseDataset(dataset);

        // // Define the schema in the Song class manually, as it is not inferred from the file
        dataset.addAttribute(Song.ARTIST);
        dataset.addAttribute(Song.TRACK);
        dataset.addAttribute(Song.TRACK_EXPLICITNESS);
        dataset.addAttribute(Song.ALBUM_YEAR);
        dataset.addAttribute(Song.ALBUM);
        dataset.addAttribute(Song.DURATION);
    }
    
    @Override
    public Song createModelFromElement(Node node, String provenanceInfo) {
        String id = getValueFromChildElement(node, "id");

        // Create the Song object with ID and provenance information
        Song song = new Song(id, provenanceInfo);

        // Fill in the attributes from XML elements
        song.setArtist(getValueFromChildElement(node, "Artist"));
        song.setTrack(getValueFromChildElement(node, "Track"));
        song.setTrackExplicitness(getValueFromChildElement(node, "Track_Explicitness"));
        song.setAlbum(getValueFromChildElement(node, "Album"));

        // Parse the album year and duration as integers
        try {
            String albumYearStr = getValueFromChildElement(node, "Album_Year");
            if (albumYearStr != null && !albumYearStr.isEmpty()) {
                song.setAlbumYear(Integer.parseInt(albumYearStr));
            }

            String durationStr = getValueFromChildElement(node, "Duration");
            if (durationStr != null && !durationStr.isEmpty()) {
                song.setDuration(Integer.parseInt(durationStr));
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        return song;
    }

    @Override
	public Song createInstanceForFusion(RecordGroup<Song, Attribute> cluster) {
	
	List<String> ids = new LinkedList<>();
	
	for (Song m : cluster.getRecords()) {
		ids.add(m.getIdentifier());
	}
	
	Collections.sort(ids);
	
	String mergedId = StringUtils.join(ids, '+');
	
	return new Song(mergedId, "fused");
	}
}
