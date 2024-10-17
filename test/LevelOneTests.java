import org.junit.jupiter.api.Test;

public class LevelOneTests
{
    /*
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
     */
}
