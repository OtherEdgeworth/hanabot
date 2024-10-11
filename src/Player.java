import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

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
        if (chopPosition >= 0)
            possibleActions.add(new DiscardAction(chopPosition));
        else if (Main.clues > 0)
        {
            //possibleActions.addAll(enumerateDesperateClueActions());
            for (Player player : Main.allPlayers)
            {
                if (this.equals(player))
                    continue;

                ArrayList<String> suitsNotInHand = new ArrayList<>(List.of(Tile.SUIT_INDEX));
                ArrayList<Integer> valuesNotInHand = new ArrayList<>(List.of(Tile.ALL_VALUES));
                for (Tile tile : player.hand)
                {
                    /*
                    suitsNotInHand.remove(tile.suit);
                    valuesNotInHand.remove(tile.value);
                     */
                }
            }
            //TODO: finish this method, idea is: if you are clued an opening hand of all 5s or something, and you have no other legal clues to give anyone else then you give them a clue that hits all their tiles by informing them there is not a given suit/value in their hand.
            //TODO: add prioritisation for these clues (use NULL as we won't be using that for any other clue actions
        }
        else
        {
            //TODO: figure out what to do when you have no chop position and no clues are available to give
        }

        // determine definitely playable tiles from information
        ArrayList<Tile> playableTiles = Main.playableTiles();

        for (int i = 0; i < handSize; i++)
        {
            Tile tile = hand[i];
            if (!tile.isClued()) //if a tile hasn't been clued it isn't playable
                continue;

            //check if the identity of the tile has been fully clued, and if so, if it's a playable tile
            if (!tile.hintedIdentity.suit.isBlank() && tile.hintedIdentity.value != 0 && playableTiles.contains(tile))
                possibleActions.add(new PlayAction(i));
            //otherwise, if tile not fully hinted, consider if it has only play clues
            else if (tile.hasOnlyPlayClues())
                possibleActions.add(new PlayAction(i));
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
                    if (tile.value == 2 && noOtherMatchingTwos(Main.allPlayers, tile.suit, i) && !(new Tile(new Clue(ClueType.TWO_SAVE, tile.value, tile.suit))).isUseless())
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
                    if (isFocus(Main.allPlayers[i].hand, tile, valueClue) && isGoodTouch(Main.allPlayers[i].hand, i, valueClue))
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

    /* OOM:
     *  - Do you NEED to give this clue (is it a clue? is it a critical/playable on the chop? is there another player without a guaranteed play between you and the player to be clued?)
     *  - Is this a play action?
     *  - What type of clue action is this (Play, Five save, Two save, critical save, delayed play (soon(tm)))
     *  - How many playable tiles does this play clue hit (0 for non-play clues)
     *  - How many tiles does the clue touch?
     *  - is this a colour clue? (used to break ties between equivalent colour and value clues)
     */
    public void prioritiseActions()
    {
        for (Action action : possibleActions)
        {
            int priority = 0;

            if (action instanceof DiscardAction)
                priority += 0;
            else if (action instanceof PlayAction)
                priority += 10000;
            else if (action instanceof ClueAction clueAction)
            {
                // a colour clue is generally more specific than a number clue
                if (!clueAction.intendedClue.suit.isBlank())
                    priority += 1;

                priority += 10 * matchedTiles(clueAction.intendedClue, Main.allPlayers[clueAction.targetPlayer].hand, false);
                priority += 100 * matchedTiles(clueAction.intendedClue, Main.allPlayers[clueAction.targetPlayer].hand, true)
                        * (clueAction.intendedClue.clueType == ClueType.PLAY ? 1 : 0);

                // prioritise a direct play clue over a save clue
                if (clueAction.intendedClue.clueType == ClueType.CRITICAL_SAVE)
                    priority += 2000;
                else if (clueAction.intendedClue.clueType == ClueType.TWO_SAVE)
                    priority += 3000;
                else if (clueAction.intendedClue.clueType == ClueType.FIVE_SAVE)
                    priority += 4000;
                else if (clueAction.intendedClue.clueType == ClueType.PLAY)
                    priority += 5000;

                // do *I* need to give this clue (i.e is it the next player) //TODO: more complicated analysis of if you need to give the clue
                if (isNextPlayer(clueAction))
                    priority += 100000;
            }

            action.priority = priority;
        }

        Collections.sort(possibleActions);
    }

    public void updateChopPosition()
    {
        int firstNonCluedChop = -1;
        int firstUselessChop = -1;

        for (int i = handSize - 1; i >= 0; i--)
        {
            if (hand[i] == null)
                continue;

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
            //TODO: display what we know a tile is not (negative information - and clean up the "i know" display
            sb.append(" - I know that it is a ").append(Tile.fullSuit(tile.hintedIdentity.suit)).append(" ")
                    .append(tile.hintedIdentity.value == 0 ? "tile" : tile.hintedIdentity.value).append(".\n");

            if ((tile.hintedIdentity.value == 0 || tile.hintedIdentity.suit.isBlank()))
            {
                if (!tile.negativeSuitInformation.isEmpty() || !tile.negativeValueInformation.isEmpty())
                {
                    sb.append(" - I know it is not a ").append(tile.negativeSuitInformation).append(" ")
                            .append(tile.negativeValueInformation.isEmpty() ? "tile" : tile.negativeValueInformation)
                            .append(".\n");
                }
                if (!tile.information.isEmpty())
                {
                    sb.append(" - I think it may be:\n");
                    for (Clue clue : tile.information)
                        sb.append("   - a ").append(clue.toStringVerbose()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    public void updateTileClues()
    {
        for (Tile tile : hand)
        {
            if (tile == null)
                continue;

            //remove all clues if the tile is known to be useless
            if (tile.isUseless())
                tile.information = new ArrayList<>();

            ArrayList<Clue> cluesToRemove = new ArrayList<>();
            ArrayList<Clue> cluesToAdd = new ArrayList<>();
            for (Clue clue : tile.information)
            {
                //remove play clues if the tile has been played and is now useless or if it is impossible (play clue with a suit/value we know the tile isn't)
                if (clue.clueType == ClueType.PLAY &&
                        (new Tile(clue).isUseless() || tile.negativeSuitInformation.contains(clue.suit) || tile.negativeValueInformation.contains(clue.value)))
                    cluesToRemove.add(clue);

                //make save clues play clues if the tile has become playable
                if (clue.clueType.isSaveClue() && clue.isPlayable())
                    clue.clueType = ClueType.PLAY;

                //remove save clues if you can see all instances of the tile it is trying to save
                int numCanSee = Main.numCanSee(self, new Tile(clue));
                if (clue.clueType.isSaveClue() && ((clue.value == 1 && numCanSee == 3) || (clue.value == 5 && numCanSee == 1) || (numCanSee == 2)))
                    cluesToRemove.add(clue);

                //update possible suits - begin with suits that we have negative information on
                ArrayList<String> removeSuits = new ArrayList<>(tile.negativeSuitInformation);
                for (String suit : clue.possibleSuits)
                {
                    Tile inPlay = Main.inPlay[Tile.suitIndex(suit)];

                    //remove suit if it is complete
                    if (inPlay != null && inPlay.value == 5)
                        removeSuits.add(suit);

                    // for play clues - remove colours if they are no longer playable
                    if (clue.clueType == ClueType.PLAY && clue.value > 0 && inPlay != null && clue.value <= inPlay.value)
                        removeSuits.add(suit);

                    //for save clues - remove colours if the tile (or greater) is in play, or you can see all copies of it
                    // - remove the colour but add a play clue if the colour is playable (different to above, that handles known suit this handle possible)
                    if (clue.clueType.isSaveClue())
                        if (clue.value > 0 && inPlay != null && clue.value <= inPlay.value)
                            removeSuits.add(suit);
                        else if (clue.value > 0 && inPlay != null && clue.value == inPlay.value + 1)
                        {
                            removeSuits.add(suit);
                            cluesToAdd.add(new Clue(ClueType.PLAY, clue.value, suit));
                        }
                }
                clue.possibleSuits.removeAll(removeSuits);
                cluesToAdd = (ArrayList<Clue>)cluesToAdd.stream().filter(c -> !tile.negativeSuitInformation.contains(c.suit)).collect(Collectors.toList());

                if (clue.possibleSuits.size() == 1)
                    clue.suit = clue.possibleSuits.remove(0);
            }
            tile.information.removeAll(cluesToRemove);
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
        ArrayList<Tile> touchedTiles = Main.touchedTiles(hand, clue);

        // if the clue only touches one tile and it's not playable right now and it has already been clued on the clue value being given, this is a bad touch
        // I might need to make a more sophisticated way of determining this (like re-hinting a previously clued tile that was not the focus to encourage playing it immediately)
        /*
        if (touchedTiles.size() == 1 && touchedTiles.get(0).isClued() && !touchedTiles.get(0).isPlayable() &&
                (touchedTiles.get(0).hintedIdentity.suit.equals(clue.suit) || touchedTiles.get(0).hintedIdentity.value.equals(clue.value)))
            return false;
         */

        for (Tile touchedTile : touchedTiles)
        {
            //save clues can violate good touch - thus for the purpose of this method they always count as good touch
            if (clue.clueType.isSaveClue())
                return true;

            //has a tile that would be touched already in play? if so, bad touch
            for (Tile ip : Main.inPlay)
                if (ip != null && touchedTile.value <= ip.value && touchedTile.suit.equals(ip.suit))
                    return false;

            //has a tile that would be touched been clued in another player's hand?
            for (int i = 0; i < Main.allPlayers.length; i++)
            {
                for (int j = 0; j < handSize; j++)
                {
                    Tile handTile = Main.allPlayers[i].hand[j];
                    if (i == playerIndex)
                    {
                        //re-clueing the same tile with the same clue is also bad (for now, this will need to be made more compex to allow re-clueing to indicate what was thought to be a save clue is actually a play clue)
                        if (isFocus(hand, touchedTile, clue) && ((!clue.suit.isBlank() && touchedTile.hintedIdentity.suit.equals(clue.suit)) ||
                                (clue.value != 0 && touchedTile.hintedIdentity.value.equals(clue.value))))
                            return false;

                        //same player's hand touching multiple of the same tile in hand is bad
                        for (int k = j + 1; k < handSize; k++)
                            if (handTile.equals(Main.allPlayers[i].hand[k]))
                                return false;
                    }

                    //your hand, you should not clue if it *might* violate GTP based on clues you have
                    else if (i == self)
                    {
                        // quick check of absolute clues
                         if (isFocus(hand, touchedTile, clue) && handTile.hintedIdentity.matches(touchedTile) && !handTile.negativeSuitInformation.contains(touchedTile.suit) && !handTile.negativeValueInformation.contains(touchedTile.value))
                             return false;

                        // iterate through interpreted  clues
                        for (Clue handClue : handTile.information)
                            if (handClue.matches(touchedTile))
                                return false;
                    }

                    //other hands, if the tile has been clued in one, it is not good touch
                    else if (handTile.equals(touchedTile) && handTile.isClued())
                        return false;
                }
            }
        }

        return true;
    }

    private boolean isNextPlayer(ClueAction action)
    {
        int mod = Main.allPlayers.length;
        return ((action.targetPlayer - self) % mod + mod) % mod == 1;
    }

    private static int matchedTiles(Clue clue, Tile[] hand, boolean matchPlayableOnly)
    {
        int matched = 0;

        for (Tile tile : hand)
            if (tile.value == clue.value || tile.suit.equals(clue.suit))
                if (!matchPlayableOnly || tile.isPlayable())
                    matched++;

        return matched;
    }

    private boolean noOtherMatchingTwos(Player[] allPlayers, String suit, int firstTwosPlayerIndex)
    {
        for (int i = 0; i < allPlayers.length; i++)
            for (Tile tile : allPlayers[i].hand)
            {
                if (i == self)
                    continue;
                if (tile != null && tile.value == 2 && suit.equals(tile.suit) && (i != firstTwosPlayerIndex || !tile.inChopPosition))
                    return false;
            }
        return true;
    }
}
