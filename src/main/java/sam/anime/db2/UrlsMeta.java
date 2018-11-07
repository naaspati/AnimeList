package sam.anime.db2;



public interface UrlsMeta {
    String TABLE_NAME = "Urls";

    String ID = "_id";    // _id 	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE
    String MAL_ID = "mal_id";    // mal_id 	integer NOT NULL
    String URL = "url";    // url 	text NOT NULL


String CREATE_TABLE_SQL = "CREATE TABLE \"\"Urls\"\" (\n"+
"	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n"+
"	`mal_id`	integer NOT NULL,\n"+
"	`url`	text NOT NULL\n"+
")\n";

}