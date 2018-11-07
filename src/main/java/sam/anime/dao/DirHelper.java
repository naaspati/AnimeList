package sam.anime.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sam.anime.db2.AnimeDB;

class DirHelper implements ModificationListener<DirSubpath> {
	final UnmodifiableCustomCollection<DirSubpath> allDirs;
	private final AnimeDao dao;
	
	public DirHelper(AnimeDB db, AnimeDao dao) throws SQLException {
		allDirs = new UnmodifiableCustomCollection<>(DirSubpath.getAll(db), null);
		this.dao = dao;
	}
	public void addDir(AnimeTitle title, DirSubpath dir) {
		int mal_id = title.mal_id;
		if(dir.getMalId() == mal_id) return;
		
		Anime nnew = dao.getCachedAnime(mal_id);
		
		if(nnew == null) 
			dir.setMalId(mal_id);
		 else 
			nnew.getDirs().add(dir);
	}
	public void hideDir(DirSubpath dir) {
		if(dir.getMalId() == -2)
			return;
		if(dir.getMalId() >= 0) {
			Anime old = dao.getCachedAnime(dir.getMalId());
			if(old != null)
				old.getDirs().remove(dir);
		}
		dir.setMalId(-2);
	}

	public void getDirs(Anime anime, Set<DirSubpath> sink) {
		int malId = anime.getMalId();
		for (DirSubpath d : allDirs) {
			if(d.getMalId() == malId)
				sink.add(d);
		}
	}
	public Stream<DirSubpath> stream() {
		return allDirs.stream();
	}
	public UnmodifiableCustomCollection<DirSubpath> getAllDirs() {
		return allDirs;
	}
	void newAnime(Anime anime) {
		anime.getDirs().addListener(this);
	}
	@Override
	public void onModify(Anime anime, DirSubpath item, Type type) {
		if(type == Type.ADDED)
			item.setMalId(anime.mal_id);
		else if(type == Type.REMOVED)
			item.setMalId(-1);
		
	}

	public void save(AnimeDB db) throws SQLException {
		List<DirSubpath> list = stream().filter(DirSubpath::isModified).collect(Collectors.toList());
		if(!list.isEmpty()) 
			DirSubpath.updateMalId(list, db);
	}
}
