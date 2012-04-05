package ml.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ml.scenario.ScenarioEvent;
import ml.scenario.ScenarioGenerator;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * The Class World.
 */
public class World {
	
	/** The previous action. */
	private int previousAction;
	
	/** The previous to previous action. */
	private int prevPreviousAction;
	
	/** The events. */
	private ArrayList<ScenarioEvent> events;
	
	/** The current event index. */
	private int currentEventIndex;
	
	/** The scenario generator. */
	private ScenarioGenerator sg;
	
	/** The people waiting. */
	private ArrayList<LinkedList<ScenarioEvent>> peopleWaiting;	
	
	/** The people in e1. */
	private LinkedList<ScenarioEvent> peopleInE1;
	
	/** The people in e2. */
	private LinkedList<ScenarioEvent> peopleInE2;
	
	/** The time. */
	private int time;
	
	/** The Constant log. */
	private static final Logger log=Logger.getLogger(World.class);
	
	/**
	 * Configure logger.
	 */
	private void configureLogger()
	{
		PatternLayout patternLayout=new PatternLayout("%-3r [%-5p] %c - %m%n");
		ConsoleAppender appender=new ConsoleAppender(patternLayout);
		log.addAppender(appender);
		log.setLevel(Level.ALL);		
	}

	/**
	 * Instantiates a new world.
	 */
	public World() {
		//Config the logger
		configureLogger();
		
		//Init the scenario & events
		sg=new ScenarioGenerator();
		time=0;
		
		peopleInE1=new LinkedList<ScenarioEvent>();
		peopleInE2=new LinkedList<ScenarioEvent>();
		peopleWaiting=new ArrayList<LinkedList<ScenarioEvent>>(ScenarioGenerator.FLOOR_COUNT);
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
			peopleWaiting.add(new LinkedList<ScenarioEvent>());		
		
		events=sg.generateScenarioDay(0);
		Collections.sort(events);
		currentEventIndex=0;
		
		//Logging
		log.info("Generated scenario with "+events.size()+" events.");
		if(log.isDebugEnabled())
			for(ScenarioEvent ev:events)
				log.debug(ev);
		log.info("World initialized.");		
	}
	
	/**
	 * Generate a start state.
	 *
	 * @return the state
	 */
	private State generateStartState() {
		State state=new State();
		state.timeInterval=0;		
		return state;
	}
	
	/**
	 * Gets the next state.
	 *
	 * @param currentState the current state
	 * @param action the action
	 * @return the state
	 */
	public State getNextState(State currentState, int action)
	{
		//Update the world data
		time++;
		injectEventsInWorld();
		
		
		
		State state=new State();
		
		//Time interval
		state.timeInterval=(byte) (time/60/2);
		
		//Next elevator positions
		switch(Action.getE1Action(action))
		{
		case Action.E1_UP: 		state.elevator1Floor=(byte) (currentState.elevator1Floor+1); break;
		case Action.E1_STOP: 	state.elevator1Floor=(byte) (currentState.elevator1Floor); break;
		case Action.E1_DOWN: 	state.elevator1Floor=(byte) (currentState.elevator1Floor-1); break;
		}
		switch(Action.getE2Action(action))
		{
		case Action.E2_UP: 		state.elevator2Floor=(byte) (currentState.elevator2Floor+1); break;
		case Action.E2_STOP: 	state.elevator2Floor=(byte) (currentState.elevator2Floor); break;
		case Action.E2_DOWN: 	state.elevator2Floor=(byte) (currentState.elevator2Floor-1); break;
		}
		
		//Destinations
		for(int i=0;i<3;i++)
		{
			state.destinationsE1[i]=currentState.destinationsE1[i];
			state.destinationsE2[i]=currentState.destinationsE2[i];
		}
		
		//If the current action is to stop the elevator or keep it stopped one more stop
		//Elevator 1
		if(Action.getE1Action(action)==Action.E1_STOP)
			//If the elevator just stopped, the people inside should get out 
			if(Action.getE1Action(previousAction)!=Action.E1_STOP)
			{
				Iterator<ScenarioEvent> it=peopleInE1.iterator();
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger's destination is current floor, get out 
					if(ev.stopFloor==state.elevator1Floor)
					{
						log.debug("Passenger from E1 reached destination: "+ev);
						//Remove from elevator
						it.remove();
						state.destinationsE1[ev.stopFloor]=false;
					}
				}
			}
			//If the elevator stopped the previous step, the people outside should get in 
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.elevator1Floor).iterator();
				boolean moreWaiting=false;
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger wants to go in the same direction as the elevator was going, hop in
					if(personShouldGoIn(Action.getE1Action(prevPreviousAction), ev))
					{
						//Check if the elevator is full
						if(peopleInE1.size()>=ScenarioGenerator.ELEVATOR_CAPACITY)
						{
							log.debug("E1 is full");
							moreWaiting=true;
							break;
						}
						log.debug("Passenger going in E1:"+ev);
						//Remove from waiting
						it.remove();
						//Add in elevator
						peopleInE1.add(ev);
						state.destinationsE1[ev.stopFloor]=true;
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE1Action(prevPreviousAction)==Action.E1_UP)
						state.waiting[state.elevator1Floor][State.UP]=false;
					else
						state.waiting[state.elevator1Floor][State.DOWN]=false;
			}
		//Elevator 2
		if(Action.getE2Action(action)==Action.E2_STOP)
			//If the elevator just stopped, the people inside should get out 
			if(Action.getE2Action(previousAction)!=Action.E2_STOP)
			{
				Iterator<ScenarioEvent> it=peopleInE2.iterator();
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger's destination is current floor, get out 
					if(ev.stopFloor==state.elevator2Floor)
					{
						log.debug("Passenger from E2 reached destination: "+ev);
						//Remove from elevator
						it.remove();
						state.destinationsE2[ev.stopFloor]=false;
					}
				}
			}
			//If the elevator stopped the previous step, the people outside should get in 
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.elevator2Floor).iterator();
				boolean moreWaiting=false;
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger wants to go in the same direction as the elevator was going, hop in
					if(personShouldGoIn(Action.getE2Action(prevPreviousAction), ev))
					{
						//Check if the elevator is full
						if(peopleInE2.size()>=ScenarioGenerator.ELEVATOR_CAPACITY)
						{
							log.debug("E2 is full");
							moreWaiting=true;
							break;
						}
						log.debug("Passenger going in E2:"+ev);
						//Remove from waiting
						it.remove();
						//Add in elevator
						peopleInE2.add(ev);
						state.destinationsE2[ev.stopFloor]=true;
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE2Action(prevPreviousAction)==Action.E2_UP)
						state.waiting[state.elevator1Floor][State.UP]=false;
					else
						state.waiting[state.elevator1Floor][State.DOWN]=false;
			}
		
		
				
				
		
		return state;
	}
	
	/**
	 * Injects the events corresponding to the current {@value time} in the world.
	 */
	private void injectEventsInWorld()
	{
		//While there are more events at the current time, inject them in the system
		while(events.get(currentEventIndex).time==time)
		{
			ScenarioEvent ev=events.get(currentEventIndex);
			peopleWaiting.get(ev.startFloor).add(ev);
			currentEventIndex++;
		}
	}
	
	/**
	 * Whether the person should go in the elevator or not, considering the elevator was going in the
	 * given direction, if any.
	 *
	 * @param prevDirection the previous direction
	 * @param ev the event/person
	 * @return true, if successful
	 */
	private boolean personShouldGoIn(int prevDirection, ScenarioEvent ev)
	{
		//Going down
		if(ev.stopFloor-ev.startFloor>0)
			if(prevDirection==Action.NO_ACTION || prevDirection==Action.E1_DOWN || prevDirection==Action.E2_DOWN)
				return true;
		
		//Going up
		if(ev.stopFloor-ev.startFloor<0)
			if(prevDirection==Action.NO_ACTION || prevDirection==Action.E1_UP || prevDirection==Action.E2_UP)
				return true;
		
		return true;
	}
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		World world = new World();
		State startState=world.generateStartState();
		Engine engine=new Engine(world,startState);
		
	}


}
