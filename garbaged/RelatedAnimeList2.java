package sam.anime.dao;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RelatedAnimeList2 extends List2<AnimeTitle> {
	AnimeDao dao = AnimeDao.getInstance();
	private List2<Integer> list;
	
	public RelatedAnimeList2(int mal_id) {
		super(mal_id);
	}
	public RelatedAnimeList2(int[] array, boolean isNew, int mal_id) {
		super(mal_id);
		this.list = array == null || array.length == 0 ? new List2<>(mal_id) : new List2<>(IntStream.of(array).boxed().collect(Collectors.toList()), isNew, mal_id);
	}
	@Override
	public void add(AnimeTitle e) {
		list.add(e == null ? null : e.getMalId());
	}
	@Override
	public void remove(AnimeTitle e) {
		list.remove(e == null ? null : e.getMalId());
	}

	@Override
	public Stream<AnimeTitle> stream() {
		return list.stream().map(this::map);
	}
	@Override
	public Stream<List<AnimeTitle>> streamOfLists() {
		throw new IllegalAccessError();
	}
	@Override
	public boolean isModified() {
		throw new IllegalAccessError();
	}
	@Override
	List<AnimeTitle> getAdded() {
		throw new IllegalAccessError();
	}
	@Override
	List<AnimeTitle> getRemoved() {
		throw new IllegalAccessError();
	}
	@Override
	public List<AnimeTitle> getCurrent() {
		throw new IllegalAccessError();
	}
	public AnimeTitle map(Integer id) {
		return dao.getMainAnime(id);
	}
	List2<Integer> getList() {
		return list;
	}
	public void add(Anime anime2) {
		// TODO Auto-generated method stub
		
	}
}
