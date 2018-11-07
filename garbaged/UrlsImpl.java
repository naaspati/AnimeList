package sam.anime.dao;
import static sam.anime.db2.UrlsMeta.MAL_ID;
import static sam.anime.db2.UrlsMeta.TABLE_NAME;
import static sam.anime.db2.UrlsMeta.URL;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import sam.sql.sqlite.AnimeDB;


class UrlsImpl {
	public static final String FIND_BY_MAL_ID = "SELECT "+URL+" FROM "+TABLE_NAME+" WHERE "+MAL_ID+"=";
	public static void getByMalId(Set<UrlsImpl> sink, AnimeDB db, int mal_id) throws SQLException {
		db.collect(FIND_BY_MAL_ID+mal_id, sink, rs -> rs.getString(1));
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", MAL_ID,URL)+") VALUES(?,?)";

	public static final int insert(int mal_id, List<String> list, AnimeDB db) throws SQLException {
		if(list.isEmpty()) return 0;

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (String item: list){
				p.setInt(1,mal_id);
				p.setString(2,item);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

