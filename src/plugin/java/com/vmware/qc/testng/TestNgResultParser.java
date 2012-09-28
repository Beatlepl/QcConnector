/* **********************************************************************
 * Copyright 2011 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 * $Id$
 * $DateTime$
 * $Change$
 * $Author$
 * ********************************************************************
 */

package com.vmware.qc.testng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.vmware.qc.testng.util.XmlParser;


/**
 * Utility class which parses the testng-results.xml file
 *
 */
public class TestNgResultParser {

    private XmlParser configParser = null;
    private String testNgResultFileWithPath = null;
    private Node testNode = null;

    private TestNgResultParser() {

    }

    public TestNgResultParser(String testNgResultFileWithPath) throws Exception {
        this.testNgResultFileWithPath = testNgResultFileWithPath;
        this.configParser = getConfigParser();
        testNode = configParser.getNode("//testng-results/suite/test", null);
    }

    /**
     * Parses testng-results.xml file for testnames and results and creates a
     * Map of the same
     *
     * @return Map
     * @throws Exception
     */
    public Map<String, String> getTestResultMap() throws Exception {
        Map<String, String> testResultMap = new LinkedHashMap<String, String>();
        NodeList classNodes = configParser.getNodeList("class", testNode);
        if (null != classNodes && classNodes.getLength() > 0) {
            for (int i = 0; i < classNodes.getLength(); i++) {
                NodeList testNodes = configParser.getNodeList("test-method", classNodes.item(i));
                if (null != testNodes && testNodes.getLength() > 0) {
                    for (int j = 0; j < testNodes.getLength(); j++) {
                        if (testNodes.item(j).getAttributes().getNamedItem("is-config") == null) {
                            String fdqTestName = classNodes.item(i).getAttributes().getNamedItem("name").getNodeValue();
                            String shortTestName = fdqTestName.substring(fdqTestName.indexOf("tests.server") + 13);
                            Node paramsNode = configParser.getNode("params", testNodes.item(j));
                            if(null != paramsNode){
                                String firstParamValue = configParser.getString("param/value",paramsNode);
                                testResultMap.put(
                                        shortTestName + "."
                                                + testNodes.item(j).getAttributes().getNamedItem("name").getNodeValue()+"-"+firstParamValue.trim(),
                                        testNodes.item(j).getAttributes().getNamedItem("status").getNodeValue());
                            } else {
                            testResultMap.put(
                                    shortTestName + "."
                                            + testNodes.item(j).getAttributes().getNamedItem("name").getNodeValue(),
                                    testNodes.item(j).getAttributes().getNamedItem("status").getNodeValue());
                            }
                        }
                    }
                }

            }
        }
        return testResultMap;
    }

    /**
     * Returns the config parser
     *
     * @return
     */
    private XmlParser getConfigParser() {

        XmlParser configParser = new XmlParser();
        try {
            if (isValidUrl(testNgResultFileWithPath)) {
                configParser.loadString(getXmlStringFromUrl(testNgResultFileWithPath));
            } else {
                configParser.loadFile(testNgResultFileWithPath);
            }
        } catch (Exception e) {
            System.out.println("Error loading the config.xml" + e);
            e.printStackTrace();
        }
        return configParser;
    }


    private boolean isValidUrl(String urlString) {
        boolean isUrl = false;
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            conn.connect();
            isUrl = true;
        } catch (MalformedURLException e) {
            // the URL is not in a valid form
        } catch (IOException e) {
            // the connection couldn't be established
        }
        return isUrl;
    }

    private String getXmlStringFromUrl(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        BufferedReader reader = null;
        StringBuffer xmlStr = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                xmlStr.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }
        return xmlStr.toString();
    }

}
