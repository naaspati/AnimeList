package sam.anime.db.entities;

import static sam.anime.db.meta.AnimesMeta.MAL_ID;
import static sam.anime.db.meta.AnimesMeta.TITLE;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MinimalAnime {
	protected final int mal_id;
	private String title;
	
	public static final String[] columns() {
		return new String[] {MAL_ID, TITLE};
	}
	
	public MinimalAnime(ResultSet rs) throws SQLException {
		this.mal_id = rs.getInt(MAL_ID);
		this.title = rs.getString(TITLE);
	}

	public MinimalAnime(int mal_id, String title) {
		this.mal_id = mal_id;
		this.title = title;
	}
	public int getMalId() { return mal_id; }
	public String getTitle() { return title; }
	
	void setTitle(String title) {
		this.title = title;
	}
	@Override
	public int hashCode() {
		return mal_id;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		return mal_id == ((MinimalAnime) obj).mal_id;
	}

	@Override
	public String toString() {
		return "MinimalAnime [mal_id=" + mal_id + ", title=" + title + "]";
	}
}
