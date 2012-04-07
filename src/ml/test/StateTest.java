/**
 * 
 */
package ml.test;

import ml.engine.State;

import org.junit.Test;

/**
 * @author cosmin
 *
 */
public class StateTest {

	/**
	 * Test method for {@link ml.engine.State#getDestinationsE1(int)}.
	 */
	@Test
	public void testGetDestinations() {
		State state=new State();
		state.setDestinationE1(State.ABOVE, true);
		state.setDestinationE1(State.BELOW, true);
		assert(state.getDestinationsE1(State.ABOVE));
	}


}
