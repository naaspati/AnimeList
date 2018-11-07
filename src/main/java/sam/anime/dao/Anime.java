package sam.anime.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import sam.anime.dao.JikanApi.JikanResult;
import sam.anime.db2.AnimeDB;
import sam.anime.db2.AnimesImpl;
import sam.anime.db2.UrlsImpl;
import sam.myutils.MyUtilsException;
import sam.sql.SqlConsumer;

public class Anime extends AnimesImpl {
	Anime(ResultSet rs) throws SQLException {
		super(rs);
	}
	public Anime(JikanResult result) {
		super(result.mal_id, result.title, result.episodes, result.aired, String.join(", ", result.genres), result.synopsis);
	}

	private CustomSet<UrlsImpl> urls;

	public CustomSet<UrlsImpl> getUrls() {
		if (urls != null)
			return urls;
		return urls = list(current -> UrlsImpl.getByMalId(current, dao().getDb(), mal_id));
	}


	private <E> CustomSet<E> list(SqlConsumer<Set<E>> filler) {
		return MyUtilsException.noError(() -> {
			TreeSet<E> set = new TreeSet<>();
			filler.accept(set);
			CustomSet<E> list = new CustomSet<>(set, this);
			return list;
		});
	}

	private CustomSet<DirSubpath> dirs;

	public CustomSet<DirSubpath> getDirs() {
		if (dirs != null)
			return dirs;
		return dirs = list(set -> dao().getDirs(this, set));
	}

	private CustomSet<AnimeTitle> synonyms;

	public CustomSet<AnimeTitle> getTitleSynonyms() {
		if (synonyms != null)
			return synonyms;

		return synonyms = list(set -> dao().getTitles(this, set));
	}

	private AnimeDao dao() {
		return AnimeDao.getInstance();
	}

	private CustomSet<AnimeTitle> related;

	public CustomSet<AnimeTitle> getRelatedAnimes() {
		if (related != null)
			return related;
		return related = list(set -> dao().getRelatedTitles(this, set));
	}

	public static List<Anime> getAll(AnimeDB db) throws SQLException {
		return getAll(db, Anime::new);
	}
	public static Anime getByMalId(AnimeDB db, int mal_id) throws SQLException {
		return getByMalId(db, mal_id, Anime::new);
	}
}