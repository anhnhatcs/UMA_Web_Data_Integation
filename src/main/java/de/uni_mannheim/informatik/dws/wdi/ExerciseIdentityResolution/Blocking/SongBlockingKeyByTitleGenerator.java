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

        String[] tokens = record.getTrack().split(" ");
        
        String blockingKeyValue = "";

        for(int i = 0; i <= 2 && i < tokens.length; i++) {
            blockingKeyValue += tokens[i].substring(0, Math.min(2, tokens[i].length())).toUpperCase();
        }

        resultCollector.next(new Pair<>(blockingKeyValue, record));
    }

}
