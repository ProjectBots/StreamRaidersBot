package include;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

public class Http {
	
	private String proxyDomain = null;
	private int proxyPort = 0;
	private String proxyUser = null;
	private String proxyPass = null;
	
	public void setProxy(String domain, int port, String username, String password) {
		proxyDomain = domain;
		proxyPort = port;
		proxyUser = username;
		proxyPass = password;
	}
	
	public static class NotAllowedProxyException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
	private static final String[] proxy_test_urls = "https://api.my-ip.io/ip.txt https://api64.ipify.org http://icanhazip.com http://ipinfo.io/ip https://www.trackip.net/ip".split(" ");
	
	public static String[] testProxy(String domain, int port, String username, String password) throws URISyntaxException, NoConnectionException, NotAllowedProxyException {
		String[] ips = new String[2];
		Http test = new Http();
		
		for(String url : proxy_test_urls) {
			test.setUrl(url);
			try {
				ips[0] = test.sendGet();
			} catch (Exception e) {
				continue;
			}
			
			test.setProxy(domain, port, username, password);
			ips[1] = test.sendGet();
								//	ip matching regex - it totally makes sense when you look at it
			if(!Pattern.matches("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$", ips[1]))
				throw new NotAllowedProxyException();
			
			return ips;
		}
		
		return null;
	}
	
	public static class NoConnectionException extends Exception {
		private static final long serialVersionUID = 1L;
		public NoConnectionException(Exception e) {
			super(e);
		}
	}
	
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
	
	public String getUrlArg(String name) {
		for(int i=0; i< urlArgs.length; i++) {
			if(urlArgs[i][0].equals(name)) return urlArgs[i][1];
		}
		return null;
	}
	
	public void addEncArg(String name, String value) {
		encArgs.add(new BasicNameValuePair(name, value));
	}
	
	public String getEncArg(String name) {
		for(NameValuePair bnvp : encArgs)
			if(bnvp.getName().equals(name))
				return bnvp.getValue();
		
		return null;
	}
	
	
	private CloseableHttpClient getClient() {
		HttpClientBuilder builder = HttpClients.custom();
		if(proxyDomain != null) {
			builder.setProxy(new HttpHost(proxyDomain, proxyPort));
			if(proxyUser != null) {
				CredentialsProvider cred = new BasicCredentialsProvider();
				cred.setCredentials(new AuthScope(proxyDomain, proxyPort), new UsernamePasswordCredentials(proxyUser, proxyPass));
				builder.setDefaultCredentialsProvider(cred);
			}
		}
		return builder.build();
	}
	
	
	public String sendGet() throws NoConnectionException, URISyntaxException {
		URIBuilder uri = new URIBuilder(this.url);
		
		HttpGet get = new HttpGet(uri.build());
		
		for(int i=0; i<headers.length; i++) {
			get.setHeader(headers[i]);
		}
		
		CloseableHttpClient client = getClient();
		
		Exception last = null;
		
		for(int i=0; i<5; i++) {
			try {
				CloseableHttpResponse response = client.execute(get);
				
				String text = new BufferedReader(new InputStreamReader(response.getEntity()
							.getContent(), StandardCharsets.UTF_8))
						.lines()
						.collect(Collectors.joining("\n"));
				
				if(text.equals(""))
					continue;
				
				client.close();
				return text;
			} catch (Exception e) {
				last = e;
			}
		}
		throw new NoConnectionException(last);
	}
	
	
	public UrlEncodedFormEntity getPayload() {
		return new UrlEncodedFormEntity(encArgs, Consts.UTF_8);
	}
	
	public String getPayloadAsString() {
		try {
			return new BufferedReader(new InputStreamReader(new UrlEncodedFormEntity(encArgs, Consts.UTF_8).getContent(), StandardCharsets.UTF_8))
					.lines()
					.collect(Collectors.joining("\n"));
		} catch (IOException e) {
			return null;
		}
	}
	
	public String sendUrlEncoded() throws URISyntaxException, NoConnectionException {
		
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
		
		CloseableHttpClient client = getClient();
		
		Exception last = null;
		for(int i=0; i<5; i++) {
			try {
				CloseableHttpResponse response = client.execute(httppost);
				
				String text = new BufferedReader(new InputStreamReader(response.getEntity()
							.getContent(), StandardCharsets.UTF_8))
						.lines()
						.collect(Collectors.joining("\n"));
				
				if(text.equals(""))
					continue;
				
				client.close();
				return text;
			} catch (Exception e) {
				last = e;
			}
		}
		throw new NoConnectionException(last);
	}
	
	private static <T>T[] add(T[] arr, T item) {
		return ArrayUtils.add(arr, item);
	}
}
