/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This util class reads the XML test data and converts to its relevant object form.
 */
public class QcXmlConfigUtil
{
   public static final String TESTINSTANCE_TAG = "TestInstance";
   public static final String TESTRUN_TAG = "TestRun";
   public static final String TESTSET_TAG = "TestSet";
   public static final String TESTSET1_TAG = "testSets.TestSet";
   public static final String TESTSET_FOLDER_TAG = "subFolders.TestSetFolder";
   public static final String SUBFOLDER_TESTSET_TAG = "subFolders.TestSetFolder.testSets.TestSet";

   public static final String ID_TAG = "ID";
   public static final String NAME_TAG = "name";
   public static final String TESTID_TAG = "testID";
   public static final String TESTNAME_TAG = "testName";
   public static final String TESTINSTANCEID_TAG = "testInstanceID";
   public static final String TESTSETID_TAG = "testSetID";
   public static final String STATUS_TAG = "status";
   public static final String LOGS_LOCATION_TAG = "logsLocation";
   public static final String RUNID_TAG = "runID";
   public static final String TYPE_TAG = "type";
   public static final String FILEPATH_TAG = "filePath";
   public static final String BUILD_TAG = "build";
   public static final String TESTER_TAG = "tester";
   public static final String PARENT_FOLDERID_TAG = "parentFolderID";
   public static final String TESTCASE_TAG = "TestCase";
   public static final String PRODUCT_TAG = "product";
   public static final String FUNCTIONALAREA_TAG = "funcArea";
   public static final String COMPONENT_TAG = "component";
   public static final String AUTOLEVEL_TAG = "autoLevel";
   public static final String PRIORITY_TAG = "priority";




   private final static Logger log = LoggerFactory.getLogger(QcXmlConfigUtil.class);

   /**
    * Get list of test sets from test set tree xml data.
    *
    * @param testsetTreeData - xml data that holds testset tree information.
    * @return list of test set objects.
    */
   public static List<TestSetInfo> getTestSetsFromTestSetTree(HierarchicalConfiguration testsetTreeData)
   {
      List<TestSetInfo> testsetInfos = new ArrayList<TestSetInfo>();
      if (testsetTreeData != null) {
         List<HierarchicalConfiguration> testsets = testsetTreeData.configurationsAt(TESTSET1_TAG);
         if (testsets != null && !testsets.isEmpty()) {
            for (HierarchicalConfiguration testset : testsets) {
               TestSetInfo testsetInfo = new TestSetInfo();
               testsetInfo.setId(testset.getLong(ID_TAG));
               testsetInfo.setName(testset.getString(NAME_TAG));
               testsetInfo.setParentFolderId(testset.getLong(PARENT_FOLDERID_TAG));
               testsetInfos.add(testsetInfo);
            }
         }
         List<HierarchicalConfiguration> subfolderTestses = testsetTreeData.configurationsAt(SUBFOLDER_TESTSET_TAG);
         if (subfolderTestses != null && !subfolderTestses.isEmpty()) {
            for (HierarchicalConfiguration subfolderTestset : subfolderTestses) {
               TestSetInfo testsetInfo = new TestSetInfo();
               testsetInfo.setId(subfolderTestset.getLong(ID_TAG));
               testsetInfo.setName(subfolderTestset.getString(NAME_TAG));
               testsetInfo.setParentFolderId(subfolderTestset.getLong(PARENT_FOLDERID_TAG));
               testsetInfos.add(testsetInfo);
            }
         }
      }
      return testsetInfos;
   }

   /**
    * Read list of testsets from xml data and returns testset id and names as a map.
    *
    * @param testsetsData - xml data that holds a list of testsets.
    * @return list of test set objects.
    */
   public static List<TestSetInfo> getTestSets(HierarchicalConfiguration testsetsData)
   {
      List<TestSetInfo> testsetInfos = new ArrayList<TestSetInfo>();
      if (testsetsData != null) {
         List<HierarchicalConfiguration> testsets = testsetsData.configurationsAt(TESTSET_TAG);
         if (testsets != null && !testsets.isEmpty()) {
            for (HierarchicalConfiguration testset : testsets) {
               TestSetInfo testsetInfo = new TestSetInfo();
               testsetInfo.setId(testset.getLong(ID_TAG));
               testsetInfo.setName(testset.getString(NAME_TAG));
               testsetInfo.setParentFolderId(testset.getLong(PARENT_FOLDERID_TAG));
               testsetInfos.add(testsetInfo);
            }
         }
      }
      return testsetInfos;
   }

   /**
    * Get testset folder id for a given testset folder name from testset xml data.
    *
    * @param testsetData - xml data that hold testset information.
    * @param testsetFolderName testset folder name.
    * @return testset folder id.
    */
   public static long getTestSetFolderId(HierarchicalConfiguration testsetData, String testsetFolderName)
   {
      long testsetFolderId = 0;
      List<HierarchicalConfiguration> testsetFoldersData = testsetData.configurationsAt(TESTSET_FOLDER_TAG);
      if (testsetFoldersData != null && !testsetFoldersData.isEmpty()) {
         for (HierarchicalConfiguration testsetFolderData : testsetFoldersData) {
            String name = testsetFolderData.getString(NAME_TAG);
            if (name.equals(testsetFolderName)) {
               testsetFolderId = testsetFolderData.getInt(ID_TAG);
               log.debug("Got testsetFolder id :{} for folderName :{}",
                        testsetFolderId, testsetFolderName);
               break;
            }
         }
      }
      return testsetFolderId;
   }

   /**
    * Get list of test instances from xml data and return as test instance id & its test names.
    *
    * @param testsetData - testset xml data that holds test instance information.
    * @return testInstance map containing test instance id as key and test name as value.
    */
   public static List<TestInstanceInfo> getTestInstances(HierarchicalConfiguration testsetData)
   {
      List<TestInstanceInfo> testInstances = null;
      List<HierarchicalConfiguration> testInstancesData = testsetData.configurationsAt(TESTINSTANCE_TAG);
      if (testInstancesData != null && !testInstancesData.isEmpty()) {
         testInstances = new ArrayList<TestInstanceInfo>();
         for (HierarchicalConfiguration testInstanceData : testInstancesData) {
            TestInstanceInfo testInstance = getTestInstanceInfo(testInstanceData);
            testInstances.add(testInstance);
         }
      }
      return testInstances;
   }


    /**
     * Returns a QcTestCase from xml data
     *
     * @param testcaseData
     * @return QcTestCase
     */
    public static QcTestCase getTestcase(HierarchicalConfiguration testcaseData) {
        QcTestCase qcTestcase = null;
        if (testcaseData != null) {
            qcTestcase = new QcTestCase();
            qcTestcase.setId(testcaseData.getString(ID_TAG));
            qcTestcase.setName(testcaseData.getString(NAME_TAG));
            qcTestcase.setProduct(testcaseData.getString(PRODUCT_TAG));
            qcTestcase.setFuncArea(testcaseData.getString(FUNCTIONALAREA_TAG));
            qcTestcase.setComponent(testcaseData.getString(COMPONENT_TAG));
            qcTestcase.setAutoLevel(testcaseData.getString(AUTOLEVEL_TAG));
            qcTestcase.setPriority(testcaseData.getString(PRIORITY_TAG));
        }
        return qcTestcase;
    }

   /**
    * Read test instance from xml data and return as test instance info object.
    *
    * @param testInstanceData - xml data holds test instance information.
    * @return TestInstanceInfo object.
    */
    public static TestInstanceInfo getTestInstanceInfo(HierarchicalConfiguration testInstanceData) {
        TestInstanceInfo testInstanceInfo = null;
        if (testInstanceData != null) {
            try {
                testInstanceInfo = new TestInstanceInfo();
                testInstanceInfo.setId(testInstanceData.getLong(ID_TAG));
                testInstanceInfo.setName(testInstanceData.getString(NAME_TAG));
                testInstanceInfo.setTestId(testInstanceData.getLong(TESTID_TAG));
                testInstanceInfo.setTestName(testInstanceData.getString(TESTNAME_TAG));
                testInstanceInfo.setStatus(QcTestStatus.fromValue(testInstanceData.getString(STATUS_TAG)));
                testInstanceInfo.setTestSetId(testInstanceData.getLong(TESTSETID_TAG));
            } catch (Exception e) {
                log.error("Error while parsing test : " + testInstanceInfo.getName(), e);
            }
        }
        return testInstanceInfo;
    }

   /**
    * Read test run from xml data and return as test run info object.
    *
    * @param testRunData - xml data holds test run information.
    * @return TestRunInfo object.
    */
   public static TestRunInfo getTestRunInfo(HierarchicalConfiguration testRunData)
   {
      TestRunInfo testRunInfo = null;
      if (testRunData != null) {
         testRunInfo = new TestRunInfo();
         testRunInfo.setId(testRunData.getLong(ID_TAG));
         testRunInfo.setName(testRunData.getString(NAME_TAG));
         testRunInfo.setTestInstanceId(testRunData.getLong(TESTINSTANCEID_TAG));
         testRunInfo.setTestId(testRunData.getLong(TESTID_TAG));
         testRunInfo.setStatus(QcTestStatus.fromValue(testRunData.getString(STATUS_TAG)));
         testRunInfo.setLogsLocation(testRunData.getString(LOGS_LOCATION_TAG));
         testRunInfo.setBuildNumbers(testRunData.getList(BUILD_TAG));
         testRunInfo.setUserId(testRunData.getString(TESTER_TAG));
      }
      return testRunInfo;
   }

   /**
    * Convert log file xml data into LogFileInfo object.
    *
    * @param logFileData - log file xml data
    * @return LogFileInfo object.
    */
   public static LogFileInfo getLogFileInfo(HierarchicalConfiguration logFileData)
   {
      LogFileInfo logFileInfo = null;
      if (logFileData != null) {
         logFileInfo = new LogFileInfo();
         logFileInfo.setRunId(logFileData.getLong(RUNID_TAG));
         logFileInfo.setType(logFileData.getString(TYPE_TAG));
         logFileInfo.setFileName(logFileData.getString(NAME_TAG));
         logFileInfo.setFilePath(logFileData.getString(FILEPATH_TAG));
      }
      return logFileInfo;
   }
}
