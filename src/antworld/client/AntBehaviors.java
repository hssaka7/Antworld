package antworld.client;

//*************************************
// Kirtus Leyba
// Enum of possible ant behaviors
//*************************************
/**
 * Created by Kirtus Leyba on 5/2/2017.
 * This is an enum of possible ant behaviors.
 */
public enum AntBehaviors
{
  DEFAULT, GATHER, GOTO, PATROL, EXPLORE,HEAL, RETURN, PICKUP, DROP, BESTRANDOM, CHOOSERANDOM, EXPLORE2, PICKUPWATER, GOTOTEMP,TOSPAWN,UNDERGROUND,

  /**
   * EXPLODE_EXPLORE,
   * has 2 stages, goto point, then explode and explore randomly
   */
  EXPLODE_EXPLORE
  {
    public int getStages()
    {
      return 2;
    }
  };
}
