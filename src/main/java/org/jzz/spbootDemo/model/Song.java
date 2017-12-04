package org.jzz.spbootDemo.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "song_info") /* 指定表名，默认用类名 */
public class Song {
	
	@Id	/*标示主键  */
	@GeneratedValue(strategy = GenerationType.AUTO) /* 自增列  */
    private Integer songid;

	@Column(nullable = false)
    private String title;
	
	@Column(nullable = true)
    private String artist;
	@Column(nullable = true)
    private String album;
	@Column(nullable = true)
    private String band;
	@Column(nullable = true)
    private String rate;
	@Column(nullable = true)
    private String len;
	@Column(nullable = true)
    private String publishyear;
	@Column(nullable = true)
	private String downsite;
	@Column(nullable = true)
    private String onsale;
	@Column(nullable = true)
    private String langtype;
	@Column(nullable = true)
    private String filetype;
	@Column(nullable = true)
    private String isdownload;
	@Column(nullable = true)
    private String localpath;
	
	@Column(nullable = true)
	@Temporal(TemporalType.DATE)  /* 定义日期精度 */
    private Date createtime;
	@Column(nullable = true)
	@Temporal(TemporalType.DATE)  /* 定义日期精度 */
    private Date updatetime;

	public Song() {}
	
    public Integer getSongid() {
        return songid;
    }

    public void setSongid(Integer songid) {
        this.songid = songid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist == null ? null : artist.trim();
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album == null ? null : album.trim();
    }

    public String getBand() {
        return band;
    }

    public void setBand(String band) {
        this.band = band == null ? null : band.trim();
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate == null ? null : rate.trim();
    }

    public String getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = len == null ? null : len.trim();
    }

    public String getPublishyear() {
        return publishyear;
    }

    public void setPublishyear(String publishyear) {
        this.publishyear = publishyear == null ? null : publishyear.trim();
    }

    public String getDownsite() {
        return downsite;
    }

    public void setDownsite(String downsite) {
        this.downsite = downsite == null ? null : downsite.trim();
    }

    public String getOnsale() {
        return onsale;
    }

    public void setOnsale(String onsale) {
        this.onsale = onsale == null ? null : onsale.trim();
    }

    public String getLangtype() {
        return langtype;
    }

    public void setLangtype(String langtype) {
        this.langtype = langtype == null ? null : langtype.trim();
    }

    public String getFiletype() {
        return filetype;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype == null ? null : filetype.trim();
    }

    public String getIsdownload() {
        return isdownload;
    }

    public void setIsdownload(String isdownload) {
        this.isdownload = isdownload == null ? null : isdownload.trim();
    }

    public String getLocalpath() {
        return localpath;
    }

    public void setLocalpath(String localpath) {
        this.localpath = localpath == null ? null : localpath.trim();
    }

    public Date getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Date createtime) {
        this.createtime = createtime;
    }

    public Date getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Date updatetime) {
        this.updatetime = updatetime;
    }
    
    @Override
    public String toString() {
        return String.format("title=[%s],artist=[%s],album=[%s],band=[%s],locakpath=[%s]", title, artist, album, band, localpath);
    }
}