import junit.framework.TestCase;
import ml.engine.State;

import org.junit.Test;


public class Tst extends TestCase  {

	@Test
	public void testElevatorFloor() {
		State st=new State();
		st.setElevator1Floor(2);
		st.setElevator2Floor(3);
		
		assertEquals(st.getElevator1Floor(), 2);
		assertEquals(st.getElevator2Floor(), 3);

	}

}
