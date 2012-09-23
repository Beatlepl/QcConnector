/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.XMLConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.exception.NotFound;
import com.vmware.qc.exception.QcException;

/**
 * This is a low level class and it makes call directly to QC REST web services API
 * to access/store information related to test into QC. Every request sent to web service
 * is authenticated with API key.
 */
public class QcRestClient
{
   private final static Logger log = LoggerFactory.getLogger(QcRestClient.class);

   /**
    * Gets the information related to the test [ Ex : Test Instance / Test Set / Test Run ] from the QC
    *
    * @param request - a specific QC functionality is requested through the request object, that encapsulates http request details.
    * @return XML object containing the corresponding test related information requested. [Ex : Test Instance / Test Set / Test Run in XML format]
    * @throws NotFound exception is thrown if requested test information is not found in QC.
    *         QcException is thrown if any other web sevice related exception is received.
    */
   public XMLConfiguration get(QcRequest request)
                               throws QcException, Exception
   {
      XMLConfiguration xmlData = null;
      log.debug("Requested Url :" + request.getURL());

      //Open HTTP connection to the url
      HttpURLConnection conn = (HttpURLConnection) new URL(request.getURL()).openConnection();
      conn.setRequestMethod("GET");

      long opStartTime = System.currentTimeMillis();

      //Add header properties.
      Properties headerProps = request.getDefaultHeaderProperties();
      for(String propName : headerProps.stringPropertyNames()) {
         conn.setRequestProperty(propName, headerProps.getProperty(propName));
      }

      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
         xmlData = new XMLConfiguration();
         xmlData.load(conn.getInputStream());
      } else {
         QcException qcException = new QcException("Response code :"
                  + conn.getResponseCode() + ", Error message :"
                  + QcUtil.readData(conn.getErrorStream()));
         if (conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            log.warn(qcException.getMessage());
            throw new NotFound();
         } else {
            throw qcException;
         }
      }
      long opEndTime = System.currentTimeMillis();
      log.debug("Time taken to process QC request: {} secs",
               (opEndTime - opStartTime) / 1000);
      if (log.isTraceEnabled()) {
         log.trace("GET Response XML data :\n"
                  + ConfigurationUtils.toString(xmlData));
      }
      return xmlData;
   }

   /**
    * Posts the information related to the test [ Ex : Test Run ] to the QC.
    *
    * @param request - a specific QC functionality is requested through the request object, that encapsulates http request details.
    * @return XML object containing the test related information posted. [Output objects will belong to Test Run]
    * @throws QcException is thrown if any web service related exception is received.
    */
   public XMLConfiguration post(QcRequest request)
                               throws QcException, Exception
   {
      log.debug("Requested Url :" + request.getURL());

      XMLConfiguration xmlResponse = null;

      //Open HTTP connection to the url
      HttpURLConnection conn = (HttpURLConnection) new URL(request.getURL()).openConnection();
      conn.setRequestMethod("POST");

      long opStartTime = System.currentTimeMillis();

      //Add header properties.
      Properties headerProps = request.getHeaderProperties();
      for(String propName : headerProps.stringPropertyNames()) {
         conn.setRequestProperty(propName, headerProps.getProperty(propName));
      }

        conn.setDoOutput(true);
        BufferedOutputStream bos = new BufferedOutputStream(conn.getOutputStream());
        String bodyStr = request.getRequestBody() != null ? request.getRequestBody() : "Sending output";
        log.debug("Request body:" + bodyStr);
        bos.write(bodyStr.getBytes());
        bos.flush();
      if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED ||
               conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
         xmlResponse = new XMLConfiguration();
         xmlResponse.load(conn.getInputStream());
      } else {
         QcException qcException = new QcException("Response code :"
                  + conn.getResponseCode() + ", Error message :"
                  + QcUtil.readData(conn.getErrorStream()));
         throw qcException;
      }
      long opEndTime = System.currentTimeMillis();
      log.debug("Time taken to process QC request: {} secs",
               (opEndTime - opStartTime) / 1000);
      if (log.isTraceEnabled()) {
         log.trace("POST Response XML data :\n"
                  + ConfigurationUtils.toString(xmlResponse));
      }
      return xmlResponse;
   }

   /**
    * Uploads a file into the QC repository.
    *
    * @param request - QC file upload functionality is requested through the request object, that encapsulates http request details.
    * @param fileName Name of the file that is being uploaded.
    * @return XML object containing the test related information posted. [Output objects will belong to Log file].
    */
   public XMLConfiguration upload(QcRequest request,
                                  String fileName)
                                  throws QcException, Exception
   {
      log.debug("Requested Url :" + request.getURL());
      XMLConfiguration xmlResponse = null;

     //Open HTTP connection to the url
      HttpURLConnection conn = (HttpURLConnection) new URL(request.getURL()).openConnection();
      conn.setRequestMethod("POST");

      long opStartTime = System.currentTimeMillis();

      //Add header properties.
      Properties headerProps = request.getHeaderProperties();
      for(String propName : headerProps.stringPropertyNames()) {
         conn.setRequestProperty(propName, headerProps.getProperty(propName));
      }

      conn.setDoInput(true);
      conn.setDoOutput(true);
      DataOutputStream dos = null;
      FileInputStream fis = null;
      File file = new File(fileName);
      try {
         dos = new DataOutputStream(conn.getOutputStream());

         // Send a file
         String lineEnd = "\r\n";
         String twoHyphens = "--";
         dos.writeBytes(twoHyphens + QcRequest.BOUNDARY + lineEnd);
         dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                  + file.getName() + "\"" + lineEnd);
         dos.writeBytes(lineEnd);

         // create a buffer of maximum size
         int maxBufferSize = 1*1024*1024;
         fis = new FileInputStream(new File(fileName));
         int bytesAvailable = fis.available();
         int bufferSize = Math.min(bytesAvailable, maxBufferSize);
         byte[] buffer = new byte[bufferSize];

         // read file and write it into form.
         while (fis.read(buffer, 0, bufferSize)> 0)
         {
          dos.write(buffer, 0, bufferSize);
          bytesAvailable = fis.available();
          bufferSize = Math.min(bytesAvailable, maxBufferSize);
         }

         // send multipart form data necesssary after file data.
         dos.writeBytes(lineEnd);
         dos.writeBytes(twoHyphens + QcRequest.BOUNDARY + twoHyphens + lineEnd);
      } catch (Exception ex) {
         log.error("Got exception while uploading file", ex);
       } finally {
          // close streams
          try {
             if (fis != null) {
                fis.close();
             }
          } catch(IOException ioe) {
             log.error("Got exception while closing file input stream :" + ioe);
          }
          try {
             if (dos != null) {
                dos.flush();
                dos.close();
             }
          } catch(IOException ioe) {
             log.error("Got exception while closing output stream :" + ioe);
          }
       }

       if (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
          xmlResponse = new XMLConfiguration();
          xmlResponse.load(conn.getInputStream());
       } else {
         QcException qcException = new QcException("Response code :"
                  + conn.getResponseCode() + ", Error message :"
                  + QcUtil.readData(conn.getErrorStream()));
          throw qcException;
       }
       long opEndTime = System.currentTimeMillis();
       log.debug("Time taken to process QC request: {} secs",
               (opEndTime - opStartTime) / 1000);
       if (log.isTraceEnabled()) {
          log.trace("UPLOAD Response XML data :\n"
                   + ConfigurationUtils.toString(xmlResponse));
       }
       log.info("Log file result data =" + QcXmlConfigUtil.getLogFileInfo(xmlResponse));
       return xmlResponse;
   }

}
