import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

public class LevelOneTests
{
    // Chop Questions
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

    //Play Clue Questions
    @Test
    public void playQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b1, Tile.g2, Tile.r5, Tile.y3, Tile.w2);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, null, Tile.b2, null, null);

        // Take action - Alice clues Bob on b
        new ClueAction(1, new Clue(ClueType.PLAY, "b")).execute(game, alice);

        // Q: What notes does Bob make? - A: b 2 PLAY
        Assertions.assertEquals(1, bob.hand[2].information.size());
        Clue clue = bob.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals("b", clue.suit);
        Assertions.assertEquals(2, clue.value);
    }

    @Test
    public void playQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Game game = new Game(0, alice, bob);
        game.inPlay = Tile.hand(Tile.b3, Tile.g3, Tile.r3, Tile.y4, Tile.w2);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(null, null, null, Tile.b4, null);

        // Take action - Alice clues Bob on 4
        new ClueAction(1, new Clue(ClueType.PLAY, 4)).execute(game, alice);

        // Q: What notes does Bob make? - A: [b, g, r] 4 PLAY
        Assertions.assertEquals(1, bob.hand[3].information.size());
        Clue clue = bob.hand[3].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals(4, clue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(clue.possibleSuits, List.of("b", "g", "r")));
    }

    //Delayed Play Clue Questions
    // TODO: Implement delayed play clue giving and interpretation
    @Test
    public void delayPlayQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b2, Tile.g4, Tile.r1, Tile.y4, Tile.w3);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[alice.handSize];
        cathy.hand = Tile.hand(null, null, Tile.r3, null);
        donald.hand = Tile.hand(null, Tile.r2, null, null);

        // Take actions - Alice clues Donald on r, Bob clues Cathy on r, Cathy enumerates and prioritises her possible actions
        new ClueAction(3, new Clue(ClueType.PLAY, "r")).execute(game, alice);
        new ClueAction(2, new Clue(ClueType.PLAY, "r")).execute(game, bob);
        cathy.enumerateActions();
        cathy.prioritiseActions();

        // Q: What does Cathy note? Is it DELAYED_PLAY? What action should Cathy take? - A: r 3 DELAYED_PLAY; yes (obviously); Discard
        Assertions.assertEquals(1, cathy.hand[2].information.size());
        Clue clue = cathy.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, clue.clueType); //TODO: delay clues
        Assertions.assertEquals("r", clue.suit);
        Assertions.assertEquals(3, clue.value);

        Assertions.assertFalse(cathy.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, cathy.possibleActions.get(0));
    }

    @Test
    public void delayPlayQ2()
    {
        // Setup game
            //inplay b2 y1 w2

        // Setup hands
            //bob b3
            //cathy r1 g1 (clued)

        // Take action - Alice clues Bob on 3

        // Q: What note does Bob make? For Bob, is this a Delayed Play Clue? What action should Bob take?
        // A: [b, w] 3 PLAY; No (no connecting tiles); Bob should play the 3
    }

    @Test
    public void delayPlayQ3()
    {
        // Setup game
            //inplay b2 r3 y1 w3

        // Setup hands
            //bob b4
            //cathy r1, y2 y3 (clued), g5 (clued)
            //donald w5 g2 w1, b3 (clued)

        // Take action - Alice clues Bob on 4

        // Q: What note does Bob make? For Bob, is this a Delayed Play Clue? What action should Bob take?
        // A: [r, w] 4 PLAY, [b, y] 4 DELAYED_PLAY; Maybe; Discard
    }

    //5-Save Clue Questions
    @Test
    public void fiveSaveQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b1, null, Tile.r1, Tile.y2, Tile.w1);
        game.discarded.put(Tile.g3, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(Tile.w4, Tile.g4, Tile.y5, Tile.y1);
        bob.updateChopPosition();

        Tile ct3 = new Tile( 3, "g");
        ct3.hintedIdentity.suit = "g";
        Tile ct4 = new Tile(2, "g");
        ct4.hintedIdentity.suit = "g";
        cathy.hand = Tile.hand(Tile.y4, Tile.g4, ct3, ct4);
        cathy.updateChopPosition();

        donald.hand = Tile.hand(Tile.w5, Tile.b5, Tile.w3, Tile.w3);
        donald.updateChopPosition();

        // Take actions - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice take? - A: Discard
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }

    @Test
    public void fiveSaveQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b3, null, Tile.r1, null, Tile.w2);
        game.clues = 1;

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(Tile.r1, Tile.w5, Tile.g5, Tile.y2);
        bob.updateChopPosition();

        Tile ct3 = new Tile(5, "b");
        ct3.hintedIdentity.value = 5;
        Tile ct4 = new Tile(2, "y");
        ct4.hintedIdentity.value = 2;
        cathy.hand = Tile.hand(Tile.r4, Tile.y5, ct3, ct4);
        cathy.updateChopPosition();

        donald.hand = Tile.hand(Tile.w4, Tile.b1, Tile.r5, Tile.w3);
        donald.updateChopPosition();

        // Take actions - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice take? - A: Clue Cathy on 5 to FIVE_SAVE her 5y
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        ClueAction clueAction = (ClueAction)alice.possibleActions.get(0);
        Assertions.assertEquals(2, clueAction.targetPlayer);
        Assertions.assertEquals(new Clue(ClueType.FIVE_SAVE, 5), clueAction.intendedClue);
    }

    // 2-Save Questions
    @Test
    public void twoSaveQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b1, null, Tile.r3, null, Tile.w2);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(null, null, null, Tile.y2, null);
        bob.updateChopPosition();

        Tile ct2 = new Tile(1, "y");
        ct2.hintedIdentity.value = 1;
        cathy.hand = Tile.hand(Tile.y5, ct2, Tile.r3, Tile.r2, Tile.b2);
        cathy.updateChopPosition();

        // Take action - Alice clues Bob on 2
        new ClueAction(1, new Clue(ClueType.NULL, 2)).execute(game, alice);

        // Q: What notes does Bob make? - A: b 2 PLAY, y 2 DELAYED_PLAY
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
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, Tile.g2, null, Tile.y1, null);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt1 = new Tile(1, "r");
        bt1.hintedIdentity.value = 1;
        Clue bc1 = new Clue(ClueType.PLAY, 1);
        bc1.possibleSuits.addAll(List.of("b", "r", "w"));
        bt1.information.add(bc1);
        bob.hand = Tile.hand(bt1, null, null, null, Tile.w2);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.g5, Tile.b3, Tile.g4, Tile.w3, Tile.w3);

        // Take action - Alice clues Bob on 2
        new ClueAction(1, new Clue(ClueType.NULL, 2)).execute(game, alice);

        // Q: For Bob, is this a Play Clue or a Save Clue? What note does Bob make?
        // A: Play clue(s); y 2 PLAY, [b, g, r] 2 DELAYED_PLAY, [b, g, r] 2 SAVE
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
        Assertions.assertTrue(CollectionUtils.containsAll(delayClue.possibleSuits, List.of("b", "r", "w")));

        Assertions.assertNotNull(saveClue);
        Assertions.assertEquals(ClueType.TWO_SAVE, saveClue.clueType);
        Assertions.assertEquals(2, saveClue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(saveClue.possibleSuits, List.of("b", "r", "w")));
    }

    @Test
    public void twoSaveQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, null, null, null, Tile.w1);

        // Setup hands
        Tile at4 = new Tile(Tile.b5);
        at4.hintedIdentity.value = 5;
        Tile at5 = new Tile(Tile.g5);
        at5.hintedIdentity.value = 5;
        Clue ac45 = new Clue(ClueType.FIVE_SAVE, 5);
        ac45.possibleSuits.addAll(List.of("b", "g", "r", "w"));
        at4.information.add(ac45);
        at5.information.add(ac45);
        alice.hand = new Tile[] { null, null, null, at4, at5 };

        bob.hand = Tile.hand(Tile.r4, Tile.w3, Tile.y2, Tile.r2, Tile.g2);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.y5, Tile.g2, Tile.g1, Tile.g1, Tile.y2);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice take? - A: Discard
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }

    // Critical Save Questions
    @Test
    public void criticalSaveQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(null, null, Tile.r1, Tile.y2, Tile.w3);
        game.discarded.put(Tile.g3, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt4 = new Tile(4, "r");
        bt4.hintedIdentity.value = 4;
        Tile bt5 = new Tile(4, "w");
        bt5.hintedIdentity.value = 4;
        bt5.information.add(new Clue(ClueType.PLAY, 4, "w"));
        bob.hand = Tile.hand(null, null, Tile.g3, bt4, bt5);
        bob.updateChopPosition();

        Tile ct2 = new Tile(new Clue(ClueType.PLAY, 2, "r"));
        cathy.hand = Tile.hand(Tile.w4, ct2, Tile.y1, Tile.y2, Tile.y4);

        // Take action - Alice clues Bob on 3
        new ClueAction(1, new Clue(ClueType.NULL, 3)).execute(game, alice);

        // Q: For Bob, is this a Play or a Save? What not does Bob make? - A: y 3 PLAY, r 3 DELAYED_PLAY, g 3 CRITICAL_SAVE
        Assertions.assertEquals(3, bob.hand[2].information.size()); //TODO: delay clues
        Assertions.assertEquals(0, bob.hand[3].information.size());
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
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g1, Tile.r4, null, null);
        game.discarded.put(Tile.w3, 1);
        game.discarded.put(Tile.w4, 1);
        game.clues = 2;

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[bob.handSize];

        cathy.hand = Tile.hand(Tile.r1, Tile.r1, Tile.w4, Tile.w3, Tile.w5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What action should Alice take? - A: 5-Save Cathy's 5w
        Assertions.assertFalse(alice.possibleActions.isEmpty());
        Action aliceAction = alice.possibleActions.get(0);
        Assertions.assertInstanceOf(ClueAction.class, aliceAction);
        Assertions.assertEquals(ClueType.FIVE_SAVE, ((ClueAction)aliceAction).intendedClue.clueType);

        // BONUS: Bob should critical-save the 3 with a white clue (also getting the 4 off the chop)
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
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b4, Tile.g1, Tile.r2, null, Tile.w3);
        game.discarded.put(Tile.y3, 1);
        game.discarded.put(Tile.y4, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt5 = new Tile(3, "r");
        bt5.hintedIdentity.value = 3;
        bt5.information.add(new Clue(ClueType.PLAY, 3, "r"));
        bob.hand = Tile.hand(null, null, null, Tile.y4, bt5);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.g3, Tile.g4, Tile.y3, Tile.g1, Tile.w1);

        // Take action - Alice clues Bob on y
        new ClueAction(1, new Clue(ClueType.NULL, "y")).execute(game, alice);

        // Q: For Bob, is this a Play or a Save? What card note will Bob make? - A: Both; y 1 PLAY, y 4 CRITICAL_SAVE
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
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, Tile.g2, null, Tile.y2, Tile.w2);
        game.discarded.put(Tile.r2, 1);

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        bob.hand = Tile.hand(null, null, null, null, Tile.r2);
        bob.updateChopPosition();

        cathy.hand = Tile.hand(Tile.w1, Tile.g1, Tile.b2, Tile.b1, Tile.g2);
        cathy.updateChopPosition();

        // Take action - Alice clues Bob on r
        new ClueAction(1, new Clue(ClueType.NULL, "r")).execute(game, alice);

        // Q: For Bob, is this a Play or Save? What note should Bob make? - A: Both; r 1 PLAY, r 2 CRITICAL_SAVE
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

    // Single Card Focus Questions
    @Test
    public void focusQ1()
    {
        // Setup hands
        Player alice = new Player();
        alice.hand = new Tile[alice.handSize];

        Player bob = new Player();
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

        Player bob = new Player();
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

        Player bob = new Player();
        Tile bt2 = new Tile(2, "b");
        bt2.hintedIdentity.value = 2;
        Tile bt5 = new Tile(2, "r");
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

        Player bob = new Player();
        Tile bt4 = new Tile(5, "b");
        bt4.hintedIdentity.value = 5;
        Tile bt5 = new Tile(2, "r");
        bt5.hintedIdentity.value = 2;
        Clue bc5 = new Clue(ClueType.TWO_SAVE, 2);
        bt5.information.add(bc5);
        bob.hand = Tile.hand(null, Tile.r3, Tile.r1, bt4, bt5);
        bob.updateChopPosition();

        // Q: Alice clues Bob on r. Which slot is focussed? - A: Tile 3 (index 2) (new + chop)
        Clue clue = new Clue(ClueType.NULL, "r");
        Assertions.assertEquals(2, bob.focusIndex(clue));
    }

    // Clue Interpretation Questions
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

        Tile dt3 = new Tile(3, "w");
        dt3.hintedIdentity.value = 3;
        Tile dt4 = new Tile(3, "r");
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

        Tile bt4 = new Tile(2, "r");
        bt4.hintedIdentity.value = 2;
        Tile bt5 = new Tile(5, "r");
        bt5.hintedIdentity.value = 5;
        bob.hand = Tile.hand(null, Tile.r4, Tile.r3, bt4, bt5);
        bob.updateChopPosition();

        // Take action - Alice clues Bob on r
        Clue clue = new Clue(ClueType.PLAY, "r");
        Assertions.assertEquals(2, bob.focusIndex(clue));
        new ClueAction(1, clue).execute(game, alice);

        // Q: Which slot is focussed? For Bob, is this a Play or a Save? What card not does Bob make?
        // A: Alot 2 (index 1) (new + chop) (checked above); Play; r 3 DELAYED_PLAY
        Assertions.assertEquals(1, bob.hand[2].information.size());
        Clue bobClue = bob.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, bobClue.clueType); //TODO: delay clues
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

        // Setup hands
        alice.hand = new Tile[alice.handSize];

        Tile bt5 = new Tile(2, "b");
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

    // Good Touch Principle
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

    // Save Principle
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

        Tile bt1 = new Tile(1, "b");
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

        Tile ct4 = new Tile(5, "w");
        ct4.hintedIdentity.suit = "w";
        Tile ct5 = new Tile(4, "w");
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

    //Minimum Clue Value Principle
    @Test
    public void mcvpQ1()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b3, null, Tile.r1, Tile.y1, null);

        // Setup hands
        alice.hand = new Tile[5];
        alice.updateChopPosition();

        Tile bt4 = new Tile(4, "b");
        bt4.hintedIdentity.suit = "b";
        bob.hand = Tile.hand(Tile.w5, Tile.g5, Tile.g3, bt4, Tile.r1);
        bob.updateChopPosition();

        Tile ct4 = new Tile(2, "r");
        ct4.hintedIdentity.value = 2;
        Tile ct5 = new Tile(2, "y");
        ct5.hintedIdentity.value = 2;
        cathy.hand = Tile.hand(Tile.b2, Tile.g4, Tile.y1, ct4, ct5);
        cathy.updateChopPosition();

        // Take action - Alice enumerates and prioritises her possible actions
        alice.enumerateActions();
        alice.prioritiseActions();

        // Q: What are all of Alice's legal Tempo clues?
        // A: b 4 PLAY clue on Bob (both prompting b and 4), r 2 PLAY clue on Cathy (via r clue)
        Assertions.assertEquals(3, alice.possibleActions.size());
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(0));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(1));
        Assertions.assertInstanceOf(ClueAction.class, alice.possibleActions.get(2));
        Assertions.assertTrue(((ClueAction)alice.possibleActions.get(0)).isTempo);
        Assertions.assertTrue(((ClueAction)alice.possibleActions.get(1)).isTempo);
        Assertions.assertTrue(((ClueAction)alice.possibleActions.get(2)).isTempo);

        Clue bobSuitClue = new Clue(ClueType.NULL);
        Clue bobValueClue = new Clue(ClueType.NULL);
        Clue cathyClue = new Clue(ClueType.NULL);
        for (Action action : alice.possibleActions)
        {
            ClueAction clueAction = (ClueAction)action;
            if (clueAction.targetPlayer == 1)
                if (!clueAction.intendedClue.suit.isBlank())
                    bobSuitClue = clueAction.intendedClue;
                else
                    bobValueClue = clueAction.intendedClue;
            else
                cathyClue = clueAction.intendedClue;
        }

        Assertions.assertEquals(ClueType.PLAY, bobSuitClue.clueType);
        Assertions.assertEquals("b", bobSuitClue.suit);

        Assertions.assertEquals(ClueType.PLAY, bobValueClue.clueType);
        Assertions.assertEquals(4, bobValueClue.value);

        Assertions.assertEquals(ClueType.PLAY, cathyClue.clueType);
        Assertions.assertEquals("r", cathyClue.suit);
    }

    // Early Game Questions
    @Test
    public void earlyGameQ1()
    {
        // Setup game
        // Setup hands
            //bob n n n n r1
            //cathy b1 g1 r1 n n
        // Take action - Alice clues Cathy on 1, Bob discards a red 1
        // Q: Is it the Early Game or the Mid-Game? - A: Mid-Game (not Early)
    }
    
    @Test
    public void earlyGameQ2()
    {
        // Setup game
            //inplay b1 r1 y2
            //1 clue
        // Setup hands
            //alice n n n n 5
            //bob r1 g2, w1 g1 (clued 1), y2
            //cathy g3 w4 w4, w5 g5 (clued)
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: What action should Alice perform? - A: Clue Bob on g (for a g 2 DELAYED_PLAY)
    }
    
    @Test
    public void earlyGameQ3()
    {
        // Setup game
            //inplay g1 r1 w1
            //1 clue
        // Setup hands
            //bob w4, r2 r3 (clued), w1
            //cathy g1 w2 r2 y4
            //donald g4 r1 g5 b4
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: What action should alice perform? - A: Clue Cathy on w (for a w 2 PLAY clue)
    }
    
    @Test
    public void earlyGameQ4()
    {
        // Setup game
            //inplay b1 g3 r2 y1
            //1 clue
        // Setup hands
            //bob r4 r1 w3 g1
            //cathy w2 b4 b4 r2
            //donald w5 g3 b1 r1
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: What action should Alice take? - A: Discard
    }
    
    // Other General Strategy Questions
    @Test
    public void generalQ1()
    {
        // Setup game
            //inplay b1 y1 w3
            //discard g3 g4 w4
        // Setup hands
            //alice y2 n n n n
            //bob w2 b1 r4 y1 w3
            //cathy r3 r3 y1 y5 b5
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: Should Alice play her 2, or save Cathy's 5b? - A: Play her 2 (Bob can save the 5)
    }
    
    @Test
    public void generalQ2()
    {
        // Setup game
            //g2 r1 y2 w3
        // Setup hands
            //alice n n n n 2?
            //bob b4 w3 r1 g2 b4
            //cathy r3 g3 y2 w1 r5
        // Take action - Alice enumerates and prioritises her possible actions
        // Q1: What are all of Alice's possible clues? - A1: Clue Cathy on g (g 3 PLAY), clue Cathy on 5 (FIVE_SAVE)
        // Q2: Which clue should Alice give? - A2: Clue Cathy on g
    }
    
    @Test
    public void generalQ3()
    {
        // Setup game
            //inplay g3 r3 y1 w3
        // Setup hands
            //bob y3 r2 g2 r1 b5
            //cathy b4, y2 (clued y 2 PLAY), w4 r3 w1
        // Take action - Alice enumerates and prioritises her possible actions
        // Q1: What are all of Alice's possible clues?
        // A1: Clue Bob on 5 (FIVE_SAVE), clue Bob on y (y 3 DELAYED_PLAY), clue Bob on 3 (y 3 DELAYED_PLAY), clue Cathy on w (w 4 PLAY)
        // Q2: Which clue should Alice give? - A2: Clue Bob on 5
    }
    
    @Test
    public void generalQ4()
    {
        // Setup game
            //inplay b1, g4, r4, y1, w1
        // Setup hands
            //bob r3 b2 y1 w1 w1
        // Take action - Alice enumerates and prioritises her possible actions
        // Q1: What are all of Alice's possible clues? - A1: Clue Bob on b (b 2 PLAY), clue Bob on 2 (b 2 PLAY)
        // Q2: Which clue should Alice give? - A2: Clue Bob on b
    }
    
    @Test
    public void generalQ5()
    {
        // Setup game
            //inplay b4 g2 r5 w1
        // Setup hands
            //bob y5 b4 g3 y3 1r
        // Take action - Alice enumerates and prioritises her possible actions
        // Q1: What are all of Alice's possible clues? - A1: Clue Bob on g (g 3 PLAY), clue Bob on 3 (g 3 PLAY)
        // Q2: Which clue should Alice give? - A1: Clue Bob on g
    }
    
    // Prompt Questions
    @Test
    public void promptQ1()
    {
        // Setup game
            //inplay g3 r1 w5
            //discard g1 g4 r3 b3
        // Setup hands
            //bob n n, 2 2 (clued), n
            //cathy b4 y3 r3, 4 (clued 4), 5 (clued 5)
        // Take action - Alice clues Cathy on r, Bob enumerates and prioritises his possible actions
        // Q: What is Bob's interpretation of Alice's clue?
        // A: Bob does not think anything, as Alice's clue is a save clue
    }
    
    @Test
    public void promptQ2()
    {
        // Setup game
            //inplay b2 g1 r1 y2 w1
        // Setup hands
            //bob y4 b4 w1 g1 w4
            //cathy y2 r1, y3 b3 (clued 3), 5 clued
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: What two possible clues can Alice give? Which one is better?
        // A: Clue Bob on y (y 4 DELAYED_PLAY), clue Bob on b (b 4 DELAYED_PLAY); clue Cathy on b because it PROMPTs Cathy to play both her 3s
    }
    
    @Test
    public void promptQ3()
    {
        // Setup game
            //inplay b2 g1 w1
        // Setup hands
            //bob b1 w4 b2 g1, r5 (clued)
            //cathy g4 b1 g4, w3 w2 (clued w)
        // Take action - Alice enumerates and prioritises her possible actions
        // Q: Alice wants to Play clue Bob's 4w, what clue should Alice give? - A: Clue Cathy on 3 (PROMPT on her 2)
    }
    
    // Finesse Questions
    @Test
    public void finesseQ1()
    {
        // Setup game
            //inplay g2 y1 w1
        // Setup hands
            //bob b1, y5 (clued), r4, g5 (clued), r3
            //cathy w4 y2 b2, g4 (clued), w3 (clued)
        // Take action - Alice enumerates and prioritises her possible actions
        // Q1: What are the five legal clues Alice could give?
        // A1: Clue Bob on b (b 1 PLAY), clue Bob on 1 (b 1 PLAY), clue Cathy on y (y 2 PLAY), clue Cathy on b (Bob b 1 FINESSE), clue Cathy on 2 (TWO_SAVE)
        // Q2: Which is the best to give? - A2: Clue Cathy on b, as the Finesse clue gives 2 plays
    }
    
    @Test
    public void finesseQ2()
    {
        // Setup game
            //inplay b4 g2 y2 w4
        // Setup hands
            //bob n n n, 3y (clued 3), 5r (clued 5)
        // Take action - Alice clues Cathy on y (touching 4y)
        // Q: For Bob, is this a Finesse? Why or why not?
        // A: This is not a Finesse, as Bob has an unknown suit 3 and considers the clue a PROMPT
    }
}
