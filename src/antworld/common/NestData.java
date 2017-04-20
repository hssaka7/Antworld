package antworld.common;

import java.io.Serializable;

public class NestData implements Serializable 
{
  private static final long serialVersionUID = Constants.VERSION;

  public final NestNameEnum nestName;
  public final int centerX, centerY;
  public TeamNameEnum team;

  public int foodInNest  = Constants.INITIAL_FOOD_UNITS;
  public int waterInNest = Constants.INITIAL_NEST_WATER_UNITS;

  /**
   * A team's score equals the sum of:
   * <ol>
   *   <li>Food units in the nest.</li>
   *   <li>For each living ant, AntType.getScore(). Note food being carried by an
   *   ant is not counted towards a team's score.</li>
   * </ol>
   */
  public int score;
  
  public NestData(NestNameEnum nestName, TeamNameEnum team, int x, int y)
  { 
    this.nestName = nestName;
    this.team = team;
    this.centerX = x;
    this.centerY = y;
  }

  public NestData(NestData source)
  {
    nestName = source.nestName;
    team = source.team;
    centerX = source.centerX;
    centerY = source.centerY;

    foodInNest  = source.foodInNest;
    waterInNest = source.waterInNest ;
    score = source.score;
  }


  
  public String toString()
  {
    String out = "Nest=["+nestName+", team="+team+", center=("+centerX+", "+centerY+ ")";

    if (team != null)
    { out += " food="+foodInNest+ "/"+ waterInNest + ", score="+score+"]";
    }
    return out;
  }
}
