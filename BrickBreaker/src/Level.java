import java.util.ArrayList;
import java.util.List;

public class Level {

	//Fields
	private final int ROW;
	private final int COL;
	private List<GameObj> bricks;  // The bricks
	
	private int brickCount = 0;
	
	Level(int row, int col){
		ROW = row;
		COL = col;
		
		float width = (Main.W - 80) / COL;
		float height = (Main.H/1.5f - 320) / ROW;
		
		bricks = new ArrayList<GameObj>();
		
		for (int x = 0; x < COL; x++) {
    	  for (int y = 0; y < ROW; y++) {
        	  bricks.add(new GameObj(x*width + 40, y*height + 160, width, height, Colour.CYAN));
        	  brickCount ++;
          }
      }
	}
	
	public int getBrickCount() {return brickCount;}
	
	public List<GameObj> getBricks() {return bricks;}
}
