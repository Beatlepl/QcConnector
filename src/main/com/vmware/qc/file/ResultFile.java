/**
 ***********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 ************************************************************************
 */
package com.vmware.qc.file;

import java.util.List;

/**
 * Interface that has the abstract methods to be implemented for Result parsing from various sources [ Example : Textfile, Excel File]
 * Will be implemented by the classes that provide parsing logic of results from the corresponding sources.
 */
public interface ResultFile
{
   public List<TestResultData> process();
   public void setResultFileName(String resultFileName);
}
