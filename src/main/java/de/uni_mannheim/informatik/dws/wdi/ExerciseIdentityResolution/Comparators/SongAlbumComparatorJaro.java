package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators;

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
/**
 * Implements the Jaro similarity measure for string comparison
 *  * {@link Comparator} for {@link Song}s based on the {@link Song#getAlbum()}
 * @author Anh-Nhat Nguyen
 * @version 1.1
 * 
 */
public class SongAlbumComparatorJaro implements Comparator<Song, Attribute> {

    private static final long serialVersionUID = 1L;
    private JaroSimilarity sim = new JaroSimilarity();
    private ComparatorLogger comparisonLog;

    @Override
    public double compare(
            Song record1,
            Song record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {

        String artist1 = record1.getAlbum();
        String artist2 = record2.getAlbum();

        // Calculate similarity using JaroSimilarity
        double similarity = sim.calculate(artist1, artist2);

        if(this.comparisonLog != null){
            this.comparisonLog.setComparatorName(getClass().getName());
            this.comparisonLog.setRecord1Value(artist1);
            this.comparisonLog.setRecord2Value(artist2);
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

    private class JaroSimilarity {
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

            int matchCount = 0;
            for (int i = 0; i < firstLength; i++) {
                int start = Math.max(0, i - matchDistance);
                int end = Math.min(i + matchDistance + 1, secondLength);

                for (int j = start; j < end; j++) {
                    if (secondMatches[j]) continue;
                    if (first.charAt(i) != second.charAt(j)) continue;
                    firstMatches[i] = true;
                    secondMatches[j] = true;
                    matchCount++;
                    break;
                }
            }

            if (matchCount == 0) {
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

            double m = matchCount;
            return ((m / firstLength) +
                    (m / secondLength) +
                    ((m - transpositions/2.0) / m)) / 3.0;
        }
    }
}