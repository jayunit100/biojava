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

package org.biojava.bio.seq.projection;

import java.util.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;

/**
 * Internal class used by ProjectionEngine to wrap Feature objects.
 *
 * @author Thomas Down
 * @since 1.1
 */

public class ProjectedFeature
  implements
    Feature,
    Projection
{ 
  private final Feature feature;
  private final ProjectionContext context;
  
  public ProjectedFeature(
    Feature f,
    ProjectionContext ctx
  ) 
  {
    this.feature = f;
    this.context = ctx;
  }
  
  public FeatureFilter getSchema() {
      return context.getSchema(feature);
  }
  
  public Feature getViewedFeature() {
    return feature;
  }
  
  public ProjectionContext getProjectionContext() {
    return context;
  }
  
  public Feature.Template makeTemplate() {
    Feature.Template ft = getViewedFeature().makeTemplate();
    ft.location = getLocation();
    ft.annotation = getAnnotation();
    return ft;
  }
  
  public Location getLocation() {
      return context.getLocation(feature);
  }
  
  public void setLocation(Location loc)
  throws ChangeVetoException {
    throw new ChangeVetoException(new ChangeEvent(
      this, LOCATION, getLocation(), loc
    ));
    
    // fixme: loc should get reverse-projected through the context
  }
  
  public FeatureHolder getParent() {
    return context.getParent(feature);
  }
  
  public Sequence getSequence() {
    return context.getSequence(feature);
  }
  
  public String getType() {
    return feature.getType();
  }
  
  public void setType(String type)
    throws ChangeVetoException 
  {
    feature.setType(type);
  }
  
  public String getSource() {
    return feature.getSource();
  }
  
  public void setSource(String source)
    throws ChangeVetoException 
  {
    feature.setSource(source);
  }
  
  public Annotation getAnnotation() {
    return context.getAnnotation(feature);
  }
  
  
  public SymbolList getSymbols() {
    Location loc = getLocation();
    Sequence seq = context.getSequence(this);
    if (loc.isContiguous())
      return seq.subList(loc.getMin(),loc.getMax());
      
      List res = new ArrayList();
      for (Iterator i = loc.blockIterator(); i.hasNext(); ) {
        Location l = (Location) i.next();
        res.add(seq.subList(l.getMin(),l.getMax()));
      }
      
      try {
        return new SimpleSymbolList(seq.getAlphabet(), res);
      } catch (IllegalSymbolException ex) {
        throw new BioError(ex);
      }
  }
  
  public int countFeatures() {
    return feature.countFeatures();
  }
  
  public boolean containsFeature(Feature f) {
    if(countFeatures() > 0) {
      return getProjectedFeatures().containsFeature(f);
    } else {
      return false;
    }
  }
  
  protected FeatureHolder getProjectedFeatures() {
      return context.projectChildFeatures(feature, this);
  }
  
  public Iterator features() {
      return getProjectedFeatures().features();
  }
  
  public FeatureHolder filter(FeatureFilter ff) {
      FeatureFilter membershipFilter = new FeatureFilter.And(new FeatureFilter.Not(FeatureFilter.top_level),
                                                             new FeatureFilter.ContainedByLocation(getLocation()));
      if (FilterUtils.areDisjoint(ff, membershipFilter)) { 
          return FeatureHolder.EMPTY_FEATURE_HOLDER;
      }
    
      return getProjectedFeatures().filter(ff);
  }
  
  public FeatureHolder filter(FeatureFilter ff, boolean recurse) {
      FeatureFilter membershipFilter = new FeatureFilter.ContainedByLocation(getLocation());
      if (FilterUtils.areDisjoint(ff, membershipFilter)) { 
          return FeatureHolder.EMPTY_FEATURE_HOLDER;
      }
    
      return getProjectedFeatures().filter(ff, recurse);
  }
  
  public Feature createFeature(Feature.Template temp)
    throws ChangeVetoException, BioException
  {
    return context.createFeature(feature, temp);
  }
  
  public void removeFeature(Feature f) 
    throws ChangeVetoException
  {
    context.removeFeature(feature, f);
  }
  
  public int hashCode() {
    return makeTemplate().hashCode();
  }
  
  public boolean equals(Object o) {
    if (o instanceof Feature) {
      Feature fo = (Feature) o;
      if (fo.getSequence().equals(getSequence())) {
        return makeTemplate().equals(fo.makeTemplate());
      }
    }
    return false;
  }
  
  public void addChangeListener(ChangeListener cl) {
      addChangeListener(cl, ChangeType.UNKNOWN);
  }
  
  public void removeChangeListener(ChangeListener cl) {
      removeChangeListener(cl, ChangeType.UNKNOWN);
  }
  
  public void addChangeListener(ChangeListener cl, ChangeType ct) {
      context.addChangeListener(feature, cl, ChangeType.UNKNOWN);
  }
  
  public void removeChangeListener(ChangeListener cl, ChangeType ct) {
      context.removeChangeListener(feature, cl, ChangeType.UNKNOWN);
  }
  
  public boolean isUnchanging(ChangeType ct) {
      return feature.isUnchanging(ct);
  }
}
