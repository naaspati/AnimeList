package sam.anime.dao;
import static sam.anime.db2.RelatedAnimesMeta.ID1;
import static sam.anime.db2.RelatedAnimesMeta.ID2;
import static sam.anime.db2.RelatedAnimesMeta.TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import sam.anime.db2.AnimeDB;

class RelatedAnimesImpl implements Comparable<RelatedAnimesImpl> {
	public final int id1;
	public final int id2;

	public RelatedAnimesImpl(ResultSet rs) throws SQLException {
		this.id1 = rs.getInt(ID1);
		this.id2 = rs.getInt(ID2);
	}
	public static void getAll(AnimeDB db, Set<RelatedAnimesImpl> sink) throws SQLException{
		db.collect("SELECT * FROM "+TABLE_NAME,sink, RelatedAnimesImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID1,ID2)+") VALUES(?,?)";

	public static final int insert(Iterable<RelatedAnimesImpl> list, AnimeDB db) throws SQLException {
		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (RelatedAnimesImpl item: list){
				p.setInt(1,item.id1);
				p.setInt(2,item.id2);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

