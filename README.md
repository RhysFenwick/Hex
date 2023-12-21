## Hex

This started as a learning exercise for Java and has morphed into a crude game engine. Maths based off Amit Patel's [hexagonal grid reference guide](https://www.redblobgames.com/grids/hexagons/).

## Global TODO

### Behind the scenes
- Overload functions that can take either int[] or int,int for QR values
- Shift bulk of functions to Func.java
- Re-design architecture (GameCore holding overarching logic, level-specific logic in a GamePanel (LevelPanel?) with GamePanels being swapped out as needed)
- Split hex types into Terrain and Unit

### Game
- Level save functionality (doubles as level editor)
- Placeable units beyond stone



