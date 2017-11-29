package org.jzz.spbootDemo.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.jzz.spbootDemo.model.Song;
import org.springframework.stereotype.Component;

/*
 * 根据本地音乐文件列表和虾米收藏列表, 找出虾米没有下载的歌曲
 */
@Component
public class LocalFile {
	
	private static final String FILETYPE = ".mp3";
	private static final String ENCODE = "gbk";
	
	private MP3Analysis mp3Analysis;
	
	public LocalFile (){
		mp3Analysis = new MP3Analysis();
	}
	
	public LocalFile (MP3Analysis mp3Analysis) {
		this.mp3Analysis = mp3Analysis;
	}
	
	//查找指定目录下的mp3文件列表
	public static List<Song> getLocalSongList(String dirName) {
		List<Song> list = new ArrayList<Song>();
		try {
			File direct = new File(dirName);
			if (direct.isDirectory()) {
				localFileList(dirName, list);
			} else {
				System.out.println("指定目录不存在!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	//递归查找并解析mp3文件，存入指定List
	private  static void localFileList(String dirName, List<Song> list) {
		try {
			File dir = new File(dirName);
			if (!dir.exists() || !dir.isDirectory()) 
				return;
			File files[] = dir.listFiles();
			for (File file:files) {
				if (file.isDirectory()) {
					localFileList(dirName + "/" + file.getName(),  list);
				} else if(file.getName().trim().endsWith(FILETYPE)) {	
					Song song = MP3Analysis.mp3Info(dirName + "/" + file.getName().trim());

					if (song.getTitle() == null || song.getTitle().trim().length() == 0) {
						System.out.println("未能解析出有用信息,跳过");
						System.out.println("=============================================");
					} else {
						song.setLocalpath(dirName + "\\" + file.getName().trim());
						list.add(song);
						System.out.println(song.getTitle() + " || " + song.getArtist() + " || " + song.getBand() + " || " 
								+ song.getAlbum() + " || " + song.getPublishyear());
						System.out.println("=============================================");
					}
				}
			}
		}	catch (Exception e) {
			e.printStackTrace();
		}
		return;
	}
	
	/* 
	 * 确定两个mp3是否是同一首歌
	 * 建议增加mp3文件内部解析, 比较MP3长度, 码率, hash值等信息是否一致
	 */
	public boolean compareStr(String str1, String str2) {
		if (str1.equals(str2)) {
			return true;
		} 
		int maxLength = str1.length() > str2.length() ? str1.length() : str2.length();
		int minLength = str1.length() < str2.length() ? str2.length() : str1.length();
		if (str1.contains(str2) || str2.contains(str1)) {
			// 相似度校验
			if (minLength / maxLength > 0.75) {
				return true;
			}
		}
		return false;
	}
	
	public void writeList(List<Song> list) {
		String fileNameStr = "localList.txt";
		try {
			//从内码写出, 可随意指定编码, 只需要保证在文本编辑器或读文件时按照对应编码即可!!!
			BufferedWriter  out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(new File(fileNameStr), false), "GBK"));
			for (Song song : list) {
				out.append(song.getTitle() + "\t" + song.getArtist() + "\n");
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public List<Song> compareList(List<Song> webList, List<Song> localList) {
		List<Song> list = new ArrayList<Song>();
		for (Song webSong : webList) {
			for (Song localSong : localList) {
				if (compareStr(webSong.getArtist(), localSong.getArtist()))
					if (compareStr(webSong.getTitle(), localSong.getTitle()))	{
						list.add(webSong);
						System.out.println(webSong.getTitle() + "\t" + webSong.getArtist());
						continue;
					}
				
			}
		}
		return list;
	}
	
//	public void xiamibulou () {
//		try {
//			webList = new ProcessHtml().findSong();
//			System.out.println(webList.size());
//			localList = readLocalMP3("D:\\Audio");
//			System.out.println(localList.size());
//			DownloadedList = compareList(webList, localList);
//		} catch (IOException e) {
//			// TODO 自动生成的 catch 块
//			e.printStackTrace();
//		}
//	}
	
}
