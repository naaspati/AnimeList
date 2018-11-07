package sam.anime.db2;

import static sam.anime.db2.AnimesMeta.AIRED;
import static sam.anime.db2.AnimesMeta.EPISODES;
import static sam.anime.db2.AnimesMeta.GENRES;
import static sam.anime.db2.AnimesMeta.MAL_ID;
import static sam.anime.db2.AnimesMeta.SYNOPSIS;
import static sam.anime.db2.AnimesMeta.TABLE_NAME;
import static sam.anime.db2.AnimesMeta.TITLE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.collection.Iterable2;
import sam.sql.SqlFunction;


public class AnimesImpl {
	public final int mal_id;
	public final String title;
	public final String episodes;
	public final String aired;
	public final String genres;
	public final String synopsis;

	public AnimesImpl(ResultSet rs) throws SQLException {
		this.mal_id = rs.getInt(MAL_ID);
		this.title = rs.getString(TITLE);
		this.episodes = rs.getString(EPISODES);
		this.aired = rs.getString(AIRED);
		this.genres = rs.getString(GENRES);
		this.synopsis = rs.getString(SYNOPSIS);
	}
	public AnimesImpl(int mal_id, String title, String episodes, String aired, String genres, String synopsis){
		this.mal_id = mal_id;
		this.title = title;
		this.episodes = episodes;
		this.aired = aired;
		this.genres = genres;
		this.synopsis = synopsis;
	}


	public int getMalId(){ return this.mal_id; }
	public String getTitle(){ return this.title; }
	public String getEpisodes(){ return this.episodes; }
	public String getAired(){ return this.aired; }
	public String getGenres(){ return this.genres; }
	public String getSynopsis(){ return this.synopsis; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static <E extends AnimesImpl>  List<E> getAll(AnimeDB db, SqlFunction<ResultSet, E> mapper) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, mapper);
	}
	private static final String FIND_BY_MAL_ID = SELECT_ALL_SQL+" WHERE "+MAL_ID+"=";
	public static <E extends AnimesImpl> E getByMalId(AnimeDB db, int mal_id, SqlFunction<ResultSet, E> mapper) throws SQLException {
		return db.findFirst(FIND_BY_MAL_ID+mal_id, mapper);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", MAL_ID,TITLE,EPISODES,AIRED,GENRES,SYNOPSIS)+") VALUES(?,?,?,?,?,?)";

	public static final int insert(Iterable<? extends AnimesImpl> data, AnimeDB db) throws SQLException {
		Iterable2<? extends AnimesImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (AnimesImpl item: list){
				p.setInt(1,item.mal_id);
				p.setString(2,item.title);
				p.setString(3,item.episodes);
				p.setString(4,item.aired);
				p.setString(5,item.genres);
				p.setString(6,item.synopsis);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

