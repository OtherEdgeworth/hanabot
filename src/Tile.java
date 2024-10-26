import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Tile implements Comparable<Tile>
{
    public static final String[] SUIT_INDEX = new String[] { "b", "g", "r", "y", "w" };
    public static final Integer[] ALL_VALUES = new Integer[] { 1, 2, 3, 4, 5 };

    String consoleColour;
    Clue hintedIdentity = new Clue(ClueType.NULL);
    boolean inChopPosition = false;
    ArrayList<Clue> information = new ArrayList<>();
    HashSet<String> negativeSuitInformation = new HashSet<>();
    HashSet<Integer> negativeValueInformation = new HashSet<>();
    String suit;
    int value;

    Tile(String suit, int value)
    {
        this.suit = suit;
        this.value = value;
        switch (suit)
        {
            case "b": consoleColour = ConsoleColours.BLUE; break;
            case "g": consoleColour = ConsoleColours.BRIGHT_GREEN; break;
            case "r": consoleColour = ConsoleColours.RED; break;
            case "y": consoleColour = ConsoleColours.BRIGHT_YELLOW; break;
            case "w": consoleColour = ConsoleColours.BRIGHT_WHITE; break;
            default: consoleColour = ConsoleColours.RESET; break;
        }
    }

    Tile (Tile tile) { this(tile, false); }

    Tile (Tile tile, boolean fullCopy)
    {
        this(tile.suit, tile.value);
        this.inChopPosition = false;

        if (fullCopy)
        {
            this.inChopPosition = tile.inChopPosition;
            this.hintedIdentity = new Clue(tile.hintedIdentity);
            this.information = new ArrayList<>(tile.information);
            this.negativeSuitInformation = new HashSet<>(tile.negativeSuitInformation);
            this.negativeValueInformation = new HashSet<>(tile.negativeValueInformation);
        }
    }

    Tile (Clue clue)
    {
        this(clue.suit, clue.value);
        this.inChopPosition = false;
        this.hintedIdentity = clue;
    }

    public static Tile[] hand(Tile... tiles)
    {
        Tile[] result = new Tile[tiles.length];
        for (int i = 0; i < tiles.length; i++)
            if (tiles[i] == null)
                result[i] = null;
            else
                result[i] = new Tile(tiles[i], true);
        return result;
    }

    @Override
    public int compareTo(Tile o)
    {
        if (this.suit.equals(o.suit))
            return this.value - o.value;
        return this.suitIndex() - o.suitIndex();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Tile t)
            return equals(t);
        return super.equals(o);
    }

    public boolean equals(Tile o)
    {
        if (o == null)
            return false;
        return this.value == o.value && this.suit.equals(o.suit);
    }

    public static String colourOf(String suit)
    {
        if ("b".equals(suit)) return ConsoleColours.BLUE;
        if ("g".equals(suit)) return ConsoleColours.BRIGHT_GREEN;
        if ("r".equals(suit)) return ConsoleColours.RED;
        if ("y".equals(suit)) return ConsoleColours.BRIGHT_YELLOW;
        if ("w".equals(suit)) return ConsoleColours.BRIGHT_WHITE;
        return ConsoleColours.RESET;
    }

    @Override
    public int hashCode() { return 11 +  (suit.isBlank() ? 0 : suit.hashCode()) + 13 * value; }

    public boolean hasAnyClueTypes(List<ClueType> clueTypes)
    {
        for (Clue clue : information)
            if (clueTypes.contains(clue.clueType))
                return true;
        return false;
    }

    public boolean hasOnlyPlayClues()
    {
        boolean onlyPlayClues = !information.isEmpty();
        for (Clue clue : information)
            if (clue.clueType != ClueType.PLAY)
            {
                onlyPlayClues = false;
                break;
            }
        return onlyPlayClues;
    }

    public boolean hasPlayClue()
    {
        for (Clue clue : information)
            if (clue.clueType == ClueType.PLAY)
                return true;
        return false;
    }

    public boolean isClued() { return (!hintedIdentity.suit.isBlank() || hintedIdentity.value != 0 || !information.isEmpty()); }

    public boolean matches(Tile tile)
    {
        if (hintedIdentity.suit.isBlank() && hintedIdentity.value == 0)
            return false;

        boolean clueContainsSuit = false;
        for (Clue clue : this.information)
            if (clue.possibleSuits.contains(tile.suit))
            {
                clueContainsSuit = true;
                break;
            }
        boolean suitMatch = hintedIdentity.suit.equals(tile.suit) || (hintedIdentity.suit.isBlank() && (clueContainsSuit || tile.information.isEmpty()));
        boolean valueMatch = (hintedIdentity.value == 0 || hintedIdentity.value == tile.value) && !this.negativeValueInformation.contains(tile.value);
        return valueMatch && suitMatch;
    }

    public static String fullSuit(String suit)
    {
        if ("b".equals(suit)) return "blue";
        if ("g".equals(suit)) return "green";
        if ("r".equals(suit)) return "red";
        if ("y".equals(suit)) return "yellow";
        if ("w".equals(suit)) return "white";
        return "";
    }

    public static String suitByIndex(int index) { return SUIT_INDEX[index]; }

    public static int suitIndex(String suit)
    {
        if ("b".equals(suit)) return 0;
        if ("g".equals(suit)) return 1;
        if ("r".equals(suit)) return 2;
        if ("y".equals(suit)) return 3;
        if ("w".equals(suit)) return 4;
        return 5;
    }

    public int suitIndex() { return suitIndex(this.suit); }

    public String toSimpleString() { return "[" + value + suit + "]"; }

    @Override
    public String toString() { return this.toString(false); }

    public String toString(boolean isPlayerOwned)
    {
        StringBuilder tile = new StringBuilder();
        String currentColour = "";

        if (inChopPosition)
            currentColour = ConsoleColours.BRIGHT_RED;
        else if (isClued())
            currentColour = ConsoleColours.YELLOW;

        tile.append(currentColour).append("[");
        currentColour = (isPlayerOwned ? (!currentColour.isBlank() ? ConsoleColours.RESET : "") : consoleColour);
        tile.append(currentColour).append(isPlayerOwned ? printHintedIdentity() : value + suit);

        if (inChopPosition)
            currentColour = ConsoleColours.BRIGHT_RED;
        else if (isClued())
            currentColour = ConsoleColours.YELLOW;
        else if (!currentColour.isBlank() && !consoleColour.equals(ConsoleColours.RESET))
            currentColour = ConsoleColours.RESET;
        tile.append(currentColour).append("]").append(!currentColour.isBlank() && !consoleColour.equals(ConsoleColours.RESET) ? ConsoleColours.RESET : "");

        return tile.toString();
    }

    //TODO: check if tile update method in Player is already doing this, if so, can it be replaced with this method, if not, then remove this
    public void updateIdentityFromNegativeInformation()
    {
        if (!hintedIdentity.suit.isBlank())
            negativeSuitInformation = new HashSet<>();
        if (hintedIdentity.value != 0)
            negativeValueInformation = new HashSet<>();

        if (hintedIdentity.suit.isBlank() && negativeSuitInformation.size() == 4)
            hintedIdentity.suit = CollectionUtils.disjunction(List.of(SUIT_INDEX), negativeSuitInformation).iterator().next();
        if (hintedIdentity.value == 0 && negativeValueInformation.size() == 4)
            hintedIdentity.value = CollectionUtils.disjunction(List.of(ALL_VALUES), negativeValueInformation).iterator().next();
    }

    private String printHintedIdentity()
    {
        String suit = (!hintedIdentity.suit.isBlank() ? hintedIdentity.suit : "-");
        String value = (hintedIdentity.value != 0 ? String.valueOf(hintedIdentity.value) : "-");
        String colour = "";
        if (!"-".equals(suit))
            colour = colourOf(suit);
        return colour + value + suit + (!colour.isBlank() ? ConsoleColours.RESET : "");
    }

    //blues
    public static final Tile b1 = new Tile("b", 1);
    public static final Tile b2 = new Tile("b", 2);
    public static final Tile b3 = new Tile("b", 3);
    public static final Tile b4 = new Tile("b", 4);
    public static final Tile b5 = new Tile("b", 5);

    //greens
    public static final Tile g1 = new Tile("g", 1); //has negative b?
    public static final Tile g2 = new Tile("g", 2); //has negative b?
    public static final Tile g3 = new Tile("g", 3);
    public static final Tile g4 = new Tile("g", 4);
    public static final Tile g5 = new Tile("g", 5);

    //reds
    public static final Tile r1 = new Tile("r", 1);
    public static final Tile r2 = new Tile("r", 2);
    public static final Tile r3 = new Tile("r", 3);
    public static final Tile r4 = new Tile("r", 4);
    public static final Tile r5 = new Tile("r", 5);

    //yellows
    public static final Tile y1 = new Tile("y", 1);
    public static final Tile y2 = new Tile("y", 2);
    public static final Tile y3 = new Tile("y", 3);
    public static final Tile y4 = new Tile("y", 4); //has negative b?
    public static final Tile y5 = new Tile("y", 5);

    //whites
    public static final Tile w1 = new Tile("w", 1);
    public static final Tile w2 = new Tile("w", 2);
    public static final Tile w3 = new Tile("w", 3);
    public static final Tile w4 = new Tile("w", 4);
    public static final Tile w5 = new Tile("w", 5); //has negative b?
}
