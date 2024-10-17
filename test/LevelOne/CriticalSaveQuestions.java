import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CriticalSaveQuestions
{
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
}
