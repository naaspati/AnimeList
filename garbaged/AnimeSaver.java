package sam.anime.dao;


import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sam.anime.db2.AnimeDB;
import sam.anime.db2.DirsMeta;
import sam.anime.db2.RelatedAnimesMeta;
import sam.anime.db2.UrlsMeta;
import sam.logging.MyLoggerFactory;
import sam.myutils.MyUtilsCheck;  

class AnimeSaver {
	private static final Logger LOGGER = MyLoggerFactory.logger(AnimeSaver.class.getSimpleName());

	private final Collection<Anime> allAnimes;
	private final AnimeDB db;

	public AnimeSaver(Collection<Anime> allAnimes, AnimeDB db) {
		this.allAnimes = allAnimes;
		this.db = db;
	}

	public void start() throws SQLException {
		saveNewAnimes();
		saveDirs();
		saveAltNames();
		saveUrls();
		saveRelatedAnimes();
	}

	private void saveNewAnimes() throws SQLException {
		List<Anime> newAnimes = allAnimes.stream().filter(Anime::isNew).collect(Collectors.toList());

		if(!newAnimes.isEmpty()) {
			int[] n = Anime.insert(newAnimes, db);
			
			for (Anime a : newAnimes)
				LOGGER.fine(() -> "NEW ANIME: "+a.getMalId() +" -> "+a.getTitle());
			
			LOGGER.info("NEW ANIME executes: "+n[0]);
			LOGGER.info("NEW JIKAN_JSON executes: "+n[1]);
		}
	}

	private void saveRelatedAnimes() throws SQLException {
		execute(a -> a.getRelatedAnimes().getList(), RelatedAnimesMeta.TABLE_NAME, RelatedAnimesMeta.ID1, RelatedAnimesMeta.ID2, m -> m.toString());		
	}
	private void saveAltNames() throws SQLException {
		execute(Anime::getTitleSynonyms2, TitleSynonymsMeta.TABLE_NAME, TitleSynonymsMeta.MAL_ID,TitleSynonymsMeta.TITLE_SYNONYMS);
	}
	private void saveUrls() throws SQLException {
		execute(Anime::getUrls, UrlsMeta.TABLE_NAME, UrlsMeta.MAL_ID, UrlsMeta.URL);
	}
	private <E> void execute(Function<Anime , LazyList<E>> mapper, String tableName, String malIdColumn, String column2) throws SQLException {
		execute(mapper, tableName, malIdColumn, column2, E::toString);
	}
	private <E> void execute(Function<Anime , LazyList<E>> mapper, String tableName, String malIdColumn, String column2, Function<E, String> toValue) throws SQLException {
		execute("Delete from "+tableName, mapper, LazyList::getRemoved, 
				qm().deleteFrom(tableName)
				.where(w -> w.eqPlaceholder(malIdColumn).and().eqPlaceholder(column2)).build()
				, toValue);

		execute("Insert into "+tableName, mapper, LazyList::getAdded, 
				qm().insertInto(tableName)
				.placeholders(malIdColumn,column2)
				, toValue);
	}
	
	private <E> void execute(String msg, Function<Anime , LazyList<E>> mapper, Function<LazyList<E>, List<E>> mapper2, String sql, Function<E, String> toValue) throws SQLException {
		List<Temp<E>> list =  stream(mapper, mapper2);
		if(list.isEmpty()) return;

		db.prepareStatementBlock(sql, 
				ps -> {
					for (Temp<E> temp : list) {
						ps.setInt(1, temp.mal_id);
						String v = toValue.apply(temp.value);
						ps.setString(2, v);
						LOGGER.fine(() -> msg+": "+temp.mal_id+" -> "+v);
						ps.addBatch();
					}
					LOGGER.info(msg+" executes: "+ps.executeBatch().length); 
					return null;
				});
	}

	private class Temp<E> {
		private int mal_id;
		private E value;

		public Temp(int mal_id, E value) {
			this.mal_id = mal_id;
			this.value = value;
		}
	} 


	private <E> List<Temp<E>> stream(Function<Anime , LazyList<E>> mapper, Function<LazyList<E>, List<E>> mapper2) {
		return stream(mapper)
				.flatMap(l -> {
					List<E> list = mapper2.apply(l);
					if(MyUtilsCheck.isEmpty(list))
						return Stream.empty();
					return list.stream().map(s -> new Temp<>(l.mal_id, s));
				}).collect(Collectors.toList());
	}

	private <E> Stream<LazyList<E>> stream(Function<Anime , LazyList<E>> mapper) {
		return allAnimes.stream()
				.map(mapper)
				.filter(LazyList::isModified);
	}

	private void saveDirs() throws SQLException {
		DirList2.touched.removeIf(d -> !d.isModified());
		 
		if(DirList2.touched.isEmpty()) return;
		
		db.prepareStatementBlock(qm()
						.update(DirsMeta.TABLE_NAME)
						.placeholders(DirsMeta.MAL_ID)
						.where(w -> w.eqPlaceholder(DirsMeta.ID))
						.build(), ps -> {
							for (MinimalAnimeDir2 d : DirList2.touched) {
								ps.setInt(1, d.getMalId());
								ps.setInt(2, d.getId());
								LOGGER.fine(() -> "DIR set: "+d.getId()+" -> "+d.getMalId());
								ps.addBatch();
							}
							LOGGER.info("Directories update executes: "+ps.executeBatch().length); 
							return null;
						});
	}



}
