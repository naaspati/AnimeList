package sam.anime.dao;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import sam.anime.dao.JikanApi.JikanResult;
import sam.logging.MyLoggerFactory;

class JikanApi implements Callable<JikanResult> {
	private static final Logger LOGGER = MyLoggerFactory.logger(JikanApi.class.getSimpleName());
	
	private final int mal_id;
	
	public JikanApi(int mal_id) {
		this.mal_id = mal_id;
	}

	public static class JikanResult {
		public String jikanJson, title, synopsis, episodes, aired;
		public int mal_id;
		public List<String> title_synonyms;
		public String[] genres;
	}
	
	private JSONObject json;
	
	@Override
	public JikanResult call() throws Exception {
		LOGGER.fine(() -> "loading json for: "+mal_id);
		HttpURLConnection con = (HttpURLConnection) new URL("https://api.jikan.moe/v3/anime/"+mal_id).openConnection();
		
		JikanResult result = new JikanResult();

		result.jikanJson = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))
				.lines()
				.collect(Collectors.joining(""));

		json = new JSONObject(result.jikanJson);
		result.title = value("title");
		result.synopsis = value("synopsis")+Optional.ofNullable(value("background")).map(s -> "\n\n"+s).orElse(""); 

		JSONArray array = json.getJSONArray("title_synonyms");
		if(array != null && array.length() != 0) {
			result.title_synonyms = new ArrayList<>(); 	
			
			int length = array.length();
			for (int i = 0; i < length; i++)
				result.title_synonyms.add(array.getString(i));
		}
		
		String t = value("title_english");
		if(t != null && !t.equalsIgnoreCase(result.title)) {
			if(result.title_synonyms == null)
				result.title_synonyms = Collections.singletonList(t);
			else
				result.title_synonyms.add(t);
		}
		
		array = json.getJSONArray("genres");
		if(array != null && array.length() != 0) {
			String[] s = new String[array.length()];

			for (int i = 0; i < s.length; i++)
				s[i] = array.getJSONObject(i).getString("name");
			
			result.genres = s;
		}
		
		result.episodes = value("episodes");
		result.aired = json.getJSONObject("aired").getString("string");

		LOGGER.fine(() -> "loaded json for: "+mal_id);
		return result;
	}
	private String value(String key) {
		if(json.isNull(key))
			return null;
		return String.valueOf(json.get(key));
	}


}
