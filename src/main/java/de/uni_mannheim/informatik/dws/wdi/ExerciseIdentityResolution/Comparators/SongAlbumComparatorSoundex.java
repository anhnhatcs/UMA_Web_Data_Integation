package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators;

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;

/**
 * {@link Comparator} for {@link Song}s based on the {@link Song#getTrack()}
 * value and their Soundex similarity value.
 * 
 * This comparator uses Soundex similarity to compare the titles of music tracks.
 */
public class SongAlbumComparatorSoundex implements Comparator<Song, Attribute> {

    private static final long serialVersionUID = 1L;
    private SoundexSimilarity sim = new SoundexSimilarity();
    
    private ComparatorLogger comparisonLog;

    @Override
    public double compare(
            Song record1,
            Song record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
        
        String album1 = record1.getAlbum();
        String album2 = record2.getAlbum();

        // Calculate similarity using SoundexSimilarity
        double similarity = sim.calculate(album1, album2);

        // Log comparison details if a logger is provided
        if(this.comparisonLog != null){
            this.comparisonLog.setComparatorName(getClass().getName());
            this.comparisonLog.setRecord1Value(album1);
            this.comparisonLog.setRecord2Value(album2);
            this.comparisonLog.setSimilarity(Double.toString(similarity));
        }
        
        return similarity;
    }

    @Override
    public ComparatorLogger getComparisonLog() {
        return this.comparisonLog;
    }

    @Override
    public void setComparisonLog(ComparatorLogger comparatorLog) {
        this.comparisonLog = comparatorLog;
    }

    /**
     * SoundexSimilarity calculates phonetic similarity between two strings using the Soundex algorithm.
     * Returns 1.0 if Soundex codes are identical, otherwise returns 0.0.
     */
    private class SoundexSimilarity {

        public double calculate(String first, String second) {
            if (first == null || second == null) {
                return 0.0;
            }
            
            // Generate Soundex codes for both strings
            String soundex1 = getSoundexCode(first);
            String soundex2 = getSoundexCode(second);
            
            // Check if Soundex codes are identical
            return soundex1.equals(soundex2) ? 1.0 : 0.0;
        }

        private String getSoundexCode(String str) {
            str = str.toUpperCase();
            
            // Step 1: Save the first letter. Remove non-letters.
            char firstLetter = str.charAt(0);
            str = str.replaceAll("[^A-Z]", "");

            // Step 2: Replace letters with Soundex numbers
            StringBuilder soundex = new StringBuilder();
            soundex.append(firstLetter);

            for (int i = 1; i < str.length(); i++) {
                char code = getSoundexDigit(str.charAt(i));
                // Avoid duplicates
                if (code != '0' && code != soundex.charAt(soundex.length() - 1)) {
                    soundex.append(code);
                }
            }

            // Step 3: Pad or trim to ensure a length of 4
            soundex.setLength(Math.min(4, soundex.length()));
            while (soundex.length() < 4) {
                soundex.append('0');
            }

            return soundex.toString();
        }

        private char getSoundexDigit(char c) {
            switch (c) {
                case 'B': case 'F': case 'P': case 'V': return '1';
                case 'C': case 'G': case 'J': case 'K': case 'Q': case 'S': case 'X': case 'Z': return '2';
                case 'D': case 'T': return '3';
                case 'L': return '4';
                case 'M': case 'N': return '5';
                case 'R': return '6';
                default: return '0';
            }
        }
    }
}
