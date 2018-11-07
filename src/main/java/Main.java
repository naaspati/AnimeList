import java.io.File;
import java.io.IOException;
import java.lang.management.RuntimeMXBean;

import javax.management.MXBean;

import javafx.application.Application;
import sam.main.App;
import sam.updater.Updater;

public class Main {

	public static void main(String[] args) throws IOException {
		if(args.length > 0 && "-u".equalsIgnoreCase(args[0]) && "--update".equalsIgnoreCase(args[0]))
			Updater.start();
		else
			Application.launch(App.class, args);
	}

}
