package antworld.common;

/**
 * On the Ant World world map, there are 13 ant nests (things that look like Cheez-ITs).
 * Each nest has a unique name.
 * When a client opens a socket to the server for the first time, it is assigned
 * a random nest.
 */
public enum NestNameEnum
{
  Gulfloss,
  Ireland,
  Iceland,
  Fjords,
  England,
  Norway,
  Scotland,
  Finland,
  Copenhagen,
  Odense,
  Aalborg,
  Esbierg;

  
  public static final int SIZE = values().length;
}
