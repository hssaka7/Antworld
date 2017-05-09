package antworld.client;

import antworld.common.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;


/**
 * This is a very simple example client that implements the following protocol:
 * <ol>
 * <li>The server must already be running (either on a networked or local machine) and
 * listening on port 5555 for a client socket connection.
 * The default host for the server is foodgame.cs.unm.edu on port 5555.</li>
 * <li>The client opens a socket to the server.</li>
 * <li>The client then sends a PacketToServer with PacketToServer.myTeam
 * set to the client's team enum.<br>
 * <p>
 * <ul>
 * <li>If this is the client's first connection this game: The client may spawn its
 * initial ants in this first message or may choose to wait for a future turn to
 * spawn the ants.</li>
 * <li>If the client is reconnecting, then the client should set myAntList = null.
 * This will cause the next message from the server to
 * include a full list of the client's ants (including ants that are underground,
 * busy, and noop).</li>
 * </ul>
 * </li>
 * <p>
 * <li>
 * The server will then send a populated PacketToClient message to the client.
 * </li>
 * <li>
 * Each tick of server, the server will send a PacketToClient message to each client.
 * After receiving the server update, the client should choose an action for each of its
 * ants and send a PacketToServer message back to the server.
 * </li>
 * </ol>
 */


public class ArmyAntClient
{
  int gametick = 0;
  private static final boolean DEBUG = true;
  private final TeamNameEnum myTeam;
  private ObjectInputStream inputStream = null;
  private ObjectOutputStream outputStream = null;
  private boolean isConnected = false;
  private NestNameEnum myNestName = null;
  private int centerX, centerY;
  private Socket clientSocket;

  private HashMap<Integer, Ants> assigendAnt = new HashMap<>(); //here integer is the ID number of ant
  private HashMap<Integer, Ants> unAssigendAnt = new HashMap<>(); // here integer is the index in the array

  private HashMap<Integer, WorkerGroup> assigendAnts = new HashMap<>(); //here integer is the ID number of ant
  private HashMap<Integer, WorkerGroup> unAssigendAnts = new HashMap<>(); // here integer is the index in the array

  private HashMap<Integer, WorkerGroup> assignedWorkerGroups = new HashMap<>();
  private HashMap<Integer, FoodData> clientFoodList = new HashMap<>();


  private ArrayList<WorkerGroup> groups = new ArrayList<>();
  private int birthCount; //number of ants being generated at each turn

  //Map utilities @KirtusL
  private int worldWidth; //width of map
  private int worldHeight; //height of map
  private BufferedImage map; //The map to be used
  private PathFinder pathFinder;

  private boolean debug = false;
  private boolean initial = true;
  private HashSet<Integer> include = new HashSet<>();
  ArrayList<Integer> nodesTosearch;


  /**
   * A random number generator is created in Constants. Use it.
   * Do not create a new generator every time you want a random number nor
   * even in every class were you want a generator.
   */
  private static Random random = Constants.random;


  public ArmyAntClient(String host, TeamNameEnum team, boolean reconnect)
  {
    myTeam = team;
    System.out.println("Starting " + team + " on " + host + " reconnect = " + reconnect);

    map = Util.loadImage("AntWorld.png", null);
    worldWidth = map.getWidth();
    worldHeight = map.getHeight();
    pathFinder = new PathFinder(map);

    isConnected = openConnection(host, reconnect);
    if (!isConnected) System.exit(0);


    mainGameLoop();
    closeAll();
  }

  /**
   * Initial Nodes for Exploreres
   *
   * @Himanshu, @Kirtus
   */
  void initializeNodesTosearch()
  {
    nodesTosearch = new ArrayList<Integer>(Arrays.asList(308, 1125, 633, 1419, 530, 1157, 95, 1295, 64, 679, 517, 987,
    320, 938, 39, 379, 379, 49, 596, 543, 853, 273, 733, 394, 913, 113, 1149, 297, 1667, 213, 940, 673, 2119, 109, 1824, 84, 1652, 664, 1927,
    716, 2155, 1389, 2419, 1123, 2475, 873, 2419, 1123, 2399, 1279, 2177, 1363, 2276, 1273, 1531, 1165, 1363, 1049, 822, 776, 644, 1321,
    309, 1125, 263, 736, 392, 392, 715, 282, 1199, 116, 1970, 287, 2232, 126, 2400, 760, 2157, 1189, 2403, 1124, 1818, 1256, 840, 1316));
  }


  private boolean openConnection(String host, boolean reconnect)
  {
    if (debug) System.out.println("opening connection");
    try
    {
      clientSocket = new Socket(host, Constants.PORT);
    } catch (UnknownHostException e)
    {
      System.err.println("Client Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    } catch (IOException e)
    {
      System.err.println("Client Error: Could not open connection to " + host
      + " on port " + Constants.PORT);
      e.printStackTrace();
      return false;
    }

    try
    {
      outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
      inputStream = new ObjectInputStream(clientSocket.getInputStream());

    } catch (IOException e)
    {
      System.err.println("Client Error: Could not open i/o streams");
      e.printStackTrace();
      return false;
    }

    PacketToServer packetOut = new PacketToServer(myTeam);

    if (reconnect) packetOut.myAntList = null;
    else
    {
      //Connected!
    }


    send(packetOut);
    return true;

  }

  /**
   * @param packetToClient
   * @param packetOut
   * @Himanshu updates data received at each tick
   */
  void updateReceivedData(PacketToClient packetToClient, PacketToServer packetOut)
  {
    if (packetToClient.foodList != null && packetToClient.foodList.size() > 0)
    {
      updateFoodSites(packetToClient.foodList, packetOut);
    }
  }

  /**
   * @param foodList
   * @param packetOut updates Foods sites contained by client
   * @Himanshu
   */
  void updateFoodSites(ArrayList<FoodData> foodList, PacketToServer packetOut)
  {
    for (FoodData food : foodList)
    {
      if (!isContainedinFoodList(food))
      {
        addFoodData(food, packetOut);
      } else
      {
        updateFoodData(food);
      }
    }

  }

  /**
   * @param food
   * @param packetOut adds food clients list
   * @Aakash
   */
  void addFoodData(FoodData food, PacketToServer packetOut)
  {
    if (food.quantity > 20)
    {
      Direction randDir = Direction.getRandomDir();
      WorkerGroup group = new WorkerGroup(myTeam, pathFinder, centerX - randDir.deltaX() * random.nextInt(10),
      centerY - randDir.deltaY() * random.nextInt(10));


      group.setGoal(food.gridX, food.gridY);
      addGroup(group, packetOut);
      clientFoodList.put(getDistance(food), food);
      assignedWorkerGroups.put((getDistance(food)), group);
    }
  }

  /**
   * @param food updates fooddata contained by client
   * @Aakash
   */
  void updateFoodData(FoodData food)
  {
    clientFoodList.put(getDistance(food), food);
  }

  /**
   * @param packetOut creates new explorers to predefined nodes
   * @Himanshu, @Kirtus
   */
  void createExplorers(PacketToServer packetOut)
  {
    initializeNodesTosearch();
    for (int i = 0; i < nodesTosearch.size(); i += 2)
    {
      Direction randDir = Direction.getRandomDir();
      ExplorerAnts explorer = new ExplorerAnts(pathFinder, centerX - randDir.deltaX() * random.nextInt(7),
      centerY - randDir.deltaY() * random.nextInt(7), myTeam);
      addAnt(explorer, packetOut);
      explorer.setGoal(nodesTosearch.get(i), nodesTosearch.get(i + 1));
    }
//
  }

  /**
   * @Kirtus
   * Adds small group
   * @param packetOut
   * @param packetIn
   */
  void createSmallExplorers(PacketToServer packetOut, PacketToClient packetIn)
  {
    initializeNodesTosearch();
    if (packetIn.nestData[myNestName.ordinal()].foodInNest > 50)
    {
      for (int i = 0; i < 1; i += 2)
      {
        Direction randDir = Direction.getRandomDir();
        ExplorerAnts explorer = new ExplorerAnts(pathFinder, centerX - randDir.deltaX() * random.nextInt(7),
        centerY - randDir.deltaY() * random.nextInt(7), myTeam);
        addAnt(explorer, packetOut);
      }
    }
  }

  /**
   * @param food
   * @return checks if food is already contained in client foodlist
   * @Aakash
   */
  boolean isContainedinFoodList(FoodData food)
  {
    int foodDistance = getDistance(food);
    for (Integer distance : clientFoodList.keySet())
    {
      if (distance == foodDistance) return true;
    }
    return false;
  }

  /**
   * @param food
   * @return
   * @Himanshu gets the dtrainght distance from nest, used as key to store food
   */
  int getDistance(FoodData food)
  {
    return (food.gridX ^ 2 + food.gridY ^ 2);
  }

  /**
   * @param ants
   * @param packetOut aads the ant to the list or group contained by client
   * @Himanshu
   */
  void addAnt(Ants ants, PacketToServer packetOut)
  {
    if (debug) System.out.println("Adding ants tp antList");
    unAssigendAnt.put(birthCount, ants);
    packetOut.myAntList.add(ants.getAnt());
    birthCount++;
  }

  /**
   * @HImanshu
   * @param ants
   * @param worker
   * @param packetOut
   *
   * Adds ants , this is designed for worker group
   */
  void addAnt(Ants ants, WorkerGroup worker, PacketToServer packetOut)
  {
    System.out.println("Adding ants to group and antlist");
    unAssigendAnt.put(birthCount, ants);
    unAssigendAnts.put(birthCount, worker);
    packetOut.myAntList.add(ants.getAnt());
    birthCount++;
  }

  /**
   * @param group
   * @param packetOut adds the group to the antlist contained by client
   * @Himanshu
   */
  void addGroup(WorkerGroup group, PacketToServer packetOut)
  {
    groups.add(group);
    for (Ants ants : group.getAntsList())
    {
      addAnt(ants, group, packetOut);
    }
  }

  /**
   * @Himanshu
   * @param packetIn
   * updates ants received from srver to the data structure i client side
   *
   */
  void updateAntsfromServer(PacketToClient packetIn)
  {
    ArrayList<AntData> antlist = packetIn.myAntList;

    if (antlist.size() < 1 || antlist == null) return;

    int index = 0;
    AntData ant = antlist.get(index);
    while (assigendAnt.containsKey(ant.id))
    {
      include.add(ant.id);
      if (debug) System.out.println("updating ant to list at index " + index + "with Id" + ant.id);
      if (!assigendAnts.isEmpty() && assigendAnts.containsKey(ant.id))
      {
        assigendAnts.get(ant.id).updateAnt(ant);
      } else assigendAnt.get(ant.id).updateAnt(ant);
      index++;
      if (index == antlist.size()) break;
      ant = antlist.get(index);
    }

    for (int i = 0; i < birthCount; i++)
    {
      if (index + i < antlist.size())
      {
        ant = antlist.get(index + i);
      }
      ant.gridX = 0;
      ant.gridY = 0;
      if (debug) System.out.println("Removing ant to list at index " + i);
      Ants temp = unAssigendAnt.get(i);
      if (unAssigendAnts.containsKey(i))
      {
        WorkerGroup group = unAssigendAnts.get(i);
        assigendAnts.put(ant.id, group);
        assigendAnt.put(ant.id, temp);
        group.updateAnt(ant);
        unAssigendAnts.remove(i);
        unAssigendAnt.remove(i);
      } else
      {
        assigendAnt.put(ant.id, temp);
        include.add(ant.id);
        temp.updateAnt(ant);
        unAssigendAnt.remove(i);
      }
    }

    unAssigendAnt.clear();
    unAssigendAnts.clear();
    birthCount = 0;
  }

  public void closeAll()
  {
    System.out.println("ClientRandomWalk.closeAll()");
    {
      try
      {
        if (outputStream != null) outputStream.close();
        if (inputStream != null) inputStream.close();
        clientSocket.close();
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk Error: Could not close");
        e.printStackTrace();
      }
    }
  }

  /**
   * This method is called ONCE after the socket has been opened.
   * The server assigns a nest to this client with an initial ant population.
   */
  public void setupNest(PacketToClient packetIn)
  {

    myNestName = packetIn.myNest;
    centerX = packetIn.nestData[myNestName.ordinal()].centerX;
    centerY = packetIn.nestData[myNestName.ordinal()].centerY;
    System.out.println("ClientRandomWalk: ==== Nest Assigned ===>: " + myNestName);
  }

  /**
   * Called after socket has been created.<br>
   * This simple example client runs in a single thread. <br>
   * The mainGameLoop() has the following structure:<br>
   * <ol>
   * <li>Start a blocking listen for message from server.</li>
   * <li>When server message is received, if a nest has not yet been set up,
   * then setup the nest.</li>
   * <li> Assign actions to all ants</li>
   * <li> Send ant actions to server.</li>
   * <li> Loop back to step 1.</li>
   * </ol>
   * This NOT a "tight loop" because the blocking socket read
   * will not return until the server sends the next message. Thus, this loop
   * uses the server as a timer.
   */
  public void mainGameLoop()
  {

    while (true)
    {
      PacketToClient packetIn = null;
      try
      {
        if (DEBUG) System.out.println("ClientRandomWalk: listening to socket....");
        packetIn = (PacketToClient) inputStream.readObject();
        if (DEBUG)
          System.out.println("ClientRandomWalk: received <<<<<<<<<" + inputStream.available() + "<...\n" + packetIn);

        if (packetIn.myNest == null)
        {
          System.err.println("ClientRandomWalk***ERROR***: Server returned NULL nest");
          System.exit(0);
        }
      } catch (IOException e)
      {
        System.err.println("ClientRandomWalk***ERROR***: client read failed");
        e.printStackTrace();
        System.exit(0);

      } catch (ClassNotFoundException e)
      {
        System.err.println("ServerToClientConnection***ERROR***: client sent incorrect common format");
        e.printStackTrace();
        System.exit(0);
      }


      if (myNestName == null) setupNest(packetIn);
      if (myNestName != packetIn.myNest)
      {
        System.err.println("Army: !!!!ERROR!!!! " + myNestName);
      }

      if (DEBUG) System.out.println("Army: chooseActions: " + myNestName);


      PacketToServer packetOut = new PacketToServer(myTeam);

      updateAntsfromServer(packetIn);
      chooseActionsOfAllAnts(packetIn, packetOut);


      if (initial)
      {
        System.out.println("Building Paths : will Take a while");
        createExplorers(packetOut);
        initial = false;
      }
      updateReceivedData(packetIn, packetOut);

      if (gametick % 400 == 0)
      {
        createSmallExplorers(packetOut, packetIn);
        gametick = 0;
      }
      gametick++;

      send(packetOut);

    }
  }


  private void send(PacketToServer packetOut)
  {
    try
    {
      System.out.println("Army: Sending>>>>>>>: " + packetOut);
      outputStream.writeObject(packetOut);
      outputStream.flush();
      outputStream.reset();
    } catch (IOException e)
    {
      System.err.println("Army***ERROR***: client write failed");
      e.printStackTrace();
      System.exit(0);
    }
  }


  private PacketToServer chooseActionsOfAllAnts(PacketToClient packetIn, PacketToServer packetOut)
  {

    for (Ants ants : assigendAnt.values())
    {
      if (!include.contains(ants.getAnt().id) || assigendAnts.containsKey(ants.getAnt().id)) continue;
      ants.update();
      packetOut.myAntList.add(ants.getAnt());
    }

    for (WorkerGroup group : groups)
    {
      group.chooseAction();
      packetOut.myAntList.addAll(group.getAntList());
    }
    return packetOut;
  }




  /**
   * @param args Array of command-line arguments (See usage()).
   */
  public static void main(String[] args)
  {
    String serverHost = "localhost";
    boolean reconnection = false;
    if (args.length > 0) serverHost = args[args.length - 1];

    //TeamNameEnum team = TeamNameEnum.RandomWalkers;
    TeamNameEnum team = TeamNameEnum.Army;
    if (args.length > 1)
    {
      team = TeamNameEnum.getTeamByString(args[0]);
    }

    new ArmyAntClient(serverHost, team, reconnection);
  }

}
