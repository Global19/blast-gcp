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

package gov.nih.nlm.ncbi.exp1;

import java.io.Console;
import java.io.IOException;

public final class EXP_MAIN
{
    private static void wait_for_console( final String exit_string, Integer sleeptime_ms )
    {
        Console cons = System.console();
        boolean running = true;

        while( running && cons != null )
        {
            running = ! cons.readLine().trim().equals( exit_string );
            if ( running )
            {
                try
                {
                    Thread.sleep( sleeptime_ms );
                }
                catch ( InterruptedException e )
                {
                }
            }
        }
    }

   public static void main( String[] args ) throws Exception
   {
        if ( args.length < 1 )
            System.out.println( "settings json-file missing" );
        else
        {
            final String appName = EXP_MAIN.class.getSimpleName();
            String ini_path = args[ 0 ];
            EXP_SETTINGS settings = EXP_SETTINGS_READER.read_from_json( ini_path, appName );
            System.out.println( String.format( "settings read from '%s'", ini_path ) );
            if ( !settings.valid() )
                System.out.println( settings.missing() );
            else
            {
                System.out.println( settings.toString() );

                EXP_DRIVER driver = new EXP_DRIVER( settings );
                driver.start();
                try
                {
                    wait_for_console( "exit", 500 );
                    driver.stop_driver();
                    driver.join();
                }
                catch ( InterruptedException e1 )
                {
                    System.out.println( String.format( "driver interrupted: %s", e1 ) );
                }
                catch( Exception e2 )
                {
                    System.out.println( String.format( "stopping driver: %s", e2 ) );
                }
            }
        }
   }
}
