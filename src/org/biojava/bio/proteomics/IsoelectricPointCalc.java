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

package org.biojava.bio.proteomics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.ProteinTools;
import org.biojava.bio.symbol.IllegalAlphabetException;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.symbol.SymbolList;
import org.biojava.bio.symbol.SymbolPropertyTable;
import org.biojava.utils.math.BinarySearch;
import org.biojava.utils.math.ComputeObject;

/**
 * class that computes isoelectric point for proteins
 *
 * @author David Huen
 * @since 1.22
 */
public class IsoelectricPointCalc
{
    public static double PK_NH2 = 8.56;
    public static double PK_COOH = 3.56;

    private static Map pKCache = new HashMap();
    private static ComputeObject computeObj = null;

    public IsoelectricPointCalc()
    {
        // recover pK table and cache only relevant residues
        SymbolPropertyTable pKTable = ProteinTools.getSymbolPropertyTable(SymbolPropertyTable.PK);

        Iterator aaSyms = ProteinTools.getAlphabet().iterator();

        try {
            // iterate thru' all AA symbols and cache the non-zero pKs
            while (aaSyms.hasNext()) {
                Symbol sym = (Symbol) aaSyms.next();

                // only cache symbols that have a non-zero pK
                try {
                double pK = pKTable.getDoubleValue(sym);
                    if (Math.abs(pK) > 0.01) {
                        pKCache.put(sym, new Double(pK));
                    }
                }
                catch (NullPointerException npe) {
                    // SimpleSymbolPropertyTable throws this if there is no value for the symbol
                    // just ignore.
                }        
            }
        }
        catch (IllegalSymbolException ise) {
            // shouldn't happen!
                ise.printStackTrace();
        }
    }

    public class ChargeCalculator
        implements ComputeObject
    {
        Map counts = null;
        boolean hasFreeNTerm = true;
        boolean hasFreeCTerm = true;

        private ChargeCalculator(SymbolList peptide, boolean hasFreeNTerm, boolean hasFreeCTerm)
        {
            counts = residueCount(peptide);
            this.hasFreeNTerm = hasFreeNTerm;
            this.hasFreeCTerm = hasFreeCTerm;
        }

        private ChargeCalculator(SymbolList peptide)
        {
            counts = residueCount(peptide);
        }

        /**
         * counts up number of times a relevant AA appears in protein
         */
        private Map residueCount(SymbolList peptide)
        {
            // iterate thru' peptide collating number of relevant residues
            Iterator residues = peptide.iterator();

            Map symbolCounts = new HashMap();

            while (residues.hasNext()) {
                Symbol sym;
                if (pKCache.containsKey(sym = (Symbol) residues.next())) {

                    // count the residues
                    Integer currCount = (Integer) symbolCounts.get(sym);
                    if (currCount != null) {
                        int currCountAsInt = currCount.intValue();
                        symbolCounts.put(sym, new Integer(++currCountAsInt));
                    }
                    else {
                        symbolCounts.put(sym, new Integer(1));
                    }
                }
            }

            return symbolCounts;
        }

        /**
         * computes charge at given pH
         */
        public double compute(double pH)
        {
            double charge = 0.0;

            // iterate thru' all counts computing the partial contribution to charge
            Iterator aaI = counts.keySet().iterator();

            // I use a convention that positive pK values reflect bases and negative pK values reflect acids.

            while (aaI.hasNext()) {
                // get back the symbol
                Symbol sym = (Symbol) aaI.next();

                // retrieve the pK and count
                Double value = (Double) pKCache.get(sym);

                if (value != null) {
                    double pK = value.doubleValue();
                    double count = ((Integer) counts.get(sym)).intValue();

                    if (pK > 0) {
                        double cr = Math.pow(10.0, pK - pH);
                        charge += count * cr / (cr + 1.0);
                    }
                    else {
                        double cr = Math.pow(10.0, pH + pK);
                        charge -= count * cr / (cr + 1.0);
                    }
                }
            }

            // account for end charges
            if (hasFreeNTerm) {
                double cr = Math.pow(10.0, PK_NH2 - pH);
                charge += cr / (1.0 + cr);
            }

            if (hasFreeCTerm) {
                double cr = Math.pow(10.0, pH - PK_COOH);
                charge -= cr / (1.0 + cr);
            }

            return charge;
        }

    }

    /**
     * Computes isoelectric point of specified peptide.
     *
     * @param peptide peptide of which pI is required.
     * @param hasFreeNTerm has free N-terminal amino group.
     * @param hasFreeCTerm has free C-terminal carboxyl group.
     */
    public double getPI(SymbolList peptide, boolean hasFreeNTerm, boolean hasFreeCTerm)
        throws IllegalAlphabetException, BioException
    {
        // verify that the peptide is really a peptide
        if ( (peptide.getAlphabet() == ProteinTools.getTAlphabet())
            || (peptide.getAlphabet() == ProteinTools.getAlphabet()) ) {

        // create object to handle the peptide
        ComputeObject computeObj = new ChargeCalculator(peptide, hasFreeNTerm, hasFreeCTerm);

        // solve the charge equation
        double pI = BinarySearch.solve(1.0, 13.0, 0.001, computeObj);

        return pI;

        }
        else {
            // not a peptide
            throw new IllegalAlphabetException();
        }
    }
}

