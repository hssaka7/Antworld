package antworld.client;

import antworld.common.Direction;

/**
 * The Class PathNode represents a single node for A* pathfinding
 * @Author: Kirtus Leyba
 */
class PathNode
{
  private int x;
  private int y;
  private double h;
  private double g;
  private double f;
  private PathNode parent;
  private boolean visited;

  /**
   * Pathnode constructo
   * @param x the x coordinate of the pathnode
   * @param y the y coordinate of the pathnode
   */
  PathNode(int x, int y)
  {
    this.x = x;
    this.y = y;
    this.h = 0;
    this.g = 0;
    this.f = 0;
    visited = false;
  }

  /**
   * Setter and getter for visited field
   */
  public void setVisited(boolean visited)
  {
    this.visited = visited;
  }
  public boolean getVisited()
  {
    return visited;
  }

  @Override
  public boolean equals(Object o)
  {
    if(o == null)
    {
      return false;
    }
    if(o.getClass()!=getClass())
    {
      return false;
    }
    else
    {
      PathNode other = (PathNode)o;
      if(other.getX() == x && other.getY() == y)
      {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString()
  {
    return x +"," + y;
  }

  //Below are the standard setters and getters for this structure
  //@Kirtus L
  public double getH()
  {
    return h;
  }

  public void setH(double h)
  {
    this.h = h;
    f = g + h;
  }

  public double getG()
  {
    return g;
  }

  public void setG(double g)
  {
    this.g = g;
    f = g + h;
  }

  //The f cost is g + h
  //@Kirtus L
  public double calcF()
  {
    f = g + h;
    return f;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public double getF() {
    return f;
  }

  public void setParent(PathNode parent)
  {
    this.parent = parent;
  }
  public PathNode getParent()
  {
    return parent;
  }

  public Direction getDirectionTo(PathNode pathNode)
  {
    int deltaX = pathNode.getX() - x;
    int deltaY = pathNode.getY() - y;

    System.out.println("(dx, dy): " + deltaX + ", " + deltaY);

    if(deltaX == 1) //pathnode is to the East
    {
      switch(deltaY)
      {
        case -1: //pathnode is to the NE
          return Direction.NORTHEAST;
        case 0: //pathnode is directly E
          return Direction.EAST;
        case 1: //pathnode is SE
          return Direction.SOUTHEAST;
        default:
          return Direction.getRandomDir();
      }
    }
    else if(deltaX == 0) //Pathnode is north or south
    {
      switch(deltaY)
      {
        case -1: //pathnode is to the N
          return Direction.NORTH;
        case 1: //pathnode is S
          return Direction.SOUTH;
        case 0:
          return null;
        default:
          return Direction.getRandomDir();
      }
    }
    else if(deltaX == -1) //Pathnode is West
    {
      switch(deltaY)
      {
        case -1: //pathnode is to the NW
          return Direction.NORTHWEST;
        case 0: //pathnode is directly W
          return Direction.WEST;
        case 1: //pathnode is SW
          return Direction.SOUTHWEST;
        default:
          return Direction.getRandomDir();
      }
    }
    return Direction.getRandomDir();
  }

  public boolean isAdjacent(PathNode otherNode)
  {
    for (Direction dir : Direction.values())
    {
      int tempX = x + dir.deltaX();
      int tempY = y + dir.deltaY();
      if(otherNode.equals(new PathNode(tempX, tempY)))
      {
        return true;
      }
    }
    int tempX = x;
    int tempY = y;
    if(otherNode.equals(new PathNode(tempX, tempY)))
    {
      return true;
    }
    return false;
  }
}
