package antworld.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import antworld.common.GameObject;
import antworld.common.PacketToClient;
import antworld.common.PacketToServer;
import antworld.common.Util;
import antworld.common.AntAction;
import antworld.common.AntAction.AntActionType;
import antworld.common.AntAction.AntState;
import antworld.common.AntData;
import antworld.common.AntType;
import antworld.common.Constants;
import antworld.common.FoodData;
import antworld.common.NestData;
import antworld.common.NestNameEnum;

import static antworld.common.AntData.UNKNOWN_ANT_ID;


/**
 * Throwing uncaught exceptions causes the server to crash.
 * True, it is bad to have the server crash, however these
 * IllegalArgumentExceptions will never happen.<br>
 * So, why include them then?<br>
 * Having a function check for and throw  IllegalArgumentExceptions is a way to
 * document to future developers what the function is supposed to do (and not do).
 */
public class Nest extends NestData implements Serializable
{
  private static final long serialVersionUID = Constants.VERSION;

  public enum NestStatus {EMPTY, CONNECTED, DISCONNECTED, UNDERGROUND};

  private NestStatus status = NestStatus.EMPTY;
  private CommToClient client = null;
  private HashMap<Integer,AntData> antCollection = new HashMap<>();

  public Nest(NestNameEnum nestName, int x, int y)
  {
    super(nestName, null, x, y);
  }


  public synchronized void setClient(CommToClient client, PacketToServer packetIn)
  {
    System.out.println("Nest.setClient: " + packetIn);
    if (status == NestStatus.EMPTY)
    {
      antCollection.clear();
      foodInNest  = Constants.INITIAL_FOOD_UNITS;
      waterInNest = Constants.INITIAL_NEST_WATER_UNITS;
    }

    team = packetIn.myTeam;

    if (team == null)
    {
      throw new IllegalArgumentException("team == null");
    }

    for (AntData ant : antCollection.values())
    {
      if (ant.state != AntState.DEAD) score+= ant.antType.getScore();
    }


    for (AntData ant : packetIn.myAntList)
    {
      if (ant.id != AntData.UNKNOWN_ANT_ID) continue;
      if (ant.action.type != AntActionType.BIRTH) continue;
      if (ant.antType == null) continue;
      spawnAnt(ant.antType);
    }


    this.client = client;
    status = NestStatus.CONNECTED;
  }


  /**
   * DO NOT call this method from the mainGameLoop thread as client.closeSocket() takes
   * 100s of ticks.
   */
  public synchronized void disconnectClient()
  {
    if (client != null)
    {
      client.closeSocket("Server disconnecting");
      client = null;
    }
    //Do not change if status is EMPTY or UNDERGROUND
    if (status == NestStatus.CONNECTED) status = NestStatus.DISCONNECTED;
  }


  public synchronized void sendAllAntsUnderground(AntWorld world)
  {
    if (status == NestStatus.EMPTY) return;
    if (status == NestStatus.UNDERGROUND) return;

    status = NestStatus.UNDERGROUND;
    for (AntData ant : antCollection.values())
    {
      world.removeGameObject(ant);
      ant.state = AntState.UNDERGROUND;
      ant.gridX = centerX;
      ant.gridY = centerY;
    }
  }


  public int getResourceCount(GameObject.GameObjectType type)
  {
    if (type == GameObject.GameObjectType.ANT) return antCollection.size();
    if (type == GameObject.GameObjectType.FOOD) return foodInNest;
    return waterInNest;
  }

  public HashMap<Integer,AntData> getAnts()
  {
    return antCollection;
  }

  public void addFood(GameObject.GameObjectType type, int quantity)
  {
    if (type == GameObject.GameObjectType.WATER) waterInNest += quantity;
    if (type == GameObject.GameObjectType.FOOD) foodInNest += quantity;
  }

  
  public int calculateScore()
  {
    int score = foodInNest;

    for (AntData ant : antCollection.values())
    {
      if (ant.state != AntState.DEAD) score+= ant.antType.getScore();
    }
    return score;
  }
  
  public double getTimeOfLastMessageFromClient()
  {
    if (client == null) return 0;
    return client.getTimeOfLastMessageFromClient();
  }

  private AntData spawnAnt(AntType antType)
  {
    //System.out.println("Nest.spawnAnt(): " + this);

    if (foodInNest < antType.TOTAL_FOOD_UNITS_TO_SPAWN)
    {
      return null;
    }

    foodInNest -= antType.TOTAL_FOOD_UNITS_TO_SPAWN;
    AntData ant = Ant.createAnt(antType, nestName, team);
    //System.out.println(ant);

    ant.gridX = centerX;
    ant.gridY = centerY;
    antCollection.put(ant.id, ant);
    return ant;
  }

  


  
  public NestStatus getStatus() {return status;}
  
  
  public boolean isInNest(int x, int y)
  { if (Util.manhattanDistance(centerX, centerY, x, y) <= Constants.NEST_RADIUS) return true;
    return false;
  }


  public void updateRemoveDeadAntsFromAntList()
  {
    Iterator iterator = antCollection.entrySet().iterator();
    while (iterator.hasNext())
    {
      Map.Entry pair = (Map.Entry)iterator.next();
      AntData ant = (AntData) pair.getValue();
      if (ant.state == AntState.DEAD) iterator.remove(); // avoids a ConcurrentModificationException
    }
  }

  public void updateReceivePacket(AntWorld world)
  {
    //System.out.println("Nest.updateReceive()==========================["+team+"]:"+commData.myAntList.size());
    // receiving common from client
    if (status != NestStatus.CONNECTED) return;
    PacketToServer packetIn = client.popPacketIn(world.getGameTick());
    if (packetIn == null) return;
    
    if (packetIn.myAntList == null) return;

    for (AntData clientAnt : packetIn.myAntList)
    {
      if (clientAnt.id == UNKNOWN_ANT_ID)
      {
        if (clientAnt.action.type != AntActionType.BIRTH) continue;

        spawnAnt(clientAnt.antType);
        continue;
      }

      AntData serverAnt = antCollection.get(clientAnt.id);

      if (serverAnt == null)
      {
        System.out.println("Nest.updateRecv() ant Illegal Ant =" + clientAnt);
        continue;
      }

      boolean okay = Ant.update(world, serverAnt, clientAnt.action);
      //if (okay)
      //{ //serverAnt.myAction.copy(clientAnt.myAction);
      //}
    }
  }

  public void updateRemoveDeadAntsFromWorld(AntWorld world)
  {
    for (AntData ant : antCollection.values())
    {
      if (ant.state == AntState.DEAD)
      {
        world.removeGameObject(ant);
        int foodUnits = AntType.getDeadAntFoodUnits();
        if (ant.carryUnits > 0 && ant.carryType == GameObject.GameObjectType.FOOD)
        {
          foodUnits += ant.carryUnits;
        }
  
        FoodData droppedFood = new FoodData(GameObject.GameObjectType.FOOD, ant.gridX, ant.gridY, foodUnits);
        world.addFood(null, droppedFood);
        //System.out.println("Nest.update() an Ant had died: Current Ant Populatuion = " + antList.size());
        ant.action.type = AntAction.AntActionType.DIED;
      }
    }
  }

  //public AntData getAntByID(int antId)
  //{
  //  tmpAntData.id = antId;
  //  int index = Collections.binarySearch(antList, tmpAntData);
  //  return antList.get(index);
  //}

  public void updateSendPacket(AntWorld world, NestData[] nestDataList)
  {
    if (team == null) return;
    if (status != NestStatus.CONNECTED) return;

    PacketToClient packetOut = new PacketToClient(nestName);

    packetOut.nestData = nestDataList;

    packetOut.tick = world.getGameTick();
    packetOut.tickTime = world.getGameTime();
    //commData.enemyAntList = new ArrayList<>();
    //commData.foodSet = new ArrayList();


    for (AntData ant : antCollection.values())
    {
      packetOut.myAntList.add(ant);
      
      //world.appendAntsInProximity(ant, commData.enemyAntList);
      //world.appendFoodInProximity(ant, commData.foodSet);
    }

    client.pushPacketOut(packetOut, world.getGameTick());
  }
  /*
  public NestData createNestData()
  {
    NestData data = new NestData(nestName, team, centerX, centerY);
    data.score = calculateScore();
    return data;
  }
  */
}
