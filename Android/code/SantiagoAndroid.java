package au.com.jobinhood.android.app.core;

import java.util.Date;

import org.scribe.model.OAuthConstants;
import org.scribe.model.Request;
import org.scribe.model.Verb;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import au.com.jobinhood.android.app.R;
import au.com.jobinhood.android.app.activity.LoginActivity;
import au.com.jobinhood.android.app.activity.SignupActivity;
import au.com.jobinhood.android.app.activity.MainActivity;
import au.com.jobinhood.android.app.api.JbhResponse;
import au.com.jobinhood.android.app.api.JobinhoodApi;
import au.com.jobinhood.android.app.api.task.ProgressRequestTask;
import au.com.jobinhood.android.app.api.task.RequestTask;
import au.com.jobinhood.android.app.modal.User;
import au.com.jobinhood.android.app.util.JbhValidator;

/**
 * Core class for the android app.
 * This class dictates 
 *  - calls to API (HTTP requests)
 *  - Persisting to db (ORMLite functionalities).
 *  - control of the conversationList
 *
 */
public class SantiagoAndroid {
	
	private static SantiagoAndroid core;
	private ConversationList conversationList;
	
	String jbhTerms;
	String jbhPriv;
	
	private SantiagoAndroid() {
		jbhTerms = null;
		jbhPriv = null;
	}
	
	public static SantiagoAndroid get() {
		if (core == null)
			core = new SantiagoAndroid();
		return core;
	}

	public ConversationList getConversationList() {
		return conversationList;
	}
	
	public void loadConversations() {
		if (conversationList == null)
			conversationList = new ConversationList();

		//Try to buildConversations from DB
		
		
		//Try to retrieve Conversations from api
		tryThreadsRequest(null);
		
	}
	
	public void onResponse(JbhResponse response) {
		int responseType = response.getType();
		
		switch (responseType) {
			case JbhResponse.RESPONSE_LOGIN:
				Log.d("santiago", "login response");
				onLoginResponse(response);
				break;
			case JbhResponse.RESPONSE_RECOVER:
				Log.d("santiago", "recovery response");
				onRecoverResponse();
				break;
			case JbhResponse.RESPONSE_USER:
				Log.d("santiago", "auth_user response");
				onUserResponse(response);
				break;
			case JbhResponse.RESPONSE_THREADS:
				Log.d("santiago", "threads response");
				onThreadsResponse(response);
				break;
			case JbhResponse.PRIVACY:
			case JbhResponse.TERMS_AND_CONDITIONS:
				onHtmlResponse(response);
				break;
				
		}
	}
	
	/**
	 * This section handles Threads Request/Response;
	 */
	
	public void onThreadsResponse(JbhResponse response) {
		JsonArray conversations = new JsonParser().parse(response.getBody()).getAsJsonArray();
		
		//TODO
		Log.d("santiago", "converting conversation");
		for (JsonElement ele : conversations ) {
			conversationList.addConversationFromJson(ele.getAsJsonObject());
		}
	}
	
	public void tryThreadsRequest(Date lastUpdate) {
		String url = JobinhoodApi.domain + JobinhoodApi.api_threads;

		Request request = buildThreadsRequest(url, lastUpdate);
		RequestTask task = new RequestTask(JbhResponse.RESPONSE_THREADS);
		task.execute(request);
	}
	
	private Request buildThreadsRequest(String url, Date lastUpdate) {
		//build request
		Request request = new Request(Verb.GET, url);
		User.get().authorizeRequest(request);
		
		if (lastUpdate != null)
			request.addQuerystringParameter("activeAfter", lastUpdate.toString());
		
		return request;
	}
	
	/**
	 * This section of methods will build the Auth-User
	 */
	
	public void onUserResponse(JbhResponse response) {
		User user = User.get();
		JsonObject jUser = new JsonParser().parse(response.getBody()).getAsJsonObject();
		//id
		user.setId(jUser.get("id").getAsInt());
		//picture
		if (jUser.get("picture") != null)
			user.setUrl(parseJsonPicture(jUser.get("picture").getAsJsonObject()));
		//age
		if (jUser.get("age") != null)
			user.setAge(jUser.get("age").getAsInt());
		//fname
		if (jUser.get("firstName") != null)
			user.setFirstname(jUser.get("firstName").getAsString());
		//lname
		if (jUser.get("lastName") != null)
			user.setLastname(jUser.get("lastName").getAsString());
		//gender
		if (jUser.get("gender") != null)
			user.setNotMale(jUser.get("gender").getAsString().equalsIgnoreCase(User.FEMALE));
		//phone
		if (jUser.get("phoneNumber") != null)
			user.setPhone(jUser.get("phoneNumber").getAsString());
		if (jUser.get("address") != null)
			user.setAddress(parseJsonAddress(jUser.get("address").getAsJsonObject()));
		
	}
	
	private String parseJsonPicture(JsonObject asJsonObject) {
		// TODO Auto-generated method stub
		return null;
	}

	private String parseJsonAddress(JsonObject jAddress) {
		String address = "";
		if (jAddress.get("suburb") != null)
			address += jAddress.get("suburb").getAsString() + User.WHITESPACE;
		if (jAddress.get("state") != null)
			address += jAddress.get("state").getAsString() + User.WHITESPACE;
		
		return address;
	}

	public void tryUserRequest(Context context) {
		String url = JobinhoodApi.domain + JobinhoodApi.api_user;
		
		//build request
		Request request = buildUserRequest(url, User.get().getToken());
		
		//execute request
		RequestTask task = new RequestTask(JbhResponse.RESPONSE_USER);
		task.execute(request);
		
	}
	
	private Request buildUserRequest(String url, String token) {
		Request request = new Request(Verb.GET, url);
		User.get().authorizeRequest(request);
		
		return request;
	}
	
	/**
	 * This section of methods provide pass-recovery-api functionalities.
	 * 
	 */
	
	public void onRecoverResponse() {
		
		//load confirmation > redirect to splash on confirmation.
		
	}
	
	public void tryRecoverPassword(Context context, String email) {
		String url =  JobinhoodApi.domain + JobinhoodApi.api_recover;
		String label = context.getResources().getString(R.string.dialog_recover);
		
		//check email
		if(JbhValidator.validateEmail(email)) {
			
		}
		
		//build request
		Request request = buildRecoverRequest(url, email);
		
		//execute request
		RequestTask task = new ProgressRequestTask(context, label, JbhResponse.RESPONSE_RECOVER);
		task.execute(request);
	}
	
	private Request buildRecoverRequest(String url, String email) {
		Request request = new Request(Verb.POST, url);
		request.addBodyParameter("email", email);
		
		return request;
	}
	
	/**
	 * This section of methods provide login-api functionalities.
	 * void onLoginSuccess
	 * void tryLogin
	 * Request buildLoginRequest
	 * 
	 */
	
	public void onLoginResponse(JbhResponse response) {
		JsonObject jsonObject = new JsonParser().parse(response.getBody()).getAsJsonObject();
		String token = jsonObject.get("access_token").getAsString();

		User.get().setToken(token);
		
		Context context = response.getContext();
		if (context instanceof LoginActivity) {
			Log.d("santiago", "login response, splash activity");
			((LoginActivity) context).onLogin();
		}
		
	}
	
	public void tryLogin(Context c, String username, String password) {
		String url = JobinhoodApi.domain + JobinhoodApi.api_login;
		String label = c.getResources().getString(R.string.dialog_login);
				
		//check email
		if(JbhValidator.validateEmail(username)) {
			
		}
		
		//check password
		
		//build request
		Request request = buildLoginRequest(url, username, password);
		
		//execute async-request
		RequestTask task = new ProgressRequestTask(c, label, JbhResponse.RESPONSE_LOGIN);
	    	task.execute(request);
	    
	}
	
	private Request buildLoginRequest(String url, String username, String password) {
		Request request = new Request(Verb.POST, url);
		request.addBodyParameter(OAuthConstants.CLIENT_ID, JobinhoodApi.client_id);
	    	request.addBodyParameter(OAuthConstants.CLIENT_SECRET, JobinhoodApi.client_secret);
	    	request.addBodyParameter("grant_type", "password");
	    	request.addBodyParameter("username", username);
	    	request.addBodyParameter("password", password);
	    
	    	return request;
	    
	}

	/**
	 * Sign up methods section - TODO complete this
	 */
	
	public void trySignup(Context context, String fname, String username,
			String password, String type) 
	{
		String url = JobinhoodApi.domain + JobinhoodApi.api_signup;
		String label = context.getResources().getString(R.string.dialog_signup);
				
		//check email
		if(!JbhValidator.validateEmail(username)) {
			
		}
		
		//check password valid
		
		//build request
		Request request = buildSignupRequest(url, fname, username, password, type);
		
		//execute async-request
		RequestTask task = new ProgressRequestTask(context, label, JbhResponse.RESPONSE_SIGNUP);
	    	task.execute(request);
	}
	
	private Request buildSignupRequest(String url, String fname, String uname, String pass, String type) 
	{
		Request request = new Request(Verb.POST, url);
		request.addBodyParameter("userType", type);
	    	request.addBodyParameter("firstName", fname);
	    	request.addBodyParameter("email", uname);
	    	request.addBodyParameter("plainPassword", pass);
	    
	    	return request;
	}
	
	/**
	 * tc and priv methods
	 */
	public void tryRequestHtml(Context context, String url, int type) {
		
		if (type == JbhResponse.TERMS_AND_CONDITIONS && jbhTerms != null) {
			((SignupActivity) context).showHtmlDialog(jbhTerms);
			return;
		}
		else if (type == JbhResponse.PRIVACY && jbhPriv != null) {
			((SignupActivity) context).showHtmlDialog(jbhPriv);
			return;
		}
		
		String label = context.getResources().getString(R.string.dialog_generic);
		Request request = new Request(Verb.GET, url);
		RequestTask task = new ProgressRequestTask(context, label, type);
	    	task.execute(request);
	    
	}
	
	public void onHtmlResponse(JbhResponse res) {
		String html = new JsonParser().parse(res.getBody()).getAsJsonObject().get("html").getAsString();
		
		if (res.getType() == JbhResponse.PRIVACY)
			jbhPriv = html;
		else if (res.getType() == JbhResponse.TERMS_AND_CONDITIONS)
			jbhTerms = html;
		
		Context con = res.getContext();
		if (con instanceof SignupActivity)
			((SignupActivity) con).showHtmlDialog(html);
		
	}
	
	public String getResString(Context c, int id) {
		return c.getResources().getString(id);
	}

	
	
}
