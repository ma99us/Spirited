package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
public class WhiskyDiff implements Comparable<WhiskyDiff> {
    private Double stdDeviation;
    private double maxDiffAmount;
    private String maxDiffFlavor;
    private final Whisky candidate;

    public WhiskyDiff(Whisky candidate){
        this.candidate = candidate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WhiskyDiff that = (WhiskyDiff) o;
        return Objects.equals(getStdDeviation(), that.getStdDeviation()) &&
                Objects.equals(getCandidate(), that.getCandidate());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getStdDeviation(), getCandidate());
    }

    @Override
    public String toString() {
        return "{" +
                stdDeviation +
                " - " + candidate.getName() +
                '}';
    }

    @Override
    public int compareTo(WhiskyDiff o) {
        if(getStdDeviation() != null && o.getStdDeviation() == null) {
            return -1;
        }
        else if(getStdDeviation() == null && o.getStdDeviation() != null) {
            return 1;
        }
        else{
            return Double.compare(getStdDeviation(), o.getStdDeviation());
        }
    }
}
