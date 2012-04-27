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
package com.socialize.api;

import java.util.List;
import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import com.socialize.Socialize;
import com.socialize.api.action.ActionType;
import com.socialize.api.action.ShareType;
import com.socialize.auth.AuthProvider;
import com.socialize.auth.AuthProviderData;
import com.socialize.auth.AuthProviderInfo;
import com.socialize.auth.AuthProviderResponse;
import com.socialize.auth.AuthProviderType;
import com.socialize.auth.AuthProviders;
import com.socialize.config.SocializeConfig;
import com.socialize.entity.ActionError;
import com.socialize.entity.ListResult;
import com.socialize.entity.Propagation;
import com.socialize.entity.SocializeAction;
import com.socialize.entity.SocializeObject;
import com.socialize.error.SocializeException;
import com.socialize.listener.AuthProviderListener;
import com.socialize.listener.SocializeActionListener;
import com.socialize.listener.SocializeAuthListener;
import com.socialize.location.SocializeLocationProvider;
import com.socialize.log.SocializeLogger;
import com.socialize.networks.ShareOptions;
import com.socialize.networks.SocialNetwork;
import com.socialize.notifications.NotificationChecker;
import com.socialize.provider.SocializeProvider;
import com.socialize.util.AppUtils;
import com.socialize.util.HttpUtils;
import com.socialize.util.StringUtils;

/**
 * @author Jason Polites
 *
 * @param <T>
 * @param <P>
 */
public class SocializeApi<T extends SocializeObject, P extends SocializeProvider<T>> {

	private P provider;
	private SocializeResponseFactory<T> responseFactory;
	private AuthProviders authProviders;
	private SocializeLogger logger;
	private HttpUtils httpUtils;
	private SocializeLocationProvider locationProvider;
	private NotificationChecker notificationChecker;
	private AppUtils appUtils;
	
	public static enum RequestType {AUTH,PUT,POST,PUT_AS_POST,GET,LIST,LIST_AS_GET,LIST_WITHOUT_ENTITY,DELETE};
	
	public SocializeApi(P provider) {
		super();
		this.provider = provider;
	}
	
	public void clearSession() {
		provider.clearSession();
	}
	
	public void clearSession(AuthProviderType type) {
		provider.clearSession(type);
	}
	
	public SocializeSession loadSession(String endpoint, String key, String secret) throws SocializeException {
		return provider.loadSession(endpoint, key, secret);
	}
	
	public SocializeSession authenticate(Context context, String endpoint, String key, String secret, String uuid) throws SocializeException {
		SocializeSession session = provider.authenticate(endpoint, key, secret, uuid);
		checkNotifications(context, session);
		return session;
	}
	
	public SocializeSession authenticate(Context context, String endpoint, String key, String secret, AuthProviderData data, String udid) throws SocializeException {
		SocializeSession session = provider.authenticate(endpoint, key, secret, data, udid);
		checkNotifications(context, session);
		return session;
	}
	
	protected void checkNotifications(Context context, SocializeSession session) {
		if(notificationChecker != null) {
			notificationChecker.checkRegistrations(context, session);
		}
	}
	
	
	protected void setPropagationData(SocializeAction action, ShareOptions shareOptions) {
		if(shareOptions != null) {
			SocialNetwork[] shareTo = shareOptions.getShareTo();
			if(shareTo != null) {
				Propagation propagation = null;
				Propagation localPropagation = null;
				
				for (SocialNetwork socialNetwork : shareTo) {
					if(socialNetwork.isLocalPropagation() || shareOptions.isSelfManaged()) {
						if(localPropagation == null) {
							localPropagation = newPropagation();
						}
						localPropagation.addThirdParty(socialNetwork);
					}
					else {
						if(propagation == null) {
							propagation = newPropagation();
						}
						propagation.addThirdParty(socialNetwork);
					}
				}
				
				SocializeConfig config = Socialize.getSocialize().getConfig();
				String appStore = config.getProperty(SocializeConfig.REDIRECT_APP_STORE);
				
				if(!StringUtils.isEmpty(appStore)) {
					
					String abbrev = appUtils.getAppStoreAbbreviation(appStore);
					
					if(localPropagation != null) {
						localPropagation.addExtraParam("f", abbrev);
					}
					
					if(propagation != null) {
						propagation.addExtraParam("f", abbrev);
					}
				}		
				
				action.setPropagation(propagation);
				action.setPropagationInfoRequest(localPropagation);
			}
		}
	}
	
	protected void setPropagationData(SocializeAction action, ShareType shareType) {
		if(shareType != null) {
			Propagation localPropagation = newPropagation();
			localPropagation.addThirdParty(shareType);
			
			SocializeConfig config = Socialize.getSocialize().getConfig();
			String appStore = config.getProperty(SocializeConfig.REDIRECT_APP_STORE);
			
			if(!StringUtils.isEmpty(appStore)) {
				String abbrev = appUtils.getAppStoreAbbreviation(appStore);
				localPropagation.addExtraParam("f", abbrev);
			}		
			action.setPropagationInfoRequest(localPropagation);
		}
	}	
	
	// Mockable
	protected Propagation newPropagation() {
		return new Propagation();
	}

	public ListResult<T> list(SocializeSession session, String endpoint, String key, String...ids) throws SocializeException {
		return provider.list(session, endpoint, key, ids, 0, SocializeConfig.MAX_LIST_RESULTS);
	}
	
	public ListResult<T> list(SocializeSession session, String endpoint, String key, String idKey, int startIndex, int endIndex, String...ids) throws SocializeException {
		return provider.list(session, endpoint, key, ids, idKey, startIndex, endIndex);
	}
	
	public ListResult<T> list(SocializeSession session, String endpoint, String key, int startIndex, int endIndex, String...ids) throws SocializeException {
		return provider.list(session, endpoint, key, ids, startIndex, endIndex);
	}
	
	public ListResult<T> list(SocializeSession session, String endpoint) throws SocializeException {
		return provider.list(session, endpoint, 0, SocializeConfig.MAX_LIST_RESULTS);
	}
	
	public ListResult<T> list(SocializeSession session, String endpoint, int startIndex, int endIndex) throws SocializeException {
		return provider.list(session, endpoint, startIndex, endIndex);
	}
	
	public T get(SocializeSession session, String endpoint, String id) throws SocializeException {
		return provider.get(session, endpoint, id);
	}
	
	public T get(SocializeSession session, String endpoint, String id, ActionType type) throws SocializeException {
		return provider.get(session, endpoint, id, type);
	}
	
	public void delete(SocializeSession session, String endpoint, String id) throws SocializeException {
		provider.delete(session, endpoint, id);
	}
	
	public ListResult<T> put(SocializeSession session, String endpoint, T object) throws SocializeException {
		return provider.put(session, endpoint, object);
	}

	public ListResult<T> put(SocializeSession session, String endpoint, List<T> objects) throws SocializeException {
		return provider.put(session, endpoint, objects);
	}

	public ListResult<T> post(SocializeSession session, String endpoint, T object, boolean isJSONResponse) throws SocializeException {
		return provider.post(session, endpoint, object, isJSONResponse);
	}
	
	public ListResult<T> post(SocializeSession session, String endpoint, List<T> objects) throws SocializeException {
		return post(session, endpoint, objects, true);
	}
	
	public ListResult<T> post(SocializeSession session, String endpoint, List<T> objects, boolean isJSONResponse) throws SocializeException {
		return provider.post(session, endpoint, objects, isJSONResponse);
	}
	
	public T putAsPost(SocializeSession session, String endpoint, T object) throws SocializeException {
		return provider.putAsPost(session, endpoint, object);
	}
	
	public void listAsync(SocializeSession session, String endpoint, String key, int startIndex, int endIndex, SocializeActionListener listener, String...ids) {
		listAsync(session, endpoint, key,  "id", startIndex, endIndex, listener, ids);
	}
	
	public void listAsync(SocializeSession session, String endpoint, String key, String idKey, int startIndex, int endIndex, SocializeActionListener listener, String...ids) {
		AsyncGetter getter = new AsyncGetter(session, listener);
		SocializeGetRequest request = new SocializeGetRequest();
		request.setEndpoint(endpoint);
		request.setRequestType(RequestType.LIST);
		request.setKey(key);
		request.setIds(ids);
		request.setIdKey(idKey);
		request.setStartIndex(startIndex);
		request.setEndIndex(endIndex);
		getter.execute(request);
	}
	
	public void listAsync(SocializeSession session, String endpoint, String key, SocializeActionListener listener, String...ids) {
		listAsync(session, endpoint, key, 0, SocializeConfig.MAX_LIST_RESULTS, listener, ids);
	}
	
	public void listAsync(SocializeSession session, String endpoint, int startIndex, int endIndex, SocializeActionListener listener) {
		AsyncGetter getter = new AsyncGetter(session, listener);
		SocializeGetRequest request = new SocializeGetRequest();
		request.setRequestType(RequestType.LIST_WITHOUT_ENTITY);
		request.setEndpoint(endpoint);
		request.setStartIndex(startIndex);
		request.setEndIndex(endIndex);
		getter.execute(request);
	}
	
	public void listAsync(SocializeSession session, String endpoint, SocializeActionListener listener) {
		listAsync(session, endpoint, 0, SocializeConfig.MAX_LIST_RESULTS, listener);
	}

	public void getByEntityAsync(SocializeSession session, String endpoint, String key, SocializeActionListener listener) {
		AsyncGetter getter = new AsyncGetter(session, listener);
		SocializeGetRequest request = new SocializeGetRequest();
		request.setRequestType(RequestType.LIST_AS_GET);
		request.setEndpoint(endpoint);
		request.setKey(key);
		getter.execute(request);
	}	
	
	
	public void getAsync(SocializeSession session, String endpoint, String id, SocializeActionListener listener) {
		AsyncGetter getter = new AsyncGetter(session, listener);
		SocializeGetRequest request = new SocializeGetRequest();
		request.setRequestType(RequestType.GET);
		request.setEndpoint(endpoint);
		request.setIds(id);
		getter.execute(request);
	}
	
	public void deleteAsync(SocializeSession session, String endpoint, String id, SocializeActionListener listener) {
		AsyncGetter getter = new AsyncGetter(session, listener);
		SocializeGetRequest request = new SocializeGetRequest();
		request.setRequestType(RequestType.DELETE);
		request.setEndpoint(endpoint);
		request.setIds(id);
		getter.execute(request);
	}

	@SuppressWarnings("unchecked")
	public void putAsync(SocializeSession session, String endpoint, T object, SocializeActionListener listener) {
		AsyncPutter poster = new AsyncPutter(session, listener);
		SocializePutRequest<T> request = new SocializePutRequest<T>();
		request.setRequestType(RequestType.PUT);
		request.setEndpoint(endpoint);
		request.setObject(object);
		poster.execute(request);
	}
	
	@SuppressWarnings("unchecked")
	public void putAsync(SocializeSession session, String endpoint, List<T> objects, SocializeActionListener listener) {
		AsyncPutter poster = new AsyncPutter(session, listener);
		SocializePutRequest<T> request = new SocializePutRequest<T>();
		request.setRequestType(RequestType.PUT);
		request.setEndpoint(endpoint);
		request.setObjects(objects);
		poster.execute(request);
	}
	
	public void postAsync(SocializeSession session, String endpoint, T object, SocializeActionListener listener) {
		postAsync(session, endpoint, object, true, listener);
	}
	
	@SuppressWarnings("unchecked")
	public void postAsync(SocializeSession session, String endpoint, T object, boolean jsonResponse, SocializeActionListener listener) {
		AsyncPutter poster = new AsyncPutter(session, listener);
		SocializePutRequest<T> request = new SocializePutRequest<T>();
		request.setRequestType(RequestType.POST);
		request.setEndpoint(endpoint);
		request.setObject(object);
		request.setJsonResponse(jsonResponse);
		poster.execute(request);
	}

	public void postAsync(SocializeSession session, String endpoint, List<T> objects, SocializeActionListener listener) {
		postAsync(session, endpoint, objects, true, listener);
	}
	@SuppressWarnings("unchecked")
	public void postAsync(SocializeSession session, String endpoint, List<T> objects, boolean jsonResponse, SocializeActionListener listener) {
		AsyncPutter poster = new AsyncPutter(session, listener);
		SocializePutRequest<T> request = new SocializePutRequest<T>();
		request.setRequestType(RequestType.POST);
		request.setEndpoint(endpoint);
		request.setObjects(objects);
		request.setJsonResponse(jsonResponse);
		poster.execute(request);
	}

	/**
	 * Does a POST, but expects a single object in return.
	 * @param session
	 * @param endpoint
	 * @param object
	 * @param listener
	 */
	@SuppressWarnings("unchecked")
	public void putAsPostAsync(SocializeSession session, String endpoint, T object, SocializeActionListener listener) {
		AsyncPutter poster = new AsyncPutter(session, listener);
		SocializePutRequest<T> request = new SocializePutRequest<T>();
		request.setRequestType(RequestType.PUT_AS_POST);
		request.setEndpoint(endpoint);
		request.setObject(object);
		poster.execute(request);
	}

	public void authenticateAsync(
			Context context,
			String key, 
			String secret, 
			String uuid, 
			final AuthProviderData data,
			final SocializeAuthListener listener, 
			final SocializeSessionConsumer sessionConsumer, 
			boolean do3rdPartyAuth) {

		SocializeActionListener wrapper = null;
		
		final SocializeAuthListener localListener = new SocializeAuthListener() {
			
			@Override
			public void onError(SocializeException error) {
				if(listener != null) {
					listener.onError(error);
				}
				else {
					if(logger != null) {
						logger.error("Failure during auth", error);
					}
				}
			}
			
			@Override
			public void onCancel() {
				if(listener != null) {
					listener.onCancel();
				}
			}
			
			@Override
			public void onAuthSuccess(SocializeSession session) {
				if(sessionConsumer != null) {
					sessionConsumer.setSession(session);
				}
				if(listener != null) {
					listener.onAuthSuccess(session);
				}				
			}
			
			@Override
			public void onAuthFail(SocializeException error) {
				if(listener != null) {
					listener.onAuthFail(error);
				}	
				else {
					if(logger != null) {
						logger.error("Failure during auth", error);
					}
				}
			}
		};		
		
		wrapper = new SocializeActionListener() {
			
			@Override
			public void onResult(RequestType type, SocializeResponse response) {
				SocializeAuthResponse authResponse = (SocializeAuthResponse) response;
				localListener.onAuthSuccess(authResponse.getSession());
			}
			
			@Override
			public void onError(SocializeException error) {
				if(httpUtils != null && httpUtils.isAuthError(error)) {
					localListener.onAuthFail(error);
				}
				else {
					localListener.onError(error);
				}
			}
		};

		final SocializeAuthRequest request = new SocializeAuthRequest();
		
		request.setEndpoint("/authenticate/");
		request.setConsumerKey(key);
		request.setConsumerSecret(secret);
		request.setUdid(uuid);
		request.setAuthProviderData(data);
		
		AuthProviderType authProviderType = getAuthProviderType(data);
		
		if(do3rdPartyAuth && !authProviderType.equals(AuthProviderType.SOCIALIZE)) {
			handle3rdPartyAuth(context, request, wrapper, localListener, key, secret);
		}
		else {
			// Do normal auth
			handleRegularAuth(context, request, wrapper, localListener, key, secret, false);
		}
	}
	
	protected AuthProviderType getAuthProviderType(AuthProviderData data) {
		if(data == null) {
			return AuthProviderType.SOCIALIZE;
		}
		
		AuthProviderType authProviderType = data.getAuthProviderType();
		
		AuthProviderInfo authProviderInfo = data.getAuthProviderInfo();
		
		if(authProviderInfo != null) {
			authProviderType = authProviderInfo.getType();
		}
		
		return authProviderType;
	}
	
	protected void validate(AuthProviderData data) throws SocializeException{
		AuthProviderInfo authProviderInfo = data.getAuthProviderInfo();
		
		if(authProviderInfo != null) {
			authProviderInfo.validate();
		}
		else {
			// Legacy
			validateLegacy(data);
		}
	}
	
	protected void validateLegacy(AuthProviderData data) throws SocializeException {
		AuthProviderType authProviderType = getAuthProviderType(data);
		
		String appId3rdParty = data.getAppId3rdParty();
		if(authProviderType != null && 
				authProviderType.equals(AuthProviderType.FACEBOOK) && 
				StringUtils.isEmpty(appId3rdParty)) {
			throw new SocializeException("No app ID found for auth type FACEBOOK");
		}
	}
	
	protected void handleRegularAuth(Context context, SocializeAuthRequest request, SocializeActionListener wrapper, SocializeAuthListener listener, String key, String secret, boolean force) {
		SocializeSession session = null;
		
		if(!force) {
			// Try loading the session first
			
			try {
				session = loadSession(request.getEndpoint(), key, secret);
			}
			catch (SocializeException e) {
				// No need to throw this, just log it
				logger.warn("Failed to load saved session data", e);
			}
		}
		
		if(session != null) {
			if(logger != null && logger.isDebugEnabled()) {
				logger.debug("Loaded saved session for user [" +
						session.getUser().getId() +
						"]");
			}
			
			if(listener != null) {
				listener.onAuthSuccess(session);
			}
		}
		else {
			AsyncAuthenicator authenicator = new AsyncAuthenicator(context, null, wrapper);
			authenicator.execute(request);
		}
	}
	
	protected void handle3rdPartyAuth(
			final Context context,
			final SocializeAuthRequest request,
			final SocializeActionListener wrapper,
			final SocializeAuthListener listener, 
			final String key,
			final String secret)  {

		final AuthProviderData authProviderData = request.getAuthProviderData();
		
		// Try loading the session first
		SocializeSession session = null;
		
		AuthProviderType authProviderType = getAuthProviderType(authProviderData);
		
		try {
			validate(authProviderData);
		} 
		catch (SocializeException error) {
			if(listener != null) {
				listener.onError(error);
			}
			else if(logger != null) {
				logger.error("Error validating auth provider data", error);
			}
		}
		
		try {
			session = loadSession(request.getEndpoint(), key, secret);
		}
		catch (SocializeException e) {
			// No need to throw this, just log it
			logger.warn("Failed to load saved session data", e);
		}
					
		if(session == null || !provider.validateSession(session, authProviderData)) {
			// Get the provider for the type
			AuthProvider<AuthProviderInfo> authProvider = authProviders.getProvider(authProviderType);
			
			if(authProvider != null) {
				
				AuthProviderListener authProviderListener = new AuthProviderListener() {
					
					@Override
					public void onError(SocializeException error) {
						if(listener != null) {
							listener.onError(error);
						}
					}
					
					@Override
					public void onAuthSuccess(AuthProviderResponse response) {
						
						authProviderData.setUserId3rdParty(response.getUserId());
						authProviderData.setToken3rdParty(response.getToken());
						authProviderData.setSecret3rdParty(response.getSecret());
						
						// Do normal auth (forced)
						handleRegularAuth(context, request, wrapper, listener, key, secret, true);
					}
					
					@Override
					public void onAuthFail(SocializeException error) {
						if(listener != null) {
							listener.onAuthFail(error);
						}
					}

					@Override
					public void onCancel() {
						if(listener != null) {
							listener.onCancel();
						}
					}
				};				
				
				AuthProviderInfo authProviderInfo = authProviderData.getAuthProviderInfo();
				
				if(authProviderInfo != null) {
					authProvider.authenticate(authProviderInfo, authProviderListener);
				}
				else {
					if(listener != null) {
						listener.onError(new SocializeException("Authentication failed.  No AuthProviderInfo found"));
					}
					
					logger.error("No AuthProviderInfo found during auth");
				}
			}
			else {
				logger.error("No provider found for auth type [" +
						authProviderType.getName() +
						"]");
			}
		}
		else {
			// We already have a session
			// then just call the listener
			if(listener != null) {
				listener.onAuthSuccess(session);
			}
		}
	}
	
	public void setResponseFactory(SocializeResponseFactory<T> responseFactory) {
		this.responseFactory = responseFactory;
	}
	
	public void setAppUtils(AppUtils appUtils) {
		this.appUtils = appUtils;
	}
	
	public void setNotificationChecker(NotificationChecker notificationChecker) {
		this.notificationChecker = notificationChecker;
	}
	
	public void setAuthProviders(AuthProviders authProviders) {
		this.authProviders = authProviders;
	}
	
	public void setLogger(SocializeLogger logger) {
		this.logger = logger;
	}
	
	public void setHttpUtils(HttpUtils httpUtils) {
		this.httpUtils = httpUtils;
	}

	public void setLocationProvider(SocializeLocationProvider locationProvider) {
		this.locationProvider = locationProvider;
	}

	abstract class AbstractAsyncProcess<Params extends SocializeRequest, Progress, Result extends SocializeResponse> extends AsyncTask<Params, Progress, Result> {

		RequestType requestType;
		SocializeSession session;
		Exception error = null;
		SocializeActionListener listener = null;
		Context context;
		
		@Deprecated
		public AbstractAsyncProcess(Context context, RequestType requestType, SocializeSession session, SocializeActionListener listener) {
			this(requestType, session, listener);
			this.context = context;
		}		
		
		@Deprecated
		public AbstractAsyncProcess(RequestType requestType, SocializeSession session, SocializeActionListener listener) {
			super();
			this.requestType = requestType;
			this.session = session;
			this.listener = listener;
		}
		
		public AbstractAsyncProcess(Context context, SocializeSession session, SocializeActionListener listener) {
			this(session, listener);
			this.context = context;
		}		
		
		public AbstractAsyncProcess(SocializeSession session, SocializeActionListener listener) {
			super();
			this.session = session;
			this.listener = listener;
		}		

		@Override
		protected final Result doInBackground(Params... params) {
			Params request = params[0];
			this.requestType = request.getRequestType();
			
			Result result = null;
			try {
				result = doInBackground(request);
			}
			catch (Exception error) {
				this.error = error;
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(Result result) {
			if(listener != null) {
				if(error != null) {
					listener.onError(SocializeException.wrap(error));
				}
				else {
					listener.onResult(requestType, result);
				}
			}
		}

		protected abstract Result doInBackground(Params param) throws SocializeException;
	}
	
	class AsyncAuthenicator extends AbstractAsyncProcess<SocializeAuthRequest, Void, SocializeAuthResponse> {

		@Deprecated
		public AsyncAuthenicator(Context context, RequestType requestType, SocializeSession session, SocializeActionListener listener) {
			super(context, requestType, session, listener);
		}

		public AsyncAuthenicator(Context context, SocializeSession session, SocializeActionListener listener) {
			super(context, session, listener);
		}
		
		@Override
		protected SocializeAuthResponse doInBackground(final SocializeAuthRequest request) throws SocializeException {
			SocializeAuthResponse response = null;
			
			if(responseFactory != null) {
				response = responseFactory.newAuthResponse();
			}
			else {
				response = new SocializeAuthResponse();
			}
			
			SocializeSession session = null;
			
			AuthProviderData authProviderData = request.getAuthProviderData();
			AuthProviderType authProviderType = getAuthProviderType(authProviderData);
			
			if(authProviderType == null || authProviderType.equals(AuthProviderType.SOCIALIZE)) {
				session = SocializeApi.this.authenticate(context, request.getEndpoint(), request.getConsumerKey(), request.getConsumerSecret(), request.getUdid());
			}
			else {
				session = SocializeApi.this.authenticate(context, request.getEndpoint(), request.getConsumerKey(), request.getConsumerSecret(), authProviderData, request.getUdid());
			}
			
			response.setSession(session);
			
			return response;
		}
	}
	
	protected void setLocation(SocializeAction action, Location location) {
		if(location == null && locationProvider != null) {
			location = locationProvider.getLastKnownLocation();
		}
		if(location != null) {
			action.setLon(location.getLongitude());
			action.setLat(location.getLatitude());
		}
	}
	
	protected void setLocation(SocializeAction action, ShareOptions shareOptions) {
		
		Location location = null;
		
		if(shareOptions != null) {
			location = shareOptions.getLocation();
		}
		
		setLocation(action, location);
	}	
	
	class AsyncPutter extends AbstractAsyncProcess<SocializePutRequest<T>, Void, SocializeEntityResponse<T>> {

		@Deprecated
		public AsyncPutter(RequestType requestType, SocializeSession session, SocializeActionListener listener) {
			super(requestType, session, listener);
		}
		
		public AsyncPutter(SocializeSession session, SocializeActionListener listener) {
			super(session, listener);
		}		

		@Override
		protected SocializeEntityResponse<T> doInBackground(SocializePutRequest<T> request) throws SocializeException {

			SocializeEntityResponse<T> response = null;
			
			if(responseFactory != null) {
				response = responseFactory.newEntityResponse();
			}
			else {
				response = new SocializeEntityResponse<T>();
			}
			
			response.setResultsExpected( request.isJsonResponse() );

			ListResult<T> results = null;

			switch (requestType) {
			
			case POST:
				
				if(request.getObjects() != null) {
					results = SocializeApi.this.post(session, request.getEndpoint(), request.getObjects(), request.isJsonResponse());
				}
				else if(request.getObject() != null) {
					results = SocializeApi.this.post(session, request.getEndpoint(), request.getObject(), request.isJsonResponse());
				}
				
				response.setResults(results);
				
				break;

			case PUT:
				
				if(request.getObjects() != null) {
					results = SocializeApi.this.put(session, request.getEndpoint(), request.getObjects());
				}
				else if(request.getObject() != null) {
					results = SocializeApi.this.put(session, request.getEndpoint(), request.getObject());
				}
				
				response.setResults(results);
				
				break;
				
			case PUT_AS_POST:
				
				if(request.getObject() != null) {
					T result = SocializeApi.this.putAsPost(session, request.getEndpoint(), request.getObject());
					response.addResult(result);
				}
			}

			return response;
		}

		@Override
		protected void onPostExecute(SocializeEntityResponse<T> result) {
			if(listener != null) {
				if(error != null) {
					listener.onError(SocializeException.wrap(error));
				}
				else {
					ListResult<T> results = result.getResults();
					
					if(results != null) {
						List<T> items = results.getItems();
						List<ActionError> errors = results.getErrors();
						
						if((items == null || items.size() == 0) && result.isResultsExpected()){
							if(errors != null && errors.size() > 0) {
								listener.onError(new SocializeException(errors.get(0).getMessage()));
							}
							else {
								listener.onError(new SocializeException("No results found in response"));
							}
						}
						else {
							if(errors != null && errors.size() > 0) {
								for (ActionError actionError : errors) {
									if(logger != null) {
										if(!StringUtils.isEmpty(actionError.getMessage())) {
											logger.warn(actionError.getMessage());
										}
									}
								}
							}
							
							listener.onResult(requestType, result);
						}
					}
					else if(result.isResultsExpected()) {
						listener.onError(new SocializeException("No results found in response"));
					}
				}
			}
		}
	}

	class AsyncGetter extends AbstractAsyncProcess<SocializeGetRequest, Void, SocializeEntityResponse<T>> {

		@Deprecated
		public AsyncGetter(RequestType requestType, SocializeSession session, SocializeActionListener listener) {
			super(requestType, session, listener);
		}
		
		public AsyncGetter(SocializeSession session, SocializeActionListener listener) {
			super(session, listener);
		}

		@Override
		protected SocializeEntityResponse<T> doInBackground(SocializeGetRequest request) throws SocializeException {

			SocializeEntityResponse<T> response = null;
			
			if(responseFactory != null) {
				response = responseFactory.newEntityResponse();
			}
			else {
				response = new SocializeEntityResponse<T>();
			}
			
			ListResult<T> results = null;
			
			switch (requestType) {
			
			case GET:
				T result = SocializeApi.this.get(session, request.getEndpoint(), request.getIds()[0]);
				response.addResult(result);
				break;

			case LIST:
				results = SocializeApi.this.list(session, request.getEndpoint(), request.getKey(), request.getIdKey(), request.getStartIndex(), request.getEndIndex(), request.getIds());
				response.setResults(results);
				break;
				
			case LIST_AS_GET:
				results = SocializeApi.this.list(session, request.getEndpoint(), request.getKey(), request.getIdKey(), request.getStartIndex(), request.getEndIndex(), request.getIds());
				response.setResults(results);
				break;				
				
			case LIST_WITHOUT_ENTITY:
				results = SocializeApi.this.list(session, request.getEndpoint(), request.getStartIndex(), request.getEndIndex());
				response.setResults(results);
				break;
			
			case DELETE:
				SocializeApi.this.delete(session, request.getEndpoint(), request.getIds()[0]);
				break;
			}

			return response;
		}
	}
}
