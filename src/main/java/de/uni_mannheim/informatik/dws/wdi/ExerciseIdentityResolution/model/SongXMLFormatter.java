package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.uni_mannheim.informatik.dws.winter.model.io.XMLFormatter;

/**
 * {@link XMLFormatter} for {@link Song}s.
 * 
 */
public class SongXMLFormatter extends XMLFormatter<Song> {

    @Override
    public Element createRootElement(Document doc) {
        return doc.createElement("songs");
    }

    @Override
    public Element createElementFromRecord(Song record, Document doc) {
        Element song = doc.createElement("song");

        song.appendChild(createTextElement("id", record.getId(), doc));
        song.appendChild(createTextElement("artist", record.getArtist(), doc));
        song.appendChild(createTextElement("track", record.getTrack(), doc));
        song.appendChild(createTextElement("trackExplicitness", record.getTrackExplicitness(), doc));
        song.appendChild(createTextElement("albumYear", Integer.toString(record.getAlbumYear()), doc));
        song.appendChild(createTextElement("duration", Integer.toString(record.getDuration()), doc));

        return song;
    }

    protected Element createTextElementWithProvenance(String name, String value, String provenance, Document doc) {
        Element elem = createTextElement(name, value, doc);
        elem.setAttribute("provenance", provenance);
        return elem;
    }
}
