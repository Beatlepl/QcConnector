/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

/**
 * Holds test instance information associated to a test case.
 */
public class TestInstanceInfo
{
   private long id;
   private String name;
   private long testId;
   private String testName;
   private long testSetId;
   private QcTestStatus status;

   /**
    * Get Test instance id.
    */
   public long getId()
   {
      return id;
   }

   /**
    * Set Test instance id.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * Get Test instance name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set Test case name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get test id.
    */
   public long getTestId()
   {
      return testId;
   }

   /**
    * Set test id.
    */
   public void setTestId(long testId)
   {
      this.testId = testId;
   }

   /**
    * Get test name.
    */
   public String getTestName()
   {
      return this.testName;
   }

   /**
    * Set test name
    */
   public void setTestName(String testName)
   {
      this.testName = testName;
   }

   /**
    * Get testset id that the test case belongs to.
    */
   public long getTestSetId()
   {
      return testSetId;
   }

   /**
    * Set testset id.
    */
   public void setTestSetId(long testSetId)
   {
      this.testSetId = testSetId;
   }

   /**
    * Get test run status.
    */
   public QcTestStatus getStatus()
   {
      return status;
   }

   /**
    * Set test run status.
    */
   public void setStatus(QcTestStatus status)
   {
      this.status = status;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("id =" + id)
        .append(", name = " + name)
        .append(", testId =" + testId)
        .append(", testName =" + testName)
        .append(", testSetId =" + testSetId)
        .append(", status =" + status);
      return sb.toString();
   }
}
