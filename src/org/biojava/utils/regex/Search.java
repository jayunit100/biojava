


package org.biojava.utils.regex;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import org.biojava.bio.seq.Sequence;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;

/**
 * A utility class to make searching a Sequence with many regex patterns
 * easier.
 * @author David Huen
 * @since 1.4
 */
public class Search
{
    /**
     * Interface for a class that will recieve match information
     * from this class.
     */
    public interface Listener
    {
        /**
         * @param seq Sequence on which the search was conducted.
         * @param pattern Pattern object used to conduct search.
         * @param start start coordinate of match.
         * @param end end of match plus one.
         */
        public void reportMatch(Sequence seq, Pattern pattern, int start, int end);
    }

    private class PatternInfo
    {
        private String patternString;
        private Pattern pattern = null;
        private Matcher matcher = null;
        private boolean overlap;

        public boolean equals(Object o)
        {
            if (o instanceof PatternInfo) {
                PatternInfo other = (PatternInfo) o;
                return patternString.equals(other.patternString);
            }
            else
                return false;
        }

        public int hashCode()
        {
            return patternString.hashCode();
        }
    }

    private FiniteAlphabet alfa;
    private Listener listener = null;
    private PatternFactory factory;
    private Set patterns = new HashSet();

    public Search(FiniteAlphabet alfa)
    {
        this.alfa = alfa;
        factory = PatternFactory.makeFactory(alfa);
    }

    public void setListener(Listener listener)
    {
        this.listener = listener;
    }

    /**
     * add a search pattern to the searches to be conducted
     * by this object.
     * @param patternString String representation of the pattern.
     * @param overlap if true, the search continues at the base following the start to the previous hit.
     * If false, it continues at the base after the existing hit.
     */
    public void addPattern(String patternString, boolean overlap)
    {
        Pattern pattern = factory.compile(patternString);
        PatternInfo info = new PatternInfo();
        info.patternString = patternString;
        info.pattern = pattern;
        info.overlap = overlap;
        patterns.add(info);
    }

    public char charValue(Symbol sym)
        throws IllegalSymbolException
    {
        return factory.charValue(sym);
    }

    /**
     * search the Sequence with the patterns already registered with this object.
     */
    public void search(Sequence seq)
    {
        for (Iterator patternsI = patterns.iterator(); patternsI.hasNext(); ) {
            PatternInfo info = (PatternInfo) patternsI.next();
            if (info.matcher == null) {
                info.matcher = info.pattern.matcher(seq);
            }
            else {
                info.matcher = info.matcher.reset(seq);
            }

            // now exhaustively search the sequence
            int begin = 1;
            while (info.matcher.find(begin)) {
                // got a hit
                int start = info.matcher.start();
                int end = info.matcher.end();
                if (info.overlap)
                    begin = Math.min(start + 1, seq.length());
                else
                    begin = Math.min(end, seq.length());;
                if (listener != null) listener.reportMatch(seq, info.pattern, start, end);
            }
        }
    }
}
