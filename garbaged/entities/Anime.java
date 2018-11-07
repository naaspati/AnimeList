package sam.anime.db.entities;

import static sam.anime.db.meta.AnimesMeta.AIRED;
import static sam.anime.db.meta.AnimesMeta.EPISODES;
import static sam.anime.db.meta.AnimesMeta.GENRES;
import static sam.anime.db.meta.AnimesMeta.MAL_ID;
import static sam.anime.db.meta.AnimesMeta.SYNOPSIS;
import static sam.anime.db.meta.AnimesMeta.TABLE_NAME;
import static sam.sql.querymaker.QueryMaker.qm;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.IntStream;

import sam.anime.db.AnimeDB;
import sam.anime.db.meta.RelatedAnimesMeta;
import sam.anime.db.meta.TitleSynonymsMeta;
import sam.anime.db.meta.UrlsMeta;
import sam.myutils.MyUtilsException;
import sam.sql.SqlFunction;

public class Anime extends MinimalAnime {
	public static final String SELECT_BY_MAL_ID = qm().selectAll().from(TABLE_NAME).where(w -> w.eq(MAL_ID, "", false)).build();

	private String episodes;
	private String aired;
	private String genre;
	private String synopsis;

	private String jikanJson;

	private AnimeList<String> title_synonyms;
	private AnimeList<MinimalAnimeDir> dirs;
	private AnimeList<String> urls;
	private AnimeList<MinimalAnime> relatedAnimes;

	public Anime(int mal_id) {
		super(mal_id, null);
	}
	public Anime(ResultSet rs) throws SQLException {
		super(rs);
		this.episodes = rs.getString(EPISODES);
		this.aired = rs.getString(AIRED);
		this.genre = rs.getString(GENRES);
		this.synopsis = rs.getString(SYNOPSIS);
	}
	protected <E> List<E> getter(String selectByMalIDSQL, SqlFunction<ResultSet, E> mapper, AnimeDB db) {
		return MyUtilsException.noError(() -> db.collectToList(selectByMalIDSQL+mal_id, mapper));
	}

	@Override
	public void setTitle(String title) {
		super.setTitle(title);
	}
	public AnimeList<MinimalAnimeDir> getDirs(){
		if(dirs != null) return dirs;
		this.dirs = new AnimeList<>(mal_id, this::getDirs);
		return this.dirs; 
	}
	protected List<MinimalAnimeDir> getDirs(AnimeDB db){
		return getter(MinimalAnimeDir.SELECT_BY_MAL_ID_SQL, AnimeDir::new, db);
	}
	public AnimeList<String> getTitleSynonyms(){
		if(title_synonyms != null) return title_synonyms;
		this.title_synonyms = new AnimeList<>(mal_id, this::getTitleSynonyms);
		return this.title_synonyms; 
	}
	protected List<String> getTitleSynonyms(AnimeDB db){
		return getter(TitleSynonymsMeta.SELECT_BY_MAL_ID, rs -> rs.getString(1), db);
	}
	
	public AnimeList<String> getUrls(){ 
		if(urls != null) return urls;
		this.urls = new AnimeList<>(mal_id, this::getUrls);
		return this.urls; 
	}
	protected List<String> getUrls(AnimeDB db){
		return getter(UrlsMeta.SELECT_BY_MAL_ID, rs -> rs.getString(1), db);
	}
	public AnimeList<MinimalAnime> getRelatedAnimes() {
		if(relatedAnimes != null) return relatedAnimes;
		this.relatedAnimes = new AnimeList<>(mal_id, this::relatedAnimesFill);
		return this.relatedAnimes; 
	}
	protected int[] getRelatedAnimeMalIds(AnimeDB db) {
		String sql = qm().selectAll().from(RelatedAnimesMeta.TABLE_NAME).where(w -> w.eq(RelatedAnimesMeta.ID1, mal_id).or().eq(RelatedAnimesMeta.ID2, mal_id)).build();
		return MyUtilsException.noError(() -> db.stream(sql, rs -> IntStream.of(rs.getInt(1), rs.getInt(2))).flatMapToInt(i -> i).distinct().filter(id -> id != mal_id).toArray());
	}
	protected List<MinimalAnime> relatedAnimesFill(AnimeDB db) {
		return MyUtilsException.noError(() -> db.collectToList(qm().select(MinimalAnime.columns()).from(TABLE_NAME).where(w -> w.in(MAL_ID, getRelatedAnimeMalIds(db))).build(), MinimalAnime::new));
	}

	public String getEpisodes(){ return this.episodes; }
	public void setEpisodes(String episodes){ this.episodes=episodes; }

	public String getAired(){ return this.aired; }
	public void setAired(String aired){ this.aired=aired; }

	public String getGenre(){ return this.genre; }
	public void setGenre(String genre){ this.genre=genre; }

	public String getSynopsis(){ return this.synopsis; }
	public void setSynopsis(String synopsis){ this.synopsis=synopsis; }

	public String getJikanJson(){ return this.jikanJson; }
	public void setJikanJson(String jikanJson){ this.jikanJson=jikanJson; }

	@Override
	public String toString() {
		return "Anime [mal_id=" + mal_id + ", title=" + getTitle() + ", episodes=" + episodes + ", aired=" + aired
				+ ", genre=" + genre + ", synopsis=" + synopsis + ", jikanJson=" + jikanJson 
				+ ", title_synonyms=" + title_synonyms + ", dirs=" + dirs + ", links=" + urls + "]";
	}
}
