package fi.iki.kuitsi.bitbeaker.data.api.model.repositories;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Response of GET a list of repo source if content is a directory.
 *
 * @see <a href="https://confluence.atlassian.com/display/BITBUCKET/src+Resources#srcResources-GETalistofreposource">src Resources - Bitbucket - Atlassian Documentation</a>
 */
public class DirectoryContent {
	private final ArrayList<String> directories;
	private final ArrayList<RepoFile> files;

	public DirectoryContent(ArrayList<String> directories, ArrayList<RepoFile> repoFiles) {
		this.directories = directories;
		this.files = repoFiles;
	}

	public List<String> getDirectories() {
		return directories;
	}

	public List<RepoFile> getFiles() {
		return files;
	}

	public static class RepoFile implements Comparable<RepoFile> {
		private final String path;

		public RepoFile(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			RepoFile repoFile = (RepoFile) o;

			return path.equals(repoFile.path);

		}

		@Override
		public int hashCode() {
			return path.hashCode();
		}

		@Override
		public int compareTo(@NonNull RepoFile another) {
			return path.compareTo(another.getPath());
		}
	}
}
