package ml.engine;

import java.util.ArrayList;

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

	/** The events. */
	private ArrayList<ScenarioEvent> events;
	
	/** The sg. */
	private ScenarioGenerator sg;
	
	/** The Constant log. */
	private static final Logger log=Logger.getLogger(World.class);
	
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
		events=sg.generateScenarioDay(0);
		log.info("Generated scenario with "+events.size()+" events.");
		
	}
	
	public static void main(String[] args)
	{
		World world = new World();
		Engine engine=new Engine(world);
	}
}
