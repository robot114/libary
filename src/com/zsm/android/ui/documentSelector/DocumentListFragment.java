package com.zsm.android.ui.documentSelector;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.R;
import com.zsm.log.Log;
import com.zsm.util.file.AsyncFileListMaker;
import com.zsm.util.file.FileDataListMakerNotifier;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentData;
import com.zsm.util.file.android.DocumentFileUtilities;

public class DocumentListFragment
				extends Fragment implements FileDataListMakerNotifier {

	private static final String KEY_URI_FOR_PERMISSION = "KEY_URI_FOR_PERMISSION";
	private static final int REQUEST_FOR_GRANT = 201;
	protected static final int REQUEST_STORAGE_PERMISSION = 202;
	
	/**
	 * Permissions required to read and write to storage.
	 */
	private static String[] PERMISSIONS_READ_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE };
	private static String[] PERMISSIONS_STORAGE = {
			Manifest.permission.READ_EXTERNAL_STORAGE,
			Manifest.permission.WRITE_EXTERNAL_STORAGE };

	public interface OnDocumentSelectedListener {
		public void onSelected( View view, DocumentData data );
	}
	
	private View mView;
	private ListView mListView;
	private TextView mMessageView;
	private OnDocumentSelectedListener mOnDocumentSelectedListener;
	
	private AsyncFileListMaker<DocumentData> mAsyncFileListMaker;
	private boolean mMakingList;
	private FileExtensionFilter mFileExtFilter;
	private boolean mIncludeSubDir;
	private DocumentOperation mOperation = DocumentOperation.LOAD;
	private Handler mHandler;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		
		if( mView == null ) {
			mView
				= inflater.inflate( R.layout.document_list_fragment, container,
									false );
			
			mListView = (ListView)mView.findViewById( R.id.listViewDocument );
			mMessageView = (TextView)mView.findViewById( R.id.textViewMessage );
			mListView.setOnItemClickListener( new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					
					onDocumentClick(
						(DocumentListAdapter) parent.getAdapter(), view, position);
				}
			} );
		}
		return mView;
	}
	
	private void onDocumentClick( DocumentListAdapter adapter, View view,
								  int position ) {
		
		DocumentData data = adapter.getItem(position);
		if( data.getType() != DocumentData.TYPE.DOCUMENT ) {
			checkPermissionAndMakeList( data.getUri(), mFileExtFilter,
										mIncludeSubDir );
		}
		
		if( mOnDocumentSelectedListener != null ) {
			mOnDocumentSelectedListener.onSelected(view, data);
		}
	}

	public void setDocumentListMaker(AsyncFileListMaker<DocumentData> maker) {
		mAsyncFileListMaker = maker;
	}
	
	public void setOnDocumentSelectedListener( OnDocumentSelectedListener l ) {
		mOnDocumentSelectedListener = l;
	}
	
	private void makeList(Activity activity, Uri current,
						  FileExtensionFilter filter, boolean includeSubDir) {
		
		DocumentListAdapter listAdapter = new DocumentListAdapter(activity);
		mListView.setAdapter(listAdapter);
		if( mAsyncFileListMaker.isMaking( ) ) {
			mAsyncFileListMaker.cancel();
		}
		mAsyncFileListMaker.makeList(activity, current,
									 filter, listAdapter,
									 DocumentData.DEFAULT_COMPARATOR,
									 includeSubDir, this );
	}

	public void checkPermissionAndMakeList( Uri current,
											FileExtensionFilter filter,
											boolean includeSubDir ) {
		
		// Save the filter and includeDir parameter for the time one item is clicked
		if( filter == null ) {
			mFileExtFilter = new FileExtensionFilter( "All files", this );
		} else {
			mFileExtFilter = filter;
		}
		
		mIncludeSubDir = includeSubDir;
		
		// To void multiple threads operate same adapter making files in the list error
		Activity activity = getActivity();
		if( !checkPermission(activity, current) ) {
			forGrant( current, filter );
		} else {
			makeList(activity, current, filter, includeSubDir);
		}
		
	}
	
	private boolean checkPermission( Context c, Uri current ) {
		if( current == null ) {
			return true;
		}
		
		if( mOperation == DocumentOperation.LOAD ) {
			return DocumentFileUtilities.hasUriReadPermission(c, current);
		}
		
		return DocumentFileUtilities.hasUriWritePermission(c, current);
	}

	private void forGrant(Uri current, FileExtensionFilter filter) {
		Intent intent
			= getIntent( Intent.ACTION_OPEN_DOCUMENT_TREE, filter );
		intent.putExtra( KEY_URI_FOR_PERMISSION, current );
		startActivityForResult( intent, REQUEST_FOR_GRANT );
	}

	private Intent getIntent( String action, FileExtensionFilter filter ) {
		Intent intent = new Intent(action);
		return intent;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		Activity activity = getActivity();
		boolean needsRead
			= ActivityCompat.checkSelfPermission(
						activity, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED;

		boolean needsDocument
			= ActivityCompat.checkSelfPermission(
						activity, Manifest.permission.MANAGE_DOCUMENTS)
				!= PackageManager.PERMISSION_GRANTED;
		
		boolean needsWrite = false;
		if( mOperation == DocumentOperation.SAVE ) {
			needsWrite
				= ActivityCompat.checkSelfPermission(
							activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
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

		final Activity activity = getActivity();
		
		boolean justifyRead
			= ActivityCompat.shouldShowRequestPermissionRationale(
					activity, Manifest.permission.READ_EXTERNAL_STORAGE);
		
		boolean justifyWrite = false;
		
		if( mOperation == DocumentOperation.SAVE ) {
			justifyWrite 
				= ActivityCompat.shouldShowRequestPermissionRationale(
					activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
		}
		final String[] permissions
			= mOperation == DocumentOperation.LOAD
				? PERMISSIONS_READ_STORAGE : PERMISSIONS_STORAGE;

		// Storage permission has been requested and denied, show a Snackbar with the option to
		// grant permission
		if (justifyRead || justifyWrite ) {
			// Provide an additional rationale to the user if the permission was not granted
			// and the user would benefit from additional context for the use of the permission.
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder
				.setTitle( R.string.documenSelector_title_requestAccess)
				.setMessage( R.string.documenSelector_prompt_requestAccess )
				.setIcon( android.R.drawable.ic_dialog_alert )
				.setPositiveButton( android.R.string.ok, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						ActivityCompat.requestPermissions(activity,
								permissions, REQUEST_STORAGE_PERMISSION);
					}
				})
				.setNegativeButton( android.R.string.cancel, new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.finish();
					}
				} )
				.show();
		} else {
			// Storage permission has not been requeste yet. Request for first time.
			ActivityCompat
				.requestPermissions(
					activity, permissions, REQUEST_STORAGE_PERMISSION);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d( "DocumentListFragment Result: ", "requestCode", requestCode,
			   "resultCode", resultCode );
		switch( requestCode ) {
			case REQUEST_FOR_GRANT:
				doForGrant(resultCode, data);
				break;
		}
	}

	private void doForGrant(int resultCode, Intent data) {
		if( resultCode == Activity.RESULT_OK ) {
			int takeFlags
				= data.getFlags() 
				  & ( Intent.FLAG_GRANT_WRITE_URI_PERMISSION
					  | Intent.FLAG_GRANT_READ_URI_PERMISSION );

			Uri uri = data.getData();
			Activity activity = getActivity();
			activity.grantUriPermission(
					activity.getPackageName(), uri,
					Intent.FLAG_GRANT_READ_URI_PERMISSION
						| Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
						| Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
						| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
			
			activity.getContentResolver()
				.takePersistableUriPermission(uri, takeFlags );
			
			Uri current = data.getParcelableExtra(KEY_URI_FOR_PERMISSION);
			
			if( current == null ) {
				Uri doucmentUri
					= DocumentsContract.buildDocumentUriUsingTree(
							uri, DocumentsContract.getTreeDocumentId(uri));
				makeList(activity, doucmentUri, mFileExtFilter, mIncludeSubDir);
			} else {
				checkPermissionAndMakeList(current, mFileExtFilter, mIncludeSubDir);
			}
		} else {
			Toast.makeText(getActivity(),
						   R.string.documenSelector_prompt_accessDeny,
						   Toast.LENGTH_LONG )
				 .show();
		}
	}
	
	@Override
	public void show() {
		mMakingList = true;
		setMessage( "" );
	}

	@Override
	public void dismiss() {
		mMakingList = false;
		setMessage( "" );
	}

	private void setMessage( final CharSequence charSequence ) {
		if( mHandler == null ) {
			mHandler = new Handler( Looper.getMainLooper() );
		}
		mHandler.post( new Runnable() {
			@Override
			public void run() {
				mMessageView.setText(charSequence);
			}
		} );
	}

	private void setMessage( final int strId ) {
		Context context = getActivity();
		if( context != null ) {
			setMessage( context.getText(strId) );
		}
	}
	
	@Override
	public boolean notifyFile(String filename, boolean isFile) {
		if( mListView.getAdapter().getCount() % 10 == 0 ) {
			String message
				= getActivity()
					.getString( R.string.documenSelector_message_progress );
			
			setMessage( message + filename );
		}
		return mMakingList;
	}

	@Override
	public void beforeToMakeOrder() {
		setMessage( R.string.documenSelector_message_sorting );
	}

	@Override
	public void cancelled() {
		mMakingList = false;
	}

	@Override
	public void finished() {
		setMessage( "" );
	}

	@Override
	public void forAcception(String filename, boolean accepted) {
	}

	public void setOperation(DocumentOperation operation) {
		mOperation = operation;
	}
}
