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

package org.biojava.bio.gui.sequence;import java.awt.geom.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

import org.biojava.utils.*;
import org.biojava.bio.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.gui.*;

import java.util.List;

public class BasicFeatureRenderer
extends AbstractChangeable
implements FeatureRenderer {
  public static final ChangeType FILL = new ChangeType(
    "The fill paint has changed",
    "org.biojava.bio.gui.sequence.BasicFeatureRenderer",
    "FILL"
  );
  
  public static final ChangeType OUTLINE = new ChangeType(
    "The outline paint has changed",
    "org.biojava.bio.gui.sequence.BasicFeatureRenderer",
    "OUTLINE"
  );
  
  public static final ChangeType SIZE = new ChangeType(
    "The size of the arrow has changed",
    "org.biojava.bio.gui.sequence.BasicFeatureRenderer",
    "SIZE"
  );
  
  public static final ChangeType SCOOP = new ChangeType(
    "The scoop of the arror has changed",
    "org.biojava.bio.gui.sequence.BasicFeatureRenderer",
    "SCOOP"
  );

  private Paint fill;
  private Paint outline;
  private double arrowSize = 15.0;
  private double arrowScoop = 4.0;

  public BasicFeatureRenderer() {
    fill = Color.red;
    outline = Color.black;
  }

  public void setFill(Paint p)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SequenceRenderContext.REPAINT);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.REPAINT,
          null, null, new ChangeEvent(
            this, FILL, p, fill
          )
        );
        cs.firePreChangeEvent(ce);
        fill = p;
        cs.firePostChangeEvent(ce);
      }
    } else {
      fill = p;
    }
  }
  
  public Paint getFill() {
    return fill;
  }
  
  public void setOutline(Paint p)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SequenceRenderContext.REPAINT);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.REPAINT,
          null, null, new ChangeEvent(
            this, OUTLINE, p, outline
          )
        );
        cs.firePreChangeEvent(ce);
        outline = p;
        cs.firePostChangeEvent(ce);
      }
    } else {
      outline = p;
    }
  }
  
  public Paint getOutline() {
    return outline;
  }
  
  public void setArrowSize(double arrowSize)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.LAYOUT,
          null, null, new ChangeEvent(
            this, SIZE, new Double(this.arrowSize), new Double(arrowSize)
          )
        );
        cs.firePreChangeEvent(ce);
        this.arrowSize = arrowSize;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.arrowSize = arrowSize;
    }
  }
  
  public double getArrowSize() {
    return arrowSize;
  }
  
  public void setArrowScoop(double arrowScoop)
  throws ChangeVetoException {
    if(hasListeners()) {
      ChangeSupport cs = getChangeSupport(SequenceRenderContext.LAYOUT);
      synchronized(cs) {
        ChangeEvent ce = new ChangeEvent(
          this, SequenceRenderContext.LAYOUT,
          null, null, new ChangeEvent(
            this, SIZE, new Double(this.arrowSize), new Double(arrowSize)
          )
        );
        cs.firePreChangeEvent(ce);
        this.arrowScoop = arrowScoop;
        cs.firePostChangeEvent(ce);
      }
    } else {
      this.arrowScoop = arrowScoop;
    }
  }
    
  public double getArrowScoop() {
    return arrowScoop;
  }
  
  public void renderFeature(
    Graphics2D g,
    Feature f, 
    SequenceRenderContext src
  ) {
    Shape s = null;
    Location loc = f.getLocation();
    float min = (float) src.sequenceToGraphics(loc.getMin());
    float max = (float) src.sequenceToGraphics(loc.getMax());
    float depth = (float) (arrowSize + 2.0 * arrowScoop);
    if( (max - min) >= arrowSize) {
      if (f instanceof StrandedFeature) {
        StrandedFeature.Strand strand = ((StrandedFeature) f).getStrand();
        if(src.getDirection() == src.HORIZONTAL) {
          float minY = 0.0f;
          float maxY = depth;
          float minYS = minY + (float) arrowScoop;
          float maxYS = maxY - (float) arrowScoop;
          float midY = (minY + maxY) * 0.5f;
          float minX = min;
          float maxX = max;
          if(strand == StrandedFeature.POSITIVE) {
            float midX = maxX - (float) arrowSize;
            GeneralPath path = new GeneralPath();
            path.moveTo(minX, minYS);
            path.lineTo(midX, minYS);
            path.lineTo(midX, minY);
            path.lineTo(maxX, midY);
            path.lineTo(midX, maxY);
            path.lineTo(midX, maxYS);
            path.lineTo(minX, maxYS);
            path.closePath();
            s = path;
          } else if(strand == StrandedFeature.NEGATIVE) {
            float midX = minX + (float) arrowSize;
            GeneralPath path = new GeneralPath();
            path.moveTo(maxX, minYS);
            path.lineTo(midX, minYS);
            path.lineTo(midX, minY);
            path.lineTo(minX, midY);
            path.lineTo(midX, maxY);
            path.lineTo(midX, maxYS);
            path.lineTo(maxX, maxYS);
            path.closePath();
            s = path;
          }
        } else { // vertical
          float minX = 0.0f;
          float maxX = depth;
          float minXS = minX + (float) arrowScoop;
          float maxXS = maxX - (float) arrowScoop;
          float midX = (minX + maxX) * 0.5f;
          float minY = min;
          float maxY = max;
          if(strand == StrandedFeature.POSITIVE) {
            float midY = maxY - (float) arrowSize;
            GeneralPath path = new GeneralPath();
            path.moveTo(minXS, minY);
            path.lineTo(minXS, midY);
            path.lineTo(minX, midY);
            path.lineTo(midX, maxY);
            path.lineTo(maxX, midY);
            path.lineTo(maxXS, midY);
            path.lineTo(maxXS, minY);
            path.closePath();
            s = path;
          } else if(strand == StrandedFeature.NEGATIVE) {
            float midY = minY + (float) arrowSize;
            GeneralPath path = new GeneralPath();
            path.moveTo(minXS, maxY);
            path.lineTo(minXS, midY);
            path.lineTo(minX, midY);
            path.lineTo(midX, minY);
            path.lineTo(maxX, midY);
            path.lineTo(maxXS, midY);
            path.lineTo(maxXS, maxY);
            path.closePath();
            s = path;
          }
        }
      }
    }
    if(s == null) {
      if(src.getDirection() == src.HORIZONTAL) {
        s = new Rectangle2D.Double(min, 0, max-min, 2.0*arrowScoop + arrowSize);
      } else {
        s = new Rectangle2D.Double(0, min, 2.0*arrowScoop + arrowSize, max-min);
      }
    }
    if(fill != null) {
      g.setPaint(fill);
      g.fill(s);
    }
    if ( (outline != null) && ( (max - min) > 4.0) ) {
      g.setPaint(outline);
      g.draw(s);
    }
  }
  
  public double getDepth(SequenceRenderContext src, RangeLocation pos) {
    return arrowSize + 2.0 * arrowScoop + 1.0;
  }
  
  public FeatureHolder processMouseEvent(
    FeatureHolder hits,
    SequenceRenderContext src,
    MouseEvent me
  ) {
    return hits;
  }
}
