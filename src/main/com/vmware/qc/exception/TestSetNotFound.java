/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc.exception;

/**
 * This exception is thrown when specific test set is not found.
 */
public class TestSetNotFound extends NotFound
{
   public TestSetNotFound(String errorMsg)
   {
      super(errorMsg);
   }
}
