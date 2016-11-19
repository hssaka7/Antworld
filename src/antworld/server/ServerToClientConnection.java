package antworld.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import antworld.common.CommData;
import antworld.common.NestNameEnum;
import antworld.common.TeamNameEnum;
import antworld.server.Nest.NetworkStatus;

public class ServerToClientConnection extends Thread
{

  
  
  private static final boolean DEBUG_SHOW_ALL_ERRORS = true;
  private static final boolean DEBUG_RECEIVE = true;
  private Server server = null;
  private Socket client = null;
  
  private ObjectInputStream clientReader = null;
  private ObjectOutputStream clientWriter = null;
  private NestNameEnum myNestName = null;
  private Nest myNest = null;
  private TeamNameEnum myTeam = null;
  private volatile CommData activeCommData;
  

 
  
  public ServerToClientConnection(Server server, Socket client)
  {
    this.server = server;
    this.client = client;
    
    
    try
    {
      clientReader = new ObjectInputStream(client.getInputStream());
      clientWriter = new ObjectOutputStream(client.getOutputStream());
    }
    catch (Exception e)
    {
      String msg = "ServerToClientConnection.openConnectionToClient():"+myNestName+":***ERROR***: Could not open com streams";
      System.out.println(msg);
      //server.log(msg);
    }
  }

  
  public static long getPassword(TeamNameEnum team)
  {
    return 1;
  }
  
  
 

  public void run()
  {
    
    establishNest();
    
    server.doneEstablishingConnection(this);
    
    if (client == null) return;
    
    //log = new Logger(myNestName.name());
    while (updateReveive());
  }
  
  
  public void establishNest()
  {
    try
    {
      CommData data = (CommData) clientReader.readObject();
      if (DEBUG_RECEIVE) System.out.println("ServerToClientConnection[EstablishNest]: received common="+data);
      
      activeCommData = new CommData(data.myTeam);
       
      if (data.myTeam == null)
      { 
        activeCommData.errorMsg = "myTeam == null)";
        closeSocket(activeCommData.errorMsg);
        return;
      }

      //Password checking since the tourney will be live
      //if ((data.password < 0) || (data.password != getPassword(data.myTeam)))
      //{
      // System.err.println("ServerToClientConnection() Missmatch in Teamname : password="+ data.password);
      //  activeCommData.errorMsg = "Missmatch in Teamname : password";
      //  activeCommData.myNest = null;
      //  send(activeCommData);
      //  closeSocket(activeCommData.errorMsg);
      //  return;
      //}

      this.myTeam = data.myTeam;
      
      
      myNest = server.assignNest(this);
      myNestName = myNest.nestName;
      
      if (myNest == null)
      {
        activeCommData.myNest = null;
        send(activeCommData);
        closeSocket(activeCommData.errorMsg);
        return;
      }
      


      activeCommData.requestNestData = true;
  
      myNest.setNetworkStatus(NetworkStatus.CONNECTED);
      myNest.receivedMessageFromClient();
      
      System.out.println("Server: Client Accepted: nest="+myNestName+", team="+myTeam);
      return;
    }
    catch (IOException e)
    {
      String msg = "ServerToClientConnection***ERROR***: client has disconnected";
      System.err.println(msg);
      closeSocket(msg);
    }
    catch (Exception e)
    {
      String msg = "ServerToClientConnection***ERROR***: client read failed: " + e.getMessage();
      e.printStackTrace();
      closeSocket(msg);
    }
  }
  
  public boolean updateReveive()
  {
    try
    {
      CommData data = (CommData) clientReader.readObject();
      
      myNest.receivedMessageFromClient(); 
     
      //String msg = "ServerToClientConnection["+myNestName+"]: received common: <<<<<<======\n"+common;
      //System.out.println(msg);
      //log.write(msg);
      
      setCommDataFromClient(data);
    }
    catch (java.net.SocketTimeoutException e)
    {
      String msg = "ServerToClientConnection***ERROR***: client timeout on read: ";
      
      e.printStackTrace();
      closeSocket(msg);
      return false;
    }
    catch (Exception e)
    {
      String msg = "ServerToClientConnection***ERROR***: client read failed: " + e.getMessage();
      
      e.printStackTrace();
      closeSocket(msg);
      return false;
    }
    return true;
  }
  
  

  
  
  public TeamNameEnum getTeamName() {return myTeam;}
  public NestNameEnum getNestName() {return myNestName;}
  public Nest getNest() {return myNest;}


  
  private void setCommDataFromClient(CommData data)
  { 
    synchronized (this)
    {
      if (data == null) return;
      activeCommData.errorMsg = null;
      
      if (data.gameTick != (AntWorld.getGameTick()+1))
      {
        activeCommData.errorMsg = "!!REJECT!! ["+myNestName + ":" + myTeam +"]: received gameTick="+data.gameTick + " expected="+(AntWorld.getGameTick()+1);
      }
      
      else if (data.myNest != myNestName)
      {
        activeCommData.errorMsg = "!!REJECT!!: nest missmatch:="+data.myNest + " <-> "+myNestName;
      }
      
      else if (data.myTeam != myTeam)
      {
        activeCommData.errorMsg = "!!REJECT!!: team missmatch:="+data.myTeam + " <-> "+myTeam;
      }
      
      if (activeCommData.errorMsg == null)
      { //System.out.println("ServerToClientConnection.setCommDataFromClient(): **ACCEPT**:");
        activeCommData = data;
      }
      else
      { if (DEBUG_SHOW_ALL_ERRORS) System.err.println("ServerToClientConnection.setCommDataFromClient():" + activeCommData.errorMsg);
      }
    }
  }
  
  
  
  public CommData getCommData() 
  { synchronized (this)
    {
      return activeCommData;
    }
  }
  
  public void send(CommData data)
  {
	
    if (data.myTeam == TeamNameEnum.EMPTY) return;
    CommData sendData = Server.deepCopyCommData(data);

    try
    {
      
      if (myNest.getNetworkStatus() != NetworkStatus.CONNECTED) closeSocket("NOT CONNECTED");
      
      activeCommData = data;
      //System.out.println("ServerToClientConnection.send[" + common.myTeam + "] START bound=" + client.isBound());
      clientWriter.writeObject(sendData);
      clientWriter.flush();
      clientWriter.reset();
      //System.out.println("ServerToClientConnection.send[" + common.myTeam + "] DONE");
      
      activeCommData = sendData;
    }
    catch (Exception e) 
    {
      closeSocket("ServerToClientConnection.send()" + e.getMessage());
    }
    
  }
  
  
  
  public void closeSocket(String msg)
  {
	if (myNest == null)
    { System.err.println(msg + " CloseSocket(NO NEST ASSIGNED)");
    }
	else
    { 
      myNest.setNetworkStatus(NetworkStatus.DISCONNECTED);
      System.err.println(msg + " CloseSocket("+myNest.nestName+")");
    }
    try
    {
      client.close();
    }
    catch (Exception e) {}

    client = null;
    
    server.doneEstablishingConnection(this);
  }
}
