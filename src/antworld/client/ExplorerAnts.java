package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class ExplorerAnts extends Ants{
    private boolean debug = true;
    private int spawnX;
    private int spawnY;
    private ArrayList<PathNode> path = new ArrayList<>();
    private int pathStep;
    private AntData ant;
    private PathNode goal;
    private AntBehaviors antBehavior;
    static HashMap<Integer, HashMap<Integer, Integer>> visited = new HashMap<>();
    private ArrayList<PathNode> undiiscoveredNodes= new ArrayList<>();
    private Direction dir;
    private boolean moved = false;
    private PathFinder pathFinder;
    private int checkForWater = 0;
    private boolean startedToheal;
    private int healUnits;

    public ExplorerAnts (PathFinder pathFinder, int spawnX, int spawnY, TeamNameEnum myTeam){
        this.ant = new AntData(AntType.EXPLORER,myTeam);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.pathFinder = pathFinder;
        antBehavior = AntBehaviors.TOSPAWN;
         dir = Direction.getRandomDir();
    }

    public ArrayList<Direction> getPossibleDirections ()
    {
        Direction[] temp = new Direction[7];
        ArrayList<Direction> toReturn = new ArrayList<>();
        temp[0] = Direction.getRightDir(dir);
        for (int i = 1; i < 7; i++)
        {
            temp[i] = Direction.getRightDir(temp[i-1]);
        }
        int[] order = {1,5,2,4,6,0,3};

        for (int i: order)
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

    public ArrayList<PathNode> unSeenList (Direction nextDir)
    {
        ArrayList<PathNode> toReturn = new ArrayList<>();
        int radius = ant.antType.getVisionRadius();
        int deltaX = nextDir.deltaX();
        int deltaY = nextDir.deltaY();
        int currentX = ant.gridX+ deltaX;
        int currentY = ant.gridY + deltaY;
        int xmin = Math.max(1,currentX - radius);
        int ymin = Math.max(1,currentY - radius);
        int xmax = Math.min(2500-2,currentX + radius);
        int ymax = Math.min(1500-2, currentY + radius);
        int newPos= 0;

        if (deltaX!=0)
        {
            if (deltaX>0) newPos = xmax;
            else  newPos = xmin;
            for (int i = ymin; i<=ymax; i++)
            {
                if (isSeen(newPos,i)) toReturn.add(new PathNode(newPos,i));
            }
        }

        if (deltaY!=0)
        {
            if (deltaY>0) newPos = ymax;
            else  newPos = ymin;
            for (int i = xmin; i<=xmax; i++)
            {
                if (isSeen(i,newPos)) toReturn.add(new PathNode(i,newPos));
            }

        }
        if (false)
        {
            System.out.println("Unseen List for Current" + ant.gridX + "," + ant.gridY + "Direction " + nextDir.toString() + "new CoOrdinates" + currentX + " , " + currentY);
            for (PathNode path : toReturn)
            {
                System.out.println(path.getX() + "," + path.getY());
            }
        }
        return toReturn;
    }

    public ArrayList<PathNode> unSeenList (PathNode node)
    {
        ArrayList<PathNode> toReturn = new ArrayList<>();

        int radius = ant.antType.getVisionRadius();
        int currentX = node.getX();
        int currentY = node.getY();
        int xmin = Math.max(1,currentX - radius);
        int ymin = Math.max(1,currentY - radius);
        int xmax = Math.min(2500-2,currentX + radius);
        int ymax = Math.min(1500-2, currentY + radius);
        for (int i = ymin; i<=ymax; i+=(ymax-ymin))
        {
            for (int j = xmin; j <= xmax; j++)
            {
                if (isSeen(j, i)) toReturn.add(new PathNode(j, i));
            }
        }

        for (int i = xmin; i<=xmax; i+=(xmax-xmin))
        {
            for (int j = ymin; j <= ymax; j++)
            {
                if (isSeen(i, j)) toReturn.add(new PathNode(i, j));
            }
        }

        if (false)
        {
            System.out.println("Unseen List for Current" + ant.gridX + "," + ant.gridY + "at Pathnode " +  node.getX() + " , " + node.getY());
            for (PathNode path : toReturn)
            {
                System.out.println(path.getX() + "," + path.getY());
            }
        }

        return toReturn;
    }

    boolean isSeen (int x, int y){
        PathNode temp = new PathNode(x,y);
        if (!pathFinder.nodeLegal(temp)) return false;
        if (visited.containsKey(x)){
            if (!visited.get(x).containsKey(y)){
                return true;
            }
        }
        else{
            return true;
        }
        return false;
    }

    public int towardsNewPath(Direction nextDir)
    {
        return  (unSeenList(nextDir).size());
    }


    /**
     * This methods finds out the list of every possible direct an ant can move
     * For each direction, it checks the number of unseen nodes and returns the direction which has the highest number of unseen nodes
     *
     * If every direction has 0 unseen nodes, it travels to the last node in its way that that unseen nodes
     * else it returns a random direction
     * @return
     */

    public Direction getRandomAdjacentDir ()
    {
        Direction toReturn = null;
        int current = 0;
        int temp;
        ArrayList<Direction> possiblePath = getPossibleDirections();
        for (Direction nextDir : possiblePath)
        {
            temp = towardsNewPath(nextDir);
            if (debug) System.out.println("Moving towards" + nextDir.toString() + "has "+ temp +" new locations");
            if (temp > current)
            {
                if (toReturn != null)
                {
                    undiiscoveredNodes.add(getNextNode(toReturn));
                    if (debug) System.out.println("Adding to Undiscovered list : " + toReturn + "with pathnode "+ getNextNode(toReturn).getX() +" , "+ getNextNode((toReturn)).getY());
                }
                toReturn = nextDir;
                current = temp;
            }
            else
            {
                if (temp!=0)
                {
                    undiiscoveredNodes.add(getNextNode(nextDir));

                    if (debug)
                        System.out.println("Adding to Undiscovered list : " + nextDir.toString() + "with pathnode " + getNextNode(nextDir).getX() + " , " + getNextNode((nextDir)).getY());
                }
            }

        }

        if (toReturn == null)
        {
            if (debug) System.out.println("Trapped between visited Nodes ----------------------------------------------------------");
            if (undiiscoveredNodes.size() < 1)
            {
                if (debug) System.out.println("No Undiscoverable Nodes ---------------Getting Random Direction");
                return Direction.getRandomDir();
            }
            PathNode start = new PathNode(ant.gridX, ant.gridY);
            int last = undiiscoveredNodes.size()-1;
            PathNode goal = undiiscoveredNodes.get(last);
            while (unSeenList(goal).size()<1)
            {
                undiiscoveredNodes.remove(last);
                if (undiiscoveredNodes.size() < 1) return Direction.getRandomDir();
                last = undiiscoveredNodes.size()-1;
                goal = undiiscoveredNodes.get(last);
            }
            this.setPath(pathFinder.getPath(start,goal));
            undiiscoveredNodes.remove(last);
            pathStep = 1;
            if (debug) System.out.println("Moving Towards----------------------------------------------------------" + start.getX() +","+ start.getY() + "to" + goal.getX() + "," +goal.getY());
            antBehavior = AntBehaviors.GOTO;
            return start.getDirectionTo(path.get(1));
        }

        if (debug) System.out.println("Returining" + toReturn.toString());
        return  toReturn;
    }


    PathNode getLastNode (AntData ant){
        int x = ant.gridX  + (ant.antType.getVisionRadius()) * dir.deltaX();
        int y = ant.gridY + (ant.antType.getVisionRadius()) * dir.deltaY();
        System.out.println("Current Node : " + ant.gridX + "," + ant.gridY + " Final values at the end "+ x+"' "+ y);
        return  new PathNode(x,y);
    }

    PathNode getNextNode (Direction dir){
        int x = ant.gridX  + dir.deltaX();
        int y = ant.gridY + dir.deltaY();
        System.out.println("Current Node : " + ant.gridX + "," + ant.gridY + " Final values at the next"+ x+"' "+ y);
        return  new PathNode(x,y);
    }

    /**
     * checks to see if it is possible to move in a direction
     * @param dir
     * @return
     */
    boolean isPossible(Direction dir){
        PathNode temp = new PathNode(ant.gridX+dir.deltaX(), ant.gridY+dir.deltaY());
        return pathFinder.nodeLegal(temp);
    }

    public void setPath (ArrayList<PathNode>path)
    {
        this.path = path;
    }
    public AntData getAnt()
    {
        return ant;
    }

    void moveAnt(ArrayList<PathNode> path){
        if (path.size() < 1) {
            ant.action.type = AntAction.AntActionType.NOOP;
            return;
        }
        if (debug) System.out.println("AntPathStep : " + pathStep + "Path Size : " + path.size());
        int x = path.get(pathStep).getX();
        int y = path.get(pathStep).getY();
        PathNode antNode = new PathNode(ant.gridX, ant.gridY);
        PathNode destination = new PathNode(x, y);

        if (debug) System.out.println("start " + ant.gridX + "," + ant.gridY + " Goal" + destination.getX() + " " + destination.getY());
        dir = antNode.getDirectionTo(destination);
        if (dir == null) {
            System.out.println("RETURNING NOOP");
            ant.action.type = AntAction.AntActionType.MOVE;
            ant.action.direction = Direction.getRandomDir();
            antBehavior = AntBehaviors.EXPLORE;
            pathStep++;
        }
        else
        {
            ant.action.type = AntAction.AntActionType.MOVE;
            ant.action.direction = dir;
        }


    }

    public void update(){
        System.out.println("Updating Ant with ID :" + ant.id + " Behaviour " + antBehavior);
        switch(antBehavior) {
            case TOSPAWN:
            {
                if (ant.state == AntAction.AntState.UNDERGROUND){
                    ant.action.type = AntAction.AntActionType.EXIT_NEST;
                    ant.action.x = spawnX;
                    ant.action.y = spawnY;
                }
                break;
            }
            case EXPLORE:
            {
                if (!pathFinder.nodeLegal(getNextNode(dir))) {
                    System.out.print("Not possible to move ahead in " + dir.toString() + " at " + ant.gridX + "," + ant.gridY);
                    dir = getRandomAdjacentDir();
                    System.out.print("New Direction " + dir.toString());
                }
                ant.action.type = AntAction.AntActionType.MOVE;
                ant.action.direction = dir;
                break;
            }

            case CHOOSERANDOM:
            {
                System.out.println("Path reached and choosing Random dir to move");
                dir = getRandomAdjacentDir();
                ant.action.type = AntAction.AntActionType.MOVE;
                ant.action.direction = dir;
                break;
            }

            case PICKUPWATER:
            {
                dir = pathFinder.getDirectionToWater(ant.gridX,ant.gridY);
                if (dir == null) {
                    antBehavior = AntBehaviors.EXPLORE;
                    return;
                }
                ant.action.type = AntAction.AntActionType.PICKUP;
                ant.action.direction = dir;
                ant.action.quantity = ant.antType.getCarryCapacity();
                break;
            }

            case GOTO:
            {
                if (pathStep == path.size() || path.size()<0 || path== null) {
                    System.out.println("Path reached and choosing Random dir to move");
                    dir = getRandomAdjacentDir();
                    ant.action.type = AntAction.AntActionType.MOVE;
                    ant.action.direction = dir;
                    antBehavior = AntBehaviors.EXPLORE;
                    return;
                }
                else moveAnt(path);
                break;
            }
            case HEAL:
            {
                ant.action.type = AntAction.AntActionType.HEAL;
                if (!startedToheal) {
                    healUnits =  (ant.antType.getMaxHealth() / ant.health) * 2;
                    startedToheal = true;
                }
            }
        }
    }

    public void updateAnt(AntData ant)
    {
        if (ant.state == AntAction.AntState.OUT_AND_ABOUT) {
            if (this.ant.gridX == ant.gridX && this.ant.gridY== ant.gridY){
                moved = false;
            }
            else {
                moved = true;
                switch (antBehavior){
                    case GOTO:
                    {
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
    void updateAntBehaviour (){
        if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);

        switch(antBehavior) {
            case TOSPAWN:
            {
                if (ant.state == AntAction.AntState.OUT_AND_ABOUT){
                    antBehavior = AntBehaviors.EXPLORE;
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
                if (healUnits>1){
                    healUnits--;
                }
                else {

                    if (this.path == null || this.path.size() < 1) {
                        antBehavior = AntBehaviors.EXPLORE;
                    } else {
                        antBehavior = AntBehaviors.GOTO;
                    }
                    startedToheal = false;
                }
                break;
            }
        }
    }

    boolean checkCriticalConditions(){
        if (checkForWater < 50) return false;
        if (pathFinder.getDirectionToWater(ant.gridX,ant.gridY)!=null){
            if (ant.carryUnits < 3 && ant.health < 23){
                antBehavior = AntBehaviors.PICKUPWATER;
                checkForWater = 0;
            }
            return true;
        }
        return false;
    }
    private void updateVisited(AntData ant){
        int radius = ant.antType.getVisionRadius();
        int x = ant.gridX;
        int y = ant.gridY ;
        int xmin = Math.max(1,x - radius);
        int ymin = Math.max(1,y - radius);
        int xmax = Math.min(2500-2,x + radius);
        int ymax = Math.min(1500-2, y + radius);

        for (int i = ymin; i<=ymax; i++){
            for (int j = xmin; j <=xmax; j++){
                if (visited.containsKey(j)){
                    if(!visited.get(j).containsKey(i)) {
                        visited.get(j).put(i, 0);
                        if (false) System.out.println("New Y visited" + j + " , " + i);
                    }
                }
                else {
                    visited.put(j,new HashMap<>());
                    visited.get(j).put(ant.gridY,0);
                    if (false) System.out.println("New X visited" + j + " , " + i);
                }
            }
        }

    }





}
