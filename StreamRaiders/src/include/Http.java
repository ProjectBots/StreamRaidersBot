package include;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

public class Http {
	
	
	private String url = "";
	private BasicHeader[] headers = new BasicHeader[0];
	private String[][] urlArgs = new String[0][0];
	
	private int max_attemps = 3;
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	private String getUrl() {
		return url;
	}

	
	public void addHeader(String name, String value) {
		headers = add(headers, new BasicHeader(name, value));
	}
	
	private BasicHeader[] getHeaders() {
		return headers;
	}
	
	public void addUrlArg(String name, String value) {
		urlArgs = add(urlArgs, new String[] {name, value});
	}

	
	
	public String sendGet() throws Exception {
		
		for(int j=0; j<max_attemps; j++) {
			try {
				Request req = Request.Get(this.getUrl());
				
				BasicHeader[] headers = this.getHeaders();
				
				for(int i=0; i<headers.length; i++) {
					req.setHeader(headers[i]);
				}
				
				return req.execute().returnContent().asString();
			} catch (Exception e) {}
		}
		
		return null;
	}
	
	/*
	public void sendPost(String... forms) throws Exception {
		
		Request req = Request.Post(this.getUrl());
		
		BasicHeader[] headers = this.getHeaders();
		
		for(int i=0; i<headers.length; i++) {
			req.setHeader(headers[i]);
		}
		
		
		Form form = Form.form();
		
		for(int i=0; i<forms.length-1; i+=2) {
			form.add(forms[i], forms[i+1]);
		}
		
		
		
		req.bodyForm(form.build());
		
		HttpResponse res = req.execute().returnResponse();
		System.out.println("code= " + res.getStatusLine().getStatusCode());
		
	}
	*/
	
	
	public String sendJson(String json, boolean getResponse) throws Exception {
		
		
		HttpPost httpPost = new HttpPost(this.url);

		httpPost.setHeader("Accept", "application/json");
		httpPost.setHeader("Content-type", "application/json");

		for(int i=0; i<headers.length; i++) {
			httpPost.setHeader(headers[i]);
		}
		
		StringEntity entity = new StringEntity(json);
		httpPost.setEntity(entity);
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httpPost);
		
		String text = null;
		
		if(getResponse) {
			text = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent(), StandardCharsets.UTF_8))
					.lines().collect(Collectors.joining("\n"));
		}
		
		client.close();
		return text;
	}
	
	public String sendJson(String json) throws Exception {
		return sendJson(json, true);
	}
	
	
	
	
	public String sendUrlEncoded(String... params) throws Exception {
		
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		
		for(int i=0; i<params.length-1; i+=2) {
			formparams.add(new BasicNameValuePair(params[i], params[i+1]));
		}
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		
		URIBuilder uri = new URIBuilder(this.url);
		
		for(int i=0; i<urlArgs.length; i++) {
			uri.setParameter(urlArgs[i][0], urlArgs[i][1]);
		}
		
		HttpPost httppost = new HttpPost(uri.build());
		

		for(int i=0; i<headers.length; i++) {
			httppost.setHeader(headers[i]);
		}
		
		httppost.setEntity(entity);
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(httppost);
		
		String text = null;
		
		text = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
		
		client.close();
		return text;
		
	}
	
	private BasicHeader[] add(BasicHeader[] arr, BasicHeader item) {
		BasicHeader[] arr2 = new BasicHeader[arr.length + 1];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
	
	private String[][] add(String[][] arr, String[] item) {
		String[][] arr2 = new String[arr.length + 1][];
		System.arraycopy(arr, 0, arr2, 0, arr.length);
		arr2[arr.length] = item;
		return arr2;
	}
}
