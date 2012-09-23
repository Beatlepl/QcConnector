/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.PostResult2Qc;
import com.vmware.qc.QcConnector;
import com.vmware.qc.QcConstants;
import com.vmware.qc.QcTestStatus;
import com.vmware.qc.QcUtil;
import com.vmware.qc.TestInstanceInfo;
import com.vmware.qc.TestRunInfo;

/**
 * This class implements the task of uploading the test results into the QC by offline upload mechanism.
 * It helps determining the valid result records from the result log file and posts the same into QC.
 *
 */
public class PostResultFile2Qc
{
   private final QcConnector connector;
   private final Map<String, Object> cacheTestInstances = new HashMap<String, Object>();
   private final Map<Long, String> cacheAllTestSets = new HashMap<Long, String>();
   public static final Logger log = LoggerFactory.getLogger(PostResultFile2Qc.class);

   public PostResultFile2Qc()
   {
      connector = new QcConnector();
   }

   /**
    * Reads the test result and posts the same into QC using PostResult2Qc.
    *
    * @param resultFile - result log file.
    */
   public void post2Qc(ResultFile resultFile) throws Exception
   {
      List<TestResultData> testResults = resultFile.process();
      if (testResults == null || testResults.isEmpty()) {
         log.error("No test results data available");
         return;
      }
      if (log.isDebugEnabled()) {
         log.debug("List of TestResults data:");
         for(TestResultData resultData : testResults) {
            log.debug(resultData + "");
         }
      }
      List<String> testsetNames = new ArrayList<String>();
      for(TestResultData testResult : testResults) {
         String testsetName = testResult.getTestSetName();
         if (!testsetNames.contains(testsetName)) {
            testsetNames.add(testsetName);
         }
      }
      log.info("List of testset names :" + testsetNames);
      loadTestInstances(testsetNames);
      if (cacheTestInstances.isEmpty()) {
         log.error("No test instances found");
         return;
      }
      int failedCount = 0;
      PostResult2Qc postResult2Qc = PostResult2Qc.getInstance();
      postResult2Qc.start();
      for(TestResultData resultData : testResults) {
         String testName = resultData.getTestName();
         QcTestStatus status = resultData.getTestStatus();
         TestInstanceInfo testInstance = findTestInstance(resultData);
         if (testInstance != null) {
            TestRunInfo testrunInfo = new TestRunInfo();
            testrunInfo.setTestInstanceId(testInstance.getId());
            testrunInfo.setStatus(status);
            testrunInfo.setBugIds(resultData.getBugIds());
            testrunInfo.setUserId(QcConstants.QC_USERID);
            testrunInfo.setBuildNumbers(resultData.getBuildNumbers() == null ? QcConstants.QC_BUILD_NUMBERS
                     : resultData.getBuildNumbers());
            postResult2Qc.addToQueue(testrunInfo);
            log.debug("Testrun info :" + testrunInfo);
         } else {
            log.error("Couldn't find test instance id for test name :" + testName);
            failedCount++;
         }
      }
      postResult2Qc.stop();
      for(Boolean status : postResult2Qc.getTestRunsPostedStatus().values()) {
         if (!status) {
            failedCount++;
         }
      }
      log.info("Total number of valid test result rows :" + testResults.size()
               + ", Number of test results failed to post into QC :" + failedCount);
   }

   /**
    * Load all test instances associated to the given testset names from QC and cache them.
    *
    * @param testsetNames list of testset name.
    */
   private void loadTestInstances(List<String> testsetNames)
                                  throws Exception
   {
      Map<Long, String> allTestsets = connector.getTestSets(QcConstants.QC_TESTSETFOLDER_PATH);
      if (allTestsets != null && !allTestsets.isEmpty()) {
         cacheAllTestSets.putAll(allTestsets);
            List<Long> testsetIds = null;
            if (QcConstants.QC_TESTSET_IDS != null && QcConstants.QC_TESTSET_IDS.length > 0) {
                testsetIds = new ArrayList<Long>(allTestsets.keySet());
            } else {
                testsetIds = QcUtil.getIds(allTestsets, testsetNames);
            }
         if (testsetIds != null && !testsetIds.isEmpty()) {
            List<TestInstanceInfo> testInstances = connector.getTestInstances(testsetIds);
            if (testInstances != null && !testInstances.isEmpty()) {
               for (TestInstanceInfo testInstance : testInstances) {
                  if (!cacheTestInstances.containsKey(testInstance.getTestName())) {
                     cacheTestInstances.put(testInstance.getTestName(),
                              testInstance);
                  } else {
                     // This same test has multiple test instances.
                     Object obj = cacheTestInstances.get(testInstance.getTestName());
                     if (obj instanceof TestInstanceInfo) {
                        cacheTestInstances.put(testInstance.getTestName(),
                                 new ArrayList(Arrays.asList(((TestInstanceInfo) obj),
                                          testInstance)));
                     } else if (obj instanceof List) {
                        List<TestInstanceInfo> testList = ((List<TestInstanceInfo>) cacheTestInstances.get(testInstance.getTestName()));
                        testList.add(testInstance);
                        cacheTestInstances.put(testInstance.getTestName(),
                                 testList);
                     }
                  }
               }
            } else {
               log.warn("No test instances found associated to specified testset names :"
                        + testsetNames);
            }
         } else {
            log.warn("No testsets found with the specified testset names :"
                     + testsetNames);
         }
      } else {
         log.warn("No testsets found under specific folder :"
                  + QcConstants.QC_TESTSETFOLDER_PATH);
      }
   }

   /**
    * Find test instance associated to a test in test instances cache.
    *
    * @param resultData
    * @return test instance
    */
   private TestInstanceInfo findTestInstance(TestResultData resultData)
                                             throws Exception
   {
      TestInstanceInfo testInstance = null;
      String testInstanceName = (resultData.getTestConfigId() == null ? resultData.getTestName()
               : resultData.getTestConfigId());
      Object obj = cacheTestInstances.get(resultData.getTestName());
      if (obj != null) {
         if (obj instanceof TestInstanceInfo) {
            TestInstanceInfo testInstance1 = (TestInstanceInfo) obj;
            if (testInstance1.getName().equals(testInstanceName)
                     && testInstance1.getTestName().equals(
                              resultData.getTestName())
                        && (QcConstants.QC_TESTSET_IDS != null || testInstance1.getTestSetId() == QcUtil
                                .getIdByName(cacheAllTestSets, resultData.getTestSetName()))) {
               testInstance = testInstance1;
            }
         } else if (obj instanceof List) {
            List<TestInstanceInfo> testInstances = (List<TestInstanceInfo>) obj;
            for (TestInstanceInfo testInstance1 : testInstances) {
               if (testInstance1.getName().equals(testInstanceName)
                        && testInstance1.getTestName().equals(
                                 resultData.getTestName())
                        && testInstance1.getTestSetId() == QcUtil.getIdByName(
                                 cacheAllTestSets, resultData.getTestSetName())) {
                  testInstance = testInstance1;
                  break;
               }
            }
         }
      }
      return testInstance;
   }
}
