package antworld.server;

import java.util.Random;

import antworld.common.AntAction;
import antworld.common.AntAction.AntActionType;
import antworld.common.AntAction.AntState;
import antworld.common.AntData;
import antworld.common.AntType;
import antworld.common.Constants;
import antworld.common.FoodData;
import antworld.common.GameObject;
import antworld.common.GameObject.GameObjectType;
import antworld.common.LandType;
import antworld.common.NestNameEnum;
import antworld.common.TeamNameEnum;

public class AntMethods
{
  public static final int INVALID_ANT_ID = -7;
  private static volatile int globalAntID = -1;
  private static Random random = Constants.random;

  
  public static AntData createAnt(AntType type, NestNameEnum nestName, TeamNameEnum teamName)
  {
    int id = getNewID();
    
    return new AntData(id, type, nestName, teamName);
  }

  
  public static synchronized int getNewID()
  {
    globalAntID++;
    return globalAntID;
  }
  
  public static int getAttackDamage(AntData ant)
  {
    int damage = 0;
    int dice = ant.antType.getAttackDiceD4();
    for (int i=0; i<dice; i++)
    {
      //add a uniformly distributed integer 1, 2, 3 or 4.
      damage += random.nextInt(4) + 1;
    }
    return damage;
  }
  

  
  public static boolean update(AntWorld world, AntData ant, AntAction action)
  {
    System.out.println("Ant.update(): "+ant+ "\n   =======>" + action);
    if (ant.state == AntState.OUT_AND_ABOUT)
    { if (random.nextDouble() < ant.antType.getAttritionDamageProbability()) ant.health--;
    }
    
    if (ant.health < 0) ant.state = AntState.DEAD;
    if (ant.state == AntState.DEAD) return false;

    if (ant.action.type == AntActionType.BUSY)
    {
      if (ant.action.quantity > 0)
      { ant.action.quantity--;
        return false;
      }
    }
    
    if (action == null || action.type == AntActionType.NOOP || action.type == AntActionType.BUSY)
    {
      ant.action.type = AntActionType.NOOP;
      return false;
    }

    
   
    //The birth action is handled in Nest so nothing to do here.
    if (action.type == AntActionType.BIRTH) return true;

    if (action.type == AntActionType.EXIT_NEST)
    {
      if (ant.state != AntState.UNDERGROUND) return false;
      Cell exitCell = world.getCell(action.x,action.y);
      //System.out.println("     ..... EXIT_NEST: exitCell="+exitCell + "("+ action.x+", " +action.y+")");
      
      if (exitCell == null) return false;
      if (!exitCell.isEmpty()) return false;
      if (exitCell.getNestName() != ant.nestName) return false;
      
      ant.gridX = action.x;
      ant.gridY = action.y;
      ant.state = AntState.OUT_AND_ABOUT;
      world.addAnt(ant);
      
      return true;
    }
    
    if (action.type == AntActionType.ENTER_NEST)
    {
      if (ant.state != AntState.OUT_AND_ABOUT) return false;
      if (world.getCell(ant.gridX, ant.gridY).getNestName() != ant.nestName) return false;

      ant.state = AntState.UNDERGROUND;
      world.removeGameObject(ant);
      return true;
    }
   
    Nest myNest = world.getNest(ant.nestName);
    
    if (action.type == AntActionType.HEAL)
    { 
      if (ant.state == AntState.UNDERGROUND)
      { if (ant.health >= ant.antType.getMaxHealth()) return false;
        if (myNest.getResourceCount(GameObjectType.WATER) < 1) return false;
        myNest.addFood(GameObjectType.WATER, -1);
        ant.health ++;
        return true;
      }
      

      if (ant.carryType != GameObjectType.WATER) return false;
      if (ant.carryUnits <= 0) return false;
      AntData targetAnt = getTargetAnt(world, ant, action);
      if (targetAnt == null) return false;
      
      if (targetAnt.health >= targetAnt.antType.getMaxHealth()) return false;
      ant.carryUnits--;
      targetAnt.health ++;
      return true;
    }
      
    if (action.type == AntActionType.ATTACK)
    {
      AntData targetAnt = getTargetAnt(world, ant, action);
      if (targetAnt == null) return false;
      targetAnt.health -= getAttackDamage(ant);
      return true;
    }
    
    
    if (action.type == AntActionType.MOVE)
    {
      Cell cellTo = getTargetCell(world, ant, action);
      if (cellTo == null) return false;
      if (cellTo.getLandType() == LandType.WATER) return false;
      
      if (ant.state != AntState.OUT_AND_ABOUT) return false;

      if (!cellTo.isEmpty()) return false;
      Cell cellFrom = world.getCell(ant.gridX, ant.gridY);

      ant.action.quantity = ant.antType.getBaseMovementTicksPerCell();
      if (cellTo.getHeight() > cellFrom.getHeight()) ant.action.quantity *= ant.antType.getUpHillMultiplier();
      if (ant.carryUnits > ant.antType.getCarryCapacity()/2)
      { ant.action.quantity *= ant.antType.getEncumbranceMultiplier();
      }
      ant.action.type = AntActionType.BUSY;
      world.moveAnt(ant, cellFrom, cellTo);

      return true;
    }
    
    if (action.type == AntActionType.DROP)
    {
      if (ant.carryType == null) return false;
      if (ant.carryUnits <= 0) return false;
      if (action.quantity <= 0) return false;
      
      
      if (action.quantity > ant.carryUnits) action.quantity = ant.carryUnits;
      if (ant.state == AntState.UNDERGROUND)
      { 
    	  myNest.addFood(ant.carryType, action.quantity);
        ant.carryUnits -= action.quantity;
        if (ant.carryUnits == 0) ant.carryType = null;
        //System.out.println("Ant.DROP "+ ant);
        
        return true;
      }
      
      
      Cell targetCell = getTargetCell(world, ant, action);
      if (targetCell == null) return false;
      if (targetCell.getLandType() == LandType.NEST)
      {
    	//System.out.println("Ant.DROP-- landtype: nest = " + targetCell.getNest().nestName 
    	//		+ ", food[" + ant.carryType + "] drop="+action.quantity + 
    	//		"stockPile="+targetCell.getNest().getFoodStockPile(ant.carryType));
        targetCell.getNest().addFood(ant.carryType, action.quantity);
        //System.out.println("       After drop: stockPile="+targetCell.getNest().getFoodStockPile(ant.carryType));
        
        //System.out.println("        targetCell.getNest()="+System.identityHashCode(targetCell.getNest()));
        //System.out.println("Ant.DROP in Nest"+ targetCell.getNestName() + "["+ant.carryType+"]="+ant.carryUnits );
      }
      else
      { if (!targetCell.isEmpty()) return false;
      
        int x = targetCell.getLocationX();
        int y = targetCell.getLocationY();
        FoodData droppedFood = new FoodData(ant.carryType, x, y, ant.carryUnits);
        world.addFood(null, droppedFood);
      }
      ant.carryUnits -= action.quantity;
      if (ant.carryUnits == 0) ant.carryType = null;
      return true;
    }
    
    
    if (action.type == AntActionType.PICKUP)
    {
      if (action.quantity <=0) return false;
      Cell targetCell = getTargetCell(world, ant, action);
      if (targetCell == null) return false;
      FoodData groundFood = null;

      if (targetCell.getLandType() == LandType.WATER)
      { 
        if ((ant.carryUnits > 0) && (ant.carryType!=GameObjectType.WATER)) return false;
        ant.carryType = GameObjectType.WATER;
      }
      else
      {  
        groundFood = targetCell.getFood();
        if (groundFood == null) return false;

        if ((ant.carryUnits > 0) && (ant.carryType!=groundFood.type)) return false;
        if (action.quantity > groundFood.quantity) action.quantity = groundFood.quantity;
        ant.carryType = groundFood.type;
      }
      if (ant.carryUnits + action.quantity > ant.antType.getCarryCapacity()) action.quantity = ant.antType.getCarryCapacity() - ant.carryUnits;
      ant.carryUnits += action.quantity;
      
      if (targetCell.getLandType() != LandType.WATER)
      {
        groundFood.quantity -=action.quantity;
        if (groundFood.quantity <= 0) world.removeGameObject(groundFood);
      }
      
      
      return true;
    }
    
    return false;
  }
  
  

  
  private static Cell getTargetCell(AntWorld world, AntData ant, AntAction action)
  {
    if (action.direction == null) return null;
    int targetX = ant.gridX + action.direction.deltaX();
    int targetY = ant.gridY + action.direction.deltaY();
    return world.getCell(targetX, targetY);
  }

  
  
  private static AntData getTargetAnt(AntWorld world, AntData ant, AntAction action)
  {
    Cell targetCell = getTargetCell(world, ant, action);
    if (targetCell == null) return null;
    return targetCell.getAnt();
  }
}
