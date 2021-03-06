package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class ExplorerAnts extends Ants
{
  static HashMap<Integer, HashMap<Integer, Integer>> visited = new HashMap<>();
  private boolean debug = false;
  private int spawnX;
  private int spawnY;
  private ArrayList<PathNode> path = new ArrayList<>();
  private int pathStep;
  private AntData ant;
  private PathNode goal;
  private AntBehaviors antBehavior;
  private ArrayList<PathNode> undiiscoveredNodes = new ArrayList<>();
  private Direction dir;
  private boolean moved = false;
  private PathFinder pathFinder;
  private int checkForWater = 0;
  private boolean startedToheal;
  private int healUnits;
  private AntBehaviors previousBehaviour;
  private int stuck;


  public ExplorerAnts(PathFinder pathFinder, int spawnX, int spawnY, TeamNameEnum myTeam)
  {
    this.ant = new AntData(AntType.EXPLORER, myTeam);
    this.spawnX = spawnX;
    this.spawnY = spawnY;
    this.pathFinder = pathFinder;
    antBehavior = AntBehaviors.TOSPAWN;
    dir = Direction.getRandomDir();

  }


  /**
   * @HImanshu
   * @return Gets possible directions to move
   *
   */
  public ArrayList<Direction> getPossibleDirections()
  {
    Direction[] temp = new Direction[7];
    ArrayList<Direction> toReturn = new ArrayList<>();
    temp[0] = Direction.getRightDir(dir);
    for (int i = 1; i < 7; i++)
    {
      temp[i] = Direction.getRightDir(temp[i - 1]);
    }
    int[] order = {1, 5, 2, 4, 6, 0, 3};

    for (int i : order)
    {
      if (isPossible(temp[i])) toReturn.add(temp[i]);
    }

    if (debug)
    {
      System.out.println();
      System.out.println("Possible Directions to move" + ant.gridX + "," + ant.gridY + "Direction " + dir.toString());
      for (Direction dir : toReturn)
      {
        System.out.println(dir.toString());
      }
      System.out.println();
    }
    return toReturn;
  }

  /**
   * @param nextDir
   * @return list of unseen pathnodes if moved in a location
   * @Himanshu
   */
  public ArrayList<PathNode> unSeenList(Direction nextDir)
  {
    ArrayList<PathNode> toReturn = new ArrayList<>();
    int radius = ant.antType.getVisionRadius();
    int deltaX = nextDir.deltaX();
    int deltaY = nextDir.deltaY();
    int currentX = ant.gridX + deltaX;
    int currentY = ant.gridY + deltaY;
    int xmin = Math.max(1, currentX - radius);
    int ymin = Math.max(1, currentY - radius);
    int xmax = Math.min(2500 - 2, currentX + radius);
    int ymax = Math.min(1500 - 2, currentY + radius);
    int newPos = 0;


    if (deltaX != 0)
    {
      if (deltaX > 0) newPos = xmax;
      else newPos = xmin;
      for (int i = ymin; i <= ymax; i++)
      {
        if (isSeen(newPos, i)) toReturn.add(new PathNode(newPos, i));
      }
    }

    if (deltaY != 0)
    {
      if (debug) System.out.println("DeltaY is not 0");
      if (deltaY > 0) newPos = ymax;
      else newPos = ymin;
      for (int i = xmin; i <= xmax; i++)
      {
        if (isSeen(i, newPos)) toReturn.add(new PathNode(i, newPos));
      }

    }
    if (debug)
    {
      System.out.println("Unseen List for Current" + ant.gridX + "," + ant.gridY + "Direction " + nextDir.toString() + "new CoOrdinates" + currentX + " , " + currentY);
      for (PathNode path : toReturn)
      {
        System.out.println(path.getX() + "," + path.getY());
      }
    }
    return toReturn;
  }

  /**
   * @param node
   * @return returns unseen list for a pathnode by comparing the visited nodes
   * @Himanshu
   */
  public ArrayList<PathNode> unSeenList(PathNode node)
  {
    ArrayList<PathNode> toReturn = new ArrayList<>();

    int radius = ant.antType.getVisionRadius();
    int currentX = node.getX();
    int currentY = node.getY();
    int xmin = Math.max(1, currentX - radius);
    int ymin = Math.max(1, currentY - radius);
    int xmax = Math.min(2500 - 2, currentX + radius);
    int ymax = Math.min(1500 - 2, currentY + radius);
    for (int i = ymin; i <= ymax; i += (ymax - ymin))
    {
      for (int j = xmin; j <= xmax; j++)
      {
        if (isSeen(j, i)) toReturn.add(new PathNode(j, i));
      }
    }

    for (int i = xmin; i <= xmax; i += (xmax - xmin))
    {
      for (int j = ymin; j <= ymax; j++)
      {
        if (isSeen(i, j)) toReturn.add(new PathNode(i, j));
      }
    }

    if (debug)
    {
      System.out.println("Unseen List for Current" + ant.gridX + "," + ant.gridY + "at Pathnode " + node.getX() + " , " + node.getY());
      for (PathNode path : toReturn)
      {
        System.out.println(path.getX() + "," + path.getY());
      }
    }

    return toReturn;
  }

  /**
   *
   * @param x
   * @param y
   * @return true or false depending upon is the node at x,y is already seen or not
   */
  boolean isSeen(int x, int y)
  {
    PathNode temp = new PathNode(x, y);
    if (!pathFinder.nodeLegal(temp)) return false;
    if (visited.containsKey(x))
    {
      if (!visited.get(x).containsKey(y))
      {
        return true;
      }
    } else
    {
      return true;
    }
    return false;
  }

  public int towardsNewPath(Direction nextDir)
  {
    return (unSeenList(nextDir).size());
  }


  /**
   * @return
   * @Himanshu This methods finds out the list of every possible direct an ant can move
   * For each direction, it checks the number of unseen nodes and returns the direction which has the highest number of unseen nodes
   * <p>
   * If every direction has 0 unseen nodes, it travels to the last node in its way that that unseen nodes
   * else it returns a random direction
   */

  public Direction getRandomAdjacentDir()
  {
    Direction toReturn = null;
    int current = 0;
    int temp;
    ArrayList<Direction> possiblePath = getPossibleDirections();
    for (Direction nextDir : possiblePath)
    {
      temp = towardsNewPath(nextDir);
      if (debug) System.out.println("Moving towards" + nextDir.toString() + "has " + temp + " new locations");
      if (temp > current)
      {
        if (toReturn != null)
        {
          undiiscoveredNodes.add(getNextNode(toReturn));
          if (debug)
            System.out.println("Adding to Undiscovered list : " + toReturn + "with pathnode " + getNextNode(toReturn).getX() + " , " + getNextNode((toReturn)).getY());
        }
        toReturn = nextDir;
        current = temp;
      } else
      {
        if (temp != 0)
        {
          undiiscoveredNodes.add(getNextNode(nextDir));

          if (debug)
            System.out.println("Adding to Undiscovered list : " + nextDir.toString() + "with pathnode " + getNextNode(nextDir).getX() + " , " + getNextNode((nextDir)).getY());
        }
      }

    }

    if (toReturn == null)
    {
      if (debug)
        System.out.println("Trapped between visited Nodes ");
      if (undiiscoveredNodes.size() < 1)
      {
        if (debug) System.out.println("No Undiscoverable Nodes Getting Random Direction");
        return Direction.getRandomDir();
      }
      PathNode start = new PathNode(ant.gridX, ant.gridY);
      int last = undiiscoveredNodes.size() - 1;
      PathNode goal = undiiscoveredNodes.get(last);
      while (unSeenList(goal).size() < 1)
      {
        undiiscoveredNodes.remove(last);
        if (undiiscoveredNodes.size() < 1) return Direction.getRandomDir();
        last = undiiscoveredNodes.size() - 1;
        goal = undiiscoveredNodes.get(last);
      }
      this.setPath(pathFinder.getPath(start, goal));
      undiiscoveredNodes.remove(last);
      pathStep = 1;
      if (debug)
        System.out.println("Moving Towards" + start.getX() + "," + start.getY() + "to" + goal.getX() + "," + goal.getY());
      antBehavior = AntBehaviors.GOTO;
      return start.getDirectionTo(path.get(1));
    }

    if (debug) System.out.println("Returining" + toReturn.toString());
    if (toReturn == null) toReturn = Direction.getRandomDir();
    return toReturn;
  }



  PathNode getNextNode(Direction dir)
  {
    if (dir == null) return new PathNode(ant.gridX, ant.gridY);
    int x = ant.gridX + dir.deltaX();
    int y = ant.gridY + dir.deltaY();
    if (debug)
      System.out.println("Current Node : " + ant.gridX + "," + ant.gridY + " Final values at the next" + x + "' " + y);
    return new PathNode(x, y);
  }

  /**
   * checks to see if it is possible to move in a direction
   *
   * @param dir
   * @return
   */
  boolean isPossible(Direction dir)
  {
    PathNode temp = new PathNode(ant.gridX + dir.deltaX(), ant.gridY + dir.deltaY());
    return pathFinder.nodeLegal(temp);
  }

  public void setPath(ArrayList<PathNode> path)
  {
    this.path = path;
  }

  @Override
  public void setReturnPath(ArrayList<PathNode> returnPath)
  {

  }

  public AntData getAnt()
  {
    return ant;
  }

  /**
   * @param path moves the ant in the path provided
   * @Himanshu
   */
  void moveAnt(ArrayList<PathNode> path)
  {
    if (path.size() < 1)
    {
      ant.action.type = AntAction.AntActionType.NOOP;
      return;
    }
    if (debug) System.out.println("AntPathStep : " + pathStep + "Path Size : " + path.size());
    int x = path.get(pathStep).getX();
    int y = path.get(pathStep).getY();
    PathNode antNode = new PathNode(ant.gridX, ant.gridY);
    PathNode destination = new PathNode(x, y);

    if (debug)
      System.out.println("start " + ant.gridX + "," + ant.gridY + " Goal" + destination.getX() + " " + destination.getY());
    dir = antNode.getDirectionTo(destination);
    if (dir == null)
    {
      if (debug) System.out.println("RETURNING NOOP");
      ant.action.type = AntAction.AntActionType.MOVE;
      ant.action.direction = Direction.getRandomDir();
      antBehavior = AntBehaviors.EXPLORE;
      pathStep++;
    } else
    {
      ant.action.type = AntAction.AntActionType.MOVE;
      ant.action.direction = dir;
    }


  }

  /**
   * @Himanshu, @Kirtus, @Aakash
   * Updates the ant at every tick
   */
  public void update()
  {
    if (debug) System.out.println("Updating Ant with ID :" + ant.id + " Behaviour " + antBehavior);
    switch (antBehavior)
    {
      case TOSPAWN:
      {
        if (ant.state == AntAction.AntState.UNDERGROUND)
        {
          ant.action.type = AntAction.AntActionType.EXIT_NEST;
          ant.action.x = spawnX;
          ant.action.y = spawnY;
        }
        break;
      }
      case EXPLORE:
      {
        if (!pathFinder.nodeLegal(getNextNode(dir)))
        {
          if (debug)
            System.out.print("Not possible to move ahead in " + dir.toString() + " at " + ant.gridX + "," + ant.gridY);
          dir = getRandomAdjacentDir();
          if (debug) System.out.print("New Direction " + dir.toString());
        }
        ant.action.type = AntAction.AntActionType.MOVE;
        ant.action.direction = dir;
        break;
      }

      case CHOOSERANDOM:
      {
        if (debug) System.out.println("Path reached and choosing Random dir to move");
        dir = getRandomAdjacentDir();
        ant.action.type = AntAction.AntActionType.MOVE;
        ant.action.direction = dir;
        break;
      }

      case PICKUPWATER:
      {
        dir = pathFinder.getDirectionToWater(ant.gridX, ant.gridY);
        if (dir == null)
        {
          antBehavior = previousBehaviour;
          return;
        }
        ant.action.type = AntAction.AntActionType.PICKUP;
        ant.action.direction = dir;
        ant.action.quantity = 5 - ant.carryUnits;
        break;
      }

      case GOTO:
      {
        if (pathStep == path.size() || path.size() < 0 || path == null)
        {
          if (debug) System.out.println("Path reached and choosing Random dir to move");
          dir = getRandomAdjacentDir();
          ant.action.type = AntAction.AntActionType.MOVE;
          ant.action.direction = dir;
          antBehavior = AntBehaviors.EXPLORE;
          return;
        } else moveAnt(path);
        break;
      }
      case HEAL:
      {
        ant.action.type = AntAction.AntActionType.HEAL;
        if (!startedToheal)
        {
          if (ant.health > 0)
          {
            healUnits = (ant.antType.getMaxHealth() / ant.health) * 2;
            startedToheal = true;
          }
        }
      }
      break;
    }
  }

  /**
   * Updates the ant to the Ants data structure
   *
   * @param ant
   */
  public void updateAnt(AntData ant)
  {
    if (debug) System.out.println("Updating Ant in ExplorerGroup " + ant.id);
    if (ant.state == AntAction.AntState.OUT_AND_ABOUT)
    {
      if (this.ant.gridX == ant.gridX && this.ant.gridY == ant.gridY)
      {
        moved = false;
        stuck++;
        if (stuck > 20)
        {
          antBehavior = AntBehaviors.EXPLORE;
          dir = Direction.getRandomDir();
          stuck = 0;
        }
      } else
      {
        moved = true;
        stuck = 0;
        switch (antBehavior)
        {
          case GOTO:
          {
            if (debug) System.out.println("Updating AntPathStep");
            pathStep++;
            break;
          }
        }
      }


    }
    this.ant = ant;
    checkForWater++;
    updateVisited(ant);
    if (!checkCriticalConditions()) updateAntBehaviour();
  }

  /**
   * @Kirtus, @Himanshu
   * Updates the AntBehaviour
   */
  void updateAntBehaviour()
  {
    if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);

    switch (antBehavior)
    {
      case TOSPAWN:
      {
        if (ant.state == AntAction.AntState.OUT_AND_ABOUT)
        {
          if (path != null || path.size() > 0)
          {
            antBehavior = AntBehaviors.GOTO;
            pathStep = 1;
            return;
          } else
          {
            antBehavior = AntBehaviors.EXPLORE;
          }

        }
        break;
      }
      case PICKUPWATER:
      {
        if (ant.health < 10)
        {
          antBehavior = AntBehaviors.HEAL;
        } else
        {
          antBehavior = previousBehaviour;
        }
        break;
      }
      case HEAL:
      {
        if (healUnits > 1 && ant.health < ant.antType.getMaxHealth() - 3)
        {
          healUnits--;
        } else
        {
          antBehavior = previousBehaviour;
          healUnits = 0;
          startedToheal = false;
        }
        break;
      }
    }
  }

  public void setGoal(int x, int y)
  {
    PathNode finalNode = new PathNode(x, y);
    PathNode startNode = new PathNode(spawnX, spawnY);
    ArrayList<PathNode> pathToGoal = pathFinder.getPath(startNode, finalNode);

    if (debug) System.out.println("Path from" + startNode + " to " + finalNode);
    if (debug) System.out.println(pathToGoal);
    this.path = pathToGoal;

  }

  @Override
  public void setGoal(PathNode goal)
  {

  }

  /**
   * @Himanshu
   * @return
   *
   * checks to see if the conditions are good to heal or pickup water
   */
  boolean checkCriticalConditions()
  {
    if (antBehavior == AntBehaviors.PICKUPWATER || antBehavior == AntBehaviors.HEAL) return false;
    if (ant.health < 20 && ant.carryUnits > 0)
    {
      previousBehaviour = antBehavior;
      antBehavior = AntBehaviors.HEAL;
      return true;
    }
    if (checkForWater < 50) return false;
    if (pathFinder.getDirectionToWater(ant.gridX, ant.gridY) != null)
    {
      if (ant.carryUnits < 3)
      {
        previousBehaviour = antBehavior;
        antBehavior = AntBehaviors.PICKUPWATER;
        checkForWater = 0;
      }
      return true;
    }
    return false;
  }

  /**
   * @param ant updates visited notes at eah tick
   * @Himanshu
   */
  private void updateVisited(AntData ant)
  {
    int radius = ant.antType.getVisionRadius();
    int x = ant.gridX;
    int y = ant.gridY;
    int xmin = Math.max(1, x - radius);
    int ymin = Math.max(1, y - radius);
    int xmax = Math.min(2500 - 2, x + radius);
    int ymax = Math.min(1500 - 2, y + radius);

    for (int i = ymin; i <= ymax; i++)
    {
      for (int j = xmin; j <= xmax; j++)
      {
        if (visited.containsKey(j))
        {
          if (!visited.get(j).containsKey(i))
          {
            visited.get(j).put(i, 0);
            if (debug) System.out.println("New Y visited" + j + " , " + i);
          }
        } else
        {
          visited.put(j, new HashMap<>());
          visited.get(j).put(ant.gridY, 0);
          if (debug) System.out.println("New X visited" + j + " , " + i);
        }
      }
    }

  }


}
