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

package org.biojava.bio.seq.impl;

import java.util.*;
import java.io.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.db.*;

/**
 * A no-frills implementation of a remote feature.
 *
 * @author Matthew Pocock
 * @since 1.2
 */
public class SimpleRemoteFeature
extends SimpleFeature
implements RemoteFeature, java.io.Serializable {
  private List regions;
  private RemoteFeature.Resolver resolver;
  
  public List getRegions() {
    return Collections.unmodifiableList(regions);
  }
  
  public RemoteFeature.Resolver getResolver() {
    return resolver;
  }
  
  public Feature getRemoteFeature() throws BioException {
    return getResolver().resolve(this);
  }
  
  public Feature.Template makeTemplate() {
    RemoteFeature.Template rt = new RemoteFeature.Template();
    fillTemplate(rt);
    return rt;
  }
  
  protected void fillTemplate(RemoteFeature.Template rt) {
    super.fillTemplate(rt);
    rt.resolver = getResolver();
    rt.regions = new ArrayList(getRegions());
  }
  
  public SimpleRemoteFeature(
    Sequence sourceSeq,
    FeatureHolder parent,
    RemoteFeature.Template template
  ) {
    super(sourceSeq, parent, template);
    this.regions = new ArrayList(template.regions);
    this.resolver = template.resolver;
  }
  
  public static class DBResolver implements RemoteFeature.Resolver {
    private SequenceDB seqDB;
    
    public SequenceDB getSeqDB() {
      return seqDB;
    }
    
    public DBResolver(SequenceDB seqDB) {
      this.seqDB = seqDB;
    }
    
    public Feature resolve(RemoteFeature rFeat) throws BioException {
      FeatureFilter remoteFilter
        = new FeatureFilter.ByClass(RemoteFeature.class);
      
      Set seqs = new HashSet();
      LinkedList ids = new LinkedList();
      Set feats = new HashSet();
      
      Sequence parent = rFeat.getSequence();
      ids.add(parent);
      
      while(!ids.isEmpty()) {
        Sequence seq = (Sequence) ids.removeFirst();
        seqs.add(seq);
        
        FeatureHolder remotes = seq.filter(remoteFilter, false);
        for(Iterator fi = remotes.features(); fi.hasNext(); ) {
          RemoteFeature rf = (RemoteFeature) fi.next();
          feats.add(rf);
          for(Iterator ri = rf.getRegions().iterator(); ri.hasNext(); ) {
            RemoteFeature.Region r = (RemoteFeature.Region) ri.next();
            if(r.isRemote()) {
              // potentialy expensive - should we cache IDs? What about the ID
              // of this sequence?
              Sequence rseq = getSeqDB().getSequence(r.getSeqID());
              if(!ids.contains(rseq) && !seqs.contains(rseq)) {
                ids.addLast(rseq);
              }
            }
          }
        }
      }
      
      StringBuffer nameBuff = new StringBuffer();
      {
        Iterator si = seqs.iterator();
        Sequence nextSeq = (Sequence) si.next(); // don't need to check hasNext
        nameBuff.append(nextSeq.getName());
        while(si.hasNext()) {
          nextSeq = (Sequence) si.next();
          nameBuff.append("-");
          nameBuff.append(nextSeq.getName());
        }
      }
      Sequence assembly = new SimpleAssembly(nameBuff.toString(), "");
      
      
      
      return null;
    }
  }
}
