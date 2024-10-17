import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PlayClueQuestionTests
{
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
}