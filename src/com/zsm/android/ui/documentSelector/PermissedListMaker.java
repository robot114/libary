package com.zsm.android.ui.documentSelector;

import com.zsm.R;
import com.zsm.log.Log;
import com.zsm.util.file.AsyncFileListMaker;
import com.zsm.util.file.FileDataListMakerNotifier;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentData;
import com.zsm.util.file.android.DocumentFileListMaker;
import com.zsm.util.file.android.DocumentFileUtilities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.widget.Toast;

public class PermissedListMaker {

	private static final String KEY_URI_FOR_PERMISSION = "KEY_URI_FOR_PERMISSION";
	static final int REQUEST_FOR_GRANT = 201;
	protected static final int REQUEST_STORAGE_PERMISSION = 202;
	
	/**
	 * Permissions required to read and write to storage.
	 */
	private static String[] PERMISSIONS_READ_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE };
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE };
	
	private Fragment mFragment;
	private Activity mActivity;

	private AsyncFileListMaker<DocumentData> mAsyncFileListMaker;
	private boolean mMakingList;
	private DocumentOperation mOperation = DocumentOperation.LOAD;
	private DocumentListAdapter mAdapter;
	private FileDataListMakerNotifier mNotifier;

	public PermissedListMaker( Activity a, DocumentListAdapter adapter,
							   FileDataListMakerNotifier n,
							   DocumentOperation operation ) {

		mFragment = null;
		mActivity = a;
		mAdapter = adapter;
		mNotifier = n;
		mOperation = operation;
		
		mAsyncFileListMaker
			= new AsyncFileListMaker<DocumentData>( new DocumentFileListMaker() );
	}

	public PermissedListMaker(Fragment f, DocumentListAdapter adapter,
							  FileDataListMakerNotifier n, DocumentOperation operation) {

		mFragment = f;
		mActivity = f.getActivity();
		mAdapter = adapter;
		mNotifier = n;
		mOperation = operation;

		mAsyncFileListMaker = new AsyncFileListMaker<DocumentData>(new DocumentFileListMaker());
	}

	private void makeList(Uri current, FileExtensionFilter filter,
						  boolean includeSubDir,
						  FileDataListMakerNotifier notifier) {

		mAdapter.clear();
		if (mAsyncFileListMaker.isMaking()) {
			mAsyncFileListMaker.cancel();
		}

		// Save the filter and includeDir parameter for the time one item is clicked
		if (filter == null) {
			filter = new FileExtensionFilter("All files", mNotifier);
		}
		mAsyncFileListMaker.makeList(mActivity, current, filter, mAdapter,
									 DocumentData.DEFAULT_COMPARATOR,
									 includeSubDir, notifier);
	}

	public void checkPermissionAndMakeList( Uri current, FileExtensionFilter filter,
											boolean includeSubDir ) {

		// To void multiple threads operate same adapter making files in the list error
		if (!checkPermission(mActivity, current)) {
			forGrant(current);
		} else {
			makeList(current, filter, includeSubDir, mNotifier);
		}

	}

	private boolean checkPermission(Context c, Uri current) {
		if (current == null) {
			return true;
		}

		if (mOperation == DocumentOperation.LOAD) {
			return DocumentFileUtilities.hasUriReadPermission(c, current);
		}

		return DocumentFileUtilities.hasUriWritePermission(c, current);
	}

	private void forGrant(Uri current) {
		Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
		intent.putExtra(KEY_URI_FOR_PERMISSION, current);
		if( mFragment != null ) {
			mFragment.startActivityForResult(intent, REQUEST_FOR_GRANT);
		} else {
			mActivity.startActivityForResult(intent, REQUEST_FOR_GRANT);
		}
	}

	public void checkPermission() {
		boolean needsRead
			= ActivityCompat.checkSelfPermission(
						mActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED;

		boolean needsDocument
			= ActivityCompat.checkSelfPermission(
						mActivity, Manifest.permission.MANAGE_DOCUMENTS)
				!= PackageManager.PERMISSION_GRANTED;
		
		boolean needsWrite = false;
		if( mOperation == DocumentOperation.SAVE ) {
			needsWrite
				= ActivityCompat.checkSelfPermission(
							mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
					!= PackageManager.PERMISSION_GRANTED;
		}

		if (needsRead || needsWrite || needsDocument) {
			requestStoragePermission();
		}
	}

	/**
	 * Requests the ability to read or write to external storage.
	 *
	 * Can be activated with {@link #setStoragePermissionRequestEnabled(boolean)}
	 *
	 * If a user has denied the permission you can supply a rationale that will be
	 * displayed in a Snackbar by setting {@link #setStoragePermissionRequestEnabled(boolean)} ()}
	 */
	private void requestStoragePermission() {
		Log.d("STORAGE permission has NOT been granted. Requesting permission.");

		boolean justifyRead
			= ActivityCompat.shouldShowRequestPermissionRationale(
					mActivity, Manifest.permission.READ_EXTERNAL_STORAGE);
		
		boolean justifyWrite = false;
		
		if( mOperation == DocumentOperation.SAVE ) {
			justifyWrite 
				= ActivityCompat.shouldShowRequestPermissionRationale(
					mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		final String[] permissions
			= mOperation == DocumentOperation.LOAD
				? PERMISSIONS_READ_STORAGE : PERMISSIONS_STORAGE;

		// Storage permission has been requested and denied, show a Snackbar with the option to
		// grant permission
		if (justifyRead || justifyWrite ) {
			// Provide an additional rationale to the user if the permission was not granted
			// and the user would benefit from additional context for the use of the permission.
			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder
				.setTitle( R.string.documenSelector_title_requestAccess)
				.setMessage( R.string.documenSelector_prompt_requestAccess )
				.setIcon( android.R.drawable.ic_dialog_alert )
				.setPositiveButton( android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityCompat.requestPermissions(mActivity,
								permissions, REQUEST_STORAGE_PERMISSION);
					}
				})
				.setNegativeButton( android.R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mActivity.finish();
					}
				} )
				.show();
		} else {
			// Storage permission has not been requeste yet. Request for first time.
			ActivityCompat
				.requestPermissions(
					mActivity, permissions, REQUEST_STORAGE_PERMISSION);
		}
	}

	void doForGrant(int resultCode, Intent data, FileExtensionFilter filter,
					boolean includeSubDir ) {
		
		if( resultCode == Activity.RESULT_OK ) {
			int takeFlags
				= data.getFlags() 
				  & ( Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					  | Intent.FLAG_GRANT_READ_URI_PERMISSION );

			Uri uri = data.getData();
			mActivity.grantUriPermission(
					mActivity.getPackageName(), uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
						| Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
						| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			
			mActivity.getContentResolver()
				.takePersistableUriPermission(uri, takeFlags );
			
			Uri current = data.getParcelableExtra(KEY_URI_FOR_PERMISSION);
			
			if( current == null ) {
				Uri doucmentUri
					= DocumentsContract.buildDocumentUriUsingTree(
							uri, DocumentsContract.getTreeDocumentId(uri));
				makeList(doucmentUri, filter, includeSubDir, mNotifier);
			} else {
				checkPermissionAndMakeList(current, filter, includeSubDir);
			}
		} else {
			Toast.makeText(mActivity,
						   R.string.documenSelector_prompt_accessDeny,
						   Toast.LENGTH_LONG )
				 .show();
		}
	}

	void setMakingListFlag() {
		mMakingList = true;
	}
	
	void clearMakingListFlag() {
		mMakingList = false;
	}
	
	boolean isMakingListFlagSet() {
		return mMakingList;
	}

	public void setOperation(DocumentOperation operation) {
		mOperation = operation;
	}
}
