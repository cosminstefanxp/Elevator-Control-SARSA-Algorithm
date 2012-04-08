package ml.scenario;

/**
 * The Class ScenarioEvent.
 */
public class ScenarioEvent implements Comparable<ScenarioEvent>{
	
	/** The time of the event. */
	public int time;
	
	/** The start floor of the passenger. */
	public int startFloor;
	
	/** The destination floor of the passenger. */
	public int stopFloor;

	/**
	 * Instantiates a new scenario event.
	 *
	 * @param time the time
	 * @param startFloor the start floor
	 * @param stopFloor the stop floor
	 */
	public ScenarioEvent(int time, int startFloor, int stopFloor) {
		super();
		this.time = time;
		this.startFloor = startFloor;
		this.stopFloor = stopFloor;
	}

	@Override
	public String toString() {
		return "ScenarioEvent [t=" + time + ", " + startFloor
				+ " -> " + stopFloor + "]";
	}

	/**
	 * Instantiates a new scenario event.
	 */
	public ScenarioEvent() {
		super();
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ScenarioEvent o) {
		return this.time-o.time;
	}
	
	/**
	 * Gets a copy of the event, with the time moved with baseTime units.
	 *
	 * @param ev the event
	 * @param baseTime the base time
	 * @return the copy
	 */
	public ScenarioEvent getCopyAt(int baseTime)
	{
		return new ScenarioEvent(this.time+baseTime, this.startFloor, this.stopFloor);
	}
	
	
}
