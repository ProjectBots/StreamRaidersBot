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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

public class Http {
	
	
	private String url = "";
	private BasicHeader[] headers = new BasicHeader[0];
	private String[][] urlArgs = new String[0][0];
	private List<NameValuePair> encArgs = new ArrayList<NameValuePair>();
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void addHeader(String name, String value) {
		headers = add(headers, new BasicHeader(name, value));
	}
	
	public void addUrlArg(String name, String value) {
		urlArgs = add(urlArgs, new String[] {name, value});
	}
	
	public void addEncArg(String name, String value) {
		encArgs.add(new BasicNameValuePair(name, value));
	}

	
	
	public String sendGet() throws Exception {
		
		URIBuilder uri = new URIBuilder(this.url);
		
		HttpGet get = new HttpGet(uri.build());
		
		for(int i=0; i<headers.length; i++) {
			get.setHeader(headers[i]);
		}
		
		
		CloseableHttpClient client = HttpClients.createDefault();
		CloseableHttpResponse response = client.execute(get);
		
		String text = new BufferedReader(new InputStreamReader(response.getEntity()
					.getContent(), StandardCharsets.UTF_8))
				.lines()
				.collect(Collectors.joining("\n"));
		
		client.close();
		return text;	
		
	}
	
	
	
	
	public String sendUrlEncoded() throws Exception {
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(encArgs, Consts.UTF_8);
		
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
		
		String text = new BufferedReader(new InputStreamReader(response.getEntity()
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
