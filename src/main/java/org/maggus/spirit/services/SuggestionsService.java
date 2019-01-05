package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.api.QueryMetadata;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.WhiskyDiff;

import javax.ejb.Stateful;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Stateful
@Log
public class SuggestionsService {

    @Inject
    private CacheService cacheService;

    public List<WhiskyDiff> findSimilarWhiskies(final Whisky whisky, Double maxDeviation) throws Exception {
        try {
            log.info("Looking for similar whiskies for \"" + whisky.getName() + "\"; maxDeviation=" + maxDeviation);
            long t0 = System.currentTimeMillis();
            List<Whisky> allWhisky = cacheService.getWhiskyService().getAllWhiskies(new QueryMetadata());
            Map<Double, WhiskyDiff> candidates = allWhisky.parallelStream().map(w -> {
                WhiskyDiff diff = new WhiskyDiff(w);
                diff.setStdDeviation(calcDifference(whisky.getFlavorProfile(), w.getFlavorProfile(), diff));
                return diff;
            }).collect(Collectors.toMap(WhiskyDiff::getStdDeviation, d -> d, (oldValue, newValue) -> oldValue, TreeMap::new));
            List<WhiskyDiff> res = new ArrayList<>();
            for (WhiskyDiff wd : candidates.values()) {
                if (whisky.equals(wd.getCandidate()) || wd.getStdDeviation() >= (Double.MAX_VALUE - 1.0)) {
                    continue;   // no not suggest the original whisky, and the worst matches
                }
                if (maxDeviation != null && wd.getStdDeviation() > maxDeviation) {
                    break;
                }
                res.add(wd);
                //log.info("candidate " + wd.getCandidate().getName() + "; deviation=" + wd.getStdDeviation());     // #DEBUG
            }
            long dt = System.currentTimeMillis() - t0;
            log.info("Found " + res.size() + " similar whiskies in " + (dt) + " ms");
            return res;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to find similar whisky", ex);
            throw ex;
        }
    }

    private double calcDifference(FlavorProfile originalFp, FlavorProfile candidateFp, WhiskyDiff diff) {
        if(originalFp == null || candidateFp == null){
            return Double.MAX_VALUE;
        }
        double totalDiff = 0;
        totalDiff += difSq(originalFp.getSmoky(), candidateFp.getSmoky(), diff, "smoky");
        totalDiff += difSq(originalFp.getPeaty(), candidateFp.getPeaty(), diff, "peaty");
        totalDiff += difSq(originalFp.getSpicy(), candidateFp.getSpicy(), diff, "spicy");
        totalDiff += difSq(originalFp.getHerbal(), candidateFp.getHerbal(), diff, "herbal");
        totalDiff += difSq(originalFp.getOily(), candidateFp.getOily(), diff, "oily");
        totalDiff += difSq(originalFp.getFull_bodied(), candidateFp.getFull_bodied(), diff, "full bodied");
        totalDiff += difSq(originalFp.getRich(), candidateFp.getRich(), diff, "rich");
        totalDiff += difSq(originalFp.getSweet(), candidateFp.getSweet(), diff, "sweet");
        totalDiff += difSq(originalFp.getBriny(), candidateFp.getBriny(), diff, "briny");
        totalDiff += difSq(originalFp.getSalty(), candidateFp.getSalty(), diff, "salty");
        totalDiff += difSq(originalFp.getVanilla(), candidateFp.getVanilla(), diff, "vanilla");
        totalDiff += difSq(originalFp.getTart(), candidateFp.getTart(), diff, "tart");
        totalDiff += difSq(originalFp.getFruity(), candidateFp.getFruity(), diff, "fruity");
        totalDiff += difSq(originalFp.getFloral(), candidateFp.getFloral(), diff, "floral");
        return Math.sqrt(totalDiff / 14.0);
    }

    private double difSq(Integer i1, Integer i2, WhiskyDiff diff, String flavor) {
        double l1 = i1 != null ? (double) i1 : 0;
        double l2 = i2 != null ? (double) i2 : 0;
        double d = l1 - l2;
        if (Math.abs(d) > Math.abs(diff.getMaxDiffAmount())) {
            // new biggest difference
            diff.setMaxDiffAmount(d);
            diff.setMaxDiffFlavor((d > 0 ? "Less" : "More") + " " + flavor);
        }
        return Math.pow(d, 2);
    }
}
