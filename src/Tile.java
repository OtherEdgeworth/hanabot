import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Tile implements Comparable<Tile>
{
    public static final String[] SUIT_INDEX = new String[] { "b", "g", "r", "y", "w" };
    public static final Integer[] ALL_VALUES = new Integer[] { 1, 2, 3, 4, 5 };

    int value;
    String suit;
    String consoleColour;
    boolean inChopPosition = false;
    Clue hintedIdentity = new Clue(ClueType.NULL);
    ArrayList<Clue> information = new ArrayList<>();
    HashSet<String> negativeSuitInformation = new HashSet<>();
    HashSet<Integer> negativeValueInformation = new HashSet<>();

    Tile(int value, String suit)
    {
        this.value = value;
        this.suit = suit;
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

    Tile (Tile tile)
    {
        this(tile.value, tile.suit);
        this.inChopPosition = false;
    }

    Tile (Clue clue)
    {
        this(clue.value, clue.suit);
        this.inChopPosition = false;
        this.hintedIdentity = clue;
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
        if (o instanceof Tile)
            return equals((Tile)o);
        return super.equals(o);
    }

    public boolean equals(Tile o)  { return this.value == o.value && this.suit.equals(o.suit); }

    public static String colourOf(String suit)
    {
        if ("b".equals(suit)) return ConsoleColours.BLUE;
        if ("g".equals(suit)) return ConsoleColours.BRIGHT_GREEN;
        if ("r".equals(suit)) return ConsoleColours.RED;
        if ("y".equals(suit)) return ConsoleColours.BRIGHT_YELLOW;
        if ("w".equals(suit)) return ConsoleColours.BRIGHT_WHITE;
        return ConsoleColours.RESET;
    }

    public boolean isClued() { return (hintedIdentity.value != 0 || !hintedIdentity.suit.isBlank() || !information.isEmpty()); }

    public boolean isPlayable()
    {
        ArrayList<Tile> playableTiles = Main.playableTiles();
        for (Tile playableTile : playableTiles)
            if (hintedIdentity.equals(playableTile) && !negativeSuitInformation.contains(playableTile.suit) &&
                    !negativeValueInformation.contains(playableTile.value))
                return true;

        return false;
    }

    public boolean isUseless()
    {
        //cannot know if a tile is useless if we know nothing about a tile
        if (hintedIdentity.value == 0 && hintedIdentity.suit.isBlank())
            return false;

        //both suit and value are known
        if (hintedIdentity.value != 0 && !hintedIdentity.suit.isBlank())
            if (Main.inPlay[suitIndex()] == null)
                return false;
            else
                return Main.inPlay[suitIndex()].value >= hintedIdentity.value;

        //suit is known, but a 5 in suit has been played
        if (!hintedIdentity.suit.isBlank())
            if (Main.inPlay[suitIndex()] == null)
                return false;
            else
                return Main.inPlay[suitIndex()].value == 5;

        //value is known, but it or greater is played in all suits
        boolean suitWideValueCheck = true;
        for (Tile tile : Main.inPlay)
            suitWideValueCheck &= (tile == null || tile.value >= hintedIdentity.value);

        return suitWideValueCheck;
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

    public void updateIdentityFromNegativeInformation()
    {
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
    public static Tile b1 = new Tile(1, "b");
    public static Tile b2 = new Tile(2, "b");
    public static Tile b3 = new Tile(3, "b");
    public static Tile b4 = new Tile(4, "b");
    public static Tile b5 = new Tile(5, "b");

    //greens
    public static Tile g1 = new Tile(1, "g");
    public static Tile g2 = new Tile(2, "g");
    public static Tile g3 = new Tile(3, "g");
    public static Tile g4 = new Tile(4, "g");
    public static Tile g5 = new Tile(5, "g");

    //reds
    public static Tile r1 = new Tile(1, "r");
    public static Tile r2 = new Tile(2, "r");
    public static Tile r3 = new Tile(3, "r");
    public static Tile r4 = new Tile(4, "r");
    public static Tile r5 = new Tile(5, "r");

    //yellows
    public static Tile y1 = new Tile(1, "y");
    public static Tile y2 = new Tile(2, "y");
    public static Tile y3 = new Tile(3, "y");
    public static Tile y4 = new Tile(4, "y");
    public static Tile y5 = new Tile(5, "y");

    //whites
    public static Tile w1 = new Tile(1, "w");
    public static Tile w2 = new Tile(2, "w");
    public static Tile w3 = new Tile(3, "w");
    public static Tile w4 = new Tile(4, "w");
    public static Tile w5 = new Tile(5, "w");
}
