import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class TwoSaveQuestionTests
{
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

        Tile ct2 = new Tile(1, "y");
        ct2.hintedIdentity.value = 1;
        Clue cc2 = new Clue(ClueType.PLAY, 1, "y");
        ct2.information.add(cc2);
        cathy.hand = Tile.hand(Tile.y5, ct2, Tile.r3, Tile.r2, Tile.b2);
        cathy.updateChopPosition();

        // Take action - Alice clues Bob on 2
        new ClueAction(1, new Clue(ClueType.NULL, 2)).execute(game, alice);

        // Q: What notes does Bob make? - A: b 2 PLAY, y 2 DELAYED_PLAY
        Assertions.assertEquals(2, bob.hand[3].information.size());

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
        Assertions.assertEquals(3, bob.hand[4].information.size());

        Clue playClue = null;
        Clue delayClue = null;
        Clue saveClue = null;
        for (Clue clue : bob.hand[4].information)
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
}
