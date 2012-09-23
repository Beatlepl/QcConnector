/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc.exception;

/**
 * This exception is thrown when specific test entity is not found.
 * It is the super class for all NotFound exception.
 */
public class NotFound extends QcException
{
   public NotFound()
   {
      super();
   }

   public NotFound(String errorMsg)
   {
      super(errorMsg);
   }
}
