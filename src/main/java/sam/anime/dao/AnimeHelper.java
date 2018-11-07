package sam.anime.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import sam.anime.db2.AnimeDB;
import sam.anime.db2.RelatedAnimesImpl;
import sam.logging.MyLoggerFactory;

class AnimeHelper {
	private static final Logger LOGGER = MyLoggerFactory.logger(AnimeHelper.class.getSimpleName());
	
	private final HashMap<Integer, Anime> cachedAnime = new HashMap<>();
	private final UnmodifiableCustomCollection<AnimeTitle> allTitles;
	private final List<Anime> newAnime = new ArrayList<>();
	private final TreeSet<RelatedAnimesImpl> relatedAnimes = new TreeSet<>(), relatedAnimesNew = new TreeSet<>();
	
	public AnimeHelper(AnimeDB db) throws SQLException {
		allTitles = new UnmodifiableCustomCollection<>(AnimeTitle.getAll(db), null);
		RelatedAnimesImpl.getAll(db, relatedAnimes);
	}
	void put(int mal_id, Anime anime) {
		cachedAnime.put(mal_id, anime);
	}

	void save(AnimeDB db) throws SQLException {
		if(!newAnime.isEmpty())
			Anime.insert(newAnime, db);
		
		if(!relatedAnimesNew.isEmpty())
			RelatedAnimesImpl.insert(relatedAnimesNew, db);
	}
	public void getTitles(Anime anime, Collection<AnimeTitle> sink) {
		int malId = anime.getMalId();
		for (AnimeTitle d : allTitles) {
			if(d.getMalId() == malId)
				sink.add(d);
		}
	}
	public void getRelatedTitles(Anime anime, Collection<AnimeTitle> sink) {
		int mal_id = anime.mal_id;
		TreeSet<RelatedAnimesImpl> result = new TreeSet<>();

		for (RelatedAnimesImpl d : relatedAnimes) {
			if(d.id1 == mal_id || d.id2 == mal_id)
				result.add(d);
		}
		if(result.isEmpty()) 
			return;

		List<RelatedAnimesImpl> found = new ArrayList<>();
		while(true) {
			found.clear();

			for (RelatedAnimesImpl d : relatedAnimes) {
				if(result.contains(d))
					continue;
				for (RelatedAnimesImpl f : result) {
					if(d == f)
						continue;
					if(d.id1 == f.id1 || d.id2 == f.id2 || d.id1 == d.id2)
						found.add(d);
				}
			}
			if(found.isEmpty())
				break;
			result.addAll(found);
		}
		map(mal_id, result, sink);
	}

	private void map(int mal_id, TreeSet<RelatedAnimesImpl> list, Collection<AnimeTitle> sink) {
		int[] n = list.stream()
				.flatMapToInt(d -> IntStream.of(d.id1, d.id2))
				.distinct()
				.sorted()
				.toArray();

		for (AnimeTitle t : allTitles) {
			if(t.mal_id != mal_id && Arrays.binarySearch(n, t.mal_id) >= 0)
				sink.add(t);
		}

	}
	
	public UnmodifiableCustomCollection<AnimeTitle> allAnimeTitle() {
		return allTitles;
	}
	void newAnime(Anime anime) {
		newAnime.add(anime);
		cachedAnime.put(anime.mal_id, anime);

		int size = allTitles.size();
		allTitles.add0(new AnimeTitle(anime));
		anime.getTitleSynonyms().forEach(e -> allTitles.add0(e));
		
		LOGGER.fine(() -> "allMinimalAnimes changes: "+size+" -> "+allTitles.size());
	}
	
	public void animeAreRelated(Anime a, Anime a2) {
		a.getRelatedAnimes().add(new AnimeTitle(a2));
		a.getRelatedAnimes().add(new AnimeTitle(a));

		RelatedAnimesImpl r = new RelatedAnimesImpl(a.mal_id, a2.mal_id);
		if(relatedAnimes.add(r))
			relatedAnimesNew.add(r);
	}
	public Anime getCachedAnime(int malId) {
		return cachedAnime.get(malId);
	}

}
