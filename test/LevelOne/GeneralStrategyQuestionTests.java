import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GeneralStrategyQuestionTests
{
    @Test
    public void generalQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b1, null, null, Tile.y4, Tile.w3);
        game.discarded.put(Tile.g3, 1);
        game.discarded.put(Tile.g4, 1);
        game.discarded.put(Tile.w4, 1);

        // Setup hands
        Tile at1 = new Tile(2, "y");
        at1.hintedIdentity.suit = "y";
        at1.information.add(new Clue(ClueType.PLAY, 2, "y"));
        alice.hand = Tile.hand(at1, null, null, null, Tile.b1); //b1 for chop
        alice.updateChopPosition();

        bob.hand = Tile.hand(Tile.w2, Tile.b1, Tile.r4, Tile.y1, Tile.w3);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.r3, Tile.r3, Tile.y1, Tile.y5, Tile.b5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: Should Alice play her 2, or save Cathy's 5b? - A: Play her 2 (in slot 1/index 0) (Bob can save the 5)
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action action = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(PlayAction.class, action);
        Assertions.assertEquals(0, ((PlayAction)action).targetTile);
    }

    @Test
    public void generalQ2()
    {
        // Setup game
        //g2 r1 y2 w3
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g2, Tile.r1, Tile.y2, Tile.w3);

        // Setup hands
        Tile at5 = new Tile(2, "b");
        at5.hintedIdentity.value = 2;
        at5.information.add(new Clue(ClueType.TWO_SAVE, 2));
        alice.hand = Tile.hand(null, null, null, null, at5);
        alice.updateChopPosition();

        bob.hand = Tile.hand(Tile.b4, Tile.w2, Tile.r1, Tile.g2, Tile.b4);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.r3, Tile.g3, Tile.y2, Tile.w1, Tile.r5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: Is it the early game? What are all of Alice's possible clues? Which clue should Alice give?
        // A: Yes; Clue Cathy on g (g 3 PLAY), clue Cathy on 5 (FIVE_SAVE); Clue Cathy on g
        Assertions.assertTrue(game.isEarlyGame());
        Assertions.assertEquals(2, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));

        Assertions.assertEquals(2, ((ClueAction)alice.possibleActions.get(0)).targetPlayer);
        Assertions.assertEquals(2, ((ClueAction)alice.possibleActions.get(1)).targetPlayer);
        Clue playClue = ((ClueAction)alice.possibleActions.get(0)).intendedClue;
        Clue saveClue = ((ClueAction)alice.possibleActions.get(1)).intendedClue;

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType); //TODO: need to re-think prioritisation of possible actions
        Assertions.assertEquals("g", playClue.suit);
        Assertions.assertEquals(ClueType.FIVE_SAVE, saveClue.clueType);
        Assertions.assertEquals(5, saveClue.value);
    }

    @Test
    public void generalQ3()
    {
        // Setup game
        //inplay g3 r3 y1 w3
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g3, Tile.r3, Tile.y1, Tile.w3);

        // Setup hands
        alice.hand = new Tile[5];

        bob.hand = Tile.hand(Tile.y3, Tile.r2, Tile.g2, Tile.r1, Tile.b5);
        bob.updateChopPosition();

        //cathy b4, y2 (clued y 2 PLAY), w4 r3 w1
        Tile ct2 = new Tile(2, "y");
        ct2.hintedIdentity.suit = "y";
        ct2.information.add(new Clue(ClueType.PLAY, 2, "y"));
        cathy.hand = Tile.hand(Tile.b4, ct2, Tile.w4, Tile.r3, Tile.w1);

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What are all of Alice's possible clues? Which clue should Alice give?
        // A: Clue Bob on 5 (FIVE_SAVE), clue Bob on y (y 3 DELAYED_PLAY), clue Bob on 3 (y 3 DELAYED_PLAY); Clue Bob on 5
        Assertions.assertEquals(4, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(2));

        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(0)).targetPlayer);
        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(1)).targetPlayer);
        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(2)).targetPlayer);
        Clue saveClue = ((ClueAction)alice.possibleActions.get(0)).intendedClue;
        Clue delaySuitClue = ((ClueAction)alice.possibleActions.get(1)).intendedClue;
        Clue delayValueClue = ((ClueAction)alice.possibleActions.get(2)).intendedClue;

        Assertions.assertEquals(ClueType.FIVE_SAVE, saveClue.clueType);
        Assertions.assertEquals(5, saveClue.value);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, delaySuitClue.clueType);
        Assertions.assertEquals("y", delaySuitClue.suit);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayValueClue.clueType);
        Assertions.assertEquals(3, delayValueClue.value);
    }

    @Test
    public void generalQ4()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b1, Tile.g4, Tile.r4, Tile.y1, Tile.w1);

        // Setup hands
        alice.hand = new Tile[5];
        bob.hand = Tile.hand(Tile.r3, Tile.b2, Tile.y1, Tile.w1, Tile.w1);
        bob.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What are all of Alice's possible clues? Which clue should Alice give?
        // A: Clue Bob on b (b 2 PLAY), clue Bob on 2 (b 2 PLAY); Clue Bob on b
        Assertions.assertEquals(3, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));

        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(0)).targetPlayer);
        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(1)).targetPlayer);
        Clue suitClue = ((ClueAction)alice.possibleActions.get(0)).intendedClue;
        Clue valueClue = ((ClueAction)alice.possibleActions.get(1)).intendedClue;

        Assertions.assertEquals(ClueType.PLAY, suitClue.clueType);
        Assertions.assertEquals("b", suitClue.suit);
        Assertions.assertEquals(ClueType.PLAY, valueClue.clueType);
        Assertions.assertEquals(2, valueClue.value);
    }

    @Test
    public void generalQ5()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b4, Tile.g2, Tile.r5, null, Tile.w1);

        // Setup hands
        alice.hand = new Tile[5];
        bob.hand = Tile.hand(Tile.y5, Tile.b4, Tile.g3, Tile.y3, Tile.r1);
        bob.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What are all of Alice's possible clues? Which clue should Alice give?
        // A: Clue Bob on g (g 3 PLAY), clue Bob on 3 (g 3 PLAY); Clue Bob on g
        Assertions.assertEquals(3, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));

        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(0)).targetPlayer);
        Assertions.assertEquals(1, ((ClueAction)alice.possibleActions.get(1)).targetPlayer);
        Clue valueClue = ((ClueAction)alice.possibleActions.get(0)).intendedClue;
        Clue suitClue = ((ClueAction)alice.possibleActions.get(1)).intendedClue;

        Assertions.assertEquals(ClueType.PLAY, valueClue.clueType);
        Assertions.assertEquals(3, valueClue.value);
        Assertions.assertEquals(ClueType.PLAY, suitClue.clueType);
        Assertions.assertEquals("g", suitClue.suit);
    }
}
