package sam.anime.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import sam.fxml.DirSearch.DirSubpath;
import sam.myutils.MyUtilsCheck;

public class DirList2 extends List2<DirSubpath> {
	static Set<DirSubpath> touched = new HashSet<>();

	public DirList2(int mal_id) {
		super(mal_id);
	}
	public DirList2(List<DirSubpath> list, int mal_id) {
		this(mal_id);
		if(MyUtilsCheck.isNotEmpty(list))
			touched.addAll(list);
	}
	@Override
	public void add(DirSubpath e) {
		touched.add(e);
		e.setMalId(mal_id);
	}
	@Override
	public void remove(DirSubpath e) {
		touched.add(e);
		e.setMalId(-1);
	}
	public static void hide(DirSubpath dir) {
		if(dir == null || dir.getMalId() == -2) return;
		dir.setMalId(-2);
		touched.add(dir);
	}
	@Override
	public Stream<DirSubpath> stream() {
		return touched.stream().filter(d -> d.getMalId() == mal_id);
	}
}
