/*
 * File: SRC/BIO/_SEQDBTIE.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class _SeqDBTie extends Bio._SeqDBImplBase {
    public Bio._SeqDBOperations servant;
    public _SeqDBTie(Bio._SeqDBOperations servant) {
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
    public String database_name()
    {
        return servant.database_name();
    }
    public short database_version()
    {
        return servant.database_version();
    }
    public Bio.PrimarySeqStream make_stream()
    {
        return servant.make_stream();
    }
    public Bio.PrimarySeq get_PrimarySeq(String primary_id)
        throws Bio.UnableToProcess    {
        return servant.get_PrimarySeq(primary_id);
    }
    public Bio.Seq get_Seq(String primary_id)
        throws Bio.UnableToProcess    {
        return servant.get_Seq(primary_id);
    }
}
