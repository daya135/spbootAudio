package org.jzz.spbootDemo.utils;
import java.io.BufferedReader;
/*
 * 为啥只修改了文件编码抓取下来的网页就能显示特殊字符了...
 * 因为读取的时候没设置编码, 默认还是按GBK解码, 另外notepad++的UTF-8解码有问题
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;  
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;  
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;  
import org.apache.http.impl.client.HttpClients;  
import org.apache.http.message.BasicNameValuePair;  
import org.apache.http.util.EntityUtils;
import org.jzz.spbootDemo.Service.SongService;
import org.jzz.spbootDemo.model.Song;
import org.jzz.spbootDemo.model.XiamiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;  

//@Component
public class XiamiCatch {
	
//	@Autowired
//	private XiamiConfig xiamiConfig;
	private static Logger logger = LoggerFactory.getLogger(XiamiCatch.class);
	
	private static String userName;
	private static String passWord;
	private static String uid;
	private static int numPerPage;
	private static String fileName;
	
	/* 这么初始化的目的是不破坏后续方法内使用的变量，保持代码可移植性 */
	static {
//		userName = xiamiConfig.getUserName();
//		passWord = xiamiConfig.getPassWord();
//		uid = xiamiConfig.getUid();
//		numPerPage = xiamiConfig.getNumPerPage();
//		fileName = xiamiConfig.getFilename();
		InputStream in = XiamiCatch.class.getResourceAsStream("/web.properties"); //神奇，不这么写就拿不到配置文件！！
		Properties prop = new Properties();
		try {
			prop.load(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {}
			}
		}
		userName = prop.getProperty("xiami_user");
		passWord = prop.getProperty("xiami_password");
		uid = prop.getProperty("xiami_uid");
		numPerPage = Integer.parseInt(prop.getProperty("numPerPage"));
		fileName = prop.getProperty("filename");
		
	}
	
//	public XiamiCatch() throws IOException {
//		/* 初始化抓取虾米网站所用的参数, 从classPath读取配置文件 */
//		InputStream inputStream = XiamiCatch.class.getClassLoader().getResourceAsStream("web.properties");
//		Properties prop = new Properties();
//		prop.load(inputStream);
//		this.userName = prop.getProperty("xiami_user");
//		this.passWord = prop.getProperty("xiami_password");
//		this.uid = prop.getProperty("xiami_uid");
//		this.numPerPage = Integer.parseInt(prop.getProperty("numPerPage"));
//	}
	
	public void saveFile(String str) {
		try {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(fileName), false), "utf-8"));
			out.write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 
	 * @description 根据虾米收藏列表，抓取歌曲名称、艺术家、上架信息和歌曲虾米链接
	 * */
	public static List<Song> CatchSongInfo() {
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		//此处不是登陆页面的地址, 而是表单提交的地址!
		HttpPost httpPost = new HttpPost("https://login.xiami.com/member/login");
		List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		List<Song> songList = new ArrayList<Song>();
		formParams.add(new BasicNameValuePair("email", userName));
		formParams.add(new BasicNameValuePair("password", passWord));
		UrlEncodedFormEntity uefEntity = null;
		
		try {
            uefEntity = new UrlEncodedFormEntity(formParams, "UTF-8");  
            httpPost.setEntity(uefEntity); 
			httpClient.execute(httpPost);
			CloseableHttpResponse response = httpClient.execute(httpPost);
			System.out.println(response.getStatusLine());
			
			httpPost = new HttpPost("http://www.xiami.com/space/lib-song/u/" + uid);
			httpPost.setEntity(uefEntity);
			response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			if (!response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
				System.out.println("登录失败! 退出");
	            try {  
	                httpClient.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }
	            return songList;
			}
			
			Pattern numPat = Pattern.compile("<span>\\(第(\\d)*页, 共(\\d)*条\\)</span>");
			Matcher matcher = numPat.matcher(EntityUtils.toString(entity));
			if (!matcher.find()) {
				System.out.println("未找到总条目数,退出!");
	            try {  
	                httpClient.close();  
	            } catch (IOException e) {  
	                e.printStackTrace();  
	            }
	            return songList;
			}
			String songNumStr = matcher.group(0);
			Integer songNum = new Integer(songNumStr.substring(songNumStr.indexOf("共") + 1, songNumStr.indexOf("条")));
			System.out.println(songNumStr + ":" +songNum );

			int pageNum = songNum / numPerPage;
			if (songNum % numPerPage != 0)
				pageNum = pageNum +1;
			for (int i = 1; i <= pageNum; i++) {
				String listUrl = "http://www.xiami.com/space/lib-song/u/" + uid + "/page/" + i;
				httpPost = new HttpPost(listUrl);
				httpPost.setEntity(uefEntity); 
				response = httpClient.execute(httpPost);
				System.out.println(listUrl);
				System.out.println(response.getStatusLine());
				entity = response.getEntity();
				String htmlContent = EntityUtils.toString(entity);
				/* 保存网页到文件再解析  */
				//saveFile(htmlContent);
				//ProcessHtml.findSongByTmpFile(fileName, songList);
				
				/* 直接从字符串解析  */
				ProcessHtml.findSongByHtmlStr(htmlContent, songList);
			}
		} catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (UnsupportedEncodingException e1) {  
            e1.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {
            try {  
                httpClient.close();
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
		return songList;
	}
	
	/**
	 *  根据虾米链接，抓取song的专辑信息。
	 *  未完成，执行一段时间后会因为不知名原因卡住。。
	 */
	public static void CatchSongAlbumInfo(List<Song> songs) {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet httpGet;
		for (int i = 0; i < songs.size();  i ++) {
			Song song = songs.get(i);
			if (song.getDownsite() != null && song.getDownsite().length() > 0) {
				if (!song.getDownsite().contains("http://www.xiami.com/")) {
					logger.info(String.format("歌曲地址不正确！：title=[%s],artist=[%s],sit=[%s]", 
							song.getTitle(), song.getArtist(), song.getDownsite()));
					continue;
				}
				httpGet = new HttpGet(song.getDownsite());
				httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build()); //禁止自动重定向，虾米的某些歌曲页面会重定向，比如http://www.xiami.com/song/xLB4rda4246
				try {
					HttpResponse response = httpClient.execute(httpGet);
					if (response.getStatusLine().toString().equals("HTTP/1.1 200 OK")) {
						String htmlContent = EntityUtils.toString(response.getEntity());
						String album = ProcessHtml.findAlbumByHtmlStr(htmlContent);
						logger.info(String.format("提取歌曲专辑信息：[%d][%s][%s][%s][%s]", 
								i, song.getTitle(), song.getArtist(),album, song.getDownsite()));
						song.setAlbum(album);
						Thread.sleep(100);
					} else {
						logger.info("获取歌曲详细信息页面出错!歌曲信息：[%d][%s][%s][%s]", i, song.getTitle(), song.getArtist(), song.getDownsite());
					}
//					Thread.sleep(50);
				} catch (Exception e) {
					logger.debug(e.getMessage());
				} 
			}
		}
        try {  
            httpClient.close();
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
	}

}
