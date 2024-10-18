import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class EarlyGameQuestionTests
{
    @Test
    public void earlyGameQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.discarded.put(Tile.r1, 1);

        // Setup hands
        alice.hand = new Tile[5];
        bob.hand = Tile.hand(null, null, null, null, Tile.r1);
        cathy.hand = Tile.hand(Tile.b1, Tile.g1, Tile.r1, null, null, Tile.b4); //b4 just to prevent chop going whack

        // Take action - Alice clues Cathy on 1, Bob discards a red 1
        new ClueAction(2, new Clue(ClueType.PLAY, 1)).execute(game, alice);
        new DiscardAction(4).execute(game, bob);

        // Q: Is it the Early Game or the Mid-Game? - A: Mid-Game (not Early)
        Assertions.assertFalse(game.isEarlyGame());
    }

    @Test
    public void earlyGameQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b1, null, Tile.r1, Tile.y2, null);
        game.clues = 1;

        // Setup hands
        Tile at5 = new Tile("b", 5);
        at5.hintedIdentity.value = 5;
        alice.hand = Tile.hand(null, null, null, Tile.b1, at5); //b1 just to avoid the chop going whack
        alice.updateChopPosition();

        Tile bt3 = new Tile("w", 1);
        bt3.hintedIdentity.value = 1;
        Clue bc3 = new Clue(ClueType.PLAY, 1);
        bc3.possibleSuits.addAll(List.of("g", "w"));
        bt3.information.add(bc3);
        Tile bt4 = new Tile("g", 1);
        bt4.hintedIdentity.value = 1;
        Clue bc4 = new Clue(ClueType.PLAY, 1);
        bt4.information.add(bc4);
        bc4.possibleSuits.addAll(List.of("g", "w"));
        bob.hand = Tile.hand(Tile.r1, Tile.g2, bt3, bt4, Tile.y2);
        bob.updateChopPosition();

        //cathy g3 w4 w4, w5 g5 (clued)
        Tile ct4 = new Tile("w", 5);
        ct4.hintedIdentity.value = 5;
        Clue cc4 = new Clue(ClueType.FIVE_SAVE, 5);
        ct4.information.add(cc4);
        Tile ct5 = new Tile("g", 5);
        ct5.hintedIdentity.value = 5;
        Clue cc5 = new Clue(ClueType.FIVE_SAVE, 5);
        ct5.information.add(cc5);
        cathy.hand = Tile.hand(Tile.g3, Tile.w4, Tile.w4, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice perform? - A: Clue Bob on g (for a g 2 DELAYED_PLAY)
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.DELAYED_PLAY, clue.clueType);
        Assertions.assertEquals("g", clue.suit);
    }

    @Test
    public void earlyGameQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(null, Tile.g1, Tile.r1, null, Tile.w1);
        game.clues = 1;

        // Setup hands
        alice.hand = Tile.hand(null, null, null, Tile.g1); //for chop
        alice.updateChopPosition();

        Tile bt2 = new Tile("r", 2);
        bt2.hintedIdentity.suit = "r";
        Clue bc2 = new Clue(ClueType.PLAY, 2, "r");
        bt2.information.add(bc2);
        Tile bt3 = new Tile("r", 3);
        bt3.hintedIdentity.suit = "r";
        Clue bc3 = new Clue(ClueType.DELAYED_PLAY, 3, "r");
        bt3.information.add(bc3);
        bob.hand = Tile.hand(Tile.w4, bt2, bt3, Tile.w1);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.g1, Tile.w2, Tile.r2, Tile.y4);
        cathy.updateChopPosition();

        donald.hand = Tile.hand(Tile.g4, Tile.r1, Tile.g5, Tile.b4);
        donald.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should alice perform? - A: Clue Cathy on w (for a w 2 PLAY clue)
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, action);
        Clue clue = ((ClueAction)action).intendedClue;
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals("w", clue.suit);
    }

    @Test
    public void earlyGameQ4()
    {
        // Setup game
        //inplay b1 g3 r2 y1
        //1 clue
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b1, Tile.g3, Tile.r2, Tile.y1, null);
        game.clues = 1;

        // Setup hands
        alice.hand = Tile.hand(null, null, null, Tile.b1); //b1 for chop
        alice.updateChopPosition();

        bob.hand = Tile.hand(Tile.r4, Tile.r1, Tile.w3, Tile.g1);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.w2, Tile.b4, Tile.b4, Tile.r2);
        cathy.updateChopPosition();

        donald.hand = Tile.hand(Tile.w5, Tile.g3, Tile.b1, Tile.r1);
        donald.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice take? - A: Discard
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }
}
