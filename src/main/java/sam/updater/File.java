package sam.updater;

import java.nio.file.Files;

import sam.myutils.MyUtilsException;
import sam.string.StringUtils;

class File {
	private final java.io.File file;
	private final String[][] data;
	private final boolean isDir;
	private final long size, lastmodified;
	private final String name;
	private boolean exists;

	public File(String s) {
		file = new java.io.File(s);
		isDir = file.isDirectory();
		exists = file.exists();

		name = s;
		size = 0;
		lastmodified = System.currentTimeMillis();
		data = data();

	}
	public File(File file, String s) {
		this.file = new java.io.File(file.file, s);
		name = s;
		String[] data = find(file.data, s);
		exists = data != null;
		isDir = exists && data.length == 2;

		String[][] dt = null;
		this.lastmodified = Long.parseLong(data[1]);
		this.size = !exists || isDir ? 0 : Long.parseLong(data[2]);
		this.data = data();
	}
	private String[][] data() {
		java.io.File f = new java.io.File(this.file, name+".txt");
		if(!f.exists())
			return null;

		return MyUtilsException.noError(() -> Files.lines(f.toPath())).map(ss -> StringUtils.split(ss, '\t')).toArray(String[][]::new);
	}
	private String[] find(String[][] data2, String s) {
		for (String[] s2 : data2) {
			if(s.equals(s2[0]))
				return s2;
		}
		return null;
	}
	public boolean exists() {
		return exists;
	}
	public String[] list() {
		return list(data);
	}

	private String[] list(String[][] data) {
		String[] s = new String[data.length];
		for (int i = 0; i < s.length; i++)
			s[i] = data[i][0];

		return s;
	}
	public long length() {
		return size;
	}

	public boolean isDirectory() {
		return isDir;
	}

	public long lastModified() {
		return lastmodified;
	}
	@Override
	public int hashCode() {
		return file.hashCode();
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		File other = (File) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}


}