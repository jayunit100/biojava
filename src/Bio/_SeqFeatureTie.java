/*
 * File: SRC/BIO/_SEQFEATURETIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class _SeqFeatureTie extends Bio._SeqFeatureImplBase {
    public Bio._SeqFeatureOperations servant;
    public _SeqFeatureTie(Bio._SeqFeatureOperations servant) {
           this.servant = servant;
    }
    public void ref()
    {
        servant.ref();
    }
    public void unref()
    {
        servant.unref();
    }
    public org.omg.CORBA.Object query_interface(String repoid)
    {
        return servant.query_interface(repoid);
    }
    public String type()
    {
        return servant.type();
    }
    public String source()
    {
        return servant.source();
    }
    public String seq_primary_id()
    {
        return servant.seq_primary_id();
    }
    public int start()
    {
        return servant.start();
    }
    public int end()
    {
        return servant.end();
    }
    public short strand()
    {
        return servant.strand();
    }
    public Bio.NameValueSet[] qualifiers()
    {
        return servant.qualifiers();
    }
    public boolean has_PrimarySeq()
    {
        return servant.has_PrimarySeq();
    }
    public Bio.PrimarySeq get_PrimarySeq()
        throws Bio.UnableToProcess    {
        return servant.get_PrimarySeq();
    }
}
