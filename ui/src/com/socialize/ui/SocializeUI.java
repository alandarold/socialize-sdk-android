package com.socialize.ui;

import java.util.Properties;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.socialize.Socialize;
import com.socialize.SocializeService;
import com.socialize.android.ioc.IOCContainer;
import com.socialize.config.SocializeConfig;
import com.socialize.ui.comment.CommentActivity;
import com.socialize.ui.comment.CommentDetailActivity;
import com.socialize.ui.profile.ProfileActivity;
import com.socialize.util.Drawables;

public class SocializeUI {

	private static final SocializeUI instance = new SocializeUI();
	
	public static final String USER_ID = "socialize.user.id";
	public static final String COMMENT_ID = "socialize.comment.id";
	public static final String ENTITY_KEY = "socialize.entity.key";
	public static final String DEFAULT_USER_ICON = "default_user_icon.png";
	public static final String SOCIALIZE_LOGO = "socialize_logo.png";
	public static final String BG_ACCENT = "bg_accent.png";
	
	private IOCContainer container;
	private Drawables drawables;
	private final Properties customProperties = new Properties();
	
	public static final SocializeUI getInstance() {
		return instance;
	}
	
	public SocializeService getSocialize() {
		return Socialize.getSocialize();
	}
	
	public void initSocialize(Context context) {
		getSocialize().init(context, new String[]{"socialize_beans.xml", "socialize_ui_beans.xml"});
		getSocialize().getConfig().merge(customProperties);
	}
	
	public void setDrawables(Drawables drawables) {
		this.drawables = drawables;
	}

	public void initUI(IOCContainer container) {
		this.container = container;
		if(container != null) {
			drawables = container.getBean("drawables");
		}
	}
	
	public void setContainer(IOCContainer container) {
		this.container = container;
	}

	public void destroy(Context context) {
		getSocialize().destroy();
	}
	
	public View getView(String name) {
		return (View) container.getBean(name);
	}
	
	public Drawable getDrawable(String name, boolean eternal) {
		return drawables.getDrawable(name, eternal);
	}
	
	/**
	 * Sets the credentials for your Socialize App.
	 * @param consumerKey Your consumer key, obtained via registration at http://getsocialize.com
	 * @param consumerSecret Your consumer secret, obtained via registration at http://getsocialize.com
	 */
	public void setSocializeCredentials(String consumerKey, String consumerSecret) {
		customProperties.put(SocializeConfig.SOCIALIZE_CONSUMER_KEY, consumerKey);
		customProperties.put(SocializeConfig.SOCIALIZE_CONSUMER_SECRET, consumerSecret);
	}
	
	/**
	 * Sets the FB credentials for the current user if available.
	 * @param userId
	 * @param token
	 */
	public void setFacebookUserCredentials(String userId, String token) {
		customProperties.put(SocializeConfig.FACEBOOK_USER_ID, userId);
		customProperties.put(SocializeConfig.FACEBOOK_USER_TOKEN, token);
	}
	
	/**
	 * 
	 * @param debug
	 */
	public void setDebugMode(boolean debug) {
		customProperties.put(SocializeConfig.SOCIALIZE_DEBUG_MODE, String.valueOf(debug));
	}
	
	/**
	 * Sets the Facebook ID for FB authentication.  
	 * @param appId Your Facebook App Id, obtained from https://developers.facebook.com/
	 * @see https://developers.facebook.com/
	 */
	public void setFacebookAppId(String appId) {
		customProperties.put(SocializeConfig.FACEBOOK_APP_ID, appId);
	}
	
	public String getCustomConfigValue(String key) {
		
		SocializeService socialize = getSocialize();
		SocializeConfig config = socialize.getConfig();
		
		if(config != null) {
			return config.getProperty(key);
		}
		
		return null;
	}
	
	public void showCommentView(Activity context, String url) {
		Intent i = new Intent(context, CommentActivity.class);
		i.putExtra(ENTITY_KEY, url);
		context.startActivity(i);
	}
	
	public void showUserProfileView(Activity context, String userId) {
		Intent i = new Intent(context, ProfileActivity.class);
		i.putExtra(USER_ID, userId);
		context.startActivity(i);
	}
	
	public void showUserProfileViewForResult(Activity context, String userId, int requestCode) {
		Intent i = new Intent(context, ProfileActivity.class);
		i.putExtra(USER_ID, userId);
		context.startActivityForResult(i, requestCode);
	}
	
	public void showCommentDetailViewForResult(Activity context, String userId, String commentId, int requestCode) {
		Intent i = new Intent(context, CommentDetailActivity.class);
		i.putExtra(USER_ID, userId);
		i.putExtra(COMMENT_ID, commentId);
		context.startActivityForResult(i, requestCode);
	}
	
	public void setEntityUrl(Activity context, String url) {
		Intent intent = context.getIntent();
		Bundle extras = getExtras(intent);
		extras.putString(ENTITY_KEY, url);
		intent.putExtras(extras);
	}
	
	public void setUserId(Activity context, String userId) {
		Intent intent = context.getIntent();
		Bundle extras = getExtras(intent);
		extras.putString(USER_ID, userId);
		intent.putExtras(extras);
	}
	
	protected Bundle getExtras(Intent intent) {
		Bundle extras = intent.getExtras();
		if(extras == null) {
			extras = new Bundle();
		}	
		return extras;
	}
	
	public Properties getCustomProperties() {
		return customProperties;
	}
}
