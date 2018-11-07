package sam.anime.dao;

import static sam.anime.db2.AltNamesMeta.MAL_ID;
import static sam.anime.db2.AltNamesMeta.TABLE_NAME;
import static sam.anime.db2.AltNamesMeta.TITLE_SYNONYMS;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.TreeSet;

import sam.anime.db2.AnimeDB;

public class AnimeTitle implements Comparable<AnimeTitle> {
	public final String title, lowercased;
	public final int mal_id;

	public AnimeTitle(ResultSet rs) throws SQLException {
		this.title = rs.getString(TITLE_SYNONYMS);
		this.lowercased = title.toLowerCase();
		this.mal_id = rs.getInt(MAL_ID);
	}
	public AnimeTitle(Anime aw) {
		this.mal_id = aw.getMalId();
		this.title = aw.getTitle();
		this.lowercased = title.toLowerCase();
	}
	public AnimeTitle(int mal_id, String title) {
		this.mal_id = mal_id;
		this.title = title;
		this.lowercased = title.toLowerCase();
	}
	static Set<AnimeTitle> getAll(AnimeDB db) throws SQLException {
		String sql = "SELECT "+MAL_ID+","+TITLE_SYNONYMS+" FROM "+TABLE_NAME;
		return db.collect(sql, new TreeSet<>(), AnimeTitle::new);
	}
	public int getMalId() {
		return mal_id;
	}
	public String getTitle() {
		return title;
	}
	@Override
	public int compareTo(AnimeTitle o) {
		if(this.mal_id == o.mal_id) 
			return this.title.compareTo(o.title);
		
		return (this.mal_id < o.mal_id) ? -1 : 1;
	}
	@Override
	public String toString() {
		return title;
	}
}
