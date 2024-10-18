import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PromptQuestionTests
{
    @Test
    public void promptQ1()
    {
        // Setup game
        //inplay g3 r1 w5
        //discard g1 g4 r3 b3
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g3, Tile.r1, null, Tile.w5);
        game.discarded.put(Tile.g1, 1);
        game.discarded.put(Tile.g4, 1);
        game.discarded.put(Tile.r3, 1);
        game.discarded.put(Tile.b3, 1);

        // Setup hands
        alice.hand = new Tile[5];

        Tile bt3 = new Tile("r", 2);
        bt3.hintedIdentity.value = 2;
        Tile bt4 = new Tile("b", 2);
        bt4.hintedIdentity.value = 2;
        Clue bc1 = new Clue(ClueType.TWO_SAVE, 2);
        bc1.possibleSuits.addAll(List.of("b", "y"));
        Clue bc2 = new Clue(ClueType.PLAY, 2, "r");
        bt3.information.addAll(List.of(bc1, bc2));
        bt4.information.addAll(List.of(bc1, bc2));
        bob.hand = Tile.hand(null, null, bt3, bt4, null);

        Tile ct4 = new Tile("g", 4);
        ct4.hintedIdentity.value = 4;
        Tile ct5 = new Tile("r", 5);
        ct5.hintedIdentity.value = 5;
        cathy.hand = Tile.hand(Tile.b4, Tile.y3, Tile.r3, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice clues Cathy on r, Bob enumerates and prioritises his possible actions
        new ClueAction(2, new Clue(ClueType.DELAYED_PLAY, "r")).execute(game, alice);
        bob.updateTileClues();

        // Q: What is Bob's interpretation of Alice's clue? - A: Bob does not think anything, as Alice's clue is a save clue
        Assertions.assertEquals(2, bob.hand[2].information.size());
        Assertions.assertEquals(2, bob.hand[3].information.size());
    }

    @Test
    public void promptQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g1, Tile.r1, Tile.y2, Tile.w1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(Tile.y4, Tile.b4, Tile.w1, Tile.g1, Tile.w4);
        bob.updateChopPosition();

        Tile ct3 = new Tile("y", 3);
        ct3.hintedIdentity.value = 3;
        Tile ct4 = new Tile("b", 3);
        ct4.hintedIdentity.value = 3;
        Tile ct5 = new Tile("w", 5);
        ct5.hintedIdentity.value = 5;
        cathy.hand = Tile.hand(Tile.y2, Tile.r1, ct3, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What two possible clues can Alice give? Which one is better?
        // A: Clue Bob on y (y 4 DELAYED_PLAY), clue Bob on b (b 4 DELAYED_PLAY); clue Bob on b because it PROMPTs Cathy to play both her 3s
        Assertions.assertEquals(2, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));

        Clue firstClue = ((ClueAction)alice.possibleActions.get(0)).intendedClue;
        Clue secondClue = ((ClueAction)alice.possibleActions.get(1)).intendedClue;
        Assertions.assertEquals(ClueType.DELAYED_PLAY, firstClue.clueType);
        Assertions.assertEquals("b", firstClue.suit);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, secondClue.clueType);
        Assertions.assertEquals("y", secondClue.suit);

        Assertions.assertEquals(1, cathy.hand[2].information.size());
        Assertions.assertTrue(cathy.hand[2].hasOnlyPlayClues());
        Assertions.assertTrue(cathy.hand[3].hasOnlyPlayClues());

        Clue clue = cathy.hand[2].information.get(0);
        Assertions.assertEquals(clue.clueType, ClueType.PLAY);
        Assertions.assertEquals("b", clue.suit);
        Assertions.assertEquals(3, clue.value);

        clue = cathy.hand[3].information.get(0);
        Assertions.assertEquals(clue.clueType, ClueType.PLAY);
        Assertions.assertEquals("b", clue.suit);
        Assertions.assertEquals(3, clue.value);
    }

    @Test
    public void promptQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g1, null, null, Tile.w1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        alice.updateChopPosition();

        Tile bt5 = new Tile("r", 5);
        bt5.hintedIdentity.value = 5;
        bob.hand = Tile.hand(Tile.b1, Tile.w4, Tile.b2, Tile.g1, bt5);
        bob.updateChopPosition();

        Tile ct4 = new Tile("w", 3);
        ct4.hintedIdentity.suit = "w";
        Tile ct5 = new Tile("w", 2);
        ct5.hintedIdentity.suit = "w";
        cathy.hand = Tile.hand(Tile.g4, Tile.b1, Tile.g4, ct4, ct5);
        bob.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: Alice wants to Play clue Bob's 4w, what clue should Alice give? - A: Clue Cathy on 3 (PROMPT on her 2)
        Assertions.assertEquals(2, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));

        ClueAction delayAction = (ClueAction)alice.possibleActions.get(0);
        ClueAction playAction = (ClueAction)alice.possibleActions.get(1);
        Assertions.assertEquals(2, delayAction.targetPlayer);
        Assertions.assertEquals(2, playAction.targetPlayer);

        Clue delayClue = delayAction.intendedClue;
        Clue playClue = playAction.intendedClue;
        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals(3, delayClue.value);
        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals(2, playClue.value);

        //TODO: execute the delay play clue and confirm Cathy has been PROMPTed to play her 2
    }
}
