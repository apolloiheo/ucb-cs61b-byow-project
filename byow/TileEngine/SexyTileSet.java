package byow.TileEngine;

import byow.TileEngine.TETile;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class SexyTileSet {
    private Map<Integer, TETile> tileset = new HashMap();
    public static int theme = 0;
    private int type = 0;

    public SexyTileSet() {
        tileset.put(0, new TETile('@', Color.black, Color.black, "empty"));
        tileset.put(1, new TETile('@', Color.white, Color.white, "walkable"));
        tileset.put(2, new TETile('-', Color.blue, Color.white, "player"));

        tileset.put(3, new TETile('@', Color.blue, Color.blue, "empty"));
        tileset.put(4, new TETile('@', Color.red, Color.red, "walkable"));
        tileset.put(5, new TETile('-', Color.white, Color.red, "player"));

        tileset.put(6, new TETile('@', Color.black, Color.black, "empty"));
        tileset.put(7, new TETile('@', Color.green, Color.green, "walkable"));
        tileset.put(8, new TETile('-', Color.red, Color.green, "player"));
    }

    public TETile get(int key) {
        return tileset.get((Integer)(3 * theme + key));
    }
}
