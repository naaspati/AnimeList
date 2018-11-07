package sam.anime.db2;

import static sam.anime.db2.JIKANMeta.JSON;
import static sam.anime.db2.JIKANMeta.MAL_ID;
import static sam.anime.db2.JIKANMeta.TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.collection.Iterable2;


public class JIKANImpl {
	private final int mal_id;
	private final String json;

	public JIKANImpl(ResultSet rs) throws SQLException {
		this.mal_id = rs.getInt(MAL_ID);
		this.json = rs.getString(JSON);
	}
	public JIKANImpl(int mal_id, String json){
		this.mal_id = mal_id;
		this.json = json;
	}
	public int getMalId(){ return this.mal_id; }
	public String getJson(){ return this.json; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static List<JIKANImpl> getAll(AnimeDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, JIKANImpl::new);
	}
	private static final String FIND_BY_MAL_ID = SELECT_ALL_SQL+" WHERE "+MAL_ID+"=";
	public static JIKANImpl getByMalId(AnimeDB db, int mal_id) throws SQLException {
		return db.findFirst(FIND_BY_MAL_ID+mal_id, JIKANImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", MAL_ID,JSON)+") VALUES(?,?)";

	public static final int insert(Iterable<JIKANImpl> data, AnimeDB db) throws SQLException {
		Iterable2<JIKANImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (JIKANImpl item: list){
				p.setInt(1,item.mal_id);
				p.setString(2,item.json);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
	public static final int insert(int mal_id, String json, AnimeDB db) throws SQLException {
		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			p.setInt(1,mal_id);
			p.setString(2,json);
			return p.executeUpdate();
		}
	}
}

