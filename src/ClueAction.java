public class ClueAction extends Action
{
    boolean isTempo;
    Clue intendedClue;
    int targetPlayer;

    public ClueAction(int targetPlayer, Clue intendedClue) { this(targetPlayer, intendedClue, false); }

    public ClueAction(int targetPlayer, Clue intendedClue, boolean isTempo)
    {
        this.targetPlayer = targetPlayer;
        this.intendedClue = intendedClue;
        this.isTempo = isTempo;
    }

    @Override
    public String execute(Game game, Player executingPlayer)
    {
        return game.clue(executingPlayer, game.players[targetPlayer], new Clue(ClueType.NULL, intendedClue));
    }
}
