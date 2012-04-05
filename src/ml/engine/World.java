package ml.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import ml.scenario.ScenarioEvent;
import ml.scenario.ScenarioGenerator;

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
		time=-1;
		
		peopleInE1=new LinkedList<ScenarioEvent>();
		peopleInE2=new LinkedList<ScenarioEvent>();
		peopleWaiting=new ArrayList<LinkedList<ScenarioEvent>>(ScenarioGenerator.FLOOR_COUNT);
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
			peopleWaiting.add(new LinkedList<ScenarioEvent>());		
		
		events=sg.generateScenarioDay(0);
		Collections.sort(events);
		currentEventIndex=0;
		previousAction=Action.NO_ACTION;
		prevPreviousAction=Action.NO_ACTION;
		
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
		State state=new State();
		
		//Update people waiting
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
		{
			state.waiting[i][State.UP]=currentState.waiting[i][State.UP];
			state.waiting[i][State.DOWN]=currentState.waiting[i][State.DOWN];
		}

		//Update the world data
		time++;
		injectEventsInWorld(state);
		
		//Time interval
		state.timeInterval=(byte) (time/60/2);

		//Next elevator positions
		switch(Action.getE1Action(action))
		{
		case Action.E1_UP: 		state.elevator1Floor=(byte) (currentState.elevator1Floor+1); break;
		case Action.E1_STOP: 	state.elevator1Floor=(byte) (currentState.elevator1Floor); break;
		case Action.E1_DOWN: 	state.elevator1Floor=(byte) (currentState.elevator1Floor-1); break;
		default: log.fatal("Illegal action for E1: "+action);
		}
		switch(Action.getE2Action(action))
		{
		case Action.E2_UP: 		state.elevator2Floor=(byte) (currentState.elevator2Floor+1); break;
		case Action.E2_STOP: 	state.elevator2Floor=(byte) (currentState.elevator2Floor); break;
		case Action.E2_DOWN: 	state.elevator2Floor=(byte) (currentState.elevator2Floor-1); break;
		default: log.fatal("Illegal action for E2: "+action);
		}
		
		/** Update the people waiting, in the elevators and update the destinations in the state
		 * 
		 * If the current action is to stop the elevator or keep it stopped one more step, get people in/out of the elevator
		 * and update everything.
		 * 
		 * If the current action is to move, just update the destinations for the people inside
		 */
		/* Case 1 - Elevator is stopped - Elevator 1 */
		if(Action.getE1Action(action)==Action.E1_STOP)
			
			/* CASE 1.1
			 * If the elevator just stopped, the people inside should get out 
			 */
			if(Action.getE1Action(previousAction)==Action.E1_DOWN || 
					Action.getE1Action(previousAction)==Action.E1_UP)
			{
				Iterator<ScenarioEvent> it=peopleInE1.iterator();
				// See which passengers should go out and also update the destinations
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger's destination is current floor, get out 
					if(ev.stopFloor==state.elevator1Floor)
					{
						log.debug("Passenger from E1 reached destination: "+ev);
						//Remove from elevator
						it.remove();
					}
					else
						//Going up
						if(ev.startFloor>ev.stopFloor)
							state.destinationsE1[State.ABOVE]=true;
						//Going down
						else 
							state.destinationsE1[State.BELOW]=true;							
				}
			}
			
		
			/* CASE 1.2
			 * If the elevator stopped the previous step (was already stopped), the people outside should get in 
			 */
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.elevator1Floor).iterator();
				boolean moreWaiting=false;
				
				//Update destinations for people who are in the elevator
				updateDestinations(state.destinationsE1, peopleInE1);
				
				//See which passengers want to use the elevator and also update the destinations for people going in 
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
						//Update destinations
						if(ev.startFloor>ev.stopFloor)
							state.destinationsE1[State.BELOW]=true;
						else
							state.destinationsE1[State.ABOVE]=true;
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE1Action(prevPreviousAction)==Action.E1_UP)
						state.waiting[state.elevator1Floor][State.UP]=false;
					else if(Action.getE1Action(prevPreviousAction)==Action.E1_DOWN)
						state.waiting[state.elevator1Floor][State.DOWN]=false;
					else
					{
						state.waiting[state.elevator1Floor][State.DOWN]=false;
						state.waiting[state.elevator1Floor][State.UP]=false;
					}
			}
		/* CASE 2 - Elevator is moving - Elevator 1*/
		else
		{
			updateDestinations(state.destinationsE1, peopleInE1);
		}
		
		
		/* Case 1 - Elevator is stopped - Elevator 2 */
		if(Action.getE2Action(action)==Action.E2_STOP)

			/* CASE 1.1
			 * If the elevator just stopped, the people inside should get out 
			 */
			if(Action.getE1Action(previousAction)==Action.E2_DOWN || 
				Action.getE2Action(previousAction)==Action.E2_UP)
			{
				Iterator<ScenarioEvent> it=peopleInE2.iterator();
				// See which passengers should go up and also update the destinations
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger's destination is current floor, get out 
					if(ev.stopFloor==state.elevator2Floor)
					{
						log.debug("Passenger from E2 reached destination: "+ev);
						//Remove from elevator
						it.remove();
					}
					else
						//Going up
						if(ev.startFloor>ev.stopFloor)
							state.destinationsE2[State.ABOVE]=true;
						//Going down
						else 
							state.destinationsE2[State.BELOW]=true;							
				}
			}
		
			/* CASE 1.2
			 * If the elevator stopped the previous step (was already stopped), the people outside should get in 
			 */
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.elevator2Floor).iterator();
				boolean moreWaiting=false;
				//Update destinations for people who are in the elevator
				updateDestinations(state.destinationsE2, peopleInE2);
				
				//See which passengers want to use the elevator and also update the destinations for people going in 
				while(it.hasNext())
				{
					ScenarioEvent ev=it.next();
					//If the passenger wants to go in the same direction as the elevator was going, hop in
					if(personShouldGoIn(Action.getE1Action(prevPreviousAction), ev))
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
						//Update destinations
						if(ev.startFloor>ev.stopFloor)
							state.destinationsE2[State.BELOW]=true;
						else
							state.destinationsE2[State.ABOVE]=true;
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE2Action(prevPreviousAction)==Action.E2_UP)
						state.waiting[state.elevator2Floor][State.UP]=false;
					else if(Action.getE2Action(prevPreviousAction)==Action.E2_DOWN)
						state.waiting[state.elevator2Floor][State.DOWN]=false;
					else
					{
						state.waiting[state.elevator2Floor][State.DOWN]=false;
						state.waiting[state.elevator2Floor][State.UP]=false;
					}
			}
		/* CASE 2 - Elevator is moving - Elevator 2*/
		else
		{
			updateDestinations(state.destinationsE2, peopleInE2);
		}
			
		//Update actions
		prevPreviousAction=previousAction;
		previousAction=action;				
				
		
		return state;
	}
	
	/**
	 * Update destinations array for the people in the elevator.
	 *
	 * @param destinationsE the destinations for elevator
	 * @param peopleInE the people in elevator
	 */
	private void updateDestinations(boolean[] destinationsE,
			LinkedList<ScenarioEvent> peopleInE) {
		
		destinationsE[State.ABOVE]=destinationsE[State.BELOW]=destinationsE[State.CURRENT]=false;
		
		for(ScenarioEvent ev: peopleInE)
			if(ev.startFloor>ev.stopFloor)
				destinationsE[State.BELOW]=true;
			else if(ev.startFloor<ev.stopFloor)
				destinationsE[State.ABOVE]=true;
			else
				destinationsE[State.CURRENT]=true;
	}

	/**
	 * Injects the events corresponding to the current {@code time} in the world. Also, updates
	 * the waiting list in the given {@code state}
	 *
	 * @param state the state
	 */
	private void injectEventsInWorld(State state)
	{
		//While there are more events at the current time, inject them in the system
		while(events.get(currentEventIndex).time==time)
		{
			ScenarioEvent ev=events.get(currentEventIndex);
			peopleWaiting.get(ev.startFloor).add(ev);
			//going UP
			if(ev.stopFloor>ev.startFloor)
				state.waiting[ev.startFloor][State.UP]=true;
			//going DOWN
			else if(ev.startFloor > ev.stopFloor)
				state.waiting[ev.startFloor][State.DOWN]=true;
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
		//Going down or no action/stopped for long or top floor
		if(ev.stopFloor-ev.startFloor>0)
			if(prevDirection==Action.NO_ACTION || prevDirection==Action.E1_DOWN || prevDirection==Action.E2_DOWN ||
					prevDirection==Action.E1_STOP || prevDirection==Action.E2_STOP)
				return true;
		
		//Going up
		if(ev.stopFloor-ev.startFloor<0)
			if(prevDirection==Action.NO_ACTION || prevDirection==Action.E1_UP || prevDirection==Action.E2_UP ||
					prevDirection==Action.E1_STOP || prevDirection==Action.E2_STOP)
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
		world.events.add(0,new ScenarioEvent(0, 0, 1));
		world.events.add(1,new ScenarioEvent(1, 1, 3));
		world.events.add(2,new ScenarioEvent(3, 1, 0));
		State startState=world.generateStartState();
		Engine engine=new Engine(world,startState);
		State nextState=world.getNextState(startState, Action.E2_UP+Action.E1_STOP);
		log.info(nextState);
		nextState=world.getNextState(nextState, Action.E2_STOP+Action.E1_STOP);
		log.info(nextState);
		nextState=world.getNextState(nextState, Action.E2_STOP+Action.E1_UP);
		log.info(nextState);
		nextState=world.getNextState(nextState, Action.E2_UP+Action.E1_STOP);
		log.info(nextState);
		nextState=world.getNextState(nextState, Action.E2_UP+Action.E1_STOP);
		log.info(nextState);		
	}


}
