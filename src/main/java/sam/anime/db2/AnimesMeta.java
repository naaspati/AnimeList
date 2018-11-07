package sam.anime.db2;



public interface AnimesMeta {
    String TABLE_NAME = "Animes";

    String MAL_ID = "mal_id";    // mal_id  INTEGER NOT NULL UNIQUE PRIMARY KEY
    String TITLE = "title";    // title  TEXT NOT NULL UNIQUE
    String EPISODES = "episodes";    // episodes  TEXT
    String AIRED = "aired";    // aired  TEXT
    String GENRES = "genres";    // genres  TEXT
    String SYNOPSIS = "synopsis";    // synopsis TEXT


String CREATE_TABLE_SQL = "CREATE TABLE `Animes` (\n"+
"  mal_id  INTEGER NOT NULL UNIQUE PRIMARY KEY, \n"+
"  title  TEXT NOT NULL UNIQUE,\n"+
"  episodes  TEXT,\n"+
"  aired  TEXT,\n"+
"  genres  TEXT,\n"+
"  synopsis TEXT\n"+
")\n";

}