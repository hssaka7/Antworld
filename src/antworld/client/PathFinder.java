package antworld.client;

import antworld.common.AntData;
import antworld.common.Direction;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Created by kleyba on 4/27/17.
 * The PathFinder class is used to generate paths for AntWorld
 */
public class PathFinder
{
  //List of ants to check nodes
  private ArrayList<AntData> antList = new ArrayList<>();
  //Map Fields
  private BufferedImage map;
  private int mapWidth;
  private int mapHeight;



  private ArrayList<PathNode> emptyList = new ArrayList<>();

  //PathNodes to exclude
  private ArrayList<PathNode> exludeList = new ArrayList<>();

  /**
   * @param map The BufferedImage representing the AntWorld Map
   * @KirtusL PathFinder constructor
   */
  public PathFinder(BufferedImage map)
  {
    this.map = map;
    mapWidth = map.getWidth();
    mapHeight = map.getHeight();
  }


  /**
   * @param start The start node
   * @param goal  The goal node
   * @return The path from start to goal, composed of a combination of prebuilt A* paths and live calculated paths
   * @KirtusL This method returns the final calculated path
   */
  public ArrayList<PathNode> getPath(PathNode start, PathNode goal)
  {
    return generatePath(start, goal);
  }

  /**
   * @param first The node to start with
   * @param last  The node to finish at
   * @return An ArrayList of all the paths
   * @KirtusL A* pathfinding implementation
   */
  private ArrayList<PathNode> generatePath(PathNode first, PathNode last)
  {
    /**
     * Essential A* Fields
     */
    boolean[][] frontierArray = new boolean[map.getWidth()][map.getHeight()];
    boolean[][] visitedArray = new boolean[map.getWidth()][map.getHeight()];
    ArrayList<PathNode> frontier = new ArrayList<PathNode>();
    ArrayList<PathNode> adjacencyList = new ArrayList<PathNode>();
    frontier.add(first);
    first.setH(distSquared(first, last));
    first.setG(0.0);
    first.setVisited(false);

    PathNode current;

    while (frontier.size() > 0)
    {
      current = getLowestF(frontier);
      if (current.equals(last))
      {
        return constructPath(current, first);
      }
      frontier.remove(current);
      removeFrom(current, frontierArray);
      addTo(current, visitedArray);
      adjacencyList = calcAdjacencies(current);
      for (PathNode adjNode : adjacencyList)
      {
        double tempG = current.getG() + distSquared(current, adjNode);
        if (!checkArray(adjNode, visitedArray))
        {
          if (!checkArray(adjNode, frontierArray))
          {
            adjNode.setG(tempG);
            adjNode.setH(distSquared(adjNode, last));
            adjNode.setParent(current);
            frontier.add(adjNode);
            addTo(adjNode, frontierArray);
          } else if (tempG < adjNode.getG())
          {
            adjNode.setG(tempG);
            adjNode.setH(distSquared(adjNode, last));
            adjNode.setParent(current);
          }
        }
      }
    }
    return emptyList;
  }

  /**
   * Another A* Helper
   *
   * @param node
   * @param array
   * @return
   */
  private boolean checkArray(PathNode node, boolean[][] array)
  {
    return array[node.getX()][node.getY()];
  }

  /**
   * Helper method for A*, returns list of adjacent nodes to passed in node
   *
   * @param node
   * @return
   */
  private ArrayList<PathNode> calcAdjacencies(PathNode node)
  {
    ArrayList<PathNode> adjTiles = new ArrayList<PathNode>();
    int x = node.getX();
    int y = node.getY();
    for (Direction dir : Direction.values())
    {
      int tempX = x + dir.deltaX();
      int tempY = y + dir.deltaY();
      PathNode tempNode = new PathNode(tempX, tempY);
      if (nodeLegal(tempNode))
      {
        adjTiles.add(tempNode);
      }
    }
    return adjTiles;
  }

  /**
   * @param node
   * @return
   * @KirtusL This method allows for checking legal nodes
   */
  public boolean nodeLegal(PathNode node)
  {
    if (node.getX() <= 0 || node.getX() >= map.getWidth())
    {
      return false;
    }
    if (node.getY() <= 0 || node.getY() >= map.getHeight())
    {
      return false;
    }
    int rgb = (map.getRGB(node.getX(), node.getY()) & 0x00FFFFFF);
    if (rgb == 0x329fff)
    {
      return false;
    }
    if (antHere(node))
    {
      return false;
    }
    if (exludeList.contains(node))
    {
      return false;
    }
    return true;
  }

  public boolean antHere(PathNode node)
  {
    for (AntData ant : antList)
    {
      if (ant.gridX == node.getX() && ant.gridY == node.getY())
      {
        return true;
      }
    }
    return false;
  }


  /**
   * A* helper method
   *
   * @param node
   * @param array
   */
  private void addTo(PathNode node, boolean[][] array)
  {
    array[node.getX()][node.getY()] = true;
  }

  /**
   * A* helper function
   *
   * @param node  Node to remove
   * @param array array to remove node from
   */
  private void removeFrom(PathNode node, boolean[][] array)
  {
    array[node.getX()][node.getY()] = false;
  }

  /**
   * A helper function so that the A* algorithm has correct priority sorting
   *
   * @param frontier The frontier of A*
   * @return The node with the lowest f value in frontier
   */
  private PathNode getLowestF(ArrayList<PathNode> frontier)
  {
    PathNode lowestNode = frontier.get(0);

    for (PathNode node : frontier)
    {
      if (node.getF() <= lowestNode.getF())
      {
        lowestNode = node;
      }
    }
    return lowestNode;
  }

  /**
   * @param last  The last node in the path
   * @param first The node that starts the path
   * @return An ArrayList representing the path
   * @KirtusL After generating a path with generate path, call this to build the path in reverse order
   */
  private ArrayList<PathNode> constructPath(PathNode last, PathNode first)
  {
    ArrayList<PathNode> path = new ArrayList<PathNode>();
    boolean done = false;
    while (!done)
    {
      if (last == null)
      {
        return path;
      }

      path.add(0, last);
      if (last.equals(first))
      {
        return path;
      }
      last = last.getParent();
      if (last.equals(first))
      {
        path.add(0, last);
        return path;
      }
    }
    return path;
  }


  /**
   * @param start The first initial Node
   * @param goal  The final initial Node
   * @return
   * @KirtusL This method returns the path between two initial nodes
   */

  public ArrayList<PathNode> getPathSelective(PathNode start, PathNode goal, ArrayList<PathNode> exludeList)
  {
    this.exludeList.addAll(exludeList);
    this.exludeList.remove(start);
    this.exludeList.remove(goal);
    ArrayList<PathNode> path = generatePath(start, goal);
    this.exludeList = emptyList;
    return path;
  }


  /**
   * @param nodeOne The first node
   * @param nodeTwo The second node
   * @return The distance squared between the nodes
   * @KirtusL A method for heuristic and distance purposes
   */
  private double distSquared(PathNode nodeOne, PathNode nodeTwo)
  {
    double dx = nodeOne.getX() - nodeTwo.getX();
    double dy = nodeOne.getY() - nodeTwo.getY();
    return ((dx * dx + dy * dy));
  }


  public void setMap(BufferedImage map)
  {
    this.map = map;
  }


  Direction getDirectionToWater(int x, int y)
  {

    Direction dir = Direction.EAST;
    for (int i = 0; i < 8; i++)
    {
      int newX = x + dir.deltaX();
      int newY = y + dir.deltaY();
      int rgb = map.getRGB(newX, newY) & 0x00FFFFFF;
      if (rgb == 0x329fff)
      {
        return dir;
      }
      dir = Direction.getLeftDir(dir);
    }
    return null;
  }

}
