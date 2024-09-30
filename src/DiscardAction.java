public class DiscardAction extends Action
{
    int chopPosition;

    public DiscardAction(int chopPosition) { this.chopPosition = chopPosition; }

    @Override
    public void execute(int self) { Main.discard(self, chopPosition); }
}
