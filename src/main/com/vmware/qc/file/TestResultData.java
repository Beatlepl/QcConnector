/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.file;

import java.util.List;

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
   private List<String> buildNumbers;
   private String bugIds;

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
   public List<String> getBuildNumbers()
   {
      return buildNumbers;
   }

   /**
    * Set build number(s) the test is run against.
    *
    * @param buildNumbers list of builds.
    */
   public void setBuildNumbers(List<String> buildNumbers)
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
    public String getBugIds() {
        return bugIds;
    }

    /**
     * @param bugIds
     *            the bugIds to set
     */
    public void setBugIds(String bugIds) {
        this.bugIds = bugIds;
    }
}
