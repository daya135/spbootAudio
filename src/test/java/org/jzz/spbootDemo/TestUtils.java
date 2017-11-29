package org.jzz.spbootDemo;

import java.util.List;

import org.junit.runner.RunWith;
import org.jzz.spbootDemo.model.Song;
import org.jzz.spbootDemo.utils.LocalFile;
import org.jzz.spbootDemo.utils.MP3Analysis;
import org.jzz.spbootDemo.utils.XiamiCatch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;


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
			System.out.println("[" + song.getTitle() +"]" + "[" + song.getArtist() +"]" 
					+ "[" + song.getOnsale() + "]");
			n++;
		}
		System.out.println("-------" + songList.size() + "-------");
	}


	public static void main(String[] args) {
//		List<Song> list = XiamiCatch.postForm();
//		printList(list);
		
//		MP3Analysis analysis = new MP3Analysis();
//		analysis.mp3Info("D:/Audio/虾米音乐/Baad-君が好きだと叫びたい.mp3");
		
		List<Song> list = LocalFile.getLocalSongList("D:/Audio/虾米音乐");
		printList(list);
	}
	
	
}
