import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GoodTouchPrincipleQuestionTests
{
    @Test
    public void gtpQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b2, Tile.g5, Tile.r1, Tile.y4, Tile.w1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.w1, Tile.r3, Tile.g1, Tile.b2, Tile.b3);
        bob.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What should Alice clue Bob? - A: 3 PLAY clue
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals(3, clue.value);
    }

    @Test
    public void gtpQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b3, null, Tile.r1, null, Tile.w3);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(Tile.w1, Tile.w5, Tile.r4, Tile.b1, Tile.r2);
        bob.updateChopPosition();

        Tile ct3 = new Tile(new Clue(ClueType.NULL, 3, "r"));
        Tile ct4 = new Tile(4, "r");
        ct4.hintedIdentity.suit = "r";
        Tile ct5 = new Tile(3, "g");
        ct5.hintedIdentity.value = 3;
        cathy.hand = Tile.hand(Tile.g2, Tile.w2, ct3, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What should Alice do? What notes should be taken if she clues?
        // A: Clue Bob on 2; Bob notes r 2 PLAY, y 2 SAVE
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals(2, clue.value);

        action.execute(game, alice);
        Assertions.assertEquals(2, bob.hand[4].information.size());

        boolean playFirst = (bob.hand[4].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[4].information.get(playFirst ? 0 : 1);
        Clue saveClue = bob.hand[4].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("r", playClue.suit);
        Assertions.assertEquals(2, playClue.value);

        Assertions.assertEquals(ClueType.TWO_SAVE, saveClue.clueType);
        Assertions.assertEquals("y", saveClue.suit);
        Assertions.assertEquals(2, saveClue.value);
    }

    @Test
    public void gtpQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b3, null, Tile.r1, null, Tile.w3);

        // Setup hands
        Tile at5 = new Tile(2, "b");
        at5.hintedIdentity.value = 2;
        Clue ac51 = new Clue(ClueType.TWO_SAVE, 2);
        ac51.possibleSuits.addAll(List.of("b", "r", "w"));
        Clue ac52 = new Clue(ClueType.PLAY, 2);
        ac52.possibleSuits.addAll(List.of("g", "y"));
        at5.information.add(ac51);
        at5.information.add(ac52);
        alice.hand = Tile.hand(null, null, null, null, at5);

        bob.hand = Tile.hand(Tile.y2, Tile.w3, Tile.b3, Tile.w5, Tile.w3);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.b4, Tile.b5, Tile.b4, Tile.g1, Tile.g3);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What should Alice do? What notes should be taken if she clues? - A: Discard
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }

    @Test
    public void gtpQ4()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g2, null, null, null);

        // Setup hands
        alice.hand = new Tile[5];

        bob.hand = Tile.hand(Tile.w3, Tile.r2, Tile.g3, Tile.w4, Tile.g3);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.r5, Tile.b3, Tile.y4, Tile.y2, Tile.g1);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What should Alice do? What notes should be taken if she clues? - A: Discard
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }
}
