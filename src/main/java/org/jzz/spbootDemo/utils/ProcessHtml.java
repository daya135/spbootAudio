package org.jzz.spbootDemo.utils;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.EntityUtils;
import org.jzz.spbootDemo.model.Song;

/*
 * 解析html, 查找歌名和歌手信息
 * 2016/11/8
 */
public class ProcessHtml {
	
	private List<Song> songList = new ArrayList<Song>();
	
	private static  String replaceKey(String str){
		/*
		 * 全角空格是E38080, 无法trim掉
		 */
		str = str.replace("&#039;", "'");	//返回新对象, 原对象未改变
		str = str.replace("&amp;", "'");	
		str = str.trim();
		return str;
	}
	
	public void printList() {
		int n = 0;
		int pageCount = 1;
		for (Song song : songList) {
			if (n%25 == 0) {
				System.out.println("-------" + pageCount + "-------");
				pageCount ++;
				n = 0;
			}
			System.out.println("[" + song.getTitle() +"]" + "[" + song.getArtist() +"]" 
					+ "[" + song.getOnsale() + "]");
			n++;
		}
		System.out.println("-------" + songList.size() + "-------");
	}
	
	public static void findSongByHtmlStr(String htmlContent, List<Song> songList) {
		BufferedReader reader = new BufferedReader(new StringReader(htmlContent));
		findSong(reader, songList);
	}
	
	public static void findSongByTmpFile(String fileName, List<Song> songList) {
		BufferedReader reader = null;
		try {
			 reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
			 findSong(reader, songList);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static void findSong(BufferedReader reader, List<Song> songList) {
		int tabFlag = 0;
		Song song = null;
		String line = null;
		try{
			Pattern tabStartPatt = Pattern.compile("[\\s]*<tr data-needpay=\"\\d\" data-playstatus=\"\\d\"");
			Pattern tabEndPatt = Pattern.compile("[\\s]*</table>");
	
			while ((line = reader.readLine()) != null) {
				Matcher sMatcher = tabStartPatt.matcher(line);
				Matcher eMatcher = tabEndPatt.matcher(line);
				if (line !=null && sMatcher.find()) {
					tabFlag = 1;	//进入歌单table
				}
				if (tabFlag == 1 && line !=null) { 
					if (line.trim().startsWith("<td class=\"chkbox\">")) {
						song = new Song();
						if (line.contains("checked=\"checked\"")) {
							song.setOnsale("1");
						} else if(line.contains("disabled=\"disabled\"")) {
							song.setOnsale("0");
						}
						song.setDownsite("xiami");
					}
					if (line.trim().equals("<td class=\"song_name\">")) {
						String titleStartStr = "title=\"";
						String titleEndStr = "</a>";
						String Title = tagAnalysis(reader, titleStartStr, titleEndStr, line);
						song.setTitle(replaceKey(Title));
					}
					if (line.trim().startsWith("<a class=\"artist_name\"")) {
						String Artist = line.substring(line.indexOf("\">") + 2, line.indexOf("</a>"));
						//System.out.println("		artist_name:[" + replaceKey(Artist) + "]");
						song.setArtist(replaceKey(Artist));
						songList.add(song);
						//System.out.println(song.getTitle() + " " + song.getArtist() + " " +song.getIsdownload());
						//跳过<tr></tr>内的余下行, 加快处理速度, 实际测试能增加处理速度15%
						while (!(line = reader.readLine()).trim().contains("</tr>")); 
					}
				}
				if (tabFlag == 1 && line !=null && eMatcher.find()) {	//离开table
					reader.close();
					return;
				}
			}
			reader.close();
		} catch (Exception e) {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			e.printStackTrace();
		}	
	}
	
	/* 从当前传过来的line行开始, 查找制定标签的内容 */
	private static String tagAnalysis(BufferedReader reader, String titleStartStr, String titleEndStr, String line) throws IOException{
		String content = null;
		
		while (!line.contains(titleStartStr)) {
			line = reader.readLine();
		}
		int index = line.indexOf(titleStartStr) + titleStartStr.length();
		line = line.substring(index);
		index = 0;
		for(char c:line.toCharArray()) {
			if (c != '>') {
				index ++;
				continue;
			} else {
				index ++;
				break;
			}
		}
		int indexEnd = -1;
		if ((indexEnd = line.indexOf(titleEndStr)) > 0) {
			content = line.substring(index, indexEnd);
		} else {
			content = line.substring(index);
			line = reader.readLine();
			while ((indexEnd = line.indexOf(titleEndStr)) < 0) {
				content += line;	
				line = reader.readLine();
			}
			content += line.substring(0, indexEnd);
		}
		return content;
	}
}
