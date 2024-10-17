import org.junit.jupiter.api.Test;

public class EarlyGameQuestionTests
{
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
}
