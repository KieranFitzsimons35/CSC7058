/**
 * 
 */
package controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.citigroup.liquifi.autopilot.bootstrap.ApplicationContext;
import com.citigroup.liquifi.autopilot.bootstrap.AutoPilotBootstrap;
import com.citigroup.liquifi.autopilot.controller.ValidationObject;
import com.citigroup.liquifi.entities.LFLabel;
import com.citigroup.liquifi.entities.LFTestCase;
import com.citigroup.liquifi.util.DBUtil;

/**
 * @author kf67095
 *
 */
public class RunPanelControllerTest {

	// test data
	RunPanelController rpc;
	LoginController lgc;
	LFTestCase testcase1;
	LFTestCase testcase2;
	List<LFTestCase> testcaseList;
	List<ValidationObject> validationObjectList;
	List<LFLabel> allLabelsList;
	List<String> expectedStringList;
	List<String> actualStringList;
	String message, testcaseId, expected, actual;
	String[] label = { "TTIOI", "testing" };
	private String[] expectedArray;
	private String[] actualArray;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// intialise data
		message = "confirmation of results";
		testcaseId = "2457187735138";

		// instantiate Lists
		testcaseList = new ArrayList<LFTestCase>();

		// create class
		rpc = new RunPanelController();
	}

	@Test
	public void testGetSetConfirmationMsg() {
		expected = message;
		rpc.setConfirmationMsg(expected);
		actual = rpc.getConfirmationMsg();
		assertEquals(expected, actual);
	}

	@Test
	public void testStopBooleanValueFalse() {
		rpc.setStop(false);
		assertFalse(rpc.isStop());
	}

	@Test
	public void testStopBooleanValueTrue() {
		rpc.setStop(true);
		assertTrue(rpc.isStop());
	}

	@Test
	public void testGetSetLabel() {
		expectedArray = label;
		rpc.setLabel(label);
		actualArray = rpc.getLabel();
		assertThat(expectedArray, is(actualArray));
	}

	@Test
	public void testStopTestcasesBoolean() {
		// check if the method sets boolean to true
		rpc.stopTestcases();
		assertTrue(rpc.isStop());
	}

	@Test
	public void testStopTestcasesString() {
		// check if the method returns a string
		expected = rpc.stopTestcases();
		actual = "Testcase run has been stopped by the user. Please wait until messages finish before checking Results table";
		assertEquals(expected, actual);

	}

}
