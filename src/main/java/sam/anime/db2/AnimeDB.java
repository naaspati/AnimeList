package sam.anime.db2;

import java.sql.SQLException;
import java.util.logging.Logger;

import sam.logging.MyLoggerFactory;
import sam.myutils.System2;
import sam.sql.sqlite.SQLiteDB;

public class AnimeDB extends SQLiteDB {
	private static final Logger LOGGER = MyLoggerFactory.logger(AnimeDB.class.getSimpleName());
	
	private static volatile AnimeDB INSTANCE;
	
	public static AnimeDB getInstance() {
		return INSTANCE;
	}
	public static AnimeDB createInstance() throws SQLException {
		if (INSTANCE != null)
			return INSTANCE;

		synchronized (AnimeDB.class) {
			if (INSTANCE != null)
				return INSTANCE;

			INSTANCE = new AnimeDB();
			return INSTANCE;
		}
	}

	private AnimeDB() throws SQLException {
		super(dbPath());
		LOGGER.fine(() -> "STARTED: "+getClass());
	}
	public static String dbPath() {
		return System2.lookup("ANIME_DB");
	}
	
	public static void close0() throws SQLException {
		if(INSTANCE != null)
			INSTANCE.close();
	}
}
