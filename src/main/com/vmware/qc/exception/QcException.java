/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc.exception;

/**
 * This is super class exception for all other QC exceptions.
 */
public class QcException extends Exception
{
   public QcException()
   {
   }

   public QcException(String exceptionMsg)
   {
      super(exceptionMsg);
   }

}
