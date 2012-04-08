/*
 * Stefan-Dobrin Cosmin
 * 342C4
 * 
 * Invatare Automata
 * 2012
 */
package ml.engine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
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
	public static final double ACTION_EPSILON=0.9;
	
	/** The Constant LEARNING_FACTOR. */
	public static final double LEARNING_FACTOR=0.8;
	
	/** The Constant ATTENUATION_FACTOR. */
	public static final double ATTENUATION_FACTOR=0.4;
	
	/** The Constant ANNEALING_FACTOR. */
	public static final double ANNEALING_FACTOR=0.001/1000; 
	
	public double ACTION_EPSILON_ANNEALED;
	
	/** A random number generator. */
	private static final Random rand=new Random();
	
	/** The Q. */
	private HashMap<State,double[]> Q;
	
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
		log.setLevel(Level.DEBUG);
	}
	
	/**
	 * Log statistics.
	 */
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
	private void setQValue(State state, Integer action, double val)
	{
		double vals[]=Q.get(state);
		if(vals==null)
		{
			double nVals[]=new double[Action.ACTION_COUNT];
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
		double vals[]=Q.get(state);
		if(vals==null)
			return 0;
		else
			return vals[action];
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
			if(ACTION_EPSILON_ANNEALED>ACTION_EPSILON/2)
				ACTION_EPSILON_ANNEALED-=ACTION_EPSILON_ANNEALED*ANNEALING_FACTOR;
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
	 * @param state the state
	 * @return the next action
	 */
	public Integer getNextAction(State state)
	{
		ArrayList<Integer> possibleActions;
		possibleActions=world.getPossibleActions(state, previousAction, prevPreviousAction);
		
		//Explore - Pick a random action
		if(!Q.containsKey(state) || rand.nextDouble()<ACTION_EPSILON_ANNEALED)
			return possibleActions.get(rand.nextInt(possibleActions.size()));
		//Exploit - Get the BEST action
		else
		{
			int maxAction=possibleActions.get(0);
			double maxQ=-Double.MAX_VALUE;
			double[] QVals=Q.get(state);
			for(Integer action:possibleActions)
			{
				double val=QVals[action];
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
		ACTION_EPSILON_ANNEALED=ACTION_EPSILON;
		Q=new HashMap<State, double[]>(State.STATE_SPACE_SIZE);
		this.world=world;
		this.previousAction=this.prevPreviousAction=Action.NO_ACTION;
		this.currentState=startState;
		log.info("Start state: "+currentState);

		log.info("Engine initialized");
	}
	
	/**
	 * Write Q to file.
	 *
	 * @param filename the filename
	 */
	public void writeQToFile(String filename)
	{
		log.info("Writing Q to file");
		
		try {
			BufferedWriter out=new BufferedWriter(new FileWriter(filename));
			out.write(Integer.toString(Q.size())+"\n");
			for(Entry<State, double[]> entry:Q.entrySet())
			{
				out.write(entry.getKey().flushState() + " - " + flushQValues(entry.getValue())+"\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("Write completed.");
	}
	
	/**
	 * Flush Q values.
	 *
	 * @param vals the values
	 * @return the string
	 */
	public static String flushQValues(double vals[])
	{
		StringBuilder build=new StringBuilder();
		for(int i=0;i<Action.ACTION_COUNT;i++)
			build.append(String.format("%.2f ", vals[i]));
		return build.toString();
	}
	
}
