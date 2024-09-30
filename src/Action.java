public abstract class Action implements Comparable<Action>
{
    int priority;

    public abstract void execute(int playerIndex);

    @Override
    public int compareTo(Action action) { return action.priority - this.priority; }
}
