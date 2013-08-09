/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.socket.client;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;


/**
 * Abstract base class for {@link WebSocketClient} implementations.
 *
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public abstract class AbstractWebSocketClient implements WebSocketClient {

	protected final Log logger = LogFactory.getLog(getClass());

	private static final Set<String> disallowedHeaders = new HashSet<String>();

	static {
		disallowedHeaders.add("cache-control");
		disallowedHeaders.add("cookie");
		disallowedHeaders.add("connection");
		disallowedHeaders.add("host");
		disallowedHeaders.add("sec-websocket-extensions");
		disallowedHeaders.add("sec-websocket-key");
		disallowedHeaders.add("sec-websocket-protocol");
		disallowedHeaders.add("sec-websocket-version");
		disallowedHeaders.add("pragma");
		disallowedHeaders.add("upgrade");
	}


	@Override
	public WebSocketSession doHandshake(WebSocketHandler webSocketHandler, String uriTemplate,
			Object... uriVars) throws WebSocketConnectFailureException {

		Assert.notNull(uriTemplate, "uriTemplate must not be null");
		URI uri = UriComponentsBuilder.fromUriString(uriTemplate).buildAndExpand(uriVars).encode().toUri();
		return doHandshake(webSocketHandler, null, uri);
	}

	@Override
	public final WebSocketSession doHandshake(WebSocketHandler webSocketHandler,
			HttpHeaders headers, URI uri) throws WebSocketConnectFailureException {

		Assert.notNull(webSocketHandler, "webSocketHandler must not be null");
		Assert.notNull(uri, "uri must not be null");

		String scheme = uri.getScheme();
		Assert.isTrue(((scheme != null) && ("ws".equals(scheme) || "wss".equals(scheme))), "Invalid scheme: " + scheme);

		if (logger.isDebugEnabled()) {
			logger.debug("Connecting to " + uri);
		}

		HttpHeaders headersToUse = new HttpHeaders();
		if (headers != null) {
			for (String header : headers.keySet()) {
				if (!disallowedHeaders.contains(header.toLowerCase())) {
					headersToUse.put(header, headers.get(header));
				}
			}
		}

		List<String> subProtocols = new ArrayList<String>();
		if ((headers != null) && (headers.getSecWebSocketProtocol() != null)) {
			subProtocols.addAll(headers.getSecWebSocketProtocol());
		}

		return doHandshakeInternal(webSocketHandler, headersToUse, uri, subProtocols);
	}

	/**
	 *
	 *
	 * @param webSocketHandler the client-side handler for WebSocket messages
	 * @param headers HTTP headers to use for the handshake, with unwanted (forbidden)
	 *        headers filtered out, never {@code null}
	 * @param uri the target URI for the handshake, never {@code null}
	 * @param subProtocols requested sub-protocols, or an empty list
	 * @return the established WebSocket session
	 * @throws WebSocketConnectFailureException
	 */
	protected abstract WebSocketSession doHandshakeInternal(WebSocketHandler webSocketHandler,
			HttpHeaders headers, URI uri, List<String> subProtocols) throws WebSocketConnectFailureException;

}
