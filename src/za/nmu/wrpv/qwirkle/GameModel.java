package za.nmu.wrpv.qwirkle;

import java.util.*;

public class GameModel {
    private Player currentPlayer;
    private final List<Tile> bag = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();
    private final int playerCount;

    public GameModel(int pCount) {
        playerCount = pCount;
        initializeTiles();
        initializePlayers();
        initialDraw();
        initialPlayer();
    }

    public int getPlayerIndex(Player player) {
        return players.indexOf(player);
    }

    public Player getPlayer(String name) {
        for (Player player: players) {
            if (player.name.toString().equals(name)) return player;
        }
        return null;
     }

    private void initializeTiles() {
        ArrayList<Tile.Color> colors = new ArrayList<>(Arrays.asList(Tile.Color.BLUE, Tile.Color.GREEN, Tile.Color.ORANGE, Tile.Color.RED, Tile.Color.PURPLE, Tile.Color.YELLOW));
        ArrayList<Tile.Shape> shapes = new ArrayList<>(Arrays.asList(Tile.Shape.CIRCLE, Tile.Shape.CLOVER, Tile.Shape.DIAMOND, Tile.Shape.EPSTAR, Tile.Shape.FPSTAR, Tile.Shape.SQUARE));
        int j = 0;
        int k = 0;
        for (int i = 0; i < 108; i++) {
            if(j > 5) {
                j = 0;
                k++;
            }
            if(k > 5)
                k = 0;

            Tile temp = new Tile();
            temp.color = colors.get(k);
            temp.shape = shapes.get(j);

            bag.add(temp);
            j++;
        }
        Collections.shuffle(bag);
    }

    private void initializePlayers() {
        List<Player.Name> names = new ArrayList<>(Arrays.asList(Player.Name.PLAYER1, Player.Name.PLAYER2, Player.Name.PLAYER3, Player.Name.PLAYER4));
        List<String> colors = new ArrayList<>(Arrays.asList("red", "green", "blue", "purple"));
        for (int i = 0; i < playerCount; i++) {
            Player temp = new Player();
            temp.name = names.get(i);
            temp.color = colors.get(i);
            players.add(temp);
        }
    }

    private void initialDraw() {
        System.out.println(">>> INITIAL DRAW");
        for (Player player: players) {
            for (int i = 0; i < 6; i++) {
                Tile temp = bag.remove(i);
                player.tiles.add(temp);
            }
        }
    }

    private void initialPlayer() {
        int cCount = -1;
        int sCount = -1;
        for (Player player: players) {
            int tempCCount = getPlayerHighestCCount(player.tiles);
            int tempSCount = getPlayerHighestSCount(player.tiles);

            if (tempCCount > cCount || tempSCount > sCount) {
                cCount = tempCCount;
                sCount = tempSCount;
                currentPlayer = player;
            }
        }
    }

    private int getPlayerHighestCCount(List<Tile> playerTiles) {
        int red = 0;
        int orange = 0;
        int yellow = 0;
        int green = 0;
        int blue = 0;
        int purple = 0;
        for (Tile tile: playerTiles) {
            switch (tile.color) {
                case RED -> red++;
                case ORANGE -> orange++;
                case YELLOW -> yellow++;
                case GREEN -> green++;
                case BLUE -> blue++;
                case PURPLE -> purple++;
            }
        }

        return Collections.max(Arrays.asList(red, orange, yellow, green, blue, purple));
    }

    private int getPlayerHighestSCount(List<Tile> playerTiles) {
        int clover = 0;
        int fpstar = 0;
        int epstar = 0;
        int square = 0;
        int circle = 0;
        int diamond = 0;
        for (Tile tile: playerTiles) {
            switch (tile.shape) {
                case CLOVER -> clover++;
                case FPSTAR -> fpstar++;
                case EPSTAR -> epstar++;
                case SQUARE -> square++;
                case CIRCLE -> circle++;
                case DIAMOND -> diamond++;
            }
        }

        return Collections.max(Arrays.asList(clover, fpstar, epstar, square, circle, diamond));
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public List<Tile> getBag() {
        return bag;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
