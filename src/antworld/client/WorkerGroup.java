package antworld.client;

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
    private HashMap<Integer, Ants> ants= new HashMap<>();

    public WorkerGroup(TeamNameEnum myTeam, PathFinder pathFinder, int spawnX, int spawnY)
    {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.pathFinder = pathFinder;
        count = 3;
        for (int i = 0; i < count; i++)
        {
            Ants tempData = new WorkerAnts(pathFinder,spawnX,spawnY,myTeam);
            ants.put(-i,tempData);
        }

    }

    ArrayList<Ants> getAntsList(){
        return new ArrayList<>(ants.values());
    }

    ArrayList<AntData> getAntList(){
        ArrayList<AntData> toReturn = new ArrayList<>();
        for (Ants ant : ants.values()){
            toReturn.add(ant.getAnt());
        }
        return toReturn;
    }

    void updateAnt (AntData ant){


    }

    void chooseAction (){

    }



}
