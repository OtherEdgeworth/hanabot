import java.util.ArrayList;

public class Clue
{
    ClueType clueType;
    Integer value;
    String suit;
    ArrayList<String> possibleSuits = new ArrayList<>();
    ArrayList<Integer> possibleValues = new ArrayList<>();

    public Clue(ClueType clueType)
    {
        this.clueType = clueType;
        this.value = 0;
        suit = "";
    }

    public Clue(ClueType clueType, int value)
    {
        this(clueType);
        this.value = value;
    }

    public Clue(ClueType clueType, String suit)
    {
        this(clueType);
        this.suit = suit;
    }

    public Clue(ClueType clueType, int value, String suit)
    {
        this.clueType = clueType;
        this.value = value;
        this.suit = suit;
    }

    public Clue(ClueType clueType, Clue clue)
    {
        this.clueType = clueType;
        this.value = clue.value;
        this.suit = clue.suit;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Clue)
            return equals((Clue)o);
        return super.equals(o);
    }

    public boolean equals(Clue o)
    {
        if (clueType != ClueType.NULL && o.clueType != ClueType.NULL && clueType == o.clueType)
            return false;
        if (suit.isBlank() || o.suit.isBlank())
            return value.equals(o.value);
        if (value == 0 || o.value == 0)
            return suit.equals(o.suit);
        return clueType == o.clueType && value.equals(o.value) && suit.equals(o.suit);
    }

    public boolean equals(Tile o)
    {
        if (value == 0 || suit.isBlank())
            return false;
        return (value == o.value && suit.equals(o.suit));
    }

    public boolean matches(Tile tile) { return tile.value == value || tile.suit.equals(suit); }
    public boolean matchesKnown(Tile tile) { return tile.hintedIdentity.value.equals(value) || tile.hintedIdentity.suit.equals(suit); }

    @Override
    public String toString()
    {
        return ((possibleSuits.isEmpty() && possibleValues.isEmpty()) || value != 0 ||
            !"".equals((suit)) ? toStringBrief() : toStringVerbose());
    }
    public String toStringBrief() { return clueType.name() + " clue '" + (value != 0 ? value : suit) + "'"; }
    public String toStringVerbose()
    {
        return ("".equals(suit) && !possibleSuits.isEmpty() ? possibleSuits.toString() : suit) + " " +
                (value != 0 && !possibleValues.isEmpty() ? possibleValues.toString() : value) + " " +  clueType.name() +
                " clue";
    }
}
