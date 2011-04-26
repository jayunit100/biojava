/**
 * 
 */
package org.biojava.bio.structure;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Methods for getting the status of a PDB file (current, obsolete, etc)
 * and for accessing different versions of the structure.
 * 
 * <p>All methods query the 
 * <a href="http://www.rcsb.org/pdb/rest/idStatus?structureId=1HHB,3HHB,4HHB">
 * PDB website.</a>
 * 
 * <p>PDB supersessions form a directed acyclic graph, where edges point from an
 * obsolete ID to the entry that directly superseded it. For example, here are  
 * edges from one portion of the graph:<br/>
 * 
 * 1CAT -> 3CAT<br/>
 * 3CAT -> 7CAT<br/>
 * 3CAT -> 8CAT<br/>
 * 
 * <p>The methods {@link #getReplaces(String, boolean) getReplaces(pdbId, false)}/ 
 * {@link #getReplacement(String, boolean, boolean) getReplacement(pdbId, false, true)}
 * just get the incoming/outgoing edges for a single node. The recursive versions 
 * ({@link #getReplaces(String, boolean) getReplaces(pdbId, true)}, 
 * {@link #getReplacement(String, boolean, boolean) getReplacement(pdbId, true, false)})
 * will do a depth-first search up/down the tree and return a list of all nodes ]
 * reached.
 * 
 * <p>Finally, the getCurrent() method returns a single PDB ID from among the 
 * results of 
 * {@link #getReplacement(String, boolean) getReplacement(pdbId, true)}. 
 * To be consistent with the old REST ordering, this is the PDB ID that occurs 
 * last alphabetically.
 * 
 * <p><b>TODO</b> Keep a small cache of queries around, to reduce server load
 * 
 * @author Spencer Bliven <sbliven@ucsd.edu>
 *
 */
public class PDBStatus {
	public static final String DEFAULT_PDB_SERVER = "www.rcsb.org";
	public static final String PDB_SERVER_PROPERTY = "PDB.SERVER";
	
	/**
	 * Represents the status of PDB IDs. 'OBSOLETE' and 'CURRENT' are the most
	 * common.
	 * @author Spencer Bliven <sbliven@ucsd.edu>
	 *
	 */
	public enum Status {
		OBSOLETE,
		CURRENT,
		AUTH,
		HOLD,
		HPUB,
		POLC,
		PROC,
		REFI,
		REPL,
		WAIT,
		WDRN,
		UNKNOWN;
		
		
		/**
		 * 
		 * @param statusStrUpper
		 * @return
		 * @throws IllegalArgumentException If the string is not recognized
		 */
		public static Status fromString(String statusStr) {
			Status status;
			String statusStrUpper = statusStr.toUpperCase();
			if(statusStrUpper.equalsIgnoreCase("OBSOLETE"))
				status = Status.OBSOLETE;
			else if(statusStrUpper.equalsIgnoreCase("CURRENT"))
				status = Status.CURRENT;
			else if(statusStrUpper.equalsIgnoreCase("AUTH"))
				status = Status.AUTH;
			else if(statusStrUpper.equalsIgnoreCase("HOLD"))
				status = Status.HOLD;
			else if(statusStrUpper.equalsIgnoreCase("HPUB"))
				status = Status.HPUB;
			else if(statusStrUpper.equalsIgnoreCase("POLC"))
				status = Status.POLC;
			else if(statusStrUpper.equalsIgnoreCase("PROC"))
				status = Status.PROC;
			else if(statusStrUpper.equalsIgnoreCase("REFI"))
				status = Status.REFI;
			else if(statusStrUpper.equalsIgnoreCase("REPL"))
				status = Status.REPL;
			else if(statusStrUpper.equalsIgnoreCase("WAIT"))
				status = Status.WAIT;
			else if(statusStrUpper.equalsIgnoreCase("WDRN"))
				status = Status.WDRN;
			else if(statusStrUpper.equalsIgnoreCase("UNKNOWN"))
				status = Status.UNKNOWN;
			else {
				throw new IllegalArgumentException("Unable to parse status '"+statusStrUpper+"'.");
			}
			return status;
		}
	}
	
	/**
	 * Get the status of the PDB in question.
	 * 
	 * <p>Possible return values are:

	 * @param pdbId
	 * @return The status, or null if an error occurred.
	 */
	public static Status getStatus(String pdbId) {
		List<Map<String,String>> attrList = getStatusIdRecords(new String[] {pdbId});
		//Expect a single record
		if(attrList == null || attrList.size() != 1) {
			System.err.println("Error getting Status for "+pdbId+" from the PDB website.");
			return null;
		}
		
		Map<String,String> attrs = attrList.get(0);
		
		//Check that the record matches pdbId
		String id = attrs.get("structureId");
		if(id == null || !id.equals(pdbId)) {
			System.err.println("Error: Results returned from the query don't match "+pdbId);
			return null;
		}
		
		//Check that the status is given
		String statusStr = attrs.get("status");
		if(statusStr == null ) {
			System.err.println("Error: No status returned for "+pdbId);
			return null;
		}
		
		Status status = Status.fromString(statusStr);
		if(status == null) {
			System.err.println("Error: Unknown status '"+statusStr+"'");
			return null;
		}
	
		return status;
	}
	
	/**
	 * Gets the current version of a PDB ID. This is equivalent to selecting
	 * the first element from
	 * {@link #getReplacement(String,boolean) getReplacement(oldPdbId,true,false)}
	 * 
	 * @param oldPdbId
	 * @return The 
	 */
	public static String getCurrent(String oldPdbId) {
		List<String> replacements =  getReplacement(oldPdbId,true, false);
		if(replacements.size()>0)
			return replacements.get(0);
		else
			return null;
	}
	
	/**
	 * Gets the PDB which superseded oldPdbId. For CURRENT IDs, this will
	 * be itself. For obsolete IDs, the behavior depends on the recursion 
	 * parameter. If false, only IDs which directly supersede oldPdbId are
	 * returned. If true, the replacements for obsolete records are recursively 
	 * fetched, yielding a list of all current replacements of oldPdbId.
	 * 
	 * 
	 * 
	 * @param oldPdbId A pdb ID
	 * @param recurse Indicates whether the replacements for obsolete records 
	 * 		should be fetched.
	 * @param includeObsolete Indicates whether obsolete records should be
	 * 		included in the results.
	 * @return The PDB which replaced oldPdbId. This may be oldPdbId itself, for
	 * 		current records. A return value of null indicates that the ID has
	 * 		been removed from the PDB.
	 */
	public static List<String> getReplacement(String oldPdbId, boolean recurse, boolean includeObsolete) {
		List<Map<String,String>> attrList = getStatusIdRecords(new String[] {oldPdbId});
		//Expect a single record
		if(attrList == null || attrList.size() != 1) {
			System.err.println("Error getting Status for "+oldPdbId+" from the PDB website.");
			return null;
		}
		
		Map<String,String> attrs = attrList.get(0);
		
		//Check that the record matches pdbId
		String id = attrs.get("structureId");
		if(id == null || !id.equalsIgnoreCase(oldPdbId)) {
			System.err.println("Error: Results returned from the query don't match "+oldPdbId);
			return null;
		}
		
		//Check that the status is given
		String statusStr = attrs.get("status");
		if(statusStr == null ) {
			System.err.println("Error: No status returned for "+oldPdbId);
			return null;
		}
		
		Status status = Status.fromString(statusStr);
		if(status == null ) {
			System.err.println("Error: Unknown status '"+statusStr+"'");
			return null;
		}
		
		// If we're current, just return
		LinkedList<String> results = new LinkedList<String>();
		switch(status) {
			case CURRENT:
				results.add(oldPdbId);
				return results;
			case OBSOLETE: {
				String replacementStr = attrs.get("replacedBy");
				if(replacementStr == null) {
					System.err.format("Error: %s is OBSOLETE but lacks a replacedBy attribute.\n",oldPdbId);
					return null;
				}
				replacementStr = replacementStr.toUpperCase();
				//include this result
				if(includeObsolete) {
					results.add(oldPdbId);
				}
				// Some PDBs are not replaced.
				if(replacementStr.equals("NONE")) {
					return results; //empty
				}
				
				String[] replacements = replacementStr.split(" ");
				Arrays.sort(replacements, new Comparator<String>() {
					public int compare(String o1, String o2) {
						return o2.compareToIgnoreCase(o1);
					}
				});
				for(String replacement : replacements) {

					// Return the replacement.
					if(recurse) {
						List<String> others = PDBStatus.getReplacement(replacement, recurse, includeObsolete);
						mergeReversed(results,others);
					}
					else {
						if(includeObsolete) {
							mergeReversed(results,Arrays.asList(replacement));
						} else {
							// check status of replacement
							Status replacementStatus = getStatus(replacement);
							switch(replacementStatus) {
							case OBSOLETE:
								//ignore obsolete
								break;
							case CURRENT:
							default:
								// include it
								mergeReversed(results,Arrays.asList(replacement));
							}
						}
					}
				}
				
				
				return results;
			}
			case UNKNOWN:
				return null;
			default: { //TODO handle other cases explicitly. They might have other syntax than "replacedBy"
				String replacementStr = attrs.get("replacedBy");
				
				if(replacementStr == null) {
					// If no "replacedBy" attribute, treat like we're current
					// TODO is this correct?
					results.add(oldPdbId);
					return results;
				}

				replacementStr = replacementStr.toUpperCase();
				// Some PDBs are not replaced.
				if(replacementStr.equals("NONE")) {
					return null;
				}
				
				
				//include this result, since it's not obsolete
				results.add(oldPdbId);
				
				String[] replacements = replacementStr.split(" ");
				Arrays.sort(replacements, new Comparator<String>() {
					public int compare(String o1, String o2) {
						return o2.compareToIgnoreCase(o1);
					}
				});
				for(String replacement : replacements) {

					// Return the replacement.
					if(recurse) {
						List<String> others = PDBStatus.getReplacement(replacement, recurse, includeObsolete);
						mergeReversed(results,others);
					}
					else {
						mergeReversed(results,Arrays.asList(replacement));
					}
				}
				
				
				return results;
			}
		}
	}

	/**
	 * Takes two reverse sorted lists of strings and merges the second into the
	 * first. Duplicates are removed.
	 * 
	 * @param merged A reverse sorted list. Modified by this method to contain
	 * 		the contents of other.
	 * @param other A reverse sorted list. Not modified.
	 */
	private static void mergeReversed(List<String> merged,
			final List<String> other) {
		
		if(other.isEmpty())
			return;

		if(merged.isEmpty()) {
			merged.addAll(other);
			return;
		}
		
		ListIterator<String> m = merged.listIterator();
		ListIterator<String> o = other.listIterator();
		
		String nextM, prevO;
		prevO = o.next();
		while(m.hasNext()) {
			// peek at m
			nextM = m.next();
			m.previous();
			
			//insert from O until exhausted or occurs after nextM
			while(prevO.compareTo(nextM) > 0) {
				m.add(prevO);
				if(!o.hasNext()) {
					return;
				}
				prevO = o.next();
			}
			//remove duplicates
			if(prevO.equals(nextM)) {
				if(!o.hasNext()) {
					return;
				}
				prevO = o.next();
			}
			
			m.next();
		}
		m.add(prevO);
		while(o.hasNext()) {
			m.add(o.next());
		}
		
	}
	

	/**
	 * Get the ID of the protein which was made obsolete by newPdbId.
	 * 
	 * @param newPdbId PDB ID of the newer structure
	 * @param recurse If true, return all ancestors of newPdbId.
	 * 		Otherwise, just go one step newer than oldPdbId.
	 * @return A (possibly empty) list of ID(s) of the ancestor(s) of
	 * 		newPdbId, or <tt>null</tt> if an error occurred.
	 */
	public static List<String> getReplaces(String newPdbId, boolean recurse) {
		List<Map<String,String>> attrList = getStatusIdRecords(new String[] {newPdbId});
		//Expect a single record
		if(attrList == null || attrList.size() != 1) {
			//TODO Is it possible to have multiple record per ID?
			// They seem to be combined into one record with space-delimited 'replaces'
			System.err.println("Error getting Status for "+newPdbId+" from the PDB website.");
			return null;
		}
		
		Map<String,String> attrs = attrList.get(0);
		
		//Check that the record matches pdbId
		String id = attrs.get("structureId");
		if(id == null || !id.equals(newPdbId)) {
			System.err.println("Error: Results returned from the query don't match "+newPdbId);
			return null;
		}
		
		
		String replacedList = attrs.get("replaces"); //space-delimited list
		if(replacedList == null) {
			// no replaces value; assume root
			return new ArrayList<String>();
		}
		String[] directDescendents = replacedList.split("\\s");
		
		// Not the root! Return the replaced PDB.
		if(recurse) {
			// Note: Assumes a proper directed acyclic graph of revisions
			// Cycles will cause infinite loops.
			List<String> allDescendents = new LinkedList<String>();
			for(String replaced : directDescendents) {
				List<String> roots = PDBStatus.getReplaces(replaced, recurse);
				mergeReversed(allDescendents,roots);
			}
			mergeReversed(allDescendents,Arrays.asList(directDescendents));
			
			return allDescendents;
		} else {
			return Arrays.asList(directDescendents);
		}
	}
	
	
	/**
	 * Fetches the status of one or more pdbIDs from the server.
	 * 
	 * <p>Returns the results as a list of Attributes.
	 * Each attribute should contain "structureId" and "status" attributes, and
	 * possibly more.
	 * 
	 * <p>Example:</br>
	 * <tt>http://www.rcsb.org/pdb/rest/idStatus?structureID=1HHB,4HHB</tt></br>
	 *<pre>&lt;idStatus&gt;
	 *  &lt;record structureId="1HHB" status="OBSOLETE" replacedBy="4HHB"/&gt;
	 *  &lt;record structureId="4HHB" status="CURRENT" replaces="1HHB"/&gt;
	 *&lt;/idStatus&gt;
	 * </pre>
	 * 
	 * @param pdbIDs
	 * @return A map between attributes and values
	 */
	private static List<Map<String, String>> getStatusIdRecords(String[] pdbIDs) {
		String serverName = System.getProperty(PDB_SERVER_PROPERTY);

		if ( serverName == null)
			serverName = DEFAULT_PDB_SERVER;
		else 
			System.out.format("Got System property %s=%s\n",PDB_SERVER_PROPERTY,serverName);

		// Build REST query URL
		if(pdbIDs.length < 1) {
			throw new IllegalArgumentException("No pdbIDs specified");
		}
		String urlStr = String.format("http://%s/pdb/rest/idStatus?structureId=%s",serverName,pdbIDs[0]);
		for(int i=1;i<pdbIDs.length;i++) {
			urlStr += "," + pdbIDs[i];
		}
		
		//System.out.println("Fetching " + urlStr);

		try {
			URL url = new URL(urlStr);

			InputStream uStream = url.openStream();
			
			/* // Print file directly
			BufferedReader r = new BufferedReader(new InputStreamReader(uStream));
			String line = r.readLine();
			while(line != null) {
				System.out.println(line);
				line = r.readLine();
			}
			r.close();
			
			uStream = url.openStream();
			*/
			
			
			InputSource source = new InputSource(uStream);
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
			SAXParser parser = parserFactory.newSAXParser();
			XMLReader reader = parser.getXMLReader();
			
			PDBStatusXMLHandler handler = new PDBStatusXMLHandler();
			
			reader.setContentHandler(handler);
			reader.parse(source);
			
			return handler.getRecords();
		} catch (Exception e){
			System.err.println("Problem getting status for " + pdbIDs.toString() + " from PDB server." );
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Handles idStatus xml by storing attributes for all record elements.
	 * 
	 * @author Spencer Bliven <sbliven@ucsd.edu>
	 *
	 */
	private static class PDBStatusXMLHandler extends DefaultHandler {
		private List<Map<String,String>> records;
		
		public PDBStatusXMLHandler() {
			records = new ArrayList<Map<String,String>>();
		}
		
		/**
		 * @param uri
		 * @param localName
		 * @param qName
		 * @param attributes
		 * @throws SAXException
		 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			//System.out.format("Starting element: uri='%s' localName='%s' qName='%s'\n", uri, localName, qName);
			if(qName.equals("record")) {
				//Convert attributes into a Map, as it should have been.
				//Important since SAX reuses Attributes objects for different calls
				Map<String,String> attrMap = new HashMap<String,String>(attributes.getLength()*2);
				for(int i=0;i<attributes.getLength();i++) {
					attrMap.put(attributes.getQName(i), attributes.getValue(i));
				}
				records.add(attrMap);
			}
		}


		/**
		 * @param e
		 * @throws SAXException
		 * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
		 */
		@Override
		public void error(SAXParseException e) throws SAXException {
			System.err.println(e.getMessage());
			super.error(e);
		}

		
		public List<Map<String, String>> getRecords() {
			return records;
		}
	}

}