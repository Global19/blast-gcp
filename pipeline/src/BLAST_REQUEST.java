/*===========================================================================
 *
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
 *
 * ===========================================================================
 *
 */
package gov.nih.nlm.ncbi.blastjni;

import java.io.Serializable;

class BLAST_REQUEST implements Serializable
{
    public final String req_id, db_selector, query, program, params;
    public final Integer top_n;

    public BLAST_REQUEST( final String line, final Integer top_n )
    {
        String[] parts = line.split( "\\:" );
        this.req_id      = ( parts.length > 0 ) ? parts[ 0 ] : "Req_Id_0001";
        this.query       = ( parts.length > 1 ) ? parts[ 1 ] : "CCGCAAGCCAGAGCAACAGCTCTAACAAGCAGAAATTCTGACCAAACTGATCCGGTAAAACCGATCAACG";
        this.top_n       = ( parts.length > 2 ) ? Integer.parseInt( parts[ 2 ] ) : top_n;
        this.db_selector = ( parts.length > 3 ) ? parts[ 3 ] : "nt";
        this.program     = ( parts.length > 4 ) ? parts[ 4 ] : "blastn";
        this.params      = ( parts.length > 5 ) ? parts[ 5 ] : "blastn";
    }

    @Override public String toString()
    {
        if ( query.length() > 10 )
            return String.format( "req( rid:'%s' dbsel:'%s' query:'%s...' prog:'%s' params:'%s' )",
                    req_id, db_selector, query.substring( 0, 10 ), program, params );
        else
            return String.format( "req( rid:'%s' dbsel:'%s' query:'%s' prog:'%s' params:'%s' )",
                    req_id, db_selector, query, program, params );
    }
}