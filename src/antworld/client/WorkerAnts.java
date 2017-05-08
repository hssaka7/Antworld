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
        return null;
    }

    @Override
    public void updateAnt(AntData ant) {

    }

    public void update (){
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
                if (pathStep == returnPath.size()-1){
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

    @Override
    public void setGoal(int x, int y) {

    }
}
