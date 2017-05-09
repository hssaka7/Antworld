package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class WorkerAnts extends Ants
{

  private boolean debug = false;
  private int spawnX;
  private int spawnY;

  private ArrayList<PathNode> path = new ArrayList<>();
  private ArrayList<PathNode> returnPath = new ArrayList<>();

  private int pathStep;
  private AntData ant;

  private PathNode goal;
  private AntBehaviors antBehavior;

  private Direction dir;
  private boolean moved = false;
  private PathFinder pathFinder;
  private int checkForWater = 0;
  private boolean startedToheal;
  private int healUnits;
  private AntBehaviors previousBehaviour;
  private int stuck;

  public WorkerAnts(PathFinder pathFinder, int spawnX, int spawnY, TeamNameEnum myTeam)
  {
    this.ant = new AntData(AntType.WORKER, myTeam);
    this.spawnX = spawnX;
    this.spawnY = spawnY;
    this.pathFinder = pathFinder;
    antBehavior = AntBehaviors.TOSPAWN;
    dir = Direction.getRandomDir();

  }

  @Override
  public AntData getAnt()
  {
    return ant;
  }

  /**
   * @HImanshu
   * @param path
   *
   * Move the ants given a path
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
      if (debug) System.out.println("Updating AntPathStep");
      pathStep++;
    } else
    {
      ant.action.type = AntAction.AntActionType.MOVE;
      ant.action.direction = dir;
    }


  }

  @Override
  /**
   * Updates the ant stored inside Ants structure
   */
  public void updateAnt(AntData ant)
  {
    {
      if (debug) System.out.println("Updating Ant in Ants");
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
            return;
          }
        } else
        {
          moved = true;
          stuck = 0;
          switch (antBehavior)
          {
            case GATHER:
            case RETURN:
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
      if (!checkCriticalConditions()) updateAntBehaviour();
    }
  }

  /**
   * @Himanshu
   * Makes the worker group to gather food and then switch states when appropriate
   */
  public void update()
  {
    if (debug) System.out.println("Updating Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);
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
      case GATHER:
      {
        moveAnt(path);
        break;
      }
      case RETURN:
      {
        moveAnt(returnPath);
        break;

      }
      case PICKUP:
      {
        PathNode antNode = new PathNode(ant.gridX, ant.gridY);
        dir = antNode.getDirectionTo(goal);
        ant.action.type = AntAction.AntActionType.PICKUP;
        ant.action.direction = dir;
        ant.action.quantity = ant.antType.getCarryCapacity();
        break;

      }
      case DROP:
      {

        if (ant.state == AntAction.AntState.UNDERGROUND)
        {
          {
            if (ant.carryUnits != 0)
            {
              ant.action.type = AntAction.AntActionType.DROP;
              ant.action.quantity = AntType.WORKER.getCarryCapacity();
            } else
            {
              ant.action.type = AntAction.AntActionType.EXIT_NEST;
              ant.action.x = spawnX;
              ant.action.y = spawnY;
            }
          }
        } else
        {
          ant.action.type = AntAction.AntActionType.ENTER_NEST;
        }
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

      case HEAL:
      {
        ant.action.type = AntAction.AntActionType.HEAL;
        if (!startedToheal)
        {
          healUnits = ant.carryUnits;
          startedToheal = true;
        }
        break;
      }
    }
  }

  /**
   * @Kirtus, @Himanshu, @Aakash
   * updates ant behaviour at each tick
   */
  void updateAntBehaviour()
  {
    if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);
    switch (antBehavior)
    {
      case GATHER:
      {
        if (pathStep == path.size() - 1)
        {
          antBehavior = AntBehaviors.PICKUP;
          pathStep = 1;
          if (debug) System.out.println("Ant behavior changed from GATHER TO: " + antBehavior);
        }
        break;
      }
      case RETURN:
      {
        if (pathStep == returnPath.size())
        {
          antBehavior = AntBehaviors.DROP;
          pathStep = 1;
          if (debug) System.out.println("Ant behavior changed from RETURN TO: " + antBehavior);
        }
        break;
      }
      case PICKUP:
      {
        antBehavior = AntBehaviors.RETURN;
        if (debug) System.out.println("Ant behavior changed from PICKUP TO: " + antBehavior);
        break;
      }
      case DROP:
      {
        if (ant.state != AntAction.AntState.UNDERGROUND)
        {
          antBehavior = AntBehaviors.GATHER;
          if (debug) System.out.println("Ant behavior changed from DROP TO: " + antBehavior);
        }
        break;
      }

      case TOSPAWN:
      {
        if (ant.state == AntAction.AntState.OUT_AND_ABOUT)
        {
          if (path != null || path.size() > 0)
          {
            antBehavior = AntBehaviors.GATHER;
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
        antBehavior = AntBehaviors.HEAL;

        break;
      }
      case HEAL:
      {
        if (healUnits > 1)
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

  /**
   * @return checks to see if water is near and conditions are good to heal while moving
   * @Himanshu
   */
  boolean checkCriticalConditions()
  {
    if (antBehavior == AntBehaviors.PICKUPWATER || antBehavior == AntBehaviors.HEAL) return false;
    if (ant.health < 3 && ant.carryUnits > 0)
    {
      previousBehaviour = antBehavior;
      antBehavior = AntBehaviors.HEAL;
      return true;
    }
    if (checkForWater < 50) return false;
    if (pathFinder.getDirectionToWater(ant.gridX, ant.gridY) != null)
    {
      if (ant.health < ant.antType.getMaxHealth() - 5 && ant.carryUnits == 0)
      {
        previousBehaviour = antBehavior;
        antBehavior = AntBehaviors.PICKUPWATER;
        checkForWater = 0;
      }
      return true;
    }
    return false;
  }

  @Override
  public void setGoal(int x, int y)
  {

  }

  public void setGoal(PathNode goal)
  {
    this.goal = goal;
  }

  public void setPath(ArrayList<PathNode> path)
  {
    this.path = path;
  }

  public void setReturnPath(ArrayList<PathNode> returnPath)
  {
    this.returnPath = returnPath;
  }
}
