/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains utility methods.
 */
public class QcUtil
{
   private static final Logger log = LoggerFactory.getLogger(QcUtil.class);

   /**
    * Return Id of a specific name.
    */
   public static long getIdByName(Map<Long, String> qcIdNames,
                                  String qcName)
   {
      long id = 0;
      for (Map.Entry<Long, String> me : qcIdNames.entrySet()) {
         if (qcName.equals(me.getValue())) {
            id = me.getKey();
            break;
         }
      }
      return id;
   }

   /**
    * Finds given QC names in QC IdNames Map object[Ex: TestSet] and
    * return their corresponding QC ids.
    *
    * @param qcIdNames - map containing list of qc ids & its names.
    * @param qcNames - list of qc names using which its corresponding qc ids are
    *           retrieved.
    * @return list of QC ids.
    */
   public static List<Long> getIds(final Map<Long, String> qcIdNames,
                                   final List<String> qcNames)
   {
      List<Long> ids = new ArrayList<Long>();
      List<String> qcNameCopies = new ArrayList<String>();
      qcNameCopies.addAll(qcNames);
      for (Map.Entry<Long, String> me : qcIdNames.entrySet()) {
         String qcName = me.getValue();
         if (qcNameCopies.contains(qcName)) {
            if (qcNameCopies.remove(qcName)) {
               ids.add(me.getKey());
            }
         }
         if (qcNameCopies.isEmpty()) {
            break;
         }
      }
      return (!ids.isEmpty() ? ids : null);
   }

   /**
    * Find test instance from a list of test instances by test name and test set id.
    *
    * @param testInstances list of test instances.
    * @param testName test name.
    * @param testInstanceName test instance name.
    *          if it is not specified, test instance name & test name are same.
    * @param testSetId test set id.
    * @return test instance.
    */
   public static TestInstanceInfo findTestInstance(List<TestInstanceInfo> testInstances,
                                                   String testName,
                                                   String testInstanceName,
                                                   Long testSetId)
   {
      TestInstanceInfo testInstanceInfo = null;
      String testInstanceName1 = (testInstanceName == null ? testName
               : testInstanceName);
      if (testInstances != null) {
         for (TestInstanceInfo testInstance : testInstances) {
            if (testName.equals(testInstance.getTestName())
                     && testInstanceName1.equals(testInstance.getName())
                     && (testSetId == null || testSetId == testInstance.getTestSetId())) {
               testInstanceInfo = testInstance;
               break;
            }
         }
      }
      return testInstanceInfo;
   }

   /**
    * Return the data from an input stream and convert into string text.
    *
    * @param is input stream
    * @return data as string
    */
   public static String readData(InputStream in)
   {
      StringBuffer sb = new StringBuffer();
      try {
         BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String line = null;
         while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
         }
      } catch (IOException ioex) {
         log.error("Got an exception while reading input stream", ioex);
      }
      return sb.toString();
   }

   /**
    * Check whether the given string data is empty.
    *
    * @param str string data
    * @return true if string data is empty, else false.
    */
   public static boolean isEmpty(String strData)
   {
      return (strData == null || "".equals(strData.trim()));
   }
}
