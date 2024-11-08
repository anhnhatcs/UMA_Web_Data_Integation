package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators;

import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;

/**
 * {@link Comparator} for {@link Song}s based on the {@link Song#getTrack()}
 * value and their {@link LevenshteinSimilarity} value.
 */

//  follow this

public class SongTitleComparatorLevenshtein implements Comparator<Song, Attribute> {

    private static final long serialVersionUID = 1L;
    private LevenshteinSimilarity sim = new LevenshteinSimilarity();
    
    private ComparatorLogger comparisonLog;

    @Override
    public double compare(
            Song record1,
            Song record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {
        
        String s1 = record1.getTrack();
        String s2 = record2.getTrack();
        
        double similarity = sim.calculate(s1, s2);
        
        if(this.comparisonLog != null){
            this.comparisonLog.setComparatorName(getClass().getName());
        
            this.comparisonLog.setRecord1Value(s1);
            this.comparisonLog.setRecord2Value(s2);
        
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
}
