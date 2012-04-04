package ml.engine;

import java.util.Arrays;

import ml.scenario.ScenarioGenerator;

/**
 * The Class State.
 */
public class State {
	
	/** The Constant HERE. */
	public static final int CURRENT=0;
	
	/** The Constant ABOVE. */
	public static final int ABOVE=1;
	
	/** The Constant BELOW. */
	public static final int BELOW=2;
	
	/** The Constant UP. */
	public static final int UP=0;
	
	/** The Constant DOWN. */
	public static final int DOWN=0;
	
	/** The Constant STATE_SPACE_SIZE. */
	public static final int STATE_SPACE_SIZE=
			(int) (ScenarioGenerator.FLOOR_COUNT*
					ScenarioGenerator.FLOOR_COUNT*
					Math.pow(2, 10)*
					Math.pow(2, 3)*
					Math.pow(2, 3)*
					12);
							
	/** The elevator1's floor. */
	byte elevator1Floor;
	
	/** The elevator2's floor. */
	byte elevator2Floor;
	
	/** If people are waiting on the floors above, below, current -first dimension- 
	 * and where do they want to go (UP, DOWN) - the second dimension. Relative to the
	 * first elevator's position.  */
	//boolean waiting[][]=new boolean[3][2];

	/** If people are waiting on each of the floor (first dimension), going either
	 * UP or DOWN (second dimension). */
	boolean waiting[][]=new boolean[ScenarioGenerator.FLOOR_COUNT][2];
	
	/**  If people from elevator 1 are going to the floors above, below, current. */
	boolean[] destinationsE1=new boolean[3];

	/**  If people from elevator 2 are going to the floors above, below, current. */
	boolean[] destinationsE2=new boolean[3];
	
	/** The time interval, as a number from 1-12 (2 hour intervals). */
	byte timeInterval;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(destinationsE1);
		result = prime * result + Arrays.hashCode(destinationsE2);
		result = prime * result + elevator1Floor;
		result = prime * result + elevator2Floor;
		result = prime * result + timeInterval;
		result = prime * result + Arrays.hashCode(waiting);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (!Arrays.equals(destinationsE1, other.destinationsE1))
			return false;
		if (!Arrays.equals(destinationsE2, other.destinationsE2))
			return false;
		if (elevator1Floor != other.elevator1Floor)
			return false;
		if (elevator2Floor != other.elevator2Floor)
			return false;
		if (timeInterval != other.timeInterval)
			return false;
		if (!Arrays.equals(waiting, other.waiting))
			return false;
		return true;
	}


	
}
