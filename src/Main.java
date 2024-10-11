import org.apache.commons.collections4.CollectionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

//TODO: Cleanup code so that it is easier to read and modify bot behaviour.
//TODO: Refactor code so that behaviour is containable in configurable levels of conventions.
//TODO: Store not just human inputs but state changes by all players so that rollback/roll-forward can be performed.
public class Main {

    public static final int MAX_HAND_SIZE = 5;
    public static final int MAX_CLUES = 8;
    public static final int MAX_STRIKES = 3;
    public static final int MAX_POSSIBLE_SCORE = 25;
    public static final String SUITS = "bgryw";
    public static final String VALUES = "12345";

    public static Player[] allPlayers;
    public static int handSize;
    public static int clues;
    public static int score;
    public static int strikes;
    public static int maxScore;
    public static Tile[] inPlay;
    public static HashMap<Tile, Integer> discarded;
    public static ArrayList<Tile> deck;

    public static void main(String[] args) throws IOException
    {
        /* clue equality tests, all passed, probably should just remove later
        Clue x = new Clue(ClueType.NULL, 1, "b");
        Clue y = new Clue(ClueType.NULL, 1, "b");
        Clue z = new Clue(ClueType.NULL, 1, "b");

        if (x.equals(x))
            System.out.println("Clue passed reflexive test.");
        if (x.equals(y) && y.equals(x))
            System.out.println("Clue passed symmetric test.");
        if (x.equals(y) && y.equals(z) && x.equals(z))
            System.out.println("Clue passed transitive test.");
        if (x.equals(y) && x.equals(y) && x.equals(y) && x.equals(y) && x.equals(y))
            System.out.println("Clue passed consistent test.");
         */

        ArrayList<String> humanPlayerMoves = new ArrayList<>();
        maxScore = MAX_POSSIBLE_SCORE;
        handSize = MAX_HAND_SIZE;
        clues = MAX_CLUES;
        strikes = 0;
        score = 0;
        inPlay = new Tile[5];
        discarded = new HashMap<>();
        deck = new ArrayList<>(Arrays.asList(
                Tile.b1, Tile.b1, Tile.b1, Tile.b2, Tile.b2, Tile.b3, Tile.b3, Tile.b4, Tile.b4, Tile.b5,
                Tile.g1, Tile.g1, Tile.g1, Tile.g2, Tile.g2, Tile.g3, Tile.g3, Tile.g4, Tile.g4, Tile.g5,
                Tile.r1, Tile.r1, Tile.r1, Tile.r2, Tile.r2, Tile.r3, Tile.r3, Tile.r4, Tile.r4, Tile.r5,
                Tile.y1, Tile.y1, Tile.y1, Tile.y2, Tile.y2, Tile.y3, Tile.y3, Tile.y4, Tile.y4, Tile.y5,
                Tile.w1, Tile.w1, Tile.w1, Tile.w2, Tile.w2, Tile.w3, Tile.w3, Tile.w4, Tile.w4, Tile.w5
        ));
        long seed = 2;
        Random rng = new Random(seed);
        Collections.shuffle(deck, rng);

        Scanner readInput = new Scanner(System.in);

        Player player = new Player(MAX_HAND_SIZE, 0, ChopMethod.USELESS_MAY_BE_CHOP, false);
        Player bot = new Player(MAX_HAND_SIZE, 1, ChopMethod.USELESS_MAY_BE_CHOP, false);
        allPlayers = new Player[] {player, bot};

        //deal
        for (int i = 0; i < MAX_HAND_SIZE; i++)
            for (Player p : allPlayers)
                p.deal(deck.remove(0));
        player.updateChopPosition();
        bot.updateChopPosition();

        int gameTurn = 0;
        int playerTurn = 0;
        int countdown = allPlayers.length;
        boolean keepPlaying = true;
        try
        {
            while (strikes < MAX_STRIKES && score < maxScore && countdown > 0 && keepPlaying)
            {
                //System.out.println("Player " +(playerTurn+1)+ "'s Turn:");
                gameTurn++;
                if (deck.isEmpty())
                    countdown--;

                System.out.println(printGameInfo());
                System.out.print("In play : " + print(inPlay));
                System.out.println(printForPlayer(allPlayers, 2));
                if (!discarded.isEmpty())
                    System.out.println("Discard : " + print(discarded));

                if (allPlayers[playerTurn].isHuman)
                {
                    while (true)
                    {
                        System.out.printf("%nWhat do you do? "); //put this in an input loop

                        /* input documentation
                         * c,(2-5),[1-5bgryw]   - clue,player #,given clue
                         * p,(1-HANDSIZE)       - play,position played from
                         * d,(1-HANDSIZE)       -discard,position discarded from
                         */
                        String input = readInput.next();
                        String[] splitInput = input.split(",");
                        try
                        {
                            if ("t".equals(splitInput[0]))
                            {
                                System.out.println(player.thoughts());
                                continue;
                            }
                            else if ("x".equals(splitInput[0]))
                                keepPlaying = false;
                            else if ("c".equals(splitInput[0]))
                            {
                                if (clues == 0)
                                {
                                    System.out.println("You have no clues left to give; you myst discard or play.");
                                    continue;
                                }
                                clue(splitInput[1], splitInput[2]);
                            }
                            else if ("p".equals(splitInput[0]))
                                play(playerTurn, splitInput[1]); //0 is hard-coded first player until we make the human anything but first player
                            else if ("d".equals(splitInput[0]))
                                discard(playerTurn, splitInput[1]); //0 is hard-coded first player until we make the human anything but first player
                            else
                            {
                                System.out.println("No valid input detected, please try again");
                                continue;
                            }
                        }
                        catch (ArrayIndexOutOfBoundsException aiobe)
                        {
                            System.out.println("I could not understand the input: " + input + ". Please try again.");
                            continue;
                        }

                        humanPlayerMoves.add(gameTurn + " : " + input);
                        break;
                    }
                }
                else
                {
                    System.out.println(allPlayers[playerTurn].thoughts());
                    allPlayers[playerTurn].enumerateActions();
                    allPlayers[playerTurn].prioritiseActions();
                    System.out.println(print(allPlayers[playerTurn].possibleActions));
                    allPlayers[playerTurn].executeFirstAction();
                }

                for (Player p : allPlayers)
                    p.updateTileClues();
                playerTurn = ++playerTurn % allPlayers.length;
                System.out.println();
            }

            System.out.println("The game has ended via " + (countdown == 0 ? "decking out" : (
                    strikes == MAX_STRIKES ? "3 strikes" : (
                            score == maxScore ? "attaining a perfrect score" : "the player ending it early"
                    )
            )) + " with a final score of " + score + " points.");
        }
        catch (Exception e)
        {
            System.out.println("Encountered exception: " + e);
            System.out.println("Stack trace:");
            e.printStackTrace();
        }
        finally
        {
            OffsetDateTime now = OffsetDateTime.now();
            FileWriter writer = new FileWriter("game-actions " + now.toString().replace(":", "-") + ".txt");
            writer.write("Run Seed: " + seed + System.lineSeparator());
            for (String str : humanPlayerMoves)
                writer.write(str + System.lineSeparator());
            writer.close();
        }
    }

    public static boolean canSee(int self, Tile lookingFor) { return numCanSee(self, lookingFor) > 0; }
    public static int numCanSee(int self, Tile lookingFor)
    {
        int numCanSee = 0;

        for (Tile tile : inPlay)
            if (tile != null && tile.suit.equals(lookingFor.suit) && lookingFor.value <= tile.value)
                numCanSee++;

        for (int i = 0; i < allPlayers.length; i++)
        {
            if (i == self)
                continue;
            for (Tile tile : allPlayers[i].hand)
                if (tile != null && lookingFor.equals(tile))
                    numCanSee++;
        }

        for (Tile tile : discarded.keySet())
            if (tile.equals(lookingFor))
                numCanSee++;

        return numCanSee;
    }

    public static void clue(String otherPlayer, String clueValue)
    {
        int cluedPlayer = Integer.parseInt(otherPlayer) - 1;
        Clue clue = new Clue(ClueType.NULL);
        if (SUITS.contains(clueValue))
            clue.suit = clueValue;
        if (VALUES.contains(clueValue))
            clue.value = Integer.parseInt(clueValue);
        clue(cluedPlayer, clue);
    }

    public static void clue(int cluedPlayer, Clue clue)
    {
        clues--;
        ArrayList<Tile> touchedTiles = touchedTiles(allPlayers[cluedPlayer].hand, clue);
        //TODO: rework this whole method.
        //TODO: start with identifying the focus tile index, then figure out what clues to apply to it (including that it was the focus)
        //TODO: then, figure out what clues (if any) should be applied to other touched tiles
        //TODO: then, iterate the hand, skipping the focus tile, apply touch clue if a touched tile, and negative informaiton if not

        for (int i = 0; i < handSize; i++)
        {
            Player player = allPlayers[cluedPlayer];
            Tile tile = player.hand[i];
            boolean isFocus = Player.isFocus(player.hand, tile, clue);

            // put a NULL clue on each tile that matches (identity)
            if (tile.value == clue.value || tile.suit.equals(clue.suit))
            {
                tile.hintedIdentity.value = (clue.value != 0 ? clue.value : tile.hintedIdentity.value);
                tile.hintedIdentity.suit = (!clue.suit.isBlank() ? clue.suit : tile.hintedIdentity.suit);
            }
            else //apply negative information to non-clued tiles
            {
                if (!clue.suit.isBlank())
                    tile.negativeSuitInformation.add(clue.suit);
                if (clue.value != 0)
                    tile.negativeValueInformation.add(clue.value);

            }

            // if this is the focussed tile, intuit more information
            if (isFocus)
            {
                tile.information = new ArrayList<>();
                if (tile.inChopPosition)
                {
                    if (clue.value == 5)
                    {
                        ArrayList<String> possibleSuits = new ArrayList<>();
                        for (String suit : Tile.SUIT_INDEX)
                        {
                            Tile possibleFive = new Tile(5, suit);
                            possibleFive.hintedIdentity = new Clue(ClueType.NULL, 5, suit);
                            if (!canSee(cluedPlayer, possibleFive) && !possibleFive.isPlayable())
                                possibleSuits.add(suit);
                        }

                        if (!possibleSuits.isEmpty()) //don't add a save clue if there are no possible suits for it to be
                        {
                            Clue fiveSaveClue = new Clue(ClueType.FIVE_SAVE, 5);
                            fiveSaveClue.possibleSuits = possibleSuits;
                            tile.information.add(fiveSaveClue);
                        }
                    }
                    if (clue.value == 2)
                    {
                        ArrayList<String> possibleSuits = new ArrayList<>();
                        for (String suit : Tile.SUIT_INDEX)
                        {
                            Tile possibleTwo = new Tile(2, suit);
                            possibleTwo.hintedIdentity = new Clue(ClueType.NULL, 2, suit);
                            if (numCanSee(cluedPlayer, possibleTwo) < 2 && !possibleTwo.isUseless() && !possibleTwo.isPlayable())
                                possibleSuits.add(suit);
                        }

                        if (!possibleSuits.isEmpty()) //don't add a save clue if there are no possible suits for it to be
                        {
                            Clue twoSaveClue = new Clue(ClueType.TWO_SAVE, 2);
                            twoSaveClue.possibleSuits = possibleSuits;
                            tile.information.add(twoSaveClue);
                        }
                    }

                    for (Tile critTile : criticalTiles())
                        if (clue.matches(critTile) && ((critTile.value == 1 && numCanSee(cluedPlayer, critTile) < 3) || numCanSee(cluedPlayer, critTile) < 2))
                            tile.information.add(new Clue(ClueType.CRITICAL_SAVE, critTile.value, critTile.suit));
                }

                // possible play clues
                if (!clue.suit.isBlank())
                {
                    Tile inPlayTile  = inPlay[Tile.suitIndex(clue.suit)];
                    tile.information.add(new Clue(ClueType.PLAY, inPlayTile == null ? 1 : inPlayTile.value + 1, clue.suit));
                    //TODO: infer things about non-focus tiles on a suit play clue (good touch, i.e. they must be of a value greater than what the play clue implies/what the tile is known to be)
                }

                if (clue.value != 0)
                {
                    ArrayList<String> otherSuits = new ArrayList<>();
                    for (Player otherPlayer : allPlayers)
                    {
                        if (player.equals(otherPlayer))
                            continue;
                        for (Tile checkTile : otherPlayer.hand)
                            if (checkTile.value == clue.value && checkTile.hasOnlyPlayClues())
                                otherSuits.add(checkTile.suit);
                    }
                    ArrayList<String> checkSuits = (ArrayList<String>)CollectionUtils.disjunction(List.of(Tile.SUIT_INDEX), otherSuits);
                    ArrayList<String> playSuits = new ArrayList<>();
                    for (String suit : checkSuits)
                    {
                        Clue checkPlayable = new Clue(ClueType.PLAY, clue.value, suit);
                        if (checkPlayable.isPlayable())
                            playSuits.add(suit);
                    }
                    if (!playSuits.isEmpty())
                    {
                        Clue valuePlayClue = new Clue(ClueType.PLAY, clue.value);
                        valuePlayClue.possibleSuits = playSuits;

                        //add to all touched tiles, as the same clue should apply to each if good touch has been followed.
                        for (Tile touchedTile : touchedTiles)
                            touchedTile.information.add(valuePlayClue);
                    }
                }
                //TODO: determine between immediate play and delayed play clues
                //TODO: put prompt and finesse clues on relevant players (other than the clued one)
            }
            tile.updateIdentityFromNegativeInformation();
        }
        allPlayers[cluedPlayer].updateChopPosition();
    }

    public static ArrayList<Tile> criticalTiles()
    {
        ArrayList<Tile> criticalTiles = new ArrayList<>();
        for (Tile tile : discarded.keySet())
            if ((tile.value == 1 && discarded.get(tile) == 2) || discarded.get(tile) == 1 && (tile.value >= 2 && tile.value <= 4))
                criticalTiles.add(tile);
        return criticalTiles;
    }

    public static void discard(int currentPlayer, String discardPosition)
    {
        int position = Integer.parseInt(discardPosition) - 1;
        discard(currentPlayer, position);
    }

    public static void discard(int currentPlayer, int position)
    {
        Tile discardTile = new Tile(allPlayers[currentPlayer].hand[position]);
        allPlayers[currentPlayer].hand[position] = null;
        discarded.merge(discardTile, 1, Integer::sum);
        clues += (clues < 8 ? 1 : 0);
        //determineMaxScore(tileDiscarded);
        allPlayers[currentPlayer].shiftTiles();
        if (!deck.isEmpty())
            allPlayers[currentPlayer].deal(deck.remove(0)); //0 index is hard-coded first player until we get this in a loop
        allPlayers[currentPlayer].updateChopPosition();

        System.out.println("Player " + (currentPlayer+1) + " discarded a " + discardTile.toSimpleString());
    }

    public static void play(int currentPlayer, String playPosition)
    {
        int position = Integer.parseInt(playPosition) - 1;
        play(currentPlayer, position);
    }

    public static void play(int currentPlayer, int position)
    {
        Tile tilePlayed = allPlayers[currentPlayer].hand[position];
        Tile currentPlayedInSuit = inPlay[tilePlayed.suitIndex()];
        allPlayers[currentPlayer].hand[position] = null;
        System.out.println("Player " + (currentPlayer+1) + " played " + tilePlayed.toSimpleString());

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
            discarded.merge(tilePlayed, 1, Integer::sum);
            strikes += 1;
            System.out.println("Which is an invalid play. " + (MAX_STRIKES-strikes) + " strikes remaining.");
        }

        allPlayers[currentPlayer].shiftTiles();
        if (!deck.isEmpty())
            allPlayers[currentPlayer].deal(deck.remove(0));
        allPlayers[currentPlayer].updateChopPosition();
    }

    public static ArrayList<Tile> playableTiles()
    {
        ArrayList<Tile> playableTiles = new ArrayList<>();

        for (int i = 0; i < Main.inPlay.length; i++)
        {
            if (Main.inPlay[i] == null)
                playableTiles.add(new Tile(1, Tile.suitByIndex(i)));
            else if (Main.inPlay[i].value < 5)
                playableTiles.add((new Tile(Main.inPlay[i].value + 1, Main.inPlay[i].suit)));
        }

        return playableTiles;
    }

    public static String printForPlayer(Player[] allPlayers, int playerIndex)
    {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < allPlayers.length; i++)
        {
            Tile[] hand = allPlayers[i].hand;
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

    public static String printGameInfo()
    {
        return "  Clues Remaining: " + (clues == 0 ? ConsoleColours.RED + clues + ConsoleColours.RESET : clues) + //" | Efficiency: " + efficiency(clues));
                "\n  Tiles Remaining: " + deck.size() + //" | Pace: " + pace(score, deck.size(), allPlayers.length, maxScore));
                "\n  Current Strikes: " + (strikes == 2 ? ConsoleColours.RED + strikes + ConsoleColours.RESET : strikes);
    }

    public static String print(Tile[] inPlay)
    {
        return ConsoleColours.BLUE + (inPlay[0] == null ? "[--]" : inPlay[0].toSimpleString()) +
                ConsoleColours.BRIGHT_GREEN + (inPlay[1] == null ? "[--]" : inPlay[1].toSimpleString()) +
                ConsoleColours.RED + (inPlay[2] == null ? "[--]" : inPlay[2].toSimpleString()) +
                ConsoleColours.BRIGHT_YELLOW + (inPlay[3] == null ? "[--]" : inPlay[3].toSimpleString()) +
                ConsoleColours.BRIGHT_WHITE + (inPlay[4] == null ? "[--]" : inPlay[4].toSimpleString()) +
                ConsoleColours.RESET;
    }

    public static String print(HashMap<Tile, Integer> discarded)
    {
        SortedSet<Tile> sortedDiscards = new TreeSet<Tile>(discarded.keySet());
        boolean firstLine = true;
        boolean firstTile = true;
        String currentColour = "";
        StringBuilder sb = new StringBuilder();
        for (Tile tile : sortedDiscards)
        {
            if (currentColour.isBlank())
                currentColour = tile.consoleColour;
            else if (!currentColour.equals(tile.consoleColour))
            {
                sb.append(ConsoleColours.RESET);
                currentColour = tile.consoleColour;
                firstLine = false;
                firstTile = true;
            }

            if (!firstLine && firstTile)
                sb.append("\n          ");
            if (!firstTile)
                sb.append(ConsoleColours.RESET).append(", ");
            sb.append(discarded.get(tile)).append("x").append(currentColour).append(tile.toSimpleString());
            firstTile = false;
        }

        sb.append(ConsoleColours.RESET);
        return sb.toString();
    }

    public static String print(ArrayList<Action> playerActions)
    {
        boolean first = true;
        StringBuilder sb = new StringBuilder();
        for (Action action : playerActions)
        {
            if (!first)
                sb.append("\n");
            sb.append("I can ");
            if (action instanceof DiscardAction)
                sb.append("discard my chop tile.");
            else if (action instanceof PlayAction playAction)
            {
                int i = playAction.targetTile + 1;
                sb.append("play my ").append(i).append(i == 1 ? "st" : (i == 2 ? "nd" : (i == 3 ? "rd" : "th"))).append(" tile");
            }
            else if (action instanceof ClueAction clueAction)
                sb.append("clue Player ").append(clueAction.targetPlayer + 1).append(" with ").append(clueAction.intendedClue.toStringBrief());
            if (action.priority > 0)
                sb.append(" (priority=").append(action.priority).append(")");
            if (first)
                first = false;
        }

        return sb.toString();
    }

    public static ArrayList<Tile> touchedTiles(Tile[] hand, Clue clue)
    {
        ArrayList<Tile> touchedTiles = new ArrayList<>();
        for (Tile t : hand)
            if (clue.matches(t))
                touchedTiles.add(t);
        return touchedTiles;
    }

    private static float efficiency(int clues) { return 25f / clues; }
    private static int pace(int score, int tilesRemaining, int numberOfPlayers, int maximumPossibleScore) { return score + tilesRemaining + numberOfPlayers - maximumPossibleScore; }
}