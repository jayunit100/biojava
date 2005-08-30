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
package org.biojavax.bio.seq.io;

import org.biojava.bio.seq.io.SequenceBuilderFactory;
import org.biojava.bio.symbol.PackedSymbolListFactory;

/**
 * Simple factory for constructing new RichSequenceBuilder objects.
 * @author Richard Holland
 */
public interface RichSequenceBuilderFactory extends SequenceBuilderFactory {
    
    /**
     * Accessor for the default factory. This implementation will not 
     * do any compression of a sequence regardless of size.
     */
    public final static RichSeqIOListener FACTORY = new SimpleRichSequenceBuilder();
    
    /**
     * Accessor for a factory that produces builders that compress the
     * SymbolList of a Sequence
     */
    public final static RichSeqIOListener PACKED = new SimpleRichSequenceBuilder(new PackedSymbolListFactory());
}