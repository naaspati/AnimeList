package sam.anime.db2;

import static sam.anime.db2.UrlsMeta.ID;
import static sam.anime.db2.UrlsMeta.MAL_ID;
import static sam.anime.db2.UrlsMeta.TABLE_NAME;
import static sam.anime.db2.UrlsMeta.URL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import sam.collection.Iterable2;


public class UrlsImpl {
	private final int id;
	private final int mal_id;
	private final String url;

	public UrlsImpl(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.mal_id = rs.getInt(MAL_ID);
		this.url = rs.getString(URL);
	}
	public UrlsImpl(String url){
		this.id = -1;
		this.mal_id = -1;
		this.url = url;
	}

	public int getId(){ return this.id; }
	public int getMalId(){ return this.mal_id; }
	public String getUrl(){ return this.url; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static List<UrlsImpl> getAll(AnimeDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, UrlsImpl::new);
	}
	public static final String FIND_BY_MAL_ID = SELECT_ALL_SQL+" WHERE "+MAL_ID+"=";
	public static void getByMalId(Set<UrlsImpl> sink, AnimeDB db, int mal_id) throws SQLException {
		db.collect(FIND_BY_MAL_ID+mal_id, sink, UrlsImpl::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static UrlsImpl getById(AnimeDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, UrlsImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID,MAL_ID,URL)+") VALUES(?,?,?)";

	public static final int insert(Iterable<UrlsImpl> data, AnimeDB db) throws SQLException {
		Iterable2<UrlsImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;
		
		int max = (int) (db.getSequnceValue(TABLE_NAME) + 1);

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (UrlsImpl item: list){
				p.setInt(1,item.id < 0 ? max++ : item.id);
				p.setInt(2,item.mal_id);
				p.setString(3,item.url);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
	
	@Override
	public String toString() {
		return url;
	}
}

