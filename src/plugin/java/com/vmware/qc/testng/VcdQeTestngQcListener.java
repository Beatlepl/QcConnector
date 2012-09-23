/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.testng;

import org.testng.ITestResult;


/**
 * VCD QE tests in TestPlan has a '.' separator instead of '-' and the
 * DataDriven test has data delimited by ','
 *
 *
 */
public class VcdQeTestngQcListener extends TestngQcListener {

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vmware.qc.testng.TestngQcListener#map2QcTestName(org.testng.ITestResult
     * )
     */
    @Override
    public String map2QcTestName(ITestResult res) {
        String methodName = res.getMethod().getMethodName();
        String testName = res.getTestClass().getName();
        String qcTestName = ("test".equals(methodName) ? testName : testName + "." + methodName);
        return qcTestName;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.vmware.qc.testng.TestngQcListener#map2QcTestInstanceName(org.testng
     * .ITestResult)
     */
    @Override
    public String map2QcTestInstanceName(ITestResult res) {
        String qcTestInstanceName = null;
        String parameters = "";
        if (res.getParameters() != null && res.getParameters().length > 0) {
            for (Object obj : res.getParameters()) {
                parameters = obj.toString() + ",";
            }
            parameters = parameters.substring(0, parameters.lastIndexOf(","));
            qcTestInstanceName = res.getTestClass().getName() + "." + res.getName() + "-" + parameters;
        }
        return qcTestInstanceName;
    }

}
