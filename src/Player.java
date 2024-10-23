import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class Player
{
    ChopMethod chopMethod;
    int chopPosition;
    Game game;
    Tile[] hand;
    int handSize;
    boolean isHuman;
    String name = "";
    ArrayList<Action> possibleActions = new ArrayList<>();

    public Player()
    {
        chopMethod = ChopMethod.NON_CLUED;
        isHuman = false;
    }

    public Player(ChopMethod chopMethod, boolean isHuman)
    {
        this.chopMethod = chopMethod;
        this.isHuman = isHuman;
    }

    public Player(int handSize, ChopMethod chopMethod, boolean isHuman)
    {
        this.handSize = handSize;
        hand = new Tile[handSize];
        this.chopMethod = chopMethod;
        this.isHuman = isHuman;
    }

    public Player(String name, ChopMethod chopMethod, boolean isHuman)
    {
        hand = new Tile[handSize];
        this.chopMethod = chopMethod;
        this.isHuman = isHuman;
        this.name = name;
    }

    public void deal(Tile tile)
    {
        for (int i = handSize - 1; i >= 0; i--)
            if (hand[i] == null)
            {
                hand[i] = new Tile(tile);
                break;
            }
    }

    public void enumerateActions()
    {
        boolean canGiveTempoClue = false;
        possibleActions = new ArrayList<>();

        //add discarding the chop, as that is always an option (provided you have a chop position)
        if (chopPosition >= 0)
            possibleActions.add(new DiscardAction(chopPosition));
        else
            canGiveTempoClue = true;

        // determine definitely playable tiles from information
        ArrayList<Tile> playableTiles = game.playableTiles();

        for (int i = 0; i < handSize; i++)
        {
            Tile tile = hand[i];
            if (tile == null || !tile.isClued()) //if a tile hasn't been clued (or doesn't exist) it isn't playable
                continue;

            //check if the identity of the tile has been fully clued, and if so, if it's a playable tile
            if (!tile.hintedIdentity.suit.isBlank() && tile.hintedIdentity.value != 0 && playableTiles.contains(tile))
                possibleActions.add(new PlayAction(i));
            //otherwise, if tile not fully hinted, consider if it has only play clues
            else if (tile.hasOnlyPlayClues())
                possibleActions.add(new PlayAction(i));
        }

        // enumerate possible clues to give
        //identify critical tiles
        ArrayList<Tile> criticalTiles = game.criticalTiles();

        //iterate players to see which ones have playable tiles
        for (int playerIndex = 0; playerIndex < game.players.length; playerIndex++)
        {
            if (this == game.players[playerIndex] || game.clues == 0)
                continue;

            for (int handIndex = 0; handIndex < handSize; handIndex++)
            {
                Tile tile = game.players[playerIndex].hand[handIndex];
                if (tile == null)
                    continue;

                // if the tile is on the player's chop, consider save clues
                if (tile.inChopPosition)
                {
                    // if a 5, add a 5 save clue
                    if (tile.value == 5)
                        possibleActions.add(new ClueAction(playerIndex, new Clue(ClueType.FIVE_SAVE, tile.value)));
                    // else if a 2, add a 2 save clue
                    Tile possibleTwo = new Tile(new Clue(ClueType.TWO_SAVE, tile.value, tile.suit));
                    if (tile.value == 2 && noOtherMatchingTwos(game.players, tile.suit, playerIndex) &&
                            !game.isUseless(possibleTwo) && !game.isPlayable(possibleTwo))
                        possibleActions.add(new ClueAction(playerIndex, new Clue(ClueType.TWO_SAVE, tile.value)));
                    // else if a critical tile, add a critical save clue
                    if (criticalTiles.contains(tile))
                    {
                        possibleActions.add(new ClueAction(playerIndex, new Clue(ClueType.CRITICAL_SAVE, tile.suit)));
                        possibleActions.add(new ClueAction(playerIndex, new Clue(ClueType.CRITICAL_SAVE, tile.value)));
                    }
                }

                // add play clues
                if (playableTiles.contains(tile))
                {
                    //confirm that the tile would be the focussed tile before giving the clue
                    Clue suitClue = new Clue(ClueType.PLAY, tile.suit);
                    boolean isTempoClue = isTempo(game.players[playerIndex].hand, tile, suitClue);
                    if (isFocus(game.players[playerIndex].hand, tile, suitClue) && isGoodTouch(game.players[playerIndex].hand, playerIndex, suitClue)
                            && (!isTempoClue || canGiveTempoClue))
                        possibleActions.add(new ClueAction(playerIndex, suitClue, isTempoClue));

                    Clue valueClue = new Clue(ClueType.PLAY, tile.value);
                    isTempoClue = isTempo(game.players[playerIndex].hand, tile, valueClue);
                    if (isFocus(game.players[playerIndex].hand, tile, valueClue) && isGoodTouch(game.players[playerIndex].hand, playerIndex, valueClue)
                            && (!isTempoClue|| canGiveTempoClue))
                        possibleActions.add(new ClueAction(playerIndex, valueClue, isTempoClue));
                }

                List<Tile> filteredPlayableTiles = playableTiles.stream().filter(pt -> pt.suit.equals(tile.suit)).toList();
                if (!game.canSeeInPlay(tile) && !filteredPlayableTiles.isEmpty())
                {
                    int nextPlayValue = filteredPlayableTiles.get(0).value;
                    boolean chainedPlayClues = true;
                    for (int k = nextPlayValue; k < tile.value; k++)
                        if (!game.canSeePlayCluedInOtherHands(this, new Tile(tile.suit, k)))
                        {
                            chainedPlayClues = false;
                            break;
                        }

                    if (chainedPlayClues)
                    {
                        Clue suitClue = new Clue(ClueType.DELAYED_PLAY, tile.suit);
                        boolean isTempoClue = isTempo(game.players[playerIndex].hand, tile, suitClue);
                        if (isFocus(game.players[playerIndex].hand, tile, suitClue) && isGoodTouch(game.players[playerIndex].hand, playerIndex, suitClue)
                                && (!isTempoClue || canGiveTempoClue) && !game.isPlayable(tile))
                            possibleActions.add(new ClueAction(playerIndex, suitClue, isTempoClue));

                        Clue valueClue = new Clue(ClueType.DELAYED_PLAY, tile.value);
                        isTempoClue = isTempo(game.players[playerIndex].hand, tile, valueClue);
                        if (isFocus(game.players[playerIndex].hand, tile, valueClue) && isGoodTouch(game.players[playerIndex].hand, playerIndex, valueClue)
                                && (!isTempoClue || canGiveTempoClue) && !game.isPlayable(tile))
                            possibleActions.add(new ClueAction(playerIndex, valueClue, isTempoClue));
                    }
                }
            }
        }

        //TODO: determine what each player may interpret the prompt clue as and remove any clues that would cause a tile
        // to be prompted as something it isn't
        //iterate delayed_play clues (prompts are only on clued tiles)
        //determine what prompt (if any) and for who this clue creates; remember prompts only fill in one space (multi-prompt is level 2)
        //remove the delayed clue (and prompt) if the prompt is invalid (i.e. it prompts a tile to be a suit/value it is not

        //TODO: no focus clues - idea is: if you are clued an opening hand of all 5s or something, and you have no other
        // legal clues to give anyone else then you give them a clue that hits all their tiles by informing them there
        // is not a given suit/value in their hand.
        //TODO: add prioritisation for these clues (use NULL as we won't be using that for any other clue actions
        /* Putting this here for later - what do if cannot discard and no clues (even tempo ones) to give
        else if (game.clues > 0)
        {
            /*
            //possibleActions.addAll(enumerateDesperateClueActions());
            for (Player player : game.players)
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
                     *//*
                }
            }
            */
    }

    public int focusIndex(Clue clue)
    {
        for (int i = 0; i < handSize; i++)
            if (Player.isFocus(hand, hand[i], clue))
                return i;
        return -1;
    }

    public boolean hasPlayAction()
    {
        if (possibleActions.isEmpty())
            return false;

        for (Action action : possibleActions)
            if (action instanceof PlayAction)
                return true;

        for (Tile tile : hand)
            if (tile != null)
                for (Clue clue : tile.information)
                    if (clue.clueType == ClueType.PLAY)
                        return true;

        return false;
    }

    public void interpretClue(Clue clue)
    {
        int focusIndex = focusIndex(clue);
        Tile focusTile = hand[focusIndex];
        focusTile.hintedIdentity.suit = clue.suit.isBlank() ? focusTile.hintedIdentity.suit : clue.suit;
        focusTile.hintedIdentity.value = clue.value.equals(0) ? focusTile.hintedIdentity.value : clue.value;
        ArrayList<Clue> focusClues = new ArrayList<>();

        //play clues
        // if a colour clue, then clearly it is either a play clue or a delayed play clue if there are other play clues
        // (but that is later) unless the value is already known and the suit+value combination is not playable
        if (!clue.suit.isBlank())
        {
            Tile inPlayTile = game.inPlay[Tile.suitIndex(clue.suit)];
            int toPlayValue = (inPlayTile == null ? 1 : inPlayTile.value + 1);

            Tile checkTile = new Tile(clue.suit, toPlayValue);
            if ((focusTile.hintedIdentity.value.equals(toPlayValue) || (focusTile.hintedIdentity.value.equals(0) && !focusTile.negativeValueInformation.contains(toPlayValue)))
                    && !game.canSeePlayCluedInOtherHands(this, checkTile)
                    && !mightSeePlayClueInOwnHand(checkTile, focusIndex))
                focusClues.add(new Clue(ClueType.PLAY, toPlayValue, clue.suit));

            for (int i = toPlayValue; i < 5; i++)
            {
                checkTile = new Tile(clue.suit, i);
                if (game.canSeePlayCluedInOtherHands(this, checkTile) || mightSeePlayClueInOwnHand(checkTile, focusIndex))
                {
                    if (game.canSeePlayCluedInOtherHands(this, new Tile(clue.suit, i + 1)))
                        continue;
                    else if (!game.allDiscarded(new Tile(clue.suit, i + 1)))
                        focusClues.add(new Clue(ClueType.DELAYED_PLAY, i + 1, clue.suit));
                    else
                        break;
                }
                else
                    break;
            }
        }
        else //value clue - iterate the in play suits to determine which are playable
        {
            Clue playClue = new Clue(ClueType.PLAY, clue.value);
            Clue delayClue = new Clue(ClueType.DELAYED_PLAY, clue.value);
            for (String suit : Tile.SUIT_INDEX)
            {
                if (focusTile.negativeSuitInformation.contains(suit))
                    continue;

                if (game.isPlayable(new Tile(new Clue(ClueType.NULL, clue.value, suit))))
                {
                    for (int i = 0; i < game.players.length; i++)
                    {
                        if (i == index())
                            continue;

                        //skip suits that have playable tiles in other hands that already have play clues on them
                        boolean skipSuit = false;
                        for (Tile tile : game.players[i].hand)
                            if (tile != null && tile.suit.equals(suit) && game.isPlayable(tile) && tile.hasPlayClue())
                                skipSuit = true;

                        if (skipSuit)
                            continue;

                        playClue.possibleSuits.add(suit);
                    }
                }

                Tile inPlayTile = game.inPlay[Tile.suitIndex(suit)];
                int toPlayValue = (inPlayTile == null ? 1 : inPlayTile.value + 1);

                for (int i = toPlayValue; i < clue.value; i++)
                {
                    Tile checkTile = new Tile(suit, i);
                    if (mightSeePlayClueInOwnHand(checkTile, focusIndex))
                        delayClue.possibleSuits.add(suit);
                    else if (game.canSeePlayCluedInOtherHands(this, checkTile))
                    {
                        if (game.canSeePlayCluedInOtherHands(this, new Tile(suit, i + 1)))
                            continue;
                        else if (!game.allDiscarded(new Tile(clue.suit, i + 1)))
                            delayClue.possibleSuits.add(suit);
                        else
                            break;
                    }
                    else
                        break;
                }
            }

            if (playClue.possibleSuits.size() == 1)
            {
                playClue.suit = playClue.possibleSuits.iterator().next();
                playClue.possibleSuits = new HashSet<>();
            }
            if (!playClue.suit.isBlank() || !playClue.possibleSuits.isEmpty())
                focusClues.add(playClue);

            if (delayClue.possibleSuits.size() == 1)
            {
                delayClue.suit = delayClue.possibleSuits.iterator().next();
                delayClue.possibleSuits = new HashSet<>();
            }
            if (! delayClue.suit.isBlank() || !delayClue.possibleSuits.isEmpty())
                focusClues.add(delayClue);
        }

        //save clues
        if (focusIndex == chopPosition)
        {
            if (clue.value == 5) //five saves
            {
                Clue fiveSaveClue = new Clue(ClueType.FIVE_SAVE, 5);
                for (String suit : Tile.SUIT_INDEX)
                {
                    Tile possibleFive = new Tile(new Clue(ClueType.NULL, 5, suit));
                    if (!game.canSee(this, possibleFive) && !game.isPlayable(possibleFive) && !focusClues.contains(new Clue(ClueType.DELAYED_PLAY, possibleFive)))
                        fiveSaveClue.possibleSuits.add(suit);
                }
                if (fiveSaveClue.possibleSuits.size() == 1)
                {
                    fiveSaveClue.suit = fiveSaveClue.possibleSuits.iterator().next();
                    fiveSaveClue.possibleSuits = new HashSet<>();
                }
                if (!fiveSaveClue.suit.isBlank() || !fiveSaveClue.possibleSuits.isEmpty()) //don't add a save clue if there are no possible suits for it to be
                    focusClues.add(fiveSaveClue);
            }
            else if (clue.value == 2) //two save
            {
                Clue twoSaveClue = new Clue(ClueType.TWO_SAVE, 2);
                for (String suit : Tile.SUIT_INDEX)
                {
                    Tile possibleTwo = new Tile(new Clue(ClueType.NULL, 2, suit));
                    if (!game.canSeeInOtherHands(this, possibleTwo) && !game.isPlayable(possibleTwo) && !game.isUseless(possibleTwo) && !focusClues.contains(new Clue(ClueType.DELAYED_PLAY, possibleTwo)))
                        twoSaveClue.possibleSuits.add(suit);
                }
                if (twoSaveClue.possibleSuits.size() == 1)
                {
                    twoSaveClue.suit = twoSaveClue.possibleSuits.iterator().next();
                    twoSaveClue.possibleSuits = new HashSet<>();
                }
                if (!twoSaveClue.suit.isBlank() || !twoSaveClue.possibleSuits.isEmpty()) //don't add a save clue if there are no possible suits for it to be
                    focusClues.add(twoSaveClue);
            }
            else //critical saves
                for (Tile critTile : game.criticalTiles())
                    if (!game.isPlayable(critTile) && clue.matches(critTile) && !focusClues.contains(new Clue(ClueType.DELAYED_PLAY, critTile)) && ((critTile.value == 1 && game.numCanSee(this, critTile) < 3) || game.numCanSee(this, critTile) < 2))
                        focusClues.add(new Clue(ClueType.CRITICAL_SAVE, critTile.value, critTile.suit));
        }

        // using play focus clues, infer clues about other touched tiles based on Good Touch Principle
        focusTile.information = focusClues;
        ArrayList<Clue> touchClues = new ArrayList<>();
        boolean focusHasOnlyPlayClues = focusTile.hasOnlyPlayClues();
        if (focusHasOnlyPlayClues) //we can infer nothing of the other touched tiles if there is any potential save clues, as saves can violate GTP
            for (Clue focusClue :focusClues)
            {
                // suit play clues - other touched tiles must be a higher possible value than the focussed tile as they will be played later
                if (!focusClue.suit.isBlank())
                {
                    for (int value : Arrays.stream(Tile.ALL_VALUES).filter(v -> v > focusClue.value).toList())
                        touchClues.add(new Clue(ClueType.DELAYED_PLAY, value, clue.suit));
                }

                // value play clues - other touched tiles will be the same combination of
                if (focusClue.value != 0)
                {
                    Clue valuePlayClue = new Clue(ClueType.PLAY, focusClue.value);
                    Clue valueDelayClue = new Clue(ClueType.DELAYED_PLAY, focusClue.value);

                    for (String suit : focusClue.possibleSuits)
                    {
                        Tile testTile = new Tile(new Clue(ClueType.NULL, clue.value, suit));
                        if (game.isPlayable(testTile))
                            valuePlayClue.possibleSuits.add(suit);
                        else if (!game.isUseless(testTile))
                            valueDelayClue.possibleSuits.add(suit);
                    }

                    if (!valuePlayClue.possibleSuits.isEmpty())
                        touchClues.add(valuePlayClue);
                    if (!valueDelayClue.possibleSuits.isEmpty())
                        touchClues.add(valueDelayClue);
                }
            }

        ArrayList<Tile> touchedTiles = game.touchedTiles(hand, clue);
        for (int i = 0; i < handSize; i++)
        {
            if (i == focusIndex)
                continue; //skip the focus tile, it has its clues

            Tile handTile = hand[i];
            if (touchedTiles.contains(handTile))
            {
                handTile.hintedIdentity.suit = clue.suit.isBlank() ? handTile.hintedIdentity.suit : clue.suit;
                handTile.hintedIdentity.value = clue.value.equals(0) ? handTile.hintedIdentity.value : clue.value;
                if (focusHasOnlyPlayClues && !touchClues.isEmpty())
                    handTile.information.addAll(touchClues);
            }
            else
            {
                if (!clue.suit.isBlank() && handTile != null)
                    handTile.negativeSuitInformation.add(clue.suit);
                if (clue.value != 0 && handTile != null)
                    handTile.negativeValueInformation.add(clue.value);
            }
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
            if (handTile == null)
                continue;

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
            return tile != null && tile.inChopPosition;

        //if no new on the chop, focus is the leftmost
        return newTiles.get(0).equals(tile);
    }

    public int index() { return List.of(game.players).indexOf(this); }
    public String name() { return (name.isBlank() ? "Player " + (index()+1) : name); }





    // TODO: Need to consider if this needs a full overhaul or just an adjustment to the priorities of save/pley clues
    //  (look at the +4000 play boost for more thoughts on that)
    /* Order of Magnitude:
     *  - Do you NEED to give this clue (is it a clue? is it a critical/playable on the chop? is there another player without a guaranteed play between you and the player to be clued?)
     *  - Is this a play action?
     *  - What type of clue action is this (Play, Five save, Two save, critical save, delayed play (soon(tm)))
     *  - How many playable tiles does this play clue hit (0 for non-play clues)
     *  - How many tiles does the clue touch?
     *  - Colour Clue (3), Value Clue (2), Fill-In Tempo Clue (1), Non-Fill-In Tempo Clue (0)
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
                if (!clueAction.isTempo)
                    if (!clueAction.intendedClue.suit.isBlank())
                        priority += 3;
                    else
                        priority += 2;

                priority += 10 * matchedTiles(clueAction.intendedClue, game.players[clueAction.targetPlayer].hand, false);
                priority += 100 * matchedTiles(clueAction.intendedClue, game.players[clueAction.targetPlayer].hand, true)
                        * (clueAction.intendedClue.clueType == ClueType.PLAY ? 1 : 0);

                // prioritise a save clue over a play, over a delay
                ClueType clueType = clueAction.intendedClue.clueType;
                if (clueType == ClueType.DELAYED_PLAY)
                    priority += 1000;
                else if (clueType == ClueType.PLAY)
                    priority += 2000;
                else if (clueType == ClueType.CRITICAL_SAVE)
                    priority += 3000;
                else if (clueType == ClueType.TWO_SAVE)
                    priority += 4000;
                else if (clueType == ClueType.FIVE_SAVE)
                    priority += 5000;

                //TODO: this should only boost when the player ALSO has a save clue enumerated for them, as giving a play is better than a save
                //increase the priority of a play clue if the player has no play actions currently
                Player targetPlayer = game.players[clueAction.targetPlayer];
                if (clueType == ClueType.PLAY && !game.players[clueAction.targetPlayer].hasPlayAction())
                    priority += 4000;
                /*
                if ((clueType.isSaveClue() || (clueType == ClueType.PLAY && isFocus(targetPlayer.hand, targetPlayer.hand[targetPlayer.chopPosition], clueAction.intendedClue)))
                        && !game.players[clueAction.targetPlayer].hasPlayAction())
                    priority += 4000;
                 */


                // do *I* need to give this save clue //TODO: more complicated analysis of if you need to give the clue
                //that means the player that needs saving is next, or there will not be enough clues for the player prior
                // to the one who needs to clue if everyone beforehand uses a clue
                if ((isNextPlayer(clueAction.targetPlayer) || betweenPlayersCannotGiveClue()) && !clueAction.isTempo)
                    priority += 100000;
            }

            action.priority = priority;
        }

        Collections.sort(possibleActions);
    }



    public void setHandSize(int handSize)
    {
        this.handSize = handSize;
        hand = new Tile[handSize];
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

            if (tile == null || !tile.isClued())
                continue;

            sb.append("For the ").append(++i).append(i == 1 ? "st" : (i == 2 ? "nd" : (i == 3 ? "rd" : "th"))).append(" tile:\n");
            i--;
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

    @Override
    public String toString() { return (name.isBlank() ? "Player " + (index() + 1) : name); }

    public void updateChopPosition()
    {
        int firstNonCluedChop = -1;
        int firstUselessChop = -1;

        for (int i = handSize - 1; i >= 0; i--)
        {
            if (hand[i] == null)
                continue;

            hand[i].inChopPosition = false;
            if (chopMethod != ChopMethod.NON_CLUED && game.isUseless(hand[i]) && firstUselessChop == -1)
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

    public void updateTileClues()
    {
        for (Tile tile : hand)
        {
            if (tile == null)
                continue;

            //remove all clues if the tile is known to be useless
            if (game.isUseless(tile))
                tile.information = new ArrayList<>();

            ArrayList<Clue> cluesToRemove = new ArrayList<>();
            ArrayList<Clue> cluesToAdd = new ArrayList<>();
            for (Clue clue : tile.information)
            {
                //remove play clues if the tile has been played and is now useless or if it is impossible (play clue with a suit/value we know the tile isn't)
                if (clue.clueType == ClueType.PLAY &&
                        (game.isUseless(new Tile(clue)) || tile.negativeSuitInformation.contains(clue.suit) || tile.negativeValueInformation.contains(clue.value)))
                    cluesToRemove.add(clue);

                //make save clues and delay clues into play clues if the tile has become playable
                if ((clue.clueType.isSaveClue() || clue.clueType == ClueType.DELAYED_PLAY) && game.isPlayable(clue))
                    clue.clueType = ClueType.PLAY;

                //remove definitive clues if you can see all instances of the tile it is trying to save
                int numCanSee = game.numCanSee(this, new Tile(clue));
                if (clue.isDefinitive() && ((clue.value == 1 && numCanSee == 3) || (clue.value == 5 && numCanSee == 1) || (numCanSee == 2)))
                    cluesToRemove.add(clue);

                //update possible suits - begin with suits that we have negative information on
                ArrayList<String> removeSuits = new ArrayList<>(tile.negativeSuitInformation);
                for (String suit : clue.possibleSuits)
                {
                    Tile inPlay = game.inPlay[Tile.suitIndex(suit)];

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
                removeSuits.forEach(clue.possibleSuits::remove);
                cluesToAdd = (ArrayList<Clue>)cluesToAdd.stream().filter(c -> !tile.negativeSuitInformation.contains(c.suit)).collect(Collectors.toList());

                if (clue.suit.isBlank() && clue.possibleSuits.size() == 1)
                {
                    clue.suit = clue.possibleSuits.iterator().next();
                    clue.possibleSuits = new HashSet<>();
                }
            }
            tile.information.removeAll(cluesToRemove);
            tile.information.addAll(cluesToAdd);

            cluesToAdd = new ArrayList<>();
            cluesToRemove = new ArrayList<>();
            ArrayList<Clue> cluesByType;
            for (ClueType clueType : ClueType.values())
            {
                if (clueType == ClueType.NULL)
                    continue;

                cluesByType = tile.information.stream().filter(c -> c.clueType == clueType).collect(Collectors.toCollection(ArrayList::new));
                if (cluesByType.size() < 2)
                    continue;

                for (int value : Tile.ALL_VALUES)
                {
                    HashSet<String> compiledSuits = new HashSet<>();

                    for (Clue clue : cluesByType)
                        if (clue.value == value)
                        {
                            if (!clue.suit.isBlank())
                                compiledSuits.add(clue.suit);
                            else
                                compiledSuits.addAll(clue.possibleSuits);
                            cluesToRemove.add(clue);
                        }

                    if (!compiledSuits.isEmpty())
                    {
                        Clue compiledClue = new Clue(clueType, value);
                        if (compiledSuits.size() == 1)
                            compiledClue.suit = compiledSuits.iterator().next();
                        else
                            compiledClue.possibleSuits = new HashSet<>(compiledSuits);
                        cluesToAdd.add(compiledClue);
                    }
                }
            }

            tile.information.removeAll(cluesToRemove);
            tile.information.addAll(cluesToAdd);
        }
    }

    private boolean betweenPlayersCannotGiveClue()
    {
        int mod = game.players.length;
        for (int i = index() + 1; i < index() + game.players.length; i++)
        {
            // skip checking yourself, skip people who have play clues when you don't and skip people who won't have clues if everyone before them uses one
            Player playerToCheck = game.players[i % mod];
            if (playerToCheck == this || (!hasPlayAction() && playerToCheck.hasPlayAction()) || game.clues - i <= 0)
                continue;
            return false;
        }

        return true;
    }

    private boolean isGoodTouch(Tile[] hand, int playerIndex, Clue clue)
    {
        //are there any duplicates in touchedTiles? If so, bad touch
        ArrayList<Tile> touchedTiles = game.touchedTiles(hand, clue);
        HashSet<Tile> uniqueTouchedTiles = new HashSet<>(touchedTiles);
        if (uniqueTouchedTiles.size() < touchedTiles.size())
            return false;

        for (Tile touchedTile : touchedTiles)
        {
            //save clues can violate good touch - thus for the purpose of this method they always count as good touch
            if (clue.clueType.isSaveClue())
                return true;

            //has a tile that would be touched already in play? if so, bad touch
            for (Tile ip : game.inPlay)
                if (ip != null && touchedTile.value <= ip.value && touchedTile.suit.equals(ip.suit))
                    return false;

            //has a tile that would be touched been clued in another player's hand?
            for (int i = 0; i < game.players.length; i++)
            {
                // skip i = playerIndex, as we check against the same hand earlier
                if (i == playerIndex)
                    continue;

                for (int j = 0; j < handSize; j++)
                {
                    Tile handTile = game.players[i].hand[j];
                    if (handTile == null)
                        continue;

                    //your hand, you should not clue if it *might* violate GTP based on clues you have
                    else if (i == index())
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

    private boolean isNextPlayer(int playerIndex) { return turnsTillPlayer(playerIndex) == 1; }
    private boolean isTempo(Tile[] hand, Tile tile, Clue clue) { return (isFocus(hand, tile, clue) && tile.isClued()); }

    private int matchedTiles(Clue clue, Tile[] hand, boolean matchPlayableOnly)
    {
        int matched = 0;

        for (Tile tile : hand)
            if (tile != null && (tile.value == clue.value || tile.suit.equals(clue.suit)))
                if (!matchPlayableOnly || game.isPlayable(tile))
                    matched++;

        return matched;
    }

    private boolean mightSeePlayClueInOwnHand(Tile checkTile, int skipTile)
    {
        for (int i = 0; i < handSize; i++)
        {
            if (i == skipTile)
                continue;
            Tile tile = hand[i];
            if (tile != null && tile.matches(checkTile))
                return true;
        }
        return false;
    }

    private boolean noOtherMatchingTwos(Player[] allPlayers, String suit, int firstTwosPlayerIndex)
    {
        for (int i = 0; i < allPlayers.length; i++)
            for (Tile tile : allPlayers[i].hand)
            {
                if (i == index())
                    continue;
                if (tile != null && tile.value == 2 && suit.equals(tile.suit) && (i != firstTwosPlayerIndex || !tile.inChopPosition))
                    return false;
            }
        return true;
    }

    private int turnsTillPlayer(int playerIndex)
    {
        int mod = game.players.length;
        return ((playerIndex - index()) % mod + mod) % mod;
    }
}
