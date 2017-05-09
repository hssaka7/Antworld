package antworld.client;

import antworld.common.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class WorkerAnts extends Ants{

    private boolean debug = true;
    private int spawnX;
    private int spawnY;

    private ArrayList<PathNode> path = new ArrayList<>();
    private ArrayList<PathNode> returnPath = new ArrayList<>();

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
    private AntBehaviors previousBehaviour;
    private int stuck;

    public WorkerAnts (PathFinder pathFinder, int spawnX, int spawnY, TeamNameEnum myTeam){
        this.ant = new AntData(AntType.WORKER,myTeam);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.pathFinder = pathFinder;
        antBehavior = AntBehaviors.TOSPAWN;
        dir = Direction.getRandomDir();

    }
    @Override
    public AntData getAnt() {
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
            System.out.println("Updating AntPathStep");
            pathStep++;
        }
        else
        {
            ant.action.type = AntAction.AntActionType.MOVE;
            ant.action.direction = dir;
        }


    }

    @Override
    public void updateAnt(AntData ant)
    {
        {
            System.out.println("Updating Ant in Ants");
            if (ant.state == AntAction.AntState.OUT_AND_ABOUT) {
                if (this.ant.gridX == ant.gridX && this.ant.gridY == ant.gridY) {
                    moved = false;
                    stuck++;
                    if (stuck > 20) {
                        antBehavior = AntBehaviors.EXPLORE;
                        dir = Direction.getRandomDir();
                        stuck = 0;
                        return;
                    }
                } else {
                    moved = true;
                    stuck = 0;
                    switch (antBehavior) {
                        case GATHER:
                        case RETURN: {
                            System.out.println("Updating AntPathStep");
                            pathStep++;
                            break;
                        }
                    }
                }


            }
            this.ant = ant;
            checkForWater++;
            updateAntBehaviour();
        }
    }

    public void update (){
        if (debug) System.out.println("Updating Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);
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

                if (ant.state == AntAction.AntState.UNDERGROUND) {
                    {
                        if (ant.carryUnits != 0) {
                            ant.action.type = AntAction.AntActionType.DROP;
                            ant.action.quantity = AntType.WORKER.getCarryCapacity();
                        } else {
                            ant.action.type = AntAction.AntActionType.EXIT_NEST;
                            ant.action.x = spawnX;
                            ant.action.y = spawnY;
                        }
                    }
                }
                else
                {
                    ant.action.type = AntAction.AntActionType.ENTER_NEST;
                }
                break;

            }
            case PICKUPWATER:
            {
                if (this.path == null) {
                    antBehavior = AntBehaviors.EXPLORE;
                }
                else {
                    antBehavior = AntBehaviors.GOTO;
                }
                break;
            }
        }
    }

    void updateAntBehaviour (){
        if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);
        switch(antBehavior) {
            case GATHER:
            {
                if (pathStep == path.size()-1){
                    antBehavior = AntBehaviors.PICKUP;
                    pathStep = 1;
                    if (debug) System.out.println("Ant behavior changed from GATHER TO: " + antBehavior );
                }
                break;
            }
            case RETURN:
            {
                if (pathStep == returnPath.size()){
                    antBehavior = AntBehaviors.DROP;
                    pathStep = 1;
                    if (debug) System.out.println("Ant behavior changed from RETURN TO: " + antBehavior );
                }
                break;
            }
            case PICKUP:
            {
                antBehavior = AntBehaviors.RETURN;
                if (debug) System.out.println("Ant behavior changed from PICKUP TO: " + antBehavior );
                break;
            }
            case DROP:
            {
                if (ant.state!= AntAction.AntState.UNDERGROUND) {
                    antBehavior = AntBehaviors.GATHER;
                    if (debug) System.out.println("Ant behavior changed from DROP TO: " + antBehavior);
                }
                break;
            }

            case TOSPAWN:
            {
                if (ant.state == AntAction.AntState.OUT_AND_ABOUT){
                    if (path != null || path.size() > 0){
                        antBehavior = AntBehaviors.GATHER;
                        pathStep = 1;
                        return;
                    }
                    else{
                        antBehavior = AntBehaviors.EXPLORE;
                    }

                }
                break;
            }
            case PICKUPWATER:
            {
                if (ant.health < 20) {
                    antBehavior = AntBehaviors.HEAL;
                }
                else{
                    antBehavior = previousBehaviour;
                }
                break;
            }
            case HEAL:
            {
                if (healUnits>1 && ant.health < ant.antType.getMaxHealth()-3){
                    healUnits--;
                }
                else {
                    antBehavior = previousBehaviour;
                    healUnits = 0;
                    startedToheal = false;
                }
                break;
            }
        }
    }

    boolean checkCriticalConditions(){
        if (antBehavior == AntBehaviors.PICKUPWATER || antBehavior == AntBehaviors.HEAL) return false;
        if (ant.health < 10 && ant.carryUnits > 0){
            previousBehaviour = antBehavior;
            antBehavior = AntBehaviors.HEAL;
            return true;
        }
        if (checkForWater < 50) return false;
        if (pathFinder.getDirectionToWater(ant.gridX,ant.gridY)!=null){
            if (ant.carryUnits < 3){
                previousBehaviour = antBehavior;
                antBehavior = AntBehaviors.PICKUPWATER;
                checkForWater = 0;
            }
            return true;
        }
        return false;
    }

    @Override
    public void setGoal(int x, int y) {

    }

    public void setGoal(PathNode goal) {
        this.goal= goal;
    }

    public void setPath(ArrayList<PathNode> path){
        this.path = path;
    }

    public void setReturnPath(ArrayList<PathNode> returnPath){
        this.returnPath = returnPath;
    }
}
