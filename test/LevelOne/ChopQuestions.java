import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ChopQuestions
{
    @Test
    public void chopQ1()
    {
        // Setup hand
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);
        alice.hand = new Tile[]{Tile.b5, Tile.y1, Tile.y2, Tile.y3, Tile.g1};
        alice.updateChopPosition();

        // Q: Which slot is Alice's chop? - A: Slot 5 (index 4)
        Assertions.assertEquals(4, alice.chopPosition);
    }

    @Test
    public void chopQ2()
    {
        // Setup hand
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);
        Tile t3 = new Tile(new Clue(ClueType.NULL, 2, "g"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 4, "w"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 1, "b"));
        alice.hand = Tile.hand(Tile.y2, Tile.b3, t3, t4, t5);
        alice.updateChopPosition();

        // Q: Which slot is Alice's chop> - A: Slot 2 (index 1)
        Assertions.assertEquals(1, alice.chopPosition);
    }

    @Test
    public void chopQ3()
    {
        // Setup hand
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);
        Tile t2 = new Tile(new Clue(ClueType.NULL, 3, "r"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 4, "r"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 5, "r"));
        alice.hand = Tile.hand(Tile.y1, t2, Tile.b2, t4, t5);
        alice.updateChopPosition();

        // Q: Which slot is Alice's chop? - A: Slot 3 (index 2)
        Assertions.assertEquals(2, alice.chopPosition);
    }

    @Test
    public void chopQ4()
    {
        // Setup hand
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);
        Tile t1 = new Tile(new Clue(ClueType.NULL, 4, "g"));
        Tile t2 = new Tile(new Clue(ClueType.NULL, 3, "w"));
        Tile t3 = new Tile(new Clue(ClueType.NULL, 4, "w"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 5, "g"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 5, "b"));
        alice.hand = new Tile[] { t1, t2, t3, t4, t5 };
        alice.updateChopPosition();

        // Q: Which slot is Alice's chop? - A: She doesn't have one (index -1)
        Assertions.assertEquals(-1, alice.chopPosition);
    }
}