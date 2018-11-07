package sam.anime.db2;

import static sam.anime.db2.DirsMeta.DIRS_COUNT;
import static sam.anime.db2.DirsMeta.FILES_COUNT;
import static sam.anime.db2.DirsMeta.ID;
import static sam.anime.db2.DirsMeta.LAST_MODIFIED;
import static sam.anime.db2.DirsMeta.MAL_ID;
import static sam.anime.db2.DirsMeta.PARENT_ID;
import static sam.anime.db2.DirsMeta.SUBPATH;
import static sam.anime.db2.DirsMeta.TABLE_NAME;
import static sam.anime.db2.DirsMeta.TOTAL_SIZE;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.collection.Iterable2;


public class DirsImpl {
	private final int id;
	private final int mal_id;
	private final int parent_id;
	private final String subpath;
	private final int last_modified;
	private final int total_size;
	private final int dirs_count;
	private final int files_count;

	public DirsImpl(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.mal_id = rs.getInt(MAL_ID);
		this.parent_id = rs.getInt(PARENT_ID);
		this.subpath = rs.getString(SUBPATH);
		this.last_modified = rs.getInt(LAST_MODIFIED);
		this.total_size = rs.getInt(TOTAL_SIZE);
		this.dirs_count = rs.getInt(DIRS_COUNT);
		this.files_count = rs.getInt(FILES_COUNT);
	}
	public DirsImpl(int id, int mal_id, int parent_id, String subpath, int last_modified, int total_size, int dirs_count, int files_count){
		this.id = id;
		this.mal_id = mal_id;
		this.parent_id = parent_id;
		this.subpath = subpath;
		this.last_modified = last_modified;
		this.total_size = total_size;
		this.dirs_count = dirs_count;
		this.files_count = files_count;
	}


	public int getId(){ return this.id; }
	public int getMalId(){ return this.mal_id; }
	public int getParentId(){ return this.parent_id; }
	public String getSubpath(){ return this.subpath; }
	public int getLastModified(){ return this.last_modified; }
	public int getTotalSize(){ return this.total_size; }
	public int getDirsCount(){ return this.dirs_count; }
	public int getFilesCount(){ return this.files_count; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static List<DirsImpl> getAll(AnimeDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, DirsImpl::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static DirsImpl getById(AnimeDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, DirsImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID,MAL_ID,PARENT_ID,SUBPATH,LAST_MODIFIED,TOTAL_SIZE,DIRS_COUNT,FILES_COUNT)+") VALUES(?,?,?,?,?,?,?,?)";

	public static final int insert(Iterable<DirsImpl> data, AnimeDB db) throws SQLException {
		Iterable2<DirsImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;

		int max = (int) (db.getSequnceValue(TABLE_NAME) + 1);

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (DirsImpl item: list){
				p.setInt(1,item.id < 0 ? max++ : item.id);
				p.setInt(2,item.mal_id);
				p.setInt(3,item.parent_id);
				p.setString(4,item.subpath);
				p.setInt(5,item.last_modified);
				p.setInt(6,item.total_size);
				p.setInt(7,item.dirs_count);
				p.setInt(8,item.files_count);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

