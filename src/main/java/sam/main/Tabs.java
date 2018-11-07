package sam.main;
import java.io.IOException;

import javafx.geometry.Side;
import javafx.scene.control.TabPane;
import sam.anime.dao.AnimeDao;
import sam.fx.helpers.FxUtils;
import sam.fxml.AboutDir;
import sam.fxml.AnimeSearch;
import sam.fxml.AnimeTab;
import sam.fxml.DirSearch;
import sam.fxml.OthersTab;

public class Tabs extends TabPane {
	private final AnimeTab animeTab;
	private final OthersTab othersTab;
	private final DirSearch dirSearch;
	private final AnimeSearch animeSearch;
	private final AboutDir aboutDir;

	public Tabs(AnimeDao dao) throws IOException {
		this.animeTab = new AnimeTab(dao);
		this.othersTab = new OthersTab();
		this.dirSearch = new DirSearch();
		this.animeSearch = new AnimeSearch();
		this.aboutDir = new AboutDir();
		
		animeTab.init(this);
		othersTab.init(animeTab.currentAnimeProperty(), dirSearch);
		animeSearch.init(dao, animeTab);
		dirSearch.init(dao, animeTab, animeSearch);
		aboutDir.init();

		getTabs()
		.setAll(animeTab,  
				othersTab,
				dirSearch,
				animeSearch, 
				aboutDir);

		setSide(Side.BOTTOM);

		animeTab.currentAnimeProperty().addListener(FxUtils.invalidationListener(i -> getSelectionModel().clearAndSelect(0)));
		setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
		othersTab.titleProperty().bind(animeTab.titleProperty());
	}
	public void stop() {
		animeTab.stop();
		dirSearch.stop();
	}
}
