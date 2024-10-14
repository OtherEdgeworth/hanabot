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
    public String execute(Game game, Player executingPlayer)
    {
        return game.clue(game.players[targetPlayer], new Clue(ClueType.NULL, intendedClue));
    }
}
