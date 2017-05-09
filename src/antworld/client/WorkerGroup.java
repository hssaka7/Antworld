package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.TeamNameEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class WorkerGroup
{

  boolean debug = false;
  private final int spawnX;
  private final int spawnY;
  private final PathFinder pathFinder;
  private int count;
  public boolean assigned;
  private int spawnCount = 1;
  private HashMap<Integer, Ants> ants = new HashMap<>();

  private ArrayList<PathNode> path = new ArrayList<>();
  private ArrayList<PathNode> returnPath = new ArrayList<>();

  private ArrayList<Ants> newAnts = new ArrayList<>();

  private HashSet<Integer> include = new HashSet<>();

  PathNode start;
  PathNode goal;


  /**
   * @param myTeam
   * @param pathFinder
   * @param spawnX
   * @param spawnY     creates a worker group
   * @Aakash
   */
  public WorkerGroup(TeamNameEnum myTeam, PathFinder pathFinder, int spawnX, int spawnY)
  {
    this.spawnX = spawnX;
    this.spawnY = spawnY;
    this.pathFinder = pathFinder;
    count = 3;
    for (int i = 0; i < count; i++) {
      System.out.println("Adding Worker Ants " + i);
      Ants tempData = new WorkerAnts(pathFinder, spawnX, spawnY, myTeam);
      newAnts.add(tempData);
    }

  }

  /**
   * @return Gets Antlist from the newAnts arraylist
   * @Aakash
   */
  ArrayList<Ants> getAntsList()
  {
    if (debug) System.out.println("Returning Ants");
    return newAnts;
  }

  /**
   * @return gets the antlist from the Ants Data Structure
   * @Aakash
   */
  ArrayList<AntData> getAntList()
  {

    ArrayList<AntData> toReturn = new ArrayList<>();
    for (Ants ant : ants.values()) {
      if (!include.contains(ant.getAnt().id)) continue;
      System.out.println("Adding Worker Ant");
      toReturn.add(ant.getAnt());
    }
    include.clear();
    return toReturn;
  }

  /**
   * @param ant Updates the ant each at each tick
   * @Himanshu, @kirtus, @Aakash
   */

  void updateAnt(AntData ant)
  {
    include.add(ant.id);
    if (debug) System.out.println("Updating Ant in WorkerGroup " + ant.id);
    if (ant.state == AntAction.AntState.UNDERGROUND && !ants.containsKey(ant.id)) {
      if (debug) System.out.println("Adding to List in Worker Group" + ant.id);
      if (newAnts.size() < 1) return;
      Ants temp = newAnts.get(newAnts.size() - 1);
      temp.updateAnt(ant);
      ants.put(ant.id, temp);
      temp.setPath(path);
      temp.setReturnPath(returnPath);
      temp.setGoal(goal);
      newAnts.remove(newAnts.size() - 1);
      return;
    }
    if (debug) System.out.println("Updating to List in Worker Group" + ant.id);
    if (ants.get(ant.id) != null) {
      ants.get(ant.id).updateAnt(ant);
    }
  }

  void chooseAction()
  {
    for (Ants ant : ants.values()) {
      if (!include.contains(ant.getAnt().id)) continue;
      ant.update();
    }
  }

  void setGoal(int x, int y)
  {
    this.goal = new PathNode(x, y);
    this.findPath();
  }


  public void findPath()
  {
    if (debug) System.out.println("Finding Path ----------------------------------------------");
    start = new PathNode(spawnX, spawnY);
    if (true) {
      if (debug) System.out.println("Start: ------------" + start.getX() + " " + start.getY());
      if (debug) System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());
    }
    path = pathFinder.getPath(start, goal);
    ArrayList<PathNode> dont = new ArrayList<>();
    dont.addAll(path);
    dont.remove(start);
    dont.remove(goal);
    // required path to Retrun
    if (dont.size() > 0) {
      returnPath = pathFinder.getPathSelective(dont.get(dont.size() - 1), start, dont);
    } else {
      returnPath = pathFinder.getPath(start, goal);
    }

    if (debug) {
      System.out.println("Start: ------------" + start.getX() + " " + start.getY());
      System.out.println(path);
      System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());

      System.out.println("--------------------" + start.getX() + " " + start.getY());

      System.out.println(returnPath);

      System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());
    }
  }


}
