package ml.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * The Class Engine.
 */
public class Engine {
	
	/** The Constant ACTION_EPSILON. */
	public static final double ACTION_EPSILON=0.4;
	
	/** A random number generator. */
	private static final Random rand=new Random();
	
	/** The Q. */
	private HashMap<State,Double[]> Q;
	
	/** The world. */
	private World world;
	
	/** The current state. */
	private State currentState;
	
	/** The time. */
	private int time;
	
	/** The previous action. */
	private Integer previousAction;
	
	/** The previous before previous action. */
	private Integer prevPreviousAction;
	
	/** The Constant log. */
	private static final Logger log=Logger.getLogger(Engine.class);
	
	/**
	 * Configures the logger.
	 */
	private void configureLogger()
	{
		PatternLayout patternLayout=new PatternLayout("%-3r [%-5p] %c - %m%n");
		ConsoleAppender appender=new ConsoleAppender(patternLayout);
		log.addAppender(appender);
		log.setLevel(Level.ALL);		
	}
	
	/**
	 * Run.
	 */
	public void run()
	{
		log.info("Engine started");	
		
		State newState;
		while(!world.isScenarioFinished())
		{
			//Next action
			time++;
			Integer action=getNextAction(currentState);
			log.debug(time+") Performing action: "+action);
			
			//Perform the action and get to the new state
			newState=world.getNextState(currentState, action);
			this.currentState=newState;
			log.debug("Now in state: "+currentState);
			log.debug("Reward: "+world.getRewardForCurrentState());
		}

		
		log.info("Engine finished");
	}
	
	/**
	 * Gets the next action. Uses epsilon-greedy.
	 *
	 * @return the next action
	 */
	public Integer getNextAction(State state)
	{
		ArrayList<Integer> possibleActions;
		possibleActions=world.getPossibleActions(currentState, previousAction, prevPreviousAction);
		
		//Explore - Pick a random action
		if(!Q.containsKey(state) || rand.nextDouble()<ACTION_EPSILON)
			return possibleActions.get(rand.nextInt(possibleActions.size()));
		//Exploit - Get the BEST action
		else
		{
			int maxAction=possibleActions.get(0);
			double maxQ=-Double.MAX_VALUE;
			Double[] QVals=Q.get(state);
			for(Integer action:possibleActions)
			{
				if(maxQ<QVals[action])
				{
					maxQ=QVals[action];
					maxAction=action;
				}
			}
			
			return maxAction;
		}		
	}

	/**
	 * Instantiates a new engine.
	 *
	 * @param world the world
	 * @param startState the start state
	 */
	public Engine(World world, State startState) {
		super();
		configureLogger();
		
		log.info("Initializing engine...");
		log.info("State space size: "+State.STATE_SPACE_SIZE);
		
		//Initialize the elements
		Q=new HashMap<State, Double[]>(State.STATE_SPACE_SIZE);
		this.world=world;
		this.previousAction=this.prevPreviousAction=Action.NO_ACTION;
		this.currentState=startState;
		log.info("Start state: "+currentState);

		log.info("Engine initialized");
	}
	
	
}
