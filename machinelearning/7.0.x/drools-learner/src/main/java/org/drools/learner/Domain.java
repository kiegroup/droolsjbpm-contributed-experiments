package org.drools.learner;

import java.util.ArrayList;
import java.util.Collections;

import org.drools.learner.tools.Util;

public class Domain {

    protected ArrayList<Object> fCategories;
    private boolean categorical, fixed, artificial, ignore;
    //private DataType dataType;
    private String   fName;
    private Class<?> fType, objKlass /* the class that attribute belongs to */; // , ownerKlass;		// not sure if necessary

    public Domain(Class<?> klass, String name, Class<?> type) {
        this.fName = name;
        this.fType = type;
        this.objKlass = klass;

        this.categorical = true; // BY DEFAULT, it is categorical
        this.artificial = false; // BY DEFAULT, it is a real field, if it is artificial it means there is no field exist but there is method which computes the value
        this.ignore = false;
        //this.dataType = DataType.PRIMITIVE; // BY DEFAULT it is primitive
        this.fCategories = new ArrayList<Object>(2);
    }

    public Domain cheapClone() {
        Domain dom = new Domain(this.objKlass, this.fName, this.fType);
        //dom.fixed = this.fixed;
        dom.categorical = this.categorical;
        //dom.readingSeq = readingSeq;
        dom.fCategories = this.fCategories;
        dom.ignore = this.ignore;

        return dom;
    }

    public Class<?> getFType() {
        return this.fType;
    }

    public String getFReferenceName() {
        return Util.getFReference(objKlass, fName);
    }

    public String getFName() {
        return this.fName;
    }

    protected void setFName(String name) {
        this.fName = name;
    }

    /**
     * Indicates that this domain cannot be changed after creation
     */
    boolean isFixed() {
        return this.fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    /**
     * Indicates that this domain has discrete set of values
     */
    public boolean isCategorical() {
        return this.categorical;
    }

    public void setCategorical(boolean cate) {
        this.categorical = cate;
    }

    public boolean isArtificial() {
        return this.artificial;
    }

    // if the field is a artificial field
    public void setArtificial(boolean b) {
        this.artificial = b;
    }
    //	public void setOwnerKlass(Class<?> owner_clazz) {
    //		 = owner_clazz;
    //	}

    public Class<?> getObjKlass() {
        return objKlass;
    }

    public void addCategory(Object value) {
        if (fixed) {
            return;
        }
        if (categorical) {
            if (!fCategories.contains(value)) {
                fCategories.add(value);
            }
        } else {
            return;
        }
    }

    public Object getCategory(int idx) {
        return fCategories.get(idx);
    }

    public int getCategoryCount() {
        return fCategories.size();
    }

    public boolean isPossible(Object value) throws Exception {
        //System.out.println("Domain.isPossible() start "+ value+ " ?");
        //		since the value is coming from the extractor i dont check the type	
        //		if (_value.getClass()!= this.fType)
        //			return false;
        //System.exit(0);
        if (fixed) {// if it is boolean type actually you do not need to check for the contains function {TRUE, FALSE}
            return this.containsValue(value);
        } else {
            return true;
        }
    }

    public boolean containsValue(Object value) throws Exception {
        if (categorical) {
            return this.fCategories.contains(value);
        } else {
            if (fCategories.isEmpty() || fCategories.size() == 1) {
                throw new Exception(" Domain " + fName + " is constant and not discrete but bounds are not set: possible values size: " + fCategories.size());
            }

            // they must be sorted 
            return (AttributeValueComparator.instance.compare(value, fCategories.get(0)) >= 0
                && AttributeValueComparator.instance.compare(value, fCategories.get(getCategoryCount() - 1)) <= 0);
            //return (nComparator.compare((Number)value, fValues.get(0)) >= 0 && nComparator.compare((Number)value, fValues.get(fValues.size()-1)) <= 0);

            /*
             * should i check if the value is in one of the intervals this is
             * necessary only if the intervals are unbroken
             */
        }
    }

    public Object getCategoryOf(Object value) {
        if (categorical) {
            return value;
        } else {

            int insertionPoint = Collections.binarySearch(this.fCategories, value, AttributeValueComparator.instance);
            /*
             * index of the search key, if it is contained in the list;
             * otherwise, (-(insertion point) - 1). The insertion point is
             * defined as the point at which the key would be inserted into the
             * list: the index of the first element greater than the key, or
             * list.size(), if all elements in the list are less than the
             * specified key. Note that this guarantees that the return value
             * will be >= 0 if and only if the key is found.
             */
            if (insertionPoint >= 0) {
                return this.fCategories.get(insertionPoint);
            } else {
                int unfoundInsertionPoint = -(insertionPoint) - 1;
                if (unfoundInsertionPoint >= this.fCategories.size()) {
                    //System.out.println("insestion point is the size domain "+this);
                    unfoundInsertionPoint = this.fCategories.size() - 1;
                }
                return this.fCategories.get(unfoundInsertionPoint);
            }
        }
    }

    public boolean ignore() {
        return ignore;
    }

    public void ignore(boolean ignoreField) {
        ignore = ignoreField;
    }

    public boolean isNotJustSelected(Domain exceptDomain) {
        if (this.objKlass.equals(exceptDomain.getObjKlass())) {
            return !this.getFName().equals(exceptDomain.getFName());
        }
        return true;
    }

    public int hashCode() {
        return objKlass.hashCode() ^ fName.hashCode() ^ fCategories.hashCode(); // TODO
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Domain domain = (Domain) o;

        if (!fCategories.equals(domain.fCategories)) {
            return false;
        }
        if (!fName.equals(domain.fName)) {
            return false;
        }
        return objKlass.equals(domain.objKlass);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(fName + "");
        //		for (Object v: fValues) {
        //			sb.append("-" + v);
        //		}
        return sb.toString();
    }
}
