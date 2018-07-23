package org.jzz.spbootDemo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.runner.RunWith;
import org.jzz.spbootDemo.model.Song;
import org.jzz.spbootDemo.utils.LocalFile;
import org.jzz.spbootDemo.utils.MP3Analysis;
import org.jzz.spbootDemo.utils.MP3Analysis2;
import org.jzz.spbootDemo.utils.ProcessHtml;
import org.jzz.spbootDemo.utils.XiamiCatch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.*;
import java.nio.charset.Charset;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestUtils {
	
	public static void printList(List<Song> songList) {
		int n = 0;
		int pageCount = 1;
		for (Song song : songList) {
			if (n%25 == 0) {
				System.out.println("-------" + pageCount + "-------");
				pageCount ++;
				n = 0;
			}
			System.out.println(song);
			n++;
		}
		System.out.println("-------" + songList.size() + "-------");
	}
	
	public static String ByteToString(byte[] bytes) {
		StringBuffer strBuffer = new StringBuffer();
		for (byte b : bytes) {
			String bString = Integer.toHexString(0xff & b);
			strBuffer.append(bString);
		}
		return strBuffer.toString();		
	}


	@org.junit.Test
	public  void test() throws Exception {
		
		
//		List<Song> list = XiamiCatch.postForm();
//		printList(list);
		
		Song song = MP3Analysis2.mp3Info("D:/Audio/虾米音乐/Baad-君が好きだと叫びたい.mp3");//ID3V2.3、ID3V1
////		Song song = MP3Analysis2.mp3Info("D:/Audio/めらみぽっぷ - 竹ノ花 - 译名竹之花.mp3");//ID3V2.2
		System.out.println(song);
		
//		long time = System.currentTimeMillis();
//		List<Song> list = LocalFile.getLocalSongList("D:/Audio");
//		printList(list);
//		System.out.println(System.currentTimeMillis() - time);
		
//		String href = "href=\"http://www.xiami.com/song/m\"";
//		System.out.println(href.substring(6, href.length() - 1));
		
//		List<Song>  songs = new ArrayList<>();		
//		Song song = new Song();
//		song.setDownsite("http://www.xiami.com/song/xL0BmDc0d58");
//		song.setTitle("＜正調＞佐渡の二ッ岩");
//		songs.add(song);
//		XiamiCatch.CatchSongAlbumInfo(songs);
//		System.out.println(song.getAlbum());
		
//		ProcessHtml.findSongByTmpFile("D:/Desktop/Jzz收藏的歌曲.html", songs);
//		XiamiCatch.CatchSongAlbumInfo(songs);
//		ProcessHtml.printList(songs);
		
//		CloseableHttpClient httpClients = HttpClients.createDefault();
//		HttpGet httpGet = new HttpGet("http://www.xiami.com/song/xLB4rda4246");
//		httpGet.setConfig(RequestConfig.custom().setRedirectsEnabled(false).build());
//		HttpResponse response = httpClients.execute(httpGet);
//		System.out.println(response.getStatusLine());
//		System.out.println(EntityUtils.toString(response.getEntity()));
   
	}
	
	public static void main(String[] args) throws Exception {
		new TestUtils().test();

	}
	
}
