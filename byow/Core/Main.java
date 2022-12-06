package byow.Core;

import byow.InputDemo.InputSource;
import byow.InputDemo.KeyboardInputSource;
import byow.TileEngine.SexyTile;
import byow.TileEngine.SexyTileSet;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;

import java.awt.*;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Random;

import java.io.FileWriter;
import java.io.IOException;

import edu.princeton.cs.algs4.StdDraw;
import org.json.JSONObject;
import org.json.simple.*;
import org.json.simple.parser.*;

/** This is the main entry point for the program. This class simply parses
 *  the command line inputs, and lets the byow.Core.Engine class take over
 *  in either keyboard or input string mode.
 */
public class Main {

    private static final int WIDTH = 100;
    private static final int HEIGHT = 60;
    private static final int BORDER = 8;

    private static TERenderer ter;
    private static TETile[][] world;

    private static int seed = -1;
    private static Random r;

    private static int playerX = 0, playerY = 0;
    private static int loadedX = -1, loadedY = -1;

    private static boolean showAll = true;
    private static int theme;

    public static void main(String[] args) {
        // starting input
        InputSource inputSource = new KeyboardInputSource();
        char c;
        char prev = '\u0000';

        // starter code
        if (args.length > 2) {
            System.out.println("Can only have two arguments - the flag and input string");
            System.exit(0);
        } else if (args.length == 2 && args[0].equals("-s")) {
            Engine engine = new Engine();
            engine.interactWithInputString(args[1]);
            System.out.println(engine.toString());
        } else {
            Engine engine = new Engine();
            engine.interactWithKeyboard();
        }

        ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        // keyboard input -- main menu
        renderMenu();
        while (inputSource.possibleNextInput()) {
            c = inputSource.getNextKey();
            if (c == 'L') {
                loadGame();
            }
            if (c == 'N') {
                newGame();
            }
            if (c == 'Q' && prev == ':') {
                saveQuit();
            }
            prev = c;
        }

    }

    private static void renderMenu() {
        StdDraw.clear(new Color(100, 0, 0));
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 40));
        StdDraw.text(50, 40, "CS 61B World");
        StdDraw.setFont(new Font("Monaco", Font.BOLD, 30));
        StdDraw.text(50, 30, "New Game (N)");
        StdDraw.text(50, 25, "Load Game (L)");
        StdDraw.text(50, 20, "Save and Quit (:Q)");
        StdDraw.show();
    }

    private static void newGame() {
        StdDraw.clear(new Color(0, 100, 0));
        StdDraw.show();

        // starting input
        InputSource inputSource = new KeyboardInputSource();
        char c;
        char prev = '\u0000';

        // keyboard input -- seed
        int inputSeed = 0;
        while (inputSource.possibleNextInput()) {
            c = inputSource.getNextKey();
            if ("1234567890".indexOf(c) >= 0) {
                inputSeed = Math.min(99999999, 10 * inputSeed + (int) c - (int) '0');
            }
            if (c == 'S') {
                seed = inputSeed;
                r = new Random(seed);
                theme = RandomUtils.uniform(r, 3);
                playGame();
            }
            prev = c;

            StdDraw.clear(new Color(0, 100, 0));
            StdDraw.text(50, 28, Integer.toString(inputSeed));
            StdDraw.show();
        }
    }

    private static void loadGame() {
        JSONParser parser = new JSONParser();
        try {
            org.json.simple.JSONObject obj = (org.json.simple.JSONObject)(parser.parse(new FileReader("./byow/Core/save.json")));
            seed = Math.round((long) obj.get("seed"));
            loadedX = Math.round((long)obj.get("playerX"));
            loadedY = Math.round((long)obj.get("playerY"));
            showAll = (boolean)obj.get("showAll");
            theme = Math.round((long)obj.get("theme"));
        } catch (Exception e) {
            // System.out.println(e);
        }
        playGame();
    }

    private static void playGame() {
        SexyTileSet.theme = theme;
        System.out.println(theme);
        // starting input
        InputSource inputSource = new KeyboardInputSource();
        char c;
        char prev = '\u0000';

        // world generation
        r = new Random(seed);
        randomWorldGeneration();

        // render world
        if (loadedX != -1) {
            playerX = loadedX;
            playerY = loadedY;
        }
        renderWorld();

        // keyboard input -- game
        while (inputSource.possibleNextInput()) {
            c = inputSource.getNextKey();
            if (c == 'W') {
                move(0, 1);
            }
            if (c == 'A') {
                move(-1, 0);
            }
            if (c == 'S') {
                move(0, -1);
            }
            if (c == 'D') {
                move(1, 0);
            }
            if (c == 'Q' && prev == ':') {
                saveQuit();
            }
            if (c == 'M') {
                showAll = !showAll;
            }
            renderWorld();
            prev = c;
        }

    }

    private static TETile[][] randomWorldGeneration() {
        SexyTile[][] sexyWorld = new SexyTile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                sexyWorld[i][j] = new SexyTile(0);
            }
        }

        int roomCount = 10;
        // generate rooms
        Room[] rooms = generateRooms(roomCount);

        // draw rooms
        drawRooms(sexyWorld, rooms);

        // create hallways
        // -- while percolating from every room
        int index1, index2;
        Integer[] floodfill = floodfillCheckRoomConnections(sexyWorld, rooms);
        while (Arrays.asList(floodfill).contains(1)) {
            index1 = Arrays.asList(floodfill).indexOf(1);
            while (true) {
                index2 = RandomUtils.uniform(r, roomCount);
                if (index1 != index2 && (int)floodfill[index1] != (int)floodfill[index2]) {
                    break;
                }
            }
            generateRoomConn(sexyWorld, createPossibleRoomConnection(rooms[index1], rooms[index2]));
            floodfill = floodfillCheckRoomConnections(sexyWorld, rooms);
        }

        // Render
        world = new TETile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                world[i][j] = sexyWorld[i][j].sexytile;
            }
        }

        // player loc
        playerX = rooms[0].posX;
        playerY = rooms[0].posY;

        return world;

    }

    private static Room[] generateRooms(int roomCount) {
        int roomWidthMin = 8;
        int roomWidthMax = 10;
        int roomHeightMin = 8;
        int roomHeightMax = 12;

        Room[] rooms = new Room[roomCount];
        for (int i = 0; i < roomCount; i++) {
            Room newRoom;
            while (true) {
                newRoom = new Room(
                        BORDER + RandomUtils.uniform(r, WIDTH - BORDER * 2),
                        BORDER + RandomUtils.uniform(r, HEIGHT - BORDER * 2),
                        roomWidthMin + RandomUtils.uniform(r, roomWidthMax - roomWidthMin + 1),
                        roomHeightMin + RandomUtils.uniform(r, roomHeightMax - roomHeightMin + 1)
                );

                if (validRoom(rooms, newRoom)) {
                    break;
                }
            }
            rooms[i] = newRoom;
        }

        // check rooms are within border of x2.5
        if (bordersEmpty(rooms)) {
            return generateRooms(roomCount);
        }

        return rooms;
    }

    private static void drawRooms(SexyTile[][] sexyWorld, Room[] rooms) {
        for (int i = 0; i < rooms.length; i++) {
            for (int j = 0; j < rooms[i].width; j++) {
                for (int k = 0; k < rooms[i].height; k++) {
                    try {
                        sexyWorld[rooms[i].posX + j][rooms[i].posY + k] = new SexyTile(1);
                    } catch (Exception e) {
                        // room out of bounds; ignore
                    }
                }
            }
        }
    }

    private static boolean validRoom(Room[] rooms, Room newRoom) {
        boolean widthOverlap, heightOverlap;

        for (Room r: rooms) {
            if (r == null) {
                continue;
            }

            widthOverlap = (
                    r.posX <= newRoom.posX && r.posX + r.width >= newRoom.posX
            ) || (
                    newRoom.posX <= r.posX && newRoom.posX + newRoom.width >= r.posX
            );
            heightOverlap = (
                    r.posY <= newRoom.posY && r.posY + r.height >= newRoom.posY
            ) || (
                    newRoom.posY <= r.posY && newRoom.posY + newRoom.height >= r.posY
            );
            if (widthOverlap && heightOverlap) {
                return false;
            }
        }

        return true;
    }

    private static boolean bordersEmpty(Room[] rooms) {
        SexyTile[][] emptyWorld = new SexyTile[WIDTH][HEIGHT];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                emptyWorld[i][j] = new SexyTile(0);
            }
        }
        drawRooms(emptyWorld, rooms);

        return bordersEmptyHelper(emptyWorld, 0, (int)(BORDER*2.5), 0, HEIGHT) ||
                bordersEmptyHelper(emptyWorld, WIDTH-(int)(BORDER*2.5), WIDTH, 0, HEIGHT) ||
                bordersEmptyHelper(emptyWorld, 0, WIDTH, 0, (int)(BORDER*2.5)) ||
                bordersEmptyHelper(emptyWorld, 0, WIDTH, HEIGHT-(int)(BORDER*2.5), HEIGHT);
    }

    private static boolean bordersEmptyHelper(SexyTile[][] emptyWorld,
                                              int xStart, int xEnd, int yStart, int yEnd) {
        for (int i=xStart; i<xEnd; i++) {
            for (int j=yStart; j<yEnd; j++) {
                if (emptyWorld[i][j].tileType != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    private static int[] createPossibleRoomConnection(Room r1, Room r2) {
        if (r2.posX < r1.posX) {
            return createPossibleRoomConnection(r2, r1);
        }

        boolean firstVertical = RandomUtils.uniform(r, 2) == 0;
        // edge cases
        if (r1.posX + r1.width >= WIDTH) {
            firstVertical = true;
        }
        if (r1.posY + r1.height >= HEIGHT && r1.posY < r2.posY) {
            firstVertical = false;
        }

        int startPosX, startPosY, dir1, nodePos, dir2;
        if (firstVertical && r1.posY < r2.posY) {
            dir1 = 1;
            startPosX = r1.posX + Math.max(0, RandomUtils.uniform(r, r1.width));
            startPosY = r1.posY + r1.height;
            nodePos = r2.posY + Math.min(HEIGHT - r2.posY, RandomUtils.uniform(r, r2.height));
            dir2 = startPosX < r2.posX ? 0 : 2;
        } else if (r1.posY < r2.posY) {
            dir1 = 0;
            startPosX = r1.posX + r1.width;
            startPosY = r1.posY + Math.min(HEIGHT - r1.posY, RandomUtils.uniform(r, r1.height));
            nodePos = r2.posX + Math.min(HEIGHT, RandomUtils.uniform(r, r2.width));
            dir2 = startPosY < r2.posY ? 1 : 3;
        } else if (firstVertical) {
            dir1 = 3;
            startPosX = startPosX = r1.posX + Math.max(0, RandomUtils.uniform(r, r1.width));
            startPosY = r1.posY - 1;
            nodePos = r2.posY + Math.min(HEIGHT - r2.posY, RandomUtils.uniform(r, r2.height));
            dir2 = startPosX < r2.posX ? 0 : 2;
        } else {
            dir1 = 0;
            startPosX = r1.posX + r1.width;
            startPosY = r1.posY + Math.min(HEIGHT - r1.posY, RandomUtils.uniform(r, r1.height));
            nodePos = r2.posX + Math.min(HEIGHT, RandomUtils.uniform(r, r2.width));
            dir2 = startPosY < r2.posY ? 1 : 3;
        }

        return new int[]{startPosX, startPosY, dir1, nodePos, dir2};
    }

    private static void generateRoomConn(SexyTile[][] world, int[] roomConn) {
        try {
            int startPosX, startPosY, dir1, nodePos, dir2;
            startPosX = roomConn[0];
            startPosY = roomConn[1];
            dir1 = roomConn[2];
            nodePos = roomConn[3];
            dir2 = roomConn[4];

            SexyTile oldTile;

            // dir 1
            if (dir1 == 0) {
                for (int i = startPosX; i <= nodePos; i++) {
                    if (world[i][startPosY].tileType != 0) {
                        return;
                    }
                    world[i][startPosY] = new SexyTile(1);
                }
            } else if (dir1 == 1) {
                for (int j = startPosY; j <= nodePos; j++) {
                    if (world[startPosX][j].tileType != 0) {
                        return;
                    }
                    world[startPosX][j] = new SexyTile(1);
                }
            } else if (dir1 == 3) {
                for (int j = startPosY; j >= nodePos; j--) {
                    if (world[startPosX][j].tileType != 0) {
                        return;
                    }
                    world[startPosX][j] = new SexyTile(1);
                }
            }

            // dir 2
            if (dir2 == 0) {
                for (int i = startPosX+1; i <= WIDTH; i++) {
                    if (world[i][nodePos].tileType != 0) {
                        return;
                    }
                    world[i][nodePos] = new SexyTile(1);
                }
            } else if (dir2 == 2) {
                for (int i = startPosX-1; i >= 0; i--) {
                    if (world[i][nodePos].tileType != 0) {
                        return;
                    }
                    world[i][nodePos] = new SexyTile(1);
                }
            } else if (dir2 == 1) {
                for (int j = startPosY+1; j <= HEIGHT; j++) {
                    if (world[nodePos][j].tileType != 0) {
                        return;
                    }
                    world[nodePos][j] = new SexyTile(1);
                }
            } else if (dir2 == 3) {
                for (int j = startPosY-1; j >=0 ; j--) {
                    if (world[nodePos][j].tileType != 0) {
                        return;
                    }
                    world[nodePos][j] = new SexyTile(1);
                }
            }


        } catch (Exception e) {
            // out of bounds error
        }
    }

    private static Integer[] floodfillCheckRoomConnections(SexyTile[][] world, Room[] rooms) {
        Integer[] floodfill = new Integer[rooms.length];
        for (int i=0; i<rooms.length; i++) {
            floodfill[i] = -1;
        }

        int[][] floodfillWorld = new int[WIDTH][HEIGHT];
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                floodfillWorld[i][j] = -1;
            }
        }

        int groups = 0;
        while (Arrays.asList(floodfill).contains(-1)) {
            int index = Arrays.asList(floodfill).indexOf(-1);
            floodfillHelper(world, floodfillWorld, floodfill, rooms, rooms[index].posX, rooms[index].posY, groups);
            groups++;
        }

        return floodfill;
    }

    private static void floodfillHelper(SexyTile[][] world, int[][] floodfillWorld, Integer[] floodfill,
                                        Room[] rooms, int x, int y, int group) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) {
            return;
        }
        if (world[x][y].tileType == 0) {
            return;
        }
        if (floodfillWorld[x][y] != -1) {
            return;
        }

        floodfillWorld[x][y] = group;
        for (int i=0; i<rooms.length; i++) {
            if (rooms[i].posX == x && rooms[i].posY == y) {
                floodfill[i] = group;
            }
        }


        floodfillHelper(world, floodfillWorld, floodfill, rooms, x-1, y, group);
        floodfillHelper(world, floodfillWorld, floodfill, rooms, x+1, y, group);
        floodfillHelper(world, floodfillWorld, floodfill, rooms, x, y-1, group);
        floodfillHelper(world, floodfillWorld, floodfill, rooms, x, y+1, group);
    }

    private static void move(int x, int y) {
        try {
            if (world[playerX + x][playerY + y].description() == "walkable") {
                playerX += x;
                playerY += y;
            }
        } catch (Exception e) {
            // out of bounds
        }
    }

    private static void renderWorld() {
        TETile[][] copyWorld = new TETile[WIDTH][HEIGHT];
        for (int i=0; i<WIDTH; i++) {
            for (int j=0; j<HEIGHT; j++) {
                copyWorld[i][j] = world[i][j];
            }
        }

        // showAll
        if (showAll) {
            for (int i=0; i<WIDTH; i++) {
                for (int j = 0; j < HEIGHT; j++) {
                    if ((i - playerX) * (i - playerX) + (j - playerY) * (j - playerY) > 15) {
                        copyWorld[i][j] = new SexyTileSet().get(0);
                    }
                }
            }
        }

        copyWorld[playerX][playerY] = new SexyTile(2).sexytile;
        ter.renderFrame(copyWorld);
    }

    private static void saveQuit() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("seed", seed);
        jsonObject.put("playerX", playerX);
        jsonObject.put("playerY", playerY);
        jsonObject.put("showAll", showAll);
        jsonObject.put("theme", theme);

        try {
            FileWriter file = new FileWriter("./byow/Core/save.json");
            file.write(jsonObject.toString());
            file.close();
        } catch (IOException e) {
            // ???
        }

        System.exit(0);
    }

}
