package com.vmware.qc;

/**
 * Contains log file information
 */
public class LogFileInfo
{
   private long runId;
   private String fileName;
   private String type;
   private long size;
   private String filePath;

   /**
    * Return test run id associated to the log file.
    *
    * @return run id.
    */
   public long getRunId()
   {
      return runId;
   }

   /**
    * Set test run id.
    *
    * @param runId
    */
   public void setRunId(long runId)
   {
      this.runId = runId;
   }

   /**
    * Return log file name in QC.
    *
    * @return file name.
    */
   public String getFileName()
   {
      return fileName;
   }

   /**
    * Set log file name.
    *
    * @param fileName file name
    */
   public void setFileName(String fileName)
   {
      this.fileName = fileName;
   }

   /**
    * Return log file type.
    *
    * @return file type.
    */
   public String getType()
   {
      return type;
   }

   /**
    * Set log file type.
    *
    * @param type file type.
    */
   public void setType(String type)
   {
      this.type = type;
   }

   /**
    * Return log file size.
    *
    * @return file size.
    */
   public long getSize()
   {
      return size;
   }

   /**
    * Set log file size.
    *
    * @param size file size.
    */
   public void setSize(long size)
   {
      this.size = size;
   }

   /**
    * Return file path in QC repository.
    *
    * @return file path.
    */
   public String getFilePath()
   {
      return filePath;
   }

   /**
    * Set file name.
    *
    * @param filePath
    */
   public void setFilePath(String filePath)
   {
      this.filePath = filePath;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("runId =" + runId)
        .append(", fileName =" + fileName)
        .append(", type =" + type)
        .append(", size =" + size)
        .append(", filePath =" + filePath);
      return sb.toString();
   }
}
