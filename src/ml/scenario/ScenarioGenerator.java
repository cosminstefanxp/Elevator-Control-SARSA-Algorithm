package ml.scenario;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * The Class ScenarioGenerator.
 */
public class ScenarioGenerator {
	
	/** The Constant ELEVATOR_CAPACITY. */
	public static final int ELEVATOR_CAPACITY=10;
	
	/** The Constant MAX_FLOOR. */
	public static final int MAX_FLOOR=4;
	
	/** The Constant MIN_FLOOR. */
	public static final int MIN_FLOOR=0;
	
	/** The Constant NO_FLOOR. */
	public static final int NO_FLOOR=MIN_FLOOR-1;
	
	/** The Constant FLOOR_COUNT. */
	public static final int FLOOR_COUNT=MAX_FLOOR-MIN_FLOOR+1;
	
	/** The Constant DAY_DURATION. */
	public static final int DAY_DURATION=60*24;
	
	/** The Constant PROB_FOLLOW_TREND. */
	public static final double PROB_FOLLOW_TREND=0.75f;
	
	/** The Constant PROB_USE_MIN_FLOOR. */
	public static final double PROB_USE_MIN_FLOOR=0.6f;
	
	/** The Constant rand that provides random numbers. */
	private static final Random rand=new Random();
	
	/**
	 * The Enum Trend.
	 */
	private enum Trend { /** The Up. */
 Up, /** The Down. */
 Down, /** The None. */
 None };
	
	/**
	 * Generate a new {@link ScenarioEvent} that is basically no-trending, up-trending or down-trending:<br/>
	 * <ul>
	 * <li>up-trending (in the part of the day when people are going up): The event will mostly be: going upwards,
	 * starting from the ground floor.</li>
	 * <li>down-trending (in the part of the day when people are going down): The event will mostly be: going downwards,
	 * stopping at the ground floor.</li>
	 * </ul>
	 * Nonetheless, any kind of event can show up, but with a smaller probability.
	 *
	 * @param startTime the start time
	 * @param intervalDuration the interval duration
	 * @param trend the trend of the event
	 * @return the event
	 */
	private ScenarioEvent generateEventWithTrend(int startTime, int intervalDuration, Trend trend)
	{
		//Init
		ScenarioEvent event=new ScenarioEvent();
		boolean trendUp;

		//no Trend
		if(trend==Trend.None)
			if(rand.nextDouble()>=0.5)
				trendUp=true;
			else
				trendUp=false;
		//Trend is down
		else if(trend==Trend.Down)
			if(rand.nextDouble()<PROB_FOLLOW_TREND)
				trendUp=false;
			else
				trendUp=true;
		//Trend is up
		else
			if(rand.nextDouble()<PROB_FOLLOW_TREND)
				trendUp=true;
			else
				trendUp=false;
		
		//If trend is up
		if(trendUp)
		{
			//start floor
			if(rand.nextDouble()<PROB_USE_MIN_FLOOR)
				event.startFloor=MIN_FLOOR;
			else
				//to exclude generating ground floor again and generating the top floor
				event.startFloor=MIN_FLOOR+1+rand.nextInt(FLOOR_COUNT-2);
			
			//end floor
			event.stopFloor=event.startFloor+1+rand.nextInt(MAX_FLOOR-event.startFloor);
		}
		else //going down
		{
			//stop floor
			if(rand.nextDouble()<PROB_USE_MIN_FLOOR)
				event.stopFloor=MIN_FLOOR;
			else
				//to exclude generating ground floor again and generating the top floor
				event.stopFloor=MIN_FLOOR+1+rand.nextInt(FLOOR_COUNT-2);
			
			//start floor
			event.startFloor=event.stopFloor+1+rand.nextInt(MAX_FLOOR-event.stopFloor);
		}
		
		//time
		event.time=startTime+rand.nextInt(intervalDuration);				
		return event;
	}
	
	/**
	 * Generates {@count} {@link ScenarioEvent}s that are basically no-trending, up-trending or down-trending:<br/>
	 * <ul>
	 * <li>up-trending (in the part of the day when people are going up): The event will mostly be: going upwards,
	 * starting from the groScenarioGenerator.FLOOR_COUNT*
			Math.pow(2, ScenarioGenerator.FLOOR_COUNT)*
			Math.pow(ScenarioGenerator.FLOOR_COUNT, ScenarioGenerator.ELEVATOR_CAPACITY)*
			Math.pow(ScenarioGenerator.FLOOR_COUNT, ScenarioGenerator.ELEVATOR_CAPACITY));und floor.</li>
	 * <li>down-trending (in the part of the day when people are going down): The event will mostly be: going downwards,
	 * stopping at the ground floor.</li>
	 * </ul>
	 * Nonetheless, any kind of event can show up, but with a smaller probability.
	 *
	 * @param startTime the start time
	 * @param intervalDuration the interval duration
	 * @param trend the trend of the event
	 * @param count the number of events
	 * @return the event
	 */
	public ArrayList<ScenarioEvent> generateEventsWithTrend(int startTime, int intervalDuration, Trend trend, int count)
	{
		ArrayList<ScenarioEvent> events=new ArrayList<ScenarioEvent>(count);
		
		for(int i=0;i<count;i++)
			events.add(this.generateEventWithTrend(startTime, intervalDuration, trend));
		
		return events;
	}
	
	/**
	 * Generate a scenario day.
	 *
	 * @param startTime the start time
	 * @return the array list
	 */
	public ArrayList<ScenarioEvent> generateScenarioDay(int startTime)
	{
		ArrayList<ScenarioEvent> events=new ArrayList<ScenarioEvent>();
		
		//Night
		events.addAll(generateEventsWithTrend(startTime+0, 60*6, Trend.None, 30+rand.nextInt(6)));
		//Morning
		events.addAll(generateEventsWithTrend(startTime+60*6, 60*1, Trend.Up, 10+rand.nextInt(3)));
		events.addAll(generateEventsWithTrend(startTime+60*7, 60*1, Trend.Up, 30+rand.nextInt(5)));
		events.addAll(generateEventsWithTrend(startTime+60*8, 60*1, Trend.Up, 40+rand.nextInt(10)));
		events.addAll(generateEventsWithTrend(startTime+60*9, 60*1, Trend.Up, 35+rand.nextInt(7)));
		events.addAll(generateEventsWithTrend(startTime+60*10, 60*2, Trend.Up, 50+rand.nextInt(6)));
		//Noon
		events.addAll(generateEventsWithTrend(startTime+60*12, 60*3, Trend.None, 100+rand.nextInt(10)));
		//After-noon
		events.addAll(generateEventsWithTrend(startTime+60*15, 60*1, Trend.Down, 25+rand.nextInt(6)));
		events.addAll(generateEventsWithTrend(startTime+60*16, 60*1, Trend.Down, 35+rand.nextInt(8)));
		events.addAll(generateEventsWithTrend(startTime+60*17, 60*1, Trend.Down, 30+rand.nextInt(10)));
		events.addAll(generateEventsWithTrend(startTime+60*18, 60*2, Trend.Down, 35+rand.nextInt(5)));
		//Evening
		events.addAll(generateEventsWithTrend(startTime+60*20, 60*4, Trend.None, 35+rand.nextInt(6)));
		
		
		return events;
	}
	
	/**
	 * Generate count identical scenario days. The events are sorted.
	 *
	 * @param count the count
	 * @return the array list of events
	 */
	public ArrayList<ScenarioEvent> generateScenarioIdenticalDays(int count)
	{
		ArrayList<ScenarioEvent> events=new ArrayList<ScenarioEvent>();
		ArrayList<ScenarioEvent> dayEvents=this.generateScenarioDay(0);
		Collections.sort(dayEvents);
		
		for(int i=0;i<count;i++)
		{
			for(ScenarioEvent ev:dayEvents)
				events.add(ev.getCopyAt(i*DAY_DURATION));				
		}
	
		return events;
	}
}
