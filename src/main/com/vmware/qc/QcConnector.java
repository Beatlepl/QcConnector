/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.exception.NotFound;
import com.vmware.qc.exception.TestInstanceNotFound;

/**
 * This class exposes a list of QC functionalities to read/create/update information related to the test into the QC.
 * It acts as bridge between QC and a Client [ Client can be any Java Client inside/outside the framework ].
 *
 * For any QC functionality, this class selects appropriate webservice url and creates
 * request object, sets header properties,  adds parameters and
 * then makes use of QcRestClient to send these information to QC web service.
 * Finally It converts XML response returned from webservice into value/collection objects
 * and sends it to the client.
 */
public class QcConnector
{
   private QcRestClient restClient;
   private final static Logger log = LoggerFactory.getLogger(QcConnector.class);

   public QcConnector()
   {
      restClient = new QcRestClient();
   }

   /**
    * Retrieves all test set under a specific testset folder in QC.
    *
    * @param testSetFolderPath - testset folder path [ Example : Root/MN.Next/Beta/Cycle1/CoreVC/VC-ESX50i ].
    * @return testset map containing testset id as key and testset name as value.
    */
   public Map<Long, String> getTestSets(String testSetFolderPath)
      throws Exception
   {
      Map<Long, String> testSets = null;
      /*
       * If testSetIds are provided then work with that
       */
        if (QcConstants.QC_TESTSET_IDS != null && QcConstants.QC_TESTSET_IDS.length > 0) {
            testSets = new HashMap<Long, String>();
            for (String testSetId : QcConstants.QC_TESTSET_IDS) {
                testSets.put(Long.valueOf(testSetId.trim()), "");
            }
        } else {
            /*
             * Start reading testset tree from root folder to last sub-folder in
             * the specified folder path and
             * then get all testsets from testset tree associated to last
             * sub-folder.
             */
            XMLConfiguration testSetTree = getTestSetTree(null, true);
            boolean foundTestSetFolders = true;
            if (testSetTree != null) {
                String[] testsetSubFolders = testSetFolderPath.split("/");
                for (int cnt = 1; cnt < testsetSubFolders.length; cnt++) {
                    long tsFolderId = QcXmlConfigUtil.getTestSetFolderId(testSetTree, testsetSubFolders[cnt]);
                    if (tsFolderId > 0) {
                        testSetTree = getTestSetTree(tsFolderId, cnt < testsetSubFolders.length - 1);
                    } else {
                        log.error(testsetSubFolders[cnt] + " testset folder is not found");
                        foundTestSetFolders = false;
                        break;
                    }
                }
                if (foundTestSetFolders) {
                    testSets = QcXmlConfigUtil.getTestSets(testSetTree);
                }
            } else {
                log.error("Couldn't get testset folder under root folder");
            }
        }
      return testSets;
   }

   /**
    * Gets testset id for a given testset name under a specific testset folder path in QC.
    *
    * @param testSetFolderPath - testset folder path.
    * @param testsetName - testset name.
    * @return testset id.
    */
   public long getTestSetId(String testSetFolderPath,
                            String testsetName)
                            throws Exception
   {
       Map<Long, String> testsets = getTestSets(testSetFolderPath);
       log.info("Retrieved TestSets :" + testsets);
       return QcUtil.getIdByName(testsets, testsetName);
   }

   /**
    * Gets a list of test instances from QC associated to specific test sets.
    *
    * @param testSetId - list of testset ids.
    * @return list of test instances. If no test set or test instances are
    *         found, return NULL.
    */
   public List<TestInstanceInfo> getTestInstances(List<Long> testSetIds) throws Exception
   {
      List<TestInstanceInfo> allTestInstances = new ArrayList<TestInstanceInfo>();
      List<TestInstanceInfo> testInstances = null;
      int startIndex = 1;
      do {
         testInstances = getTestInstances(testSetIds, startIndex);
         if (testInstances != null && !testInstances.isEmpty()) {
            allTestInstances.addAll(testInstances);
            startIndex = startIndex + testInstances.size();
         }
      } while(testInstances != null && testInstances.size() >= 500);
      return (allTestInstances != null && !allTestInstances.isEmpty() ? allTestInstances
               : null);
   }

   /**
    * Gets a list of test instances from QC associated to specific test sets using start index.
    *
    * @param testSetId - list of testset ids.
    * @param startIndex - Index of starting test instance.
    * @return list of test instances that contain only max of 500(limitation by HPQC Webservice API).
    *            If no test set or test instances are found, return NULL.
    */
   private List<TestInstanceInfo> getTestInstances(List<Long> testSetIds, int startIndex) throws Exception
   {
      List<TestInstanceInfo> testInstances = null;
      QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL + "/test-instances");
      qcRequest.addField("testSetIDs", testSetIds);
      qcRequest.addField("startIndex", startIndex);
      try {
         XMLConfiguration config = restClient.get(qcRequest);
         testInstances = QcXmlConfigUtil.getTestInstances(config);
      } catch(NotFound notFound) {
         log.warn("No test instances found for testsets :" + testSetIds);
      }
      return testInstances;
   }

    /**
     * Returns a TestCases from QC TestPlan by its ids
     *
     * @param testcaseIds
     * @return List<QcTestCase>
     * @throws Exception
     */
    public List<QcTestCase> getTestCases(List<Long> testcaseIds) throws Exception {
        List<QcTestCase> testCases = new ArrayList<QcTestCase>();
        ;
        for (Long testcaseId : testcaseIds) {
            QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL + "/test-case/" + testcaseId);
            try {
                XMLConfiguration config = restClient.get(qcRequest);
                testCases.add(QcXmlConfigUtil.getTestcase(config));
            } catch (NotFound notFound) {
                log.warn("No testcase found for testCase id: " + testcaseId);
            }
        }
        return testCases;
    }

   /**
    * Gets a specific test instance's information from QC using its id.
    *
    * @param testInstanceId - test instance id
    * @return test instance object.
    */
   public TestInstanceInfo getTestInstance(long testInstanceId)
      throws Exception
   {
      TestInstanceInfo testInstance = null;
      QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL
               + "/test-instance/" + testInstanceId);
      try {
         XMLConfiguration testInstanceData = restClient.get(qcRequest);
         testInstance = QcXmlConfigUtil.getTestInstanceInfo(testInstanceData);
      } catch (NotFound notFound) {
         throw new TestInstanceNotFound("No test instance is found for Id #"
                  + testInstanceId);
      }
      return testInstance;
   }

   /**
    * Finds a specific test instance using its test name and testset name under a specific testset folder.
    * If testset is not specified, the test instance will be returned from any testset.
    *
    * @param testSetFolderPath - testset folder path.
    * @param testsetName - testset name.
    * @param testName - test name.
    * @param testInstanceName - test instance name that is mapped to instance name in QC.
    *        If it is not specified, testInstanceName and testName are same.
    * @return test instance object. If the object is not found, then it returns NULL.
    */
   public TestInstanceInfo findTestInstance(String testSetFolderPath,
                                            String testsetName,
                                            String testName,
                                            String testInstanceName)
                                            throws Exception
   {
      TestInstanceInfo testInstanceInfo = null;
      String testInstanceName1 = (QcUtil.isEmpty(testInstanceName) ? testName
               : testInstanceName);
      try {
         List<Long> testsetIds = null;
         /*
          * Get testset id for a given testset. if no testset specified,
          * get all testsets under the testset folder.
          */
         if (testsetName != null) {
            long testsetId = getTestSetId(testSetFolderPath, testsetName);
            if (testsetId > 0) {
               testsetIds = Arrays.asList(testsetId);
            }
         } else {
            Map<Long, String> testSets = getTestSets(testSetFolderPath);
            if (testSets != null) {
               testsetIds = new ArrayList<Long>(testSets.keySet());
            }
         }
         if (testsetIds != null) {
            List<TestInstanceInfo> testInstances = getTestInstances(testsetIds);
            if (testInstances != null) {
               for(TestInstanceInfo testInstance : testInstances) {
                  if (testName.equals(testInstance.getTestName())
                           && testInstanceName1.equals(testInstance.getName())
                           && (testsetName == null || testsetIds.get(0) == testInstance.getTestSetId())) {
                     testInstanceInfo = testInstance;
                     break;
                  }
               }
            }
         } else {
            log.error("Couldn't find test set id for testset :"
                     + testsetName);
         }
      } catch (NotFound notFound) {
      } catch (Exception ex) {
         log.warn("Got an exception while getting test instances :"
                  + ex.getMessage());
      }
      return testInstanceInfo;
   }

   /**
    * Posts a test run result into QC using test instance id.
    *
    * @param testInstanceId - test instance id.
    * @param status - test run status.
    * @return testRun info object.
    */
   public TestRunInfo postResult2Qc(long testInstanceId,
                                    QcTestStatus status)
                                    throws Exception
   {
      TestRunInfo testRunInfo = new TestRunInfo();
      testRunInfo.setTestInstanceId(testInstanceId);
      testRunInfo.setStatus(status);
      testRunInfo.setUserId(QcConstants.QC_USERID);
      testRunInfo.setBuildNumbers(QcConstants.QC_BUILD_NUMBERS);
      return postResult2Qc(testRunInfo);
   }

   /**
    * Posts a test run result into QC.
    *
    * @param testRunInfo - test run result information.
    * @return testRun info object.
    */
   public TestRunInfo postResult2Qc(TestRunInfo testRunInfo)
                                    throws Exception
   {
        TestRunInfo newTestRunInfo = null;
        QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL + "/run");
        StringBuffer body = new StringBuffer();
        body.append("tester=").append(testRunInfo.getUserId());
        body.append("&testInstanceID=").append(testRunInfo.getTestInstanceId());
        body.append("&status=").append(testRunInfo.getStatus());
        if (testRunInfo.getBuildNumbers() != null && !testRunInfo.getBuildNumbers().isEmpty()) {
            body.append("&build=").append(testRunInfo.getBuildNumbers().get(0));
        }
        if (testRunInfo.getBugIds() != null && !testRunInfo.getBugIds().isEmpty()) {
            body.append("&bugIDs=").append(testRunInfo.getBugIds());
        }
        body.append("&runName=qcConnectorPoster");
        qcRequest.setRequestBody(body.toString());
      qcRequest.addHeaderProperty("Accept", "application/xml");
      qcRequest.addHeaderProperty("Content-Type", "application/x-www-form-urlencoded");
      XMLConfiguration testRunData = restClient.post(qcRequest);
      newTestRunInfo = QcXmlConfigUtil.getTestRunInfo(testRunData);
      if (newTestRunInfo != null) {
         log.info("Test result is posted into QC successfully :\n" + newTestRunInfo);
      } else {
         log.error("Post test result to QC failed");
      }
      return newTestRunInfo;
   }

   /**
    * Uploads a log file associated to test run id to its assigned log directory in QC repository.
    *
    * @param testRunId - test run id.
    * @param logFileName - log file name that is being uploaded.
    * @return log file object.
    */
   public LogFileInfo uploadLogFile2Qc(long testRunId,
                                       String logFileName)
                                       throws Exception
   {
      QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL + "/run/"
               + testRunId + "/log-attachment");
      qcRequest.addField("overwrite", true);
      qcRequest.addHeaderProperty("Accept", "application/xml");
      qcRequest.addHeaderProperty("Content-Type", "multipart/form-data;boundary="
               + QcRequest.BOUNDARY);
      XMLConfiguration uploadResultData = restClient.upload(qcRequest, logFileName);
      return QcXmlConfigUtil.getLogFileInfo(uploadResultData);
   }

   /**
    * Returns sub-folders and testsets in the specific folder. If folder id is not specified,
    * sub folders and testsets will be returned from root folder.
    *
    * @param testSetFolderId - testset folder id.
    * @param viewOneLevelOnly - If this flag is set to true, only immediate sub-folders & testsets
    *          will be returned from the specified folder.
    * @return XML object containing the sub-folder and testsets information.
    */
   private XMLConfiguration getTestSetTree(Long testSetFolderId,
                                           boolean viewOneLevelOnly)
                                           throws Exception
   {
      QcRequest qcRequest = new QcRequest(QcConstants.QC_ENDPOINT_URL + "/test-lab-tree");
      if (testSetFolderId != null && testSetFolderId > 0) {
         qcRequest.addField("folderID", testSetFolderId);
      }
      qcRequest.addField("viewTestInstances", false);
      qcRequest.addField("viewOneLevelOnly", viewOneLevelOnly);
      XMLConfiguration testsetXmlData = restClient.get(qcRequest);
      return testsetXmlData;
   }

}
