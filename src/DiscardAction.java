public class DiscardAction extends Action
{
    int chopPosition;

    public DiscardAction(int chopPosition) { this.chopPosition = chopPosition; }

    @Override
    public String execute(Game game, Player executingPlayer)
    {
        return game.discard(executingPlayer, chopPosition);
    }
}
