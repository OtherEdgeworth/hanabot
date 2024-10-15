import java.util.ArrayList;

public class Clue
{
    ClueType clueType;
    ArrayList<String> possibleSuits = new ArrayList<>();
    String suit;
    Integer value;

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

    public Clue(Clue clue)
    {
        this.clueType = clue.clueType;
        this.possibleSuits = new ArrayList<>(clue.possibleSuits);
        this.suit = clue.suit;
        this.value = clue.value;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Clue c)
            return equals(c);
        if (o instanceof Tile t)
            return equals(t);
        return super.equals(o);
    }

    public boolean equals(Clue o) { return clueType == o.clueType && value.equals(o.value) && suit.equals(o.suit); }
    public boolean equals(Tile o)
    {
        if (value == 0 || suit.isBlank())
            return false;
        return (value == o.value && suit.equals(o.suit));
    }

    public boolean matches(Tile tile)
    {
        return tile != null && ((suit.isBlank() && tile.value == value) || (tile.suit.equals(suit) && value == 0) ||
                (this.equals(tile)));
    }

    @Override
    public String toString()
    {
        return (possibleSuits.isEmpty() || value != 0 || !"".equals((suit)) ? toStringBrief() : toStringVerbose());
    }
    public String toStringBrief() { return clueType.name() + " clue '" + (value != 0 ? value : suit) + "'"; }
    public String toStringVerbose()
    {
        return ("".equals(suit) && !possibleSuits.isEmpty() ? possibleSuits.toString() : suit) + " " + value + " "
                +  clueType.name() + " clue";
    }
}
