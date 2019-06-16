import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Displays a graphical view of the game of breakout.
 *  Uses Graphics2D would need to be re-implemented for Android.
 * @author Mike Smith University of Brighton
 */
public class View extends JFrame implements Observer
{ 
  private Controller controller;
  private GameObj   bat;            // The bat
  private GameObj   ball;           // The ball
  private List<GameObj> bricks;     // The bricks
  private int       score;     // The score
  private int       frames = 0;     // Frames output
  private int		playerLives;
  private int		gameState;
  private int levelNum;
  public boolean displayLevelStart;
  private Image bgImage;


  public final int width;  // Size of screen Width
  public final int height;  // Sizeof screen Height

  /**
   * Construct the view of the game
   * @param width Width of the view pixels
   * @param height Height of the view pixels
   */
  public View(int width, int height)
  {
    this.width = width; this.height = height;

    setSize(width, height);                 // Size of window
    addKeyListener( new Transaction() );    // Called when key press
      setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    setResizable(false);
    Timer.startTimer();
  }

  public void drawStartScreen(Graphics2D g) {
	  try {
    	  bgImage = ImageIO.read(new File("assets/start.png"));
    	  g.drawImage(bgImage, 0, 0, width, height, null);
      } catch (IOException e) {
    	  System.out.println("Cannot draw background.");
      }
  }

  public void drawLevelText(Graphics2D g) {
      try {
          String pathname = "assets/level"+ levelNum +"Text.png";

          Image levelText = ImageIO.read(new File(pathname));

          g.drawImage(levelText, 0, Main.H/4, Main.W, Main.H, null);

      } catch (IOException e) {
          System.out.println("Cannot draw Level text.");
      }

      repaint();
  }
  
  public void drawGameOverScreen(Graphics2D g) {
	  try {
    	  bgImage = ImageIO.read(new File("assets/gameOver.png"));
    	  g.drawImage(bgImage, 0, 0, width, height, null);
      } catch (IOException e) {
    	  System.out.println("Cannot draw background.");
      }

	  Font font = new Font("Monospaced", Font.BOLD, 50); 
      g.setFont( font );
      
      String scoreNow = Integer.toString(score);
	  
	  g.setPaint(Color.red);
      g.drawString( scoreNow, width/2 + width/6 , height/2 + 50);
  }
  
  public void drawWinScreen(Graphics2D g) {

      if (levelNum < 5){
          try {

              String pathname = "assets/level"+ levelNum +"Complete.png";

              bgImage = ImageIO.read(new File(pathname));

              g.drawImage(bgImage, 0, 0, width, height, null);

          } catch (IOException e) {
              System.out.println("Cannot draw level " + levelNum + " Complete screen");
          }

          Font font = new Font("Monospaced", Font.BOLD, 50);
          g.setFont( font );

          String scoreNow = Integer.toString(score);

          g.setPaint(Color.red);
          g.drawString( scoreNow, width/2 + width/6 , height/2 + 50);
      } else {
          try {
              bgImage = ImageIO.read(new File("assets/gameComplete.png"));
              g.drawImage(bgImage, 0, 0, width, height, null);
          } catch (IOException e) {
              System.out.println("Cannot draw background.");
          }

          Font font = new Font("Monospaced", Font.BOLD, 50);
          g.setFont( font );

          String scoreNow = Integer.toString(score);

          g.setPaint(Color.red);
          g.drawString( scoreNow, width/2 + width/6 , height/2 + 50);
      }

  }
  
  /**
   *  Code called to draw the current state of the game
   *   Uses draw:       Draw a shape
   *        fill:       Fill the shape
   *        setPaint:   Colour used
   *        drawString: Write string on display
   *  @param g Graphics context to use
   */
  public void drawActualPicture( Graphics2D g )
  {
    final int  RESET_AFTER = 200; // Movements
    frames++;
    synchronized( Model.class )   // Make thread safe
    {
    	
      switch (gameState) {
      case 0:
    	  drawStartScreen(g);
    	  break;
      case 1:
    	// Draw the background 
          try {
        	  bgImage = ImageIO.read(new File("assets/BG.png"));
        	  g.drawImage(bgImage, 0, 0, width, height, null);
          } catch (IOException e) {
        	  System.out.println("Cannot draw background.");
          }
          
          Font font = new Font("Monospaced", Font.BOLD, 24); 
          g.setFont( font );
          
          displayBall( g, ball );   // Display the Ball
          displayBat( g, bat  );   // Display the Bat

          // *[4]****************************************************[4]*
          // * Display the bricks that make up the game                 *
          // * Fill in code to display bricks                           *
          // * Remember only a visible brick is to be displayed         *
          // ************************************************************
          
          for (GameObj brick : bricks)
          {  
        	  if (brick.isVisible())
        	  {
        		  displayBrick(g, brick);
        	  }
          }
          
          // Display lives
          for(int x = 0; x < playerLives; x++) {

        	  try {
        	      Image lifeImage = ImageIO.read( new File("assets/life.png"));
                  g.drawImage(lifeImage, Main.W - x*30 - 40, Main.H - 100, 30, 30, null);
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
          
          // Display state of game
          g.setPaint(Color.red);
          FontMetrics fm = getFontMetrics( font );
          String fmt = "BreakOut: Score = [%6d] fps=%5.1f";
          String text = String.format(fmt, score, frames/(Timer.timeTaken()/1000.0) );
          
          if ( frames > RESET_AFTER ) 
            { frames = 0; Timer.startTimer(); }
          g.drawString( text, width /2-fm.stringWidth(text)/2, 80 );

          break;
          
      case 2:
    	  drawWinScreen(g);
    	  break;
      case 3:
    	  drawGameOverScreen(g);
    	  break;
	    }
	  }
      
  }
  
  private void displayBat( Graphics2D g, GameObj go )
  {
	  Image batImage;
	  
	  try {
		  batImage = ImageIO.read(new File("assets/bat.png"));
		  g.drawImage(batImage, (int) go.getX(), (int) go.getY(), (int) go.getWidth(), (int) go.getHeight(), null);
	  } catch (IOException e) {
		  System.out.println("Cannot draw bat");
	  }
  }
  
  private void displayBrick(Graphics2D g, GameObj go)
  {
	  Image brickImg;
	  
	  try {
		  brickImg = ImageIO.read(new File("assets/brick"+go.getHitCount()+".png"));  
		  g.drawImage(brickImg, (int) go.getX(), (int) go.getY(), (int) go.getWidth(), (int) go.getHeight(), null);
	  } catch  (IOException e) {
			System.out.println("Cannot draw brick");
	  }
  }
  
  private void displayBall(Graphics2D g, GameObj go)
  {
	  Image ballImage;
	  
	  try {
		  ballImage = ImageIO.read(new File("assets/ball.png"));
		  g.drawImage(ballImage, (int) go.getX(), (int) go.getY(), (int) go.getWidth(), (int) go.getHeight(), null);
	  } catch (IOException e) {
		  System.out.println("Cannot draw ball");
	  }
  }
  
  /**
   * Called indirectly from the model when its state has changed
   * @param aModel Model to be displayed
   * @param arg    Any arguments (Not used)
   */
  @Override
  public void update( Observable aModel, Object arg )
  {
    Model model = (Model) aModel;
    // Get from the model the ball, bat, bricks & score
    ball    = model.getBall();              // Ball
    bricks  = model.getBricks();            // Bricks
    bat     = model.getBat();               // Bat
    score   = model.getScore();             // Score
    playerLives = model.getPlayerLives();   // Player Lives
    gameState = model.getGameState();       // Game State
    levelNum = model.getLevelNum();
    
    //Debug.trace("Update");
    repaint();                              // Re draw game
  }

  /**
   * Called by repaint to redraw the Model
   * @param g    Graphics context
   */
  @Override
  public void update( Graphics g )          // Called by repaint
  {
    drawPicture( (Graphics2D) g );          // Draw Picture
  }

  /**
   * Called when window is first shown or damaged
   * @param g    Graphics context
   */
  @Override
  public void paint( Graphics g )           // When 'Window' is first
  {                                         //  shown or damaged
    drawPicture( (Graphics2D) g );          // Draw Picture
  }

  private BufferedImage theAI;              // Alternate Image
  private Graphics2D    theAG;              // Alternate Graphics

  /**
   * Double buffer graphics output to avoid flicker
   * @param g The graphics context
   */
  private void drawPicture( Graphics2D g )   // Double buffer
  {                                          //  to avoid flicker
    if ( bricks == null ) return;            // Race condition
    if (  theAG == null )
    {
      Dimension d = getSize();              // Size of curr. image
      theAI = (BufferedImage) createImage( d.width, d.height );
      theAG = theAI.createGraphics();
    }
    drawActualPicture( theAG );             // Draw Actual Picture
    g.drawImage( theAI, 0, 0, this );       //  Display on screen
  }

  /**
   * Need to be told where the controller is
   * @param aPongController The controller used
   */
  public void setController(Controller aPongController)
  {
    controller = aPongController;
  }

  /**
   * Methods Called on a key press 
   *  calls the controller to process
   */
  private class Transaction implements KeyListener  // When character typed
  {
    @Override
    public void keyPressed(KeyEvent e)      // Obey this method
    {
      // Make -ve so not confused with normal characters
      controller.userKeyInteraction( -e.getKeyCode() );
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
      // Called on key release including specials
    }

    @Override
    public void keyTyped(KeyEvent e)
    {
      // Send internal code for key
      controller.userKeyInteraction( e.getKeyChar() );
    }
  }
}
