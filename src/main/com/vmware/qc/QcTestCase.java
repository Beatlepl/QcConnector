/* **********************************************************************
 * Copyright 2011 VMware, Inc. All rights reserved. VMware Confidential
 * **********************************************************************
 * $Id$
 * $DateTime$
 * $Change$
 * $Author$
 * ********************************************************************
 */

package com.vmware.qc;

/**
 * @author bpaul
 *
 */
public class QcTestCase {

    private String id = null;
    private String name = null;
    private String product = null;
    private String funcArea = null;
    private String component = null;
    private String autoLevel = null;
    private String priority = null;

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the product
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param product
     *            the product to set
     */
    public void setProduct(String product) {
        this.product = product;
    }

    /**
     * @return the funcArea
     */
    public String getFuncArea() {
        return funcArea;
    }

    /**
     * @param funcArea
     *            the funcArea to set
     */
    public void setFuncArea(String funcArea) {
        this.funcArea = funcArea;
    }

    /**
     * @return the component
     */
    public String getComponent() {
        return component;
    }

    /**
     * @param component
     *            the component to set
     */
    public void setComponent(String component) {
        this.component = component;
    }

    /**
     * @return the autoLevel
     */
    public String getAutoLevel() {
        return autoLevel;
    }

    /**
     * @param autoLevel
     *            the autoLevel to set
     */
    public void setAutoLevel(String autoLevel) {
        this.autoLevel = autoLevel;
    }

    /**
     * @return the priority
     */
    public String getPriority() {
        return priority;
    }

    /**
     * @param priority
     *            the priority to set
     */
    public void setPriority(String priority) {
        this.priority = priority;
    }


}
