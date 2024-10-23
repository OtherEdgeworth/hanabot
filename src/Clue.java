import java.util.HashSet;

public class Clue
{
    ClueType clueType;
    HashSet<String> possibleSuits = new HashSet<>();
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

    //TODO: refactor so that code is always suit-value and dispaly/ui output is value-suit to distinguish them more easily
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

    public Clue(ClueType clueType, Tile tile)
    {
        this.clueType = clueType;
        this.value = tile.value;
        this.suit = tile.suit;
    }

    public Clue(Clue clue)
    {
        this.clueType = clue.clueType;
        this.possibleSuits = new HashSet<>(clue.possibleSuits);
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

    public boolean isDefinitive() { return !suit.isBlank() && value != 0; }

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
    public String toStringBrief() { return (value != 0 ? value + " " : "") + suit + " " + clueType.name(); }
    public String toStringVerbose()
    {
        return ("".equals(suit) && !possibleSuits.isEmpty() ? possibleSuits.toString() : suit) + " " + value + " "
                +  clueType.name() + " clue";
    }
}
