/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.qc.QcTestStatus;
import com.vmware.qc.file.ResultFile;
import com.vmware.qc.file.TestResultData;

/**
 * CsvResultFile and its operations
 *
 */
public class CsvResultFile implements ResultFile {
    private static final Logger log = LoggerFactory.getLogger(CsvResultFile.class);

    private String resultFile;

    /**
     * Set result file name.
     */
    public void setResultFileName(String resultFileName) {
        this.resultFile = resultFileName;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.vmware.qc.file.ResultFile#process()
     */
    @Override
    public List<TestResultData> process() {
        List<TestResultData> testResults = new ArrayList<TestResultData>();
        try {
            File csvFile = new File(resultFile);

            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line = null;
            String[] csvValues = null;
            while ((line = br.readLine()) != null) {

                TestResultData resultData = new TestResultData();
                csvValues = line.split(",");
                for (int i = 0; i < csvValues.length; i++) {
                    if (i == 0) {
                        if (csvValues[i].contains("{")) {
                            resultData.setTestName(csvValues[i].substring(0, csvValues[i].indexOf("{")));
                        } else {
                            resultData.setTestName(csvValues[i]);
                        }
                        resultData.setTestConfigId(csvValues[i]);
                    } else if (i == 1) {
                        resultData.setTestStatus(toTestStatus(csvValues[i]));
                    } else if (i == 2) {
                        resultData.setBugIds(Arrays.asList(csvValues[i].split(";")));
                    }
                }
                resultData.setTestSetName("");
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
     * Creates a csv file from the provided TestResultData
     *
     * @param pathWithName
     * @param testResultData
     * @throws Exception
     */
    public void createCsvResultFile(String pathWithName, List<TestResultData> testResultData) throws Exception {
        File csvFile = new File(pathWithName);
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(csvFile));
            for (TestResultData result : testResultData) {
                bw.write(result.getTestName() + "," + result.getTestStatus().toString() + "\n");
            }
            log.info("csv file location :" + csvFile.getAbsolutePath());
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

    /**
     * Map the result to QC test status and return it.
     */
    private QcTestStatus toTestStatus(String strStatus) {
        QcTestStatus status = null;
        if ("Passed".equals(strStatus)) {
            status = QcTestStatus.PASSED;
        } else if ("Failed".equals(strStatus)) {
            status = QcTestStatus.FAILED;
        } else if ("Skipped".equals(strStatus)) {
            status = QcTestStatus.NOT_COMPLETED;
        }
        return status;
    }
}
