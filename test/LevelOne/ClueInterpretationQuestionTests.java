import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ClueInterpretationQuestionTests
{
    @Test
    public void interpretationQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b3, Tile.g2, Tile.r1, Tile.y3, Tile.w1);
        game.discarded.put(Tile.b2, 1);
        game.discarded.put(Tile.g1, 1);
        game.discarded.put(Tile.g3, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[alice.handSize];

        cathy.hand = Tile.hand(null, null, Tile.g4, Tile.g3);
        cathy.updateChopPosition();

        Tile dt3 = new Tile("w", 3);
        dt3.hintedIdentity.value = 3;
        Tile dt4 = new Tile("r", 3);
        dt4.hintedIdentity.value = 3;
        donald.hand = Tile.hand(Tile.b3, Tile.g2, dt3, dt4);

        // Take action - Alice clues Cathy on g
        Clue clue = new Clue(ClueType.PLAY, "g");
        Assertions.assertEquals(3, cathy.focusIndex(clue));
        new ClueAction(2, clue).execute(game, alice);

        // Q: Which slot is focused? For Cathy, is this a Play or Save? What card note should Cathy make?
        // A: Slot 4 (index 3) (new + chop) (checked above); Play;  g 3 PLAY clue
        Assertions.assertTrue(cathy.hand[2].isClued());
        Assertions.assertEquals(1, cathy.hand[3].information.size());
        Clue cathyClue = cathy.hand[3].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, cathyClue.clueType);
        Assertions.assertEquals("g", cathyClue.suit);
        Assertions.assertEquals(3, cathyClue.value);
    }

    @Test
    public void interpretationQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b1, Tile.g1, Tile.r2, Tile.y1, Tile.w3);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, null, null, null, Tile.b2);

        // Take action - Alice clues Bob on 2
        Clue clue = new Clue(ClueType.PLAY, 2);
        Assertions.assertEquals(4, bob.focusIndex(clue));
        new ClueAction(1, clue).execute(game, alice);

        // Q: Which slot is focussed? For Bob, is this a Play or a Save? What note does Bob make?
        // A: Slot 5 (index 4) (new + chop) (checked above); Play;, [b, g, y] 2 PLAY clue
        Assertions.assertEquals(1, bob.hand[4].information.size());
        Clue bobClue = bob.hand[4].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, bobClue.clueType);
        Assertions.assertEquals(2, bobClue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(bobClue.possibleSuits, List.of("b", "g", "y")));
    }

    @Test
    public void interpretationQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b5, Tile.g3, Tile.r1, null, Tile.w4);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt4 = new Tile("r", 2);
        bt4.hintedIdentity.value = 2;
        Tile bt5 = new Tile("r", 5);
        bt5.hintedIdentity.value = 5;
        bob.hand = Tile.hand(null, Tile.r4, Tile.r3, bt4, bt5);
        bob.updateChopPosition();

        // Take action - Alice clues Bob on r
        Clue clue = new Clue(ClueType.PLAY, "r");
        Assertions.assertEquals(2, bob.focusIndex(clue));
        new ClueAction(1, clue).execute(game, alice);

        // Q: Which slot is focussed? For Bob, is this a Play or a Save? What card not does Bob make?
        // A: Slot 2 (index 1) (new + chop) (checked above); Play; r 3 DELAYED_PLAY
        Assertions.assertEquals(1, bob.hand[2].information.size());
        Clue bobClue = bob.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, bobClue.clueType);
        Assertions.assertEquals("r", bobClue.suit);
        Assertions.assertEquals(3, bobClue.value);
    }

    @Test
    public void interpretationQ4()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b1, Tile.g2, Tile.r5, null, Tile.w2);
        game.discarded.put(Tile.b3, 1);
        game.discarded.put(Tile.b4, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt5 = new Tile("b", 2);
        bt5.hintedIdentity.value = 2;
        bob.hand = Tile.hand(null, null, Tile.b5, Tile.b3, bt5);
        bob.updateChopPosition();

        // Take action - Alice clues Bob on b
        Clue clue = new Clue(ClueType.PLAY, "b");
        Assertions.assertEquals(3, bob.focusIndex(clue));

        new ClueAction(1, clue).execute(game, alice);

        // Q: Which slot is focussed? For Bob, is this a Play or Save? What note does Bob make?
        // A: Slot 4 (index 3) (new + chop); Both; b 3 DELAYED_PLAY, b 4 CRITICAL_SAVE
        Assertions.assertEquals(2, bob.hand[3].information.size()); //TODO: delay clues
        boolean saveFirst = bob.hand[3].information.get(0).clueType.isSaveClue();
        Clue saveClue = bob.hand[3].information.get(saveFirst ? 0 : 1);
        Clue delayClue = bob.hand[3].information.get(saveFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals("b", delayClue.suit);
        Assertions.assertEquals(3, delayClue.value);

        Assertions.assertEquals(ClueType.CRITICAL_SAVE, saveClue.clueType);
        Assertions.assertEquals("b", saveClue.suit);
        Assertions.assertEquals(4, saveClue.value);
    }
}
