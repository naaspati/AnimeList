package sam.anime.db2;

import static sam.anime.db2.FilesMeta.FILE_NAME;
import static sam.anime.db2.FilesMeta.ID;
import static sam.anime.db2.FilesMeta.LAST_MODIFIED;
import static sam.anime.db2.FilesMeta.PARENT_ID;
import static sam.anime.db2.FilesMeta.SIZE;
import static sam.anime.db2.FilesMeta.TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import sam.collection.Iterable2;


public class FilesImpl {
	private final int id;
	private final int parent_id;
	private final String file_name;
	private final int size;
	private final int last_modified;

	public FilesImpl(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.parent_id = rs.getInt(PARENT_ID);
		this.file_name = rs.getString(FILE_NAME);
		this.size = rs.getInt(SIZE);
		this.last_modified = rs.getInt(LAST_MODIFIED);
	}
	public FilesImpl(int id, int parent_id, String file_name, int size, int last_modified){
		this.id = id;
		this.parent_id = parent_id;
		this.file_name = file_name;
		this.size = size;
		this.last_modified = last_modified;
	}


	public int getId(){ return this.id; }
	public int getParentId(){ return this.parent_id; }
	public String getFileName(){ return this.file_name; }
	public int getSize(){ return this.size; }
	public int getLastModified(){ return this.last_modified; }

	private static final String SELECT_ALL_SQL = "SELECT * FROM "+TABLE_NAME;
	public static List<FilesImpl> getAll(AnimeDB db) throws SQLException{
		return db.collectToList(SELECT_ALL_SQL, FilesImpl::new);
	}
	private static final String FIND_BY_ID = SELECT_ALL_SQL+" WHERE "+ID+"=";
	public static FilesImpl getById(AnimeDB db, int id) throws SQLException {
		return db.findFirst(FIND_BY_ID+id, FilesImpl::new);
	}

	private static final String INSERT_SQL = "INSERT INTO " + TABLE_NAME+"("+String.join(",", ID,PARENT_ID,FILE_NAME,SIZE,LAST_MODIFIED)+") VALUES(?,?,?,?,?)";

	public static final int insert(Iterable<FilesImpl> data, AnimeDB db) throws SQLException {
		Iterable2<FilesImpl> list = Iterable2.wrap(data);
		if(!list.hasNext()) return 0;

		int max = (int) (db.getSequnceValue(TABLE_NAME) + 1);

		try(PreparedStatement p = db.prepareStatement(INSERT_SQL)) {
			for (FilesImpl item: list){
				p.setInt(1,item.id < 0 ? max++ : item.id);
				p.setInt(2,item.parent_id);
				p.setString(3,item.file_name);
				p.setInt(4,item.size);
				p.setInt(5,item.last_modified);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}
}

