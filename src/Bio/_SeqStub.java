/*
 * File: SRC/BIO/_SEQSTUB.JAVA
 * From: IDL/BIO.IDL
 * Date: Mon Feb 07 12:51:47 2000
 *   By: idltojava Java IDL 1.2 Aug 18 1998 16:25:34
 */

package Bio;
public class _SeqStub
	extends org.omg.CORBA.portable.ObjectImpl
    	implements Bio.Seq {

    public _SeqStub(org.omg.CORBA.portable.Delegate d) {
          super();
          _set_delegate(d);
    }

    private static final String _type_ids[] = {
        "IDL:Bio/Seq:1.0",
        "IDL:Bio/PrimarySeq:1.0",
        "IDL:GNOME/Unknown:1.0"
    };

    public String[] _ids() { return (String[]) _type_ids.clone(); }

    //	IDL operations
    //	    Implementation of ::GNOME::Unknown::ref
    public void ref()
 {
           org.omg.CORBA.Request r = _request("ref");
           r.invoke();
   }
    //	    Implementation of ::GNOME::Unknown::unref
    public void unref()
 {
           org.omg.CORBA.Request r = _request("unref");
           r.invoke();
   }
    //	    Implementation of ::GNOME::Unknown::query_interface
    public org.omg.CORBA.Object query_interface(String repoid)
 {
           org.omg.CORBA.Request r = _request("query_interface");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_objref));
           org.omg.CORBA.Any _repoid = r.add_in_arg();
           _repoid.insert_string(repoid);
           r.invoke();
           org.omg.CORBA.Object __result;
           __result = r.return_value().extract_Object();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::type
    public Bio.SeqType type()
 {
           org.omg.CORBA.Request r = _request("type");
           r.set_return_type(Bio.SeqTypeHelper.type());
           r.invoke();
           Bio.SeqType __result;
           __result = Bio.SeqTypeHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::length
    public int length()
 {
           org.omg.CORBA.Request r = _request("length");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::get_seq
    public String get_seq()
        throws Bio.RequestTooLarge {
           org.omg.CORBA.Request r = _request("get_seq");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.exceptions().add(Bio.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(Bio.RequestTooLargeHelper.type())) {
                   throw Bio.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::get_subseq
    public String get_subseq(int start, int end)
        throws Bio.OutOfRange, Bio.RequestTooLarge {
           org.omg.CORBA.Request r = _request("get_subseq");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           org.omg.CORBA.Any _start = r.add_in_arg();
           _start.insert_long(start);
           org.omg.CORBA.Any _end = r.add_in_arg();
           _end.insert_long(end);
           r.exceptions().add(Bio.OutOfRangeHelper.type());
           r.exceptions().add(Bio.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(Bio.OutOfRangeHelper.type())) {
                   throw Bio.OutOfRangeHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(Bio.RequestTooLargeHelper.type())) {
                   throw Bio.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::display_id
    public String display_id()
 {
           org.omg.CORBA.Request r = _request("display_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::primary_id
    public String primary_id()
 {
           org.omg.CORBA.Request r = _request("primary_id");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::accession_number
    public String accession_number()
 {
           org.omg.CORBA.Request r = _request("accession_number");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
           r.invoke();
           String __result;
           __result = r.return_value().extract_string();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::version
    public int version()
 {
           org.omg.CORBA.Request r = _request("version");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::PrimarySeq::max_request_length
    public int max_request_length()
 {
           org.omg.CORBA.Request r = _request("max_request_length");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::Seq::all_features
    public Bio.SeqFeature[] all_features()
        throws Bio.RequestTooLarge {
           org.omg.CORBA.Request r = _request("all_features");
           r.set_return_type(Bio.SeqFeatureListHelper.type());
           r.exceptions().add(Bio.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(Bio.RequestTooLargeHelper.type())) {
                   throw Bio.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           Bio.SeqFeature[] __result;
           __result = Bio.SeqFeatureListHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::Seq::all_features_iterator
    public Bio.SeqFeatureIterator all_features_iterator()
 {
           org.omg.CORBA.Request r = _request("all_features_iterator");
           r.set_return_type(Bio.SeqFeatureIteratorHelper.type());
           r.invoke();
           Bio.SeqFeatureIterator __result;
           __result = Bio.SeqFeatureIteratorHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::Seq::features_region
    public Bio.SeqFeature[] features_region(int start, int end)
        throws Bio.OutOfRange, Bio.UnableToProcess, Bio.RequestTooLarge {
           org.omg.CORBA.Request r = _request("features_region");
           r.set_return_type(Bio.SeqFeatureListHelper.type());
           org.omg.CORBA.Any _start = r.add_in_arg();
           _start.insert_long(start);
           org.omg.CORBA.Any _end = r.add_in_arg();
           _end.insert_long(end);
           r.exceptions().add(Bio.OutOfRangeHelper.type());
           r.exceptions().add(Bio.UnableToProcessHelper.type());
           r.exceptions().add(Bio.RequestTooLargeHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(Bio.OutOfRangeHelper.type())) {
                   throw Bio.OutOfRangeHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(Bio.UnableToProcessHelper.type())) {
                   throw Bio.UnableToProcessHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(Bio.RequestTooLargeHelper.type())) {
                   throw Bio.RequestTooLargeHelper.extract(__userEx.except);
               }
           }
           Bio.SeqFeature[] __result;
           __result = Bio.SeqFeatureListHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::Seq::features_region_iterator
    public Bio.SeqFeatureIterator features_region_iterator(int start, int end)
        throws Bio.OutOfRange, Bio.UnableToProcess {
           org.omg.CORBA.Request r = _request("features_region_iterator");
           r.set_return_type(Bio.SeqFeatureIteratorHelper.type());
           org.omg.CORBA.Any _start = r.add_in_arg();
           _start.insert_long(start);
           org.omg.CORBA.Any _end = r.add_in_arg();
           _end.insert_long(end);
           r.exceptions().add(Bio.OutOfRangeHelper.type());
           r.exceptions().add(Bio.UnableToProcessHelper.type());
           r.invoke();
           java.lang.Exception __ex = r.env().exception();
           if (__ex instanceof org.omg.CORBA.UnknownUserException) {
               org.omg.CORBA.UnknownUserException __userEx = (org.omg.CORBA.UnknownUserException) __ex;
               if (__userEx.except.type().equals(Bio.OutOfRangeHelper.type())) {
                   throw Bio.OutOfRangeHelper.extract(__userEx.except);
               }
               if (__userEx.except.type().equals(Bio.UnableToProcessHelper.type())) {
                   throw Bio.UnableToProcessHelper.extract(__userEx.except);
               }
           }
           Bio.SeqFeatureIterator __result;
           __result = Bio.SeqFeatureIteratorHelper.extract(r.return_value());
           return __result;
   }
    //	    Implementation of ::Bio::Seq::max_feature_request
    public int max_feature_request()
 {
           org.omg.CORBA.Request r = _request("max_feature_request");
           r.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_long));
           r.invoke();
           int __result;
           __result = r.return_value().extract_long();
           return __result;
   }
    //	    Implementation of ::Bio::Seq::get_PrimarySeq
    public Bio.PrimarySeq get_PrimarySeq()
 {
           org.omg.CORBA.Request r = _request("get_PrimarySeq");
           r.set_return_type(Bio.PrimarySeqHelper.type());
           r.invoke();
           Bio.PrimarySeq __result;
           __result = Bio.PrimarySeqHelper.extract(r.return_value());
           return __result;
   }

};
