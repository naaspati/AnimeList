package sam.anime.db2;

import static sam.anime.db2.AltNamesMeta.ID;
import static sam.anime.db2.AltNamesMeta.MAL_ID;
import static sam.anime.db2.AltNamesMeta.TABLE_NAME;
import static sam.anime.db2.AltNamesMeta.TITLE_SYNONYMS;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.anime.dao.AnimeTitle;
import sam.collection.Iterable2;


/**
 * using {@link AnimeTitle}
 * @author Sameer
 *
 */
@Deprecated 
public class AltNamesImpl {
	private final int id;
	private final int mal_id;
	private final String title_synonyms;

	public AltNamesImpl(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.mal_id = rs.getInt(MAL_ID);
		this.title_synonyms = rs.getString(TITLE_SYNONYMS);
	}
	public AltNamesImpl(int id, int mal_id, String title_synonyms){
		this.id = id;
		this.mal_id = mal_id;
		this.title_synonyms = title_synonyms;
	}
	public int getId(){ return this.id; }
	public int getMalId(){ return this.mal_id; }
	public String getTitleSynonyms(){ return this.title_synonyms; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static List<AltNamesImpl> getAll(AnimeDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, AltNamesImpl::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static AltNamesImpl getById(AnimeDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, AltNamesImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID,MAL_ID,TITLE_SYNONYMS)+") VALUES(?,?,?)";

	public static final int insert(Iterable<AltNamesImpl> data, AnimeDB db) throws SQLException {
		Iterable2<AltNamesImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;
		
		int max = (int) (db.getSequnceValue(TABLE_NAME) + 1);

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (AltNamesImpl item: list){
				p.setInt(1,item.id < 0 ? max++ : item.id);
				p.setInt(2,item.mal_id);
				p.setString(3,item.title_synonyms);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

