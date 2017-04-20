package antworld.common;

public enum TeamNameEnum
{
  RandomWalkers,
  Army,
  Bullet,
  Carpenter,
  Fire,
  Formica,
  Harvester,
  Pharaoh,
  Weaver,
  SimpleSolid_0,
  SimpleSolid_1,
  SimpleSolid_2,
  SimpleSolid_3,
  SimpleSolid_4,
  SimpleSolid_5,
  SimpleSolid_6,
  SimpleSolid_7,
  SimpleSolid_8,
  SimpleSolid_9,
  SimpleSolid_10,
  SimpleSolid_11;

  public static TeamNameEnum getTeamByString(String name)
  {
    for(TeamNameEnum team : values())
    {
      if( name.equals(team.name()))
      {
        return team;
      }
    }
    return null;
  }
}
