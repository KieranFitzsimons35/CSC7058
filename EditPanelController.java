/*package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import com.citigroup.liquifi.autopilot.controller.TestCaseController;
import com.citigroup.liquifi.autopilot.controller.ValidationObject;
import com.citigroup.liquifi.autopilot.logger.AceLogger;
import com.citigroup.liquifi.entities.LFTag;
import com.citigroup.liquifi.entities.LFTemplate;
import com.citigroup.liquifi.entities.LFTestCase;
import com.citigroup.liquifi.entities.LFTestInputSteps;
import com.citigroup.liquifi.util.DBUtil;

@EnableAutoConfiguration
@Controller
@RequestMapping("editPanel")
public class EditPanelController {

	protected final static AceLogger logger = AceLogger.getLogger("EditPanelController");
	*/
	
	//Only the code that has not been commentd out was created by myself. //This code and the showTestCaseID method below are used to take the //testcaseID from the popup form in RunPanel
	//and use it to help populate the fields in EditPanel page of the app.
	
	//The String and LFTestcase object were added to this class along with getters and setters
	
	private static String testid;
	private static LFTestCase test;
	
	
	public static LFTestCase getTest() {
		return test;
	}

	public static void setTest(LFTestCase test) {
		EditPanelController.test = test;

	}

	public static String getTestid() {
		return testid;
	}

	public void setTestid(String testid) {
		EditPanelController.testid = testid;
	}

	/**
	*This method takes the testcaseID that was passed in the form after the *double click event 
	* in the Run Panel table. It is using the string in the input form to *set the String object in this class and then set the LFTestcase Object *in this class. The final part of the method redirects to the Edit Panel *page on the web app.
	*/
	@RequestMapping("/selectedTestCaseID")
	@ResponseBody
	public void showTestCaseID(HttpServletRequest request, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");

		String id;

		id = request.getParameter("selectedTestcase").trim();

		setTestid(id);

		setTest(DBUtil.getInstance().getTcm().getTestCase(getTestid()));

		response.sendRedirect("http://localhost:8080/#/editPanel/");

	}
/*
	@RequestMapping("/testObject")
	@ResponseBody
	public List<List> testObject() {

		List<List> listOfLists = new ArrayList<>();
		List<LFTestCase> testList = new ArrayList<>();
		List<LFTemplate> templateList = new ArrayList<>();
		Map<String, LFTemplate> templateMap = DBUtil.getInstance().getTem().getAllTemplateMap();
		List<LFTestInputSteps> list = test.getInputStep();
		List<LFTag> tagList = new ArrayList<>();

		try {

			LFTestCase testCase = test.clone(test.getTestID());

			for (int loop1 = 0; loop1 < test.getInputStepList().size(); loop1++) {
				for (int loop2 = 0; loop2 < test.getInputStepList().get(loop1).getInputTagsValueList()
						.size(); loop2++) {
					tagList.add(test.getInputStepList().get(loop1).getInputTagsValueList().get(loop2));
				}
			}

			testCase.setInputStepList(null);
			testCase.setInputStep(null);
			testCase.setLabelnames(null);
			testCase.setLabelSet(null);

			testList.add(testCase);
			templateList.addAll(templateMap.values());

			listOfLists.add(test.getInputStepList());
			listOfLists.add(testList);
			listOfLists.add(templateList);
			listOfLists.add(tagList);

		} catch (Exception e) {
			System.out.println("something wrong with how lists populating");
			listOfLists.size();
		}

		return listOfLists;

	}


	public static LFTestCase grabTestCase(){
		
		
		System.out.println("Instance TestID : " + DBUtil.getInstance().getTcm().getTestCase(getTestid()).getTestID());
		System.out.println("Object TestID : " + getTestid());
		
		return DBUtil.getInstance().getTcm().getTestCase(getTestid());
		
		
	}
	@RequestMapping("/updateTestcaseDetails")
	@ResponseBody
	public String updateTestCaseDetails(@RequestBody LFTestCase testCaseUpdated) {

		System.out.println(testCaseUpdated.getTestID());
		System.out.println(testCaseUpdated.getDescription());
		System.out.println(testCaseUpdated.getAppName());
		System.out.println(testCaseUpdated.getRegion());

		// insertIntoDB(testCaseUpdated);
		
		return System.getProperty("user.name");
	}
	
	public  void setValidationObject(ValidationObject result){
		
		EditPanelController.result = result;
		
	}
	
	public static ValidationObject getValidationObject(){
		
		return result; 
	}

	@RequestMapping("/starttestcase")
	@ResponseBody
	public String[] runTestCase(String symbolStr, boolean isBenchMarkModeOn, boolean canTakeItSlow) {
		LFTestCase testCase = DBUtil.getInstance().getTcm().getTestCase(getTestid());
		ValidationObject testResult = TestCaseController.INSTANCE.runTestCase(testCase, symbolStr, canTakeItSlow,
				isBenchMarkModeOn);
		logger.info("running test caseeeeeeee");
		
		setValidationObject(testResult);
		
		String passedFailed = "ERROR", details = "ERROR";

		if (testResult.isSuccess() == true) {

			passedFailed = "Passed";
			details = "TestCase Passed?" + testResult.isSuccess();

		}

		if (testResult.isSuccess() == false) {

			passedFailed = "Failed";
			details = "Reason: " + testResult.getValidationResultMsg() + " | FailedOutputID: "
					+ testResult.getFailedOutputMsgID() + " | FailedInputStep:" + testResult.getFailedInputStep();

		}

		String[] status = { passedFailed, details };

		return status;
	}
*/
}
