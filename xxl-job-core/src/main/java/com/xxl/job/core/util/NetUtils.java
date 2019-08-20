package com.xxl.job.core.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.PasswordAuthentication;
import java.net.SocketException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import io.netty.handler.codec.http.HttpHeaders.Names;

/**
 * 项目: SF_Common
 * 描述: HTTP|HTTPS请求
 * 作者: zhangyi183790
 * 时间: 2019年3月20日 下午1:52:54
 * 版本: v1.0
 * JDK: 1.8
 */
public class NetUtils {
	private static Logger logger = LoggerFactory.getLogger(NetUtils.class);
	/**
	 * HTTP协议:http://
	 */
	public final static String PROTOCOL_HTTP = "http://";
	/**
	 * HTTPS协议:https://
	 */
	public final static String PROTOCOL_HTTPS = "https://";
	/**
	 * Get请求
	 */
	public final static String METHOD_GET = "GET";
	/**
	 * Post请求
	 */
	public final static String METHOD_POST = "POST";
	/**
	 * Head请求
	 */
	public final static String METHOD_HEAD = "HEAD";
	/**
	 * Options请求
	 */
	public final static String METHOD_OPTIONS = "OPTIONS";
	/**
	 * Put请求
	 */
	public final static String METHOD_PUT = "PUT";
	/**
	 * Delete请求
	 */
	public final static String METHOD_DELETE = "DELETE";
	/**
	 * Trace请求
	 */
	public final static String METHOD_TRACE = "TRACE";
	
	/**
	 * @param proxyHost 代理地址
	 * @param port      代理端口
	 * @param account   认证账号
	 * @param password  认证密码
	 */
	public static void auth(String proxyHost, int port, final String account, final String password) {
		System.setProperty("https.proxyHost", proxyHost);
		System.setProperty("https.proxyPort", port + "");
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(account, new String(password).toCharArray());
			}
		});
	}

	/**
	 * 描述: 判断服务连通性
	 * 作者: ZhangYi
	 * 时间: 2019年3月20日 下午1:53:53
	 * 参数: (参数列表)
	 * 
	 * @param url  请求URL
	 * @param auth 认证信息(username+":"+password)
	 * @return (true:连接成功,false:连接失败)
	 */
	public static boolean checkConnection(String url, String auth) {
		boolean flag = false;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(5 * 1000);
			if (auth != null && !"".equals(auth)) {
				String authorization = "Basic " + new String(Base64Utils.encodeToString(auth.getBytes()));
				connection.setRequestProperty("Authorization", authorization);
			}
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				flag = true;
			}
			connection.disconnect();
		} catch (Exception e) {
			logger.error("--Server Connect Error !", e);
		}
		return flag;
	}

	/**
	 * @param url    请求URL
	 * @param method 请求URL
	 * @param param  json参数(post|put)
	 * @param auth   认证(username+:+password)
	 * @return 返回结果
	 */
	public static String urlRequest(String url, String method, String param, String auth) {
		return urlRequest(url, method, param, null, auth);
	}

	/**
	 * @param url    请求URL
	 * @param method 请求URL
	 * @param param  json参数(post|put)
	 * @param auth   认证(username+:+password)
	 * @return 返回结果
	 */
	public static String urlRequest(String url, String method, String param, Map<String, String> headers, String auth) {
		String result = null;
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setConnectTimeout(60 * 1000);
			connection.setRequestMethod(method.toUpperCase());
			if (auth != null && !"".equals(auth)) {
				String authorization = "Basic " + new String(Base64Utils.encodeToString(auth.getBytes()));
				connection.setRequestProperty(Names.AUTHORIZATION, authorization);
			}
			String contentType = null;
			String charset = null;
			if (headers != null && !headers.isEmpty()) {
				for (Entry<String, String> entity : headers.entrySet()) {
					String key = entity.getKey();
					if (StringUtils.isEmpty(key)) {
						continue;
					}
					String value = entity.getValue();
					if (key.equalsIgnoreCase(Names.CONTENT_TYPE) || key.equalsIgnoreCase("contentType")) {
						contentType = value;
						continue;
					}
					if (key.equalsIgnoreCase(Names.CONTENT_ENCODING) || key.equalsIgnoreCase("contentEncoding")) {
						charset = value;
						continue;
					}
					connection.setRequestProperty(key, value);
				}
			}
			if (!StringUtils.isEmpty(contentType)) {
				connection.setRequestProperty(Names.CONTENT_TYPE, contentType);
			}
			if (!StringUtils.isEmpty(charset) && Charset.isSupported(charset)) {
				connection.setRequestProperty(Names.CONTENT_ENCODING, charset);
			}
			connection.setRequestProperty(Names.CONNECTION, "close");
			if (param != null && !"".equals(param)) {
				connection.setDoInput(true);
				connection.setDoOutput(true);
				connection.connect();
				DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
				dos.write(param.getBytes("UTF-8"));
				dos.flush();
				dos.close();
			} else {
				connection.connect();
			}
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK || connection.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
				InputStream in = connection.getInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = in.read(buff, 0, buff.length)) > 0) {
					out.write(buff, 0, len);
				}
				byte[] data = out.toByteArray();
				in.close();
				result = data != null && data.length > 0 ? new String(data, "UTF-8") : null;
			} else {
				result = "{\"statuscode\":" + connection.getResponseCode() + ",\"message\":\"" + connection.getResponseMessage() + "\"}";
			}
			connection.disconnect();
		} catch (Exception e) {
			logger.error("--http request error {url:"+url+",method:"+method+",params:"+param+"}!", e);
		}
		return result;
	}
	/**
	 * 描述: URL编码
	 * @author yi.zhang
	 * 时间: 2017年9月15日 下午3:33:38
	 * @param target 目标字符串
	 * @return
	 */
	public static String encode(String target) {
		String result = target;
		try {
			result = URLEncoder.encode(target, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("--http encode error !", e);
		}
		return result;
	}

	/**
	 * 描述: URL解码
	 * @author yi.zhang
	 * 时间: 2017年9月15日 下午3:33:38
	 * @param target 目标字符串
	 * @return
	 */
	public static String decode(String target) {
		String result = target;
		try {
			result = URLDecoder.decode(target, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("--http decode error !", e);
		}
		return result;
	}

	public static final Pattern pattern = Pattern.compile("[^\\x00-\\xff]");

	/**
	 * 描述: 匹配双字节字符（汉字、中文标点符号等）
	 * 作者: ZhangYi
	 * 时间: 2019年3月19日 下午2:22:50
	 * 参数: (参数列表)
	 * 
	 * @param target
	 * @return
	 */
	public static boolean matchChinese(String target) {
		if (StringUtils.isEmpty(target)) {
			return false;
		}
		return pattern.matcher(target).find();
	}

	/**
	 * 描述: 获取本地IP
	 * 
	 * @author ZhangYi
	 * @date 2019-06-20 14:43:34
	 * @return
	 * @throws SocketException
	 */
	public static String getLocalHost() throws SocketException {
		String local = "127.0.0.1";
		Map<String, NetworkInterface> networks = networks();
		for (Map.Entry<String, NetworkInterface> entry : networks.entrySet()) {
			String host = entry.getKey();
			NetworkInterface network = entry.getValue();
			// 网卡名称
			String netname = System.getProperty("network.name");
			if(StringUtils.isEmpty(netname)) {
			    netname = System.getenv("network.name");
			}
			if(StringUtils.isEmpty(netname)) {
			    netname = System.getProperty("NETWORK_NAME");
			}
			if(StringUtils.isEmpty(netname)) {
			    netname = System.getenv("NETWORK_NAME");
			}
			logger.info("==>>>netname:{},host:{},name:{},displayName:{}",netname,host,network.getName(),network.getDisplayName());
			if(!StringUtils.isEmpty(netname)&&(netname.equalsIgnoreCase(network.getName())||netname.equalsIgnoreCase(network.getDisplayName()))) {
			    local = host;
			    logger.info("++>>>netname:{},host:{},name:{},displayName:{}",netname,host,network.getName(),network.getDisplayName());
			    break;
			}
			if (network.isLoopback() || network.isVirtual()) {
				continue;
			}
			String[] splits = host.split("\\.");
			if (splits.length != 4) {
				continue;
			}
			if (!(host.startsWith("192.168") || host.startsWith("172.") && Integer.valueOf(splits[1]) >= 16 && Integer.valueOf(splits[1]) <= 31 || host.startsWith("10.") && Integer.valueOf(splits[1]) >= 0 && Integer.valueOf(splits[1]) <= 255)) {
				continue;
			}
			if (host.endsWith(".0") || host.endsWith(".1") || host.endsWith(".255") || host.endsWith(".254")) {
				continue;
			}
			if (!StringUtils.isEmpty(network.getDisplayName()) && network.getDisplayName().toLowerCase().contains("loopback")) {
				continue;
			}
			local = host;
		}
		return local;
	}
	/**
	 * 
	 * 描述: 获取主机所有地址对应网卡信息
	 * 
	 * @author ZhangYi
	 * @date 2019-06-20 14:44:07
	 * @return
	 */
	public static Map<String, NetworkInterface> networks() {
		Map<String, NetworkInterface> networks = new HashMap<String, NetworkInterface>();
		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			NetworkInterface network;
			Enumeration<InetAddress> inetAddresses;
			InetAddress inetAddress;
			String host;
			while (networkInterfaces.hasMoreElements()) {
			    network = networkInterfaces.nextElement();
				inetAddresses = network.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					inetAddress = inetAddresses.nextElement();
					if (inetAddress instanceof Inet4Address) { // IPV4
					    host = inetAddress.getHostAddress();
						networks.put(host, network);
						logger.info("host:{},name:{},displayName:{},Loopback:{},PointToPoint:{},Up:{},Virtual:{},Index:{},MTU:{}",host,network.getName(),network.getDisplayName(),network.isLoopback(),network.isPointToPoint(),network.isUp(),network.isVirtual(),network.getIndex(),network.getMTU());
					}
				}
			}
		} catch (SocketException e) {
			logger.error("--Get networks Error!",e);
		}
		return networks;
	}

	public static void main(String[] args) {
		String id = "";
//		String url = "http://172.21.32.31:8172/hollysys-eam/realtime/read-tags";
		String url = "http://172.21.32.31:8172/hollysys-eam/realtime/write-tags";
		if (!"".equals(id)) {
			url = url + "/" + id;
		} else {
//			url=url+"/_search";
		}

		String method = "get";
//		String body = "[\"dbs-test#cd1\",\"dbs-test#cd3\"]";
		String body = "[\r\n" + "  {\r\n" + "    \"tag\": \"A_3\",\r\n" + "    \"type\": 0,\r\n" + "    \"value\": \"1\"\r\n" + "  }\r\n" + "]";
//		String body = "{\"name\":\"mobile music\",\"operator\":\"10000\",\"content\":\"I like music!\",\"createTime\":\"2017-04-20\"}";
		String result = null;
//		String auth="elastic:elastic";
//		result = checkConnection("http://127.0.0.1:9200",null)+"";
//		System.out.println(result);
		System.out.println("---------------------------------------------------------");
//		result = urlRequest(url, method, param);
		System.out.println(result);
	}
}
