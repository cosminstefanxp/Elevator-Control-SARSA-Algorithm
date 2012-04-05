package ml.engine;

import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * The Class Engine.
 */
public class Engine {
	
	/** The Q. */
	private HashMap<State,Double[]> Q;
	
	/** The world. */
	private World world;
	
	/** The current state. */
	private State currentState;
	
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
	 * Instantiates a new engine.
	 *
	 * @param world the world
	 */
	public Engine(World world, State startState) {
		super();
		configureLogger();
		
		log.info("Initializing engine...");
		log.info("State space size: "+State.STATE_SPACE_SIZE);
		
		//Initialize the elements
		Q=new HashMap<State, Double[]>((int) (State.STATE_SPACE_SIZE));
		this.world=world;
		this.currentState=startState;
		log.info("Start state: "+currentState);

		log.info("Engine initialized");
	}
	
	
}
