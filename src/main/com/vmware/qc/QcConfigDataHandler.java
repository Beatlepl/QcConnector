/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc;

import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It is a singleton class to handle initializing QC configuration data which can be used
 * by other classes in this framework.
 */
public class QcConfigDataHandler
{
   private Configuration configData;
   private static QcConfigDataHandler handler = new QcConfigDataHandler();
   private static final Logger log = LoggerFactory.getLogger(QcConfigDataHandler.class);

   private QcConfigDataHandler()
   {
   }

   public static QcConfigDataHandler getConfigDataHandler()
   {
      return handler;
   }

   /**
    * Returns qc configuration data stored in this object, else does the followings.
    * - Load data from qcConfig.properties in the classpath.
    * - Overwrite any existing data with the data specified via command line.
    *
    * @return configuration data.
    */
   public Configuration getConfigData()
   {
      if (this.configData == null) {
         synchronized (this) {
            this.configData = new HierarchicalConfiguration();
            /**
             * - Load data from config properties file.
             * - Overwrite any existing data with the data that is specified via command line.
             */
            loadConfigData("qcConfig.properties", false);
            for (Entry entry : System.getProperties().entrySet()) {
               String key = (String) entry.getKey();
               if (key.toLowerCase().startsWith("qc.")) {
                  configData.setProperty((String) entry.getKey(),
                           entry.getValue());
               }
            }
            log.info("QC configuration data set :\n"
                     + ConfigurationUtils.toString(configData));
         }
      }
      return this.configData;
   }

   /**
    * Load data from property file.
    *
    * @param propFile The property file that contains configuration data.
    * @param overwriteProperty true if any existing property is to be overwritten, else false.
    */
   public synchronized void loadConfigData(String propFile, boolean overwriteProperty)
   {
      Configuration propData = getConfigFileData(propFile);
      if (propData != null) {
         Iterator<String> itr = propData.getKeys();
         while (itr.hasNext()) {
            String key = itr.next();
            if (overwriteProperty || !configData.containsKey(key)) {
               configData.setProperty(key, propData.getProperty(key));
            }
         }
      }
   }

   /**
    * Gets the configuration data from the specified file
    *
    * @param filePath file containing configuration information
    * @return configuration
    */
   private Configuration getConfigFileData(String filePath)
   {
      PropertiesConfiguration propConfig = new PropertiesConfiguration();
      try {
         propConfig.load(filePath);
         return ConfigurationUtils.convertToHierarchical(propConfig);
      } catch (ConfigurationException configEx) {
         log.error("Failed to load/locate/read configuration file :" + filePath, configEx);
         return null;
      }
   }

}
