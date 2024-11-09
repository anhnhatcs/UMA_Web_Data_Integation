package de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.Comparators;

import de.uni_mannheim.informatik.dws.wdi.ExerciseIdentityResolution.model.Song;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.Comparator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.comparators.ComparatorLogger;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.defaultmodel.Attribute;
import de.uni_mannheim.informatik.dws.winter.similarity.numeric.AbsoluteDifferenceSimilarity;

public class SongDurationComparator2Seconds implements Comparator<Song, Attribute> {
    private static final long serialVersionUID = 1L;
    private ComparatorLogger comparisonLog;
    private AbsoluteDifferenceSimilarity sim = new AbsoluteDifferenceSimilarity(2000); //compare durations with max 2 seconds diff

    @Override
    public double compare(
            Song record1,
            Song record2,
            Correspondence<Attribute, Matchable> schemaCorrespondences) {

        int s1 = record1.getDuration();
        int s2 = record2.getDuration();

        double similarity = sim.calculate((double) s1, (double) s2);

        if(this.comparisonLog != null){
            this.comparisonLog.setComparatorName(getClass().getName());
            this.comparisonLog.setRecord1Value(Integer.toString(s1));
            this.comparisonLog.setRecord2Value(Integer.toString(s2));
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

