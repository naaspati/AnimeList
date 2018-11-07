package sam.anime.db2;



public interface DirsMeta {
    String TABLE_NAME = "Dirs";

    String ID = "_id";    // _id 	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE
    String MAL_ID = "mal_id";    // mal_id 	INTEGER NOT NULL DEFAULT -1
    String PARENT_ID = "parent_id";    // parent_id 	INTEGER NOT NULL DEFAULT -1
    String SUBPATH = "subpath";    // subpath 	TEXT NOT NULL UNIQUE
    String LAST_MODIFIED = "last_modified";    // last_modified 	INTEGER NOT NULL
    String TOTAL_SIZE = "total_size";    // total_size 	INTEGER NOT NULL DEFAULT 0
    String DIRS_COUNT = "dirs_count";    // dirs_count 	INTEGER NOT NULL DEFAULT 0
    String FILES_COUNT = "files_count";    // files_count 	INTEGER NOT NULL DEFAULT 0


String CREATE_TABLE_SQL = "CREATE TABLE \"\"Dirs\"\" (\n"+
"	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n"+
"	`mal_id`	INTEGER NOT NULL DEFAULT -1,\n"+
"	`parent_id`	INTEGER NOT NULL DEFAULT -1,\n"+
"	`subpath`	TEXT NOT NULL UNIQUE,\n"+
"	`last_modified`	INTEGER NOT NULL,\n"+
"	`total_size`	INTEGER NOT NULL DEFAULT 0,\n"+
"	`dirs_count`	INTEGER NOT NULL DEFAULT 0,\n"+
"	`files_count`	INTEGER NOT NULL DEFAULT 0\n"+
")\n";

}