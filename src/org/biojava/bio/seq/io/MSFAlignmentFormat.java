/*
 * put your module comment here
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.biojava.bio.seq.io;

import  org.biojava.bio.seq.io.AlignmentFormat;
import  org.biojava.bio.symbol.Alignment;
import  org.biojava.bio.symbol.*;
import  java.io.BufferedReader;
import  org.apache.regexp.*;
import  org.biojava.bio.seq.*;
import  org.biojava.bio.seq.io.*;
import  java.lang.Integer;
import  java.io.*;
import  java.util.*;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:
 * @version 1.0
 */
public class MSFAlignmentFormat {
       // implements AlignmentFormat {
    private static final boolean DEBUGPRINT = false;
    private static final int DNA = 1;
    private static final int PROTEIN = 2;

    /**
     * put your documentation comment here
     */
    public MSFAlignmentFormat () {
    }

    /**
     * used to quick test the code
     * @param args
     */
    public static void main (String[] args) {
        String filename;
        if (args.length < 1) {
            filename = "SimpleMSF.msf";         //change to your favorite
        }
        else {
            filename = args[0];
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            MSFAlignmentFormat MSFAlignmentFormat1 = new MSFAlignmentFormat();
            MSFAlignmentFormat1.read(reader);
        } catch (Exception E) {}
    }

    /**
     * put your documentation comment here
     * @param reader
     * @return Alignment
     */
    public Alignment read (BufferedReader reader) throws Exception{
        Vector sequenceNames = new Vector();
        String sequenceName = null;
        String sequenceData[] = null;
        int startOfData = 0;                    //the start of the sequence data in the line
        int currSeqCount = 0;                   //which sequence data you are currently trying to get
        try {
            RE mtc = new RE("Name:\\s+(.*?)\\s+(oo|Len:)");
            RE removewhitespace = new RE("\\s");
            // REMatch rem = null;
            String line = reader.readLine();
            //parse past header
            while (line.indexOf("Name:") == -1) {
                line = reader.readLine();
            }
            //read each name (between Name:   and Len:
            while ((line.indexOf("//") == -1) && ((line.trim()).length() !=
                    0)) {
                mtc.match(line);
                sequenceName = line.substring(mtc.getParenStart(1), mtc.getParenEnd(1)).trim();
                if (sequenceName == null) {
                    break;
                }               //end of sequence names
                //sequenceName = line.substring(rem.getSubStartIndex(1),
                //                              rem.getSubEndIndex(1));
                if ((line.trim()).length() == 0) {
                    break;
                }
                sequenceNames.add(sequenceName);
                line = reader.readLine();
            }
            sequenceData = new String[sequenceNames.size()];
            for (int it = 0; it < sequenceNames.size(); it++) {
                sequenceData[it] = new String();
            }
            //until you get a line that matches the first sequence
            while (line.indexOf((String)sequenceNames.get(0)) == -1)          // || (   (line.trim()) .length()>0  )    )
            {
                line = reader.readLine();
            }
            //now you on the first line of the sequence data
            while (line != null) {
                for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                    if (line.indexOf((String)sequenceNames.get(currSeqCount))
                            == -1) {
                        break;
                    }           //error

                    startOfData = line.indexOf((String)sequenceNames.get(currSeqCount))
                            + ((String)sequenceNames.get(currSeqCount)).length();
                    line = (line.substring(startOfData));
                    line = removewhitespace.subst(line, "", RE.REPLACE_ALL);
                    sequenceData[currSeqCount] = sequenceData[currSeqCount].concat(line);
                    line = reader.readLine();
                    if ((currSeqCount < sequenceNames.size() - 1) && (line.trim().length() == 0)) {
                        break;
                    }           //could be an error
                }
                //until you get a line that matches the first sequence
                while ((line != null) && (line.indexOf((String)sequenceNames.get(0))
                        == -1))                 // || (   (line.trim()) .length()>0  )    )
                {
                    line = reader.readLine();
                }
            }
            //print them out for testing
            if (DEBUGPRINT) {
                for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                    System.out.println((String)sequenceNames.get(currSeqCount)
                            + ":" + sequenceData[currSeqCount]);
                }
            }
            //check DNA, RNA or Prot
            StringBuffer testString = new StringBuffer();
            int agct = 0;
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                testString.append(sequenceData[currSeqCount]);
            }
            StringTokenizer st = null;
            st = new StringTokenizer(testString.toString().toLowerCase(), "a");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "g");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "c");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "t");
            agct += st.countTokens();
            st = new StringTokenizer(testString.toString().toLowerCase(), "u");
            agct += st.countTokens();
            //now parse through them and create gapped symbol lists
            HashMap sequenceDataMap = new HashMap();
            Symbol sym = null;
            FiniteAlphabet alph = null;
            /*if ((agct/testString.length()) > 0.90) {            //if DNA alph
                if (st.countTokens() > 0) {                     //rna alph
                    //get the rna alph
                    alph = DNATools.getDNA();
                }
                else {          //get DNA alph
                    alph = DNATools.getDNA();
                }
            }
            else {
                alph = ProteinTools.getTAlphabet();
            }*/
            //replaced above method of protein/dna determination with method below
            for (int i = 0; i < testString.toString().length(); i++) {
                if (Character.toUpperCase(testString.toString().charAt(i)) == 'F' ||
                    Character.toUpperCase(testString.toString().charAt(i)) == 'L' ||
                    Character.toUpperCase(testString.toString().charAt(i)) == 'I' ||
                    Character.toUpperCase(testString.toString().charAt(i)) == 'P' ||
                    Character.toUpperCase(testString.toString().charAt(i)) == 'Q' ||
                    Character.toUpperCase(testString.toString().charAt(i)) == 'E') {
                        alph = ProteinTools.getTAlphabet();
                }
            }
            if (alph == null) {
                alph = DNATools.getDNA();
            }
            SymbolTokenization parse = alph.getTokenization("token");
            for (currSeqCount = 0; currSeqCount < sequenceNames.size(); currSeqCount++) {
                String sd = null;
                //change stop codons to specified symbols
                sd = sequenceData[currSeqCount].replace('~', '-');              //sometimes this is a term signal not a gap
                sd = sequenceData[currSeqCount].replace('.', '-');              //sometimes this is a term signal not a gap
                StringBuffer sb = new StringBuffer();
                SymbolList sl = null;
                sequenceDataMap.put((String)sequenceNames.get(currSeqCount),
                        new SimpleSymbolList(parse, sd));
            }
            return  (new SimpleAlignment(sequenceDataMap));
        } catch (Exception e) {
            System.err.println("MSFFormatReader " + e.getMessage());
             throw (e);
        }
        //return  (null);
    }           //end read it

    //This is where I am writing an alignment writer
    public void write(OutputStream os, Alignment align, int fileType) throws Exception {
        PrintStream out = new PrintStream(os);
        Object labels[] = align.getLabels().toArray();
        int numSeqs = labels.length;
        Iterator seqIts[] = new Iterator[numSeqs];
        int maxLabelLength = 0;
        for (int i = 0; i < numSeqs; i++) {
            seqIts[i] = align.symbolListForLabel(labels[i]).iterator();
            if (((String) labels[i]).length() > maxLabelLength) {
                maxLabelLength = ((String) labels[i]).length();
            }
        }
        String nl = System.getProperty("line.separator");
        SymbolTokenization toke = null;


        if (fileType == DNA) {
            out.println("!!NA_MULTIPLE_ALIGNMENT");
            out.println();
            out.print(" MSF: " + align.length() + "  Type: ");
            out.print("N");
            toke = DNATools.getDNA().getTokenization("token");
        }
        else if (fileType == PROTEIN) {
            out.println("!!AA_MULTIPLE_ALIGNMENT");
            out.println();
            out.print(" MSF: " + align.length() + "  Type: ");
            out.print("P");
            toke = ProteinTools.getTAlphabet().getTokenization("token");
        }
        else {
            System.out.println("MSFAlignment.write -- File type not recognized.");
            return;
        }
        out.print("  .." + nl);
        out.println();

        for (int i = 0; i < numSeqs; i++) {
            out.print(" Name: " + labels[i]);
            for (int j = 0; j < (maxLabelLength - ((String) labels[i]).length()); j++) {
                out.print(" ");
            }
            out.print("  Len: " + align.length() + nl);
        }

        out.println("//");
        out.println();

        while (seqIts[0].hasNext()) {
            for (int i = 0; i < numSeqs; i++) {
                while (((String) labels[i]).length() < maxLabelLength + 1) {
                    labels[i] = " " + labels[i];
                }
                out.print(labels[i] + " ");
                theLabel:
                for (int j = 0; j < 5; j++) {
                    out.print(" ");
                    for (int k = 0; k < 10; k++) {
                        if (seqIts[i].hasNext()) {
                            out.print(toke.tokenizeSymbol((Symbol) seqIts[i].next()));
                        }
                        else {
                            break theLabel;
                        }
                    }
                }
                out.print(nl);
            }
            out.print(nl);
        }

    } //end write

    public void writeDna(OutputStream os, Alignment align) throws Exception {
        write(os, align, DNA);
    }

    public void writeProtein(OutputStream os, Alignment align) throws Exception {
        write(os, align, PROTEIN);
    }

}               //end class



