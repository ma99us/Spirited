package org.maggus.spirit.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WhiskyDiff {
    private double stdDeviation;
    private double maxDiffAmount;
    private String maxDiffFlavor;
    private Whisky candidate;

    public WhiskyDiff(Whisky candidate){
        this.candidate = candidate;
    }
}
