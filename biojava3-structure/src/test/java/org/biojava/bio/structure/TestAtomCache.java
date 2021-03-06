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
 * Created on Mar 1, 2010
 * Author: Andreas Prlic 
 *
 */

package org.biojava.bio.structure;

import java.io.File;
import java.util.ArrayList;
import org.biojava.bio.structure.align.util.AtomCache;
import junit.framework.TestCase;

public class TestAtomCache extends TestCase
{
	public static final String lineSplit = System.getProperty("file.separator");

	public void setUp() {
		// Delete files which were cached in previous tests
		String cacheDir = TmpAtomCache.tmpDir;
		String[] uncacheIDs = new String[] {
				"1cmw", "1hhb","4hhb"
		};
		
		ArrayList<String> extensions    = new ArrayList<String>();

		extensions.add(".ent");
		extensions.add(".pdb");
		extensions.add(".ent.gz");
		extensions.add(".pdb.gz");
		extensions.add(".ent.Z");
		extensions.add(".pdb.Z");

		
		for(String pdbId : uncacheIDs) {
			String middle = pdbId.substring(1,3).toLowerCase();
			
			String fpath = cacheDir+lineSplit + middle + lineSplit + pdbId;
			String ppath = cacheDir +lineSplit +  middle + lineSplit + "pdb"+pdbId;
			
			String[] paths = new String[]{fpath,ppath};

			for ( int p=0;p<paths.length;p++ ){
				String testpath = paths[p];
				//System.out.println(testpath);
				for (int i=0 ; i<extensions.size();i++){
					String ex = (String)extensions.get(i) ;
					//System.out.println("PDBFileReader testing: "+testpath+ex);
					File f = new File(testpath+ex) ;

					if ( f.exists()) {
						System.out.println("Deleting "+testpath+ex);
						assertTrue("Error deleting "+testpath+ex+" during setup.",f.delete());
					}
				}
			}

		}
	}

	public void testAtomCacheNameParsing(){

		String name1= "4hhb";

		String name2 = "4hhb.C";
		String chainId2 = "C";

		String name3 = "4hhb:1";
		String chainId3 = "B";

		String name4 = "4hhb:A:10-20,B:10-20,C:10-20";
		String name5 = "4hhb:(A:10-20,A:30-40)";


		AtomCache cache = TmpAtomCache.cache;

		try {
			Structure s = cache.getStructure(name1);
			assertNotNull(s);
			assertTrue(s.getChains().size() == 4);
			s = cache.getStructure(name2);

			assertTrue(s.getChains().size() == 1);
			Chain c = s.getChainByPDB(chainId2);
			assertEquals(c.getChainID(),chainId2);

			s = cache.getStructure(name3);
			assertNotNull(s);
			assertTrue(s.getChains().size() == 1);

			c = s.getChainByPDB(chainId3);
			assertEquals(c.getChainID(),chainId3);

			s = cache.getStructure(name4);
			assertNotNull(s);

			assertEquals(s.getChains().size(), 3);

			c = s.getChainByPDB("B");
			assertEquals(c.getAtomLength(),11);

			s =cache.getStructure(name5);
			assertNotNull(s);

			assertEquals(s.getChains().size(),1 );
			c = s.getChainByPDB("A");
			assertEquals(c.getAtomLength(),22);


		} catch (Exception e){
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
	
	public void testFetchCurrent() {
		AtomCache cache = TmpAtomCache.cache;
		
		cache.setAutoFetch(true);
		cache.setFetchCurrent(true);
		cache.setFetchFileEvenIfObsolete(false);
		
		Structure s;
		try {
			// OBSOLETE PDB; should throw an exception
			s = cache.getStructure("1CMW");
			fail("1CMW has no current structure. Should have thrown an error");
		} catch(Exception e) {
			//expected
			System.err.println("Please ignore previous exceptions. They are expected.");
		}
		
		try {
			s = cache.getStructure("1HHB");
			assertEquals("Failed to get the current ID for 1HHB.","4HHB",s.getPDBCode());
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testFetchObsolete() {
		AtomCache cache = TmpAtomCache.cache;
		
		cache.setAutoFetch(true);
		cache.setFetchCurrent(false);
		cache.setFetchFileEvenIfObsolete(true);
		
		Structure s;
		try {
			// OBSOLETE PDB; should throw an exception
			s = cache.getStructure("1CMW");
			assertEquals("Failed to get OBSOLETE file 1CMW.","1CMW", s.getPDBCode());

			s = cache.getStructure("1HHB");
			assertEquals("Failed to get OBSOLETE file 1HHB.","1HHB",s.getPDBCode());
			System.err.println("Please ignore the previous four errors. They are expected for this ancient PDB.");
		} catch(Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
