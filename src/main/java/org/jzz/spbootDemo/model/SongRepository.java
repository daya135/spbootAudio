package org.jzz.spbootDemo.model;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SongRepository extends JpaRepository<Song, Integer>{
	
	List<Song> findByTitle(String title);

	List<Song> findByTitleAndArtist(String title, Pageable artist);

}
