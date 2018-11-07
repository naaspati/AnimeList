package sam.anime.db2;



public interface FilesMeta {
    String TABLE_NAME = "Files";

    String ID = "_id";    // _id 	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE
    String PARENT_ID = "parent_id";    // parent_id 	INTEGER NOT NULL
    String FILE_NAME = "file_name";    // file_name 	TEXT NOT NULL
    String SIZE = "_size";    // _size 	INTEGER NOT NULL
    String LAST_MODIFIED = "last_modified";    // last_modified 	INTEGER DEFAULT 0


String CREATE_TABLE_SQL = "CREATE TABLE \"\"Files\"\" (\n"+
"	`_id`	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,\n"+
"	`parent_id`	INTEGER NOT NULL,\n"+
"	`file_name`	TEXT NOT NULL,\n"+
"	`_size`	INTEGER NOT NULL,\n"+
"	`last_modified`	INTEGER DEFAULT 0\n"+
")\n";

}