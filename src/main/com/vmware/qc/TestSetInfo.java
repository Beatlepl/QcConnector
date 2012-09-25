/* **********************************************************************
 * Copyright 2012 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 */
package com.vmware.qc;

/**
 * Contains test set information.
 */
public class TestSetInfo
{
   private long id;
   private String name;
   private long parentFolderId;

   /**
    * Get Test Set id.
    */
   public long getId()
   {
      return id;
   }

   /**
    * Set Test Set id.
    */
   public void setId(long id)
   {
      this.id = id;
   }

   /**
    * Get Test Set name.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Set Test Set name.
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * Get test set's parent folder id.
    */
   public long getParentFolderId()
   {
      return parentFolderId;
   }

   /**
    * Sets test set's parent folder id.
    */
   public void setParentFolderId(long parentFolderId)
   {
      this.parentFolderId = parentFolderId;
   }

   public String toString()
   {
      StringBuilder sb = new StringBuilder();
      sb.append("id =" + id)
        .append(", name = " + name)
        .append(", parentFolderId =" + parentFolderId);
      return sb.toString();
   }

}
