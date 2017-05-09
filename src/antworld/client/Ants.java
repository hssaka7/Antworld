package antworld.client;

import antworld.common.AntData;

import java.util.ArrayList;

/**
 * Created by hiManshu on 5/8/2017.
 */
public abstract class Ants {

    public abstract AntData getAnt();
    public abstract void updateAnt (AntData ant);
    public abstract void update();
    public abstract void setGoal(int x, int y);
    public abstract void setGoal(PathNode goal) ;
    public abstract void setPath(ArrayList<PathNode> path);
    public abstract void setReturnPath(ArrayList<PathNode> returnPath);



}
