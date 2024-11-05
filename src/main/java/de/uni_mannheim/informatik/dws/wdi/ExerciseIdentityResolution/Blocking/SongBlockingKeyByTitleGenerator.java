package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Blocking;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.RecordBlockingKeyGenerator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * {@link BlockingKeyGenerator} for {@link Song}s, which generates a blocking
 * key based on the track title.
 */
public class SongBlockingKeyByTitleGenerator extends
        RecordBlockingKeyGenerator<Song, Attribute> {

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see de.uni_mannheim.informatik.dws.winter.matching.blockers.generators.BlockingKeyGenerator#generateBlockingKeys(de.uni_mannheim.informatik.dws.winter.model.Matchable, de.uni_mannheim.informatik.dws.winter.processing.Processable, de.uni_mannheim.informatik.dws.winter.processing.DataIterator)
     */
    @Override
    public void generateBlockingKeys(Song record, Processable<Correspondence<Attribute, Matchable>> correspondences,
    DataIterator<Pair<String, Song>> resultCollector) {

// Check if the track title is null or empty
if (record.getTrack() == null || record.getTrack().isEmpty()) {
    return;
}

// Split the track title into words and preprocess it to improve key compatibility
String[] tokens = record.getTrack().toLowerCase().split(" ");

StringBuilder blockingKeyValue = new StringBuilder();

// Define stopwords
Set<String> stopwords = new HashSet<>(Arrays.asList("the", "a", "an", "of", "and"));

// Include first characters of each of the first three non-stopword words
for (String token : tokens) {
    if (!stopwords.contains(token) && blockingKeyValue.length() < 6) {
        blockingKeyValue.append(token.substring(0, Math.min(2, token.length())).toUpperCase());
    }
}

// Collect the blocking key and the record
resultCollector.next(new Pair<>(blockingKeyValue.toString(), record));
}

}
