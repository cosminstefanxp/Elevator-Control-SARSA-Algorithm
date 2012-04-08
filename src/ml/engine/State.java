/*
 * Stefan-Dobrin Cosmin
 * 342C4
 * 
 * Invatare Automata
 * 2012
 */
package ml.engine;

import ml.scenario.ScenarioGenerator;

/**
 * The Class State.
 */
public class State {
	
	/** The Constant HERE. */
	public static final int CURRENT=1;
	
	/** The Constant ABOVE. */
	public static final int ABOVE=2;
	
	/** The Constant BELOW. */
	public static final int BELOW=0;
	
	/** The Constant UP. */
	public static final int UP=1;
	
	/** The Constant DOWN. */
	public static final int DOWN=0;
	
	/** The Constant DEST_E1_BIT for bitwise operations on {@literal value}. */
	private static final int DEST_E1_BIT=0;
	
	/** The Constant DEST_E2_BIT for bitwise operations on {@literal value}. */
	private static final int DEST_E2_BIT=3;
	
	/** The Constant WAITING_BIT for bitwise operations on {@literal value}. */
	private static final int WAITING_BIT=6;
	
	/** The Constant WMANY_BIT bitwise operations on {@literal waitingMany}. */
	private static final int WMANY_BIT=0;

	/** The Constant STATE_SPACE_SIZE. */
	public static final int STATE_SPACE_SIZE=
			(int) (ScenarioGenerator.FLOOR_COUNT*
					ScenarioGenerator.FLOOR_COUNT*
					Math.pow(2, 10)*
					Math.pow(2, 3)*
					Math.pow(2, 3)*
					6);
							
	/** The elevators' floors. In the first 4 bits, the floor for E1 is stored,
	 * and in the next 4 bits, the floor for E2 is stored. */
	private byte elevatorFloor;
	
	/** Some boolean values are being stored as bits, in the following field. They can be accessed using
	 * the constants defined: 
	 * <ul>
	 * <li> WAITING_BIT - If people are waiting on each of the floor (first dimension), going either
	 * UP or DOWN (second dimension). Access: floor*2+direction</li>
	 * <li> DEST_E1_BIT - If people from elevator 1 are going to the floors above, below, current. Access: +direction </li>
	 * <li> DEST_E2_BIT -  If people from elevator 2 are going to the floors above, below, current. Access: +direction </li>
	 * </ul>*/
	private short value;
	
	/** The time interval, as a number from 0-11 (2 hour intervals). */
	private byte timeInterval;
	
	
	/** BONUS - Byte in which we store a bit value which says if there are more than WAITING_THRESHOLD people
	 * waiting in front of the elevator. Same structure as value above, but with the constant WMANY_BIT */
	//private short waitingMany;

	/**
	 * Sets the bit.
	 *
	 * @param bit the new bit
	 */
	private void setBit(int bit)
	{
		value |= 1 << bit;
	}
	
	/**
	 * Clear bit.
	 *
	 * @param bit the bit
	 */
	private void clearBit(int bit)
	{
		value &= ~(1 << bit);
	}
	
	/**
	 * Gets the bit.
	 *
	 * @param bit the bit
	 * @return the bit
	 */
	private boolean getBit(int bit)
	{
		return (value & (1 << bit)) != 0;
	}
	
	/**
	 * Gets the elevator1 floor.
	 *
	 * @return the elevator1 floor
	 */
	public int getElevator1Floor() {
		return elevatorFloor & 0x0f;
	}

	/**
	 * Sets the elevator1 floor.
	 *
	 * @param elevator1Floor the new elevator1 floor
	 */
	public void setElevator1Floor(int elevator1Floor) {
		this.elevatorFloor = (byte) ((this.elevatorFloor & 0xf0) + elevator1Floor);
	}

	/**
	 * Gets the elevator2 floor.
	 *
	 * @return the elevator2 floor
	 */
	public int getElevator2Floor() {
		return ((elevatorFloor & 0xf0)>>4);
	}

	/**
	 * Sets the elevator2 floor.
	 *
	 * @param elevator2Floor the new elevator2 floor
	 */
	public void setElevator2Floor(int elevator2Floor) {
		this.elevatorFloor = (byte) ((this.elevatorFloor & 0x0f) + (elevator2Floor<<4));
	}

	
	/**
	 * Gets the waiting many info. If more than WAITING_THRESHOLD people are waiting on the 
	 * {@code floor} for going in the {@code direction}.
	 *
	 * @param floor the floor
	 * @param direction the direction
	 * @return true, if anyone waiting
	 */
	public boolean getWaitingMany(int floor, int direction) {
		return getBit(WMANY_BIT + floor*2+direction);
	}

	/**
	 * Sets the many waiting. If more than WAITING_THRESHOLD people are waiting on the 
	 * {@code floor} for going in the {@code direction}.
	 * @param floor the floor
	 * @param direction the direction
	 * @param waiting the waiting
	 */
	public void setWaitingMany(int floor, int direction, boolean waiting) {
		if(waiting)
			setBit(WMANY_BIT + floor*2+direction);
		else
			clearBit(WMANY_BIT + floor*2+direction);
	}

	
	
	
	/**
	 * Gets the waiting info. If people are waiting on the {@code floor} for going in the {@code direction}.
	 *
	 * @param floor the floor
	 * @param direction the direction
	 * @return true, if anyone waiting
	 */
	public boolean getWaiting(int floor, int direction) {
		return getBit(WAITING_BIT + floor*2+direction);
	}

	/**
	 * Sets the waiting. If people are waiting on the {@code floor} for going in the {@code direction}.
	 *
	 * @param floor the floor
	 * @param direction the direction
	 * @param waiting the waiting
	 */
	public void setWaiting(int floor, int direction, boolean waiting) {
		if(waiting)
			setBit(WAITING_BIT + floor*2+direction);
		else
			clearBit(WAITING_BIT + floor*2+direction);
	}

	/**
	 * Gets the destinations for elevator 1. If people want to go Above, Below or on the current floor.
	 *
	 * @param direction the direction
	 * @return the destinations for elevator 1
	 */
	public boolean getDestinationsE1(int direction) {
		return getBit(DEST_E1_BIT+direction);
	}

	/**
	 * Sets the destination e1.
	 *
	 * @param direction the direction
	 * @param destinationE1 the destination e1
	 */
	public void setDestinationE1(int direction, boolean destinationE1) {
		if(destinationE1)
			setBit(DEST_E1_BIT+direction);
		else
			clearBit(DEST_E1_BIT+direction);
	}

	/**
	 * Gets the destinations for elevator 2. If people want to go Above, Below or on the current floor.
	 *
	 * @param direction the direction
	 * @return the destinations for elevator 2
	 */
	public boolean getDestinationsE2(int direction) {
		return getBit(DEST_E2_BIT+direction);
	}

	/**
	 * Sets the destination for elevator 2.
	 *
	 * @param direction the direction
	 * @param destinationE2 the destination e2
	 */
	public void setDestinationE2(int direction, boolean destinationE2) {
		if(destinationE2)
			setBit(DEST_E2_BIT+direction);
		else
			clearBit(DEST_E2_BIT+direction);
	}

	/**
	 * Gets the time interval.
	 *
	 * @return the time interval
	 */
	public byte getTimeInterval() {
		return timeInterval;
	}

	/**
	 * Sets the time interval.
	 *
	 * @param i the new time interval
	 */
	public void setTimeInterval(int i) {
		this.timeInterval = (byte) i;
	}
	
	/**
	 * Bits to string.
	 *
	 * @param startBit the start bit
	 * @param count the count
	 * @return the string
	 */
	private String bitsToString(int startBit, int count)
	{
		String ret="[";
		
		for(int i=startBit;i<startBit+count;i++)
			ret+=getBit(i)?"T ":"F ";
		
		return ret+"]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder outp=new StringBuilder();
		outp.append("State [" + timeInterval);
		outp.append(", [E1: " + getElevator1Floor() + " - "	+ bitsToString(DEST_E1_BIT, 3));
		outp.append("], [E2: " + getElevator2Floor() + " - "	+ bitsToString(DEST_E2_BIT, 3));
		outp.append("], waiting=[");
		for(int i=0;i<ScenarioGenerator.FLOOR_COUNT;i++)
			outp.append(bitsToString(WAITING_BIT+2*i, 2)+" ");
		outp.append("]");
		return outp.toString();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 67;
		int result = 1;
		result = prime * result + elevatorFloor;
		result = prime * result + timeInterval;
		result = prime * result + value;
		//result = prime * result + waitingMany;
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
		State other = (State) obj;
		if (elevatorFloor != other.elevatorFloor)
			return false;
		if (timeInterval != other.timeInterval)
			return false;
		if (value != other.value)
			return false;
//		if (waitingMany != other.waitingMany)
//			return false;
		return true;
	}

	/**
	 * Instantiates a new state.
	 */
	public State() {
		super();
		this.value=0;
	}
	
	/**
	 * Flush state. Efficient string to be written to file.
	 *
	 * @return the string
	 */
	public String flushState()
	{
		return this.timeInterval+" "+this.elevatorFloor+" "+this.value;
	}

	


	
}
