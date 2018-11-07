package sam.anime.dao;

import java.util.Objects;

import sam.myutils.MyUtilsCheck;

public class IdParseResult {
	public final int id;
	public final String error;
	public final boolean failed;

	IdParseResult(int id, String error) {
		this.id = id;
		this.error = error;
		failed = error != null;
	}
	public IdParseResult(String error) {
		this(-1, Objects.requireNonNull(error));
	}
	public IdParseResult(int id) {
		this(id, null);
		MyUtilsCheck.checkArgument(id >= 0, "invalid id");
	}
	public boolean isFailed() {
		return failed;
	}
}
