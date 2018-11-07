package sam.anime.dao;

import static sam.myutils.MyUtilsCheck.isEmptyTrimmed;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javafx.application.Platform;
import sam.anime.dao.JikanApi.JikanResult;
import sam.anime.db2.AnimeDB;
import sam.anime.db2.JIKANImpl;
import sam.fx.alert.FxAlert;
import sam.logging.MyLoggerFactory;
import sam.myutils.System2;

public class AnimeDao implements AutoCloseable {

	private static volatile AnimeDao INSTANCE;

	public static AnimeDao getInstance() {
		return INSTANCE;
	}
	public static AnimeDao createInstance() throws SQLException {
		return INSTANCE != null ? INSTANCE : (INSTANCE =  new AnimeDao());
	}

	private final AnimeDB db;
	private ExecutorService SINGLE_THREAD;
	private final AtomicBoolean stopping = new AtomicBoolean(false);
	private static final Logger LOGGER = MyLoggerFactory.logger(AnimeDao.class.getSimpleName());
	private final DirHelper dirHelper;
	private final AnimeHelper animeHelper;

	private AnimeDao() throws SQLException {
		db = AnimeDB.createInstance();
		animeHelper = new AnimeHelper(db);
		dirHelper = new DirHelper(db, this);
	}

	private static final Pattern ISNUMER = Pattern.compile("\\d+");

	public void loadAnime(int mal_id, BiConsumer<Anime, Throwable> oncomplete) {
		if(stopping.get()) {
			oncomplete.accept(null, new IllegalStateException("thread is stopping"));
			return;
		}

		Anime anime = getCachedAnime(mal_id);
		if(anime != null) {
			LOGGER.fine(() -> "Loaded from cache: "+mal_id);
			oncomplete.accept(anime, null);
			return;
		}

		try {
			anime = Anime.getByMalId(db, mal_id);
		} catch (Exception e1) {
			oncomplete.accept(null, e1);
			return;
		}

		if(SINGLE_THREAD == null)
			SINGLE_THREAD = Executors.newSingleThreadExecutor();

		if(anime != null) {
			animeHelper.put(mal_id, anime);
			LOGGER.fine(() -> "Loaded from DB: "+mal_id);
			oncomplete.accept(anime, null);
			return;
		}
		LOGGER.info(() -> "anime not found in db: "+mal_id);

		SINGLE_THREAD.execute(() -> {
			try {
				if(stopping.get()) {
					oncomplete.accept(null, new IllegalStateException("thread is stopping"));
					return;
				}
				JikanApi jikan = new JikanApi(mal_id);
				JikanResult result = jikan.call();
				Platform.runLater(() -> processNewMangas(result, oncomplete));
				Thread.sleep(3000); // 3 sec delay is recommended by API
			} catch (Exception e) {
				oncomplete.accept(null, e);
			}
		});
	}

	private final String MAL_START = System2.lookup("MAL_START", "https://myanimelist.net/anime/"); 

	public IdParseResult toId(String s) {
		if(isEmptyTrimmed(s)) 
			return new IdParseResult("empty input");

		s = s.trim();

		if(s.startsWith(MAL_START)) {
			int n = s.indexOf('/', MAL_START.length() + 1);
			s = MAL_START.length() == s.length() ? "" : s.substring(MAL_START.length(), n < 0 ? s.length() : n);
		}
		if(!ISNUMER.matcher(s).matches()) return new IdParseResult("invalid input: "+s);
		return new IdParseResult(Integer.parseInt(s.trim()));
	}
	public void stop() {
		stopping.set(true);
		if(SINGLE_THREAD != null)
			SINGLE_THREAD.shutdownNow();
	}
	public Executor executor() {
		return SINGLE_THREAD;
	}
	public boolean isStopping() {
		return stopping.get();
	}
	private void save() throws SQLException {
		/**
		 * instead of AnimeSaver, finish it in this method
		 * new AnimeSaver(Collections.unmodifiableCollection(cachedAnime.values()), db)
		.start();
		 */
		
		animeHelper.save(db);
		dirHelper.save(db);
		
		db.commit();

	}
	@Override
	public void close() throws Exception {
		save();
		db.close();
	}


	private void processNewMangas(JikanResult result, BiConsumer<Anime, Throwable> oncomplete) {
		if(result == null) {
			oncomplete.accept(null, null);
			return;
		}

		Anime anime = new Anime(result);
		
		dirHelper.newAnime(anime);
		animeHelper.newAnime(anime);
		
		try {
			JIKANImpl.insert(result.mal_id, result.jikanJson, db);
		} catch (SQLException e1) {
			FxAlert.showErrorDialog(null, "failed to save JikanJson", e1);
		}
	}
	AnimeDB getDb() {
		return db;
	}

	/* --------------------------------------------------
	 *                       DirHelper
	 *  --------------------------------------------------
	 * TODO just a marker
	 */

	public void addDir(AnimeTitle title, DirSubpath dir) {
		dirHelper.addDir(title, dir);
	}
	public void hideDir(DirSubpath dir) {
		dirHelper.hideDir(dir);
	}
	public void getDirs(Anime anime, Set<DirSubpath> sink) {
		dirHelper.getDirs(anime, sink);
	}
	public UnmodifiableCustomCollection<DirSubpath> getAllDirs() {
		return dirHelper.getAllDirs();
	}
	
	/* --------------------------------------------------
	 *                       AnimeHelper
	 *  --------------------------------------------------
	 * TODO just a marker
	 */
	
	public UnmodifiableCustomCollection<AnimeTitle> allAnimeTitle() {
		return animeHelper.allAnimeTitle();
	}
	public void animeAreRelated(Anime a, Anime a2) {
		animeHelper.animeAreRelated(a, a2);
	}
	public Anime getCachedAnime(int malId) {
		return animeHelper.getCachedAnime(malId);
	}
	public void getTitles(Anime anime, Collection<AnimeTitle> sink) {
		animeHelper.getTitles(anime, sink);
	}
	public void getRelatedTitles(Anime anime, Collection<AnimeTitle> sink) {
		animeHelper.getRelatedTitles(anime, sink);
	}
}
