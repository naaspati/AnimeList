package sam.anime.db2;



public interface JIKANMeta {
    String TABLE_NAME = "JIKAN";

    String MAL_ID = "mal_id";    // mal_id  INTEGER NOT NULL UNIQUE PRIMARY KEY REFERENCES Animes(mal_id) on delete cascade
    String JSON = "json";    // json TEXT NOT NULL


String CREATE_TABLE_SQL = "CREATE TABLE JIKAN (\n"+
"  mal_id  INTEGER NOT NULL UNIQUE PRIMARY KEY REFERENCES Animes(mal_id) on delete cascade,\n"+
"  json TEXT NOT NULL  \n"+
")\n";

}