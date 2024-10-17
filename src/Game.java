import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Game
{
    public static final int MAX_HAND_SIZE = 5;
    public static final int MAX_CLUES = 8;
    public static final int MAX_STRIKES = 3;
    public static final int MAX_POSSIBLE_SCORE = 25;
    public static final String SUITS = "bgryw";
    public static final String VALUES = "12345";


    public HashMap<Tile, Integer> discarded;
    public int handSize;
    public Tile[] inPlay;
    public boolean keepPlaying;
    public int maxScore;
    public Player[] players;
    public int score;
    public int strikes;

    private final ArrayList<Tile> deck;

    public int clues;
    private int countdown;
    private int gameTurn;
    private int playerTurn;

    public Game(int seed, Player... players)
    {
        // Setup game variables
        clues = MAX_CLUES;
        countdown = players.length;
        discarded = new HashMap<>();
        gameTurn = 0;
        handSize = MAX_HAND_SIZE;
        inPlay = new Tile[5];
        keepPlaying = true;
        maxScore = MAX_POSSIBLE_SCORE;
        playerTurn = 0;
        score = 0;
        strikes = 0;

        // Set deck, seed and shuffle
        deck = new ArrayList<>(Arrays.asList(
                Tile.b1, Tile.b1, Tile.b1, Tile.b2, Tile.b2, Tile.b3, Tile.b3, Tile.b4, Tile.b4, Tile.b5,
                Tile.g1, Tile.g1, Tile.g1, Tile.g2, Tile.g2, Tile.g3, Tile.g3, Tile.g4, Tile.g4, Tile.g5,
                Tile.r1, Tile.r1, Tile.r1, Tile.r2, Tile.r2, Tile.r3, Tile.r3, Tile.r4, Tile.r4, Tile.r5,
                Tile.y1, Tile.y1, Tile.y1, Tile.y2, Tile.y2, Tile.y3, Tile.y3, Tile.y4, Tile.y4, Tile.y5,
                Tile.w1, Tile.w1, Tile.w1, Tile.w2, Tile.w2, Tile.w3, Tile.w3, Tile.w4, Tile.w4, Tile.w5
        ));
        Random rng = new Random(seed);
        Collections.shuffle(deck, rng);

        // Set player hand sizes
        this.players = players;
        if (this.players.length == 2 || this.players.length == 3)
            handSize = 5;
        if (this.players.length == 4 || this.players.length == 5)
            handSize = 4;
        for (Player player : this.players)
        {
            player.game = this;
            player.setHandSize(handSize);
        }

        // Deal tiles and set chop position
        for (int i = 0; i < MAX_HAND_SIZE; i++)
            for (Player player : this.players)
                player.deal(deck.remove(0));

        // Initial housekeeping to determine each player's possible actions and their priority
        housekeeping();
    }

    public String executeCurrentBotPlayerTurn()
    {
        Player currentPlayer = players[playerTurn];
        Action currentPlayersTopAction = currentPlayer.possibleActions.get(0);
        String result;
        if (currentPlayersTopAction == null)
        {
            result = "Ending the game early, due to " + currentPlayer.name() + " not having any legal actions.";
            keepPlaying = false;
        }
        else
            result = currentPlayersTopAction.execute(this, currentPlayer);
        housekeeping();
        return result;
    }

    public boolean isEarlyGame() { return discarded.keySet().isEmpty(); }
    public boolean isHumanPlayerTurn() { return players[playerTurn].isHuman; }
    public boolean isInProgress() { return strikes < MAX_STRIKES && score < maxScore && countdown > 0 && keepPlaying; }

    public String printPlayersViewOfHands(int playerIndex)
    {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < players.length; i++)
        {
            Tile[] hand = players[i].hand;
            output.append("\nPlayer ").append(i+1).append(": ");
            for (Tile tile : hand)
            {
                if (tile == null)
                    output.append("[--]");
                else
                    output.append(tile.toString(playerIndex == i));
            }
        }

        return output.toString();
    }

    public String printGameInfo()
    {
        return "  Clues Remaining: " + (clues == 0 ? ConsoleColours.RED + clues + ConsoleColours.RESET : clues) + //" | Efficiency: " + efficiency(clues));
                "\n  Tiles Remaining: " + deck.size() + //" | Pace: " + pace(score, deck.size(), allPlayers.length, maxScore));
                "\n  Current Strikes: " + (strikes == 2 ? ConsoleColours.RED + strikes + ConsoleColours.RESET : strikes);
    }

    boolean allDiscarded(Tile lookingFor)
    {
        int numDiscarded = numCanSeeInDiscard(lookingFor);
        return switch (lookingFor.value)
        {
            case 1 -> numDiscarded == 3;
            case 5 -> numDiscarded == 1;
            default -> numDiscarded == 2;
        };
    }

    boolean canSee(Player playersView, Tile lookingFor) { return numCanSee(playersView, lookingFor) > 0; }
    boolean canSeeInOtherHands(Player playersView, Tile lookingFor) { return numCanSeeInOtherHands(playersView, lookingFor) > 0; }
    boolean canSeePlayCluedInOtherHands(Player playersView, Tile lookingFor) { return numCanSeeInOtherHands(playersView, lookingFor, List.of(ClueType.PLAY, ClueType.DELAYED_PLAY)) > 0; };

    //TODO: fix this return
    String clue(Player cluedPlayer, Clue clue)
    {
        // Reduce number of clues and have the clued player interpret the clue
        clues--;
        cluedPlayer.interpretClue(clue);
        cluedPlayer.updateChopPosition();
        String result = "player 1 clued player 2 on clue suit/value";
        return "Need to fill in the clue response text still.";
    }

    ArrayList<Tile> criticalTiles()
    {
        ArrayList<Tile> criticalTiles = new ArrayList<>();
        for (Tile tile : discarded.keySet())
            if ((tile.value == 1 && discarded.get(tile) == 2) || discarded.get(tile) == 1 && (tile.value >= 2 && tile.value <= 4))
                criticalTiles.add(tile);
        return criticalTiles;
    }

    String discard(Player currentPlayer, int position)
    {
        // Remove tile from hand and merge into discarded tiles
        Tile discardTile = new Tile(currentPlayer.hand[position]);
        currentPlayer.hand[position] = null;
        discarded.merge(discardTile, 1, Integer::sum);

        // Recalculate scores (get a clue back if you aren't at 8, and check if max score has been reduced by the discard
        clues += (clues < 8 ? 1 : 0);
        //determineMaxScore(tileDiscarded);

        // Shift the tiles in the player's hand, deal a new tile (if any still in the deck), and update chop position
        currentPlayer.shiftTiles();
        if (!deck.isEmpty())
            currentPlayer.deal(deck.remove(0));
        currentPlayer.updateChopPosition();

        return currentPlayer.name() + " discarded a " + discardTile;
    }

    boolean isPlayable(Clue clue)
    {
        for (Tile playableTile : playableTiles())
        {
            if (!clue.suit.isBlank())
            {
                if (clue.equals(playableTile))
                    return true;
            }
            else
                for (String suit : Tile.SUIT_INDEX)
                    if (clue.equals(new Tile(clue.value, suit)))
                        return true;
        }

        return false;
    }

    boolean isPlayable(Tile tile)
    {
        for (Tile playableTile : playableTiles())
            if (tile.equals(playableTile))
                return true;

        return false;
    }

    boolean isUseless(Tile tile)
    {
        //cannot know if a tile is useless if we know nothing about a tile
        if (tile.hintedIdentity.value == 0 && tile.hintedIdentity.suit.isBlank())
            return false;

        //both suit and value are known
        if (tile.hintedIdentity.value != 0 && !tile.hintedIdentity.suit.isBlank())
            if (inPlay[Tile.suitIndex(tile.suit)] == null)
                return false;
            else
                return inPlay[Tile.suitIndex(tile.suit)].value >= tile.hintedIdentity.value;

        //suit is known, but a 5 in suit has been played
        if (!tile.hintedIdentity.suit.isBlank())
            if (inPlay[Tile.suitIndex(tile.suit)] == null)
                return false;
            else
                return inPlay[Tile.suitIndex(tile.suit)].value == 5;

        //value is known, but it or greater is played in all suits
        boolean suitWideValueCheck = true;
        for (Tile playTile : inPlay)
            suitWideValueCheck &= !(playTile == null || playTile.value < tile.hintedIdentity.value);

        return suitWideValueCheck;
    }

    int numCanSee(Player playersView, Tile lookingFor)
    {
        int numCanSee = 0;

        for (Tile tile : inPlay)
            if (tile != null && tile.suit.equals(lookingFor.suit) && lookingFor.value <= tile.value)
                numCanSee++;

        numCanSee += numCanSeeInOtherHands(playersView, lookingFor);

        for (Tile tile : discarded.keySet())
            if (tile.equals(lookingFor))
                numCanSee++;

        return numCanSee;
    }

    int numCanSeeInDiscard(Tile lookingFor)
    {
        int numCanSee = 0;

        for (Tile tile : discarded.keySet())
            if (tile.equals(lookingFor))
                numCanSee++;

        return numCanSee;
    }

    int numCanSeeInOtherHands(Player playersView, Tile lookingFor) { return numCanSeeInOtherHands(playersView, lookingFor, new ArrayList<>()); }
    int numCanSeeInOtherHands(Player playersView, Tile lookingFor, List<ClueType> withClueTypes)
    {
        int numCanSee = 0;

        for (Player player : players)
        {
            if (playersView.equals(player))
                continue;
            for (Tile tile : player.hand)
                if (tile != null && (lookingFor.equals(tile) && (withClueTypes.isEmpty() || tile.hasAnyClueTypes(withClueTypes))))
                    numCanSee++;
        }

        return numCanSee;
    }

    String play(Player currentPlayer, int position)
    {
        // Remove the tile from the player's hand
        Tile tilePlayed = currentPlayer.hand[position];
        currentPlayer.hand[position] = null;

        String result = currentPlayer.name() + " played a " + tilePlayed;
        Tile currentPlayedInSuit = inPlay[tilePlayed.suitIndex()];

        // Valid play
        if ((currentPlayedInSuit == null && tilePlayed.value == 1) || (currentPlayedInSuit != null && tilePlayed.value == currentPlayedInSuit.value + 1))
        {
            inPlay[tilePlayed.suitIndex()] = new Tile(tilePlayed);
            score += 1;
            if (tilePlayed.value == 5)
                clues += (clues < 8 ? 1 : 0);
        }
        else // Invalid play
        {
            result += ", which is an invalid play. " + (MAX_STRIKES - strikes) + " strikes remaining";
            discarded.merge(tilePlayed, 1, Integer::sum);
            strikes += 1;
        }

        // Shift the player's tiles, deal a tile (if the deck still has tiles), and update chop position
        currentPlayer.shiftTiles();
        if (!deck.isEmpty())
            currentPlayer.deal(deck.remove(0));
        currentPlayer.updateChopPosition();

        return result + ".";
    }

    ArrayList<Tile> playableTiles()
    {
        ArrayList<Tile> playableTiles = new ArrayList<>();

        for (int i = 0; i < inPlay.length; i++)
        {
            if (inPlay[i] == null)
                playableTiles.add(new Tile(1, Tile.suitByIndex(i)));
            else if (inPlay[i].value < 5)
                playableTiles.add((new Tile(inPlay[i].value + 1, inPlay[i].suit)));
        }

        return playableTiles;
    }

    ArrayList<Tile> touchedTiles(Tile[] hand, Clue clue)
    {
        ArrayList<Tile> touchedTiles = new ArrayList<>();
        for (Tile t : hand)
            if (clue.matches(t))
                touchedTiles.add(t);
        return touchedTiles;
    }

    /*
    private void clue(String otherPlayer, String clueValue)
    {
        int cluedPlayer = Integer.parseInt(otherPlayer) - 1;
        Clue clue = new Clue(ClueType.NULL);
        if (SUITS.contains(clueValue))
            clue.suit = clueValue;
        if (VALUES.contains(clueValue))
            clue.value = Integer.parseInt(clueValue);
        clue(cluedPlayer, clue);
    }

    private void discard(int currentPlayer, String discardPosition)
    {
        int position = Integer.parseInt(discardPosition) - 1;
        discard(currentPlayer, position);
    }
    */

    private void housekeeping()
    {
        // Each player updates their clues and their chop position, in turn
        for (Player player : players)
        {
            player.updateTileClues();
            player.updateChopPosition();
        }

        // Each player enumerates their possible actions and prioritises them
        for (Player player : players)
        {
            player.enumerateActions();
            player.prioritiseActions();
        }
    }

    /*
    private void play(int currentPlayer, String playPosition)
    {
        int position = Integer.parseInt(playPosition) - 1;
        play(currentPlayer, position);
    }
    */
}
