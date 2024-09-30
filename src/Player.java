import java.util.ArrayList;
import java.util.Collections;

public class Player
{
    boolean isHuman;
    int handSize;
    int chopPosition;
    Tile[] hand;
    ArrayList<Action> possibleActions;
    int self;

    public Player(int handSize, int self, boolean isHuman)
    {
        this.handSize = handSize;
        hand = new Tile[handSize];
        this.self = self;
        this.isHuman = isHuman;
    }

    public void enumerateActions()
    {
        possibleActions = new ArrayList<>();

        //add discarding the chop, as that is always an option (provided you have a chop position)
        if (chopPosition >= 0) //TODO: figure out what to do when no guaranteed plays, no chop position, and no clues available/possible to give (i.e. no "valid" actions)
            possibleActions.add(new DiscardAction(chopPosition));

        // determine definitely playable tiles from information
        //identify playable tiles
        ArrayList<Tile> playableTiles = new ArrayList<>();
        for (int i = 0; i < Main.inPlay.length; i++)
        {
            if (Main.inPlay[i] == null)
                playableTiles.add(new Tile(1, Tile.suitByIndex(i)));
            else if (Main.inPlay[i].value < 5)
                playableTiles.add((new Tile(Main.inPlay[i].value + 1, Main.inPlay[i].suit)));
        }
        for (int i = 0; i < handSize; i++)
        {
            Tile tile = hand[i];
            if (!tile.isClued()) //if a tile hasn't been clued it isn't playable
                continue;

            //check if the identity of the tile has been fully clued, and if so, if its a playable tile
            if (!tile.hintedIdentity.suit.isBlank() && tile.hintedIdentity.value != 0)
            {
                if (playableTiles.contains(tile))
                    possibleActions.add(new PlayAction(i));
            }
            //otherwise, if tile not fully hinted, consider if it has only play clues
            else
            {
                boolean onlyPlayClues = !tile.information.isEmpty();
                for (Clue clue : tile.information)
                    if (clue.clueType != ClueType.PLAY)
                    {
                        onlyPlayClues = false;
                        break;
                    }
                if (onlyPlayClues)
                    possibleActions.add(new PlayAction(i));
            }
        }
        //TODO: add support for other clues (prompt, finesse; check if delayed is now playable?)

        // enumerate possible clues to give
        //identify critical tiles
        ArrayList<Tile> criticalTiles = Main.criticalTiles();

        //iterate players to see which ones have playable tiles
        for (int i = 0; i < Main.allPlayers.length; i++)
        {
            if (this == Main.allPlayers[i] || Main.clues == 0)
                continue;

            for (int j = 0; j < handSize; j++)
            {
                Tile tile = Main.allPlayers[i].hand[j];
                if (tile == null)
                    continue;

                // if the tile is on the player's chop, consider save clues
                if (tile.inChopPosition)
                {
                    // if a 5, add a 5 save clue
                    if (tile.value == 5)
                        possibleActions.add(new ClueAction(i, new Clue(ClueType.FIVE_SAVE, tile.value)));
                    // else if a 2, add a 2 save clue
                    if (tile.value == 2 && noOtherMatchingTwos(Main.allPlayers, tile.suit, i))
                        possibleActions.add(new ClueAction(i, new Clue(ClueType.TWO_SAVE, tile.value)));
                    // else if a critical tile, add a critical save clue
                    if (criticalTiles.contains(tile))
                    {
                        possibleActions.add(new ClueAction(i, new Clue(ClueType.CRITICAL_SAVE, tile.suit)));
                        possibleActions.add(new ClueAction(i, new Clue(ClueType.CRITICAL_SAVE, tile.value)));
                    }
                }

                // add play clues
                if (playableTiles.contains(tile))
                {
                    //confirm that the tile would be the focussed tile before giving the clue
                    Clue suitClue = new Clue(ClueType.PLAY, tile.suit);
                    if (isFocus(Main.allPlayers[i].hand, tile, suitClue))
                        possibleActions.add(new ClueAction(i, suitClue));

                    Clue valueClue = new Clue(ClueType.PLAY, tile.value);
                    if (isFocus(Main.allPlayers[i].hand, tile, valueClue))
                        possibleActions.add(new ClueAction(i, valueClue));
                }
                //TODO: add delayed play clues
            }
        }
    }

    public void executeFirstAction() { possibleActions.get(0).execute(self); }

    public void deal(Tile tile)
    {
        for (int i = handSize - 1; i >= 0; i--)
            if (hand[i] == null)
            {
                hand[i] = new Tile(tile);
                break;
            }
    }

    public boolean hasPlayAction()
    {
        for (Action action : possibleActions)
            if (action instanceof PlayAction)
                return true;

        for (Tile tile : hand)
            for (Clue clue : tile.information)
                if (clue.clueType == ClueType.PLAY)
                    return true;

        return false;
    }

    public void prioritiseActions()
    {
        boolean youShouldClue = true;
        for (Action action : possibleActions)
        {
            if (action instanceof DiscardAction)
                action.priority = 0;
            else if (action instanceof ClueAction clueAction)
            {
                // prioritise a direct play clue over a save clue
                if (clueAction.intendedClue.clueType == ClueType.CRITICAL_SAVE)
                    action.priority = 20;
                else if (clueAction.intendedClue.clueType == ClueType.TWO_SAVE)
                    action.priority = 30;
                else if (clueAction.intendedClue.clueType == ClueType.FIVE_SAVE)
                    action.priority = 40;
                else if (clueAction.intendedClue.clueType == ClueType.PLAY)
                    action.priority = 50;

                // increase the priority of clues only you can give
                if ((clueAction.targetPlayer - self) % Main.allPlayers.length == 1)
                    action.priority += 5;

                // a colour clue is generally more specific than a number clue
                if (!clueAction.intendedClue.suit.isBlank())
                    action.priority += 5;

                // a clue that gives information about more tiles is better (as long as we are obeying Good Touch Principle)
                action.priority += matchedTiles(clueAction.intendedClue, Main.allPlayers[clueAction.targetPlayer].hand) - 1;

            }
            else if (action instanceof PlayAction)
            {
                action.priority = 61;
                youShouldClue = false;
            }
        }

        if (youShouldClue)
            //check if other players could also clue
            for (int i = self + 2; i < Main.allPlayers.length; i++)  //i = player to clue
            {
                youShouldClue = true;
                for (int j = self + 1; j < i; j++)              //j = players between you and i
                {
                    if (!Main.allPlayers[j % Main.allPlayers.length].hasPlayAction())
                    {
                        youShouldClue = false;
                        break;
                    }
                }
                if (youShouldClue) //if you should still clue, increase priority of all clue actions
                    for (Action action : possibleActions)
                        if (action instanceof ClueAction clueAction && clueAction.targetPlayer == i)
                            clueAction.priority += 50;
            }

        Collections.sort(possibleActions);
    }

    public void setChopToDefault()
    {
        for (chopPosition = handSize - 1; chopPosition >= -1; chopPosition--)
        {
            // Special case, you have no chopPosition; i.e. all tiles are clued
            if (chopPosition == -1)
                break;

            // If the tile has been clued it cannot be in the chop position, if it's not been clued it is the chop
            hand[chopPosition].inChopPosition = !hand[chopPosition].isClued() || hand[chopPosition].isUseless();
            if (hand[chopPosition].inChopPosition)
                break;
        }
    }

    public void shiftTiles()
    {
        for (int i = handSize - 1; i >= 1; i--)
        {
            if (hand[i] == null)
            {
                hand[i] = hand[i-1];
                hand[i-1] = null;
            }
        }
    }

    public String thoughts()
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < handSize; i ++)
        {
            Tile tile = hand[i];

            if (!tile.isClued())
                continue;

            sb.append("For the ").append(++i).append(i == 1 ? "st" : (i == 2 ? "nd" : (i == 3 ? "rd" : "th"))).append(" tile:\n");
            i--;
            sb.append(" - I know that it is ");
            if (tile.hintedIdentity.value != 0)
                sb.append("a");
            sb.append(Tile.fullSuit(tile.hintedIdentity.suit));
            if (tile.hintedIdentity.value != 0)
                sb.append(" ").append(tile.hintedIdentity.value);
            sb.append(".\n");

            if ((tile.hintedIdentity.value == 0 || tile.hintedIdentity.suit.isBlank()) && !tile.information.isEmpty())
            {
                sb.append(" - I think it may be:\n");
                for (Clue clue : tile.information)
                    sb.append("   - a ").append(Tile.fullSuit(clue.suit)).append(!clue.suit.isBlank() ? " " : "").append(clue.value != 0 ? + clue.value : "one").append(" (to be ").append(clue.clueType.name().toLowerCase()).append("ed).\n");
            }
        }

        return sb.toString();
    }

    public void updateChopPosition()
    {
        /*
        for (int i = handSize-1; i >= 0; i--)
            if (hand[i].inChopPosition && !hand[i].isClued())
            {
                chopPosition = i;
                return;
            }
         */

        // chop position not found, reset to default
        setChopToDefault();
    }

    public static boolean isFocus(Tile[] hand, Tile tile, Clue clue)
    {
        //determine unclued tiles touched
        ArrayList<Tile> newTiles = new ArrayList<>();
        ArrayList<Tile> oldTiles = new ArrayList<>();
        boolean chopIsNew = false;
        for (Tile handTile : hand)
        {
            if (handTile.value == clue.value || handTile.suit.equals(clue.suit))
            {
                if (handTile.isClued())
                    oldTiles.add(handTile);
                else
                {
                    newTiles.add(handTile);
                    if (handTile.inChopPosition)
                        chopIsNew = true;
                }
            }
        }

        //if no tiles are new, focus is leftmost old
        if (newTiles.isEmpty())
            return oldTiles.get(0).equals(tile);

        //if only one tile is new, it is the focus
        if (newTiles.size() == 1)
            return newTiles.get(0).equals(tile);

        //if multiple new and one is the chop, it is the focus
        if (chopIsNew)
            return tile.inChopPosition;

        //if no new on the chop, focus is the leftmost
        return newTiles.get(0).equals(tile);
    }

    private static int matchedTiles(Clue clue, Tile[] hand)
    {
        int matched = 0;
        for (Tile tile : hand)
            if (tile.value == clue.value || tile.suit.equals(clue.suit))
                matched++;

        return matched;
    }

    private boolean noOtherMatchingTwos(Player[] allPlayers, String suit, int firstTwosPlayerIndex)
    {
        for (int i = 0; i < allPlayers.length; i++)
            for (Tile tile : allPlayers[i].hand)
                if (tile.value == 2 && suit.equals(tile.suit) && (i != firstTwosPlayerIndex || !tile.inChopPosition))
                    return false;
        return true;
    }
}
