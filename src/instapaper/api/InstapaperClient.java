package instapaper.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class InstapaperClient {

	private OAuthConsumer _consumer;
	private HttpClient _client;
	private static String _ACCESS_TOKEN_URL = "https://www.instapaper.com/api/1/oauth/access_token";
	private static String _VERIFY_CREDENTIALS_URL = "https://www.instapaper.com/api/1/account/verify_credentials";
	private static String _UPDATE_READ_PROCESS = "https://www.instapaper.com/api/1/bookmarks/update_read_progress";
	private static String _BOOKMARK_LIST_URL = "https://www.instapaper.com/api/1/bookmarks/list";
	private static String _BOOKMARK_ADD = "https://www.instapaper.com/api/1/bookmarks/add";
	private static String _BOOKMARK_DEL = "https://www.instapaper.com/api/1/bookmarks/delete";
	private static String _BOOKMARK_STAR = "https://www.instapaper.com/api/1/bookmarks/star";
	private static String _BOOKMARK_UNSTAR = "https://www.instapaper.com/api/1/bookmarks/unstar";
	private static String _BOOKMARK_ARCHIVE = "https://www.instapaper.com/api/1/bookmarks/archive";
	private static String _BOOKMARK_UNARCHIVE = "https://www.instapaper.com/api/1/bookmarks/unarchive";
	private static String _BOOKMARK_MOVE = "https://www.instapaper.com/api/1/bookmarks/move";
	private static String _GET_TEXT = "https://www.instapaper.com/api/1/bookmarks/get_text";
	private static String _FOLDERS_LIST = "https://www.instapaper.com/api/1/folders/list";
	private static String _FOLDERS_ADD = "https://www.instapaper.com/api/1/folders/add";
	private static String _FOLDERS_DEL = "https://www.instapaper.com//api/1/folders/delete";
	
	public InstapaperClient(String key, String secret) {
		_consumer = new CommonsHttpOAuthConsumer(key, secret);
		_client = new DefaultHttpClient();
	}
	
	public synchronized void getAccessToken(String username, String password) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
        		new BasicNameValuePair("x_auth_username", username), 
        		new BasicNameValuePair("x_auth_password", password), 
        		new BasicNameValuePair("x_auth_mode", "client_auth"));
		
        HttpResponse response = null;
        String responseString = null;
        HttpPost request = new HttpPost(_ACCESS_TOKEN_URL);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			InstaMessage respMsg =  MessageParser.getToken(responseString);
			if (respMsg instanceof TokenInstaMessage)
				_consumer.setTokenWithSecret(((TokenInstaMessage) respMsg).getToken(), ((TokenInstaMessage)respMsg).getSecret());
			else if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			//TODO: else throw unknown message exception
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public synchronized CredentialsInstaMessage getCredentials() throws ApiException {				
        HttpResponse response = null;
        String responseString = null;
        HttpPost request = new HttpPost(_VERIFY_CREDENTIALS_URL);
        InstaMessage respMsg = null;
        try {
        	_consumer.sign(request);
        	response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg = MessageParser.getCredentials(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
        	
        } catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
        return (CredentialsInstaMessage) respMsg;
	}
	
	public synchronized BookmarkInstaMessage updateReadProcess(int bookmarkId, float progress, int progressTimeStamp) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
        		new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId)), 
        		new BasicNameValuePair("progress", String.valueOf(progress)), 
        		new BasicNameValuePair("progress_timestamp", String.valueOf(progressTimeStamp)));
		
        HttpResponse response = null;
        String responseString = null;
        InstaMessage respMsg = null;
        HttpPost request = new HttpPost(_UPDATE_READ_PROCESS);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			//TODO: else throw unknown message exception
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (BookmarkInstaMessage) respMsg;
	}
	
	public synchronized BookmarkListInstaMessage getBookmarkList(int limit, String folderId, String have) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
        		new BasicNameValuePair("limit", String.valueOf(limit)), 
        		new BasicNameValuePair("folder_id", folderId), 
        		new BasicNameValuePair("have", have));
		
        HttpResponse response = null;
        String responseString = null;
        InstaMessage respMsg = null;
        HttpPost request = new HttpPost(_BOOKMARK_LIST_URL);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			response.getEntity().consumeContent();
			respMsg =  MessageParser.getBookmarkList(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			//TODO: else throw unknown message exception
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (BookmarkListInstaMessage) respMsg;
	}

	public synchronized BookmarkInstaMessage addBookmark(String url, String title, String description, int folderId, int isNotFinalUrl) throws ApiException{
		List<BasicNameValuePair> params = null;  
		if (folderId > 0 )
			params = Arrays.asList(
        		new BasicNameValuePair("url", url),
        		new BasicNameValuePair("title", title),
        		new BasicNameValuePair("description", description),
        		new BasicNameValuePair("folder_id", String.valueOf(folderId)),
        		new BasicNameValuePair("resolve_final_url", String.valueOf(isNotFinalUrl))
   				);
		else 
			params = Arrays.asList(
        		new BasicNameValuePair("url", url),
        		new BasicNameValuePair("title", title),
        		new BasicNameValuePair("description", description),	
        		new BasicNameValuePair("resolve_final_url", String.valueOf(isNotFinalUrl))
   				);
		
		
        HttpResponse response = null;
        String responseString = null;
        InstaMessage respMsg = null;
        HttpPost request = new HttpPost(_BOOKMARK_ADD);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			//TODO: else throw unknown message exception
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (BookmarkInstaMessage) respMsg;
	}

	public synchronized boolean delBookmark(int bookmarkId){
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_DEL);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getResult(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				return false;
			   
		} catch (OAuthMessageSignerException e) {
			return false;	
		} catch (OAuthExpectationFailedException e) {
			return false;
		} catch (OAuthCommunicationException e) {
			return false;
		} catch (UnsupportedEncodingException e) {
			return false;
		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		} 	
		return true;
	}
	
	public synchronized BookmarkInstaMessage starBookmark(int bookmarkId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_STAR);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			throw new ApiException("0",0);
		} catch (OAuthExpectationFailedException e) {
			throw new ApiException("0",0);
		} catch (OAuthCommunicationException e) {
			throw new ApiException("0",0);
		} catch (UnsupportedEncodingException e) {
			throw new ApiException("0",0);
		} catch (ClientProtocolException e) {
			throw new ApiException("0",0);
		} catch (IOException e) {
			throw new ApiException("0",0);
		} 	
		return (BookmarkInstaMessage) respMsg;
	}
	
	public synchronized BookmarkInstaMessage unstarBookmark(int bookmarkId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_UNSTAR);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			throw new ApiException("0",0);
		} catch (OAuthExpectationFailedException e) {
			throw new ApiException("0",0);
		} catch (OAuthCommunicationException e) {
			throw new ApiException("0",0);
		} catch (UnsupportedEncodingException e) {
			throw new ApiException("0",0);
		} catch (ClientProtocolException e) {
			throw new ApiException("0",0);
		} catch (IOException e) {
			throw new ApiException("0",0);
		} 	
		return (BookmarkInstaMessage) respMsg;
	}
	
	public synchronized BookmarkInstaMessage archiveBookmark(int bookmarkId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_ARCHIVE);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			throw new ApiException("0",0);
		} catch (OAuthExpectationFailedException e) {
			throw new ApiException("0",0);
		} catch (OAuthCommunicationException e) {
			throw new ApiException("0",0);
		} catch (UnsupportedEncodingException e) {
			throw new ApiException("0",0);
		} catch (ClientProtocolException e) {
			throw new ApiException("0",0);
		} catch (IOException e) {
			throw new ApiException("0",0);
		} 	
		return (BookmarkInstaMessage) respMsg;
	}
	
	public synchronized BookmarkInstaMessage unarchiveBookmark(int bookmarkId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_UNARCHIVE);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			throw new ApiException("0",0);
		} catch (OAuthExpectationFailedException e) {
			throw new ApiException("0",0);
		} catch (OAuthCommunicationException e) {
			throw new ApiException("0",0);
		} catch (UnsupportedEncodingException e) {
			throw new ApiException("0",0);
		} catch (ClientProtocolException e) {
			throw new ApiException("0",0);
		} catch (IOException e) {
			throw new ApiException("0",0);
		} 	
		return (BookmarkInstaMessage) respMsg;
	}
	
	public synchronized BookmarkInstaMessage moveBookmark(int bookmarkId, int folderId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId)),
	    			new BasicNameValuePair("folder_id", String.valueOf(folderId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_BOOKMARK_MOVE);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getBookmark(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (BookmarkInstaMessage) respMsg;
	}

	public synchronized BookmarkTextInstaMessage getBookmarkText(int bookmarkId) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("bookmark_id", String.valueOf(bookmarkId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_GET_TEXT);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			response.getEntity().consumeContent();
			respMsg =  MessageParser.getBookmarkText(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (BookmarkTextInstaMessage) respMsg;
	}

	public synchronized FoldersListInstaMessage getFoldersList() throws ApiException{

		
        HttpResponse response = null;
        String responseString = null;
        InstaMessage respMsg = null;
        HttpPost request = new HttpPost(_FOLDERS_LIST);
		try {
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getFoldersList(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (FoldersListInstaMessage) respMsg;
	}
	
	public synchronized FolderInstaMessage addFolder(String title) throws ApiException{
		List<BasicNameValuePair> params = Arrays.asList(
    			new BasicNameValuePair("title", title)
				);
	
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_FOLDERS_ADD);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getFolder(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				throw new ApiException((ErrorInstaMessage) respMsg);
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return (FolderInstaMessage) respMsg;
	}

	public synchronized boolean delFolder(int folderId){
		List<BasicNameValuePair> params = Arrays.asList(
	    			new BasicNameValuePair("folder_id", String.valueOf(folderId))
					);
		
	    HttpResponse response = null;
	    String responseString = null;
	    InstaMessage respMsg = null;
	    HttpPost request = new HttpPost(_FOLDERS_DEL);
		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			request.setEntity(entity);
			_consumer.sign(request);
			response = _client.execute(request);
			responseString = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
			respMsg =  MessageParser.getResult(responseString);
			if (respMsg instanceof ErrorInstaMessage)
				return false;
			   
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();	
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	
		return true;
	}
}