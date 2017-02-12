/*
 * %W% %E%
 * 
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package javax.management.openmbean;


// java import
//
import java.io.Serializable;
import java.util.Set;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Arrays;


// jmx import
//


/**
 * The <tt>CompositeDataSupport</tt> class is the <i>open data</i> class which
 * implements the <tt>CompositeData</tt> interface.
 *
 * @version     %I%  %E%
 * @author      Sun Microsystems, Inc.
 *
 * @since 1.5
 * @since.unbundled JMX 1.1
 */
public class CompositeDataSupport 
    implements CompositeData, Serializable {

    /* Serial version */
    static final long serialVersionUID = 8003518976613702244L;

    /**
     * @serial Internal representation of the mapping of item names to their
     * respective values.
     *         A {@link SortedMap} is used for faster retrieval of elements.
     */
    private SortedMap<String, Object> contents = new TreeMap<String, Object>();

    /**
     * @serial The <i>composite type </i> of this <i>composite data</i> instance.
     */
    private CompositeType compositeType;
    
    /**
     * <p>
     * Constructs a <tt>CompositeDataSupport</tt> instance with the specified
     * <tt>compositeType</tt>, whose item values
     * are specified by <tt>itemValues[]</tt>, in the same order as in
     * <tt>itemNames[]</tt>. 
     * As a <tt>CompositeType</tt> does not specify any order on its items,
     * the <tt>itemNames[]</tt> parameter is used
     * to specify the order in which the values are given in <tt>itemValues[]</tt>. 
     * The items contained in this <tt>CompositeDataSupport</tt> instance are
     * internally stored in a <tt>TreeMap</tt>,
     * thus sorted in ascending lexicographic order of their names, for faster
     * retrieval of individual item values.
     * <p>
     * The constructor checks that all the constraints listed below for each
     * parameter are satisfied,
     * and throws the appropriate exception if they are not.
     * <p>
     * @param  compositeType  the <i>composite type </i> of this <i>composite
     * data</i> instance;
     *                        must not be null.
     * <p>
     * @param  itemNames  <tt>itemNames</tt> must list, in any order, all the
     * item names defined in <tt>compositeType</tt>;
     *                    the order in which the names are listed, is used to
     * match values in <tt>itemValues[]</tt>;
     *                    must not be null or empty. 
     * <p>
     * @param  itemValues  the values of the items, listed in the same order as
     * their respective names in <tt>itemNames</tt>;
     *                     each item value can be null, but if it is non-null it must be 
     *                     a valid value for the open type defined in <tt>compositeType</tt> for the corresponding item;
     *                     must be of the same size as <tt>itemNames</tt>; must not be null or empty.
     * <p>
     * @throws  IllegalArgumentException  <tt>compositeType</tt> is null, or <tt>itemNames[]</tt> or <tt>itemValues[]</tt> is null or empty,
     *                                    or one of the elements in <tt>itemNames[]</tt>  is a null or empty string,
     *                                    or <tt>itemNames[]</tt> and <tt>itemValues[]</tt> are not of the same size.
     * <p>
     * @throws  OpenDataException  <tt>itemNames[]</tt> or <tt>itemValues[]</tt>'s size differs from 
     *                             the number of items defined in <tt>compositeType</tt>,
     *                             or one of the elements in <tt>itemNames[]</tt> does not exist as an item name defined in <tt>compositeType</tt>,
     *                             or one of the elements in <tt>itemValues[]</tt> is not a valid value for the corresponding item 
     *                             as defined in <tt>compositeType</tt>.
     * <p>
     */
    public CompositeDataSupport(CompositeType compositeType, String[] itemNames, Object[] itemValues)
        throws OpenDataException {

        // Check compositeType is not null 
        //
        if (compositeType == null) {
            throw new IllegalArgumentException("Argument compositeType cannot be null.");
        }

        // item names defined in compositeType:
        Set<String> namesSet = compositeType.keySet();

        // Check the array itemNames is not null or empty (length!=0) and 
        // that there is no null element or empty string in it
        //
        checkForNullElement(itemNames, "itemNames");
        checkForEmptyString(itemNames, "itemNames");

        // Check the array itemValues is not null or empty (length!=0)
        // (NOTE: we allow null values as array elements) 
        //
        if ( (itemValues == null) || (itemValues.length == 0) ) {
            throw new IllegalArgumentException("Argument itemValues[] cannot be null or empty.");
        }

        // Check that the sizes of the 2 arrays itemNames and itemValues are the same
        //
        if (itemNames.length != itemValues.length) {
            throw new IllegalArgumentException("Array arguments itemNames[] and itemValues[] "+
                                               "should be of same length (got "+ itemNames.length +
                                               " and "+ itemValues.length +").");
        }
        
        // Check the size of the 2 arrays is equal to the number of items defined in compositeType
        //
        if (itemNames.length != namesSet.size()) {
            throw new OpenDataException("The size of array arguments itemNames[] and itemValues[] should be equal to the number of items defined"+
                                        " in argument compositeType (found "+ itemNames.length +" elements in itemNames[] and itemValues[],"+
                                        " expecting "+ namesSet.size() +" elements according to compositeType.");
        }

        // Check parameter itemNames[] contains all names defined in the compositeType of this instance
        //
        if ( ! Arrays.asList(itemNames).containsAll(namesSet) ) {
            throw new OpenDataException("Argument itemNames[] does not contain all names defined in the compositeType of this instance.");
        }
        
        // Check each element of itemValues[], if not null, is of the open type defined for the corresponding item
        //
        OpenType<?> itemType;
        for (int i=0; i<itemValues.length; i++) {
            itemType = compositeType.getType(itemNames[i]);
            if ( (itemValues[i] != null) && (! itemType.isValue(itemValues[i])) ) {
                throw new OpenDataException("Argument's element itemValues["+ i +"]=\""+ itemValues[i] +"\" is not a valid value for"+
                                            " this item (itemName="+ itemNames[i] +",itemType="+ itemType +").");
            }
        }
        
        // Initialize internal fields: compositeType and contents 
        //
        this.compositeType = compositeType;
        for (int i=0; i<itemNames.length; i++) {
            this.contents.put(itemNames[i], itemValues[i]);
        }
    }

    /**
     * <p>
     * Constructs a <tt>CompositeDataSupport</tt> instance with the specified <tt>compositeType</tt>, whose item names and corresponding values
     * are given by the mappings in the map <tt>items</tt>.
     * This constructor converts the keys to a string array and the values to an object array and calls
     * <tt>CompositeDataSupport(javax.management.openmbean.CompositeType, java.lang.String[], java.lang.Object[])</tt>.
     * <p>
     * @param  compositeType  the <i>composite type </i> of this <i>composite data</i> instance;
     *                        must not be null.
     * <p>
     * @param  items  the mappings of all the item names to their values;
     *                <tt>items</tt> must contain all the item names defined in <tt>compositeType</tt>;
     *                must not be null or empty. 
     * <p>
     * @throws  IllegalArgumentException  <tt>compositeType</tt> is null, or <tt>items</tt> is null or empty,
     *                                    or one of the keys in <tt>items</tt>  is a null or empty string,
     *                                    or one of the values in <tt>items</tt>  is null.
     * <p>
     * @throws  OpenDataException  <tt>items</tt>' size differs from the number of items defined in <tt>compositeType</tt>,
     *                             or one of the keys in <tt>items</tt> does not exist as an item name defined in <tt>compositeType</tt>,
     *                             or one of the values in <tt>items</tt> is not a valid value for the corresponding item 
     *                             as defined in <tt>compositeType</tt>.
     * <p>
     * @throws ArrayStoreException  one or more keys in <tt>items</tt> is not of the class <tt>java.lang.String</tt>.
     * <p>
     */
    public CompositeDataSupport(CompositeType compositeType,
                                Map<String,?> items)
            throws OpenDataException {


        // Let the other constructor do the job, as the call to another constructor must be the first call
        //
        this( compositeType, 
              (items==null  ?  null  :  items.keySet().toArray(new String[items.size()])), // may raise an ArrayStoreException
              (items==null  ?  null  :  items.values().toArray()) );
    }

    /**
     *
     */
    private static void checkForNullElement(Object[] arg, String argName) {
        if ( (arg == null) || (arg.length == 0) ) {
            throw new IllegalArgumentException(
                       "Argument "+ argName +"[] cannot be null or empty.");
        }
        for (int i=0; i<arg.length; i++) {
            if (arg[i] == null) {
                throw new IllegalArgumentException(
                       "Argument's element "+ argName +"["+ i +"] cannot be null.");
            }
        }
    }

    /**
     *
     */
    private static void checkForEmptyString(String[] arg, String argName) {
        for (int i=0; i<arg.length; i++) {
            if (arg[i].trim().equals("")) {
                throw new IllegalArgumentException(
                  "Argument's element "+ argName +"["+ i +"] cannot be an empty string.");
            }
        }
    }

    /**
     * Returns the <i>composite type </i> of this <i>composite data</i> instance.
     */
    public CompositeType getCompositeType() {

        return compositeType;
    }

    /**
     * Returns the value of the item whose name is <tt>key</tt>.
     *
     * @throws IllegalArgumentException  if <tt>key</tt> is a null or empty String.
     *
     * @throws InvalidKeyException  if <tt>key</tt> is not an existing item name for
     * this <tt>CompositeData</tt> instance.
     */
    public Object get(String key) {

        if ( (key == null) || (key.trim().equals("")) ) {
            throw new IllegalArgumentException("Argument key cannot be a null or empty String.");
        }
        if ( ! contents.containsKey(key.trim())) {
            throw new InvalidKeyException("Argument key=\""+ key.trim() +"\" is not an existing item name for this CompositeData instance.");
        }
        return contents.get(key.trim());
    }

    /**
     * Returns an array of the values of the items whose names are specified by
     * <tt>keys</tt>, in the same order as <tt>keys</tt>.
     *
     * @throws IllegalArgumentException  if an element in <tt>keys</tt> is a null
     * or empty String.
     *
     * @throws InvalidKeyException  if an element in <tt>keys</tt> is not an existing
     * item name for this <tt>CompositeData</tt> instance.
     */
    public Object[] getAll(String[] keys) {

        if ( (keys == null) || (keys.length == 0) ) {
            return new Object[0];
        }
        Object[] results = new Object[keys.length];
        for (int i=0; i<keys.length; i++) {
            results[i] = this.get(keys[i]);
        }
        return results;
    }

    /**
     * Returns <tt>true</tt> if and only if this <tt>CompositeData</tt> instance contains 
     * an item whose name is <tt>key</tt>. 
     * If <tt>key</tt> is a null or empty String, this method simply returns false.
     */
    public boolean containsKey(String key) { 

        if ( (key == null) || (key.trim().equals("")) ) {
            return false;
        }
        return contents.containsKey(key);
    }

    /**
     * Returns <tt>true</tt> if and only if this <tt>CompositeData</tt> instance
     * contains an item 
     * whose value is <tt>value</tt>.
     */
    public boolean containsValue(Object value) { 

        return contents.containsValue(value);
    }

    /**
     * Returns an unmodifiable Collection view of the item values contained in this
     * <tt>CompositeData</tt> instance.
     * The returned collection's iterator will return the values in the ascending
     * lexicographic order of the corresponding 
     * item names. 
     */
    public Collection<?> values() { 

        return Collections.unmodifiableCollection(contents.values());
    }

    /**
     * Compares the specified <var>obj</var> parameter with this
     * <code>CompositeDataSupport</code> instance for equality. 
     * <p>
     * Returns <tt>true</tt> if and only if all of the following statements are true:
     * <ul>
     * <li><var>obj</var> is non null,</li>
     * <li><var>obj</var> also implements the <code>CompositeData</code> interface,</li>
     * <li>their composite types are equal</li>
     * <li>their contents, i.e. (name, value) pairs are equal. If a value contained in
     * the content is an array, the value comparison is done as if by calling
     * the {@link java.util.Arrays#deepEquals(Object[], Object[]) deepEquals} method
     * for arrays of object reference types or the appropriate overloading of
     * {@code Arrays.equals(e1,e2)} for arrays of primitive types</li>
     * </ul>
     * <p>
     * This ensures that this <tt>equals</tt> method works properly for
     * <var>obj</var> parameters which are different implementations of the
     * <code>CompositeData</code> interface, with the restrictions mentioned in the 
     * {@link java.util.Collection#equals(Object) equals} 
     * method of the <tt>java.util.Collection</tt> interface.
     *
     * @param  obj  the object to be compared for equality with this
     * <code>CompositeDataSupport</code> instance.
     * @return  <code>true</code> if the specified object is equal to this
     * <code>CompositeDataSupport</code> instance.
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        // if obj is not a CompositeData, return false
        if (!(obj instanceof CompositeData)) {
            return false;
        }

        CompositeData other = (CompositeData) obj;

        // their compositeType should be equal
        if (!this.getCompositeType().equals(other.getCompositeType()) ) {
            return false;
        }

        if (contents.size() != other.values().size()) {
            return false;
        }

        for (Map.Entry<String,Object> entry : contents.entrySet()) {
            Object e1 = entry.getValue();
            Object e2 = other.get(entry.getKey());

            if (e1 == e2)
                continue;
            if (e1 == null)
                return false;

            boolean eq = e1.getClass().isArray() ?
                Arrays.deepEquals(new Object[] {e1}, new Object[] {e2}) :
                e1.equals(e2);
 
            if (!eq)
                return false;
        }

        // All tests for equality were successful
        //
        return true;
    }

    /**
     * Returns the hash code value for this <code>CompositeDataSupport</code> instance. 
     * <p>
     * The hash code of a <code>CompositeDataSupport</code> instance is the sum of the hash codes 
     * of all elements of information used in <code>equals</code> comparisons
     * (ie: its <i>composite type</i> and all the item values). 
     * <p>
     * This ensures that <code> t1.equals(t2) </code> implies that <code> t1.hashCode()==t2.hashCode() </code>
     * for any two <code>CompositeDataSupport</code> instances <code>t1</code> and <code>t2</code>,
     * as required by the general contract of the method
     * {@link Object#hashCode() Object.hashCode()}.
     * <p>
     * Each item value's hash code is added to the returned hash code.
     * If an item value is an array,
     * its hash code is obtained as if by calling the
     * {@link java.util.Arrays#deepHashCode(Object[]) deepHashCode} method
     * for arrays of object reference types or the appropriate overloading
     * of {@code Arrays.hashCode(e)} for arrays of primitive types.
     *
     * @return the hash code value for this <code>CompositeDataSupport</code> instance
     */
    public int hashCode() {
        int hashcode = compositeType.hashCode();

        for (Object o : contents.values()) {
            if (o instanceof Object[])
                hashcode += Arrays.deepHashCode((Object[]) o);
            else if (o instanceof byte[])
                hashcode += Arrays.hashCode((byte[]) o);
            else if (o instanceof short[])
                hashcode += Arrays.hashCode((short[]) o);
            else if (o instanceof int[])
                hashcode += Arrays.hashCode((int[]) o);
            else if (o instanceof long[])
                hashcode += Arrays.hashCode((long[]) o);
            else if (o instanceof char[])
                hashcode += Arrays.hashCode((char[]) o);
            else if (o instanceof float[])
                hashcode += Arrays.hashCode((float[]) o);
            else if (o instanceof double[])
                hashcode += Arrays.hashCode((double[]) o);
            else if (o instanceof boolean[])
                hashcode += Arrays.hashCode((boolean[]) o);
            else if (o != null)
                hashcode += o.hashCode();
        }

        return hashcode;
    }

    /**
     * Returns a string representation of this <code>CompositeDataSupport</code> instance. 
     * <p>
     * The string representation consists of the name of this class (ie <code>javax.management.openmbean.CompositeDataSupport</code>), 
     * the string representation of the composite type of this instance, and the string representation of the contents
     * (ie list the itemName=itemValue mappings).
     * 
     * @return  a string representation of this <code>CompositeDataSupport</code> instance
     */
    public String toString() { 

        return new StringBuffer()
            .append(this.getClass().getName())
            .append("(compositeType=")
            .append(compositeType.toString())
            .append(",contents=")
            .append(contents.toString())
            .append(")")
            .toString();
    }

}
