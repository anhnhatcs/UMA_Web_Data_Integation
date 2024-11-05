package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import java.io.Serializable;
import de.uni_mannheim.informatik.dws.winter.model.AbstractRecord;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;

/**
 * A {@link AbstractRecord} which represents a song.
 * 
 */
public class Song extends AbstractRecord<Attribute> implements Serializable {


    public Song(String identifier, String provenance) {
        super(identifier, provenance);
    }
    private static final long serialVersionUID = 1L;
    private String id;
    private String artist;
    private String track;
    private String trackExplicitness;
    private int albumYear;
    private int duration;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTrack() {
        return track;
    }

    public void setTrack(String track) {
        this.track = track;
    }

    public String getTrackExplicitness() {
        return trackExplicitness;
    }

    public void setTrackExplicitness(String trackExplicitness) {
        this.trackExplicitness = trackExplicitness;
    }

    public int getAlbumYear() {
        return albumYear;
    }

    public void setAlbumYear(int albumYear) {
        this.albumYear = albumYear;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static final Attribute ARTIST = new Attribute("Artist");
    public static final Attribute TRACK = new Attribute("Track");
    public static final Attribute TRACK_EXPLICITNESS = new Attribute("Track_Explicitness");
    public static final Attribute ALBUM_YEAR = new Attribute("Album_Year");
    public static final Attribute DURATION = new Attribute("Duration");

    @Override
    public boolean hasValue(Attribute attribute) {
        if(attribute == ARTIST)
            return artist != null;
        else if(attribute == TRACK) 
            return track != null;
        else if(attribute == TRACK_EXPLICITNESS)
            return trackExplicitness != null;
        else if(attribute == ALBUM_YEAR)
            return albumYear != 0;
        else if(attribute == DURATION)
            return duration != 0;
        return false;
    }

    @Override
	public String toString() {
		return String.format("[Song %s: %s / %s / %s]", getIdentifier(), getTrack(),
        getArtist(), getAlbumYear());
	}

	@Override
	public int hashCode() {
		return getIdentifier().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Song){
			return this.getIdentifier().equals(((Song) obj).getIdentifier());
		}else
			return false;
	}
}