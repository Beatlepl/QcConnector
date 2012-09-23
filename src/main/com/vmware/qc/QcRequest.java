/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * The object of this class encapsulates HTTP request information like
 *  resource path(URI),
 *  request parameters,
 *  request headers,
 *  authentication key.
 *
 * The constructed request object is sent to QC to avail its exposed functionalities using QCRestClient.
 */
public class QcRequest
{
   private String uri;
   private Map<String, List<Object>> fields = new Hashtable<String, List<Object>>();
   private Map<String, List<Object>> customFields = new Hashtable<String, List<Object>>();
   private Properties headerProps = new Properties();
   private String requestBody = null;
   public static final String BOUNDARY = "*****";

   /**
    * Constructor.
    * Add web service authentication API key to header by default.
    */
   public QcRequest(String uri)
   {
      this.uri = uri;
      headerProps.put("APIKey", QcConstants.QC_WEBSERVICE_APIKEY);
   }

   /**
    * Return url path that also includes query string composed of field-value pairs.
    *
    * @return url.
    */
   public String getURL()
   {
      String queryString = getQueryString();
      return (queryString != null ? this.uri + "?" + queryString : this.uri);
   }

   /**
    * Add field name & its value.
    *
    * @param fieldKey field name
    * @param fieldValue field value
    */
   public void addField(String fieldName,
                            Object fieldValue)
   {
      fields.put(fieldName, Arrays.asList(fieldValue));
   }

   /**
    * Add field name & its multiple values.
    *
    * @param fieldName field name
    * @param fieldValue field values.
    */
   public void addField(String fieldName,
                            List fieldValues)
   {
      fields.put(fieldName, fieldValues);
   }

   /**
    * Add custom field name & its value.
    *
    * @param customFieldName custom field name.
    * @param customFieldValue custome field value.
    */
   public void addCustomField(String customFieldName,
                              Object customFieldValue)
   {
      customFields.put(customFieldName, Arrays.asList(customFieldValue));
   }

   /**
    * Add custom field name & its values.
    *
    * @param customFieldName custom field name.
    * @param customFieldValues custom field values.
    */
   public void addCustomField(String customFieldName,
                              List customFieldValues)
   {
      customFields.put(customFieldName, customFieldValues);
   }

   /**
    * Add header property name and its value.
    *
    * @param propName header property name.
    * @param propValue hearder property value.
    */
   public void addHeaderProperty(String propName,
                                 String propValue)
   {
      headerProps.put(propName, propValue);
   }

   /**
    * Get a list of header properties.
    *
    * @return list of header properties.
    */
   public Properties getHeaderProperties()
   {
      return headerProps;
   }

   /**
    * Return default header properties.
    */
   public Properties getDefaultHeaderProperties()
   {
      Properties defaultProps = new Properties();
      defaultProps.setProperty("APIKey", QcConstants.QC_WEBSERVICE_APIKEY);
      defaultProps.setProperty("Accept", "application/xml");
      return defaultProps;
   }

   /**
    * Build query string composed of field-value pairs in the following format.
    * field-name1=value1&field-name2=value2,value3...
    * &customFields={customfield-name1[custom-value1],customfield-name2[custom-value2;custom-value3;...],...}
    *
    * @return query string
    */
   public String getQueryString()
   {
      StringBuilder queryString = new StringBuilder();
      if (!fields.isEmpty()) {
         Iterator<Map.Entry<String, List<Object>>> meIt = fields.entrySet().iterator();
         while(meIt.hasNext()) {
            Map.Entry<String, List<Object>> me = meIt.next();
            queryString.append(me.getKey() + "=");
            Iterator listIt = me.getValue().iterator();
            while(listIt.hasNext()) {
               Object fieldValue = listIt.next();
               try {
                  queryString.append(URLEncoder.encode(fieldValue.toString(), "UTF-8"));
               } catch(Exception ex) { }
               if (listIt.hasNext()) {
                  queryString.append(",");
               }
            }
            if (meIt.hasNext()) {
               queryString.append("&");
            }
         }
      }
      if(!customFields.isEmpty()) {
         //queryString append comes here also.
      }
      return !queryString.equals("") ? queryString.toString() : null;
   }

    /**
     * @return the requestBody
     */
    public String getRequestBody() {
        return requestBody;
    }

    /**
     * @param requestBody
     *            the requestBody to set
     */
    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

}
