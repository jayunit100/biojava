/*
 *                    BioJava development code
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  If you do not have a copy,
 * see:
 *
 *      http://www.gnu.org/copyleft/lesser.html
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.bio.seq.io.filterxml;

import java.io.*;
import java.util.*;
import java.lang.reflect.*;

import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.utils.xml.*;

/**
 * Main class for writing FeatureFilters as XML.
 *
 * @author Thomas Down
 * @since 1.3
 */

public class XMLFilterWriterConfig {
    public static final String XML_FILTER_NS = "http://www.biojava.org/FeatureFilter";
    private static final Class[] NO_ARGS = new Class[0];
    private static final Object[] NO_ARGS_VAL = new Object[0];
    
    private Map filterWritersByClass = new HashMap();
    private boolean strict = false;
    
    /**
     * Construct a new <code>XMLFilterWriterConfig</code> which can serialize the buildin types of
     * <code>FeatureFilter</code>.
     */
    
    public XMLFilterWriterConfig() {
        try {
            filterWritersByClass.put(
                FeatureFilter.all,
                new BlankFilterWriter(XML_FILTER_NS, "all")
            );
            filterWritersByClass.put(
                FeatureFilter.none,
                new BlankFilterWriter(XML_FILTER_NS, "none")
            );
            filterWritersByClass.put(
                FeatureFilter.ByType.class,
                new StringFilterWriter(XML_FILTER_NS, "byType", FeatureFilter.ByType.class.getMethod("getType", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.BySource.class,
                new StringFilterWriter(XML_FILTER_NS, "bySource", FeatureFilter.BySource.class.getMethod("getSource", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.ByClass.class,
                new ByClassFilterWriter()
            );
            filterWritersByClass.put(
                FeatureFilter.ContainedByLocation.class,
                new LocationFilterWriter(XML_FILTER_NS, "containedByLocation", FeatureFilter.ContainedByLocation.class.getMethod("getLocation", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.OverlapsLocation.class,
                new LocationFilterWriter(XML_FILTER_NS, "overlapsLocation", FeatureFilter.OverlapsLocation.class.getMethod("getLocation", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.Not.class,
                new FilterFilterWriter(XML_FILTER_NS, "not", FeatureFilter.Not.class.getMethod("getChild", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.And.class,
                new AndFilterWriter()
            );
            filterWritersByClass.put(
                FeatureFilter.Or.class,
                new OrFilterWriter()
            );
            filterWritersByClass.put(
                FeatureFilter.Or.class,
                new StrandFilterWriter()
            );
            filterWritersByClass.put(
                FeatureFilter.ByParent.class,
                new FilterFilterWriter(XML_FILTER_NS, "byParent", FeatureFilter.ByParent.class.getMethod("getFilter", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.ByAncestor.class,
                new FilterFilterWriter(XML_FILTER_NS, "byAncestor", FeatureFilter.ByAncestor.class.getMethod("getFilter", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.ByComponentName.class,
                new StringFilterWriter(XML_FILTER_NS, "byComponentName", FeatureFilter.ByComponentName.class.getMethod("getComponentName", NO_ARGS))
            );
            filterWritersByClass.put(
                FeatureFilter.IsTopLevel.class,
                new BlankFilterWriter(XML_FILTER_NS, "isTopLevel")
            );
            AnnotationTypeFilterWriter atfw = new AnnotationTypeFilterWriter(new XMLAnnotationTypeWriter());
            filterWritersByClass.put(
                FeatureFilter.ByAnnotationType.class,
                atfw
            );
            filterWritersByClass.put(
                FeatureFilter.ByAnnotation.class,
                atfw
            );
            filterWritersByClass.put(
                FeatureFilter.HasAnnotation.class,
                atfw
            );
        } catch (Exception ex) {
            throw new BioError(ex, "Assertion failed: couldn't initialize XMLFilterWriters");
        }
    }
    
    /**
     * Add a writer for the specified class of filters
     */
    
    public void addXMLFilterWriter(Class clazz, XMLFilterWriter xfw) {
        filterWritersByClass.put(clazz, xfw);
    }
    
    /**
     * Add a writer for a singleton filter.
     */
     
    public void addXMLFilterWriter(FeatureFilter ff, XMLFilterWriter xfw) {
        filterWritersByClass.put(ff, xfw);
    }
    
    /**
     * Determine if this writer is in strict mode.
     */
    
    public boolean isStrict() {
        return strict;
    }
    
    /**
     * Selects strict mode.  In strict mode, the writer will throw an <code>IllegalArgumentException</code>
     * if it encounters a type of <code>FeatureFilter</code> it doesn't recognize.  When not
     * in strict model, unrecognized filters are silently replaced by <code>FeatureFilter.all</code>.
     * Default is <code>false</code>.
     */
    
    public void setIsStrict(boolean b) {
        this.strict = b;
    }
    
    /**
     * Write a FeatureFilter to the supplied XMLWriter
     *
     * @throws IllegalArgumentException if the FeatureFilter is unrecognized, and the writer is 
     *                                  in strict mode.
     * @throws IOException if an error occurs while outputting XML.
     */
    
    public void writeFilter(FeatureFilter ff, XMLWriter xw) 
        throws IllegalArgumentException, IOException
    {
        XMLFilterWriter xfw = (XMLFilterWriter) filterWritersByClass.get(ff);
        if (xfw == null) {
            xfw = (XMLFilterWriter) filterWritersByClass.get(ff.getClass());
        }
        if (xfw != null) {
            try {
                xfw.writeFilter(ff, xw, this);
            } catch (ClassCastException ex) {
                throw new BioError(ex, "Filter type mismatch");
            }
        } else {
            if (strict) {
                throw new IllegalArgumentException("Couldn't find a writer for filter of type " + ff.getClass().getName());
            } else {
                writeFilter(FeatureFilter.all, xw);
            }
        }
    }
    
    private static class BlankFilterWriter implements XMLFilterWriter {
        private String nsURI;
        private String localName;
        
        BlankFilterWriter(String nsURI, String localName) {
            this.nsURI = nsURI;
            this.localName =  localName;
        }
        
        public void writeFilter(FeatureFilter ff,
                                XMLWriter xw,
                                XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            xw.openTag(nsURI, localName);
            xw.closeTag(nsURI, localName);
        }
    }
    
    private static abstract class PropertyFilterWriter implements XMLFilterWriter {
        private final String nsURI;
        private final String localName;
        private final Method propMethod;
        
        PropertyFilterWriter(String nsURI,
                             String localName,
                             Method propMethod) 
        {
            this.nsURI = nsURI;
            this.localName =  localName;
            this.propMethod = propMethod;
        }
        
        public void writeFilter(FeatureFilter ff,
                                XMLWriter xw,
                                XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            try {
                Object obj = propMethod.invoke(ff, NO_ARGS_VAL);
                xw.openTag(nsURI, localName);
                writeContent(obj, xw, config);
                xw.closeTag(nsURI, localName);
            } catch (IllegalAccessException ex) {
                throw new BioError(ex, "Can't access property method");
            } catch (InvocationTargetException ex) {
                throw new BioError(ex, "Coudln't access property");
            }
        }
        
        protected abstract void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException;
    }
    
    private static class StringFilterWriter extends PropertyFilterWriter {
        StringFilterWriter(String nsURI,
                           String localName,
                           Method propMethod) 
        {
            super(nsURI, localName, propMethod);
        }
        
        protected void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            xw.print(obj.toString());
        }
    }
    
    private static class LocationFilterWriter extends PropertyFilterWriter {
        LocationFilterWriter(String nsURI,
                           String localName,
                           Method propMethod) 
        {
            super(nsURI, localName, propMethod);
        }
        
        protected void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            Location l = (Location) obj;
            for (Iterator i = l.blockIterator(); i.hasNext(); ) {
                Location block = (Location) i.next();
                xw.openTag("span");
                xw.attribute("start", "" + block.getMin());
                xw.attribute("stop", "" + block.getMax());
                xw.closeTag("span");
            }   
        }
    }
    
    private static class FilterFilterWriter extends PropertyFilterWriter {
        FilterFilterWriter(String nsURI,
                           String localName,
                           Method propMethod) 
        {
            super(nsURI, localName, propMethod);
        }
        
        protected void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            FeatureFilter ff = (FeatureFilter) obj;
            config.writeFilter(ff, xw);
        }
    }
    
    private static class AndFilterWriter implements XMLFilterWriter {
        public void writeFilter(FeatureFilter ff,
                                XMLWriter xw,
                                XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            FeatureFilter.And ffa = (FeatureFilter.And) ff;
            xw.openTag(XML_FILTER_NS, "and");
            writeSubFilter(ff, xw, config);
            xw.closeTag(XML_FILTER_NS, "and");
        }
        
        private void writeSubFilter(FeatureFilter ff,
                                    XMLWriter xw,
                                    XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            if (ff instanceof FeatureFilter.And) {
                FeatureFilter.And ffa = (FeatureFilter.And) ff;
                writeSubFilter(ffa.getChild1(), xw, config);
                writeSubFilter(ffa.getChild2(), xw, config);
            } else {
                config.writeFilter(ff, xw);
            }
        }
    }
    
    private static class OrFilterWriter implements XMLFilterWriter {
        public void writeFilter(FeatureFilter ff,
                                XMLWriter xw,
                                XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            FeatureFilter.Or ffa = (FeatureFilter.Or) ff;
            xw.openTag(XML_FILTER_NS, "or");
            writeSubFilter(ff, xw, config);
            xw.closeTag(XML_FILTER_NS, "or");
        }
        
        private void writeSubFilter(FeatureFilter ff,
                                    XMLWriter xw,
                                    XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            if (ff instanceof FeatureFilter.Or) {
                FeatureFilter.Or ffa = (FeatureFilter.Or) ff;
                writeSubFilter(ffa.getChild1(), xw, config);
                writeSubFilter(ffa.getChild2(), xw, config);
            } else {
                config.writeFilter(ff, xw);
            }
        }
    }
    
    private static class ByClassFilterWriter extends PropertyFilterWriter {
        ByClassFilterWriter()
            throws NoSuchMethodException
        {
                super(XML_FILTER_NS, "byClass", FeatureFilter.ByClass.class.getMethod("getTestClass", NO_ARGS));
        }
        
        protected void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            xw.print(((Class) obj).getName());
        }
    }
    
    private static class StrandFilterWriter extends PropertyFilterWriter {
        StrandFilterWriter()
            throws NoSuchMethodException
        {
                super(XML_FILTER_NS, "byStrand", FeatureFilter.StrandFilter.class.getMethod("getStrand", NO_ARGS));
        }
        
        protected void writeContent(Object obj,
                                             XMLWriter xw,
                                             XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            xw.print(((StrandedFeature.Strand) obj).toString());
        }
    }
    
    private static class AnnotationTypeFilterWriter extends PropertyFilterWriter {
        private XMLAnnotationTypeWriter xatw;
        
        AnnotationTypeFilterWriter(XMLAnnotationTypeWriter xatw)
            throws NoSuchMethodException
        {
                super(XML_FILTER_NS, "byAnnotationType", FeatureFilter.ByAnnotationType.class.getMethod("getType", NO_ARGS));
                this.xatw = xatw;
        }
        
        protected void writeContent(Object obj,
                                    XMLWriter xw,
                                    XMLFilterWriterConfig config)
            throws ClassCastException, IllegalArgumentException, IOException
        {
            xatw.writeAnnotationType((AnnotationType) obj, xw);
        }
    }
}
