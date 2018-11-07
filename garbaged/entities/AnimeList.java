package sam.anime.db.entities;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import sam.anime.db.AnimeDB;
import sam.collection.Iterators;
import sam.myutils.MyUtilsCheck;

public class AnimeList<E> implements Iterable<E> {
	private boolean modified;
	private List<E> data;
	/**
	 * use this data is not loaded 
	 */
	private ArrayList<E> newData;
	private final int mal_id;
	private Function<AnimeDB, List<E>> mapper;
	
	public AnimeList(int mal_id, List<E> data) {
		this(mal_id);
		this.data = data;
		unmodif = data == null ? null : Collections.unmodifiableList(data);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AnimeList(int mal_id) {
		this(mal_id, (Function)null);
	}
	public int getMalId() {
		return mal_id;
	}
	public AnimeList(int mal_id, Function<AnimeDB, List<E>> mapper) {
		this.mal_id = mal_id;
		this.mapper = mapper;
	}
	private List<E> unmodif;
	@SuppressWarnings("unchecked")
	public List<E> get(AnimeDB db) throws SQLException {
		if(unmodif != null) return unmodif;
		if(db == null || mapper == null) return Collections.EMPTY_LIST;
		
		data = mapper.apply(db);
		unmodif = Collections.unmodifiableList(data);

		if(newData != null) {
			for (E e : newData) add(e);
			newData = null;
		}
		return unmodif;
	}

	public void set(List<E> list) {
		modified = true;
		this.newData = null;
		this.data = new ArrayList<>();
		if(list != null) {
			for (E e : list) add(e);
		}
		unmodif = Collections.unmodifiableList(data);
	}
	public void add(E e) {
		if(e == null) return;

		if(this.data == null) {
			if(newData == null) {
				newData = new ArrayList<>();
				unmodif = Collections.unmodifiableList(newData);
			}
			add0(e);
			return;
		}

		add0(e);
	}
	
	private List<E> list() {
		return newData != null ? newData : data;
	}
	private void add0(E e) {
		List<E> list = list();
		String string = e instanceof String ? (String)e : null;

		if(list.stream().noneMatch(s -> string != null ? s.toString().equalsIgnoreCase(string) : s.equals(e))) {
			modified = true;
			list.add(e);
		}
	}

	public boolean remove(E e) {
		modified = true;
		String string = e instanceof String ? (String)e : null;
		return list().removeIf(s -> string != null ? s.toString().equalsIgnoreCase(string) : s.equals(e));
	}
	public void removeAll(List<E> list) {
		if(MyUtilsCheck.isEmpty(list))
			return;
		
		if(newData != null)
			newData.removeAll(list);
		if(data != null)
			data.removeAll(list);
		
		modified = true;
	}

	@Override
	public String toString() {
		return String.valueOf(data != null ? data : newData != null ? newData : null);
	}
	public boolean isModified() {
		return modified;
	}
	@Override
	public Iterator<E> iterator() {
		return list() == null ? Iterators.empty() : Collections.unmodifiableList(list()).iterator();
	}
	public boolean isEmpty() {
		return MyUtilsCheck.isEmpty(list());
	}
}
