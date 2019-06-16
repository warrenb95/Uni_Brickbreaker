import javax.sound.sampled.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Model of the game of breakout
 * @author Mike Smith University of Brighton
 */

public class Model extends Observable
{
  // Boarder
  private static final int B              = 20;  // Border offset
  private static final int M              = 40; // Menu offset
  
  // Size of things
  private static final float BALL_SIZE    = 30; // Ball side
  private static final float BRICK_WIDTH  = 50; // Brick size
  private static final float BRICK_HEIGHT = 30;

  private static final int BAT_MOVE       = 10; // Distance to move bat
   
  // Scores
  private static final int HIT_BRICK      = 50;  // Score
  private static final int HIT_BOTTOM     = -200;// Score

  private GameObj ball;          // The ball
  private List<GameObj> bricks;  // The bricks
  private GameObj bat;           // The bat
  
  private boolean fast = false;   // Sleep in run loop

  private int score = 0;  		 // Player score
  private Level level;			 // The level
  private int levelNum;          // Level number
  private int gameState;		 // Game state 0=intro, 1=running, 2=won game, 3=game over
  private boolean musicPlaying = false;

  private final float W;         // Width of area
  private final float H;         // Height of area
  
  private int playerLives;
  private int brickCount = 0;

  public Model( int width, int height )
  {
    this.W = width; this.H = height;
  }

  /**
   * Create in the model the objects that form the game
   */

  public void createGameObjects()
  {
    synchronized( Model.class )
    {
      ball   = new GameObj(W/2, H/2, BALL_SIZE, BALL_SIZE, Colour.WHITE );
      bat    = new GameObj(W/2, H - BRICK_HEIGHT*2, BRICK_WIDTH*3, BRICK_HEIGHT/4, Colour.WHITE);

      bricks = new ArrayList<GameObj>();
      // *[1]******************************************************[1]*
      // * Fill in code to place the bricks on the board              *
      // **************************************************************
      
      brickCount = level.getBrickCount();
      bricks = level.getBricks();

      playerLives = 3;
      
      gameState = 0;
    }
      
  }
  
  private ActivePart active  = null;

  /**
   * Start the continuous updates to the game
   */
  public void startGame()
  {
    synchronized ( Model.class )
    {
      stopGame();
      active = new ActivePart();
      Thread t = new Thread( active::runAsSeparateThread );
      t.setDaemon(true);   // So may die when program exits
      t.start();
    }
  }

  /**
   * Stop the continuous updates to the game
   * Will freeze the game, and let the thread die.
   */
  public void stopGame()
  {  
    synchronized ( Model.class )
    {
      if ( active != null ) { active.stop(); active = null; }
    }
  }

  public GameObj getBat()             { return bat; }

  public GameObj getBall()            { return ball; }

  public List<GameObj> getBricks()    { return bricks; }
  
  public int getPlayerLives() 		  { return playerLives; }
  
  public int getGameState() {return gameState;}

  public int getLevelNum() { return levelNum; }
  
  public void setGameState(int gameState) { this.gameState = gameState; }


  public void resetScores(){
    score = 0;
  }

  public void setLevel(int levelNum) {

    this.levelNum = levelNum;

   switch (levelNum) {
     case 1:
       level = new Level(1,3);
       break;

     case 2:
       level = new Level(2,5);
       break;

     case 3:
       level = new Level(3,7);
       break;

     case 4:
       level = new Level(4,9);
       break;
   }

  }


  /**
   * Add to score n units
   * @param n units to add to score
   */
  protected void addToScore(int n)    { score += n; }
  
  public int getScore()               { return score; }

  /**
   * Set speed of ball to be fast (true/ false)
   * @param fast Set to true if require fast moving ball
   */
  public void setFast(boolean fast)   
  { 
    this.fast = fast; 
  }

  /**
   * Move the bat. (-1) is left or (+1) is right
   * @param direction - The direction to move
   */
  public void moveBat( int direction )
  {
    // *[2]******************************************************[2]*
    // * Fill in code to prevent the bat being moved off the screen *
    // **************************************************************
	  
	float dist = direction * BAT_MOVE;    // Actual distance to move
	
	if (bat.getX() + dist > B && bat.getX() + dist <  W - B - BRICK_WIDTH*3)
	{
		Debug.trace( "Model: Move bat = %6.2f", dist );
	    bat.moveX(dist);
	}

  }

  public void exitGame(){
    System.exit(0);
  }

  /**
   * This method is run in a separate thread
   * Consequence: Potential concurrent access to shared variables in the class
   */
  class ActivePart
  {
    private boolean runGame = true;

    public void stop()
    {
      runGame = false;
    }

    public void runAsSeparateThread()
    {
      final int S = 5; // Units to move (Speed)
      
      if (gameState == 1) {
    	  try
          {
            synchronized ( Model.class ) // Make thread safe
            {
              GameObj       ball   = getBall();     // Ball in game
              GameObj       bat    = getBat();      // Bat
              List<GameObj> bricks = getBricks();   // Bricks
              int playerLives	   = getPlayerLives(); // Player Lives
              gameState			   = getGameState();
            }
      
            while (runGame)
            {
              synchronized ( Model.class ) // Make thread safe
              {
            	  
            	// Check player lives
            	if (playerLives == 0) {
            		runGame = false;
            		setGameState(3);
            	}
            	
            	// Check brick Count
            	if (brickCount == 0) {
            		runGame = false;
            		setGameState(2);
            	}

                // Start background music
                if (!musicPlaying){

                  musicPlaying = true;

                  // Background Music play
                  try {
                    File wow = new File("assets/bgMusic.wav");
                    AudioInputStream stream = AudioSystem.getAudioInputStream(wow);
                    AudioFormat format = stream.getFormat();
                    DataLine.Info info = new DataLine.Info(Clip.class, format);
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.open(stream);
                    clip.loop(100);

                  } catch (Exception e){}
                }
            	
                float ballX = ball.getX();  // Current x,y position
                float ballY = ball.getY();
                // Deal with possible edge of board hit
                if (ballX >= W - B - BALL_SIZE)  ball.changeDirectionX();
                if (ballX <= 0 + B            )  ball.changeDirectionX();
                if (ballY >= H - B - BALL_SIZE)  // Bottom
                { 
                  ball.changeDirectionY(); 
                  addToScore( HIT_BOTTOM );
                  playerLives--;
                  
                  ball.setYPos(H/2);
                  ball.changeDirectionX();
                  
                }
                if (ballY <= 0 + M            )  ball.changeDirectionY();

                // As only a hit on the bat/ball is detected it is 
                //  assumed to be on the top or bottom of the object.
                // A hit on the left or right of the object
                //  has an interesting affect
        
                boolean hit = false;
                // *[3]******************************************************[3]*
                // * Fill in code to check if a visible brick has been hit      *
                // *      The ball has no effect on an invisible brick          *
                // **************************************************************
                if (hit)
                  ball.changeDirectionY();
        
                if ( ball.hitBy(bat) )
                  ball.changeDirectionY();
                
                for (GameObj brick : bricks)
                {
                	float brickX = brick.getX();
                	float brickY = brick.getY();
                	
                	float brickWidth = brick.getWidth();
                	float brickHeight = brick.getHeight();
                	
                	float ballWidth = ball.getWidth();
                	
                	if (ball.hitBy(brick) && brick.isVisible() && brick.getHitCount() == 1)
                	{
                		ball.changeDirectionY();
                		brick.setVisibility(false);
                		brickCount--;
                		
                		score += HIT_BRICK;


                        // When brick is hit plays wav file sound
                        try {
                          File wow = new File("assets/hit.wav");
                          AudioInputStream stream = AudioSystem.getAudioInputStream(wow);
                          AudioFormat format = stream.getFormat();
                          DataLine.Info info = new DataLine.Info(Clip.class, format);
                          Clip clip = (Clip) AudioSystem.getLine(info);
                          clip.open(stream);
                          clip.start();
                        } catch (Exception e){}
                	} 
                	else if (ball.hitBy(brick) && brick.isVisible() && brick.getHitCount() == 0)
                	{
                		ball.changeDirectionY();
                		brick.incrementHitCount();
                		
                		score += HIT_BRICK;


                      // When brick is hit plays wav file sound
                      try {
                        File wow = new File("assets/hit.wav");
                        AudioInputStream stream = AudioSystem.getAudioInputStream(wow);
                        AudioFormat format = stream.getFormat();
                        DataLine.Info info = new DataLine.Info(Clip.class, format);
                        Clip clip = (Clip) AudioSystem.getLine(info);
                        clip.open(stream);
                        clip.start();
                      } catch (Exception e){}
                	}
                	
            	}
                
              }
              modelChanged();      // Model changed refresh screen
              Thread.sleep( fast? 1 : 3 );
              ball.moveX(S);  ball.moveY(-S);
            }
          } catch (Exception e) 
          { 
            Debug.error("Model.runAsSeparateThread - Error\n%s", 
                        e.getMessage() );
          }
      } else {
    	  modelChanged();      // Model changed refresh screen
      }
    }
  }
  
  /**
   * Model has changed so notify observers so that they
   *  can redraw the current state of the game
   */
  public void modelChanged()
  {
    setChanged(); notifyObservers();
  }

}
