package sam.main;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sam.anime.dao.AnimeDao;
import sam.fx.alert.FxAlert;
import sam.fx.popup.FxPopupShop;
import sam.myutils.System2;

public class App extends Application {
	private static Stage stage;
	public static final Path ANIME_DIR = Paths.get(System2.lookup("ANIME_DIR", ".") ).normalize().toAbsolutePath();
	private Tabs tabs;

	public static Stage getStage() {
		return stage;
	}
	
	private static final String VERSION = "0.025";
	
	@Override
	public void start(Stage stage) throws Exception {
		App.stage = stage;
		FxAlert.setParent(stage);
		FxPopupShop.setParent(stage);
		
		tabs = new Tabs(AnimeDao.createInstance());

		stage.setScene(new Scene(tabs));
		stage.setTitle("Anime List: ".concat(VERSION));
		stage.setWidth(350);
		stage.setHeight(450);
		stage.show();
	}

	@Override
	public void stop() throws Exception {
		tabs.stop();
		AnimeDao.getInstance().close();
		super.stop();
	}
}
