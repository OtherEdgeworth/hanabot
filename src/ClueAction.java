public class ClueAction extends Action
{
    int targetPlayer;
    Clue intendedClue;

    public ClueAction(int targetPlayer, Clue intendedClue)
    {
        this.targetPlayer = targetPlayer;
        this.intendedClue = intendedClue;
    }

    @Override
    public void execute(int self)
    {
        String clue = (intendedClue.value != 0 ? String.valueOf(intendedClue.value) : Tile.fullSuit(intendedClue.suit));
        System.out.println("Player " + (self+1) + " clued you on " + clue + "s");
        Main.clue(targetPlayer, new Clue(ClueType.NULL, intendedClue));
    }
}
