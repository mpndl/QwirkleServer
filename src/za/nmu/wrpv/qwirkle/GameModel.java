package za.nmu.wrpv.qwirkle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameModel {
    public Player currentPlayer;
    private static int TILE_COUNT = 108;
    public static final int XLENGTH = 50;
    public static final int YLENGTH = 50;
    public Tile[][] board = new Tile[XLENGTH][YLENGTH];
    public List<Tile> bag = new ArrayList<>();
    public List<Player> players = new ArrayList<>();
    public final List<Tile> placed = new ArrayList<>();
    public final List<PlayerMessage> messages = new ArrayList<>();
    public int playerCount;

    public GameModel(int pCount) {
        playerCount = pCount;
        initializeTiles();
        initializePlayers();
        initialDraw();
        initialPlayer();
    }

    public static List<Player> clonePlayers(List<Player> players) {
        List<Player> playersCopy = new ArrayList<>();
        for (Player player: players) {
            playersCopy.add(clonePlayer(player));
        }
        return playersCopy;
    }

    public void setNewCurrentPlayer(Player player) {
        List<Player> differentFirstPlayer = players.stream().filter(p -> p.name != player.name).filter(p -> p.name != currentPlayer.name).toList();
        currentPlayer = differentFirstPlayer.get(0);
    }

    public static synchronized Player clonePlayer(Player player) {
        Player temp = new Player();
        temp.name = player.name;
        temp.color = player.color;
        temp.points = player.points;
        temp.tiles = player.tiles;
        return temp;
    }

    public boolean gameEnded() {
        return currentPlayer.tiles.size() == 0;
    }

    public void updatePlayerScore(Player player) {
        for (Player p: players) {
            if (p.name.toString().equals(player.name.toString())) {
                p.points = player.points;
                return;
            }
        }
    }

    public void updatePlayerTiles(Player player) {
        for (Player p: players) {
            if (p.name == player.name) {
                p.tiles = player.tiles;
                return;
            }
        }
    }

    public void turn() {int i = 0;
        for (; i < players.size(); i++) {
            if(currentPlayer.name == players.get(i).name) {
                i++;
                if(i >= players.size()) i = 0;
                currentPlayer = players.get(i);
                return;
            }
        }
    }

    public void removePlayer(Player player) {
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (player.name == p.name) {
                players.remove(i);
                return;
            }
        }
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
        for (int i = 0; i < TILE_COUNT; i++) {
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
