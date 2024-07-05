package com.konloch.av.gui.js.webserver.http.request;

import com.konloch.av.gui.js.webserver.http.client.ClientBuffer;

/**
 * Takes in a request and responds with a byte array containing the request results.
 *
 * The request object has variables that can be adjusted to modify the webserver response.
 *
 * @author Konloch
 * @since 3/1/2023
 */
public interface RequestListener
{
	byte[] request(ClientBuffer buffer);
}
