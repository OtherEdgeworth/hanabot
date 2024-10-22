import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

//TODO: Cleanup code so that it is easier to read and modify bot behaviour.
//TODO: Refactor code so that behaviour is containable in configurable levels of conventions.
//TODO: Store not just human inputs but state changes by all players so that rollback/roll-forward can be performed.
public class Main
{
    public static void main(String[] args) throws IOException
    {
        Player miles = new Player("Miles", ChopMethod.USELESS_MAY_BE_CHOP, false);
        Player bot1 = new Player(ChopMethod.USELESS_MAY_BE_CHOP, false);
        Game game = new Game(0, miles, bot1);

        boolean debug = true;
        boolean logHumanActionsToFile = true;
        ArrayList<String> humanPlayerMoves = new ArrayList<>();
        while (game.isInProgress())
        {
            System.out.println();
            if (game.isHumanPlayerTurn() || debug)
                System.out.println(print(game, debug));

            if (game.isHumanPlayerTurn())
                humanPlayerMoves.add(parseInput(game));
            else
            {
                if (debug)
                {
                    String possibleActions = print(game.currentPlayer());
                    String thoughts = game.currentPlayer().thoughts();

                    if (!possibleActions.isBlank())
                        System.out.println(possibleActions);
                    if (!thoughts.isBlank())
                        System.out.println(thoughts);
                }
                System.out.println(game.executeCurrentBotPlayerTurn());
            }
        }

        if (logHumanActionsToFile || debug)
        {
            OffsetDateTime now = OffsetDateTime.now();
            FileWriter writer = new FileWriter("game-actions " + now.toString().replace(":", "-") + ".txt");
            writer.write("Run Seed: " + game.seed + System.lineSeparator());
            for (String str : humanPlayerMoves)
                writer.write(str + System.lineSeparator());
            writer.close();
        }

        System.out.println(game.endReason());
    }

    public static String parseInput(Game game)
    {
        Player currentPlayer = game.currentPlayer();
        System.out.println(currentPlayer + "'s Turn:");
        Scanner readInput = new Scanner(System.in);
        while (true)
        {
            System.out.printf("%nWhat do you do? ");

            //get player input
            /* input documentation
             * c,(2-5),[1-5bgryw]   - clue,player #,given clue
             * d,(1-HANDSIZE)       - discard,position discarded from
             * p,(1-HANDSIZE)       - play,position played from
             * t                    - get what a bot in the player's position would think
             * x                    - quit playing the game
             */
            String input = readInput.next();
            String[] splitInput = input.split(",");
            try
            {
                if ("t".equals(splitInput[0]))
                {
                    System.out.println(currentPlayer.thoughts());
                    continue;
                }
                else if ("x".equals(splitInput[0]))
                    game.keepPlaying = false;
                else if ("c".equals(splitInput[0]))
                {
                    int cluedPlayerIndex = Integer.parseInt(splitInput[1]);
                    String clueResult = game.clue(currentPlayer, game.players[cluedPlayerIndex], new Clue(ClueType.NULL, splitInput[2]));
                    System.out.println(clueResult);
                    if (Game.NO_CLUES.equals(clueResult))
                        continue;
                }
                else if ("p".equals(splitInput[0]))
                {
                    int tileIndexToPlay = Integer.parseInt(splitInput[1]);
                    System.out.println(game.play(currentPlayer, tileIndexToPlay));
                }
                else if ("d".equals(splitInput[0]))
                {
                    int tileIndexToDiscard = Integer.parseInt(splitInput[1]);
                    System.out.println(game.discard(currentPlayer, tileIndexToDiscard));
                }
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

            return game.playerTurn + " : " + input;
        }
    }

    public static String print(Game game, boolean debug)
    {
        return game.printGameInfo() +
                print(game, game.inPlay) +
                game.playersViewOfHands(debug) +
                print(game, game.discarded);
    }

    public static String print(Game game, Tile[] inPlay)
    {
        return game.sameWidth("In play") + ": " +
                ConsoleColours.BLUE + (inPlay[0] == null ? "[--]" : inPlay[0].toSimpleString()) +
                ConsoleColours.BRIGHT_GREEN + (inPlay[1] == null ? "[--]" : inPlay[1].toSimpleString()) +
                ConsoleColours.RED + (inPlay[2] == null ? "[--]" : inPlay[2].toSimpleString()) +
                ConsoleColours.BRIGHT_YELLOW + (inPlay[3] == null ? "[--]" : inPlay[3].toSimpleString()) +
                ConsoleColours.BRIGHT_WHITE + (inPlay[4] == null ? "[--]" : inPlay[4].toSimpleString()) +
                ConsoleColours.RESET + "\n";
    }

    public static String print(Game game, HashMap<Tile, Integer> discarded)
    {
        if (discarded.isEmpty())
            return "";

        SortedSet<Tile> sortedDiscards = new TreeSet<Tile>(discarded.keySet());
        boolean firstLine = true;
        boolean firstTile = true;
        String currentColour = "";
        StringBuilder sb = new StringBuilder(game.sameWidth("Discard")).append(": ");
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
        if (!sb.toString().isBlank())
            sb.append("\n");
        sb.append(ConsoleColours.RESET);
        return sb.toString();
    }

    public static String print(Player player)
    {
        ArrayList<Action> playerActions = player.possibleActions;
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
                sb.append("clue ").append(player).append(" with ").append(clueAction.intendedClue.toStringBrief());
            if (action.priority > 0)
                sb.append(" (priority=").append(action.priority).append(")");
            if (first)
                first = false;
        }
        if (!sb.toString().isBlank())
            sb.append("\n");
        return sb.toString();
    }

    private static float efficiency(int clues) { return 25f / clues; }
    private static int pace(int score, int tilesRemaining, int numberOfPlayers, int maximumPossibleScore) { return score + tilesRemaining + numberOfPlayers - maximumPossibleScore; }
}