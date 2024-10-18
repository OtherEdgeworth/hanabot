import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SingleCardFocusQuestionTests
{
    @Test
    public void focusQ1()
    {
        // Setup hands
        Player alice = new Player();
        alice.hand = new Tile[alice.handSize];

        Player bob = new Player(5, ChopMethod.NON_CLUED, false);
        bob.hand = Tile.hand(Tile.w1, Tile.w2, Tile.w3, Tile.w4, Tile.b2);
        bob.updateChopPosition();

        // Q: Alice clues Bob on w. Which slot is focussed? - A: Tile 1 (index 0) (new)
        Clue clue = new Clue(ClueType.NULL, "w");
        Assertions.assertEquals(0, bob.focusIndex(clue));
    }

    @Test
    public void focusQ2()
    {
        // Setup game
        Player alice = new Player();
        alice.hand = new Tile[alice.handSize];

        Player bob = new Player(5, ChopMethod.NON_CLUED, false);
        bob.hand = Tile.hand(Tile.g5, Tile.g2, Tile.g3, Tile.g4, Tile.g1);
        bob.updateChopPosition();

        // Q: Alice clues Bob on g. Which slot is focussed? - A: Tile 5 (index 4) (new + chop)
        Clue clue = new Clue(ClueType.NULL, "g");
        Assertions.assertEquals(4, bob.focusIndex(clue));
    }

    @Test
    public void focusQ3()
    {
        // Setup hands
        Player alice = new Player();
        alice.hand = new Tile[alice.handSize];

        Player bob = new Player(5, ChopMethod.NON_CLUED, false);
        Tile bt2 = new Tile("b", 2);
        bt2.hintedIdentity.value = 2;
        Tile bt5 = new Tile("r", 2);
        bt5.hintedIdentity.value = 2;
        Clue bc5 = new Clue(ClueType.TWO_SAVE, 2);
        bt5.information.add(bc5);
        bob.hand = Tile.hand(null, bt2, Tile.b1, null, bt5);
        bob.updateChopPosition();

        // Q: Alice clues Bob on b. Which slot is focussed? - A: Tile 3 (index 2) (new)
        Clue clue = new Clue(ClueType.NULL, "b");
        Assertions.assertEquals(2, bob.focusIndex(clue));
    }

    @Test
    public void focusQ4()
    {
        // Setup hands
        Player alice = new Player();
        alice.hand = new Tile[alice.handSize];

        Player bob = new Player(5, ChopMethod.NON_CLUED, false);
        Tile bt4 = new Tile("b", 5);
        bt4.hintedIdentity.value = 5;
        Tile bt5 = new Tile("r", 2);
        bt5.hintedIdentity.value = 2;
        Clue bc5 = new Clue(ClueType.TWO_SAVE, 2);
        bt5.information.add(bc5);
        bob.hand = Tile.hand(null, Tile.r3, Tile.r1, bt4, bt5);
        bob.updateChopPosition();

        // Q: Alice clues Bob on r. Which slot is focussed? - A: Tile 3 (index 2) (new + chop)
        Clue clue = new Clue(ClueType.NULL, "r");
        Assertions.assertEquals(2, bob.focusIndex(clue));
    }
}
