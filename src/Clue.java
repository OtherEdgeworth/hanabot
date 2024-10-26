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
        this.suit = "";
        this.value = 0;
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
        this.suit = suit;
        this.value = value;
    }

    public Clue(ClueType clueType, Clue clue)
    {
        this.clueType = clueType;
        this.suit = clue.suit;
        this.value = clue.value;
    }

    public Clue(ClueType clueType, Tile tile)
    {
        this.clueType = clueType;
        this.suit = tile.suit;
        this.value = tile.value;
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

    public boolean equals(Clue o) { return clueType == o.clueType && suit.equals(o.suit) && value.equals(o.value); }
    public boolean equals(Tile o)
    {
        if (suit.isBlank() || value == 0)
            return false;
        return (suit.equals(o.suit) && value == o.value);
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
        return value + " " + ("".equals(suit) && !possibleSuits.isEmpty() ? possibleSuits.toString() : suit) + " "
                +  clueType.name() + " clue";
    }
}
