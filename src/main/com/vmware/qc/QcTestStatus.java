/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

/**
 *  Consists of a set of possible QC test status constant values.
 */
public enum QcTestStatus {
   FAILED("Failed"),
   PASSED("Passed"),
   NOT_COMPLETED("Not Completed");

   private String qcTestStatusText;

   QcTestStatus(String qcTestStatusText)
   {
      this.qcTestStatusText = qcTestStatusText;
   }

   /**
    * Converts a specific status text into enum constant.
    *
    * @param statusText status text.
    * @return enum constant.
    */
   public static QcTestStatus fromValue(String statusText)
   {
      QcTestStatus status = null;
      for (QcTestStatus st : QcTestStatus.values()) {
         if (statusText.equals(st.toString())) {
            status = st;
            break;
         }
      }
      return status;
   }

   public String toString()
   {
      return this.qcTestStatusText;
   }
}
