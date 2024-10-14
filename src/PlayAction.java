public class PlayAction extends Action
{
    int targetTile;

    public PlayAction(int targetTile)
    {
        this.targetTile = targetTile;
    }

    @Override
    public String execute(Game game, Player executingPlayer)
    {
        return game.play(executingPlayer, targetTile);
    }
}
