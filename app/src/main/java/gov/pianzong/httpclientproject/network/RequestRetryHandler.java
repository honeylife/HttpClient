package gov.pianzong.httpclientproject.network;

import android.util.Log;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import javax.net.ssl.SSLException;

/**
 * 新增4.2用户主动abort中断异常情况
 */
public class RequestRetryHandler implements HttpRequestRetryHandler {

	private static final String TAG = "http";
	private static final int TIME = 1500;
	/** the number of times a method will be retried */
	private final int retryCount;

	/**
	 * Whether or not methods that have successfully sent their request will be
	 * retried
	 */
	private final boolean requestSentRetryEnabled;

	/**
	 * Default constructor
	 */
	public RequestRetryHandler(int retryCount, boolean requestSentRetryEnabled) {
		super();
		this.retryCount = retryCount;
		this.requestSentRetryEnabled = requestSentRetryEnabled;
	}

	/**
	 * Default constructor
	 */
	public RequestRetryHandler() {
		this(3, false);
	}

	/**
	 * Used <code>retryCount</code> and <code>requestSentRetryEnabled</code> to
	 * determine if the given method should be retried.
	 */
	public boolean retryRequest(final IOException exception,
			int executionCount, final HttpContext context) {
		Log.d(TAG, "retry:" + executionCount);
		if (exception == null) {
			throw new IllegalArgumentException(
					"Exception parameter may not be null");
		}
		if (context == null) {
			throw new IllegalArgumentException("HTTP context may not be null");
		}
		if (executionCount > this.retryCount) {
			// Do not retry if over max retry count
			return false;
		}
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (exception instanceof InterruptedIOException) {
			// Timeout
			return false;
		}
		if (exception instanceof UnknownHostException) {
			// Unknown host
			return false;
		}
		if (exception instanceof ConnectException) {
			// Connection refused
			return false;
		}
		if (exception instanceof SSLException) {
			// SSL handshake exception
			return false;
		}

		HttpRequest request = (HttpRequest) context
				.getAttribute(ExecutionContext.HTTP_REQUEST);

		if (requestIsAborted(request)) {
			return false;
		}

		if (handleAsIdempotent(request)) {
			// Retry if the request is considered idempotent
			Log.d(TAG, "retry - HttpEntityEnclosingRequest , count:"
					+ executionCount);
			return true;
		}

		Boolean b = (Boolean) context
				.getAttribute(ExecutionContext.HTTP_REQ_SENT);
		boolean sent = (b != null && b.booleanValue());

		if (!sent || this.requestSentRetryEnabled) {
			// Retry if the request has not been sent fully or
			// if it's OK to retry methods that have been sent
			Log.d(TAG, "requestSentRetryEnabledr true, count:" + executionCount);
			return true;
		}
		// otherwise do not retry
		return false;
	}

	/**
	 * @return <code>true</code> if this handler will retry methods that have
	 *         successfully sent their request, <code>false</code> otherwise
	 */
	public boolean isRequestSentRetryEnabled() {
		return requestSentRetryEnabled;
	}

	/**
	 * @return the maximum number of times a method will be retried
	 */
	public int getRetryCount() {
		return retryCount;
	}

	/**
	 * @since 4.2
	 */
	protected boolean handleAsIdempotent(final HttpRequest request) {
		return !(request instanceof HttpEntityEnclosingRequest);
	}

	/**
	 * @since 4.2
	 */
	protected boolean requestIsAborted(final HttpRequest request) {
		HttpRequest req = request;
		if (request instanceof RequestWrapper) { // does not forward request to
													// original
			req = ((RequestWrapper) request).getOriginal();
		}
		return (req instanceof HttpUriRequest && ((HttpUriRequest) req)
				.isAborted());
	}

}
