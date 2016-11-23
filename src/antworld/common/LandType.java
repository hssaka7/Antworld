package antworld.common;

/**
 * The Enum LandType.
 */
public enum LandType
{
  
  /** The nest. */
  NEST
  { public int getMapColor() {return 0xF0E68C;}
  },
  
  /**
   * The grass.
   * Note: grass land only uses the green color channel.
   * Thus, the client AI can use the red and blue channels to store
   * other common, such as something that takes the role of a pheromone trail.
   */
  GRASS
  {
    public int getMapColor() {return 0x283724;}
    public int getMapHeight(int rgb)
    {
      int g = (rgb & 0x0000FF00) >> 8;
      return Math.max(0, g - 48);
    }
  },
  
  
  /** The water. */
  WATER
  { public int getMapColor() {return 0x1E90FF;}
  };
  
  /**
   * Gets the map color.
   *
   * @return the map color
   */
  public abstract int getMapColor();
  
  /**
   * Gets the map height.
   *
   * @param rgb the rgb
   * @return the map height
   */
  public int getMapHeight(int rgb) {return 0;}
  
  /**
   * Gets the max map height.
   *
   * @return the max map height
   */
  public static int getMaxMapHeight() {return 200;}
 
  
}
