package ml.engine;
/**
 * The Enumeration Action that defines the possible actions.
 */
public class Action {
	
	/** First elevator is doing the action. */
	public static final int E1_UP=0;
	/** First elevator is doing the action. */
	public static final int E1_DOWN=1;
	/** First elevator is doing the action. */
	public static final int E1_STOP=2;
	/** Second elevator is doing the action. */
	public static final int E2_UP=0;
	/** Second elevator is doing the action. */
	public static final int E2_DOWN=3;
	/** Second elevator is doing the action. */
	public static final int E2_STOP=6;	
	/** The Constant NO_ACTION. */
	public static final int NO_ACTION=-1;
	
	/**
	 * Gets the e1 action.
	 *
	 * @param val the val
	 * @return the e1 action
	 */
	public static int getE1Action(int val)
	{
		return val%3;
	}
	
	/**
	 * Gets the e2 action.
	 *
	 * @param val the val
	 * @return the e2 action
	 */
	public static int getE2Action(int val)
	{
		return val-val%3;
	}
	
	
//	
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_UP_E2_UP(1),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_UP_E2_STOP(2),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_UP_E2_DOWN(3),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_STOP_E2_UP(4),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_STOP_E2_STOP(5),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_STOP_E2_DOWN(6),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_DOWN_E2_UP(7),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_DOWN_E2_STOP(8),
//	/** First elevator is doing first action and second elevator is doing second action. */
//	E1_DOWN_E2_DOWN(9),	
//	/** First elevator is doing the action. */
//	E1_UP(10),
//	/** First elevator is doing the action. */
//	E1_DOWN(11),
//	/** First elevator is doing the action. */
//	E1_STOP(12),
//	/** Second elevator is doing the action. */
//	E2_UP(13),
//	/** Second elevator is doing the action. */
//	E2_DOWN(14),
//	/** Second elevator is doing the action. */
//	E2_STOP(15);
//	
//	public static Action fromID(int id) {
//		switch(id)
//		{
//		case 1: return E1_UP_E2_UP;
//
//		}
//		return null;
//	}
//
//
//
//	/** The id of the action.  */
//	private final int id;
//
//	/**
//	 * Gets the id.
//	 *
//	 * @return the id
//	 */
//	public int getID() {
//		return id;
//	}
//
//	/**
//	 * Instantiates a new action, with an id.
//	 *
//	 * @param id the id
//	 */
//	Action(int id) {
//		this.id = id;
//	}

}
