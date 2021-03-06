/*
 *                            PUBLIC DOMAIN NOTICE
 *               National Center for Biotechnology Information
 *
 *  This software/database is a "United States Government Work" under the
 *  terms of the United States Copyright Act.  It was written as part of
 *  the author's official duties as a United States Government employee and
 *  thus cannot be copyrighted.  This software/database is freely available
 *  to the public for use. The National Library of Medicine and the U.S.
 *  Government have not placed any restriction on its use or reproduction.
 *
 *  Although all reasonable efforts have been taken to ensure the accuracy
 *  and reliability of the software and data, the NLM and the U.S.
 *  Government do not and cannot warrant the performance or results that
 *  may be obtained by using this software or data. The NLM and the U.S.
 *  Government disclaim all warranties, express or implied, including
 *  warranties of performance, merchantability or fitness for any particular
 *  purpose.
 *
 *  Please cite the author in any work or product based on this material.
 */

package gov.nih.nlm.ncbi.blastjni;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

final class BLAST_TEST {

  // hits
  private static final String LOCATION =
      "/panfs/pan1.be-md.ncbi.nlm.nih.gov/blastprojects/GCP_blastdb/50M/";
  // private static final String DB_PART = "nt_50M";
  private static final String PROGRAM = "blastn";
  private static final Integer TOP_N = 100;
  private static final String LOGLEVEL = "DEBUG";
  /*
  private static String part =
      "/panfs/pan1.be-md.ncbi.nlm.nih.gov/blastprojects/GCP_blastdb/50M/nt_50M.14"; // 14 & 18 have
    */
  private static String rid = "ReqID123";
  private static String queryseq =
      "CCGCAAGCCAGAGCAACAGCTCTAACAAGCAGAAATTCTGACCAAACTGATCCGGTAAAACCGATCAACG";

  private BLAST_TEST() {}

  public static void main(final String[] args) throws Exception {
    final Logger logger = LogManager.getLogger(BLAST_TEST.class);
    logger.info("Beginning");

    final BC_DATABASE_SETTING dbset = new BC_DATABASE_SETTING();
    dbset.key = "nt";
    dbset.worker_location = LOCATION;
    dbset.direct = true;
    final BC_CHUNK_VALUES values = new BC_CHUNK_VALUES("nt_50M.14");
    final BC_DATABASE_RDD_ENTRY chunk = new BC_DATABASE_RDD_ENTRY(dbset, values);

    // String query_url = "gs://blast-largequeries/query-021125518.txt";

    String params = "{";
    params += "\n\"version\": 1,";
    params += "\n \"RID\": \"" + rid + "\" ,";
    params += "\n \"blast_params\": { \"todo\": \"todo\" } }";
    params += "\n";
    /*
        if (args.length > 0) {
          final String[] req = args[0].split("\\:");
          if (req.length > 0) {
            part = req[0];
          }
          if (req.length > 1) {
            rid = req[1];
          }
          if (req.length > 2) {
            queryseq = req[2];
          }
          if (req.length > 3) {
            params = req[3];
          }
        }
    */
    String dbg = "";
    // dbg = dhb + String.format("partition ... %s\n", part);
    dbg = dbg + String.format("req-id ...... %s\n", rid);
    dbg = dbg + String.format("query ....... %s\n", queryseq);
    dbg = dbg + String.format("params ...... %s\n", params);
    logger.info(dbg);

    final BC_REQUEST requestobj = new BC_REQUEST();
    requestobj.id = rid;
    requestobj.query_seq = queryseq;
    // requestobj.query_url = query_url;
    requestobj.params = params;
    requestobj.db = params;
    requestobj.program = PROGRAM;

    if (!requestobj.valid()) {
      System.err.println("Warning: BC_REQUEST not valid:\t" + requestobj);
    }
    requestobj.top_n_prelim = TOP_N;
    requestobj.top_n_traceback = TOP_N;

    //    for (int i = 0; i != 10; ++i) {
    //      ConcLoad p = new ConcLoad( partitionobj );
    //      p.start();
    //    }

    // BLAST_SETTINGS bls = BLAST_SETTINGS.getValue();
    // BLAST_LIB blaster = BLAST_LIB_SINGLETON.get_lib(partitionobj, bls);
    System.out.println("Creating blaster");
    final BLAST_LIB blaster = new BLAST_LIB("blastjni.so", true);
    System.out.println("Created  blaster");

    params = "nt"; // FIX - When Blast team ready for JSON params

    final BLAST_HSP_LIST hspl[] = blaster.jni_prelim_search(chunk, requestobj, LOGLEVEL);
    System.out.println("--- PRELIM_SEARCH RESULTS ---");
    if (hspl == null) {
      System.out.println("NULL hspl");
    } else {
      System.out.println(" prelim_search returned " + hspl.length + " HSP lists:");
      for (final BLAST_HSP_LIST hsp : hspl) {
        System.out.println("HSP: " + hsp.toString().replace("\n", " "));
      }
    }

    final BLAST_TB_LIST[] tbs = blaster.jni_traceback(hspl, chunk, requestobj, LOGLEVEL);

    System.out.println("traceback done");
    System.out.println("--- TRACEBACK RESULTS ---");

    if (tbs == null) {
      System.out.println("NULL asn1");
    } else {
      for (final BLAST_TB_LIST tb : tbs) {
        System.out.println("TB: " + tb.toString().replace("\n", " "));
      }

      byte[][] oneasn = new byte[1][0];
      oneasn[0] = tbs[0].asn1_blob;
      // BLAST_TB_LIST.save(rid + ".seq-annot.asn1", oneasn);
      System.out.println("Dumped " + rid + " to file.");
    }
    logger.info("Finishing");
  }
}
