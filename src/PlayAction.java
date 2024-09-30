public class PlayAction extends Action
{
    int targetTile;

    public PlayAction(int targetTile)
    {
        this.targetTile = targetTile;
    }

    @Override
    public void execute(int self) { Main.play(self, targetTile); }
}
