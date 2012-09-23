/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.util.List;

import org.apache.commons.configuration.Configuration;

/**
 * This class contains all generic QC configuration constants.
 */
public final class QcConstants
{
   public static final String QC_SERVER;
   public static final int QC_PORT;
   public static final String QC_DOMAIN_NAME;
   public static final String QC_PROJECT_NAME;
   public static final String QC_USERID;
   public static final String QC_WEBSERVICE_APIKEY;
   public static final String QC_TESTSETFOLDER_PATH;
   public static final String QC_ENDPOINT_URL;
   public static final boolean OVERWRITE_PASS_STATUS;
   public static final List<String> QC_BUILD_NUMBERS;
   public static final String QC_TESTSET_NAME;
   public static final String[] QC_TESTSET_IDS;

   public QcConstants()
   {
   }

   static
   {
      Configuration configData = QcConfigDataHandler.getConfigDataHandler().getConfigData();
      QC_SERVER = configData.getString("qc.server");
      QC_PORT = configData.getInt("qc.port", 8080);
      QC_DOMAIN_NAME = configData.getString("qc.domain.name");
      QC_PROJECT_NAME = configData.getString("qc.project.name");
      QC_USERID = configData.getString("qc.userid");
      QC_TESTSETFOLDER_PATH = configData.getString("qc.testsetfolder.path");
      QC_WEBSERVICE_APIKEY = configData.getString("qc.webservice.apikey");
      QC_ENDPOINT_URL = "http://" + QC_SERVER + ":" + QC_PORT
               + "/QCIntgrt/rest/" + QC_DOMAIN_NAME + "/" + QC_PROJECT_NAME;

      OVERWRITE_PASS_STATUS = configData.getBoolean("qc.overwrite.pass.status", false);
      QC_BUILD_NUMBERS = (!QcUtil.isEmpty(configData.getString("qc.build.numbers")) ? configData.getList("qc.build.numbers")
               : null);
      QC_TESTSET_NAME = (!QcUtil.isEmpty(configData.getString("qc.testset.name"))
               ? configData.getString("qc.testset.name") : null);
        String testidsString =
                (!QcUtil.isEmpty(configData.getString("qc.testset.ids")) ? configData.getString("qc.testset.ids")
                        : null);
        QC_TESTSET_IDS = testidsString != null ? testidsString.split(",") : null;
   }

}
