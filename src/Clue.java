public class Clue
{
    ClueType clueType;
    Integer value;
    String suit;

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

    @Override
    public String toString() { return clueType.name() + " clue '" + (value != 0 ? value : suit) + "'"; }
}
