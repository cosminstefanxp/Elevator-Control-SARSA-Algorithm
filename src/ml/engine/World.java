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
	
	/** The Constant REWARD_TIME_LIMIT that defines the number of delay time units that are
	 * still positively rewarded. After that, the reward gets strictly negative. */
	//private static final int REWARD_DELAY_LIMIT=5;
	
	/** The Constant REWARD_PER_UNIT that defines the reward per time unit. */
	private static final int REWARD_PER_UNIT=-1;
	
	/** The Constant EPISODE_SIZE that defines the number of identical "days" in an episode. */
	private static final int EPISODE_SIZE=500;
	
	/** The Constant EPISODE_COUNT. */
	private static final int EPISODE_COUNT=100;
	
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
		log.setLevel(Level.INFO);		
	}
	
	/**
	 * Gets the delay time: TDestination − TShowUp − |StartFloor − StopFloor|
	 *
	 * @param ev the event
	 * @return the delay (negative number)
	 */
	private int getDelay(ScenarioEvent ev, int stopTime)
	{
		if(ev.stopFloor>ev.startFloor)
			return stopTime-ev.time-(ev.stopFloor-ev.startFloor);
		else
			return stopTime-ev.time-(ev.startFloor-ev.stopFloor);
	}
	
	/**
	 * Gets an array with the all the possible action starting from a given state.
	 *
	 * @param state the state
	 * @param prevAction the previous action
	 * @return the possible actions
	 */
	public ArrayList<Integer> getPossibleActions(State state, Integer previousAction, Integer prevPreviousAction)
	{
		ArrayList<Integer> a=new ArrayList<Integer>();
		boolean e1up, e2up, e1down, e2down;
		e1up=e2up=e1down=e2down=true;
		int e1a, e2a;
		e1a=Action.getE1Action(previousAction);
		e2a=Action.getE1Action(previousAction);
		
		//If elevator just stopped -> it can't move for 2 units
		if(e1a==Action.E1_STOP && 
				(Action.getE1Action(prevPreviousAction)==Action.E1_UP || 
				 Action.getE1Action(prevPreviousAction)==Action.E1_DOWN))
			e1up=e1down=false;
		if(e2a==Action.E2_STOP &&
				(Action.getE2Action(prevPreviousAction)==Action.E2_UP || 
				 Action.getE2Action(prevPreviousAction)==Action.E2_DOWN))
			e2up=e2down=false;
		
		//If elevator is at top, it can't move up
		if(state.getElevator1Floor()==ScenarioGenerator.MAX_FLOOR)
			e1up=false;
		if(state.getElevator2Floor()==ScenarioGenerator.MAX_FLOOR)
			e2up=false;
		
		//If elevator is at bottom, it can't move up
		if(state.getElevator1Floor()==ScenarioGenerator.MIN_FLOOR)
			e1down=false;
		if(state.getElevator2Floor()==ScenarioGenerator.MIN_FLOOR)
			e2down=false;
		
		//Generate actions
		if(e1up)
		{
			a.add(Action.combine(Action.E1_UP, Action.E2_STOP));
			if(e2up) a.add(Action.combine(Action.E1_UP, Action.E2_UP));
			if(e2down) a.add(Action.combine(Action.E1_UP, Action.E2_DOWN));
		}
		if(e1down)
		{
			a.add(Action.combine(Action.E1_DOWN, Action.E2_STOP));
			if(e2up) a.add(Action.combine(Action.E1_DOWN, Action.E2_UP));
			if(e2down) a.add(Action.combine(Action.E1_DOWN, Action.E2_DOWN));
		}
		//The elevator can stop at any time
		a.add(Action.combine(Action.E1_STOP, Action.E2_STOP));
		if(e2up) a.add(Action.combine(Action.E1_STOP, Action.E2_UP));
		if(e2down) a.add(Action.combine(Action.E1_STOP, Action.E2_DOWN));
	
		
		return a;
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
		
		//Generate episode 
		events=sg.generateScenarioIdenticalDays(EPISODE_SIZE);
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
		state.setTimeInterval((byte) 0);
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
			state.setWaiting(i,State.UP,currentState.getWaiting(i,State.UP));
			state.setWaiting(i,State.DOWN,currentState.getWaiting(i,State.DOWN));
		}

		//Update the world data
		time++;
		injectEventsInWorld(state);
		
		//Time interval
		state.setTimeInterval((byte) (time/60/2));

		//Next elevator positions
		switch(Action.getE1Action(action))
		{
		case Action.E1_UP: 		state.setElevator1Floor((byte) (currentState.getElevator1Floor()+1)); break;
		case Action.E1_STOP: 	state.setElevator1Floor((byte) (currentState.getElevator1Floor())); break;
		case Action.E1_DOWN: 	state.setElevator1Floor((byte) (currentState.getElevator1Floor()-1)); break;
		default: log.fatal("Illegal action for E1: "+action);
		}
		switch(Action.getE2Action(action))
		{
		case Action.E2_UP: 		state.setElevator2Floor((byte) (currentState.getElevator2Floor()+1)); break;
		case Action.E2_STOP: 	state.setElevator2Floor((byte) (currentState.getElevator2Floor())); break;
		case Action.E2_DOWN: 	state.setElevator2Floor((byte) (currentState.getElevator2Floor()-1)); break;
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
					if(ev.stopFloor==state.getElevator1Floor())
					{
						//log.debug("Passenger from E1 reached destination: "+ev);
						//Remove from elevator
						it.remove();
					}
					else
						//Going up
						if(ev.startFloor>ev.stopFloor)
							state.setDestinationE1(State.ABOVE,true);
						//Going down
						else 
							state.setDestinationE1(State.BELOW,true);					
				}
			}
			
		
			/* CASE 1.2
			 * If the elevator stopped the previous step (was already stopped), the people outside should get in 
			 */
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.getElevator1Floor()).iterator();
				boolean moreWaiting=false;
				
				//Update destinations for people who are in the elevator
				updateDestinations(state, 1, peopleInE1);
				
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
							//log.debug("E1 is full");
							moreWaiting=true;
							break;
						}
						//log.debug("Passenger going in E1:"+ev);
						//Remove from waiting
						it.remove();
						//Add in elevator
						peopleInE1.add(ev);
						//Update destinations
						if(ev.startFloor>ev.stopFloor)
							state.setDestinationE1(State.BELOW,true);
						else
							state.setDestinationE1(State.ABOVE,true);
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE1Action(prevPreviousAction)==Action.E1_UP)
						state.setWaiting(state.getElevator1Floor(), State.UP, false);
					else if(Action.getE1Action(prevPreviousAction)==Action.E1_DOWN)
						state.setWaiting(state.getElevator1Floor(), State.DOWN, false);
					else
					{
						state.setWaiting(state.getElevator1Floor(), State.UP, false);
						state.setWaiting(state.getElevator1Floor(), State.DOWN, false);
					}
			}
		/* CASE 2 - Elevator is moving - Elevator 1*/
		else
		{
			updateDestinations(state, 1, peopleInE1);
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
					if(ev.stopFloor==state.getElevator2Floor())
					{
						//log.debug("Passenger from E2 reached destination: "+ev);
						//Remove from elevator
						it.remove();
					}
					else
						//Going up
						if(ev.startFloor>ev.stopFloor)
							state.setDestinationE2(State.ABOVE, true);
						//Going down
						else 
							state.setDestinationE2(State.ABOVE, false);							
				}
			}
		
			/* CASE 1.2
			 * If the elevator stopped the previous step (was already stopped), the people outside should get in 
			 */
			else
			{
				Iterator<ScenarioEvent> it=peopleWaiting.get(state.getElevator2Floor()).iterator();
				boolean moreWaiting=false;
				//Update destinations for people who are in the elevator
				updateDestinations(state, 2, peopleInE2);
				
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
							//log.debug("E2 is full");
							moreWaiting=true;
							break;
						}
						//log.debug("Passenger going in E2:"+ev);
						//Remove from waiting
						it.remove();
						//Add in elevator
						peopleInE2.add(ev);
						//Update destinations
						if(ev.startFloor>ev.stopFloor)
							state.setDestinationE2(State.BELOW, true);
						else
							state.setDestinationE2(State.ABOVE, true);
					}
				}
				//Mark if no more people are waiting on this floor to go in the same direction as the elevator
				if(!moreWaiting)
					if(Action.getE2Action(prevPreviousAction)==Action.E2_UP)
						state.setWaiting(state.getElevator2Floor(), State.UP, false);
					else if(Action.getE2Action(prevPreviousAction)==Action.E2_DOWN)
						state.setWaiting(state.getElevator2Floor(), State.DOWN, false);
					else
					{
						state.setWaiting(state.getElevator2Floor(), State.UP, false);
						state.setWaiting(state.getElevator2Floor(), State.DOWN, false);
					}
			}
		/* CASE 2 - Elevator is moving - Elevator 2*/
		else
		{
			updateDestinations(state, 2, peopleInE2);
		}
			
		//Update actions
		prevPreviousAction=previousAction;
		previousAction=action;				
				
		return state;
	}
	
	/**
	 * Gets the reward for the current state. Due to memory restrictions, states cannot contain
	 * all the information required to get the reward. So, only the reward for the current state
	 * can be obtained.
	 * 
	 * @return the reward for current state
	 */
	public Double getRewardForCurrentState()
	{
		int reward=0;
		for(ScenarioEvent passenger:peopleInE1)
			reward+=this.getDelay(passenger, this.time);
		for(ScenarioEvent passenger:peopleInE2)
			reward+=this.getDelay(passenger, this.time);
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
			for(ScenarioEvent passenger:peopleWaiting.get(i))
				reward+=this.getDelay(passenger, this.time);
		return (double) (reward*REWARD_PER_UNIT);
	}
	
	/**
	 * Update destinations array for the people in the elevator.
	 *
	 * @param state the state
	 * @param elevator the elevator (as 1 or 2)
	 * @param peopleInE the people in elevator
	 */
	private void updateDestinations(State state, int elevator,
			LinkedList<ScenarioEvent> peopleInE) {
		
		if(elevator==1)
		{
			state.setDestinationE1(State.ABOVE, false);
			state.setDestinationE1(State.BELOW, false);
			state.setDestinationE1(State.CURRENT, false);
		}
		else
		{
			state.setDestinationE2(State.ABOVE, false);
			state.setDestinationE2(State.BELOW, false);
			state.setDestinationE2(State.CURRENT, false);
		}
			
		
		for(ScenarioEvent ev: peopleInE)
			if(ev.startFloor>ev.stopFloor)
				if(elevator==1)
					state.setDestinationE1(State.BELOW,true);		
				else
					state.setDestinationE2(State.BELOW,true);
			else if(ev.startFloor<ev.stopFloor)
				if(elevator==1)
					state.setDestinationE1(State.ABOVE,true);		
				else
					state.setDestinationE2(State.ABOVE,true);
			else
				if(elevator==1)
					state.setDestinationE1(State.CURRENT,true);		
				else
					state.setDestinationE2(State.CURRENT,true);
	}
	
	/**
	 * Checks if the scenario is finished.
	 *
	 * @return true, if is scenario finished
	 */
	public boolean isScenarioFinished()
	{
		if(currentEventIndex>=events.size())
		{
			if(this.peopleInE1.size()>0)
				return false;
			if(this.peopleInE2.size()>0)
				return false;
			for(LinkedList<ScenarioEvent> l:peopleWaiting)
				if(l.size()>0)
					return false;
			
			return true;
		}
		return false;
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
		while(currentEventIndex<events.size() && events.get(currentEventIndex).time<=time)
		{
			ScenarioEvent ev=events.get(currentEventIndex);
			peopleWaiting.get(ev.startFloor).add(ev);
			//going UP
			if(ev.stopFloor>ev.startFloor)
				state.setWaiting(ev.startFloor, State.UP, true);
			//going DOWN
			else if(ev.startFloor > ev.stopFloor)
				state.setWaiting(ev.startFloor, State.DOWN, true);
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
	 * Resets an episode.
	 */
	public void resetEpisode()
	{
		this.peopleInE1.clear();
		this.peopleInE2.clear();
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
			this.peopleWaiting.get(i).clear();
		
		time=-1;
		events=sg.generateScenarioIdenticalDays(EPISODE_SIZE);
		currentEventIndex=0;
		previousAction=Action.NO_ACTION;
		prevPreviousAction=Action.NO_ACTION;
		
		log.info("World resetted. New Episode of size: "+events.size());
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
		
//		world.events.add(0,new ScenarioEvent(0, 0, 1));
//		world.events.add(1,new ScenarioEvent(1, 1, 3));
//		world.events.add(2,new ScenarioEvent(3, 1, 0));
		for(int i=0;i<EPISODE_COUNT;i++)
		{
			log.info("Run "+i);
			world.resetEpisode();
			engine.run();
			engine.logStatistics();
		}
		
		engine.writeQToFile("out_Q");
		
		
//		log.debug(world.getPossibleActions(startState, Action.E1_STOP+Action.E2_STOP, Action.E1_STOP+Action.E2_STOP));
//		
//		State nextState=world.getNextState(startState, Action.E1_UP+Action.E2_STOP);
//		log.info(nextState);
//		log.info(world.getRewardForCurrentState());
//		nextState=world.getNextState(nextState, Action.E1_STOP+Action.E2_STOP);
//		log.info(nextState);
//		log.info(world.getRewardForCurrentState());
//		nextState=world.getNextState(nextState, Action.E1_STOP+Action.E2_UP);
//		log.info(nextState);
//		log.info(world.getRewardForCurrentState());
//		nextState=world.getNextState(nextState, Action.E1_UP+Action.E2_STOP);
//		log.info(nextState);
//		log.info(world.getRewardForCurrentState());
//		nextState=world.getNextState(nextState, Action.E1_UP+Action.E2_STOP);
//		log.info(nextState);		
	}


}
