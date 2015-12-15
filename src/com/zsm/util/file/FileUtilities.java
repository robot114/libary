package com.zsm.util.file;

import java.io.File;
import java.io.FileFilter;

public class FileUtilities {

	private static class FlagedFileFilter implements FileFilter {
		private FileExtensionFilter filter;
		private boolean includeSubDir;
		
		private FlagedFileFilter( FileExtensionFilter ff, boolean includeSubDir ) {
			filter = ff;
			this.includeSubDir = includeSubDir;
		}
		
		@Override
		public boolean accept(File file) {
			if( file.isDirectory() ) {
				return includeSubDir;
			}
			
			return filter.accept(file);
		}
	}
	
	public static File[] listFile( File parent, FileExtensionFilter filter, boolean includeSubDir ) {
		if( parent == null || !parent.exists() || !parent.isDirectory() ) {
			return null;
		}
		
		FlagedFileFilter fff = new FlagedFileFilter( filter, includeSubDir );
		return parent.listFiles( fff );
	}
}
