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
 * Created on 25.04.2004
 * @author Andreas Prlic
 *
 */
package org.biojava.bio.structure;

import java.util.ArrayList ;

/**
 * Defines the interface for a Chain.
 *
 * @author Andreas Prlic
 * @version %I% %G%
 * @since 1.4
 */
public interface Chain {
	
    /** returns an identical copy of this Chain. 
     * @return  an identical copy of this Chain 
     */
    public Object clone();

    /** add a group to this chain.
     * @param group  a Group object
     */
    public void addGroup(Group group);
	
    /** return the amino acid at position X.
     * @param position  an int
     * @return a Group object
     */
    public Group getGroup (int position);
	
    /** return an ArrayList of all groups of a special type (e.g. amino,
     * hetatm, nucleotide).
     * @param type  a String
     * @return an ArrayList object
     */
    public ArrayList getGroups (String type);

    /** return all groups of this chain.
     * @return an ArrayList of all Group objects of this chain

     */
    public ArrayList getGroups ();

    /** get total length of chain, including HETATMs..
     * @return an int representing the length of the whole chain including HETATMs
     */
    public int getLength();
    
    /** returns the length of the AminoAcids in chain, without HETATMs.
     * note: not all amino acids need to have 3D coords, in fact in could be that none
     * has!
     * so length always corresponds to Sequence ( = uniprot,swissprot) length.
     * @return an int representing the length of the AminoAcids in chain, without HETATMs.
     */

    public int getLengthAminos();

    /** get and set the name of this chain (Chain id in PDB file ).
     * @param name  a String specifying the name value
     * @see #getName
     * 
     */
    public void setName(String name);	

    /** get and set the name of this chain (Chain id in PDB file ).
     * @return a String representing the name value
     * @see #setName
     */

    public String getName();
	
    /** string representation.  */
    public String toString();
	
    /** return the amino acid sequqence of this chain
     * ( all aminos even if they do not have 3D data ...).
     * @return the sequence as a string
 
     */
    public String getSequence() ;

    /** set the Swissprot id of this chains .
     * @param sp_id  a String specifying the swissprot id value
     * @see #getSwissprotId
     */
    public void setSwissprotId(String sp_id);

    /** get the Swissprot id of this chains .
     * @return a String representing the swissprot id value
     * @see #setSwissprotId
     */
    public String getSwissprotId() ;

    
}
