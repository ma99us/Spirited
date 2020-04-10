package org.maggus.spirit.services;

import lombok.extern.java.Log;
import org.maggus.spirit.models.Locators;

import javax.ejb.Stateless;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
@Log
public class SpiritCharacterParser {
    public static class SpiritCharacters {
        public static class Character {
            private final String[] synonyms;
            private final int weight;

            Character(int weight, String... synonyms) {
                this.weight = weight;
                this.synonyms = synonyms;
            }

            public int getWeight() {
                return weight;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Character character = (Character) o;
                return toString().equalsIgnoreCase(character.toString());
            }

            @Override
            public int hashCode() {
                return Objects.hash(toString());
            }

            public String toString() {
                return synonyms[0];
            }
        }

        protected Set<Character> characters = new HashSet<Character>();

        public Character parse(String str) {
            for (Character value : characters) {
                if (Arrays.stream(value.synonyms).anyMatch(t -> t.equalsIgnoreCase(str))) {
                    return value;
                }
            }
            return null;
        }

        public boolean hasCharacter(String characters, String tryCharacter) {
            Character chr = parse(tryCharacter);
            if (chr == null) {
                return false;
            } else {
                return hasCharacter(characters, chr);
            }
        }

        public boolean hasCharacter(String characters, Character tryCharacter) {
            final String s = characters.toUpperCase();
            for (String t : tryCharacter.synonyms) {
                if (s.contains(t.toUpperCase())) {
                    return true;
                }
            }
            return false;
        }

    }

    private SpiritCharacters beerCharacters = new SpiritCharacters() {
        {
            // types
            characters.add(new SpiritCharacters.Character(1, "Dark", "Porter", "Stout"));
            characters.add(new SpiritCharacters.Character(1, "IPA"));
            characters.add(new SpiritCharacters.Character(1, "Lager"));
            characters.add(new SpiritCharacters.Character(1, "Ale"));
            characters.add(new SpiritCharacters.Character(1, "Pilsner"));
            // notes
            characters.add(new SpiritCharacters.Character(1, "Light", "Clear", "Yellow"));
            characters.add(new SpiritCharacters.Character(1, "Gold", "Golden", "Amber", "Copper"));
            characters.add(new SpiritCharacters.Character(1, "Blood"));
            characters.add(new SpiritCharacters.Character(1, "Thick"));
            characters.add(new SpiritCharacters.Character(1, "Blond", "Blonde"));
            characters.add(new SpiritCharacters.Character(1, "Strong", "Firm", "Bold"));
            characters.add(new SpiritCharacters.Character(1, "Red"));
            characters.add(new SpiritCharacters.Character(1, "Brown"));
            characters.add(new SpiritCharacters.Character(1, "Ambroise"));
            characters.add(new SpiritCharacters.Character(1, "Barrel"));
            characters.add(new SpiritCharacters.Character(1, "Dry"));
            characters.add(new SpiritCharacters.Character(1, "Winter"));
            characters.add(new SpiritCharacters.Character(1, "Spring"));
            characters.add(new SpiritCharacters.Character(1, "Summer"));
            characters.add(new SpiritCharacters.Character(1, "Autumn", "Fall"));
            characters.add(new SpiritCharacters.Character(1, "Oktoberfest"));
            characters.add(new SpiritCharacters.Character(1, "Christmas"));
            characters.add(new SpiritCharacters.Character(1, "Mountain", "Glasiar"));
            characters.add(new SpiritCharacters.Character(1, "Cold-filtration", "Cold-filtered"));
            characters.add(new SpiritCharacters.Character(1, "Experimental", "Experiment", "Funky"));
            characters.add(new SpiritCharacters.Character(1, "Refreshing", "Fresh", "Refreshment"));
            characters.add(new SpiritCharacters.Character(1, "Crispy", "Crisp", "Sharp", "Bright"));
            characters.add(new SpiritCharacters.Character(1, "Smooth", "Smoothness", "Drinkability", "Drinkable"));
            characters.add(new SpiritCharacters.Character(1, "Warm", "Warmth"));
            characters.add(new SpiritCharacters.Character(1, "Rich", "Complex", "Full-bodied", "Robust", "Rounded", "Well-balanced", "Full-flavoured"));
            characters.add(new SpiritCharacters.Character(2, "Hops", "Hoppy", "Hop", "Hopped"));
            characters.add(new SpiritCharacters.Character(2, "Malt", "Malty", "Malted", "Malts", "Maltiness"));
            characters.add(new SpiritCharacters.Character(2, "Tropical", "Exotic"));
            characters.add(new SpiritCharacters.Character(2, "Barley"));
            characters.add(new SpiritCharacters.Character(2, "Yeast", "Yeasty"));
            // origin
            characters.add(new SpiritCharacters.Character(3, "Belgian"));
            characters.add(new SpiritCharacters.Character(3, "Bavarian", "Bavaria", "Munich"));
            characters.add(new SpiritCharacters.Character(3, "German"));
            characters.add(new SpiritCharacters.Character(3, "Czech", "Bohemian"));
            characters.add(new SpiritCharacters.Character(3, "Amsterdam", "Dutch"));
            characters.add(new SpiritCharacters.Character(3, "England", "English", "British"));
            characters.add(new SpiritCharacters.Character(3, "Canada", "Canadian"));
            characters.add(new SpiritCharacters.Character(3, "Irish"));
            characters.add(new SpiritCharacters.Character(3, "Europe", "European"));
            // flavours
            characters.add(new SpiritCharacters.Character(5, "Bourbon"));
            characters.add(new SpiritCharacters.Character(5, "Whisky", "Whiskey"));
            characters.add(new SpiritCharacters.Character(5, "Wine"));
            characters.add(new SpiritCharacters.Character(5, "Chardonnay"));
            characters.add(new SpiritCharacters.Character(5, "Amarillo"));
            characters.add(new SpiritCharacters.Character(5, "Berries", "Berry"));
            characters.add(new SpiritCharacters.Character(5, "Fruit", "Fruitiness", "Fruits", "Fruity", "Tropical", "Juicy", "Juice", "Juices"));
            characters.add(new SpiritCharacters.Character(5, "Citrus", "Citrusy", "Lemon", "Lemony", "Lime", "Orange", "Grapefruit", "Tangerine", "Zesty", "Tangy"));
            characters.add(new SpiritCharacters.Character(5, "Toasted", "Toasty", "Toast", "Roasted", "Smoke", "Roasty", "Smokebeer", "Smokemalt"));
            characters.add(new SpiritCharacters.Character(5, "Chocolate", "Cocoa"));
            characters.add(new SpiritCharacters.Character(5, "Caramel", "Caramelly"));
            characters.add(new SpiritCharacters.Character(5, "Butterscotch"));
            characters.add(new SpiritCharacters.Character(5, "Fudge"));
            characters.add(new SpiritCharacters.Character(5, "Ginger"));
            characters.add(new SpiritCharacters.Character(5, "Cake", "Candy"));
            characters.add(new SpiritCharacters.Character(5, "Floral", "Herbal", "Grassy", "Earthy", "Herbs"));
            characters.add(new SpiritCharacters.Character(5, "Creamy", "Cream", "Lactose"));
            characters.add(new SpiritCharacters.Character(5, "Coffee", "Mocha"));
            characters.add(new SpiritCharacters.Character(5, "Honey"));
            characters.add(new SpiritCharacters.Character(5, "Lemonade", "Soda"));
            characters.add(new SpiritCharacters.Character(5, "Marmalade"));
            characters.add(new SpiritCharacters.Character(5, "Dates"));
            characters.add(new SpiritCharacters.Character(5, "Maple"));
            characters.add(new SpiritCharacters.Character(5, "Molasses"));
            characters.add(new SpiritCharacters.Character(5, "Vanilla"));
            characters.add(new SpiritCharacters.Character(5, "Apple"));
            characters.add(new SpiritCharacters.Character(5, "Cereal"));
            characters.add(new SpiritCharacters.Character(5, "Oak", "Oakwood"));
            characters.add(new SpiritCharacters.Character(5, "Oatmeal", "Oats"));
            characters.add(new SpiritCharacters.Character(5, "Biscuit", "Biscuity"));
            characters.add(new SpiritCharacters.Character(5, "Tart"));
            characters.add(new SpiritCharacters.Character(5, "Toffee"));
            characters.add(new SpiritCharacters.Character(5, "Bread"));
            characters.add(new SpiritCharacters.Character(5, "Coconut"));
            characters.add(new SpiritCharacters.Character(5, "Apricot", "Peach"));
            characters.add(new SpiritCharacters.Character(5, "Mango"));
            characters.add(new SpiritCharacters.Character(5, "Blueberry", "Blueberries"));
            characters.add(new SpiritCharacters.Character(5, "Cranberry", "Cranberries"));
            characters.add(new SpiritCharacters.Character(5, "Pumpkin"));
            characters.add(new SpiritCharacters.Character(5, "Grape", "Grapes"));
            characters.add(new SpiritCharacters.Character(5, "Pie"));
            characters.add(new SpiritCharacters.Character(5, "Pine", "Piney"));
            characters.add(new SpiritCharacters.Character(5, "Rhubarb"));
            characters.add(new SpiritCharacters.Character(5, "Rice"));
            characters.add(new SpiritCharacters.Character(5, "Pear"));
            characters.add(new SpiritCharacters.Character(5, "Pineapple"));
            characters.add(new SpiritCharacters.Character(5, "Raspberries", "Raspberry"));
            characters.add(new SpiritCharacters.Character(5, "Strawberries", "Strawberry"));
            characters.add(new SpiritCharacters.Character(5, "Cherries", "Cherry"));
            characters.add(new SpiritCharacters.Character(5, "Hazelnut", "Nuts", "Pecan"));
            characters.add(new SpiritCharacters.Character(5, "Watermelon", "Melon"));
            characters.add(new SpiritCharacters.Character(5, "Champagne"));
            characters.add(new SpiritCharacters.Character(5, "Bananas", "Banana"));
            characters.add(new SpiritCharacters.Character(5, "Barbecue", "Barbecued"));
            characters.add(new SpiritCharacters.Character(5, "Wheat", "Weiss"));
            characters.add(new SpiritCharacters.Character(5, "Litchi", "Litchiness"));
            characters.add(new SpiritCharacters.Character(5, "Resin"));
            characters.add(new SpiritCharacters.Character(5, "Sour", "Sourness"));
            characters.add(new SpiritCharacters.Character(5, "Spicy", "Spice", "Spiced", "Spices", "Spiciness", "Pepper", "Peppery", "Peppermint"));
            characters.add(new SpiritCharacters.Character(5, "Sweet", "Sweetness", "Sugar"));
            characters.add(new SpiritCharacters.Character(5, "Bitter", "Bitterness", "Bittering", "Bitters"));
        }
    };

    private SpiritCharacters selectSpiritCharactersForType(Locators.SpiritType type) {
        if (type == Locators.SpiritType.BEER) {     // currently only works for beers
            return beerCharacters;
        }
        // add more SpiritCharacters templates for other spirit types here
        else if (type != null) {
            //log.warning("Unsupported spirit type: " + type);      // #DEBUG
            return null;
        } else {
            //log.warning("Type is empty");
            return null;
        }
    }

    /**
     * Extracts characters list from spirit description text
     * @param description spirit description unformated text
     * @param type spirit type
     * @return coma separated string of Characters
     */
    public String extractSpiritCharacter(String description, Locators.SpiritType type) {
        if (description == null || description.isEmpty()) {
            log.warning("Description is empty");
            return null;
        }
        final SpiritCharacters characters = selectSpiritCharactersForType(type);
        if(characters == null){
            return null;
        }

        String[] words = description.toUpperCase().replaceAll("[^a-zA-Z0-9-\\s]", "").split("\\s+");
        Set<String> chars = Arrays.stream(words).parallel().map(p -> {
            SpiritCharacters.Character character = characters.parse(p);
            return character != null ? character.toString() : null;
        }).filter(Objects::nonNull).collect(Collectors.toSet());

        return String.join(", ", chars);
    }

    /**
     * Parses coma separated spirit characters string, inot a collection of Characters objects
     * @param character coma separated characters string
     * @param type spirit type
     * @return set of Characters objects
     */
    public Set<SpiritCharacters.Character> getSpiritCharacters(String character, Locators.SpiritType type) {
        if (character == null || character.isEmpty()) {
            return null;
        }

        final SpiritCharacters characters = selectSpiritCharactersForType(type);
        if (characters == null) {
            return null;
        }

        Set<SpiritCharacters.Character> charSet = new HashSet<>();
        String[] chars = character.split(", ");
        for(String charStr : chars) {
            SpiritCharacters.Character schar = characters.parse(charStr);
            if(schar != null) {
                charSet.add(schar);
            }
        }

        return charSet;
    }
}
