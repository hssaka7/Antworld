package antworld.client;

import antworld.common.AntData;

/**
 * Created by hiManshu on 5/8/2017.
 */
public abstract class Ants {

    public abstract AntData getAnt();
    public abstract void updateAnt (AntData ant);
    public abstract void update();
    public abstract void setGoal(int x, int y);
}
