package com.zsm.util.file.android;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v4.provider.DocumentFile;

import com.zsm.log.Log;
import com.zsm.util.file.FileDataListMaker;
import com.zsm.util.file.FileDataListMakerNotifier;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.SortableAdapter;
import com.zsm.util.file.android.DocumentData.TYPE;

public class DocumentFileListMaker implements FileDataListMaker<DocumentData> {

	@Override
	public void makeList(Object context, Uri location,
						 FileExtensionFilter filesFilter,
						 SortableAdapter<DocumentData> adapter,
						 Comparator<DocumentData> comparator,
						 boolean includeSubDir,
						 FileDataListMakerNotifier notifier) {

		final Context c = (Context)context;
		
		if( location == null ) {
			addVolumes(c, adapter);
			return;
		}
		
		Uri parentUri = DocumentFileUtilities.getTreeParentUri(location);
		if (parentUri != null) {
			// First item on the list.
			adapter.add(
				new DocumentFileData(c, parentUri, DocumentData.TYPE.UP_FOLDER));
		} else {
			adapter.add(new DocumentRootData(TYPE.UP_FOLDER));
		}
		
		DocumentFile[] listFiles
				= DocumentFileUtilities.listFiles( c, location, filesFilter,
												   includeSubDir, notifier );
		if (listFiles != null) {
			for ( DocumentFile file : listFiles ) {
				DocumentData.TYPE type
					= file.isDirectory()
						? DocumentData.TYPE.FOLDER : DocumentData.TYPE.DOCUMENT;
				
				DocumentData dfd = new DocumentFileData(file, type);
				adapter.add(dfd);
			}
			
			notifier.beforeToMakeOrder();
			adapter.sort( comparator );
		}
		
	}

	private void addVolumes(Context c, SortableAdapter<DocumentData> adapter) {
		
	   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
	        List<MyStorageVolume> list;
	        if( Build.VERSION.SDK_INT < Build.VERSION_CODES.N ) {
	        	list = getAvailableVolumesKitKat(c);
	        } else {
	        	list = getAvailableVolumesNougat(c);
	        }
	        
	        for( MyStorageVolume volume : list ) {
	        	Uri volumeUri
	        		= DocumentFileUtilities
	        			.buildExternalStorageTreeDocumentUri( volume.mIsPrimary,
	        												  volume.mUuid );
	        	adapter.add(
	    				new DocumentFileData(c, volumeUri, DocumentData.TYPE.VOLUME));
	        }
	    } else {
	    	Log.e( "Unsupported version: ", Build.VERSION.SDK_INT );
	    	throw new UnsupportedClassVersionError(
	    				"Unsupported SDK version: " + Build.VERSION.SDK_INT );
	    }
	}
	
	private List<MyStorageVolume> getAvailableVolumesKitKat( Context c ) {
		StorageManager sm
			= (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
		List<MyStorageVolume> list = new ArrayList<MyStorageVolume>();
		try {
			Method getVolumeListMethod = sm.getClass().getMethod("getVolumeList");
			Object[] volumes = (Object[]) getVolumeListMethod.invoke(sm);
			if( volumes.length <= 0 ) {
				Log.d( "No storage volume." );
				return list;
			}

			Class<?> clz = volumes[0].getClass();
			Method getPath = clz.getMethod( "getPathFile" );
			Method getState = clz.getMethod( "getState" );
			Method getUuid = clz.getMethod( "getUuid" );
			Method isPrimary = clz.getMethod( "isPrimary" );
			
			for( Object volume : volumes ) {
				String state = (String)getState.invoke( volume );
				if( "mounted".equals( state ) ) {
					MyStorageVolume msv
						= new MyStorageVolume( (File)getPath.invoke( volume ),
											   (boolean)isPrimary.invoke( volume ),
											   state,
											   (String)getUuid.invoke( volume ) );
					list.add(msv);
				}
			}
		} catch ( InvocationTargetException
				  | NoSuchMethodException | IllegalAccessException
				  | IllegalArgumentException e) {
			
			Log.e( e, "Get volumes failed!" );
		}
		return list;
	}
	
	@TargetApi(Build.VERSION_CODES.N)
	private List<MyStorageVolume> getAvailableVolumesNougat(Context c) {
		StorageManager sm
			= (StorageManager) c.getSystemService(Context.STORAGE_SERVICE);
		List<StorageVolume> volumes = sm.getStorageVolumes();
		List<MyStorageVolume> list = new ArrayList<MyStorageVolume>();
		
		try {
			Method getPathFile = StorageVolume.class.getMethod( "getPathFile" );
			for( StorageVolume volume : volumes ) {
				String state = volume.getState();
				if( "mounted".equals( state ) ) {
					MyStorageVolume msv
						= new MyStorageVolume( (File)getPathFile.invoke(volume),
										   	   volume.isPrimary(), state,
										   	   volume.getUuid() );
					list.add(msv);
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			
			Log.e( e, "Get storage volume information failed!" );
		}
		
		return list;
	}

	private class MyStorageVolume {
		private File mPath;
		private boolean mIsPrimary;
		private String mState;
		private String mUuid;
		
		private MyStorageVolume( File path, boolean isPrimary, String state,
								 String uuid ) {
			mPath = path;
			mIsPrimary = isPrimary;
			mState = state;
			mUuid = uuid;
		}

		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer( "[MyStorageVolume: " );
			buffer.append( "mPath = " );
			buffer.append( mPath.getAbsolutePath() );
			buffer.append( ", " );
			
			buffer.append( "mState = " );
			buffer.append( mState );
			buffer.append( ", " );

			buffer.append( "mIsPrimary = " );
			buffer.append( mIsPrimary );
			buffer.append( ", " );

			buffer.append( "mUuid = " );
			buffer.append( mUuid );
			buffer.append( "]" );
			
			return buffer.toString();
		}
		
	}
}
