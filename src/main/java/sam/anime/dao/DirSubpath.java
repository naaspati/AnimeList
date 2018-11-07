package sam.anime.dao;

import static sam.anime.db2.DirsMeta.ID;
import static sam.anime.db2.DirsMeta.MAL_ID;
import static sam.anime.db2.DirsMeta.SUBPATH;
import static sam.anime.db2.DirsMeta.TABLE_NAME;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sam.anime.db2.AnimeDB;

public class DirSubpath implements Comparable<DirSubpath> {
	public final String subpath, lowercased;
	private final int mal_id;
	private int new_mal_id;
	private final int id;

	public DirSubpath(ResultSet rs) throws SQLException {
		this.subpath = rs.getString(SUBPATH);
		this.lowercased = subpath.toLowerCase();
		this.mal_id = rs.getInt(MAL_ID);
		this.id = rs.getInt(ID);
		this.new_mal_id = mal_id;
	}
	void setMalId(int malId) {
		this.new_mal_id = malId;
	}
	public int getMalId() {
		return new_mal_id;
	}
	
	boolean  isModified() {
		return new_mal_id != mal_id;
	}
	
	private final static String SELECT_ALL = "SELECT "+String.join(",", MAL_ID, ID, SUBPATH)+" FROM "+TABLE_NAME;
	
	static ArrayList<DirSubpath> getAll(AnimeDB db) throws SQLException {
		return db.collectToList(SELECT_ALL, DirSubpath::new);
	}
	@Override
	public int compareTo(DirSubpath o) {
		return Integer.compare(this.id, o.id);
	}

	private static final String UPDATE_SQL = "UPDATE "+TABLE_NAME+" SET "+MAL_ID+"=? WHERE "+ID+"=?";
	
	public static int updateMalId(List<DirSubpath> list, AnimeDB db) throws SQLException {
		if(list.isEmpty()) return 0;
		
		try(PreparedStatement p = db.prepareStatement(UPDATE_SQL)) {
			for (DirSubpath d : list) {
				p.setInt(1, d.new_mal_id);
				p.setInt(2, d.id);
				p.addBatch();
			}
			return p.executeBatch().length;
		}
	}

	/*
	 * 	public static List<DirSubpath> collect(List<DirSubpath> sink, AnimeDB db) throws SQLException {
		String sql = SELECT_ALL;
		
		if(!sink.isEmpty()) {
			StringBuilder sb = new StringBuilder(100).append(sql);
			
			sb.append(" WHERE NOT ").append(ID)
			.append(" IN(");
			sink.forEach(s -> sb.append(Integer.toString(s.id)).append(','));
			sb.setLength(sb.length() - 1);
			sb.append(')');
			
			sql = sb.toString();
					
		}
		db.collect(sql, sink, DirSubpath::new);
		return sink;
	}
	private static final String FIND_BY_MAL_ID = SELECT_ALL+" WHERE "+MAL_ID+"=";
	public static List<DirSubpath> getByMalId(int malId, AnimeDB db) throws SQLException {
		return db.collectToList(FIND_BY_MAL_ID+malId, DirSubpath::new);
	}
	 */

}