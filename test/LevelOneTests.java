import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LevelOneTests
{
    // Chop Questions
    @Test
    public void chopQ1()
    {
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);

        alice.hand = new Tile[]{Tile.b5, Tile.y1, Tile.y2, Tile.y3, Tile.g1};
        alice.updateChopPosition();

        Assertions.assertEquals(4, alice.chopPosition);
    }

    @Test
    public void chopQ2()
    {
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);

        Tile t3 = new Tile(new Clue(ClueType.NULL, 2, "g"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 4, "w"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 1, "b"));

        alice.hand = Tile.hand(Tile.y2, Tile.b3, t3, t4, t5);
        alice.updateChopPosition();

        Assertions.assertEquals(1, alice.chopPosition);
    }

    @Test
    public void chopQ3()
    {
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);

        Tile t2 = new Tile(new Clue(ClueType.NULL, 3, "r"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 4, "r"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 5, "r"));

        alice.hand = Tile.hand(Tile.y1, t2, Tile.b2, t4, t5);
        alice.updateChopPosition();

        Assertions.assertEquals(2, alice.chopPosition);
    }

    @Test
    public void chopQ4()
    {
        Player alice = new Player(5, ChopMethod.NON_CLUED, false);

        Tile t1 = new Tile(new Clue(ClueType.NULL, 4, "g"));
        Tile t2 = new Tile(new Clue(ClueType.NULL, 3, "w"));
        Tile t3 = new Tile(new Clue(ClueType.NULL, 4, "w"));
        Tile t4 = new Tile(new Clue(ClueType.NULL, 5, "g"));
        Tile t5 = new Tile(new Clue(ClueType.NULL, 5, "b"));

        alice.hand = new Tile[] { t1, t2, t3, t4, t5 };
        alice.updateChopPosition();

        Assertions.assertEquals(-1, alice.chopPosition);
    }

    //Play Clue Questions
    // TODO: Requries the refactor so that clue interpretation is within the player's purview, rather than Main's
    @Test
    public void playQ1()
    {
        Player alice = new Player();
        Player bob = new Player();

        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b1, Tile.g2, Tile.r5, Tile.y3, Tile.w2);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.g1, Tile.g2, Tile.b2, Tile.y4, Tile.w5);
        bob.updateChopPosition();

        new ClueAction(1, new Clue(ClueType.PLAY, "b")).execute(game, alice);
        Assertions.assertTrue(bob.hand[0].information.isEmpty());
        Assertions.assertTrue(bob.hand[1].information.isEmpty());
        Assertions.assertTrue(bob.hand[3].information.isEmpty());
        Assertions.assertTrue(bob.hand[4].information.isEmpty());
        Assertions.assertEquals(1, bob.hand[2].information.size());

        Clue clue = bob.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals("b", clue.suit);
        Assertions.assertEquals(2, clue.value);
    }

    @Test
    public void playQ2()
    {
        Player alice = new Player();
        Player bob = new Player();

        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b3, Tile.g3, Tile.r3, Tile.y4, Tile.w2);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.b1, Tile.g2, Tile.r3, Tile.y4, Tile.w5);
        bob.updateChopPosition();

        new ClueAction(1, new Clue(ClueType.PLAY, 4)).execute(game, alice);
        Assertions.assertTrue(bob.hand[0].information.isEmpty());
        Assertions.assertTrue(bob.hand[1].information.isEmpty());
        Assertions.assertTrue(bob.hand[2].information.isEmpty());
        Assertions.assertTrue(bob.hand[4].information.isEmpty());
        Assertions.assertEquals(1, bob.hand[3].information.size());

        Clue clue = bob.hand[3].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals(4, clue.value);
        Assertions.assertEquals(List.of(new String[] { "b", "g", "r" }), clue.possibleSuits);
    }

    //Delayed Play Clue Questions
    // TODO: Implement delayed play clue giving and interpretation

    //5-Save Clue Questions
    @Test
    public void fiveSaveQ1()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();

        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b1, null, Tile.r1, Tile.y2, Tile.w1);
        game.discarded.put(Tile.g3, 1);

        Tile ct3 = new Tile(new Clue(ClueType.NULL, 3, "g"));
        Tile ct4 = new Tile(new Clue(ClueType.NULL, 2, "g"));

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.w4, Tile.g4, Tile.y5, Tile.y1);
        cathy.hand = Tile.hand(Tile.y4, Tile.g4, ct3, ct4);
        donald.hand = Tile.hand(Tile.w5, Tile.b5, Tile.w3, Tile.w3);

        bob.updateChopPosition();
        cathy.updateChopPosition();
        donald.updateChopPosition();

        alice.enumerateActions();
        alice.prioritiseActions();

        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }

    @Test
    public void fiveSaveQ2()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();

        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b3, null, Tile.r1, null, Tile.w2);
        game.clues = 1;

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.r1, Tile.w5, Tile.g5, Tile.y2);
        cathy.hand = Tile.hand(Tile.r4, Tile.y5, new Tile(new Clue(ClueType.NULL, 5, "b")), new Tile(new Clue(ClueType.NULL, 2, "y")));
        donald.hand = Tile.hand(Tile.w4, Tile.b1, Tile.r5, Tile.w3);

        bob.updateChopPosition();
        cathy.updateChopPosition();
        donald.updateChopPosition();

        alice.enumerateActions();
        alice.prioritiseActions();

        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertEquals(2, ((ClueAction)alice.possibleActions.get(0)).targetPlayer);
        Assertions.assertEquals(new Clue(ClueType.FIVE_SAVE, 5), ((ClueAction)alice.possibleActions.get(0)).intendedClue);
    }

    @Test
    public void twoSaveQ1()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();

        Tile ct2 = new Tile(1, "y");
        ct2.hintedIdentity.suit = "y";
        ct2.information.add(new Clue(ClueType.PLAY, 1, "y"));

        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b1, null, Tile.r3, null, Tile.w2);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.b1, Tile.g3, Tile.r3, Tile.b2, Tile.w5);
        cathy.hand = Tile.hand(Tile.y5, ct2, Tile.r3, Tile.r2, Tile.b2);

        bob.updateChopPosition();
        cathy.updateChopPosition();

        new ClueAction(1, new Clue(ClueType.NULL, 2)).execute(game, alice);
        Assertions.assertTrue(bob.hand[0].information.isEmpty());
        Assertions.assertTrue(bob.hand[1].information.isEmpty());
        Assertions.assertTrue(bob.hand[2].information.isEmpty());
        Assertions.assertTrue(bob.hand[4].information.isEmpty());
        Assertions.assertEquals(2, bob.hand[3].information.size()); //TODO: delay clues

        boolean playFirst = (bob.hand[3].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[3].information.get(playFirst ? 0 : 1);
        Clue delayClue = bob.hand[3].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("b", playClue.suit);
        Assertions.assertEquals(2, playClue.value);

        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals("y", delayClue.suit);
        Assertions.assertEquals(2, delayClue.value);
    }

    @Test
    public void twoSaveQ2()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g2, null, Tile.y1, null);

        Tile bt1 = new Tile(1, "r");
        bt1.hintedIdentity.value = 1;
        Clue bc1 = new Clue(ClueType.PLAY, 1);
        bc1.possibleSuits.addAll(List.of("b", "r", "w"));
        bt1.information.add(bc1);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(bt1, Tile.g1, Tile.r3, Tile.y4, Tile.w2);
        cathy.hand = Tile.hand(Tile.g5, Tile.b3, Tile.g4, Tile.w3, Tile.w3);
        bob.updateChopPosition();
        cathy.updateChopPosition();

        new ClueAction(1, new Clue(ClueType.NULL, 2)).execute(game, alice);
        Assertions.assertTrue(bob.hand[1].information.isEmpty());
        Assertions.assertTrue(bob.hand[2].information.isEmpty());
        Assertions.assertTrue(bob.hand[3].information.isEmpty());
        Assertions.assertEquals(1, bob.hand[0].information.size());
        Assertions.assertEquals(3, bob.hand[4].information.size()); //TODO: delay clues

        Clue playClue = null;
        Clue delayClue = null;
        Clue saveClue = null;
        for (Clue clue : bob.hand[3].information)
        {
            if (clue.clueType == ClueType.PLAY)
                playClue = clue;
            else if (clue.clueType == ClueType.DELAYED_PLAY)
                delayClue = clue;
            else if (clue.clueType == ClueType.TWO_SAVE)
                saveClue = clue;
        }

        Assertions.assertNotNull(playClue);
        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("y", playClue.suit);
        Assertions.assertEquals(2, playClue.value);

        Assertions.assertNotNull(delayClue);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals(2, delayClue.value);
        Assertions.assertEquals(List.of(new String[] { "b", "r", "w" }), delayClue.possibleSuits);

        Assertions.assertNotNull(saveClue);
        Assertions.assertEquals(ClueType.TWO_SAVE, saveClue.clueType);
        Assertions.assertEquals(2, saveClue.value);
        Assertions.assertEquals(List.of(new String[] { "b", "r", "w" }), saveClue.possibleSuits);
    }

    @Test
    public void twoSaveQ3()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, null, null, null, Tile.w1);

        Tile at4 = new Tile(Tile.r5);
        at4.hintedIdentity.value = 5;
        Tile at5 = new Tile(Tile.w5);
        at5.hintedIdentity.value = 5;
        Clue ac45 = new Clue(ClueType.FIVE_SAVE, 5);
        ac45.possibleSuits.addAll(List.of("b", "g", "r", "w"));
        at4.information.add(ac45);
        at5.information.add(ac45);

        alice.hand = new Tile[] { null, null, null, at4, at5 };
        bob.hand = Tile.hand(Tile.r4, Tile.w3, Tile.y2, Tile.r2, Tile.g2);
        cathy.hand = Tile.hand(Tile.y5, Tile.g2, Tile.g1, Tile.g1, Tile.y2);

        bob.updateChopPosition();
        cathy.updateChopPosition();
        alice.enumerateActions();
        alice.prioritiseActions();

        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }

    @Test
    public void criticalSaveQ1()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, null, Tile.r1, Tile.y2, Tile.w3);

        Tile bt4 = new Tile(4, "r");
        bt4.hintedIdentity.value = 4;
        Tile bt5 = new Tile(4, "w");
        bt5.hintedIdentity.value = 4;
        bt5.information.add(new Clue(ClueType.PLAY, 4, "w"));
        Tile ct2 = new Tile(new Clue(ClueType.PLAY, 2, "r"));

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.b2, Tile.g2, Tile.g3, bt4, bt5);
        cathy.hand = Tile.hand(Tile.w4, ct2, Tile.y1, Tile.y2, Tile.y4);
        bob.updateChopPosition();
        cathy.updateChopPosition();

        new ClueAction(1, new Clue(ClueType.NULL, 3));

        // y 3 PLAY, r 3 DELAYED_PLAY, g 3 CRITICAL_SAVE
        Assertions.assertTrue(bob.hand[0].information.isEmpty());
        Assertions.assertTrue(bob.hand[1].information.isEmpty());
        Assertions.assertEquals(3, bob.hand[2].information.size()); //TODO: delay clues
        Assertions.assertTrue(bob.hand[3].information.isEmpty());
        Assertions.assertEquals(1, bob.hand[4].information.size());

        Clue playClue = null;
        Clue delayClue = null;
        Clue saveClue = null;
        for (Clue clue : bob.hand[2].information)
        {
            if (clue.clueType == ClueType.PLAY)
                playClue = clue;
            else if (clue.clueType == ClueType.DELAYED_PLAY)
                delayClue = clue;
            else if (clue.clueType == ClueType.TWO_SAVE)
                saveClue = clue;
        }

        Assertions.assertNotNull(playClue);
        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("y", playClue.suit);
        Assertions.assertEquals(3, playClue.value);

        Assertions.assertNotNull(delayClue);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals("r", delayClue.suit);
        Assertions.assertEquals(3, delayClue.value);

        Assertions.assertNotNull(saveClue);
        Assertions.assertEquals(ClueType.TWO_SAVE, saveClue.clueType);
        Assertions.assertEquals("g", saveClue.suit);
        Assertions.assertEquals(2, saveClue.value);
    }

    @Test
    public void criticalSaveQ2()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g1, Tile.r4, null, null);
        game.discarded.put(Tile.w3, 1);
        game.discarded.put(Tile.w4, 1);
        game.clues = 2;

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[bob.handSize];
        cathy.hand = Tile.hand(Tile.r1, Tile.r1, Tile.w4, Tile.w3, Tile.w5);
        cathy.updateChopPosition();

        // Stage 1: Alice should 5-save Cathy
        alice.enumerateActions();
        alice.prioritiseActions();

        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action aliceAction = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, aliceAction);
        Assertions.assertEquals(ClueType.FIVE_SAVE, ((ClueAction)aliceAction).intendedClue.clueType);

        // Stage 2: Bob should critical-save the 3 with a purple clue (also getting the 4 off the chop)
        aliceAction.execute(game, alice);
        bob.enumerateActions();
        bob.prioritiseActions();

        Assertions.assertFalse(bob.possibleActions.isEmpty());
        Action bobAction = bob.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, bobAction);
        ClueAction bobClue = (ClueAction)bobAction;
        Assertions.assertEquals(ClueType.CRITICAL_SAVE, bobClue.intendedClue.clueType);
        Assertions.assertEquals("w", bobClue.intendedClue.suit);
    }

    @Test
    public void criticalSaveQ3()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b4, Tile.g1, Tile.r2, null, Tile.w3);
        game.discarded.put(Tile.y3, 1);
        game.discarded.put(Tile.y4, 1);

        // Setup Hands
        Tile bt5 = new Tile(3, "r");
        bt5.hintedIdentity.value = 3;
        bt5.information.add(new Clue(ClueType.PLAY, 3, "r"));

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, null, null, Tile.y4, bt5);
        cathy.hand = Tile.hand(Tile.g3, Tile.g4, Tile.y3, Tile.g1, Tile.w1);

        bob.updateChopPosition();
        cathy.updateChopPosition();

        // Q: ALice clues Bob on Yellow - A: y 1 PLAY, y 4 CRITICAL_SAVE
        new ClueAction(1, new Clue(ClueType.NULL, "y")).execute(game, alice);

        Assertions.assertEquals(2, bob.hand[3].information.size());
        Assertions.assertEquals(1, bob.hand[4].information.size());

        boolean playFirst = (bob.hand[3].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[3].information.get(playFirst ? 0 : 1);
        Clue saveClue = bob.hand[3].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("y", playClue.suit);
        Assertions.assertEquals(1, playClue.value);

        Assertions.assertEquals(ClueType.CRITICAL_SAVE, saveClue.clueType);
        Assertions.assertEquals("y", saveClue.suit);
        Assertions.assertEquals(4, saveClue.value);
    }

    @Test
    public void criticalSaveQ4()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g2, null, Tile.y2, Tile.w2);
        game.discarded.put(Tile.r2, 1);

        // Setup Hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, null, null, null, Tile.r2);
        cathy.hand = Tile.hand(Tile.w1, Tile.g1, Tile.b2, Tile.b1, Tile.g2);

        bob.updateChopPosition();
        cathy.updateChopPosition();

        // Q: ALice clues Bob on Red - A: r 2 CRITICAL_SAVE
        new ClueAction(1, new Clue(ClueType.NULL, "r")).execute(game, alice);

        Assertions.assertEquals(2, bob.hand[4].information.size());
        boolean playFirst = (bob.hand[4].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[4].information.get(playFirst ? 0 : 1);
        Clue saveClue = bob.hand[4].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals("r", playClue.suit);
        Assertions.assertEquals(1, playClue.value);

        Assertions.assertEquals(ClueType.CRITICAL_SAVE, saveClue.clueType);
        Assertions.assertEquals("r", saveClue.suit);
        Assertions.assertEquals(2, saveClue.value);
    }

    @Test
    public void focusQ1()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        bob.handSize = 5;

        // Setup Hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.w1, Tile.w2, Tile.w3, Tile.w4, Tile.b2);
        bob.updateChopPosition();

        // Q: Alice clues Bob on White - A: Tile 1 (index 0) is focussed (new)
        Clue clue = new Clue(ClueType.NULL, "w");
        Assertions.assertEquals(0, bob.focusIndex(clue));
    }

    @Test
    public void focusQ2()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        bob.handSize = 5;

        // Setup Hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.g5, Tile.g2, Tile.g3, Tile.g4, Tile.g1);
        bob.updateChopPosition();

        // Q: Alice clues Bob on White - A: Tile 5 (index 4) is focussed (new + chop)
        Clue clue = new Clue(ClueType.NULL, "g");
        Assertions.assertEquals(4, bob.focusIndex(clue));
    }

    @Test
    public void focusQ3()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        bob.handSize = 5;

        // Setup Hands
        Tile bt2 = new Tile(2, "b");
        bt2.hintedIdentity.value = 2;
        Tile bt5 = new Tile(2, "r");
        bt5.hintedIdentity.value = 2;
        Clue bc5 = new Clue(ClueType.TWO_SAVE, 2);
        bt5.information.add(bc5);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, bt2, Tile.b1, null, bt5);
        bob.updateChopPosition();

        // Q: Alice clues Bob on Blue - A: Tile 3 (index 2) is focussed (new)
        Clue clue = new Clue(ClueType.NULL, "b");
        Assertions.assertEquals(2, bob.focusIndex(clue));
    }

    @Test
    public void focusQ4()
    {
        // Setup Game
        Player alice = new Player();
        Player bob = new Player();
        bob.handSize = 5;

        // Setup Hands
        Tile bt4 = new Tile(5, "b");
        bt4.hintedIdentity.value = 5;
        Tile bt5 = new Tile(2, "r");
        bt5.hintedIdentity.value = 2;
        Clue bc5 = new Clue(ClueType.TWO_SAVE, 2);
        bt5.information.add(bc5);

        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, Tile.r3, Tile.r1, bt4, bt5);
        bob.updateChopPosition();

        // Q: Alice clues Bob on Red - A: Tile 3 (index 2) is focussed (new + chop)
        Clue clue = new Clue(ClueType.NULL, "r");
        Assertions.assertEquals(2, bob.focusIndex(clue));
    }

    
}
