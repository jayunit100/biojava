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

package org.biojava.bio.seq;

import java.util.Iterator;

import org.biojava.bio.Annotation;
import org.biojava.bio.BioError;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.impl.FeatureImpl;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.SimpleGappedSymbolList;
import org.biojava.utils.ChangeVetoException;

/**
 * Simple implementation of GappedSequence.
 *
 * @author Thomas Down
 * @author Matthew Pocock
 * @since 1.3
 */

public class SimpleGappedSequence
extends SimpleGappedSymbolList
implements GappedSequence {
  private Sequence sequence;
  
  private MergeFeatureHolder features;
  private SimpleFeatureHolder localFeatures;
  private FeatureHolder projectedFeatures;
  
  private boolean createOnUnderlying;
  
  public SimpleGappedSequence(Sequence seq) {
    super(seq);
    this.sequence = seq;
    createOnUnderlying = false;
  }
  
  public boolean getCreateOnUnderlyingSequence() {
    return createOnUnderlying;
  }
  
  public void setCreateOnUnderlyingSequence(boolean underlying) {
    this.createOnUnderlying = underlying;
  }
  
  public Annotation getAnnotation() {
    return sequence.getAnnotation();
  }
  
  public String getName() {
    return sequence.getName();
  }
  
  public String getURN() {
    return sequence.getURN();
  }
  
  private FeatureHolder getFeatures() {
    if (features == null) {
      features = makeFeatures();
    }
    return features;
  }
  
  private MergeFeatureHolder makeFeatures() {
    projectedFeatures = new ProjectedFeatureHolder(sequence, this, 0, false) {
      public FeatureHolder getParent(Feature f) {
        return SimpleGappedSequence.this;
      }
      
      public Sequence getSequence(Feature f) {
        return SimpleGappedSequence.this;
      }
      
      public Location getLocation(Feature f) {
        return locationToGapped(f.getLocation());
      }
      
      public StrandedFeature.Strand getStrand(StrandedFeature f) {
        return f.getStrand();
      }
      
      public Annotation getAnnotation(Feature f) {
        return f.getAnnotation();
      }
      
      public FeatureHolder projectChildFeatures(Feature f, FeatureHolder parent) {
        return FeatureHolder.EMPTY_FEATURE_HOLDER;
      }
      
      public Feature createFeature(Feature.Template templ)
      throws ChangeVetoException, BioException
      {
        templ.location = gappedToLocation(templ.location);
        return projectFeature(getWrapped().createFeature(templ));
      }
      
      public void removeFeature(Feature f, Feature f2)
      throws ChangeVetoException
      {
        throw new ChangeVetoException("NO");
      }
      
      public FeatureFilter getSchema(Feature f) {
        return f.getSchema();
      }
    };
    
    localFeatures = new SimpleFeatureHolder();
    
    features = new MergeFeatureHolder();
    
    try {
      features.addFeatureHolder(projectedFeatures);
      features.addFeatureHolder(localFeatures);
    } catch (ChangeVetoException cve) {
      throw new BioError(cve, "Assertion Failure: Should be able to do this");
    }
    
    return features;
  }
  
  public Iterator features() {
    return getFeatures().features();
  }
  
  public FeatureHolder filter(FeatureFilter ff) {
    return getFeatures().filter(ff);
  }
  
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
    return getFeatures().filter(ff, recurse);
  }
  
  public int countFeatures() {
    return getFeatures().countFeatures();
  }
  
  public boolean containsFeature(Feature f) {
    return getFeatures().containsFeature(f);
  }
  
  public FeatureFilter getSchema() {
    return getFeatures().getSchema();
  }
  
  public void removeFeature(Feature f)
  throws ChangeVetoException
  {
    getFeatures().removeFeature(f);
  }
  
  public Feature createFeature(Feature.Template templ)
  throws ChangeVetoException, BioException
  {
    getFeatures();
    if(createOnUnderlying) {
      return projectedFeatures.createFeature(templ);
    } else {
      Feature f = FeatureImpl.DEFAULT.realizeFeature(this, this, templ);
      localFeatures.addFeature(f);
      return f;
    }
  }
}
