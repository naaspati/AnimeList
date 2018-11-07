package sam.fxml;

import java.io.IOException;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import sam.anime.dao.Anime;
import sam.anime.dao.AnimeTitle;
import sam.fx.helpers.FxFxml;

public class OthersTab extends Tab {
	@FXML private VBox othersBox;
	@FXML private Text othersTitleT;
	@FXML private ListVBox<AnimeTitle> altNamesList;
	@FXML private DirList dirList;
	@FXML private UrlsList urlsList;

	@FXML private RelatedAnimesList relatedAnimesList;

	public OthersTab() throws IOException  {
		FxFxml.load(this, true);

		urlsList.setTitle("Urls");
		setDisable(true);

		setOnSelectionChanged(e -> load());
	}
	public void init(ObservableValue<Anime> animeProperty, DirSearch dirSearch) {
		animeProperty.addListener((p, o, n) -> currentAnime = n);
		dirList.init(dirSearch);
	}

	private Anime anime, currentAnime;
	
	private void load() {
		if(currentAnime == this.anime) return;
		
		this.anime = currentAnime;
		setDisable(anime == null);
		
		if(anime == null) {
			clear();
			return;
		}
		
		altNamesList.setSource(anime, anime.getTitleSynonyms());
		dirList.setSource(anime, anime.getDirs());
		urlsList.setSource(anime, anime.getUrls());
		relatedAnimesList.setSource(anime, anime.getRelatedAnimes());
	}
	
	public void clear() {
		altNamesList.setSource(null,null);
		dirList.setSource(null,null);
		urlsList.setSource(null,null);
		relatedAnimesList.setSource(null,null);
	}
	public StringProperty titleProperty() {
		System.out.println(othersTitleT);
		return othersTitleT.textProperty();
	}

	public DirList getDirsList() {
		return dirList;
	}
}
