package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators;

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;

/**
 * Implements the Jaro-Winkler similarity measure for comparing strings
 * {@link Comparator} for {@link Song}s based on {@link Song#getArtist()} values.
 * Created by Shamalan Rajesvaran
 * @version 1.1
 */
public class SongArtistComparatorJaroWinkler implements Comparator<Song, Attribute> {

    private static final long serialVersionUID = 1L;
    private JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();
    private ComparatorLogger comparisonLog;

    @Override
    public double compare(
            Song record1,
            Song record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {

        String artist1 = record1.getArtist();
        String artist2 = record2.getArtist();

        // Calculate similarity using Jaro-Winkler similarity
        double similarityScore = similarity.calculate(artist1, artist2);

        if(this.comparisonLog != null){
            this.comparisonLog.setComparatorName(getClass().getName());
            this.comparisonLog.setRecord1Value(artist1);
            this.comparisonLog.setRecord2Value(artist2);
            this.comparisonLog.setSimilarity(Double.toString(similarityScore));
        }

        return similarityScore;
    }

    @Override
    public ComparatorLogger getComparisonLog() {
        return this.comparisonLog;
    }

    @Override
    public void setComparisonLog(ComparatorLogger comparatorLog) {
        this.comparisonLog = comparatorLog;
    }

    private class JaroWinklerSimilarity {
        private static final double SCALING_FACTOR = 0.1;

        public double calculate(String first, String second) {
            if (first == null || second == null) {
                return 0.0;
            }

            int firstLength = first.length();
            int secondLength = second.length();

            if (firstLength == 0 && secondLength == 0) {
                return 1.0;
            }

            int matchDistance = Math.max(firstLength, secondLength) / 2 - 1;
            matchDistance = Math.max(0, matchDistance);

            boolean[] firstMatches = new boolean[firstLength];
            boolean[] secondMatches = new boolean[secondLength];

            int matches = 0;
            for (int i = 0; i < firstLength; i++) {
                int start = Math.max(0, i - matchDistance);
                int end = Math.min(i + matchDistance + 1, secondLength);

                for (int j = start; j < end; j++) {
                    if (secondMatches[j]) continue;
                    if (first.charAt(i) != second.charAt(j)) continue;
                    firstMatches[i] = true;
                    secondMatches[j] = true;
                    matches++;
                    break;
                }
            }

            if (matches == 0) {
                return 0.0;
            }

            int transpositions = 0;
            int k = 0;
            for (int i = 0; i < firstLength; i++) {
                if (!firstMatches[i]) continue;
                while (!secondMatches[k]) k++;
                if (first.charAt(i) != second.charAt(k)) {
                    transpositions++;
                }
                k++;
            }

            double jaroScore = ((matches / (double) firstLength) +
                    (matches / (double) secondLength) +
                    ((matches - transpositions / 2.0) / matches)) / 3.0;

            int prefixLength = 0;
            for (int i = 0; i < Math.min(4, Math.min(firstLength, secondLength)); i++) {
                if (first.charAt(i) == second.charAt(i)) {
                    prefixLength++;
                } else {
                    break;
                }
            }

            return jaroScore + (prefixLength * SCALING_FACTOR * (1 - jaroScore));
        }
    }
}
