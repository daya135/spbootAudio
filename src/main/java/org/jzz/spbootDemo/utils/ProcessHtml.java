package org.jzz.spbootDemo.utils;

import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jzz.spbootDemo.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 解析html, 查找歌名和歌手信息
 * 2016/11/8
 */
public class ProcessHtml {
	
	private static Logger logger = LoggerFactory.getLogger(ProcessHtml.class);
	private static Pattern tabStartPatt = Pattern.compile("[\\s]*<tr data-needpay=\"\\d\" data-playstatus=\"\\d\"");
	private static Pattern tabEndPatt = Pattern.compile("[\\s]*</table>");
	private static Pattern hrefPatt = Pattern.compile("(href=\")\\S*\"");
	
	private static  String replaceKey(String str){
		/*
		 * 全角空格是E38080, 无法trim掉
		 */
		str = str.replace("&#039;", "'");	//返回新对象, 原对象未改变
		str = str.replace("&amp;", "'");	
		str = str.trim();
		return str;
	}
	
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
						String TitleInfo[] = tagAnalysis(reader, titleStartStr, titleEndStr, line);
						song.setDownsite(TitleInfo[0]); //设置歌曲虾米链接
						song.setTitle(replaceKey(TitleInfo[1])); //设置歌曲名称
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
	
	/**
	 * @description 从当前传过来的line行开始, 查找链接和标签内容
	 * @parm 
	 * @return String[2], 其中sting[0]=链接信息、string[1]=歌曲名称
	 */
	private static String[] tagAnalysis(BufferedReader reader, String startTag, String endTag, String line) throws IOException{
		String content[] = new String[2];
		
		//提取歌曲链接信息
		//默认，歌曲链接位于titleStartStr同一行：：<a title="いろは唄" href="http://www.xiami.com...
		while (!line.contains(startTag)) {
			line = reader.readLine();
		}
		Matcher matcher = hrefPatt.matcher(line);
		if (matcher.find()) {
			String href = matcher.group(0);
			href = href.substring(6, href.length() - 1);
			content[0] = href;
		} else {
			logger.debug("没有匹配到href信息: " + line);
		}
		
		//提取歌曲名，从<a>标签中提取
		int index = line.indexOf(startTag) + startTag.length();
		line = line.substring(index); //截断字符串，因为前面可能有'">'内容	
		index = line.lastIndexOf("\">") + 2; //最后一处'">'视为<startTag ...>结束
		
		int indexEnd = -1;
		if ((indexEnd = line.indexOf(endTag)) > 0) {
			content[1] = line.substring(index, indexEnd);
		} else {
			//如果<a>的内容换行，则需要拼接内容
			content[1] = line.substring(index);
			line = reader.readLine();
			while ((indexEnd = line.indexOf(endTag)) < 0) {
				content[1] += line;	
				line = reader.readLine();
			}
			content[1] += line.substring(0, indexEnd);
		}
		return content;
	}
	
	/**
	 * 根据音乐页面，提取专辑信息
	 * @param htmlContent 音乐信息页面
	 * @return String 专辑名称
	 */
	public static String findAlbumByHtmlStr (String htmlContent) {
		BufferedReader reader = new BufferedReader(new StringReader(htmlContent));
		String line = null;
		String startTag = "<a href=";
		String endTag = "</a>";
		try {
			while((line = reader.readLine()) == null || !line.contains("<table id=\"albums_info\"")) {}
			String[] taginfo = tagAnalysis(reader, startTag, endTag, line);
			if (taginfo != null && taginfo[1] != null) {
				return taginfo[1];
			}
		} catch (Exception e) {
			
		}
		
		return null;
	}
}
