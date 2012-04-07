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
	
	/** The Constant LEARNING_FACTOR. */
	public static final double LEARNING_FACTOR=0.3;
	
	/** The Constant ATTENUATION_FACTOR. */
	public static final double ATTENUATION_FACTOR=0.3;
	
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
	public static final Logger log=Logger.getLogger(Engine.class);
	
	/**
	 * Configures the logger.
	 */
	private void configureLogger()
	{
		PatternLayout patternLayout=new PatternLayout("%-3r [%-5p] %c - %m%n");
		ConsoleAppender appender=new ConsoleAppender(patternLayout);
		appender.setImmediateFlush(true);
		log.addAppender(appender);
		log.setLevel(Level.INFO);
	}
	
	public void logStatistics()
	{
		log.info("State Space Size: "+Q.size());
	}
	
	/**
	 * Sets the Q value corresponding to a state and an action Q(s,a).
	 *
	 * @param state the state
	 * @param action the action
	 * @param val the value
	 */
	private void setQValue(State state, Integer action, Double val)
	{
		Double vals[]=Q.get(state);
		if(vals==null)
		{
			Double nVals[]=new Double[Action.ACTION_COUNT];
			nVals[action]=val;
			Q.put(state, nVals);			
		}
		else
		{
			vals[action]=val;
		}
	}
	
	/**
	 * Gets the Q value corresponding to a state and an action Q(s,a).
	 *
	 * @param state the state
	 * @param action the action
	 * @return the Q value
	 */
	private double getQValue(State state, Integer action)
	{
		Double vals[]=Q.get(state);
		if(vals==null)
			return 0;
		else
			return vals[action]!=null?vals[action]:0;
	}
	
	/**
	 * Run.
	 */
	public void run()
	{
		//log.info("Engine started");	
		
		State newState;
		Integer action=getNextAction(currentState);
		Integer newAction;
		Double reward;
		Double newQVal;
		while(!world.isScenarioFinished())
		{	
			time++;
			//log.debug(time+") Now in state: "+currentState+" performing "+action);
			
			//Perform the action and get to the new state & the reward
			newState=world.getNextState(currentState, action);
			reward=world.getRewardForCurrentState();
			//log.debug("Reward: "+world.getRewardForCurrentState());
			
			//Choose next action
			newAction=getNextAction(newState);
			//log.debug("Next action: "+newAction+". Updating Q Value.");
			
			//Update Q
			newQVal=getQValue(this.currentState, action);
			newQVal+=LEARNING_FACTOR*(reward+ATTENUATION_FACTOR*getQValue(newState, newAction)-newQVal);
			setQValue(this.currentState, action, newQVal);
			//log.debug("Q value updated. Step finished.");
			
			//Update actions and state
			this.prevPreviousAction=previousAction;
			this.previousAction=action;
			action=newAction;
			this.currentState=newState;
		}
		
		
		//log.info("Engine finished");
	}
	
	/**
	 * Gets the next action. Uses epsilon-greedy.
	 *
	 * @return the next action
	 */
	public Integer getNextAction(State state)
	{
		ArrayList<Integer> possibleActions;
		possibleActions=world.getPossibleActions(state, previousAction, prevPreviousAction);
		
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
				double val=QVals[action]!=null?QVals[action]:0;
				if(maxQ<val)
				{
					maxQ=val;
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
