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
import java.util.stream.Collectors;

//TODO: Cleanup code so that it is easier to read and modify bot behaviour.
//TODO: Refactor code so that behaviour is containable in configurable levels of conventions.
//TODO: Store not just human inputs but state changes by all players so that rollback/roll-forward can be performed.
public class Main
{

    //TODO: FIX THE GODDAMN PLAY INTERFACE!!!
    public static void main(String[] args) throws IOException
    {
        Player player = new Player(ChopMethod.USELESS_MAY_BE_CHOP, false);
        Player bot = new Player(ChopMethod.USELESS_MAY_BE_CHOP, false);

        Game game = new Game(0, player, bot);
        while (game.isInProgress())
        {
            if (game.isHumanPlayerTurn())
            {
                //get player input
                //parse it into valid game interaction
                //execeute the requisite interaction
                /*
                Scanner readInput = new Scanner(System.in);
                ArrayList<String> humanPlayerMoves = new ArrayList<>();
                 */
                /*
                while (true)
                    {
                        System.out.printf("%nWhat do you do? "); //put this in an input loop

                        /* input documentation
                         * c,(2-5),[1-5bgryw]   - clue,player #,given clue
                         * p,(1-HANDSIZE)       - play,position played from
                         * d,(1-HANDSIZE)       -discard,position discarded from
                         *//*
                String input = readInput.next();
                String[] splitInput = input.split(",");
                try
                {
                    if ("t".equals(splitInput[0]))
                    {
                        System.out.println(this.players[playerTurn].thoughts());
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

            if (print)
                    System.out.println("Player " +(playerTurn+1)+ "'s Turn:");
                gameTurn++;
                if (deck.isEmpty())
                    countdown--;

                if (print)
                {
                    System.out.println(printGameInfo());
                    System.out.print("In play : " + print(inPlay));
                    System.out.println(printPlayersViewOfHands(-1));
                    if (!discarded.isEmpty())
                        System.out.println("Discard : " + print(discarded));
                }

                if (this.players[playerTurn].isHuman)
                {

                }
                else
                {

                }

                for (Player p : this.players)
                    p.updateTileClues();
                playerTurn = ++playerTurn % this.players.length;
                if (print)
                    System.out.println();
                 */
            }
            else
                game.executeCurrentBotPlayerTurn();
                //print(game.executeCurrentBotPlayerTurn());
        }
        /*
        catch (IndexOutOfBoundsException iobe)
        {
            System.out.println("Probably a player having their whole hand clued with things they cannot necessarily play.");
        }
        catch (Exception e)
        {
            System.out.println("Encountered exception: " + e);
            System.out.println("Stack trace:");
            e.printStackTrace();
        }
        finally
        {
            if (print)
            {
                OffsetDateTime now = OffsetDateTime.now();
                FileWriter writer = new FileWriter("game-actions " + now.toString().replace(":", "-") + ".txt");
                writer.write("Run Seed: " + seed + System.lineSeparator());
                for (String str : humanPlayerMoves)
                    writer.write(str + System.lineSeparator());
                writer.close();
            }
        }
        System.out.println("The game has ended via " + (countdown == 0 ? "decking out" : (
                    strikes == MAX_STRIKES ? "3 strikes" : (
                            score == maxScore ? "attaining a perfrect score" : "the player ending it early"
                    )
            )) + " with a final score of " + score + " points.");
         */
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

    private static float efficiency(int clues) { return 25f / clues; }
    private static int pace(int score, int tilesRemaining, int numberOfPlayers, int maximumPossibleScore) { return score + tilesRemaining + numberOfPlayers - maximumPossibleScore; }
}