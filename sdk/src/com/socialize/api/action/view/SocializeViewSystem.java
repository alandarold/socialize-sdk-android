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
package com.socialize.api.action.view;

import java.util.ArrayList;
import java.util.List;
import android.location.Location;
import com.socialize.api.SocializeApi;
import com.socialize.api.SocializeSession;
import com.socialize.entity.Entity;
import com.socialize.entity.User;
import com.socialize.entity.View;
import com.socialize.error.SocializeException;
import com.socialize.listener.view.ViewListListener;
import com.socialize.listener.view.ViewListener;
import com.socialize.provider.SocializeProvider;

/**
 * @author Jason Polites
 */
public class SocializeViewSystem extends SocializeApi<View, SocializeProvider<View>> implements ViewSystem {

	public SocializeViewSystem(SocializeProvider<View> provider) {
		super(provider);
	}
	
	/* (non-Javadoc)
	 * @see com.socialize.api.action.ViewSystem#addView(com.socialize.api.SocializeSession, com.socialize.entity.Entity, android.location.Location, com.socialize.listener.view.ViewListener)
	 */
	@Override
	public void addView(SocializeSession session, Entity entity, Location location, ViewListener listener) {
		View c = new View();
		c.setEntity(entity);
		
		setLocation(c, location);
		
		List<View> list = new ArrayList<View>(1);
		list.add(c);
		
		postAsync(session, ENDPOINT, list, listener);
	}
	
	/* (non-Javadoc)
	 * @see com.socialize.api.action.ViewSystem#getViewsByEntity(com.socialize.api.SocializeSession, java.lang.String, int, int, com.socialize.listener.view.ViewListener)
	 */
	@Override
	public void getViewsByEntity(SocializeSession session, String key, int startIndex, int endIndex, ViewListener listener) {
		listAsync(session, ENDPOINT, key, null, startIndex, endIndex, listener);
	}
	
	/* (non-Javadoc)
	 * @see com.socialize.api.action.ViewSystem#getViewsByUser(com.socialize.api.SocializeSession, long, com.socialize.listener.view.ViewListener)
	 */
	@Override
	public void getViewsByUser(SocializeSession session, long userId, int startIndex, int endIndex, ViewListener listener) {
		String endpoint = "/user/" + userId + ENDPOINT;
		listAsync(session, endpoint, startIndex, endIndex, listener);
	}

	@Override
	public void getView(SocializeSession session, Entity entity, final ViewListener listener) {
		final User user = session.getUser();
		if(user != null) {
			
			final Long userId = user.getId();
			
			String endpoint = "/user/" + userId.toString() + ENDPOINT;
			listAsync(session, endpoint, entity.getKey(), null, 0, 1, new ViewListListener() {
				
				@Override
				public void onError(SocializeException error) {
					listener.onError(error);
				}
				
				@Override
				public void onList(List<View> items, int totalSize) {
					if(items != null && items.size() > 0) {
						listener.onGet(items.get(0));
					}
					else {
						listener.onGet(null);
					}
				}
			});
		}
		else {
			listener.onError(new SocializeException("Invalid session [No user object found]"));
		}
	}

	@Override
	public void getView(SocializeSession session, long id, ViewListener listener) {
		getAsync(session, ENDPOINT, String.valueOf(id), listener);
	}	
}