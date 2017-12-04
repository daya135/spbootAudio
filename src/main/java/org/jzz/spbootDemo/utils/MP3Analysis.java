package org.jzz.spbootDemo.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jzz.spbootDemo.model.Song;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MP3Analysis {
	
	private static Logger logger = LoggerFactory.getLogger(MP3Analysis.class);
	private static final String ID3V2_3_TAG[] = {"TIT2", "TPE1", "TPE2", "TALB", "TYER", "TIME", "TLEN", "TSIZ"};
	private static final String ID3V2_2_TAG[] = {"TT2", "TP1", "TP2", "TAL", "TYE", "TIM", "TLE", "TSI"};
	
	private static final String regYear = "[0-9]{4}";
	private static Pattern pattern = Pattern.compile(regYear);
	
	public static Song mp3Info (String fileName) throws Exception {
		Song song = new Song(); //值为null的指针调用任何方法, 报NullPointerException!!!!
		RandomAccessFile raf = null;
		logger.debug(String.format("开始解析mp3文件[%s]" , fileName));	
		try {
			raf = new RandomAccessFile(fileName, "r");
			String fileHead = fileType(raf);
			switch (fileHead) {
			case "ID3V2.3":
				ID3v2_3_info(raf, song);
				break;
			case "ID3V2.2":
				ID3v2_2_info(raf, song);
				break;
			case "ID3V1":
				ID3v1_info(raf, song);
				break;
			default:
				throw new RuntimeException(String.format("不支持的文件头:[%s]", fileHead));
			}	
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				logger.debug(String.format("关闭mp3文件[%s]完成" , fileName));
			}
		}
		logger.debug(String.format("解析mp3文件[%s]完成" , fileName));
		return song;
	}
	
	private static void ID3v2_3_info(RandomAccessFile raf, Song song) throws Exception{
		//数组length属性是32位的有符号整数，最大长度为0x7fffffff, 实际上MP3的标签帧长度最大只能定义28位(省略4个高位0)，而且实际上远远用不上这么大
		int bufMaxSize = 0x00100000;	//暂定这个长度, 约为1MB
		byte buf[] = new byte[bufMaxSize];
		//文件偏移量，设为6 表示从标签帧大小位置开始读取
		int offset = 6;
		raf.seek(offset);
		raf.read(buf, 0, 4);  
		//总标签帧大小(不含标签头) + 标签头10字节
		int totalSize = (buf[0]&0x7F)*0x200000+ (buf[1]&0x7F)*0x400 + (buf[2]&0x7F)*0x80 +(buf[3]&0x7F) + 10;
			
		offset += 4;
		while (offset < totalSize) {
			raf.seek(offset);
			raf.read(buf, 0, 4);	//读取帧标识
			String tagStr = new String(buf, 0, 4, "ASCII");
			
			//帧大小, 不包括帧头10字节
			offset += 4;
			raf.seek(offset);
			raf.read(buf, 0, 4);  			
			int frameSize = (buf[0]&0xff)*0x1000000 + (buf[1]&0xff)*0x10000 + (buf[2]&0xff)*0x100 +(buf[3]&0xff);
			if (frameSize > bufMaxSize || frameSize < 1) {	
				throw new RuntimeException(tagStr + " 帧大小异常! frameSize=" + frameSize);
			}
			
			//标签头为其它帧， 不处理
			if (!isInfoTag2_3(tagStr)) {
				logger.debug(tagStr + " 其它帧, 跳过");
				offset = offset + 6 + frameSize;
				continue;
			}
			
			//其它帧标签头，读取信息
			offset += 6;
			raf.seek(offset);
			//读取帧内容
			raf.read(buf, 0, frameSize);
			int tmpOffset = 0;
			String content = null;
			
			//id3v2.3帧标识编码指示
			if (buf[0] == (byte)0x01) {
				tmpOffset ++;
				if ((buf[tmpOffset] == (byte)0xff && buf[tmpOffset + 1] == (byte)0xfe)
						|| (buf[tmpOffset] == (byte)0xfe && buf[tmpOffset + 1] == (byte)0xff)) {
					logger.debug(tagStr + "  01 unicode");
					content = new String(buf, tmpOffset, frameSize - tmpOffset, "unicode").trim();
				}
				else {
					logger.debug(tagStr + " use default GBK");
					content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
				}
			} else if (buf[0] == (byte)0x00){
				tmpOffset ++;
				logger.debug(tagStr + " 00 GBK");
				content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
			} else {
				logger.debug(tagStr + " use default GBK");
				content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
			}
			
//			if (frameSize < 100) 
//				logger.debug(tagStr + " " + content);
			
			switch (tagStr) {
			case "TIT2":
				song.setTitle(content);
				break;
			case "TPE1":
				song.setArtist(content);
			case "TPE2":
				song.setBand(content);
			case "TALB":
				song.setAlbum(content);
//			case "TIME":
//				logger.debug(content);
//			case "TLEN":
//				logger.debug(content);
//			case "TSIZ":
//				logger.debug(content);		
			case "TYER":
				{	
					Matcher matcher = pattern.matcher(content);
					if(matcher.matches()) {
						song.setPublishyear(content);
					}
				}
			default:
				break;
			}

			offset += frameSize;
		}
	}
	
	private static void ID3v2_2_info(RandomAccessFile raf, Song song) throws Exception{
		int bufMaxSize = 0x00100000;	//暂定这个长度, 约为1MB
		byte buf[] = new byte[bufMaxSize];
		//文件偏移量，设为6 表示从标签帧大小位置开始读取
		int offset = 6;
		raf.seek(offset);
		raf.read(buf, 0, 4);  
		//计算总标签帧大小， 不包括标签头10字节
		int totalSize = (buf[0]&0x7F)*0x200000+ (buf[1]&0x7F)*0x400 + (buf[2]&0x7F)*0x80 +(buf[3]&0x7F);
			
		offset += 4;
		while (offset < totalSize) {
			raf.seek(offset);
			raf.read(buf, 0, 3);	//读取帧标识
			String tagStr = new String(buf, 0, 3, "ASCII");
			
			//帧大小, 不包括帧头
			offset += 3;
			raf.seek(offset);
			raf.read(buf, 0, 3);  			
			int frameSize = (buf[0]&0xff)*0x10000 + (buf[1]&0xff)*0x100 +(buf[2]&0xff);
			if (frameSize > bufMaxSize || frameSize < 1) {
				throw new RuntimeException(tagStr + " 帧大小异常! frameSize=" + frameSize);
			}
			
			if (!isInfoTag2_2(tagStr)) {
				logger.debug(tagStr + " 其它帧, 跳过");
				offset = offset + 3 + frameSize;
				continue;
			}
			
			//其它帧标签头，读取信息
			offset += 3;
			raf.seek(offset);
			//读取帧内容
			raf.read(buf, 0, frameSize);
			int tmpOffset = 0;
			String content = null;
			
			//id3v2.2帧标识编码指示
			if (buf[0] == (byte)0x01) {
				tmpOffset ++;
				if ((buf[tmpOffset] == (byte)0xff && buf[tmpOffset + 1] == (byte)0xfe)
						|| (buf[tmpOffset] == (byte)0xfe && buf[tmpOffset + 1] == (byte)0xff)) {
					logger.debug(tagStr + "  01 unicode");
					content = new String(buf, tmpOffset, frameSize - tmpOffset, "unicode").trim();
				}
				else {
					logger.debug(tagStr + " use default GBK");
					content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
				}
			} else if (buf[0] == (byte)0x00){
				tmpOffset ++;
				logger.debug(tagStr + " 00 GBK");
				content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
			} else {
				logger.debug(tagStr + " use default GBK");
				content = new String(buf, tmpOffset, frameSize - tmpOffset, "GBK").trim();
			}
			

//			if (frameSize < 100) 
//				logger.debug(tagStr + " " + content);
			
			switch (tagStr) {
			case "TT2":
				song.setTitle(content);
				break;
			case "TP1":
				song.setArtist(content);
			case "TP2":
				song.setBand(content);
			case "TAL":
				song.setAlbum(content);
			case "TYE":
			{	
				Matcher matcher = pattern.matcher(content);
				if(matcher.matches()) {
					song.setPublishyear(content);
				}
			}
			default:
				break;
			}
			
			offset += frameSize;
		}
	}
	
	private static void ID3v1_info(RandomAccessFile raf, Song song) throws Exception{ 
		byte[] buf = new byte[128];
		raf.seek(raf.length() - 128);
		raf.read(buf);
		song.setTitle(new String(buf, 3, 30, "GBK").trim());
		song.setArtist(new String(buf, 33, 30, "GBK").trim());
		song.setAlbum(new String(buf, 63, 30, "GBK").trim());
		song.setPublishyear(new String(buf, 93, 4, "GBK"));
	}
	
	
	/* 判断是否是作者/专辑/歌名 */
	private static boolean isInfoTag2_3(String s) {
		for (String tag:ID3V2_3_TAG) {
			if (tag.equals(s))
				return true;
		}
		return false;
	}
	
	private static boolean isInfoTag2_2(String s) {
		for (String tag:ID3V2_2_TAG) {
			if (tag.equals(s))
				return true;
		}
		return false;
	}
	
	/**
	 * 判断mp3文件头
	 * 返回值：ID3V3,ID3V2,ID3V1,或者8个16进制字符表示的四字节文件头
	 */
	private static String fileType(RandomAccessFile raf) throws Exception{
		byte[] buf = new byte[128];
		raf.seek(0);
		raf.read(buf, 0, 4);
		if ("ID3".equals(new String(buf, 0, 3, "ASCII")) ) {
			if (buf[3] == (byte)0x03)
				return "ID3V2.3";
			else if (buf[3] == (byte)0x02) 
				return "ID3V2.2";
		} 
		StringBuffer headInfo = new StringBuffer(); 
		for (int i = 0; i < 4; i ++) {
			headInfo.append(Integer.toHexString(buf[i]).toUpperCase()); //记录下前四位，如果ID3V1判断失败则返回
		}
		//判断ID3V1，文件尾128位
		raf.seek(raf.length() - 128);
		raf.read(buf);
		if (buf.length == 128 && "TAG".equalsIgnoreCase(new String(buf, 0, 3, "ASCII"))) {
			return "ID3V1";
		} 

		return headInfo.toString();
	}

}
