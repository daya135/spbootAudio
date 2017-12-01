package org.jzz.spbootDemo.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.assertj.core.internal.cglib.core.CollectionUtils;
import org.jzz.spbootDemo.model.Song;
import org.jzz.spbootDemo.model.SongRepository;
import org.jzz.spbootDemo.utils.ProcessHtml;
import org.jzz.spbootDemo.utils.XiamiCatch;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service("userService")
@Transactional
public class SongService {
	
	private org.slf4j.Logger logger = LoggerFactory.getLogger(SongService.class);
	private static final int MaxPage = 9999;
	
	@Autowired 
	SongRepository songRepository;
	
	public void xiamiSynchronize() {

		List<Song> insertSongList = new ArrayList<>();
		List<Song> updateSongList = new ArrayList<>();
		List<Song> songDBList = songRepository.findAll();
		List<Song> songXiamiList = XiamiCatch.CatchSongInfo(MaxPage);
		
		Collections.reverse(songXiamiList); //反转顺序，保证越新的歌曲id越大
		//XiamiCatch.CatchSongAlbumInfo(songXiamiList);
		
		logger.info(String.format("新处理虾米收藏数:[%d],数据库条目数:[%d],开始同步到数据库...", songXiamiList.size(), songDBList.size()));
		int processFlag = 0;
		for (int i = 0; i < songXiamiList.size(); i++) {
			Song songXiami = songXiamiList.get(i);
			logger.info("处理第" + i + "首歌曲: "+ songXiami);
			if (StringUtils.isEmpty(songXiami.getTitle()) || StringUtils.isEmpty(songXiami.getArtist())) {
				logger.info("虾米抓取的音乐名或艺术家为空: " + songXiami);
				continue;
			}
			//是否已经为当前歌曲更改数据库标志, 1代表已经插入或更新, 0代表未进行插入或更新
			processFlag = 0;
			for (Song songDB : songDBList) {
				if (compareSongXiami(songXiami, songDB)) {
					//数据库存在, 更新下架信息
					logger.debug("更新下架信息: " + songXiami);
					songDB.setOnsale(songXiami.getOnsale());
					songDB.setUpdatetime(new Date());
					updateSongList.add(songDB);
					processFlag = 1;
					continue;
				}
			}
			if (processFlag == 0) {
				//数据库不存在,入库
				logger.info("插入新条目: " + songXiami);
				songXiami.setIsdownload("0");
				insertSongList.add(songXiami);
			}
		}
	}
	
	public void updateAlbumInfo() {
		List<Song> songDBList = songRepository.findAll();
		XiamiCatch.CatchSongAlbumInfo(songDBList);
		ProcessHtml.printList(songDBList);
		songRepository.save(songDBList);

	}
	
	/* 比较虾米歌曲与数据库区别，并根据需求记录日志 */
	private Boolean compareSongXiami(Song songNew, Song songDB) {
		if (songNew.getTitle().equals(songDB.getTitle())) {
			if (songNew.getArtist().equals(songDB.getArtist())){
				return true;
			} else {
				//分析相似度，记录日志
				if(songNew.getArtist().contains(songDB.getArtist()) 
						|| songDB.getArtist().contains(songNew.getArtist())) {
					logger.info("同名,作者有交集" + songNew + "  " + songDB);
				} else {
					logger.info("同名,作者无交集" + songNew + "  " + songDB);
				}	
			}
		} else {
			//分析相似度，记录日志
			
		}
		return false;
	}
	
	/* 比较本地歌曲与数据库区别，并根据需求记录日志  */
	/* 设置两个方法，因为比较策略和记录日志的策略不一样  */
	private Boolean compareSongLocal(Song songNew, Song songDB) {
		if (songNew.getTitle().equals(songDB.getTitle())) {
			if (songNew.getArtist().equals(songDB.getArtist())){
				
				return true;
			} else {
				//分析相似度，记录日志
				if(songNew.getArtist().contains(songDB.getArtist()) 
						|| songDB.getArtist().contains(songNew.getArtist())) {
					logger.info("同名,作者有交集" + songNew + "  " + songDB);
				}
			}
		} else {
			//分析相似度，记录日志
		}
		return false;
	}
}
