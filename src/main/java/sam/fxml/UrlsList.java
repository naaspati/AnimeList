package sam.fxml;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

import sam.anime.db2.UrlsImpl;
import sam.fx.alert.FxAlert;
import sam.myutils.MyUtilsCheck;

public class UrlsList extends StringList<UrlsImpl> {
	@Override
	protected UrlsImpl create(String url) {
		if(MyUtilsCheck.isEmptyTrimmed(url)) return null;
		url = url.trim();
		try {
			if(Pattern.compile("\\s+").matcher(url).find())
				throw new MalformedURLException("space charater found in url");
			new URL(url);
		} catch (MalformedURLException e) {
			FxAlert.showErrorDialog(null, "bad url", e);
			return null;
		}
		return new UrlsImpl(url);
	}
}
