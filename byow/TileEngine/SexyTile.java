package byow.TileEngine;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.HashMap;

public class SexyTile {
    public boolean emptyTile = true;
    public int tileType = 0;
    public TETile sexytile = null;

    public SexyTile (int type) {
        tileType = type;
        sexytile = new SexyTileSet().get(type);
    }
}
