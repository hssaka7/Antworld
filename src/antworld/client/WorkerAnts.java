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
        {
            if (ant.state == AntAction.AntState.OUT_AND_ABOUT) {
                if (this.ant.gridX == ant.gridX && this.ant.gridY== ant.gridY){
                    moved = false;
                    stuck++;
                    if (stuck > 20){
                        antBehavior = AntBehaviors.EXPLORE;
                        dir = Direction.getRandomDir();
                        stuck = 0;
                    }
                }
                else {
                    moved = true;
                    stuck = 0;
                    switch (antBehavior){
                        case GOTO:
                        {
                            System.out.println("Updating AntPathStep");
                            pathStep++;
                            break;
                        }
                    }
                }


            }
            this.ant = ant;
            checkForWater++;
            if (!checkCriticalConditions()) updateAntBehaviour();
        }

    }

    public void update (){
        if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);
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

    void updateAntBehaviour (){
        if (debug) System.out.println("Current Ant behavior: " + antBehavior + " With PathStep " + pathStep);

        switch(antBehavior) {
            case TOSPAWN:
            {
                if (ant.state == AntAction.AntState.OUT_AND_ABOUT){
                    if (path != null || path.size() > 0){
                        antBehavior = AntBehaviors.GOTO;
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
}
