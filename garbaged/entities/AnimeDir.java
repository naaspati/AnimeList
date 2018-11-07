package sam.anime.db.entities;

import static sam.anime.db.meta.DirsMeta.ID;
import static sam.anime.db.meta.DirsMeta.LAST_MODIFIED;
import static sam.anime.db.meta.DirsMeta.MAL_ID;
import static sam.anime.db.meta.DirsMeta.PARENT_ID;
import static sam.anime.db.meta.DirsMeta.TABLE_NAME;
import static sam.anime.db.meta.DirsMeta.TOTAL_SIZE;
import static sam.myutils.MyUtilsException.noError;
import static sam.sql.querymaker.QueryMaker.qm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sam.anime.db.AnimeDB;
import sam.anime.db.meta.FilesMeta;

public class AnimeDir extends MinimalAnimeDir {
	public static final String SELECT_BY_MAL_ID_SQL = qm().selectAll().from(TABLE_NAME).where(w -> w.eq(MAL_ID, "", false)).build();
	
	private final int parent_id;
	private final long last_modified;
	private final long total_size;
	private List<AnimeDirOrFile> children;
	private AnimeDir parent;

	public AnimeDir(ResultSet rs) throws SQLException {
		super(rs);
		this.parent_id = rs.getInt(PARENT_ID);
		this.last_modified = rs.getLong(LAST_MODIFIED);
		this.total_size = rs.getLong(TOTAL_SIZE);
	}
	
	public int getParentId(){ return this.parent_id; }
	public long getLastModified(){ return this.last_modified; }

	@Override
	public int hashCode() {
		return id;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnimeDir other = (AnimeDir) obj;
		if(id < 0) 
			return subpath.equals(other.subpath);
		else
			return id == other.id;
	}
	public List<AnimeDirOrFile> getChildren(AnimeDB db) {
		if(children != null) return children;
		children = new ArrayList<>();

		noError(() -> {
			db.collect(qm().selectAll().from(TABLE_NAME).where(w -> w.eq(PARENT_ID, id)).build(), children, AnimeDir::new);
			db.collect(qm().selectAll().from(FilesMeta.TABLE_NAME).where(w -> w.eq(FilesMeta.PARENT_ID, id)).build(), children, AnimeFile::new);
			return null;
		});
		children.forEach(f -> {
			if(f.isDir())
				((AnimeDir)f).setParent(this); 
			else
				((AnimeFile)f).setParent(this);	
		});
		return children;
	}

	private static final String ONE_SELECT = qm().selectAll().from(TABLE_NAME).where(w -> w.eq(ID, "", false)).build();

	public AnimeDir getParent(AnimeDB db){
		if(parent != null || parent_id < 0) return parent;
		parent = noError(() -> db.executeQuery(ONE_SELECT+parent_id, AnimeDir::new));
		return this.parent; 
	}
	void setParent(AnimeDir parent){ this.parent=parent; }
	public long getTotalSize() { return total_size; }

	@Override
	public boolean isDir() {
		return true;
	}

}
