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

import java.util.*;

import org.biojava.utils.ChangeVetoException;
import org.biojava.bio.BioException;
import org.biojava.bio.symbol.Location;
import org.biojava.bio.symbol.LocationTools;

/**
 * A set of FeatureFilter algebraic operations.
 *
 * @since 1.2
 * @author Matthew Pocock
 * @author Thomas Down
 */

public class FilterUtils {
    /**
     * Determines if the set of features matched by sub can be <code>proven</code> to be a
     * proper subset of the features matched by sup.
     * <p>
     * If the filter sub matches only features that are matched by sup, then it is
     * a proper subset. It is still a proper subset if it does not match every
     * feature in sup, as long as no feature matches sub that is rejected by sup.
     * </p>
     *
     * @param sub the subset filter
     * @param sup the superset filter
     * @return <code>true</code> if <code>sub</code> is a proper subset of <code>sup</code>
     */

    public static boolean areProperSubset(FeatureFilter sub, FeatureFilter sup) {
      // Preconditions
      
      if (sub == null) {
        throw new NullPointerException("Null FeatureFilter: sub");
      }
      if (sup == null) {
        throw new NullPointerException("Null FeatureFilter: sup");
      }
      
      // Body
      
      if(sub.equals(sup)) {
        return true;
      }
      
      if (sup instanceof FeatureFilter.AcceptAllFilter) {
        return true;
      } else if (sub instanceof FeatureFilter.AcceptNoneFilter) {
        return true;
      } else if (sup instanceof FeatureFilter.And) {
        FeatureFilter.And and_sup = (FeatureFilter.And) sup;
        return areProperSubset(sub, and_sup.getChild1()) && areProperSubset(sub, and_sup.getChild2());
      } else if (sub instanceof FeatureFilter.And) {
        FeatureFilter.And and_sub = (FeatureFilter.And) sub;
        return areProperSubset(and_sub.getChild1(), sup) || areProperSubset(and_sub.getChild2(), sup);
      } else if (sub instanceof FeatureFilter.Or) {
        FeatureFilter.Or or_sub = (FeatureFilter.Or) sub;
        return areProperSubset(or_sub.getChild1(), sup) && areProperSubset(or_sub.getChild2(), sup);
      } else if (sup instanceof FeatureFilter.Or) {
        FeatureFilter.Or or_sup = (FeatureFilter.Or) sup;
        return areProperSubset(sub, or_sup.getChild1()) || areProperSubset(sub, or_sup.getChild2());
      } else if (sup instanceof FeatureFilter.Not) {
        FeatureFilter not_sup = ((FeatureFilter.Not) sup).getChild();
        return areDisjoint(sub, not_sup);
      } else if (sub instanceof FeatureFilter.Not) {
        // How do we prove this one?
      } else if (sub instanceof OptimizableFilter) {
        return ((OptimizableFilter) sub).isProperSubset(sup);
      }
      
      return false;
    }
  
    /**
     * Determines is two queries can be proven to be disjoint.
     * <p>
     * They are disjoint if there is no element that is matched by both filters
     * - that is, they have an empty intersection.  Order of arguments to this
     * method is not significant.
     * </p>
     *
     * @param a   the first FeatureFilter
     * @param b   the second FeatureFilter
     * @return <code>true</code> if they are proved to be disjoint, <code>false</code> otherwise
     */

    public static boolean areDisjoint(FeatureFilter a, FeatureFilter b) {
      // Preconditions
      
      if (a == null) {
        throw new NullPointerException("Null FeatureFilter: a");
      }
      if (b == null) {
        throw new NullPointerException("Null FeatureFilter: b");
      }
      
      // Body
      
      if(a.equals(b)) {
        return false;
      }
      
      if (a instanceof FeatureFilter.AcceptAllFilter) {
        return areProperSubset(b, FeatureFilter.none);
      } else if(b instanceof FeatureFilter.AcceptAllFilter) {
        return areProperSubset(a, FeatureFilter.none);
      } else if (a instanceof FeatureFilter.AcceptNoneFilter || b instanceof FeatureFilter.AcceptNoneFilter) {
        return true;
      } if (a instanceof FeatureFilter.And) {
        FeatureFilter.And and_a = (FeatureFilter.And) a;
        return areDisjoint(and_a.getChild1(), b) || areDisjoint(and_a.getChild2(), b);
      } else if (b instanceof FeatureFilter.And) {
        FeatureFilter.And and_b = (FeatureFilter.And) b;
        return areDisjoint(a, and_b.getChild1()) || areDisjoint(a, and_b.getChild2());
      } else if (a instanceof FeatureFilter.Or) {
        FeatureFilter.Or or_a = (FeatureFilter.Or) a;
        return areDisjoint(or_a.getChild1(), b) && areDisjoint(or_a.getChild2(), b);
      } else if (b instanceof FeatureFilter.Or) {
        FeatureFilter.Or or_b = (FeatureFilter.Or) b;
        return areDisjoint(a, or_b.getChild1()) && areDisjoint(a, or_b.getChild2());
      } else if (a instanceof FeatureFilter.Not) {
        FeatureFilter not_a = ((FeatureFilter.Not) a).getChild();
        return areProperSubset(b, not_a);
      } else if (b instanceof FeatureFilter.Not) {
        FeatureFilter not_b = ((FeatureFilter.Not) b).getChild();
        return areProperSubset(a, not_b);
      } else if (a instanceof OptimizableFilter) {
        return ((OptimizableFilter) a).isDisjoint(b);
      } else if (b instanceof OptimizableFilter) {
        return ((OptimizableFilter) b).isDisjoint(a);
      }
      
      // *SIGH* we don't have a proof here...
      
      return false;
    }
    
    /**
     * Try to determine the minimal location which all features matching a given
     * filter must overlap.
     *
     * @param ff A feature filter
     * @return the minimal location which any features matching <code>ff</code>
     *          must overlap, or <code>null</code> if no proof is possible
     *          (normally indicates that the filter has nothing to do with
     *          location).
     * @since 1.2
     */

    public static Location extractOverlappingLocation(FeatureFilter ff) {
	if (ff instanceof FeatureFilter.OverlapsLocation) {
	    return ((FeatureFilter.OverlapsLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.ContainedByLocation) {
	    return ((FeatureFilter.ContainedByLocation) ff).getLocation();
	} else if (ff instanceof FeatureFilter.And) {
	    FeatureFilter.And ffa = (FeatureFilter.And) ff;
	    Location l1 = extractOverlappingLocation(ffa.getChild1());
	    Location l2 = extractOverlappingLocation(ffa.getChild2());

	    if (l1 != null) {
		if (l2 != null) {
		    return l1.intersection(l2);
		} else {
		    return l1;
		}
	    } else {
		if (l2 != null) {
		    return l2;
		} else {
		    return null;
		}
	    }
	} else if (ff instanceof FeatureFilter.Or) {
	    FeatureFilter.Or ffo = (FeatureFilter.Or) ff;
	    Location l1 = extractOverlappingLocation(ffo.getChild1());
	    Location l2 = extractOverlappingLocation(ffo.getChild2());
	    
	    if (l1 != null && l2 != null) {
		return LocationTools.union(l1, l2);
	    }
	}

	return null;
    }
}
