import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        alice.hand = new Tile [] { Tile.y2, Tile.b3, t3, t4, t5 };
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

        alice.hand = new Tile[] { Tile.y1, t2, Tile.b2, t4, t5 };
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
        game.inPlay = new Tile[] { Tile.b1, Tile.g2, Tile.r5, Tile.y3, Tile.w2 };

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { Tile.g1, Tile.g2, Tile.b2, Tile.y4, Tile.w5 };
        bob.updateChopPosition();

        //alice clues bob on blue, either through the clue method directly or inserting the action into alice's possible actions and executing it
        //assert that bob thinks it is a b 2 PLAY clue (and no other clues)
    }

    @Test
    public void playQ2()
    {
        Player alice = new Player();
        Player bob = new Player();

        Game game = new Game(0, alice, bob);
        game.inPlay = new Tile[] { Tile.b3, Tile.g3, Tile.r3, Tile.y4, Tile.w2 };

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { Tile.b1, Tile.g2, Tile.r3, Tile.y4, Tile.w5 };
        bob.updateChopPosition();

        //alice clues bob on 4
        //asser that bob thinks that this is a [b, g, r] 4 PLAY clue (even though the tile isn't that isn't the important part of the test)
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
        game.inPlay = new Tile[] { Tile.b1, null, Tile.r1, Tile.y2, Tile.w1 };
        game.discarded.put(Tile.g3, 1);

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { Tile.w4, Tile.g4, Tile.y5, Tile.y1 };
        cathy.hand = new Tile[] { Tile.y4, Tile.g4, new Tile(new Clue(ClueType.NULL, 3, "g")), new Tile(new Clue(ClueType.NULL, 2, "g")) };
        donald.hand = new Tile[] { Tile.w5, Tile.b5, Tile.w3, Tile.w3 };

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
        game.inPlay = new Tile[] { Tile.b3, null, Tile.r1, null, Tile.w2 };
        game.clues = 1;

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { Tile.r1, Tile.w5, Tile.g5, Tile.y2 };
        cathy.hand = new Tile[] { Tile.r4, Tile.y5, new Tile(new Clue(ClueType.NULL, 5, "b")), new Tile(new Clue(ClueType.NULL, 2, "y")) };
        donald.hand = new Tile[] { Tile.w4, Tile.b1, Tile.r5, Tile.w3 };

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
        game.inPlay = new Tile[] { Tile.b1, null, Tile.r3, null, Tile.w2 };

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { Tile.b1, Tile.g3, Tile.r3, Tile.b2, Tile.w5 };
        cathy.hand = new Tile[] { Tile.y5, ct2, Tile.r3, Tile.r2, Tile.b2 };

        bob.updateChopPosition();
        cathy.updateChopPosition();

        //alice clues bobs on 2
        //assert that bob has b 2 PLAY clue and y 2 DELAYED_PLAY clue (as
    }

    @Test
    public void twoSaveQ2()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();

        Tile bt1 = new Tile(1, "r");
        bt1.hintedIdentity.value = 1;
        Clue bc1 = new Clue(ClueType.PLAY, 1);
        bc1.possibleSuits.addAll(List.of("b", "r", "w"));
        bt1.information.add(bc1);

        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = new Tile[] { null, Tile.g2, null, Tile.y1, null };

        alice.hand = new Tile[alice.handSize];
        bob.hand = new Tile[] { bt1, Tile.g1, Tile.r3, Tile.y4, Tile.w2 };
        cathy.hand = new Tile[] { Tile.g5, Tile.b3, Tile.g4, Tile.w3, Tile.w3 };

        bob.updateChopPosition();
        cathy.updateChopPosition();

        //alice clues bob on 2
        //assert bob has y 2 PLAY clue, a [b, r, w] 2 DELAYED_PLAY clue (because of his 1), and [b, r, w] TWO_SAVE clue
    }

    @Test
    public void twoSaveQ3()
    {
        Player alice = new Player();
        Player bob = new Player();
        Player cathy = new Player();

        Tile at4 = new Tile(Tile.r5);
        at4.hintedIdentity.value = 5;
        Tile at5 = new Tile(Tile.w5);
        at5.hintedIdentity.value = 5;
        Clue ac45 = new Clue(ClueType.FIVE_SAVE, 5);
        ac45.possibleSuits.addAll(List.of("b", "g", "r", "w"));
        at4.information.add(ac45);
        at5.information.add(ac45);

        Game game = new Game(0, alice, bob, cathy);
        game.inPlay = new Tile[] { Tile.b2, null, null, null, Tile.w1 };

        alice.hand = new Tile[] { null, null, null, at4, at5 };
        bob.hand = new Tile[] { Tile.r4, Tile.w3, Tile.y2, Tile.r2, Tile.g2 };
        cathy.hand = new Tile[] { Tile.y5, Tile.g2, Tile.g1, Tile.g1, Tile.y2 };

        bob.updateChopPosition();
        cathy.updateChopPosition();

        alice.enumerateActions();
        alice.prioritiseActions();

        Assertions.assertInstanceOf(DiscardAction.class, alice.possibleActions.get(0));
    }
}
