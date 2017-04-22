package antworld.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import antworld.common.Constants;
import antworld.common.NestNameEnum;
import antworld.common.PacketToServer;
import antworld.server.Nest.NestStatus;

public class Server extends Thread
{

  /**
   * If the server does not receive a PacketToServer from a particular client
   * in greater than TIMEOUT_CLIENT_TO_UNDERGROUND seconds, then the client's ants
   * are sent back to its nest. <br><br>
   * Note: this is a few minutes longer than SOCKET_READ_TIMEOUT. Thus, a client that
   * loses a connection due to timeout, can reconnect within a few minutes to find
   * the ants still in the world with NOOP commands assumed during the time when
   * no client directions were received.
   */
  public static final double TIMEOUT_CLIENT_TO_UNDERGROUND = 60*5;


  /**
   * SOCKET_READ_TIMEOUT specifies the maximum time in milliseconds that a read()
   * call on the InputStream associated with the Socket will block. <br>
   * If the timeout expires, a java.net.SocketTimeoutException is raised,
   * though the Socket is still valid. If this exception is raised,
   * the server will send an error message to that client and close the socket.
   */
  public static final int SOCKET_READ_TIMEOUT = 60*2*1000;

  /**
   * The maximum queue length for incoming connection indications
   * (a request to connect) is set to the backlog parameter.
   * If a connection indication arrives when the queue is
   * full, the connection is refused.
   */
  private static final int SERVER_SOCKET_BACKLOG = 10;
  private long timeStartOfGameNano;
  
  private ServerSocket serverSocket = null;
  private ArrayList<Nest> nestList;
  private AntWorld world;


  public Server(AntWorld world, ArrayList<Nest> nestList)
  {
    timeStartOfGameNano = System.nanoTime();
    this.world = world;
    this.nestList = nestList;
    //System.out.println("Server: Opening socket to listen for client connections....");
    try
    {
      serverSocket = new ServerSocket(Constants.PORT, SERVER_SOCKET_BACKLOG);
    }
    catch (Exception e)
    {
      //System.err.println("Server: ***ERROR***: Opening socket failed.");
      //e.printStackTrace();
      System.exit(-1);
    }
    //System.out.println("Server: socket opened on port "+Constants.PORT);
  }
  
  
  public void run()
  {
    while (true)
    {
      Socket clientSocket = null;
      CommToClient client = null;
      System.out.println("Server: waiting for client connection.....");
      try
      {
        clientSocket = serverSocket.accept();
        clientSocket.setSoTimeout(SOCKET_READ_TIMEOUT);

        System.out.println("Server: Client attempting to connect.....");
        client = new CommToClient(this, clientSocket);
        client.start();
      }
      catch (Exception e)
      {
        String msg = "Server ***ERROR***: Failed to connect to client.";
        if (client != null) client.closeSocket(msg);
      }
    }
  }


  /**
   * ContinuousTime is the time in seconds from the start of the game to the current moment.
   * By contrast, getGameTime returns the time to the current game tick.
   * @return time in seconds
   */
  public double getContinuousTime()
  {
    //System.out.println("Server.getContinuousTime(): timeStartOfGameNano = " + timeStartOfGameNano);
    //System.out.println("                            System.nanoTime() = " + System.nanoTime());
    return (System.nanoTime() - timeStartOfGameNano)*Constants.NANO;
  }


  /*
  public int getNestIdxOfTeam(TeamNameEnum team)
  {
    if (team == null) return Nest.INVALID_NEST_ID;

    for (int i=0; i < nestList.size(); i++)
    {
      Nest myNest = nestList.get(i);
      if (myNest.team == team) return i;
    }

   return Nest.INVALID_NEST_ID;
  }
  */

  public double getGameTime() {return world.getGameTime();}
  public int getGameTick() {return world.getGameTick();}
  
  public Nest getNest(NestNameEnum nestName)
  {
    int nestIdx = nestName.ordinal();
   
    return nestList.get(nestIdx);
  }

  
  public synchronized Nest assignNest(CommToClient client, PacketToServer packetIn)
  {
    if (packetIn.myTeam == null) return null;

    Nest assignedNest = world.getNest(packetIn.myTeam);

    if (assignedNest != null)
    {
      if (assignedNest.getStatus() == NestStatus.CONNECTED)
      {
        String msg = "Already connected: team="+packetIn.myTeam + " to nest "+ assignedNest.nestName;
        client.setErrorMsg(msg);
        return null;
      }
      System.out.println("Server() Reconnecting " + packetIn.myTeam + " to nest " + assignedNest.nestName);
      assignedNest.setClient(client, packetIn);
      return assignedNest;
    }

    int largestMinDistance = 0;
    ArrayList<FoodSpawnSite> foodSpawnSites = world.getFoodSpawnList();
    for (Nest nest : nestList)
    {
      if (nest.team != null) continue;
      int minDistance = Integer.MAX_VALUE;
      for (FoodSpawnSite spawnSite : foodSpawnSites)
      {
        int dx = nest.centerX - spawnSite.getLocationX();
        int dy = nest.centerY - spawnSite.getLocationY();
        int distance = Math.abs(dx) + Math.abs(dy);
        if (distance < minDistance) minDistance = distance;
      }

      if (minDistance > largestMinDistance)
      {
        largestMinDistance = minDistance;
        assignedNest = nest;
      }
    }

    assignedNest.setClient(client, packetIn);

    return assignedNest;
  }
  

  /*
  public void closeClient(NestNameEnum nestName)
  {
    int nestIdx = nestName.ordinal();
    Nest nest = nestList.get(nestIdx);
    if (nest.getNetworkStatus() == NetworkStatus.CONNECTED)
    { nest.setNetworkStatus(NetworkStatus.DISCONNECTED);
    }
    if (clientConnectionList[nestIdx] != null) 
    { 
      try { clientConnectionList[nestIdx].closeSocket("Server.closeClient("+nestName+"): Disconnect"); } 
      catch (Exception e) { }
      clientConnectionList[nestIdx] = null;
    }
  }
  */

  /*
  private void closeClient(ServerToClientConnection myClientListener)
  {
    if (myClientListener != null)
    { 
      NestNameEnum nestName = myClientListener.getNestName();
      if (nestName != null)
      { 
    	    int nestIdx = nestName.ordinal();
    	    Nest nest = nestList.get(nestIdx);
    	    if (nest.getNetworkStatus() == NetworkStatus.CONNECTED)
    	    { nest.setNetworkStatus(NetworkStatus.DISCONNECTED);
    	    }  
    	  
        clientConnectionList[nestName.ordinal()] = null;
      }
      
      try { myClientListener.closeSocket("Server.closeClient() Disconnect"); } catch (Exception e) { }
    }
  }
  */
}
