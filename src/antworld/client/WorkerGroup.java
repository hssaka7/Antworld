package antworld.client;

import antworld.common.AntAction;
import antworld.common.AntData;
import antworld.common.AntType;
import antworld.common.TeamNameEnum;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hiManshu on 5/8/2017.
 */
public class WorkerGroup {

    private final int spawnX;
    private final int spawnY;
    private final PathFinder pathFinder;
    private int count;
    private int spawnCount = 1;
    private HashMap<Integer, Ants> ants= new HashMap<>();

    private ArrayList<PathNode> path = new ArrayList<>();
    private ArrayList<PathNode> returnPath = new ArrayList<>();

    PathNode start ;
    PathNode goal ;


    public WorkerGroup(TeamNameEnum myTeam, PathFinder pathFinder, int spawnX, int spawnY)
    {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.pathFinder = pathFinder;
        count = 3;
        for (int i = 0; i < count; i++)
        {
            System.out.println("Adding Worker Ants");
            Ants tempData = new WorkerAnts(pathFinder,spawnX,spawnY,myTeam);
            ants.put(-(i*1),tempData);
            spawnCount++;
        }

    }

    ArrayList<Ants> getAntsList(){
        System.out.println("Returning Ants");
        return new ArrayList<>(ants.values());
    }

    ArrayList<AntData> getAntList(){

        ArrayList<AntData> toReturn = new ArrayList<>();
        for (Ants ant : ants.values()){
            System.out.println("Adding Worker Ant");
            toReturn.add(ant.getAnt());
        }
        return toReturn;
    }

    void updateAnt (AntData ant){
        if (ant.state == AntAction.AntState.UNDERGROUND && !ants.containsKey(ant.id)){
            if (spawnCount>-1) return;
            ants.put(ant.id,ants.get(spawnCount*-1));
            spawnCount--;
            return;
        }
        ants.get(ant.id).updateAnt(ant);
    }

    void chooseAction (){
        for (Ants ant: ants.values()){
             ant.update();
        }
    }

    void setGoal (int x, int y) {
        this. goal = new PathNode(x,y);
        this.findPath();
    }

    public void findPath()
    {
        System.out.println("Finding Path ----------------------------------------------");
        start = new PathNode(spawnX,spawnY);
        if (true)
        {
            System.out.println("Start: ------------" + start.getX() + " " + start.getY());
            System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());
        }
        path = pathFinder.getPath(start, goal);
        ArrayList<PathNode> dont = new ArrayList<>();
        dont.addAll(path);
        dont.remove(start);
        dont.remove(goal);
        // required path to Retrun
        returnPath = pathFinder.getPathSelective(dont.get(dont.size()-1),start,dont);

        if (true)
        {
            System.out.println("Start: ------------" + start.getX() + " " + start.getY());
            System.out.println(path);
            System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());

            System.out.println("--------------------" + start.getX() + " " + start.getY());

            System.out.println(returnPath);

            System.out.println("Goal: ------------" + goal.getX() + " " + goal.getY());
        }
    }




}
