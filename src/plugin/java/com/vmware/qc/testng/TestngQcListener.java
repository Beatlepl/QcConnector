/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.testng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.vmware.qc.PostResult2Qc;
import com.vmware.qc.QcConfigDataHandler;
import com.vmware.qc.QcConnector;
import com.vmware.qc.QcConstants;
import com.vmware.qc.QcTestStatus;
import com.vmware.qc.QcUtil;
import com.vmware.qc.TestInstanceInfo;
import com.vmware.qc.TestRunInfo;

/**
 * TestNGListener class for QC is to collect test result & log file after each test execution and
 * post them into HPQC using QcConnector & PostResult2Qc. This is achieved by the following steps,
 *  i) Read testsets from QC and map the testset that matches to current suite.
 *  ii) Read test instances from QC for the testset mapped to current suite.
 *  iii) Map the test instance that matches to the current test.
 *  iv) Passes test result information to PostResult2Qc using test instance id.
 *  v) PostResult2Qc reads the test result and post it to QC using QcCOnnector.
 */
public class TestngQcListener implements ISuiteListener, IInvokedMethodListener
{
   private final QcConnector qcConnector = new QcConnector();
   private final PostResult2Qc postResult2Qc = PostResult2Qc.getInstance();
   private final Configuration configData = QcConfigDataHandler.getConfigDataHandler().getConfigData();
   private final Map<Object, List<ITestResult>> testResults = new Hashtable<Object, List<ITestResult>>();
   private final Map<Integer, QcTestStatus> testngQcStatusValues = new HashMap<Integer, QcTestStatus>();
   private List<TestInstanceInfo> testInstances;
   private long qcTestSetId;
   private String qcTestSetName = null;
   private boolean enableTestLogUpload = false;
   private boolean uploadAllTestLogs = false;
   private static final Logger log = LoggerFactory.getLogger(TestngQcListener.class);

   public TestngQcListener()
   {
      initialize();
      postResult2Qc.start();
   }

   /**
    * Initialize objects.
    */
   private void initialize()
   {
      if (!QcUtil.isEmpty(configData.getString("qc.testset.id"))) {
         this.qcTestSetId = configData.getLong("qc.testset.id") ;
      }
      if (!QcUtil.isEmpty(configData.getString("qc.testset.name"))) {
         this.qcTestSetName = configData.getString("qc.testset.name") ;
      }
      this.enableTestLogUpload = configData.getBoolean("qc.testlog.upload.enable");
      this.uploadAllTestLogs = configData.getBoolean("qc.upload.testlog.all");

      //map testng & qc test result constants.
      testngQcStatusValues.put(ITestResult.FAILURE, QcTestStatus.FAILED);
      testngQcStatusValues.put(ITestResult.SKIP, QcTestStatus.NOT_COMPLETED);
      testngQcStatusValues.put(ITestResult.SUCCESS, QcTestStatus.PASSED);
   }

   /**
    * Maps the suite to testset name in QC.
    * If testset name is sent via command line or config file, return testset name.
    * else return suite name as testset name.
    *
    * @param suite testng suite.
    * @return mapped testset name.
    */
   public String map2QcTestSetName(ISuite suite)
   {
      return (this.qcTestSetName != null ? this.qcTestSetName : suite.getName());
   }

   /**
    * Extracts test name from test result and map it to QC test name.
    *
    * @param res testng result.
    * @return testname that is mapped to QC testname.
    */
   public String map2QcTestName(ITestResult res)
   {
      String methodName = res.getMethod().getMethodName();
      String testName = res.getTestClass().getName();
      String qcTestName = ("test".equals(methodName) ? testName : testName
               + "-" + methodName); //TestGroup method.
      return qcTestName;
   }

   /**
    * Extracts config dataset id from test result and map it to test instance name in QC.
    *
    * @param res testng result.
    * @return test instance name that is mapped to QC test instance name.
    */
   public String map2QcTestInstanceName(ITestResult res)
   {
      String qcTestInstanceName = null;
      if (res.getTestName() != null) { //DD test that is in [test name]-[data id] format.
         String testInstanceName = res.getTestName();
         qcTestInstanceName = testInstanceName.substring(testInstanceName.indexOf("-") + 1);
      }
      return qcTestInstanceName;
   }

   /**
    * Gets list of testlog files that are uploaded.
    *
    * @param res testng result.
    * @return list of testlog files
    */
   public List<String> getUploadLogFiles(ITestResult res)
   {
      List<String> testLogFiles = null;
      if (this.enableTestLogUpload) {
         if (this.uploadAllTestLogs || res.getStatus() != ITestResult.SUCCESS) {
            String testLogFile = (res.getTestName() != null ? res.getTestName()
                     : res.getTestClass().getName()) + ".log";
            testLogFiles = Arrays.asList(testLogFile);
         }
      }
      return testLogFiles;
   }

   /**
    * Populates test instances for a set of testsets.
    *
    * @param testsetIds a set of testset ids.
    * @return list of test instances associated with the testsets.
    */
   private List<TestInstanceInfo> populateTestInstances(List<Long> testsetIds)
   {
      try {
         if (testsetIds != null && !testsetIds.isEmpty()) {
            testInstances = qcConnector.getTestInstances(testsetIds);
            if (testInstances != null && !testInstances.isEmpty()) {
               log.info("Got test instances for testset :" + testsetIds);
            } else {
               log.warn("No test instances found for testset :" + testsetIds);
            }
         }
      } catch (Exception ex) {
         log.error("Got an exception while fetching testInstances from QC for testset :"
                           + testsetIds, ex);
      }
      return testInstances;
   }

   /**
    * Populate testset ids for the suite or testset id/name sent via command line or config file.
    *
    * @param suite current suite.
    * @return testset ids.
    */
   private List<Long> populateTestSetIds(ISuite suite)
   {
      List<Long> testSetIds = null;
      try {
         if (this.qcTestSetId > 0) { //testset id sent via command line or config file.
            testSetIds = Arrays.asList(this.qcTestSetId);
         } else {
            String testsetName = map2QcTestSetName(suite);
            if (testsetName != null) {
               long testSetId = qcConnector.getTestSetId(
                        QcConstants.QC_TESTSETFOLDER_PATH, testsetName);
               if (testSetId > 0) {
                  log.info("Got testset for suite {} : {}", testsetName,
                           testSetId);
                  testSetIds = Arrays.asList(testSetId);
               } else {
                  log.warn("No testset found for suite :");
               }
            } else {
               Map<Long, String> testsets = qcConnector.getTestSets(QcConstants.QC_TESTSETFOLDER_PATH);
               if (testsets != null && !testsets.isEmpty()) {
                  testSetIds = Arrays.asList(testsets.keySet().toArray(
                           new Long[0]));
                  log.info("Got testsets under specified folder {} : {}",
                           QcConstants.QC_TESTSETFOLDER_PATH, testSetIds);
               } else {
                  log.warn("No testsets found under testset folder :"
                           + QcConstants.QC_TESTSETFOLDER_PATH);
               }
            }
         }
      } catch (Exception ex) {
         log.error("Got an exception while fetching testSetId from QC :", ex);
      }
      return testSetIds;
   }

   /**
    * This method is invoked before the test suite starts.
    * This method caches test instances from QC for the current suite.
    */
   public void onStart(ISuite suite)
   {
      List<Long> testSetIds = populateTestSetIds(suite);
      if (testSetIds != null && !testSetIds.isEmpty()) {
         this.testInstances = populateTestInstances(testSetIds);
      }
   }

   public void onFinish(ISuite suite)
   {
   }

   /**
    * This method is invoked before the each testng method invoked.
    * This method collects all test results and test logs associated with a test and
    * adds them to a processing queue.
    */
   public void afterInvocation(IInvokedMethod m,
                               ITestResult tr)
   {

      ITestNGMethod currMethod = m.getTestMethod();
      Object testInstance = tr.getInstance();
      //Collect all test results (of setup/test/cleanup) associated with a test
      if (currMethod.isBeforeMethodConfiguration() || currMethod.isTest()
               || currMethod.isAfterMethodConfiguration()) {
         List<ITestResult> results = null;
         if (testResults.containsKey(testInstance)) {
            results = testResults.get(testInstance);
         } else {
            results = new ArrayList<ITestResult>();
         }
         results.add(tr);
         testResults.put(testInstance, results);
      } else if (currMethod.isAfterClassConfiguration()) {
         ITestNGMethod[] afterClassMethods = currMethod.getTestClass().getAfterClassMethods();
         // At the end of the test, add test logs and the collected test results
         // to the processing queue.
         if (currMethod.getMethodName().equals(
                  afterClassMethods[afterClassMethods.length - 1].getMethodName())) {
            //initialize the values.
            ITestResult setupResult = null;
            ITestResult testResult = null;
            ITestResult cleanupResult = null;
            TestRunInfo testRunInfo = null;
            List<ITestResult> results = testResults.get(testInstance);
            testResults.remove(testInstance);
            for(ITestResult result : results) {
               ITestNGMethod method = result.getMethod();
               if (method.isBeforeMethodConfiguration()) {
                  setupResult = result;
               } else if (method.isTest()) {
                  testResult = result;
               } else if (method.isAfterMethodConfiguration()) {
                  cleanupResult = result;
                  int status = combineTestStatus(
                           (setupResult != null ? setupResult.getStatus()
                                    : ITestResult.FAILURE),
                           testResult.getStatus(), cleanupResult.getStatus());
                  //Convert test results to QC test run and add to processing queue.
                  testRunInfo = map2QcTestRun(testResult, status);
                  log.info("Test run info :" + testRunInfo);
                  if (testRunInfo != null) {
                     postResult2Qc.addToQueue(testRunInfo);
                  }

                  //reset the values.
                  setupResult = null;
                  testResult = null;
                  cleanupResult = null;
                  testRunInfo = null;
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void beforeInvocation(IInvokedMethod method, ITestResult tr)
   {
   }

   /**
    * Combines the status of beforeMethod, testMethod and afterMethod runs and returns final status.
    */
   private int combineTestStatus(int beforeMethodStatus,
                                 int testMethodStatus,
                                 int afterMethodStatus)
   {
      int finalStatus = ITestResult.SKIP;
      if (beforeMethodStatus == ITestResult.SUCCESS
               && testMethodStatus == ITestResult.SUCCESS
               && afterMethodStatus == ITestResult.SUCCESS) {
         finalStatus = ITestResult.SUCCESS;
      } else if ((testMethodStatus == ITestResult.SUCCESS && afterMethodStatus == ITestResult.FAILURE)
               || (testMethodStatus == ITestResult.FAILURE && afterMethodStatus == ITestResult.SUCCESS)) {
         finalStatus = ITestResult.FAILURE;
      }
      return finalStatus;
   }

   /**
    * Map the result of a test to QC test run that is sent to processing queue.
    *
    * @param testMethodResult test method result.
    * @param finalStatus combined status of setup/test/cleanup methods.
    * @return qc test run object.
    */
   private TestRunInfo map2QcTestRun(ITestResult testMethodResult,
                                     int finalStatus)
   {
      TestRunInfo testRunInfo = null;
      String qcTestName = map2QcTestName(testMethodResult);
      String qcTestInstanceName = map2QcTestInstanceName(testMethodResult);
      List<String> logFiles = getUploadLogFiles(testMethodResult);
      QcTestStatus qcStatus = null;
      if (testngQcStatusValues.containsKey(finalStatus)) {
         qcStatus = testngQcStatusValues.get(finalStatus);
      } else {
         log.warn("Unable to match testng result to QC :{}, assigning default QC status FAILED",
                  finalStatus);
         qcStatus = QcTestStatus.FAILED;
      }
      TestInstanceInfo testInstanceInfo = QcUtil.findTestInstance(
               this.testInstances, qcTestName, qcTestInstanceName, null);
      if (testInstanceInfo != null) {
         log.info("Test instance id for test {}: {}", qcTestName,
                  testInstanceInfo.getId());
         testRunInfo = new TestRunInfo();
         testRunInfo.setTestInstanceId(testInstanceInfo.getId());
         testRunInfo.setStatus(qcStatus);
         testRunInfo.setClientLogFilePaths(logFiles);
         testRunInfo.setUserId(QcConstants.QC_USERID);
         testRunInfo.setBuildNumbers(QcConstants.QC_BUILD_NUMBERS);
      } else {
         log.error("No test instance found for test :" + qcTestName);
      }
      return testRunInfo;
   }

}
