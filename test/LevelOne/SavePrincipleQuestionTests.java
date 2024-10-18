import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

public class SavePrincipleQuestionTests
{
    @Test
    public void spQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g2, null, null, null);
        game.discarded.put(Tile.g4, 1);

        // Setup hands
        alice.hand = new Tile[5];

        Tile bt1 = new Tile("b", 1);
        bob.hand = Tile.hand(bt1, null, null, null, null);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.r1, Tile.w4, Tile.w3, Tile.r1, Tile.g4);
        cathy.updateChopPosition();

        // Take action - Alice clues Bob on b, Bob enumerates and prioritises his possible actions
        new ClueAction(1, new Clue(ClueType.PLAY, "b")).execute(game, alice);
        bob.enumerateActions();
        bob.prioritiseActions();

        // Q: Should Bob play the blue 1? - A: No, he should critical-save Cathy's 4g (either 4 or g)
        Assertions.assertFalse(bob.possibleActions.isEmpty());
        Action action = bob.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.CRITICAL_SAVE, clue.clueType);
        Assertions.assertTrue("g".equals(clue.suit) || clue.value == 4);
    }

    @Test
    public void spQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b1, Tile.g1, Tile.r2, null, null);

        // Setup hands
        alice.hand = new Tile[5];

        bob.hand = Tile.hand(Tile.g4, Tile.b5, Tile.g1, Tile.r2, Tile.y2);
        bob.updateChopPosition();

        Tile ct4 = new Tile("w", 5);
        ct4.hintedIdentity.suit = "w";
        Tile ct5 = new Tile("w", 4);
        ct5.hintedIdentity.suit = "w";
        cathy.hand = Tile.hand(Tile.r5, Tile.w3, Tile.g3, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What should Alice do? What notes should be taken if she clues?
        // A: 2-save Bob; [b, g] 2 PLAY, [y, w] TWO_SAVE
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.TWO_SAVE, clue.clueType);
        Assertions.assertEquals(2, clue.value);

        action.execute(game, alice);
        Assertions.assertEquals(2, bob.hand[4].information.size());

        boolean playFirst = (bob.hand[4].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[4].information.get(playFirst ? 0 : 1);
        Clue saveClue = bob.hand[4].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals(2, playClue.value);
        Assertions.assertEquals(new HashSet<>(List.of("b", "g")), playClue.possibleSuits);

        Assertions.assertEquals(ClueType.TWO_SAVE, saveClue.clueType);
        Assertions.assertEquals(2, saveClue.value);
        Assertions.assertEquals(new HashSet<>(List.of("y", "w")), saveClue.possibleSuits);
    }
}
