package org.jzz.spbootDemo.Service;

import java.util.Date;
import java.util.List;

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
	
	@Autowired 
	SongRepository songRepository;
	
	public void xiamiSynchronize() {

		List<Song> songDBList = songRepository.findAll();
		List<Song> songXiamiList = XiamiCatch.CatchSongInfo();
//		XiamiCatch.CatchSongAlbumInfo(songXiamiList);
		
		logger.info("虾米收藏数: " + songXiamiList.size() + "  数据库条目数: " + songDBList.size() + " 开始同步...");
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
					//本地存在, 更新下架信息
					logger.debug("更新下架信息: " + songXiami);
					songDB.setOnsale(songXiami.getOnsale());
					songDB.setUpdatetime(new Date());
					songRepository.save(songDB);
					processFlag = 1;
					continue;
				}
			}
			if (processFlag == 0) {
				//本地不存在,入库
				logger.info("插入新条目: " + songXiami);
				songXiami.setIsdownload("0");
				songRepository.save(songXiami);
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
