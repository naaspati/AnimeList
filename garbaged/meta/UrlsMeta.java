package sam.anime.db.meta;

import sam.sql.querymaker.QueryMaker;

public interface UrlsMeta {
	String TABLE_NAME = "Urls",
			
			MAL_ID = "mal_id",
			URL = "url";
	
	
	String SELECT_BY_MAL_ID = QueryMaker.qm().select(URL).from(TABLE_NAME).where(w -> w.eq(MAL_ID, "", false)).build();;

}
