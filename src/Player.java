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
    ChopMethod chopMethod;

    public Player(int handSize, int self, ChopMethod chopMethod, boolean isHuman)
    {
        this.handSize = handSize;
        hand = new Tile[handSize];
        this.self = self;
        this.chopMethod = chopMethod;
        this.isHuman = isHuman;
    }

    public void enumerateActions()
    {
        possibleActions = new ArrayList<>();

        //add discarding the chop, as that is always an option (provided you have a chop position)
        if (chopPosition >= 0) //TODO: figure out what to do when no guaranteed plays, no chop position, and no clues available/possible to give (i.e. no "valid" actions)
            possibleActions.add(new DiscardAction(chopPosition));

        // determine definitely playable tiles from information
        ArrayList<Tile> playableTiles = Main.playableTiles();

        for (int i = 0; i < handSize; i++)
        {
            Tile tile = hand[i];
            if (!tile.isClued()) //if a tile hasn't been clued it isn't playable
                continue;

            //check if the identity of the tile has been fully clued, and if so, if it's a playable tile
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
                    if (isFocus(Main.allPlayers[i].hand, tile, suitClue) && isGoodTouch(Main.allPlayers[i].hand, i, suitClue))
                        possibleActions.add(new ClueAction(i, suitClue));

                    Clue valueClue = new Clue(ClueType.PLAY, tile.value);
                    if (isFocus(Main.allPlayers[i].hand, tile, valueClue) && isGoodTouch(Main.allPlayers[i].hand, i, suitClue))
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

    public void updateChopPosition()
    {
        int firstNonCluedChop = -1;
        int firstUselessChop = -1;

        for (int i = handSize - 1; i >= 0; i--)
        {
            hand[i].inChopPosition = false;
            if (hand[i].isUseless() && firstUselessChop == -1)
                firstUselessChop = i;
            if (!hand[i].isClued() && firstNonCluedChop == -1)
                firstNonCluedChop = i;
        }

        switch (chopMethod)
        {
            case NON_CLUED -> chopPosition = firstNonCluedChop;
            case USELESS_MAY_BE_CHOP -> chopPosition = (Math.max(firstUselessChop, firstNonCluedChop));
            case USELESS_PRIORITISED -> chopPosition = (firstUselessChop > -1 ? firstUselessChop : firstNonCluedChop);
        }

        if (chopPosition > -1)
            hand[chopPosition].inChopPosition = true;
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
                    sb.append("   - a ").append(clue.toStringVerbose()).append("\n");
            }
        }

        return sb.toString();
    }

    public void updateTileClues()
    {
        for (Tile tile : hand)
        {    //remove all clues if the tile is known to be useless
            if (tile.isUseless())
                tile.information = new ArrayList<>();

            ArrayList<Clue> cluesToRemove = new ArrayList<>();
            ArrayList<Clue> cluesToAdd = new ArrayList<>();
            for (Clue clue : tile.information)
            {
                //remove play clues if the tile has been played and is now useless
                if (clue.clueType.isPlayClue() && new Tile(clue).isUseless())
                    cluesToRemove.add(clue);

                //make save clues play clues if the tile has become playable
                if (clue.clueType.isSaveClue() && tile.isPlayable())
                    clue.clueType = ClueType.PLAY;

                //update possible suits
                ArrayList<String> removeSuits = new ArrayList<>();
                for (String suit : clue.possibleSuits)
                {
                    Tile checkPlayTile = Main.inPlay[Tile.suitIndex(suit)];
                    if (checkPlayTile == null && clue.value == 1)
                        removeSuits.add(suit);
                    else if (checkPlayTile != null && checkPlayTile.value >= clue.value) //clue has been played already by someone else
                        removeSuits.add(suit);
                    else if (checkPlayTile != null && checkPlayTile.value == clue.value - 1) //clue is currently playable
                    {
                        removeSuits.add(suit);
                        cluesToAdd.add(new Clue(ClueType.PLAY, clue.value, suit));
                    }
                }
                clue.possibleSuits.removeAll(removeSuits);

                if (clue.possibleSuits.size() == 1)
                    clue.suit = clue.possibleSuits.remove(0);
            }
            tile.information.removeAll(cluesToRemove); //TODO figure out what is happening with this FUCKING BASTARD LINE OF CODE!!!!
            tile.information.addAll(cluesToAdd);
        }
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

    private boolean isGoodTouch(Tile[] hand, int playerIndex, Clue clue)
    {
        ArrayList<Tile> touchedTiles = new ArrayList<>();
        for (Tile t : hand)
            if (clue.matches(t))
                touchedTiles.add(t);

        // if the clue only touches one tile and it has alrady been clued on the clue value being given, this is a bad touch
        // I might need to make a more sophisticated way of determining this (like re-hinting a previously clued tile that was not the focus to encourage playing it immediately)
        if (touchedTiles.size() == 1 && touchedTiles.get(0).isClued() &&
                (touchedTiles.get(0).hintedIdentity.suit.equals(clue.suit) || touchedTiles.get(0).hintedIdentity.value == clue.value))
            return false;

        for (Tile t : touchedTiles)
        {
            //has a tile that would be touched been played?
            for (Tile ip : Main.inPlay)
                if (ip != null && t.value <= ip.value && t.suit.equals(ip.suit))
                    return false;

            //has a tile that would be touched been clued in another player's hand
            for (int i = 0; i < Main.allPlayers.length; i++)
            {
                for (int j = 0; j < handSize; j++)
                {
                    Tile h = Main.allPlayers[i].hand[j];
                    //same player's hand, re-touching previously clued tiles is okay, but touching multiple of the same tile in hand is not
                    if (i == playerIndex)
                        for (int k = j+1; k < handSize; k++)
                            if (h.equals(Main.allPlayers[i].hand[k]))
                                return false;

                    //your hand, you should not clue if it *might* violate GTP based on clues you have.
                    if (i == self)
                        continue; //TODO: evaluate own hand clues and if your clue could potentially break GTP

                    //other hands, if the tile has been clued in one, it is not good touch
                    if (h.equals(t) && h.isClued())
                        return false;
                }
            }
        }

        return true;
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
                if (tile != null && tile.value == 2 && suit.equals(tile.suit) && (i != firstTwosPlayerIndex || !tile.inChopPosition))
                    return false;
        return true;
    }
}
