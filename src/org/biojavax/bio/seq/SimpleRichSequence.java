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

package org.biojavax.bio.seq;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.FilterUtils;
import org.biojava.bio.seq.SimpleFeatureHolder;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.Edit;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.SimpleSymbolList;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.utils.ChangeEvent;
import org.biojava.utils.ChangeSupport;
import org.biojava.utils.ChangeVetoException;
import org.biojavax.Namespace;
import org.biojavax.bio.BioEntry;
import org.biojavax.bio.SimpleBioEntry;


/**
 * A simple implementation of RichSequence.
 * @author Richard Holland
 */
public class SimpleRichSequence extends SimpleBioEntry implements RichSequence {
    
    private SymbolList symList;
    private Set features = new HashSet();
    private double symListVersion = 0.0;
    
    
    /** 
     * Creates a new instance of SimpleRichSequence.
     * @param ns the namespace for this sequence.
     * @param name the name of the sequence.
     * @param accession the accession of the sequence.
     * @param version the version of the sequence.
     * @param symList the symbols for the sequence.
     * @param seqversion the version of the symbols for the sequence.
     */
    public SimpleRichSequence(Namespace ns, String name, String accession, int version, SymbolList symList, double seqversion) {
        super(ns,name,accession,version);
        this.symList = symList;
        this.symListVersion = seqversion;
    }
    
    // Hibernate requirement - not for public use.
    protected SimpleRichSequence() {}
    
    /**
     * {@inheritDoc}
     */
    public double getSeqVersion() { return this.symListVersion; }
    
    /**
     * {@inheritDoc}
     */
    public void setSeqVersion(double seqVersion) throws ChangeVetoException {        
        if(!this.hasListeners(RichSequence.SEQVERSION)) {            
            this.symListVersion = seqVersion;            
        } else {            
            ChangeEvent ce = new ChangeEvent(                    
                    this,                    
                    BioEntry.SEQVERSION,
                                        Double.valueOf(seqVersion),
                                        Double.valueOf(this.symListVersion)
                                        );
                        ChangeSupport cs = this.getChangeSupport(RichSequence.SEQVERSION);
                        synchronized(cs) {
                             cs.firePreChangeEvent(ce);
                             this.symListVersion = seqVersion;
                              cs.firePostChangeEvent(ce);
                          }
                    }
            }
    
     /**
     * {@inheritDoc}
     */
    public void edit(Edit edit) throws IndexOutOfBoundsException, IllegalAlphabetException, ChangeVetoException {
        this.symList.edit(edit);
    }
    
    /**
     * {@inheritDoc}
     */
    public Symbol symbolAt(int index) throws IndexOutOfBoundsException { return this.symList.symbolAt(index); }
    
    /**
     * {@inheritDoc}
     */
    public List toList() { return this.symList.toList();}
    
    /**
     * {@inheritDoc}
     */
    public String subStr(int start, int end) throws IndexOutOfBoundsException { return this.symList.subStr(start, end); }
    
    /**
     * {@inheritDoc}
     */
    public SymbolList subList(int start, int end) throws IndexOutOfBoundsException {
        return this.symList.subList(start, end);        
    }
    
    /**
     * {@inheritDoc}
     */
    public String seqString() { return this.symList.seqString(); }
       
    /**
     * {@inheritDoc}
     */
    public int length() { return this.symList.length(); }
    
    /**
     * {@inheritDoc}
     */
    public Iterator iterator() { return this.symList.iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public Alphabet getAlphabet() { return this.symList.getAlphabet(); }
    
    // Hibernate requirement - not for public use.
    private String alphaname;
    
    // Hibernate requirement - not for public use.
    private void setAlphabetName(String alphaname) throws IllegalSymbolException, BioException {
        this.alphaname = alphaname;
        this.checkMakeSequence();
    }
    
    // Hibernate requirement - not for public use.
    private String getAlphabetName() { return (this.symList==SymbolList.EMPTY_LIST?null:this.getAlphabetName()); }
    
    // Hibernate requirement - not for public use.
    private String seqstring;
        
    // Hibernate requirement - not for public use.
    private void setStringSequence(String seq) throws IllegalSymbolException, BioException {
        this.seqstring = seq;
        this.checkMakeSequence();
    }
    
    // Hibernate requirement - not for public use.
    private String getStringSequence() { return (this.symList==SymbolList.EMPTY_LIST?null:this.seqString()); }
    
    // Hibernate requirement - not for public use.
    private void checkMakeSequence() throws IllegalSymbolException, BioException {
        if (this.alphaname!=null && this.seqstring!=null) {
            // Make the symbol list and assign it.
            Alphabet a = AlphabetManager.alphabetForName(this.alphaname);
            this.symList = new SimpleSymbolList(a.getTokenization("token"), seqstring);
        }
    }
    
    // Hibernate requirement - not for public use.
    private void setSequenceLength(int length) {
        // ignore - it's calculated anyway.
    }
    
    // Hibernate requirement - not for public use.
    private int getSequenceLength() { return this.length(); }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder filter(FeatureFilter fc, boolean recurse) {
        SimpleFeatureHolder fh = new SimpleFeatureHolder();
        for (Iterator i = this.features.iterator(); i.hasNext(); ) {
            Feature f = (RichFeature)i.next();
            try {
                if (fc.accept(f)) fh.addFeature(f);
            } catch (ChangeVetoException e) {
                throw new RuntimeException("What? You don't like our features??");
            }
        }
        return fh;
    }
    
    /**
     * {@inheritDoc}
     */
    public Feature createFeature(Feature.Template ft) throws BioException, ChangeVetoException {
        Feature f = new SimpleRichFeature(this,ft);
        this.features.add(f);
        return f;
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeFeature(Feature f) throws ChangeVetoException, BioException { this.features.remove((RichFeature)f); }
    
    /**
     * {@inheritDoc}
     */
    public boolean containsFeature(Feature f) { return this.features.contains((RichFeature)f); }
    
    /**
     * {@inheritDoc}
     */
    public FeatureHolder filter(FeatureFilter filter) {
        boolean recurse = !FilterUtils.areProperSubset(filter, FeatureFilter.top_level);
        return this.filter(filter, recurse);
    }
    
    /**
     * {@inheritDoc}
     */
    public Set getFeatureSet() {
        return Collections.unmodifiableSet(this.features);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setFeatureSet(Set features) throws ChangeVetoException {
        this.features.clear();
        for (Iterator i = features.iterator(); i.hasNext(); ) {
            RichFeature f = (RichFeature)i.next();
            f.setParent(this);
            this.features.add(f);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public FeatureFilter getSchema() { return FeatureFilter.top_level;}
    
    /**
     * {@inheritDoc}
     */
    public Iterator features() { return this.getFeatureSet().iterator(); }
    
    /**
     * {@inheritDoc}
     */
    public int countFeatures() { return this.features.size(); }
}