package sam.anime.db.entities;

import static sam.anime.db.meta.FilesMeta.FILE_NAME;
import static sam.anime.db.meta.FilesMeta.ID;
import static sam.anime.db.meta.FilesMeta.PARENT_ID;
import static sam.anime.db.meta.FilesMeta.SIZE;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AnimeFile implements AnimeDirOrFile {
	private final int id;
	private final int parent_id;
	private final String file_name;
	private final int size;
	private AnimeDir parent;

	public AnimeFile(ResultSet rs) throws SQLException {
		this.id = rs.getInt(ID);
		this.parent_id = rs.getInt(PARENT_ID);
		this.file_name = rs.getString(FILE_NAME);
		this.size = rs.getInt(SIZE);
	}

	public int getId(){ return this.id; }
	public int getParentId(){ return this.parent_id; }
	public String getFileName(){ return this.file_name; }
	public int getSize(){ return this.size; }

	@Override
	public boolean isDir() {
		return false;
	}
	
	public AnimeDir getParent(){ return this.parent; }
	void setParent(AnimeDir parent){ this.parent=parent; }
}
