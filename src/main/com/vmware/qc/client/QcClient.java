/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.client;
import java.io.File;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.QcConfigDataHandler;
import com.vmware.qc.QcConnector;
import com.vmware.qc.QcConstants;
import com.vmware.qc.QcTestStatus;
import com.vmware.qc.QcUtil;
import com.vmware.qc.TestInstanceInfo;
import com.vmware.qc.TestRunInfo;


/**
 * This client class helps to post the result of an executed test and its test logs into QC using QC connector.
 */
public class QcClient
{
   private static QcConnector connector = new QcConnector();
   public static final Logger log = LoggerFactory.getLogger(QcClient.class);

   /**
    * Post the result of a test and its log files into QC.
    *
    * @param testsetName testset name to which the test is associated.
    * @param testName name of the test.
    * @param testInstanceName test instance name associated with the specified test.
    * @param testStatus test status.
    * @param logFileNames log/screenshot files that are to be uploaded.
    * @return true if post result to QC is succcessful, else false.
    * @throws Exception
    */
   public static boolean postResult2Qc(String testsetName,
                                       String testName,
                                       String testInstanceName,
                                       QcTestStatus testStatus,
                                       List<String> logFileNames)
                                       throws Exception
   {
      boolean posted = false;
      boolean uploaded = true;
      try {
         TestInstanceInfo testInstance = connector.findTestInstance(
                  QcConstants.QC_TESTSETFOLDER_PATH, testsetName, testName, testInstanceName);
         if (testInstance != null) {
            TestRunInfo testRun = connector.postResult2Qc(testInstance.getId(),
                     testStatus);
            if (testRun != null) {
               posted = true;
               if (logFileNames != null && !logFileNames.isEmpty()) {
                  for (String logFileName : logFileNames) {
                     if (connector.uploadLogFile2Qc(testRun.getId(),
                              logFileName) != null) {
                        log.info(logFileName + " file is uploaded successfully");
                     } else {
                        log.error("Upload failed :" + logFileName);
                        uploaded = false;
                     }
                  }
               }
            }
         } else {
            log.error("Failed to get test instance in QC");
         }
      } catch (Exception ex) {
         ex.printStackTrace();
      }
      return posted && uploaded;
   }

   /**
    * Main method.
    */
   public static void main(String[] args) throws Exception
   {
      //QcConstants.class.getClass();
      Configuration configData = QcConfigDataHandler.getConfigDataHandler().getConfigData();
      String testName = configData.getString("qc.test.name");
      String testInstanceName = configData.getString("qc.test.instancename");
      String testsetName = configData.getString("qc.testset.name");
      String strTestStatus = configData.getString("qc.test.status");

      //Check for empty string.
      testName = (!QcUtil.isEmpty(testName) ? testName : null);
      testInstanceName = (!QcUtil.isEmpty(testInstanceName) ? testInstanceName : null);
      testsetName = (!QcUtil.isEmpty(testsetName) ? testsetName : null);
      strTestStatus = (!QcUtil.isEmpty(strTestStatus) ? strTestStatus : null);
      List<String> fileNames = (!QcUtil.isEmpty(configData.getString("qc.log.filenames"))
               ? configData.getList("qc.log.filenames") : null);
      QcTestStatus testStatus = QcTestStatus.fromValue(strTestStatus);

      //Check that all files exist.
      if (fileNames != null) {
         for (String fileName : fileNames) {
            File file = new File(fileName);
            if (!(file.exists() && file.isFile())) {
               log.error("File is not found :" + fileName);
               System.exit(-1);
            }
         }
      }

      boolean success = postResult2Qc(testsetName, testName, testInstanceName,
               testStatus, fileNames);
      if (!success) {
         System.exit(-1);
      }
   }
}
