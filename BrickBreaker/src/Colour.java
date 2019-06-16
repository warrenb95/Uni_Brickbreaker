import java.awt.Color;

/**
 * Hide the specific internal representation of colours
 *  from most of the program.
 * Map to Swing color when required.
 */
public enum Colour  
{ 
  RED(Color.RED), BLUE(Color.BLUE), WHITE(Color.WHITE), PINK(Color.PINK), 
  DARK_GRAY(Color.DARK_GRAY), CYAN(Color.CYAN);

  private final Color c;

  Colour( Color c ) { this.c = c; }

  public Color forSwing() { return c; }
}


