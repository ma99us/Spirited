package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.api.QueryMetadata;
import org.maggus.spirit.models.FlavorProfile;
import org.maggus.spirit.models.Locators;
import org.maggus.spirit.models.Whisky;
import org.maggus.spirit.models.SpiritDiff;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

@Stateless
@Log
public class SuggestionsService {

    @Inject
    private CacheService cacheService;

    @Inject
    private SpiritCharacterParser spiritCharacterParser;

    public List<SpiritDiff> findSimilarSpirits(final Whisky whisky, int maxCandidates) throws Exception {
        try {
            log.info("Looking for similar spirits for \"" + whisky.getName() + "\"; maxCandidates=" + maxCandidates);
            long t0 = System.currentTimeMillis();
            final Set<SpiritCharacterParser.SpiritCharacters.Character> spiritCharacters = spiritCharacterParser.getSpiritCharacters(
                    whisky.getSpiritCharacter(), Locators.SpiritType.getType(whisky.getType()));
            List<Whisky> allWhisky = cacheService.getWhiskyService().getWhiskies(null, null, null, null, false, new QueryMetadata());
            //log.info("allWhisky size " + allWhisky.size());     // #DEBUG
            Map<Double, SpiritDiff> candidates = allWhisky.parallelStream()
                    .map(w -> {
                        SpiritDiff diff = new SpiritDiff(w);
                        if (whisky.getFlavorProfile() != null && w.getFlavorProfile() != null) {
                            diff.setStdDeviation(calcFlavorProfileDifference(whisky.getFlavorProfile(), w.getFlavorProfile(), diff));
                        } else if (spiritCharacters != null && !spiritCharacters.isEmpty()) {
                            diff.setStdDeviation(calcCharacterDifference(spiritCharacters, w.getSpiritCharacter(), diff));
                        } else {
                            diff.setStdDeviation(Double.MAX_VALUE);
                        }
                        return diff;
                    })
                    .filter(wd -> {
                        if(whisky.equals(wd.getCandidate())){
                            return false;
                        }
                        if(wd.getStdDeviation() == Double.MAX_VALUE){
                            return false;
                        }
                        return true;
                    })
                    .collect(Collectors.toMap(SpiritDiff::getStdDeviation, d -> d, (oldValue, newValue) -> oldValue, TreeMap::new));
            //log.info("candidates size " + candidates.size());     // #DEBUG
            List<SpiritDiff> res = new ArrayList<>(candidates.values());
            if (res.size() > maxCandidates) {
                res = res.subList(0, maxCandidates);
            }
            long dt = System.currentTimeMillis() - t0;
            log.info("Found " + res.size() + " similar spirits in " + (dt) + " ms");
            return res;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed to find similar spirit", ex);
            throw ex;
        }
    }

    private double calcFlavorProfileDifference(FlavorProfile originalFp, FlavorProfile candidateFp, SpiritDiff diff) {
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
        double res = Math.sqrt(totalDiff / 14.0);
        return res < 20.0 ? res : Double.MAX_VALUE;
    }

    private double difSq(Integer i1, Integer i2, SpiritDiff diff, String flavor) {
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

    private double calcCharacterDifference(Set<SpiritCharacterParser.SpiritCharacters.Character> originalChars, String candidateChars, SpiritDiff diff) {
        if(originalChars == null || originalChars.isEmpty() || candidateChars == null || candidateChars.isEmpty()){
            return Double.MAX_VALUE;
        }
        final Set<SpiritCharacterParser.SpiritCharacters.Character> candidateCharacters = spiritCharacterParser.getSpiritCharacters(
                candidateChars, Locators.SpiritType.getType(diff.getCandidate().getType()));
        if(candidateCharacters == null || candidateCharacters.isEmpty()){
            return Double.MAX_VALUE;
        }

        Set<SpiritCharacterParser.SpiritCharacters.Character> common = originalChars.stream().filter(candidateCharacters::contains).collect(Collectors.toSet());
        Set<SpiritCharacterParser.SpiritCharacters.Character> originalDiff = originalChars.stream().filter(item -> !candidateCharacters.contains(item)).collect(Collectors.toSet());
        Set<SpiritCharacterParser.SpiritCharacters.Character> candidateDiff = candidateCharacters.stream().filter(item -> !originalChars.contains(item)).collect(Collectors.toSet());

        double totalMatch = 0;
        int totalMatchNum = 0;
        // common
        for(SpiritCharacterParser.SpiritCharacters.Character character : common) {
            totalMatch += Math.pow(character.getWeight(), 2);
            totalMatchNum++;
        }
        totalMatch = totalMatchNum > 0 ? Math.sqrt(totalMatch / totalMatchNum) : 0;

        double totalDiff = 0;
        int totalDiffNum = 0;
        // original has, candidate does not
        for(SpiritCharacterParser.SpiritCharacters.Character character : originalDiff) {
            totalDiff += difSq(character.getWeight(), 0, diff, character.toString());
            totalDiffNum++;
        }
        // candidate has, original does not
        for(SpiritCharacterParser.SpiritCharacters.Character character : candidateDiff) {
            totalDiff += difSq(0, character.getWeight(), diff, character.toString());
            totalDiffNum++;
        }
        totalDiff = totalDiffNum > 0 ? Math.sqrt(totalDiff / totalDiffNum) : 0;

        return totalMatch > totalDiff ? -totalMatch + totalDiff : Double.MAX_VALUE;
    }
}
