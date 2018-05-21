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

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpBackOffUnsuccessfulResponseHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.client.util.Sleeper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * RetryHttpInitializerWrapper will automatically retry upon RPC
 * failures, preserving the auto-refresh behavior of the Google
 * Credentials.
 */
public class RetryHttpInitializerWrapper implements HttpRequestInitializer {

  /**
   * A private logger.
   */
  private static final Logger LOG =
      Logger.getLogger(RetryHttpInitializerWrapper.class.getName());

  /**
   * One minutes in miliseconds.
   */
  private static final int ONEMINITUES = 60000;

  /**
   * Intercepts the request for filling in the "Authorization"
   * header field, as well as recovering from certain unsuccessful
   * error codes wherein the Credential must refresh its token for a
   * retry.
   */
  private final Credential wrappedCredential;

  /**
   * A sleeper; you can replace it with a mock in your test.
   */
  private final Sleeper sleeper;

  /**
   * A constructor.
   *
   * @param wrappedCredential Credential which will be wrapped and
   * used for providing auth header.
   */
  public RetryHttpInitializerWrapper(final Credential wrappedCredential) {
    this(wrappedCredential, Sleeper.DEFAULT);
  }

  /**
   * A protected constructor only for testing.
   *
   * @param wrappedCredential Credential which will be wrapped and
   * used for providing auth header.
   * @param sleeper Sleeper for easy testing.
   */
  RetryHttpInitializerWrapper(
      final Credential wrappedCredential, final Sleeper sleeper) {
    this.wrappedCredential = Preconditions.checkNotNull(wrappedCredential);
    this.sleeper = sleeper;
  }

  /**
   * Initializes the given request.
   */
  @Override
  public final void initialize(final HttpRequest request) {
    request.setReadTimeout(2 * ONEMINITUES); // 2 minutes read timeout
    final HttpUnsuccessfulResponseHandler backoffHandler =
        new HttpBackOffUnsuccessfulResponseHandler(
            new ExponentialBackOff())
            .setSleeper(sleeper);
    request.setInterceptor(wrappedCredential);
    request.setUnsuccessfulResponseHandler(
        new HttpUnsuccessfulResponseHandler() {
          @Override
          public boolean handleResponse(
              final HttpRequest request,
              final HttpResponse response,
              final boolean supportsRetry) throws IOException {
            if (wrappedCredential.handleResponse(
                request, response, supportsRetry)) {
              // If credential decides it can handle it,
              // the return code or message indicated
              // something specific to authentication,
              // and no backoff is desired.
              return true;
            } else if (backoffHandler.handleResponse(
                request, response, supportsRetry)) {
              // Otherwise, we defer to the judgement of
              // our internal backoff handler.
              LOG.info("Retrying "
                  + request.getUrl().toString());
              return true;
            } else {
              return false;
            }
          }
        });
    request.setIOExceptionHandler(
        new HttpBackOffIOExceptionHandler(new ExponentialBackOff())
            .setSleeper(sleeper));
  }
}
