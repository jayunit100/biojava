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

package org.biojava.bio.seq.db.biosql;

import org.biojava.bio.BioError;
import org.biojava.bio.seq.DNATools;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureHolder;
import org.biojava.bio.seq.Sequence;
import org.biojava.bio.seq.StrandedFeature;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.SymbolList;

/**
 * @author Thomas Down
 */
class BioSQLStrandedFeature extends BioSQLFeature implements StrandedFeature {
    private StrandedFeature.Strand strand;
    
    public StrandedFeature.Strand getStrand() {
	return strand;
    }
  
    public SymbolList getSymbols() {
	SymbolList symList = super.getSymbols();
	if(getStrand() == NEGATIVE) {
	    try {
		symList = DNATools.reverseComplement(symList);
	    } catch (IllegalAlphabetException iae) {
		throw new BioError(
				   iae,
				   "Could not retrieve symbols for feature as " +
				   "the alphabet can not be complemented."
				   );
	    }
	}
	return symList;
    }
  
    public Feature.Template makeTemplate() {
	StrandedFeature.Template ft = new StrandedFeature.Template();
	fillTemplate(ft);
	return ft;
    }
    
    protected void fillTemplate(StrandedFeature.Template ft) {
	super.fillTemplate(ft);
	ft.strand = getStrand();
    }
  
    public BioSQLStrandedFeature(Sequence sourceSeq,
				 FeatureHolder parent,
				 StrandedFeature.Template template)
	throws IllegalArgumentException, IllegalAlphabetException 
    {
	super(sourceSeq, parent, template);
	this.strand = template.strand;
    }

    public BioSQLStrandedFeature(Sequence sourceSeq,
				 StrandedFeature.Template template)
	throws IllegalArgumentException, IllegalAlphabetException 
    {
	super(sourceSeq, template);
	this.strand = template.strand;
    }
  
    public String toString() {
	String pm;
	if(getStrand() == POSITIVE) {
	    pm = "+";
	} else {
	    pm = "-";
	}
	return super.toString() + " " + pm;
    }
} 
