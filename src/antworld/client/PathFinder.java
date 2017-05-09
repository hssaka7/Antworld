package antworld.client;

import antworld.common.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import static antworld.common.Constants.random;

/**
 * Created by kleyba on 4/27/17.
 * The PathFinder class is used to generate paths for AntWorld
 */
public class PathFinder
{
  //List of ants to check nodes
  private ArrayList<AntData> antList = new ArrayList<>();
  private ArrayList<FoodData> foodList = new ArrayList<FoodData>();
  //Map Fields
  private BufferedImage map;
  private int mapWidth;
  private int mapHeight;

  ////Initial Nodes and Paths Fields
  //private ArrayList<PathNode> initialNodes = new ArrayList<>();
  //private ArrayList<PathNode>[] initialPaths;

  private ArrayList<PathNode> emptyList = new ArrayList<>();

  //PathNodes to exclude
  private ArrayList<PathNode> exludeList = new ArrayList<>();
  private ArrayList<AntData> enemyList;

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
   * @param initialNodes The list of nodes to initialize
   * @KirtusL This method initializes the initialNodes
   */
  //public void initializeNodes(ArrayList<PathNode> initialNodes) {
//
  //  System.out.println("Initializing: " + initialNodes.size() + "nodes!");
//
  //  this.initialNodes = initialNodes;
//
  //  initialPaths = new ArrayList[initialNodes.size() * (initialNodes.size() - 1)];
  //  int i = 0;
//
  //  for (PathNode node : initialNodes) {
  //    for (PathNode otherNode : initialNodes) {
  //      if (!node.equals(otherNode)) {
  //        System.out.println("Initializing " + i + "th path from: " + node + " to " + otherNode);
  //        initialPaths[i] = generatePath(node, otherNode);
  //        System.out.println("Path Initialized: " + initialPaths[i]);
  //        i++;
  //      }
  //    }
  //  }
  //}
//
  /**
   * @param start The start node
   * @param goal  The goal node
   * @return The path from start to goal, composed of a combination of prebuilt A* paths and live calculated paths
   * @KirtusL This method returns the final calculated path
   */
  public ArrayList<PathNode> getPath(PathNode start, PathNode goal) {
//
    // System.out.println("Getting Path from: " + start + " to " + goal);
//
    // PathNode closestInitialNodeToStart = findClosestInitialNode(start); //The initial node closest to the start of the path
    // PathNode closestInitialNodeToGoal = findClosestInitialNode(goal); //The initial node closest to the goal of the path
//
    // System.out.println("Found initial nodes: " + closestInitialNodeToStart + ", " + closestInitialNodeToGoal);
//
    // ArrayList<PathNode> pathStart = emptyList;
    // ArrayList<PathNode> pathFinal = emptyList;
    // ArrayList<PathNode> pathMiddle = emptyList;
    // if (!closestInitialNodeToGoal.equals(closestInitialNodeToStart)) //Check if we need to use nodes
    // {
//
    //   pathStart = generatePath(start, closestInitialNodeToStart);
    //   pathFinal = generatePath(closestInitialNodeToGoal, goal);
    //   pathMiddle = calculateTraversalPath(closestInitialNodeToStart, closestInitialNodeToGoal);
//
    //   if (pathStart.size() > 0) {
    //     System.out.println("Start found: " + pathStart.get(0) + " to: " + pathStart.get(pathStart.size() - 1));
    //   }
    //   if (pathFinal.size() > 0) {
    //     System.out.println("final found" + pathFinal.get(0) + " to: " + pathFinal.get(pathFinal.size() - 1));
    //   }
    //   if (pathMiddle.size() > 0) {
    //     System.out.println("Middle found" + pathMiddle.get(0) + " to: " + pathMiddle.get(pathMiddle.size() - 1));
    //   }
    // } else {
    //   pathMiddle = generatePath(start, goal);
    //   if (pathMiddle.size() > 0)
    //     System.out.println("Short Path found" + pathMiddle.get(0) + " to: " + pathMiddle.get(pathMiddle.size() - 1));
    // }
//
//
    // ArrayList<PathNode> path = new ArrayList<>();
//
//
    // path.addAll(pathStart);
    // path.addAll(pathMiddle);
    // path.addAll(pathFinal);

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

    if(!nodeLegal(last))
    {
      return emptyList;
    }

    int iterations = 0;
    int maxIterations = 30000;

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
      iterations++;
      if(iterations >= maxIterations)
      {
        return emptyList; //Too many iterations
      }
      System.out.println(iterations);
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
  private boolean checkArray(PathNode node, boolean[][] array) {
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
    if (node.getX() <= 0 || node.getX() >= map.getWidth()) {
      return false;
    }
    if (node.getY() <= 0 || node.getY() >= map.getHeight()) {
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
    if (foodHere(node))
    {
      return false;
    }
    return true;
  }

  private boolean foodHere(PathNode node)
  {
    for(FoodData fd: foodList)
    {
      if(fd.gridX == node.getX() && fd.gridY == node.getY())
      {
        return true;
      }
    }
    return false;
  }

  public boolean antHere(PathNode node)
  {
    for(AntData ant: antList)
    {
      if(ant.gridX == node.getX() && ant.gridY == node.getY())
      {
        return true;
      }
    }
    return false;
  }


  public PathNode foodAdjacent(PathNode node)
  {
    for(Direction testDir: Direction.values())
    {
      PathNode testNode = new PathNode(node.getX() + testDir.deltaX(), node.getY() + testDir.deltaY());
      if(foodHere(testNode))
      {
        return testNode;
      }
    }
    return null;
  }

  public PathNode enemyAdjacent(PathNode node)
  {
    for(Direction testDir: Direction.values())
    {
      PathNode testNode = new PathNode(node.getX() + testDir.deltaX(), node.getY() + testDir.deltaY());
      if(enemyHere(testNode))
      {
        return testNode;
      }
    }
    return null;
  }

  private boolean enemyHere(PathNode node)
  {
    for(AntData ant: enemyList)
    {
      if(ant.gridX == node.getX() && ant.gridY == node.getY())
      {
        return true;
      }
    }
    return false;
  }

  public void setAntList(ArrayList<AntData> antList)
  {
    this.antList = antList;
  }
  /**
   * A* helper method
   * @param node
   * @param array
   */
  private void addTo(PathNode node, boolean[][] array)
  {
    array[node.getX()][node.getY()] = true;
  }

  /**
   * A* helper function
   * @param node
   * Node to remove
   * @param array
   * array to remove node from
   */
  private void removeFrom(PathNode node, boolean[][] array)
  {
    array[node.getX()][node.getY()] = false;
  }

  /**
   * A helper function so that the A* algorithm has correct priority sorting
   * @param frontier
   * The frontier of A*
   * @return
   * The node with the lowest f value in frontier
   */
  private PathNode getLowestF(ArrayList<PathNode> frontier)
  {
    PathNode lowestNode = frontier.get(0);

    for(PathNode node: frontier)
    {
      if(node.getF() <= lowestNode.getF())
      {
        lowestNode = node;
      }
    }
    return lowestNode;
  }

  /**
   * @KirtusL
   * After generating a path with generate path, call this to build the path in reverse order
   * @param last
   * The last node in the path
   * @param first
   * The node that starts the path
   * @return
   * An ArrayList representing the path
   */
  private ArrayList<PathNode> constructPath(PathNode last, PathNode first)
  {
    ArrayList<PathNode> path = new ArrayList<PathNode>();
    boolean done = false;
    while(!done)
    {
      if(last == null)
      {
        return path;
      }

      path.add(0, last);
      if(last.equals(first))
      {
        return path;
      }
      last = last.getParent();
      if(last.equals(first))
      {
        path.add(0, last);
        return path;
      }
    }
    return path;
  }
  //private PathNode findClosestInitialNode(PathNode node)
  //{
  //  PathNode nearest = initialNodes.get(0);
  //  for(PathNode pNode: initialNodes)
  //  {
  //    if(distSquared(node, nearest) > distSquared(node, pNode))
  //    {
  //      nearest = pNode;
  //    }
  //  }
  //  return nearest;
  //}

  //private ArrayList<PathNode> calculateTraversalPath(PathNode nodeOne, PathNode nodeTwo)
  //{
  //  for(ArrayList<PathNode> path: initialPaths)
  //  {
  //    if (path.get(0).equals(nodeOne) && path.get(path.size() - 1).equals(nodeTwo))
  //    {
  //      return path;
  //    }
  //  }
  //  return emptyList;
  //}


  /**
   * @KirtusL
   * @param start
   * @param goal
   * @param exludeList
   * @return
   * The path from start to goal ignoring nodes on excludeList
   */
  public ArrayList<PathNode> getPathSelective(PathNode start, PathNode goal, ArrayList<PathNode> exludeList)
  {
    this.exludeList = exludeList;
    ArrayList<PathNode> path = generatePath(start, goal);
    this.exludeList = emptyList;
    return path;
  }


  /**
   * @KirtusL
   * A method for heuristic and distance purposes
   * @param nodeOne
   * The first node
   * @param nodeTwo
   * The second node
   * @return
   * The distance squared between the nodes
   */
  public double distSquared(PathNode nodeOne, PathNode nodeTwo)
  {
    double dx = nodeOne.getX() - nodeTwo.getX();
    double dy = nodeOne.getY() - nodeTwo.getY();
    return ((dx*dx + dy*dy));
  }

  public void printPath (ArrayList<PathNode> path){
    if (path == null || path.size() < 1) {
      System.out.println("NULL PATH or ZERO SIZE");
      return;
    }
    PathNode Start = path.get(0);
    PathNode goal = path.get(path.size()-1);

    System.out.println("Start : " + Start + " Goal : " + goal + " has PathSize " + path.size());
    for (PathNode pathnode : path){
      System.out.println(pathnode);
    }
  }

  public void setMap(BufferedImage map)
  {
    this.map = map;
  }

  /**
   * @KirtusL
   * Getter for initial paths, used for writing paths to file
   * @return
   * Array of initial paths
   */
//public ArrayList<PathNode>[] getInitialPaths()
//{
//  return initialPaths;
//}

//public void setInitialPaths(ArrayList<PathNode>[] initialPaths)
//{
//  this.initialPaths = initialPaths;

//  for(ArrayList<PathNode> path: initialPaths)
//  {
//    PathNode start = path.get(0);
//    if(!initialNodes.contains(start))
//    {
//      initialNodes.add(start);
//    }
//  }

//}

  Direction getDirectionToWater (int x, int y){

    Direction dir =  Direction.EAST;
    for (int i =0; i <8; i++){
      int newX =  x + dir.deltaX();
      int newY = y + dir.deltaY();
      int rgb = map.getRGB(newX, newY) & 0x00FFFFFF;
      System.out.println("Direction " + dir + " has rgb value " + rgb);
      if(rgb == 0x329fff)
      {
        return dir;
      }
      dir = Direction.getLeftDir(dir);
    }
    return null;
  }

  public void setFoodList(ArrayList<FoodData> foodList)
  {
    this.foodList = foodList;
  }
  public void setEnemyList(ArrayList<AntData> enemyList)
  {
    this.enemyList = enemyList;
  }

  public PathNode getRandomExplorerNode(PathNode start)
  {
    PathNode attempt;
    int x = Direction.getRandomDir().deltaX()*(start.getX() + 10 + random.nextInt(20));
    int y = Direction.getRandomDir().deltaY()*(start.getY() + 10 + random.nextInt(20));
    attempt = new PathNode(x, y);
    while(!nodeLegal(attempt))
    {
      x = Direction.getRandomDir().deltaX()*(start.getX() + 10 + random.nextInt(20));
      y = Direction.getRandomDir().deltaY()*(start.getY() + 10 + random.nextInt(20));
      attempt = new PathNode(x,y);
    }
    return attempt;
  }
}
