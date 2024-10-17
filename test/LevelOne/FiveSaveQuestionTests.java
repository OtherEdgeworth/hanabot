import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FiveSaveQuestionTests
{
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
}