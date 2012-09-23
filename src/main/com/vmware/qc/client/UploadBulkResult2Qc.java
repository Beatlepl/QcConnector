/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.client;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.QcConfigDataHandler;
import com.vmware.qc.file.PostResultFile2Qc;
import com.vmware.qc.file.ResultFile;

/**
 * This client class uploads bulk test results from a file into QC by making use of PostResultFile2Qc.
 */
public class UploadBulkResult2Qc
{
   public static final Logger log = LoggerFactory.getLogger(UploadBulkResult2Qc.class);

   /**
    * Main method
    */
   public static void main(String[] args) throws Exception
   {
      Configuration configData = QcConfigDataHandler.getConfigDataHandler().getConfigData();
      String resultFilename = configData.getString("qc.result.file");
      String resultClass = configData.getString("qc.result.class");
      String className = "com.vmware.qc.file." + resultClass;
      ResultFile resultFile = null;
      try {
         Class clz = Class.forName(className);
         resultFile = (ResultFile) clz.newInstance();
      } catch(ClassNotFoundException cnfe) {
         log.error("Class is not found :" + className, cnfe);
      } catch(Exception ex) {
         log.error("Unable to instantiate the class :" + className, ex);
      }
      resultFile.setResultFileName(resultFilename);
      PostResultFile2Qc postResultFile2Qc = new PostResultFile2Qc();
      postResultFile2Qc.post2Qc(resultFile);
   }
}
