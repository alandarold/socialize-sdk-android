/*
 * Copyright (c) 2011 Socialize Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.socialize.auth;

import android.content.Context;

import com.socialize.api.SocializeAuthRequest;
import com.socialize.error.SocializeException;
import com.socialize.listener.AuthProviderListener;

/**
 * An empty provider to replace a real one when we don't want the dependency.
 * @author Jason Polites
 *
 */
public class EmptyAuthProvider implements AuthProvider<AuthProviderInfo> {

	/* (non-Javadoc)
	 * @see com.socialize.auth.AuthProvider#authenticate(com.socialize.api.SocializeAuthRequest, java.lang.String, com.socialize.listener.AuthProviderListener)
	 */
	@Override
	public void authenticate(SocializeAuthRequest authRequest, String appId, AuthProviderListener listener) {
		listener.onAuthFail(new SocializeException("Empty auth provider used!"));
	}
	

	@Override
	public void authenticate(AuthProviderInfo info, AuthProviderListener listener) {
		listener.onAuthFail(new SocializeException("Empty auth provider used!"));
	}

	/* (non-Javadoc)
	 * @see com.socialize.auth.AuthProvider#clearCache(android.content.Context, java.lang.String)
	 */
	@Override
	public void clearCache(Context context, String appId) {}


	@Override
	public void clearCache(Context context, AuthProviderInfo info) {}
}
