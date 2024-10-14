public abstract class Action implements Comparable<Action>
{
    int priority;

    public abstract String execute(Game game, Player executingPlayer);

    @Override
    public int compareTo(Action action) { return action.priority - this.priority; }
}
