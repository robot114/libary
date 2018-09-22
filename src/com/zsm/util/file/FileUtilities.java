package com.zsm.util.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import com.zsm.log.Log;

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
	
	public static File[] listFile( File parent, FileExtensionFilter filter,
								   boolean includeSubDir ) {
		
		if( parent == null || !parent.exists() || !parent.isDirectory() ) {
			return null;
		}
		
		FlagedFileFilter fff = new FlagedFileFilter( filter, includeSubDir );
		return parent.listFiles( fff );
	}
	
	public static String getExtension( String fileName ) {
		int i = fileName.lastIndexOf(".");
		return i > 0 ? fileName.substring(i) : "";
	}
	
	public static String removeExtension( String fileName ) {
		int i = fileName.lastIndexOf(".");
		return i > 0 ? fileName.substring(0, i) : fileName;
	}
	
	public static long sizeFromUri(ContentResolver cr, Uri uri) {
		try ( Cursor c = cr.query(uri, null, null, null, null) ) {
			if( c == null || c.getCount() != 1 || !c.moveToFirst() ) {
				Log.w( "Sth. wrong in quering uri. ", uri, "cursor", c,
					   "count", c == null ? 0 : c.getCount() );
				return 0;
			}
			
			
			int colIndex = c.getColumnIndex( OpenableColumns.SIZE );
			if( colIndex < 0 ) {
				Log.w( "No size column in the query result for uri. ", uri );
				return 0;
			}
			return c.getLong( colIndex );
		}

	}
	
	public static boolean doesFileExist(ContentResolver cr, Uri uri) {
		try ( Cursor c = cr.query(uri, null, null, null, null) ) {
			if( c == null || c.getCount() != 1 || !c.moveToFirst() ) {
				return false;
			}
			
			return true;
		}

	}
	
	public static void copyFile(String src, String target) throws IOException {
		copyFile( new File(src), new File(target) );
	}
	
	public static void copyFile(File src, File target) throws IOException {

		// create output directory if it doesn't exist
		File targetPath = target.getAbsoluteFile().getParentFile();
		if (!targetPath.exists()) {
			targetPath.mkdirs();
		}
		
		try( InputStream in = new FileInputStream(src);
			 OutputStream out = new FileOutputStream(target) ) {

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			// write the output file (You have now copied the file)
			out.flush();
		}
	}
	
	public static String displayNameFromUri( ContentResolver cr, Uri uri ) {
		String result = null;
		if (uri.getScheme().equals("content")) {
			try( Cursor cursor
					= cr.query(uri, null, null, null, null) ) {
				
				if ( cursor != null && cursor.moveToFirst() ) {
					int columnIndex
						= cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
					
					result = cursor.getString(columnIndex);
				}
			} catch( Exception e ) {
				Log.w( e, "Get display name failed: ", uri );
			}
		}
		if (result == null) {
			result = uri.getLastPathSegment();
			int cut = result.lastIndexOf('/');
			if (cut != -1) {
				result = result.substring(cut + 1);
			}
		}
		return result;
	}

	/**
	 * Rename the file to the new name. When the file with the new name 
	 * exists, and if <br> {@code force} is true, the old one will be over written;
	 * <br> {@code force} is false, the rename will abort and false returned.
	 * 
	 * @param src File to be renamed
	 * @param target new full name of target
	 * @param force
	 * @return true, when rename successfully; false, otherwise
	 */
	public static boolean checkAndRenameTo( String src, String target,
										    boolean force ) {
		if( src == null || target == null ) {
			return false;
		}
		
		return checkAndRenameTo( new File( src ), new File( target ), force );
	}

	/**
	 * Rename the file to the new name file. When the file with the new name 
	 * exists, and if <br> {@code force} is true, the old one will be over written;
	 * <br> {@code force} is false, the rename will abort and false returned.
	 * 
	 * @param src File to be renamed
	 * @param target file with new name of target
	 * @param force
	 * @return true, when rename successfully; false, otherwise
	 */
	public static boolean checkAndRenameTo( File src, File target, boolean force ) {
		if( target == null || src == null || src.isDirectory() || !src.exists() ) {
			return false;
		}
		
		if( target.exists() ) {
			if( force ) {
				target.delete();
			} else {
				return false;
			}
		}
		
		if( !src.renameTo(target) ) {
			try {
				copyFile( src, target );
			} catch (IOException e) {
				Log.e( e, "Copy file failed. ", "src", src, "target", target );
				return false;
			}
		}

		return true;
	}
}
