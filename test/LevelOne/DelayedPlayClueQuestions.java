import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DelayedPlayClueQuestions
{
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
        cathy.hand = Tile.hand(null, null, Tile.r3, Tile.b1); //1b to avoid the chop going whack
        donald.hand = Tile.hand(null, Tile.r2, null, null);

        // Take actions - Alice clues Donald on r, Bob clues Cathy on r, Cathy enumerates and prioritises her possible actions
        new ClueAction(3, new Clue(ClueType.PLAY, "r")).execute(game, alice);
        new ClueAction(2, new Clue(ClueType.PLAY, "r")).execute(game, bob);
        cathy.enumerateActions();
        cathy.prioritiseActions();

        // Q: What does Cathy note? Is it DELAYED_PLAY? What action should Cathy take? - A: r 3 DELAYED_PLAY; yes (obviously); Discard
        Assertions.assertEquals(1, cathy.hand[2].information.size());
        Clue clue = cathy.hand[2].information.get(0);
        Assertions.assertEquals(ClueType.DELAYED_PLAY, clue.clueType);
        Assertions.assertEquals("r", clue.suit);
        Assertions.assertEquals(3, clue.value);

        Assertions.assertFalse(cathy.possibleActions.isEmpty());
        Assertions.assertInstanceOf(DiscardAction.class, cathy.possibleActions.get(0));
    }

    @Test
    public void delayPlayQ2()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = Tile.hand(Tile.b2, null, null, Tile.y1, Tile.w2);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.b3, null, null, null, Tile.b1); //1b to prevent the chop going whack

        Tile ct3 = new Tile(1, "r");
        ct3.hintedIdentity.value = 1;
        Tile ct4 = new Tile(1, "g");
        ct4.hintedIdentity.value = 1;
        cathy.hand = Tile.hand(null, null, ct3, ct4, null);

        // Take action - Alice clues Bob on 3, Bob enum & prior
        new ClueAction(1, new Clue(ClueType.NULL, 3)).execute(game, alice);
        bob.enumerateActions();
        bob.prioritiseActions();

        // Q: What note does Bob make? For Bob, is this a Delayed Play Clue? What action should Bob take?
        // A: [b, w] 3 PLAY; No (no connecting tiles); Bob should play the 3
        Assertions.assertEquals(1, bob.hand[0].information.size());
        Clue clue = bob.hand[0].information.get(0);
        Assertions.assertEquals(ClueType.PLAY, clue.clueType);
        Assertions.assertEquals(3, clue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(clue.possibleSuits, List.of("b", "w")));

        Assertions.assertFalse(bob.possibleActions.isEmpty());
        Action action = bob.possibleActions.get(0);
        Assertions.assertInstanceOf(PlayAction.class, action);
        Assertions.assertEquals(0, ((PlayAction)action).targetTile);
    }

    @Test
    public void delayPlayQ3()
    {
        // Setup game
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();
        Player donald = new Player();
        Game game = new Game(0, alice, bob, cathy, donald);
        game.inPlay = Tile.hand(Tile.b2, null, Tile.r3, Tile.y1, Tile.w3);

        // Setup hands
        alice.hand = new Tile[alice.handSize];
        bob.hand = Tile.hand(Tile.b4, null, null, Tile.b1); //1b to stop the chop going whack

        Tile ct2 = new Tile(2, "y");
        ct2.hintedIdentity.suit = "y";
        Clue cc2 = new Clue(ClueType.PLAY, 2, "y");
        ct2.information.add(cc2);
        Tile ct3 = new Tile(3, "y");
        ct3.hintedIdentity.suit = "y";
        Clue cc33 = new Clue(ClueType.DELAYED_PLAY, 3, "y");
        Clue cc34 = new Clue(ClueType.DELAYED_PLAY, 4, "y");
        ct3.information.addAll(List.of(cc33, cc34));
        Tile ct4 = new Tile(5, "g");
        ct4.hintedIdentity.value = 5;
        cathy.hand = Tile.hand(Tile.r1, ct2, ct3, ct4);
        cathy.updateChopPosition();

        Tile dt4 = new Tile(3, "b");
        dt4.hintedIdentity.suit = "b";
        Clue dc4 = new Clue(ClueType.PLAY, 3, "b");
        dt4.information.add(dc4);
        donald.hand = Tile.hand(Tile.w5, Tile.g2, Tile.w1, dt4);
        donald.updateChopPosition();

        // Take action - Alice clues Bob on 4, Bob thinks
        new ClueAction(1, new Clue(ClueType.NULL, 4)).execute(game, alice);
        bob.enumerateActions();
        bob.prioritiseActions();

        // Q: What note does Bob make? For Bob, is this a Delayed Play Clue? What action should Bob take?
        // A: [r, w] 4 PLAY, [b, y] 4 DELAYED_PLAY; Maybe; Discard
        Assertions.assertEquals(2, bob.hand[0].information.size()); //TODO: delay clues
        boolean playFirst = (bob.hand[0].information.get(0).clueType == ClueType.PLAY);
        Clue playClue = bob.hand[0].information.get(playFirst ? 0 : 1);
        Clue delayClue = bob.hand[0].information.get(playFirst ? 1 : 0);

        Assertions.assertEquals(ClueType.PLAY, playClue.clueType);
        Assertions.assertEquals(4, playClue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(playClue.possibleSuits, List.of("r", "w")));

        Assertions.assertEquals(ClueType.DELAYED_PLAY, delayClue.clueType);
        Assertions.assertEquals(4, delayClue.value);
        Assertions.assertTrue(CollectionUtils.containsAll(delayClue.possibleSuits, List.of("b", "y")));
    }
}