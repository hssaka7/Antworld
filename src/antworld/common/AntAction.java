package antworld.common;

import java.io.Serializable;

/**
 * Each AntData contain a reference to an AntAction instance.<br><br>
 *
 * The client tells the server what its ants request to do in the next tick
 * by setting each ant's action to an AntActionType and by setting
 * the parameters required by that action type.<br><br>
 *
 * The following update form the server will include each of the client's
 * ants with each ant's action types unchanged if that action was successful or
 * set to AntActionType.BUSY if the action failed. <br><br>
 *
 * If an ant takes an action that requires more than one tick (such as movement),
 * the action executes on the tick that it is initiated. Then, on all remaining turns
 * required by that action, the ant will take the action AntActionType.BUSY.
 */


public class AntAction implements Serializable
{
  private static final long serialVersionUID = Constants.VERSION;
  public enum AntState {UNDERGROUND, OUT_AND_ABOUT, DEAD};

  public enum AntActionType 
  {
    /**
     * This action requires setting the field: <tt>direction</tt>.<br><br>
     * When set by the client, this is a request to move the ant in the
     * specified direction.<br>
     * When set by the server, it is a statement of where the ant moved on
     * the preceding tick.<br><br>
     * In most cases, the MOVE action takes more than one turn to complete.
     * Use the methods AntType.getBaseMovementTicksPerCell(),  AntType.getUpHillMultiplier()
     * and AntType.getEncumbranceMultiplier() to determine the movement cost.<br>
     * Regardless of the ticks required, the move action takes place on the turn
     * the command is given. All additional turns required by the move action will
     * be AntActionType.BUSY
     */
    MOVE,

    /**
     * This action requires setting the field: <tt>direction</tt>.<br><br>
     * The ATTACK action requires in one tick. <br>
     * When set by the client, this is a request for the ant to attack an adjacent
     * ant in the specified direction.<br>
     * When set by the server, this is statement that the ant attacked the ant
     * in the specified direction.<br><br>
     * An ant that is underground cannot attack or be attacked.
     */
    ATTACK,

    /**
     * This action requires setting the fields: <tt>direction</tt> and
     * <tt>quantity</tt>.<br><br>
     * When set by the client, if <tt>quantity</tt> exceeds either the
     * ant's carrying capacity or the amount of food/water in the adjacent cell
     * in the target specified direction, the command partly fails and the ant
     * picks up as much as it can from the specified cell.<br>
     * When set by the server, <tt>quantity</tt> is the number of units actually
     * picked up by the ant on and preceding tick.
     */
    PICKUP,


    /**
     * This action requires setting the fields: <tt>direction</tt> and
     * <tt>quantity</tt>.<br><br>
     * When set by the client, if <tt>quantity</tt> exceeds the amount of
     * food/water the ant is carrying, the command partly fails and the ant
     * drops all its food in the specified cell.<br>
     * When set by the server, <tt>quantity</tt> is the number of units actually
     * dropped by the ant on and preceding tick.<br><br>
     *
     * The PICKUP action requires in one tick. However,
     * during a given tick, ant actions of a nest are executed in the order
     * the client places them in the list.
     * Therefore, if two ants are are separated by an empty cell, then
     * during a single tick, one ant can DROP food in the empty cell and the other
     * can pick it up. This is a very efficient method of transporting a heavy load
     * of food up hill, moving the food in one tick what would have taken a single
     * ant 16 ticks to carry.
     */
    DROP,



    /**
     * If the ant is above ground, then this action requires setting
     * the field: <tt>direction</tt>.<br>
     * If the ant is underground it can only heal itself and <tt>direction</tt>
     * is ignored.<br><br>
     *
     *
     */
    HEAL,       // HEAL direction (must be medic ant) | HEAL (must be underground)
    ENTER_NEST, // ENTER_NEST (must be on home nest area)
    EXIT_NEST,  // EXIT_NEST x y (must be underground and x,y must be in home nest area)
    BIRTH,      // Client adds new ant to antlist, sets ant type. Server deducts needed food from nest store.
    DIED,       //
    BUSY,        //
    NOOP      // STASIS
  }; 

  public AntActionType type;
  
  /** One of the 8 possible directions. The AntActionTypes requiring Direction 
   * are: MOVE, ATTACK, PICKUP, DROP, HEAL (when executed by a medic ant outside of the nest)
   */
  public Direction direction;
  
  
  /** (x,y) specify absolute coordinates in the Ant World grid. 
   * These fields are only used by the EXIT_NEST action. 
   * An EXIT_NEST action is only valid when the specified coordinates are within
   * a Manhattan Distance of Constants.NEST_RADIUS from the ant's own nest center and,
   * of course, when the ant is underground.
   */
  public int x, y;
  
  
  /** Specifies a quantity needed by the PICKUP and DROP ant actions.
   */
  public int quantity;
  
  
  /** Simple constructor.
   */
  public AntAction(AntActionType type)
  {
    this.type = type;
  }
  
  
  /** Constructor used for an AntActionType that requires a direction.
   */
  public AntAction(AntActionType type, Direction dir)
  {
    this.type = type;
    this.direction = dir;
  }
  
  
  /** Constructor used for an AntActionType that requires direction and quantity.
   */
  public AntAction(AntActionType type, Direction dir, int quantity)
  {
    this.type = type;
    this.direction = dir;
    this.quantity = quantity;
  }
  
  
  /** Constructor used for an AntActionType that requires ant world map coordinates.
   */
  public AntAction(AntActionType type, int x, int y)
  {
    this.type = type;
    this.x = x;
    this.y = y;
  }
  
  
  /** Constructor that returns a deep copy of an AntAction object. It happens
   *  that all elements of AntAction are enums or primitive types. Thus, a 
   *  "deep copy" is actually the same as a copy; however, if this class were
   *  ever to be expanded to include object instances, then this constructor 
   *  would deep copy those objects.
   */
  public AntAction(AntAction source)
  {
    type = source.type;
    direction = source.direction;
    x = source.x;
    y = source.y;
    quantity = source.quantity;
  }
  
  
  /** Deep copies common in source into this.
   */
  public void copy(AntAction source)
  {
    type = source.type;
    direction = source.direction;
    x = source.x;
    y = source.y;
    quantity = source.quantity;
  }
  
  
  /** Used for debugging, this method returns a formatted string of this action
   * and the field values required by the value of type.
   */
  public String toString()
  {
    String out = "AntAction: ["+type;
    if (type == AntActionType.MOVE) out += ", " + direction +"]";
    else if (type == AntActionType.ATTACK) out += ", " + direction +"]";
    else if (type == AntActionType.PICKUP) out += ", " + direction +" "+quantity+"]";
    else if (type == AntActionType.DROP) out += ", " + direction +" "+quantity+"]";
    else if (type == AntActionType.HEAL)
    {
      if (direction != null) out += ", " + direction +"]";
    }
    else if (type == AntActionType.ENTER_NEST) out += "]";
    else if (type == AntActionType.EXIT_NEST) out += "("+x + ", " + y + ")]";
    else if (type == AntActionType.BUSY) out += ", " +quantity+"]";
    else if (type == AntActionType.NOOP) out += "]";
    
    return out;
  }
}
