package sam.anime.db2;



public interface RelatedAnimesMeta {
    String TABLE_NAME = "RelatedAnimes";

    String ID = "_id";    // _id 	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE
    String ID1 = "id1";    // id1 	INTEGER NOT NULL
    String ID2 = "id2";    // id2 	INTEGER NOT NULL


String CREATE_TABLE_SQL = "CREATE TABLE \"\"RelatedAnimes\"\" (\n"+
"	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n"+
"	`id1`	INTEGER NOT NULL,\n"+
"	`id2`	INTEGER NOT NULL\n"+
")\n";

}