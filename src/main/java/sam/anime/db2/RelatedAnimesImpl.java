package sam.anime.db2;

import static sam.anime.db2.RelatedAnimesMeta.ID;
import static sam.anime.db2.RelatedAnimesMeta.ID1;
import static sam.anime.db2.RelatedAnimesMeta.ID2;
import static sam.anime.db2.RelatedAnimesMeta.TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import sam.collection.Iterable2;


public class RelatedAnimesImpl implements Comparable<RelatedAnimesImpl> {
	public final int id;
	public final int id1;
	public final int id2;

	public RelatedAnimesImpl(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.id1 = rs.getInt(ID1);
		this.id2 = rs.getInt(ID2);
	}
	public RelatedAnimesImpl(int id1, int id2) {
		if(id1 == id2) throw new IllegalArgumentException("id1 == id2");

		this.id = -1;
		this.id1 = Math.min(id1, id2);
		this.id2 = Math.max(id1, id2);
	}

	@Override
	public int compareTo(RelatedAnimesImpl o) {
		int n = Integer.compare(this.id1, o.id1);
		if(n == 0)
			return Integer.compare(this.id2, o.id2);

		return n;
	}
	
	// public int getId(){ return this.id; }
	public int getId1(){ return this.id1; }
	public int getId2(){ return this.id2; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static void getAll(AnimeDB db, Set<RelatedAnimesImpl> sink) throws SQLException{
		db.collect(SELECT_ALL_SQL, sink, RelatedAnimesImpl::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static RelatedAnimesImpl getById(AnimeDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, RelatedAnimesImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID,ID1,ID2)+") VALUES(?,?,?)";

	public static final int insert(Iterable<RelatedAnimesImpl> data, AnimeDB db) throws SQLException {
		Iterable2<RelatedAnimesImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;
		
		int max = (int) (db.getSequnceValue(TABLE_NAME) + 1);

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (RelatedAnimesImpl item: list){
				p.setInt(1,item.id < 0 ? max++ : item.id);
				p.setInt(2,item.id1);
				p.setInt(3,item.id2);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

