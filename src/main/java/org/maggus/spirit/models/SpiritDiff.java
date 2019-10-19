package org.maggus.spirit.models;

import lombok.Data;

import java.util.Objects;

@Data
public class SpiritDiff implements Comparable<SpiritDiff> {
    private Double stdDeviation;
    private double maxDiffAmount;
    private String maxDiffFlavor;
    private final Whisky candidate;

    public SpiritDiff(Whisky candidate){
        this.candidate = candidate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpiritDiff that = (SpiritDiff) o;
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
    public int compareTo(SpiritDiff o) {
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
