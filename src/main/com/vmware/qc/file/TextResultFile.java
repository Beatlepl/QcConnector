/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.file;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.QcConstants;
import com.vmware.qc.QcTestStatus;

/**
 * This class is used to parse result logs and create TestResultData objects.
 */
public class TextResultFile implements ResultFile
{
   private static final Logger log = LoggerFactory.getLogger(TextResultFile.class);

   private String resultFile;

   /**
    * Set result file name.
    */
   public void setResultFileName(String resultFileName)
   {
      this.resultFile = resultFileName;
   }

   /**
    * Read test result file and filter valid result rows containing the following format
    * "RESULT:  <serial no>  <testclass name>[-<dataset id>].<method name>  <test status>  <suite file name>"
    *
    * @return list of test result objects
    */
   public List<TestResultData> process()
   {
      List<TestResultData> testResults = new ArrayList<TestResultData>();
      try {
         BufferedReader bufferReader = new BufferedReader(new FileReader(
                  resultFile));
         String line = null;
         while((line = bufferReader.readLine()) != null) {
            if (line.startsWith("RESULT:") && line.endsWith(".xml")) {
               String[] fieldsData = line.split("  ");
               TestResultData resultData = new TestResultData();
               String testNameField = fieldsData[2];
               String methodName = testNameField.substring(testNameField.lastIndexOf(".") + 1);
               String datasetId = (testNameField.indexOf("-") >= 0 ? testNameField.substring(
                        testNameField.indexOf("-") + 1,
                        testNameField.lastIndexOf(methodName) - 1) : null);
               int testClassendIndex = (datasetId == null ? testNameField.lastIndexOf(".")
                        : testNameField.indexOf("-"));
               String testClassName = testNameField.substring(0,
                        testClassendIndex);
               resultData.setTestName(testClassName
                        + (!"test".equals(methodName) ? "-" + methodName : "")); //append method name if TestGroup.
               resultData.setTestConfigId(datasetId);
               resultData.setTestStatus(toTestStatus(fieldsData[3]));
               String suiteNameField = fieldsData[4];
               if (QcConstants.QC_TESTSET_NAME != null) {
                  resultData.setTestSetName(QcConstants.QC_TESTSET_NAME);
               } else {
                  resultData.setTestSetName(suiteNameField.substring(0,
                           suiteNameField.lastIndexOf(".")));
               }
               testResults.add(resultData);
            }
         }
      } catch(FileNotFoundException fnfe) {
         log.error("File is not found :" + resultFile);
      } catch(IOException ioe) {
         log.error("Got an exception while reading file :", ioe);
      }
      return testResults;
   }

   /**
    * Map the result to QC test status and return it.
    */
   private QcTestStatus toTestStatus(String strStatus)
   {
      QcTestStatus status = null;
      if ("PASS".equals(strStatus)) {
         status = QcTestStatus.PASSED;
      } else if ("FAIL".equals(strStatus)) {
         status = QcTestStatus.FAILED;
      } else if ("SKIP".equals(strStatus)) {
         status = QcTestStatus.NOT_COMPLETED;
      }
      return status;
   }
}
