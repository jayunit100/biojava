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

package org.biojava.bio.program.xff;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.seq.io.SeqIOListener;
import org.biojava.utils.stax.DelegationManager;
import org.biojava.utils.stax.StAXContentHandler;
import org.biojava.utils.stax.StAXContentHandlerBase;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * StAX handler which converts and stream of parse events for an XFF
 * featureSet element into BioJava SeqIO events.
 *
 * <strong>NOTE</strong> This class is not thread-safe -- it
 * must only be used for one parse at any time.
 *
 * @author Thomas Down
 * @since 1.2
 */

public class XFFFeatureSetHandler extends StAXContentHandlerBase {
    public final static String PROPERTY_XFF_ID = "org.biojava.bio.program.xff.id";

    private List featureHandlers;
    private List detailHandlers;
    private SeqIOListener featureListener;

    {
	featureHandlers = new ArrayList();
	detailHandlers = new ArrayList();
    }		

    //
    // Current parse status
    //

    private boolean inFeatureSet = false;

    /**
     * Construct a new XFFFeatureSetHandler with the default set of handlers.
     */

    public XFFFeatureSetHandler() {
	addFeatureHandler(ElementRecognizer.ALL, FeatureHandler.FEATURE_HANDLER_FACTORY);
	addFeatureHandler(new ElementRecognizer.HasAttribute("strand"),
			  StrandedFeatureHandler.STRANDEDFEATURE_HANDLER_FACTORY);

	addDetailHandler(new ElementRecognizer.ByLocalName("prop"),
			 PropDetailHandler.PROPDETAIL_HANDLER_FACTORY);
    }

    /**
     * Set the object which receives startFeature/endFeature notifications.
     */

    public void setFeatureListener(SeqIOListener siol)
    {
	featureListener = siol;
    }

    /**
     * Return the object which receives startFeature/endFeature notifications.
     */

    public SeqIOListener getFeatureListener() {
	return featureListener;
    }

    /**
     * Extend this FeatureSetHandler to delegate certain feature elements
     * to the specified handler type.
     *
     * @param rec A selector for some sub-set of feature elements.
     * @param handler A factory which returns StAX handlers for matching elements.
     */

    public void addFeatureHandler(ElementRecognizer rec,
				  XFFPartHandlerFactory handler)
    {
	featureHandlers.add(new Binding(rec, handler));
    }

    /**
     * Extend this FeatureSetHandler to delegate certain detail elements
     * to the specified handler type.
     *
     * @param rec A selector for some sub-set of detail elements.
     * @param handler A factory which returns StAX handlers for matching elements.
     */

    public void addDetailHandler(ElementRecognizer rec,
				 XFFPartHandlerFactory handler)
    {
	detailHandlers.add(new Binding(rec, handler));
    }

    class Binding {
	final ElementRecognizer recognizer;
	final XFFPartHandlerFactory handlerFactory;

	Binding(ElementRecognizer er,
		XFFPartHandlerFactory hf)
	{
	    recognizer = er;
	    handlerFactory = hf;
	}
    }


    public void startElement(String nsURI,
			     String localName,
			     String qName,
			     Attributes attrs,
			     DelegationManager dm)
	 throws SAXException
    {
	if (localName.equals("featureSet")) {
	    inFeatureSet = true;
	    return;
	}

	for (int i = featureHandlers.size() - 1; i >= 0; --i) {
	    Binding b = (Binding) featureHandlers.get(i);
	    if (b.recognizer.filterStartElement(nsURI, localName, qName, attrs)) {
		dm.delegate(b.handlerFactory.getPartHandler(this));
		return;
	    }
	}

	throw new SAXException("Couldn't handle element " + localName + " in namespace " + nsURI);
    }

    public void endElement(String nsURI,
			   String localName,
			   String qName,
			   StAXContentHandler handler)
    {
	if (localName.equals("featureSet")) {
	    inFeatureSet = false;
	}
    }

    /**
     * Return a handler for the XFF <code>details</code> element.
     * This handler will, in turn, delegate to the specific detail
     * handlers provided with <code>addDetailHandler</code>
     */

    public StAXContentHandlerBase getDetailsHandler() {
	return new XFFDetailsHandler();
    }

    private class XFFDetailsHandler extends StAXContentHandlerBase {
	private boolean inDetails;
	
	public void startElement(String nsURI,
				 String localName,
				 String qName,
				 Attributes attrs,
				 DelegationManager dm)
	    throws SAXException
	{
	    if (localName.equals("details")) {
		inDetails = true;
		return;
	    }

	    for (int i = detailHandlers.size() - 1; i >= 0; --i) {
		Binding b = (Binding) detailHandlers.get(i);
		if (b.recognizer.filterStartElement(nsURI, localName, qName, attrs)) {
		    dm.delegate(b.handlerFactory.getPartHandler(XFFFeatureSetHandler.this));
		    return;
		}
	    }
	    
	    // Unknown detail types get silently ignored.
	}

	public void endElement(String nsURI,
			       String localName,
			       String qName)
	{
	    if (localName.equals("details")) {
		inDetails = false;
	    }
	}
    }
}
