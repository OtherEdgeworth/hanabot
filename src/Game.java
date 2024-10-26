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
    public static final String NO_CLUES = "You have no clues left to give; you myst discard or play.";
    public static final String SUITS = "bgryw";
    public static final String VALUES = "12345";

    public HashMap<Tile, Integer> discarded;
    public int handSize;
    public Tile[] inPlay;
    public boolean keepPlaying;
    public int maxScore;
    public Player[] players;
    public int playerTurn;
    public int score;
    public long seed;
    public int strikes;

    private final ArrayList<Tile> deck;

    public int clues;
    private int countdown;
    private int gameTurn;

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
        playerTurn = -1; //set to -1 to account for housekeeping incrementing it
        score = 0;
        this.seed = seed;
        strikes = 0;

        // Set deck, seed and shuffle
        deck = new ArrayList<>(Arrays.asList(
                Tile.b1, Tile.b1, Tile.b1, Tile.b2, Tile.b2, Tile.b3, Tile.b3, Tile.b4, Tile.b4, Tile.b5,
                Tile.g1, Tile.g1, Tile.g1, Tile.g2, Tile.g2, Tile.g3, Tile.g3, Tile.g4, Tile.g4, Tile.g5,
                Tile.r1, Tile.r1, Tile.r1, Tile.r2, Tile.r2, Tile.r3, Tile.r3, Tile.r4, Tile.r4, Tile.r5,
                Tile.y1, Tile.y1, Tile.y1, Tile.y2, Tile.y2, Tile.y3, Tile.y3, Tile.y4, Tile.y4, Tile.y5,
                Tile.w1, Tile.w1, Tile.w1, Tile.w2, Tile.w2, Tile.w3, Tile.w3, Tile.w4, Tile.w4, Tile.w5
        ));
        Random rng = new Random(this.seed);
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

    public String clue(Player clueGiver, Player cluedPlayer, Clue clue)
    {
        // Reduce number of clues and have the clued player interpret the clue
        if (clues == 0)
            return NO_CLUES;
        clues--;
        cluedPlayer.interpretClue(clue);
        cluedPlayer.updateChopPosition();
        return clueGiver + " clued " + cluedPlayer + " on " + (clue.suit.isBlank() ? clue.value : clue.suit);
    }

    public Player currentPlayer() { return players[playerTurn]; }

    public String discard(Player currentPlayer, int position)
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

        return currentPlayer + " discarded a " + discardTile;
    }

    public String endReason()
    {
        StringBuilder sb = new StringBuilder("The game has ended via ");
        if (countdown == 0)
            sb.append("decking out");
        else if (strikes == MAX_STRIKES)
            sb.append("3 strikes");
        else if (score == maxScore)
            sb.append("attaining a perfrect score");
        else
            sb.append("the player ending it early");
        sb.append(" with a final score of ").append(score).append(" points.");
        return sb.toString();
    }

    public String executeCurrentBotPlayerTurn()
    {
        Player currentPlayer = currentPlayer();
        Action currentPlayersTopAction = currentPlayer.possibleActions.get(0);
        String result;
        if (currentPlayersTopAction == null)
        {
            result = "Ending the game early, due to " + currentPlayer + " not having any legal actions.";
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

    public String play(Player currentPlayer, int position)
    {
        // Remove the tile from the player's hand
        Tile tilePlayed = currentPlayer.hand[position];
        currentPlayer.hand[position] = null;

        String result = currentPlayer + " played a " + tilePlayed;
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

    public String playersViewOfHands(boolean debug)
    {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < players.length; i++)
        {
            Player player = players[i];
            Tile[] hand = player.hand;
            output.append(sameWidth(player.toString())).append(": ");
            int playerIndex = currentPlayer().index();
            for (Tile tile : hand)
            {
                if (tile == null)
                    output.append("[--]");
                else
                    output.append(tile.toString( playerIndex== i && !debug));
            }
            if (i < players.length - 1)
                output.append("\n");
        }
        if (!output.toString().isBlank())
            output.append("\n");

        return output.toString();
    }

    public String printGameInfo()
    {
        return "  Clues Remaining: " + (clues == 0 ? ConsoleColours.RED + clues + ConsoleColours.RESET : clues) + //" | Efficiency: " + efficiency(clues));
                "\n  Tiles Remaining: " + deck.size() + //" | Pace: " + pace(score, deck.size(), allPlayers.length, maxScore));
                "\n  Current Strikes: " + (strikes == 2 ? ConsoleColours.RED + strikes + ConsoleColours.RESET : strikes) + "\n";
    }

    public String sameWidth(String format)
    {
        int nameWidth = 9; //"Player X "
        for (Player player : players)
            if (!player.name.isBlank())
                nameWidth = Math.max(nameWidth, player.name.length());

        return String.format("%-" + nameWidth + "s", format);
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

    boolean canSeeInPlay(Tile lookingFor)
    {
        for (Tile tile : inPlay)
            if (tile != null && tile.suit.equals(lookingFor.suit) && lookingFor.value <= tile.value)
                return true;
        return false;
    }

    boolean canSeePlayCluedInOtherHands(Player playersView, Tile lookingFor) { return numCanSeeInOtherHands(playersView, lookingFor, List.of(ClueType.PLAY, ClueType.DELAYED_PLAY)) > 0; };

    ArrayList<Tile> criticalTiles()
    {
        ArrayList<Tile> criticalTiles = new ArrayList<>();
        for (Tile tile : discarded.keySet())
            if ((tile.value == 1 && discarded.get(tile) == 2) || discarded.get(tile) == 1 && (tile.value >= 2 && tile.value <= 4))
                criticalTiles.add(tile);
        return criticalTiles;
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
                    if (clue.equals(new Tile(suit, clue.value)))
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
        numCanSee += (canSeeInPlay(lookingFor) ? 1 : 0);
        numCanSee += numCanSeeInOtherHands(playersView, lookingFor);
        numCanSee += numCanSeeInDiscard(lookingFor);
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

    ArrayList<Tile> playableTiles()
    {
        ArrayList<Tile> playableTiles = new ArrayList<>();

        for (int i = 0; i < inPlay.length; i++)
        {
            if (inPlay[i] == null)
                playableTiles.add(new Tile(Tile.suitByIndex(i), 1));
            else if (inPlay[i].value < 5)
                playableTiles.add((new Tile(inPlay[i].suit, inPlay[i].value + 1)));
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

        playerTurn = ++playerTurn % this.players.length;
        gameTurn++;
        if (deck.isEmpty())
            countdown--;
    }
}
