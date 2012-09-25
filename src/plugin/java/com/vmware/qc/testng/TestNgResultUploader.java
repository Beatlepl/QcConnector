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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanToPropertyValueTransformer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import com.vmware.qc.QcConnector;
import com.vmware.qc.QcConstants;
import com.vmware.qc.QcTestCase;
import com.vmware.qc.TestInstanceInfo;
import com.vmware.qc.csv.CsvResultFile;
import com.vmware.qc.file.PostResultFile2Qc;
import com.vmware.qc.file.ResultFile;
import com.vmware.qc.file.TestResultData;

/**
 *
 * Utility class which takes in commands to create csv file by parsing
 * testng-results.xml and also uploads results from both csv and
 * testng-results.xml
 *
 */
public class TestNgResultUploader {

    private static void appendToCsvFile(String csvFileName, List<QcTestCase> qcTestCases) throws Exception {
        File csvFile = new File(csvFileName);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(csvFile, true));
            for (QcTestCase qcTestCase : qcTestCases) {
                bw.write(qcTestCase.getName() + "," + "\n");
            }
            System.out.println("csv file location :" + csvFile.getAbsolutePath());
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static List<QcTestCase> getNonAutomatedTests(String[] testSetIdArr) throws Exception {
        List<QcTestCase> qcTestCases = getAllTestCasesFromTestSets(testSetIdArr);
        qcTestCases = (List<QcTestCase>) CollectionUtils.select(qcTestCases, new Predicate() {
            @Override
            public boolean evaluate(Object arg0) {
                return ((QcTestCase) arg0).getAutoLevel().equals("Automated");
            }
        });
        return qcTestCases;
    }

    public static List<QcTestCase> getAllTestCasesFromTestSets(String[] testSetIdArr) throws Exception {
        QcConnector connector = new QcConnector();
        List<String> testSetIds = Arrays.asList(testSetIdArr);
        List<Long> testSetIdsLong = (List<Long>) CollectionUtils.collect(testSetIds, new Transformer() {
            @Override
            public Object transform(Object arg0) {
                return Long.valueOf((String) arg0);
            }
        });
        List<TestInstanceInfo> testInstanceInfos = connector.getTestInstances(testSetIdsLong);
        List<Long> testIds =
                (List<Long>) CollectionUtils.collect(testInstanceInfos, new BeanToPropertyValueTransformer("testId"));
        return connector.getTestCases(testIds);

    }

    private static void parseTestNgResultCreateCsv(List<String> urlsOrFiles, String csvFileName) {
        List<TestResultData> testNgResultData = parseTestNgResultFromFilesOrUrls(urlsOrFiles);
        CsvResultFile csvResultFile = new CsvResultFile();
        try {
            System.out.println("Creating csv file...");
            csvResultFile.createCsvResultFile(csvFileName, testNgResultData);
            System.out.println("Successfully created csv file...");
        } catch (Exception e) {
            System.err.print("Error while creating csvResultFile " + e);
        }
    }

    private static List<TestResultData> parseTestNgResultFromFilesOrUrls(List<String> urlsOrFiles) {
        List<TestResultData> testNgResultData = new ArrayList<TestResultData>();
        try {
            for (String url : urlsOrFiles) {
                TestNgResultFile testNgResultFile = new TestNgResultFile();
                testNgResultFile.setResultFileName(url);
                System.out.println("Parsing testng-result.xml file..." + url);
                testNgResultData.addAll(testNgResultFile.process());
                System.out.println("Successfully parsed testng-result.xml...");
            }
        } catch (Exception e) {
            System.err.print("Error while reading/parsing testNgResultFile" + e);
        }
        return testNgResultData;
    }


    private static void compare(List<String> urlsOrFiles, String[] testSetIdArr) throws Exception {
        List<TestResultData> testNgResultData = parseTestNgResultFromFilesOrUrls(urlsOrFiles);
        List<String> testNgTests =
                (List<String>) CollectionUtils
                        .collect(testNgResultData, new BeanToPropertyValueTransformer("testName"));
        Collections.sort(testNgTests);

        List<QcTestCase> qcTestCases = getAllTestCasesFromTestSets(testSetIdArr);
        List<String> qcTestNames =
                (List<String>) CollectionUtils.collect(qcTestCases, new BeanToPropertyValueTransformer("name"));
        Collections.sort(qcTestNames);

        File csvFile = new File("testNgQcDiv.csv");
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(csvFile));

            for (String testNgTestName : testNgTests) {
                bw.write(testNgTestName + ",");
                // for(String qcTestName: qcTestNames){
                // if(qcTestName.contains(testNgTestName)){
                if (qcTestNames.contains(testNgTestName)) {
                    bw.write(testNgTestName);
                }

                // break;
                // }
                // }
                bw.write("\n");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

//
//    public static void main(String... args) throws Exception {
//
//        compare(Arrays
//                .asList(new String[] { "http://cloud-hudson.eng.vmware.com/view/All/job/ETC-QE/ws/workspace-jenkins-ETC-QE-97/results-server-daily1/testng-results.xml" }),
//                new String[] { "378" });
//
//    }


    /**
     * main method
     *
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        String usageString =
                "\n\nFor parsing a testng-results.xml file and create a csv file out of it use \n\t createCsvFile -resultsFile <coma separated fileNames or http Urls> -csvFile <csvFileName> \n "
                        + " For uploading results from .csv file or testng-results.xml file use \n \t uploadResult -resultsFile <fileName> -Dqc.testset.ids=<coma separted testSetIds> \n"
                        + " For appending the csv file with Non Automated tests from testSets \n\t appendCsvFile -csvFile <fileName> -Dqc.testset.ids=<coma separted testSetIds> \n";
        String resultsFile = null;
        String csvFile = null;
        List<String> argList = Arrays.asList(args);
        if (args != null && args.length > 0) {
            resultsFile = argList.get(argList.indexOf("-resultsFile") + 1);
            if (argList.contains("createCsvFile")) {
                csvFile = argList.get(argList.indexOf("-csvFile") + 1);
                TestNgResultUploader.parseTestNgResultCreateCsv(Arrays.asList(resultsFile.split(",")), csvFile);
            } else if (argList.contains("uploadResult")) {
                if (QcConstants.QC_TESTSET_IDS == null) {
                    System.err.println("Please provide coma separated testSetIds after -Dqc.testset.ids=");
                    System.exit(0);
                }
                ResultFile resultFile = null;
                if (resultsFile.endsWith(".csv")) {
                    resultFile = new CsvResultFile();
                } else if (resultsFile.endsWith(".xml")) {
                    resultFile = new TestNgResultFile();
                } else {
                    System.err.print("Invalid file format");
                    System.exit(0);
                }
                resultFile.setResultFileName(resultsFile);
                PostResultFile2Qc postResultFile2Qc = new PostResultFile2Qc();
                postResultFile2Qc.post2Qc(resultFile);
            } else if (argList.contains("appendCsvFile")) {
                if (QcConstants.QC_TESTSET_IDS == null) {
                    System.err.println("Please provide coma separated testSetIds after -Dqc.testset.ids=");
                    System.exit(0);
                }
                if (argList.indexOf("-csvFile") == 0) {
                    System.err.println("Please provide csv file to be appended as -csvFile <csvFilenameWithPath>");
                    System.exit(0);
                }
                List<QcTestCase> qcTestCases = getNonAutomatedTests(QcConstants.QC_TESTSET_IDS);
                if (qcTestCases != null && qcTestCases.size() > 0) {
                    appendToCsvFile("csvFile.csv", qcTestCases);
                    System.out.println("Appended the csv file successfully");
                } else {
                    System.out.println("No Non Automated tests found in the test set Id/s");
                }
            } else {
                System.err.println("Error: Usage :-" + usageString);
            }
        } else {
            System.err.println("Error: Usage :-" + usageString);
        }
    }

}
