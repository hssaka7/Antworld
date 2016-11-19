package antworld.server;


import java.util.HashSet;
import java.util.Random;

import antworld.common.AntData;
import antworld.common.AntType;
import antworld.common.CommData;
import antworld.common.Constants;
import antworld.common.Direction;
import antworld.common.FoodData;
import antworld.common.FoodType;
import antworld.common.LandType;
import antworld.common.NestNameEnum;
import antworld.common.TeamNameEnum;
import antworld.common.Util;
import antworld.common.AntAction.AntActionType;


public class NearlyBrainlessBots
{
  private static final boolean DEBUG = false;
  private static final TeamNameEnum myTeam = TeamNameEnum.NEARLY_BRAINLESS_BOTS;

  private static Random random = Constants.random;

  private static Cell[][] world;
  private static Nest myNest;
 
  private static final int DIR_BIT_N  = 1;
  private static final int DIR_BIT_NE = 2;
  private static final int DIR_BIT_E  = 4;
  private static final int DIR_BIT_SE = 8;
  private static final int DIR_BIT_S  = 16;
  private static final int DIR_BIT_SW = 32;
  private static final int DIR_BIT_W  = 64;
  private static final int DIR_BIT_NW = 128;
  
  private static final int DIR_BIT_ANY_N = DIR_BIT_N | DIR_BIT_NE | DIR_BIT_NW;
  private static final int DIR_BIT_ANY_S = DIR_BIT_S | DIR_BIT_SE | DIR_BIT_SW;
  private static final int DIR_BIT_ANY_E = DIR_BIT_E | DIR_BIT_NE | DIR_BIT_SE;
  private static final int DIR_BIT_ANY_W = DIR_BIT_W | DIR_BIT_NW | DIR_BIT_SW;

  private static final int MAX_EXPLORE_DIST = 300;
  

  public static CommData chooseActionsOfAllAnts(Cell[][] worldTmp, Nest myNestTmp)
  {
    
    NearlyBrainlessBots.world = worldTmp;
    NearlyBrainlessBots.myNest = myNestTmp;
    
    CommData data = new CommData(myTeam);
    data.myAntList = myNest.getAntList();
    
    for (AntData ant : data.myAntList)
    {
      setAntAction(data, ant);
      if (DEBUG)
      { if (myNest.nestName == NestNameEnum.ARGENTINE)
        {  System.out.println (ant);
        }
      }
    }
    /*
    if (data.myAntList.size() < Constants.INITIAL_ANT_SPAWN_COUNT)
    {
      for (AntType type: AntType.values())
      {
        if (myNest.getFoodStockPile(type.getBirthFoodType()) >= type.getFoodUnitsToSpawn())
        {
          AntData ant = new AntData(Constants.UNKNOWN_ANT_ID, type, myNest.nestName, TeamNameEnum.NEARLY_BRAINLESS_BOTS);
          data.myAntList.add(ant);
          break;
        }
      }
    }
    */
    return data;
  }

  public static void setAntAction(CommData data, AntData ant)
  {
    if (ant.ticksUntilNextAction > 0)
    {
      ant.myAction.type = AntActionType.STASIS;
      return;
    }
    
    if (undergroundAction(ant)) return;
   
    Cell myCell = world[ant.gridX][ant.gridY];
    if (myCell == null) return;
    
    if (goExplore(ant)) return;

    if (goRandom(ant)) return;
    ant.myAction.type = AntActionType.STASIS;
    return;
  }
  
  
  public static boolean undergroundAction(AntData ant)
  {
    if (DEBUG) System.out.println("NearlyBrainlessBot.undergroundAction()");
    if (ant.underground == false) return false;
    
    if (ant.health < ant.antType.getMaxHealth())
    {
      ant.myAction.type = AntActionType.HEAL;
      return true;
    }
   
    ant.myAction.type = AntActionType.EXIT_NEST;
    ant.myAction.x = myNest.centerX - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);
    ant.myAction.y = myNest.centerY - Constants.NEST_RADIUS + random.nextInt(2 * Constants.NEST_RADIUS);

    return true;
  }
  
  
  public static int getDirectionBitsOpen(AntData ant)
  {
    if (DEBUG) System.out.println("  getDirectionBitsOpen()");
    int dirBits = 255;
    for (Direction dir : Direction.values())
    {
      int x = ant.gridX + dir.deltaX();
      int y = ant.gridY + dir.deltaY();
      int bit = 1 << dir.ordinal();
      
      Cell neighborCell = world[x][y];

      if (neighborCell == null) dirBits = dirBits & bit;

      else if (neighborCell.getLandType() == LandType.WATER) dirBits -= bit;
      
      else if ((neighborCell.getNest() != null) && (neighborCell.getNest() != myNest))  dirBits -= bit;

      else if (neighborCell.getGameObject() != null) dirBits -= bit;
    }
    
    //System.out.println("  getDirectionBitsOpen() dirBits="+dirBits);
    return dirBits;
  }
  
  public static int getDirBitsToLocation(int dirBits, int x, int y, int xx, int yy)
  {

    //System.out.println("  getDirBitsToLocation("+dirBits+", " + x +", " + y + ", " + xx+ ", " + yy + ")");
    //System.out.println("0 " + dirBits  +" & DIR_BIT_ANY_E=" + DIR_BIT_ANY_E);
    if (xx <= x) dirBits = dirBits & (~DIR_BIT_ANY_E);

    //System.out.println("1 " + dirBits);
    if (xx >= x) dirBits = dirBits & (~DIR_BIT_ANY_W);

    //System.out.println("2 " + dirBits);
    if (yy <= y) dirBits = dirBits & (~DIR_BIT_ANY_S);
    if (yy >= y) dirBits = dirBits & (~DIR_BIT_ANY_N);

    return dirBits;
  }


  public static boolean continueStraight(AntData ant)
  {
    if (ant.myAction.type == AntActionType.EXIT_NEST) return false;

    Direction oldDir = ant.myAction.direction;
    if (oldDir == null) return false;

    int dirBits = getDirectionBitsOpen(ant);

    int bit = 1 << oldDir.ordinal();
    if ((bit & dirBits) != 0)
    {
      ant.myAction.type = AntActionType.MOVE;
      ant.myAction.direction = oldDir;
      return true;
    }
    return false;
  }


  public static Direction getRandomDirection(int dirBits)
  {
    Direction dir = Direction.getRandomDir();
    for (int i = 0; i<Direction.SIZE; i++)
    {
      int bit = 1 << dir.ordinal();
      if ((bit & dirBits) != 0) return dir;

      dir = Direction.getRightDir(dir);
    }
    return null;
  }
  
  public static boolean attackAdjacent(AntData ant)
  {
    if (DEBUG) System.out.println("  attackAdjacent()");
    for (Direction dir : Direction.values())
    {
      int x = ant.gridX + dir.deltaX();
      int y = ant.gridY + dir.deltaY();
      Cell neighborCell = world[x][y];

      if (neighborCell == null) continue;

      if (neighborCell.getLandType() == LandType.WATER) continue;

      AntData neighborAnt = neighborCell.getAnt();

      if (neighborAnt == null) continue;
      if (neighborAnt.teamName == TeamNameEnum.NEARLY_BRAINLESS_BOTS) continue;

      ant.myAction.type = AntActionType.ATTACK;
      ant.myAction.direction = dir;
      return true;
    }
    return false;
  }
  

  
  

  
  
  public static boolean pickUpFoodAdjactent(AntData ant)
  {
    if (DEBUG) System.out.println("  pickUpFoodAdjactent()");
    if (ant.carryUnits > 0) return false;
    for (Direction dir : Direction.values())
    {
      int x = ant.gridX + dir.deltaX();
      int y = ant.gridY + dir.deltaY();
      Cell neighborCell = world[x][y];

      if (neighborCell == null) continue;

      if (neighborCell.getLandType() == LandType.WATER) continue;

      FoodData food = neighborCell.getFood();

      if (food == null) continue;

      ant.myAction.type = AntActionType.PICKUP;
      ant.myAction.direction = dir;
      ant.myAction.quantity = ant.antType.getCarryCapacity();
      return true;
    }
    return false;
  }
  

  
  
  
  public static boolean pickUpWater(AntData ant)
  {
    if (DEBUG) System.out.println("  pickUpWater()");
    if (myNest.getFoodStockPile(FoodType.WATER) > 3*Constants.INITIAL_NEST_WATER_UNITS) return false;
    
    if (ant.carryUnits > 0) 
    { 
      if (ant.carryUnits >= ant.antType.getCarryCapacity()) return false;
      if (ant.carryType != FoodType.WATER) return false;
    }
        
    
    for (Direction dir : Direction.values())
    {
      int x = ant.gridX + dir.deltaX();
      int y = ant.gridY + dir.deltaY();
      Cell neighborCell = world[x][y];

      if (neighborCell == null) continue;

      if (neighborCell.getLandType() == LandType.WATER)
      { ant.myAction.type = AntActionType.PICKUP;
        ant.myAction.direction = dir;
        ant.myAction.quantity = ant.antType.getCarryCapacity();
        return true;
      }
    }
    return false;
  }


  
  public static boolean goHome(Cell myCell, AntData ant)
  {
    if (myCell.getNest() == myNest) return false;
    
    return goToward(ant, myNest.centerX, myNest.centerY);
  }
  
  public static boolean goExplore(AntData ant)
  {

    if (Util.manhattanDistance(ant.gridX, ant.gridY, myNest.centerX, myNest.centerY) > MAX_EXPLORE_DIST) return false;

    
    int goalX = 0;
    int goalY = 0;
    if (ant.gridX > myNest.centerX) goalX = 1000000;
    if (ant.gridY > myNest.centerY) goalY = 1000000;
    
    int dirBits = getDirectionBitsOpen(ant);
    dirBits = getDirBitsToLocation(dirBits, ant.gridX, ant.gridY, goalX, goalY);
    
    if (ant.myAction.type == AntActionType.MOVE)
    { 
      int dx = ant.myAction.direction.deltaY();
      int dy = ant.myAction.direction.deltaY();
      int lastGoalX = goalX;
      int lastGoalY = goalY;
      if (dx != 0) lastGoalX = ant.gridX + dx;
      if (dy != 0) lastGoalY = ant.gridY + dy;
      
      dirBits = getDirBitsToLocation(dirBits, ant.gridX, ant.gridY, lastGoalX, lastGoalY);
    }
    
    if (dirBits == 0) return false;

    return goToward(ant, goalX, goalY);
  }

  
  public static boolean goToward(AntData ant, int x, int y)
  {
    int dirBits = getDirectionBitsOpen(ant);
    
    dirBits = getDirBitsToLocation(dirBits, ant.gridX, ant.gridY, x, y);
    
    Direction dir = getRandomDirection(dirBits);
    
    if (dir == null) return false;
    ant.myAction.type = AntActionType.MOVE;
    ant.myAction.direction = dir;
    return true;
  }
  
  public static boolean goRandom(AntData ant)
  {
    int dirBits = getDirectionBitsOpen(ant);
    Direction dir = getRandomDirection(dirBits);
    if (dir == null) return false;
    ant.myAction.type = AntActionType.MOVE;
    ant.myAction.direction = dir;
    return true;
  }
  



  public static FoodData getNearestFood(HashSet<FoodData> foodSet, int x, int y)
  {
    int minDist = 70;
    FoodData nearestFood = null;
    for (FoodData food : foodSet)
    {

      int dist = Util.manhattanDistance(x, y, food.gridX, food.gridY);
      if (dist < minDist)
      {
        minDist = dist;
        nearestFood = food;
      }
    }
    return nearestFood;
  }
  
  public static void main (String[] args)
  {
    world = new Cell[6][4];
    for (int x=0; x<6; x++)
    { 
      for (int y=0; y<4; y++)
      {
        world[x][y] = new Cell(LandType.GRASS, 0, x, y);
      }
    }
    
    world[1][1].setLandType(LandType.WATER);
    world[2][1].setLandType(LandType.WATER);
    world[3][1].setLandType(LandType.WATER);
    world[1][2].setLandType(LandType.WATER);
    
    AntData ant = new AntData(0, AntType.ATTACK, NestNameEnum.ACORN, TeamNameEnum.NEARLY_BRAINLESS_BOTS);
    ant.gridX = 4;  ant.gridY = 2;
    int dirBits1 = getDirectionBitsOpen(ant);
    if (dirBits1 != (DIR_BIT_N | DIR_BIT_NE | DIR_BIT_E | DIR_BIT_SE | DIR_BIT_S | DIR_BIT_SW | DIR_BIT_W)) System.out.println("***ERROR*** 1");
   
    
    ant.gridX = 2;  ant.gridY = 2;
    int dirBits2 = getDirectionBitsOpen(ant);
    if (dirBits2 != ( DIR_BIT_E | DIR_BIT_SE | DIR_BIT_S | DIR_BIT_SW )) System.out.println("***ERROR*** 2");
    
    
    int dirBits3 = getDirBitsToLocation(dirBits2, 2, 2, 2, 0);
    if (dirBits3 != 0) System.out.println("***ERROR*** 3  dirBits2=" + dirBits2 + ",   dirBits3=" + dirBits3);
    
    System.out.println("Done =======> NearlyBrainlessBots Test");
    
  }
  
  
}
