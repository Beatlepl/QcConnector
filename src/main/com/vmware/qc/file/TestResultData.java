/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.file;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.qc.QcTestStatus;

/**
 * Holds test result data of result log file.
 */
public class TestResultData
{
   private String testName;
   private String testConfigId;
   private QcTestStatus testStatus;
   private String testSetName;
   private List<String> artifactPaths;
   private List<Integer> buildNumbers;
   private List<String> bugIds;
   private Map<String, String> customFields = new HashMap<String, String>();

   /**
    * Return test name.
    *
    * @return test name.
    */
   public String getTestName()
   {
      return testName;
   }

   /**
    * Set test name.
    *
    * @param testName.
    */
   public void setTestName(String testName)
   {
      this.testName = testName;
   }

   /**
    * Return test configuration id that represents different set of data for the same test.
    *
    * @return test configuration id.
    */
   public String getTestConfigId()
   {
      return testConfigId;
   }

   /**
    * Set test configuration id that represents different set of data for the same test.
    *
    * @param testConfigId test configuration id.
    */
   public void setTestConfigId(String testConfigId)
   {
      this.testConfigId = testConfigId;
   }

   /**
    * Return test run status.
    *
    * @return test run status.
    */
   public QcTestStatus getTestStatus()
   {
      return testStatus;
   }

   /**
    * Set test run status.
    *
    * @param testStatus
    */
   public void setTestStatus(QcTestStatus testStatus)
   {
      this.testStatus = testStatus;
   }

   /**
    * Return test set name.
    *
    * @return test set name.
    */
   public String getTestSetName()
   {
      return testSetName;
   }

   /**
    * Set test set name.
    *
    * @param testSetName
    */
   public void setTestSetName(String testSetName)
   {
      this.testSetName = testSetName;
   }

   /**
    * Return list of artifacts/log files.
    *
    * @return
    */
   public List<String> getArtifactPaths()
   {
      return artifactPaths;
   }

   /**
    * Set list of artifacts/log files.
    * @param artifactPaths
    */
   public void setArtifactPaths(List<String> artifactPaths)
   {
      this.artifactPaths = artifactPaths;
   }

   /**
    * Return build number(s) the test is run against.
    *
    * @return list of builds.
    */
   public List<Integer> getBuildNumbers()
   {
      return buildNumbers;
   }

   /**
    * Set build number(s) the test is run against.
    *
    * @param buildNumbers list of builds.
    */
   public void setBuildNumbers(List<Integer> buildNumbers)
   {
      this.buildNumbers = buildNumbers;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("testName =" + testName)
        .append(", configDataId =" + testConfigId)
        .append(", testsetName =" + testSetName)
        .append(", testStatus =" + testStatus)
        .append(", artifactPaths =" + artifactPaths)
        .append(", buildNumbers =" + buildNumbers);
      return sb.toString();
   }

    /**
     * @return the bugIds
     */
    public List<String> getBugIds() {
        return bugIds;
    }

    /**
     * @param bugIds
     *            the bugIds to set
     */
    public void setBugIds(List<String> bugIds) {
        this.bugIds = bugIds;
    }

    /**
     * @return the customFields
     */
    public Map<String, String> getCustomFields() {
        return customFields;
    }

    /**
     * @param customFields
     *            the customFields to set
     */
    public void setCustomFields(Map<String, String> customFields) {
        this.customFields = customFields;
    }

    public void addCustomField(String fieldName, String fieldValue){
        this.customFields.put(fieldName, fieldValue);
    }

}
