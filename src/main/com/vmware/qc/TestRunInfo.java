/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains test run information.
 */
public class TestRunInfo
{
   private long id;
   private String name;
   private long testInstanceId;
   private long testId;
   private String userId;
   private List<Integer> buildNumbers;
   private QcTestStatus status;
   private String logsLocation;
   private List<String> clientLogFilePaths;
   private List<String> bugIds;
   public Map<String, String> customFields = new HashMap<String, String>();

   /**
    * Return test run id.
    *
    * @return run id.
    */
   public long getId()
   {
      return id;
   }

   /**
    * Set test run id.
    *
    * @param id run id.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * Return test run name.
    *
    * @return run name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set test run name.
    *
    * @param name run name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Return test instance id.
    *
    * @return test instance id.
    */
   public long getTestInstanceId()
   {
      return testInstanceId;
   }

   /**
    * Set test instance id.
    *
    * @param testInstanceId
    */
   public void setTestInstanceId(long testInstanceId)
   {
      this.testInstanceId = testInstanceId;
   }

   /**
    * Return test id.
    *
    * @return test id.
    */
   public long getTestId()
   {
      return testId;
   }

   /**
    * Set test id.
    *
    * @param test id.
    */
   public void setTestId(long testId)
   {
      this.testId = testId;
   }

   /**
    * Return test run status.
    *
    * @return status.
    */
   public QcTestStatus getStatus()
   {
      return status;
   }

   /**
    * Set test run status
    *
    * @param status
    */
   public void setStatus(QcTestStatus status)
   {
      this.status = status;
   }

   /**
    * Return QC test logs directory path.
    *
    * @return logs location.
    */
   public String getLogsLocation()
   {
      return logsLocation;
   }

   /**
    * Set QC test logs directory path.
    *
    * @param logsLocation logs location.
    */
   public void setLogsLocation(String logsLocation)
   {
      this.logsLocation = logsLocation;
   }

   /**
    * Return list of file names at client location.
    *
    * @return list of file names.
    */
   public List<String> getClientLogFilePaths()
   {
      return clientLogFilePaths;
   }

   /**
    * Set list of file names.
    *
    * @param clientLogFilePaths list of file names.
    */
   public void setClientLogFilePaths(List<String> clientLogFilePaths)
   {
      this.clientLogFilePaths = clientLogFilePaths;
   }

   /**
    * Return tester who runs this test run.
    *
    * @return user id
    */
   public String getUserId()
   {
      return userId;
   }

   /**
    * Set user id who runs this test run.
    *
    * @param userId
    */
   public void setUserId(String userId)
   {
      this.userId = userId;
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

   /**
    * Concatenate all the attributes and return as string.
    */
   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("id =" + id)
        .append(", name = " + name)
        .append(", testInstanceId =" + testInstanceId)
        .append(", testId =" + testId)
        .append(", status =" + status)
        .append(", logsLocation =" + logsLocation)
        .append(", userId =" + userId)
        .append(", buildNumbers =" + buildNumbers)
        .append(", clientLogFilePaths =" + clientLogFilePaths);
      return sb.toString();
   }

   /**
    * Return hash code.
    */
   public int hashCode()
   {
      return toString().hashCode();
   }

   /**
    * Check this object and given object are equal.
    */
   public boolean equals(Object obj)
   {
      boolean isEqual = false;
      if (obj != null && obj.getClass() == this.getClass()) {
         TestRunInfo testRunInfo = (TestRunInfo) obj;
         if (testRunInfo != null) {
            isEqual = (this.toString().equals(testRunInfo.toString()));
         }
      }
      return isEqual;
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

}