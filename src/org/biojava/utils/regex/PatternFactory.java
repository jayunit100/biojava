



package org.biojava.utils.regex;

import java.util.Iterator;

import org.biojava.bio.BioException;
import org.biojava.bio.symbol.AtomicSymbol;
import org.biojava.bio.symbol.Alphabet;
import org.biojava.bio.symbol.AlphabetManager;
import org.biojava.bio.symbol.AbstractAlphabet;
import org.biojava.bio.symbol.FiniteAlphabet;
import org.biojava.bio.symbol.IllegalSymbolException;
import org.biojava.bio.symbol.Symbol;
import org.biojava.bio.seq.io.SymbolTokenization;
import org.biojava.bio.seq.io.CharacterTokenization;

/**
 * A class that creates Patterns for regex matching on 
 * SymbolLists of a specific Alphabet.
 * @author David Huen
 * @since 1.4
 */
public class PatternFactory
{
    private FiniteAlphabet alfa;
    private SymbolTokenization toke = null;

    PatternFactory(FiniteAlphabet alfa)
    {
        this.alfa = alfa;
        fetchTokenizer();
    }

    private void fetchTokenizer()
    {
        boolean gotCharTokenizer =false;
        try {
            toke = alfa.getTokenization("token");
            if (toke.getTokenType() == SymbolTokenization.CHARACTER)
                gotCharTokenizer = true;
        }
        catch (BioException be) {
        }

        if (!gotCharTokenizer) {
            // make own tokenizer for this turkey
            CharacterTokenization cToke = new CharacterTokenization(alfa, true);

            // go thru' and associate all atomic symbols with a unicode char
            char uniChar = '\uE000';
            for (Iterator symI = alfa.iterator(); symI.hasNext(); ) {
                AtomicSymbol sym = (AtomicSymbol) symI.next();
                cToke.bindSymbol(sym, uniChar);
                uniChar++;
            }

            // add all ambiguity symbol
            cToke.bindSymbol(
                AlphabetManager.getAllAmbiguitySymbol((FiniteAlphabet) alfa),
                '\uF8FF');
            // add terminal gap
            cToke.bindSymbol(Alphabet.EMPTY_ALPHABET.getGapSymbol(), '~');
            // add interstitial gap
            cToke.bindSymbol(alfa.getGapSymbol(), '-');

            // bind alphabet to this tokenization
            ((AbstractAlphabet) alfa).putTokenization("unicode", cToke);
            toke = cToke;
        }
    }

    /**
     * Returns a Pattern object that applies the specified regex
     * against SymbolLists in the Alphabet that this PatternFactory 
     * was defined against.
     */
    public org.biojava.utils.regex.Pattern compile(String pattern)
    {
        // validate the pattern is from this alphabet
        // we only accept RE tokens and characters from 
        // the alphabet itself.
        return new org.biojava.utils.regex.Pattern(java.util.regex.Pattern.compile(pattern), alfa);
    }

    /**
     * Returns a Pattern object that applies the specified regex
     * against SymbolLists in the Alphabet that this PatternFactory
     * was defined against.
     *
     * @param pattern regex pattern expressed as a String.
     * @param label A String label assigned to the Pattern object.  Can be retrieved later with getName().
     */
    public org.biojava.utils.regex.Pattern compile(String pattern, String label)
    {
        // validate the pattern is from this alphabet
        // we only accept RE tokens and characters from
        // the alphabet itself.
        return new org.biojava.utils.regex.Pattern(java.util.regex.Pattern.compile(pattern), alfa, label);
    }

    /**
     * Returns the character that represents the specified Symbol in
     * the Alphabet that this PatternFactory was defined for.
     * <p>
     * The character will be ASCII in Alphabets that define a Character tokenization.
     * In Alphabets that don't a Unicode character in the private range is returned
     * instead and this can be used to assemble the String that is the argument
     * for the compile method.
     */
    public char charValue(Symbol sym)
        throws IllegalSymbolException
    {
        // this class is only used with alphabets that have a character tokenization.
        return toke.tokenizeSymbol(sym).charAt(0);
    }

    /**
     * Returns a factory for Patterns in the specified Alphabet.
     */
    public static PatternFactory makeFactory(FiniteAlphabet alfa)
    {
        return new PatternFactory(alfa);
    }
}
