package sam.updater;


import static sam.anime.db2.DirsMeta.DIRS_COUNT;
import static sam.anime.db2.DirsMeta.FILES_COUNT;
import static sam.anime.db2.DirsMeta.ID;
import static sam.anime.db2.DirsMeta.LAST_MODIFIED;
import static sam.anime.db2.DirsMeta.PARENT_ID;
import static sam.anime.db2.DirsMeta.SUBPATH;
import static sam.anime.db2.DirsMeta.TABLE_NAME;
import static sam.anime.db2.DirsMeta.TOTAL_SIZE;
import static sam.myutils.MyUtilsCheck.isNotEmpty;
import static sam.sql.ResultSetHelper.getString;
import static sam.sql.querymaker.QueryMaker.qm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import sam.anime.db2.AnimeDB;
import sam.anime.db2.FilesMeta;
import sam.console.ANSI;
import sam.logging.MyLoggerFactory;
import sam.myutils.MyUtilsCheck;
import sam.myutils.MyUtilsPath;
import sam.myutils.System2;
import sam.sql.SqlConsumer;
import sam.sql.SqlFunction;
import sam.sql.querymaker.InserterBatch;
import sam.string.BasicFormat;

public class Updater {
	private static final Logger LOGGER = MyLoggerFactory.logger(Updater.class.getSimpleName());
	
	public static void main(String[] args) throws IOException {
		start();
	}
	public static void start() throws IOException {
		new Updater().start0();
	}

	private final File root;
	private AnimeDB db;
	private final List<String> prinlnLines = new ArrayList<>();

	public Updater() {
		String s = System2.lookup("ANIME_DIR");
		if(MyUtilsCheck.isEmptyTrimmed(s)) {
			LOGGER.severe(ANSI.red("ANIME_DIR env/property not found"));
			System.exit(0);
		}

		root = file(s);
		if(!root.exists()) {
			LOGGER.severe(ANSI.red("ANIME_DIR dir not found: ")+s);
			System.exit(0);
		}
	}
	
	private File file(String s) {
		return new File(s);
	}
	private File file(File file, String s) {
		return new File(file, s);
	}

	enum Type {
		NEW(ANSI.cyan("NEW")), UPDATE(ANSI.yellow("UPDATE")), REMOVE(ANSI.red("REMOVE"));

		final String string;
		private Type(String s) {
			this.string = s;
		} 
	}

	long maxDirID, maxFileID, maxDirID_FIXED, maxFileID_FIXED;
	int rootFileLength;

	private void start0() throws IOException {
		backup_temp();
		
		try(AnimeDB db = AnimeDB.getInstance();
				Statement stmnt = db.createStatement();) {
			this.db = db;

			String[] animes = root.list();
			Dir rootDir = new Dir(root,null, -1, null, null, new DirDB());
			Map<String, DirDB> map = getDirs(rootDir);

			long l = Arrays.stream(animes).filter(map::containsKey).count();
			if(l < animes.length - 10 && JOptionPane.showConfirmDialog(null, "<html>Seems not the desired Anime dir: <br>"+root+"<br>missing animes in this dir: "+(animes.length - l)+"<html>", "I'm suspicious", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.OK_OPTION) {
				LOGGER.info(ANSI.green("cancelled processing AnimeDir(s)"));
				return;
			}

			maxDirID = db.getSequnceValue(TABLE_NAME) + 1;
			maxFileID = db.getSequnceValue(FilesMeta.TABLE_NAME) + 1;
			maxDirID_FIXED = maxDirID -1;
			maxFileID_FIXED = maxFileID - 1; 
			rootFileLength = root.toString().length() + 1;
			
			println("maxDirID: "+maxDirID);
			println("maxFileID: "+maxFileID);

			walkDir(rootDir);

			Map<Type, List<Dir>> dirs = new EnumMap<>(Type.class);
			Map<Type, List<FileW>> files = new EnumMap<>(Type.class);
			
			for (Type t : Type.values()) {
				dirs.put(t, new ArrayList<>());
				files.put(t, new ArrayList<>());
			} 
			
			rootDir.fill(dirs, files, "| ");

			dirs.values().removeIf(List::isEmpty);
			files.values().removeIf(List::isEmpty);

			if(dirs.isEmpty() && files.isEmpty()) {
				LOGGER.info(ANSI.yellow("NO Changes/Updates"));
				return;
			}

			dirs = Collections.unmodifiableMap(dirs);
			files = Collections.unmodifiableMap(files);

			SqlConsumer<String> stmnt2 = s -> {
				prinlnLines.add(s);
				stmnt.addBatch(s);
			} ;

			processFiles(stmnt2, files);
			processDirs(stmnt2, dirs);

			remainingUpdates(stmnt2, rootDir);
			println("stmnt executed: "+stmnt.executeBatch().length);

			db.commit();
			backup();
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, null, e);
		}

		
		if(!prinlnLines.isEmpty())
			Files.write(MyUtilsPath.TEMP_DIR.resolve(MyUtilsPath.pathFormattedDateTime()), prinlnLines, StandardOpenOption.CREATE);
	}

	private void backup() throws IOException {
		Path dbpath = Paths.get(AnimeDB.dbPath());
		Path target = dbpath.resolveSibling("--temp--"+dbpath.getFileName());
		
		Files.createDirectories(dbpath.resolveSibling("backup"));
		Files.move(target, dbpath.resolveSibling("backup/"+dbpath.getFileName()+"-"+DayOfWeek.from(LocalDate.now()).getValue()), StandardCopyOption.REPLACE_EXISTING);
	}
	private void backup_temp() throws IOException {
		Path dbpath = Paths.get(AnimeDB.dbPath());
		Path target = dbpath.resolveSibling("--temp--"+dbpath.getFileName());
		Files.copy(dbpath, target, StandardCopyOption.REPLACE_EXISTING);
	}

	private void remainingUpdates(SqlConsumer<String> stmnt, Dir rootDir) throws SQLException {
		BasicFormat fileSize = new BasicFormat(qm().update(TABLE_NAME).set(TOTAL_SIZE, "("+qm().select("sum("+FilesMeta.SIZE+")").from(FilesMeta.TABLE_NAME).where(w -> w.eq(FilesMeta.PARENT_ID, "{0}", false)).build()+")", false).where(w -> w.eq(ID, "{0}", false)).build());
		BasicFormat dirSize = new BasicFormat(qm().update(TABLE_NAME).set(TOTAL_SIZE, TOTAL_SIZE+"+("+qm().select("sum("+TOTAL_SIZE+")").from(TABLE_NAME).where(w -> w.eq(PARENT_ID, "{0}", false)).build()+")", false).where(w -> w.eq(ID, "{0}", false)).build());
		BasicFormat fileCount = new BasicFormat(qm().update(TABLE_NAME).set(FILES_COUNT, "("+qm().select("count("+FilesMeta.PARENT_ID+")").from(FilesMeta.TABLE_NAME).where(w -> w.eq(FilesMeta.PARENT_ID, "{0}", false)).build()+")", false).where(w -> w.eq(ID, "{0}", false)).build());
		BasicFormat dirCount = new BasicFormat(qm().update(TABLE_NAME).set(DIRS_COUNT, "("+qm().select("count("+PARENT_ID+")").from(TABLE_NAME).where(w -> w.eq(PARENT_ID, "{0}", false)).build()+")", false).where(w -> w.eq(ID, "{0}", false)).build());

		for (Dir parent : rootDir.dirs) {
			parent.bubble(dir -> {
				stmnt.accept(fileSize.format(dir.dbEntry.id));
				stmnt.accept(fileCount.format( dir.dbEntry.id));
				stmnt.accept(dirSize.format( dir.dbEntry.id));
				stmnt.accept(dirCount.format( dir.dbEntry.id));
			});
		}
	}

	private void processFiles(SqlConsumer<String> stmnt, Map<Type, List<FileW>> map) throws SQLException {
		List<FileW> list = map.get(Type.REMOVE); 
		
		if(isNotEmpty(list)) {
			int[] n = list.stream().mapToInt(d -> d.dbEntry.id).toArray();
			stmnt.accept(qm().deleteFrom(FilesMeta.TABLE_NAME).where(w -> w.in(FilesMeta.ID, n)).build());
			println("Files delete: "+n.length);
		}
		
		list = map.get(Type.NEW);

		if(isNotEmpty(list)) {
			InserterBatch<FileW> insert = new InserterBatch<>(FilesMeta.TABLE_NAME);
			insert.setLong(FilesMeta.ID, d -> d.newId);
			insert.setLong(FilesMeta.PARENT_ID, d -> d.parent.dbEntry.id);
			insert.setString(FilesMeta.FILE_NAME, d -> d.name);
			insert.setLong(FilesMeta.SIZE, d -> d.file.length());
			insert.setLong(FilesMeta.LAST_MODIFIED, d -> d.lastModified);

			println("NEW Files: "+insert.execute(db, list));
		}

		list = map.get(Type.UPDATE);

		if(isNotEmpty(list)) {
			BasicFormat sql = new BasicFormat(qm().update(FilesMeta.TABLE_NAME).set(FilesMeta.LAST_MODIFIED, "{}",false).set(FilesMeta.SIZE, "{}", false).where(w -> w.eq(FilesMeta.ID, "{}", false)).build());
			for (FileW d : list) 
				stmnt.accept(sql.format(d.lastModified, d.file.length(), d.dbEntry.id));

			println("UPDATE Files: "+list.size());
		}
	}

	private void processDirs(SqlConsumer<String> stmnt, Map<Type, List<Dir>> map) throws SQLException {
		List<Dir> list = map.get(Type.REMOVE);
		
		if(isNotEmpty(list)) {
			int n[] = list.stream().mapToInt(d -> d.dbEntry.id).toArray();

			stmnt.accept(qm().deleteFrom(TABLE_NAME).where(w -> w.in(ID, n)).build());
			stmnt.accept(qm().deleteFrom(FilesMeta.TABLE_NAME).where(w -> w.in(FilesMeta.PARENT_ID, n)).build());

			println("Dirs delete: "+n.length);
			println("Files delete2: "+n.length);
		}
		
		list = map.get(Type.NEW);

		if(isNotEmpty(list)) {
			InserterBatch<Dir> insert = new InserterBatch<>(TABLE_NAME);
			insert.setLong(ID, d -> d.newId);
			insert.setLong(PARENT_ID, d -> d.parent.dbEntry.id);
			insert.setString(SUBPATH, d -> d.subpath);
			insert.setLong(LAST_MODIFIED, d -> d.lastModified);

			println("NEW ANIME: "+insert.execute(db, list));
		}

		list = map.get(Type.UPDATE);
		if(isNotEmpty(list)) {
			BasicFormat sql = new BasicFormat(qm().update(TABLE_NAME).set(LAST_MODIFIED, "{}",false).where(w -> w.eq(ID, "{}", false)).build());
			for (Dir d : list) 
				stmnt.accept(sql.format(d.lastModified, d.dbEntry.id));

			println("UPDATE Dirs: "+list.size());
		}
	}

	private void walkDir(Dir parent) throws SQLException {
		Map<String, DirDB> dbdirs = getDirs(parent);
		Map<String, FileDB> dbfiles = getfiles(parent);

		for (String name : parent.file.list()) {
			File file = file(parent.file, name);
			boolean isDir = file.isDirectory();
			String subpath = !isDir ? null : file.toString().substring(rootFileLength);
			DbEntry dbe = (isDir ? dbdirs : dbfiles).remove(isDir ? subpath : name);
			long lm = file.lastModified();

			if(dbe != null && dbe.lastModified == lm)
				continue;
			
			if(isDir) {
				Dir d = dbe == null ? new Dir(file, name, lm, subpath, parent) : new Dir(file, name, lm,subpath, parent, (DirDB)dbe);
				parent.add(d);
				walkDir(d);
			} else {
				FileW d = dbe == null ? new FileW(file, name, lm, parent) : new FileW(file, name, lm, parent, (FileDB)dbe);
				parent.add(d);
			} 
		}
		dbdirs.forEach((s,d) -> parent.add(new Dir(d, parent)));
		dbfiles.forEach((s,d) -> parent.add(new FileW(d, parent)));
	}

	private Map<String, FileDB> getfiles(Dir parent) throws SQLException {
		return get(selectFilesSQL, parent, FilesMeta.FILE_NAME, FileDB::new);
	}
	private Map<String, DirDB> getDirs(Dir parent) throws SQLException {
		return get(selectDirsSQL, parent, SUBPATH, DirDB::new);
	}

	private <E> Map<String, E> get(String sqlbase,Dir parent, String pathCol, SqlFunction<ResultSet, E> mapper) throws SQLException {
		return parent.dbEntry == null ? Collections.emptyMap() : db.collectToMap(sqlbase+parent.dbEntry.id, getString(pathCol), mapper);
	}
	final String selectFilesSQL = qm().select(FilesMeta.ID,FilesMeta.PARENT_ID,FilesMeta.FILE_NAME,FilesMeta.LAST_MODIFIED).from(FilesMeta.TABLE_NAME).where(w -> w.eq(FilesMeta.PARENT_ID, "", false)).build();
	final String selectDirsSQL = qm().select(ID,PARENT_ID,SUBPATH,LAST_MODIFIED).from(TABLE_NAME).where(w -> w.eq(PARENT_ID, "", false)).build();

	abstract class DbEntry {
		final int id, parent_id;
		final long lastModified;
		final String path;

		public DbEntry(ResultSet rs, String path, String id, String parent_id, String lastModified) throws SQLException {
			this.path = rs.getString(path);
			this.id = rs.getInt(id);
			this.parent_id = rs.getInt(parent_id);
			this.lastModified = rs.getLong(lastModified);
		}
		public DbEntry() {
			this.path = null;
			this.id = -1;
			this.parent_id = -1;
			this.lastModified = -1;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName()+" [id=" + id + ", lastModified=" + lastModified + ", path=" + path + "]";
		}
		public abstract boolean isDir();
	}

	class DirDB extends DbEntry {
		public DirDB(ResultSet rs) throws SQLException {
			super(rs, SUBPATH, ID, PARENT_ID, LAST_MODIFIED);

		}
		public DirDB() {
			super();
		}

		@Override
		public boolean isDir() {
			return true;
		}

	}
	class FileDB extends DbEntry {
		public FileDB(ResultSet rs) throws SQLException {
			super(rs, FilesMeta.FILE_NAME, FilesMeta.ID, FilesMeta.PARENT_ID, FilesMeta.LAST_MODIFIED);
		}

		@Override
		public boolean isDir() {
			return false;
		}
	}

	abstract class FileOrDir<E extends DbEntry> {
		final File file;
		final long lastModified;
		final E dbEntry;
		final Dir parent;
		final Type type;
		final long newId;
		final String name;

		public FileOrDir(File file,String name,  long lastModified, E dbEntry, Dir parent, Type status, long newId) {
			this.file = file;
			this.lastModified = lastModified;
			this.dbEntry = dbEntry;
			this.parent = parent;
			this.type = status;
			this.newId = newId;
			this.name = name;
			
			switch (type) {
				case NEW:
					if(dbEntry != null || 
					!idCheck() || 
					lastModified <= 0 || 
					parent == null
					) {
						throw new IllegalArgumentException(toString());
					}
					break;
				case REMOVE:
					if(file != null)
						throw new IllegalArgumentException(toString());
					break;
				case UPDATE:
					if(file == null || dbEntry == null || lastModified == 0 || parent == null)
						throw new IllegalArgumentException(toString());
					break;
				default:
					break;
			} 
			
			
			if(Type.REMOVE == type && file != null)
				
			if(dbEntry == null && type != Type.NEW)
				throw new IllegalArgumentException("dbEntry == null && type != type.NEW, "+toString());
			
			
		}

		private boolean idCheck() {
			return (newId > (getClass() == Dir.class ? maxDirID_FIXED : maxFileID_FIXED));
		}

		public FileOrDir(File file,String name,  long lastModified, Dir parent, long newId) {
			this(file,name, lastModified, null, parent, Type.NEW, newId);
		}
		public FileOrDir(File file,String name,  long lastModified, Dir parent, E dbEntry) {
			this(file,name, lastModified, Objects.requireNonNull(dbEntry), parent, Type.UPDATE, dbEntry.id);
		}
		public FileOrDir(E dbEntry, Dir parent) {
			this(null,null, -1, Objects.requireNonNull(dbEntry), parent, Type.REMOVE, -1);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			
			builder.append(getClass().getSimpleName()).append(" [");
			if (file != null) {
				builder.append("file=");
				builder.append(file);
				builder.append(", ");
			}
			builder.append("lastModified=");
			builder.append(lastModified);
			builder.append(", ");
			if (dbEntry != null) {
				builder.append("dbEntry=");
				builder.append(dbEntry);
				builder.append(", ");
			}
			if (parent != null) {
				builder.append("parent=");
				builder.append(parent);
				builder.append(", ");
			}
			if (type != null) {
				builder.append("type=");
				builder.append(type);
				builder.append(", ");
			}
			builder.append("newId=");
			builder.append(newId);
			builder.append(", ");
			if (name != null) {
				builder.append("name=");
				builder.append(name);
			}
			builder.append("]");
			return builder.toString();
		}
		
	}
	class FileW extends FileOrDir<FileDB> {
		public FileW(File file,String name,  long lastModified, Dir parent, FileDB dbEntry) {
			super(file,name, lastModified, parent, dbEntry);
		}
		public FileW(File file,String name,  long lastModified, Dir parent) {
			super(file,name, lastModified, parent, maxFileID++);
		}
		public FileW(FileDB dbEntry, Dir parent) {
			super(dbEntry, parent);
		}
	}
	private class Dir extends FileOrDir<DirDB> {
		final String subpath;
		final ArrayList<Dir> dirs = new ArrayList<>();
		final ArrayList<FileW> files = new ArrayList<>();

		public Dir(File file, String name, long lastModified, String subpath,  Dir parent, DirDB dbEntry) {
			super(file, name, lastModified, parent, dbEntry);
			this.subpath = subpath;
		}
		public Dir(DirDB dbEntry, Dir parent) {
			super(dbEntry, parent);
			subpath = dbEntry.path;
		}

		/**
		 * walk from child to parent in tree
		 * @param consumer
		 * @throws SQLException
		 */
		public void bubble(SqlConsumer<Dir> consumer) throws SQLException {
			for (Dir dir : dirs) 
				dir.bubble(consumer);

			consumer.accept(this);
		}
		public Dir(File file, String name, long lastModified, String subpath, Dir parent) {
			super(file, name, lastModified, parent, maxDirID++);
			this.subpath = subpath;
		}

		public void fill(Map<Type, List<Dir>> dirs, Map<Type, List<FileW>> files, String indent) {
			//TODO

			for (FileW f : this.files) {
				println(indent+f.name+"  "+f.type.string);
				files.get(f.type).add(f);
			}

			for (Dir f : this.dirs) {
				println(indent+f.name+"  "+f.type.string);

				dirs.get(f.type).add(f);
				f.fill(dirs, files, indent+"..|.");
			}
		}
		
		public void add(FileW fd) {
			files.add(fd);
		}
		public void add( Dir fd) {
			dirs.add(fd);
		}
	}
	public void println(String s) {
		System.out.println(s);
		prinlnLines.add(s);
	}
}
