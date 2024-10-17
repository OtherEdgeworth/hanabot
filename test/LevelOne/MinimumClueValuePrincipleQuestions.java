import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MinimumClueValuePrincipleQuestions
{
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
}
