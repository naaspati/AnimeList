package sam.anime.db2;



public interface AltNamesMeta {
    String TABLE_NAME = "AltNames";

    String ID = "_id";    // _id 	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE
    String MAL_ID = "mal_id";    // mal_id 	INTEGER NOT NULL
    String TITLE_SYNONYMS = "title_synonyms";    // title_synonyms 	TEXT NOT NULL UNIQUE


String CREATE_TABLE_SQL = "CREATE TABLE \"\"AltNames\"\" (\n"+
"	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n"+
"	`mal_id`	INTEGER NOT NULL,\n"+
"	`title_synonyms`	TEXT NOT NULL UNIQUE,\n"+
"	FOREIGN KEY(`mal_id`) REFERENCES `Animes`(`mal_id`) on delete cascade\n"+
")\n";

}