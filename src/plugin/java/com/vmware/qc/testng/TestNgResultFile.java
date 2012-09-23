/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.testng;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.QcTestStatus;
import com.vmware.qc.file.ResultFile;
import com.vmware.qc.file.TestResultData;

/**
 * This class is used to parse testng-result.xml file and create TestResultData
 * objects.
 */
public class TestNgResultFile implements ResultFile {
    private static final Logger log = LoggerFactory.getLogger(TestNgResultFile.class);

    private String resultFile;

    /**
     * Set result file name.
     */
    public void setResultFileName(String resultFileName) {
        this.resultFile = resultFileName;
    }

    /**
     *
     * @return list of test result objects
     */
    public List<TestResultData> process() {
        List<TestResultData> testResults = new ArrayList<TestResultData>();
        try {
            TestNgResultParser resultParser = new TestNgResultParser(resultFile);

            Map<String, String> testResultMap = resultParser.getTestResultMap();
            Iterator<Entry<String, String>> testResultSet = testResultMap.entrySet().iterator();
            while (testResultSet.hasNext()) {
                TestResultData resultData = new TestResultData();
                Entry<String, String> entry = testResultSet.next();
                resultData.setTestName(entry.getKey());
                resultData.setTestSetName("");
                resultData.setTestStatus(toTestStatus(entry.getValue()));
                testResults.add(resultData);
            }
        } catch (FileNotFoundException fnfe) {
            log.error("File is not found :" + resultFile);
        } catch (IOException ioe) {
            log.error("Got an exception while reading file :", ioe);
        } catch (Exception e) {
            log.error("Got an exception while reading file :", e);
            e.printStackTrace();
        }
        return testResults;
    }

    /**
     * Map the result to QC test status and return it.
     */
    private QcTestStatus toTestStatus(String strStatus) {
        QcTestStatus status = null;
        if ("PASS".equals(strStatus)) {
            status = QcTestStatus.PASSED;
        } else if ("FAIL".equals(strStatus)) {
            status = QcTestStatus.FAILED;
        } else if ("SKIP".equals(strStatus)) {
            status = QcTestStatus.NOT_COMPLETED;
        }
        return status;
    }
}
