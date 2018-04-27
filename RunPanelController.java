package controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.UnknownFormatConversionException;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.citigroup.liquifi.autopilot.controller.TestCaseController;
import com.citigroup.liquifi.autopilot.controller.ValidationObject;
import com.citigroup.liquifi.autopilot.logger.AceLogger;
import com.citigroup.liquifi.entities.LFCategory;
import com.citigroup.liquifi.entities.LFLabel;
import com.citigroup.liquifi.entities.LFTestCase;
import com.citigroup.liquifi.util.DBUtil;

import mailController.SendEmail;

@EnableAutoConfiguration
@Controller
@RequestMapping("/runpanel")
public class RunPanelController {

	protected final static AceLogger logger = AceLogger.getLogger("RunPanelController");
	private List<ValidationObject> selectedTestcases = new ArrayList<ValidationObject>();
	private List<LFTestCase> passedList = new ArrayList<LFTestCase>();
	private List<LFTestCase> failedList = new ArrayList<LFTestCase>();
	private ArrayList<ValidationObject> passedCases = new ArrayList<ValidationObject>();
	private ArrayList<ValidationObject> failedCases = new ArrayList<ValidationObject>();
	private List<LFTestCase> dbUtilTestcases = new ArrayList<LFTestCase>();
	public String testcaseid;
	private String[] label;
	private String confirmationMsg;
	private LFTestCase testCase;
	private List<LFTestCase> labeltestcase = new ArrayList<LFTestCase>();
	private boolean stop = false;
	private int pass;
	private int fail;

	public String getConfirmationMsg() {
		return confirmationMsg;
	}

	public void setConfirmationMsg(String confirmationMsg) {
		this.confirmationMsg = confirmationMsg;
	}

	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public LFTestCase getTestCase() {
		return testCase;
	}

	public void setTestCase(LFTestCase testCase) {
		this.testCase = testCase;
	}

	public String getTestcaseid() {
		return testcaseid;
	}

	public void setTestcaseid(String testcaseid) {
		this.testcaseid = testcaseid;
	}

	public String[] getLabel() {
		return label;
	}

	public void setLabel(String[] label) {
		this.label = label;
	}

	/**
	*Method to return all Labels for this instance of AutoPilot
	*/
	@RequestMapping("/labels")
	@ResponseBody
	public List<String> getAllLabels() {

		List<LFLabel> allLabelList = DBUtil.getInstance().getLbm().getLabelsFromDB();

		List<String> labels = new ArrayList<>();
		for (LFLabel s : allLabelList) {
			labels.add(s.getLabel());

		}
		logger.info("Labels for these testcases");

		return labels;

	}

	/**
	 * Retrieves the selected label or labels and then creates a list of test
	 * cases and passes this a parameter for the runTestcasesBatch method
	 * @param request
	 * @param response
	 * @throws IOException
	 * @throws NullPointerException
	 */
	@RequestMapping("/showTestCasesByLabel")
	@ResponseBody
	public void labelProcessing(HttpServletRequest request, HttpServletResponse response)
			throws IOException, NullPointerException {

		response.setContentType("text/html");

		String[] labelName = request.getParameterValues("labelOption");
		setLabel(labelName);
		List<String> list = new ArrayList<String>(Arrays.asList(getLabel()));

		logger.info("Choosing the label(s): " + list.toString());
		List<LFTestCase> testcaselist = new ArrayList<LFTestCase>();
		new ArrayList<String>();
		List<String> testcasebylabel2 = new ArrayList<String>();

		appendTestcaseListByLabel(labelName, testcasebylabel2);

		testcaselist = getTestcaseListByIDs(testcasebylabel2);// pass this appended list
																
		Integer size = testcaselist.size();
		logger.info("This label has " + size.toString() + " testcases");
		logger.info("The first testcase is for AppName: " + testcaselist.get(0).getAppName() + ", testid: "
				+ testcaselist.get(0).getTestID() + " for testcasename: " + testcaselist.get(0).getName());

		runTestcasesBatch(testcaselist);

		setConfirmationMsg("Back in first method");
		response.sendRedirect("http://localhost:8080/#/runpanel/");

	}

	/**Joins two lists of strings together into an amalgamated list
	 * @param labelName String
	 * @param testcasebylabel2 List of type String
	 */
	private void appendTestcaseListByLabel(String[] labelName, List<String> testcasebylabel2) {
		List<String> testcasebyLabel1;
		for (String l : labelName) {

			testcasebyLabel1 = DBUtil.getInstance().getTcm().getTestCaseListByLabel(l);
			testcasebylabel2.addAll(testcasebyLabel1);// create appended list of all testcaseids for all labels
														
		}
														
	}

	/**This method takes a list of strings and returns a list of test case objects
	 * @param testcasebyLabel1 List of String
	 * @return List of test case objects
	 */
	private List<LFTestCase> getTestcaseListByIDs(List<String> testcasebyLabel1) {
		List<LFTestCase> testcaselist = new ArrayList<LFTestCase>();
		for (int x = 0; x < testcasebyLabel1.size(); x++) {
			LFTestCase lftcs = DBUtil.getInstance().getTcm().getTestCase(testcasebyLabel1.get(x));
			testcaselist.add(lftcs);
		}
		return testcaselist;
	}
	/**
	*Method to return useful Label details to the user.Current label running, current testcases that have ran and total
	*number of testcases for the batch.
	*/
	@RequestMapping("/labelName")
	@ResponseBody
	private String selectedLabel() {

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String date = dateFormat.format(cal.getTime());

		String labelName = Arrays.toString(getLabel()) + "  : " + selectedTestcases.size() + " testcase(s) ran from "
				+ labeltestcase.size() + " testcase(s) on: " + date + " ";

		return labelName;

	}

	/**
	*Method to return all testcase details for this inatnce of AutoPilot
	*/
	@RequestMapping("/testcases")
	@ResponseBody
	public List<LFTestCase> getAllTestcases() {
		logger.info("returning all testcases directly from the database");

		List<LFTestCase> allTestcaseDetails = DBUtil.getInstance().getTcm().getAllTestcases();

		List<LFTestCase> smalllist = new ArrayList<>();

		for (LFTestCase tc : allTestcaseDetails) {

			LFTestCase clone = new LFTestCase();

			clone.setAppName(tc.getAppName());
			clone.setTestID(tc.getTestID());
			clone.setName(tc.getName());
			clone.setDescription(tc.getDescription());
			clone.setCategory(tc.getCategory());
			clone.setRegion(tc.getRegion());
			clone.setReleaseNum(tc.getReleaseNum());
			clone.setJiraNum(tc.getJiraNum());
			clone.setLastEditedUser(tc.getLastEditedUser());

			smalllist.add(clone);
		}
		logger.info("The number of testcases being returned for" + smalllist.get(0).getAppName() + "/ "
				+ smalllist.get(0).getRegion() + " are:" + smalllist.size());

		return smalllist;

	}


	@RequestMapping("/labels/count")
	@ResponseBody
	public String getAllLabelsCount() {

		Integer count = getAllLabels().size();

		String labelcount = "The number of unique labels for these testcases is " + Integer.toString(count);

		logger.info("Returning the count of the labels:" + labelcount);
		return labelcount;

	}

	@RequestMapping("/categories")
	@ResponseBody
	public List<LFCategory> getAllCategories() {

		List<LFCategory> Category = DBUtil.getInstance().getCtm().getCategoriesFromDB();

		for (LFCategory c : Category) {
			c.getCategory();

		}

		logger.info("Categories for these testcases");
		return Category;

	}

	@RequestMapping("/categories/count")
	@ResponseBody
	public String getAllCategoriesCount() {

		Integer count = getAllCategories().size();

		String catcount = "The number of unique categories for these testcases is " + Integer.toString(count);
		logger.info("Returning the count for these categories:" + catcount);

		return catcount;

	}

	@RequestMapping("/releases")
	@ResponseBody
	public List<String> getAllReleases() {

		List<String> listOfReleaseNum = DBUtil.getInstance().getCtm().loadReleaseNumFromDB();

		logger.info("release numbers..");
		return listOfReleaseNum;

	}

	@RequestMapping("/jiranums")
	@ResponseBody
	public List<String> getAllJiras() {

		List<String> listOfJiraNum = DBUtil.getInstance().getCtm().loadJiraNumFromDB();

		logger.info("jira numbers...");
		return listOfJiraNum;

	}
	
	/**
	*Method to stop the testcase batch running by breaking from the loop.
	*A message is returned to infomr the user the run has been stopped.
	*/
	@RequestMapping("/stopTestcases")
	@ResponseBody
	public String stopTestcases() {
		setStop(true);
		return "Testcase run has been stopped by the user. Please wait until messages finish before checking Results table";

	}

	/**
	 * This method takes a list of test case objects and iterates over them to run the testcases and validate the results.
	 * Lists are passsed to another method for use in returning data to the user about the results of the testcase run.
	 * @param testcaselist
	 */
	public String runTestcasesBatch(List<LFTestCase> testcaselist) throws NullPointerException {
		setStop(false);
		pass = 0;
		fail = 0;
		selectedTestcases.clear();

		logger.info("Connection Manager intitalising....");

		logger.info("Tibco messaging occurring....");

		logger.info("TotalCaseCount:" + testcaselist.size());

		labeltestcase = testcaselist;

		runTestcaseLoop(testcaselist);

		setConfirmationMsg("Testcases have finished running");// flag to show end of loop

		logger.info("End of for loop for runTestcasesByLabel method. " + "Test cases: " + pass + " passed and " + fail
				+ " failed from " + testcaselist.size() + " testcases.");
		return confirmationMsg;
	}

	/**
	 *Method that loops over lisyt of Testcase objects and runs the testcase while creating a Validation Object 
	 * @param testcaselist
	 */
	private void runTestcaseLoop(List<LFTestCase> testcaselist) {
		for (int i = 0; i < testcaselist.size(); i++) {
			setConfirmationMsg("Testcases are running");// flag to show start of loop
			if (isStop()) {
				break;
			}
			try {
				ValidationObject validationObject = TestCaseController.INSTANCE
						.runPersistedTestCase(testcaselist.get(i).getTestID(), " ", false);

				selectedTestcases.add(validationObject);

				populateResultTables(selectedTestcases);// populate result tables

				testCase = testcaselist.get(i);
												
				setTestCase(testCase);

				if (validationObject.isSuccess()) {
					pass++;

					logger.info("Current pass count:" + pass + " passed out of " + testcaselist.size() + " testcases.");
				} else {
					fail++;

					logger.info("Current fail count:" + fail + " failed out of " + testcaselist.size() + " testcases.");
				}

				Thread.sleep(10000);// pauseTimeBetweenCases wait 10 seconds
			} catch (Exception e) {
				logger.info("Exception being thrown here " + e);
				continue;
			}

		}
	}
	/**
	*Method that returns a running status message.
	*/
	@RequestMapping("/runningStatus")
	@ResponseBody
	private String runningStatus() {

		return getConfirmationMsg();
	}

	/**
	 * Method to create a table for passed and failed tests based on validation
	 * Need further methods to use testcase lists to iterate over and populate
	 * fields and validation objects for info about failure reasons, etc.
	 * 
	 * @param selected
	 */
	private void populateResultTables(List<ValidationObject> selected) {
		if (!passedList.isEmpty()) {
			passedList.clear();
		}
		if (!failedList.isEmpty()) {
			failedList.clear();
		}
		if (!failedCases.isEmpty()) {
			failedCases.clear();
		}
		if (!passedCases.isEmpty()) {
			passedCases.clear();
		}

		for (ValidationObject testcase : selected) {
			if (!testcase.isSuccess()) {
				// Add testcase to a list of testcases for failed 
				failedList.add(testcase.getTestcase());
				failedCases.add(testcase);
			} else {
				// Add testcase to a list of testcases for passed
				passedList.add(testcase.getTestcase());// ArrayList of Testcase objects										
				passedCases.add(testcase);// ArrayList of Validation Objects
			}
		}

		logger.info("Passed Testcase List after testcases ran:" + passedList.toString());
		logger.info("Failed Testcase List after testcases ran:" + failedList.toString());

	}
	/**
	*Method to return the number of failed testcases
	*/
	@RequestMapping("/failedNum")
	@ResponseBody
	private String failedNumber() {

		Integer num = failedList.size();

		return num.toString();

	}
	
	/**
	*Method to return the number of passed testcases
	*/
	@RequestMapping("/passedNum")
	@ResponseBody
	private String passedNumber() {

		Integer num = passedList.size();

		return num.toString();

	}
	
	/**
	*Method to return a list of Testcase objects that have failed
	*during exectuion within the batch run
	*/
	@RequestMapping("/failedList")
	@ResponseBody
	private List<LFTestCase> showFailedTestcaseList() {

		List<LFTestCase> list = new ArrayList<>();

		logger.info(failedList.toString());

		for (LFTestCase tc : failedList) {
			LFTestCase clone = new LFTestCase();
			clone.setCategory(tc.getCategory());
			clone.setDescription(tc.getDescription());
			clone.setName(tc.getName());
			clone.setTestID(tc.getTestID());
			clone.setReleaseNum(tc.getReleaseNum());

			list.add(clone);
		}
		return list;
	}
	/**
	*Method to return a list of Testcase objects that have passed
	*during exectuion within the batch run
	*/
	@RequestMapping("/passedList")
	@ResponseBody
	private List<LFTestCase> showPassedTestcaseList() {

		logger.info(passedList.toString());

		List<LFTestCase> list = new ArrayList<>();

		for (LFTestCase tc : passedList) {
			LFTestCase clone = new LFTestCase();
			clone.setCategory(tc.getCategory());
			clone.setDescription(tc.getDescription());
			clone.setName(tc.getName());
			clone.setTestID(tc.getTestID());
			clone.setReleaseNum(tc.getReleaseNum());

			list.add(clone);
		}
		return list;
	}
	
	/**
	*Method to return a list of steps for failed testcases
	*/
	@RequestMapping("/failedStepList")
	@ResponseBody
	private List<String> failedStepList() {

		List<String> list = new ArrayList<>();

		for (ValidationObject f : failedCases) {

			list.add("Test Case ID : [" + f.getTestcase().getTestID() + "] - Failed on Step: [" + f.getFailedInputStep()
					+ "] ");
		}
		return list;
	}
	/**
	*Method to return a list of validation reasons fro failing testcases
	*/
	@RequestMapping("/failedReasonList")
	@ResponseBody
	private List<String> failedReasonList() {

		List<String> list = new ArrayList<>();

		for (ValidationObject f : failedCases) {

			list.add("TestID: " + f.getTestcase().getTestID() + " - Reason: " + f.getValidationResultMsg() + " ");
		}
		return list;
	}

}