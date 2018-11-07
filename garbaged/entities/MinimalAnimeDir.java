package sam.anime.db.entities;

import static sam.anime.db.meta.DirsMeta.ID;
import static sam.anime.db.meta.DirsMeta.MAL_ID;
import static sam.anime.db.meta.DirsMeta.SUBPATH;
import static sam.anime.db.meta.DirsMeta.TABLE_NAME;
import static sam.sql.querymaker.QueryMaker.qm;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MinimalAnimeDir implements AnimeDirOrFile {
	public static final String SELECT_BY_MAL_ID_SQL = qm().select(columns()).from(TABLE_NAME).where(w -> w.eq(MAL_ID, "", false)).build();
	
	final int mal_id;
	final int id;
	final String subpath;
	
	public static final String[] columns() {
		return new String[] {MAL_ID, ID, SUBPATH};
	}
	
	public MinimalAnimeDir(ResultSet rs) throws SQLException {
		this.mal_id = rs.getInt(MAL_ID);
		this.id = rs.getInt(ID);
		this.subpath = rs.getString(SUBPATH);
	}
	public MinimalAnimeDir(int mal_id, int id, String subpath) {
		this.mal_id = mal_id;
		this.id = id;
		this.subpath = subpath;
	}
	
	public int getMalId(){ return this.mal_id; }
	public int getId(){ return this.id; }
	public String getSubpath(){ return this.subpath; }

	@Override
	public boolean isDir() {
		return true;
	}

}
