package sam.anime.db.meta;

import sam.sql.querymaker.QueryMaker;

public interface TitleSynonymsMeta {
	String
	MAL_ID = "mal_id",
	TITLE_SYNONYMS = "title_synonyms",
	TABLE_NAME = "AltNames";
	
	String SELECT_BY_MAL_ID = QueryMaker.qm().select(TITLE_SYNONYMS).from(TABLE_NAME).where(w -> w.eq(MAL_ID, "", false)).build();
}
