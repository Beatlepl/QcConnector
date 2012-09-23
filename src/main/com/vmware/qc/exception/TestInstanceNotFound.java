/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc.exception;

/**
 * This exception is thrown when specific test instance is not found.
 */
public class TestInstanceNotFound extends NotFound
{
   public TestInstanceNotFound(String errorMsg)
   {
      super(errorMsg);
   }
}
