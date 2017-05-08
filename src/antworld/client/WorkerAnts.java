package antworld.client;

import antworld.common.AntData;
import antworld.common.AntType;
import antworld.common.Direction;
import antworld.common.TeamNameEnum;

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

    @Override
    public void update() {

    }

    @Override
    public void setGoal(int x, int y) {

    }
}
