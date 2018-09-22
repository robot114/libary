package com.zsm.android.ui.documentSelector;

import com.zsm.R;
import com.zsm.log.Log;
import com.zsm.util.file.FileDataListMakerNotifier;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.android.DocumentData;
import com.zsm.util.file.android.DocumentFileUtilities;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.provider.DocumentFile;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class DocumentSelectorFragment extends DialogFragment 
				implements DocumentUserInterface, FileDataListMakerNotifier {

	private static final String KEY_CURRENT_LOCATION = "CURRENT_LOCATION";
	private static final String KEY_FILTERS = "FILTERS";
	private static final String KEY_OPERATION = "OPERATION";
	private static final String KEY_TITLE = "TITLE";
	private static final String KEY_INCL_SUBDIR = "INCLUDE_SUBDIR";
	
	private static final String STORAGE_PRIMARY_URI_STR
		= "content://com.android.externalstorage.documents/tree/primary%3A";

	private static final Uri STORAGE_PRIMARY_URI = Uri.parse( STORAGE_PRIMARY_URI_STR );
	
	private View mView;
	private ListView mDocumentList;
	private Spinner mFilterSpinner;
	private EditText mNameView;
	
	private SelectActionClickListener mSaveLoadClickListener;
	private Activity mActivity;
	private String mTitle;
	private boolean mIncludeSubDir;
	private DocumentOperation mOperation;
	private DocumentHandler mDocumentHandler;
	private FileExtensionFilter[] mFileFilters;
	private DocumentFile mCurrentLocation;
	private boolean mShowFileName;
	private boolean mCustomerFilter;
	
	private PermissedListMaker mPermissedListMaker;
	private TextView mMessageView;
	
	/**
	 * Constructor with the full parameters list.
	 * 
	 * @param activity Activity to attach this fragment
	 * @param title Title of the file dialog
	 * @param operation	LOAD/SAVE/FOLDER
	 * @param currentPath Path from which to list the files
	 * @param onHandleFileListener Notified when the positive button clicked.
	 * @param filters Filters to list the files
	 * @param includeSubDir True, all the files are list in the dialog 
	 * 				including the folders; false, only files are list
	 * @param showFileName	Whether there is a view to display the file name
	 * 				selected from the file list. When the file name view is
	 * 				shown, the user can input the file in it. When it is not
	 * 				shown, and the selection of a FILE from the file list will
	 * 				equal to click the load button when the operation is LOAD
	 * @param customerFilter Whether there are views to enable the user to 
	 * 				input the extensions for filtering the files in the list
	 */
	public DocumentSelectorFragment(final Activity activity, String title,
							    	final DocumentOperation operation,
							    	final Uri currentPath,
							    	final DocumentHandler handler,
							    	final FileExtensionFilter[] filters,
							    	final boolean includeSubDir,
							    	final boolean showFileName,
							    	final boolean customerFilter) {

		init(activity, title, operation, currentPath, handler,
			 filters, includeSubDir, showFileName, customerFilter );
	}

	public DocumentSelectorFragment(Activity activity, String title,
							    	final DocumentOperation operation,
							    	final Uri currentPath,
							    	final DocumentHandler handler,
							    	final boolean includeSubDir,
							    	final boolean showFileName) {
		
		init(activity, title, operation, currentPath, handler,
			 null, includeSubDir, showFileName, true );
	}

	public DocumentSelectorFragment(Activity activity, String title,
							    	final DocumentOperation operation,
							    	final Uri currentPath,
							    	final DocumentHandler handler,
							    	final FileExtensionFilter[] filters,
							    	final boolean includeSubDir,
							    	final boolean showFileName ) {
				
		init(activity, title, operation, currentPath, handler,
			 filters, includeSubDir, showFileName, false );
	}

	public DocumentSelectorFragment(Activity activity, String title,
							    	final DocumentOperation operation,
							    	final Uri currentPath,
							    	final DocumentHandler handler,
							    	final FileExtensionFilter[] filters) {

		init(activity, title, operation, currentPath, handler,
			 filters, true, true, false );
	}

	public DocumentSelectorFragment(Activity activity, String title,
									final DocumentOperation operation,
									final Uri currentPath,
									final DocumentHandler handler,
									final String[] extensions,
									final String filterDescription,
									final boolean includeSubDir,
									final boolean showFileName) {

		FileExtensionFilter ff
			= new FileExtensionFilter(extensions, filterDescription);

		init(activity, title, operation, currentPath, handler,
			 new FileExtensionFilter[] { ff }, includeSubDir, showFileName,
			 false);
	}

	private void init(Activity activity, String title,
					  final DocumentOperation operation,
					  final Uri currentPath,
					  final DocumentHandler handler,
					  final FileExtensionFilter[] filters,
					  final boolean includeSubDir,
					  boolean showFileName,
					  boolean customerFilter) {
		
		mActivity = activity;
		mTitle = title;
		mIncludeSubDir = includeSubDir;

		mOperation = operation;
		mDocumentHandler = handler;
		mFileFilters = filters;

		initCurrentLocation(currentPath);
		
		mSaveLoadClickListener
			= new SelectActionClickListener(operation, this, mDocumentHandler,
											activity);
		mShowFileName = showFileName;
		
		mCustomerFilter = customerFilter;
	}

	private void initCurrentLocation(final Uri currentPath) {
		mCurrentLocation = null;
		
		if( currentPath != null ) {
			mCurrentLocation = directoryFromUri(currentPath);
		}
		if( mCurrentLocation == null ) {
			mCurrentLocation
				= DocumentFile.fromTreeUri(mActivity, STORAGE_PRIMARY_URI);
		}
	}

	/**
	 * Get the directory DocumentFile from the uri. If the uri is pointed to
	 * a directory, the retrieved DocumentFile's documentId will be the same
	 * as the uri. If the uri is to a file, the parent of the file will be
	 * returned.
	 * 
	 * @param uri the uri
	 * @return A DocumentFile instance from the uri or its parent if successfully,
	 * 		null otherwise
	 */
	private DocumentFile directoryFromUri(final Uri uri) {
		Uri pathUri = null;
		try {
			pathUri = DocumentFileUtilities.getPathUri(mActivity, uri, false);
		} catch( Exception e ) {
			return null;
		}
		
		return DocumentFile.fromSingleUri(mActivity, pathUri);
	}

    @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString( KEY_TITLE, mTitle );
		outState.putString( KEY_OPERATION, mOperation.name() );
		outState.putParcelableArray( KEY_FILTERS, mFileFilters );
		if( mCurrentLocation != null && mCurrentLocation.getUri() != null ) {
			outState.putString( KEY_CURRENT_LOCATION,
								mCurrentLocation.getUri().toString() );
		}
		outState.putBoolean( KEY_INCL_SUBDIR, mIncludeSubDir);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( savedInstanceState != null ) {
			mTitle = savedInstanceState.getString( KEY_TITLE );
			mOperation
				= DocumentOperation.valueOf( 
						savedInstanceState.getString( KEY_OPERATION ) );
			mFileFilters
				= (FileExtensionFilter[]) savedInstanceState
											.getParcelableArray( KEY_FILTERS );
			String ls = savedInstanceState.getString( KEY_CURRENT_LOCATION );
			initCurrentLocation( Uri.parse(ls) );
			mIncludeSubDir = savedInstanceState.getBoolean(KEY_INCL_SUBDIR);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if( mView == null ) {
			mView
				= inflater.inflate( R.layout.document_selector_dialog, container,
									false );
			mDocumentList
				= (ListView)mView.findViewById( R.id.listViewDocument );
			
			mDocumentList.setOnItemClickListener( new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,
										int position, long id) {
					
					onDocumentClick(
						(DocumentListAdapter) parent.getAdapter(), view, position);
				}
			} );
			
			DocumentListAdapter adapter = new DocumentListAdapter(mActivity);
			mDocumentList.setAdapter(adapter);
			
			mMessageView = (TextView)mView.findViewById( R.id.textViewMessage );
			
			prepareFilterSpinner(mView, mFileFilters);
			
			mNameView = (EditText)mView.findViewById( R.id.editTextName );
			
			View selectBtn = mView.findViewById( R.id.buttonOk );
			selectBtn.setOnClickListener(mSaveLoadClickListener);
			View cancelBtn = mView.findViewById( R.id.buttonCancel );
			cancelBtn.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismiss();
				}
			} );
			
			mPermissedListMaker
				= new PermissedListMaker( this, adapter, this, mOperation );
			
			getDialog().setTitle(mTitle);
		}
		return mView;
	}

	private void onDocumentClick(DocumentListAdapter adapter, View view, int position) {

		DocumentData data = adapter.getItem(position);
		if (data.getType() != DocumentData.TYPE.DOCUMENT) {
			mPermissedListMaker
				.checkPermissionAndMakeList(data.getUri(),
						(FileExtensionFilter) mFilterSpinner.getSelectedItem(),
						mIncludeSubDir);
		}

		mCurrentLocation = directoryFromUri( data.getUri() );
		if( data.getType() == DocumentData.TYPE.DOCUMENT ) {
			mNameView.setText( data.getName() );
		} else {
			mNameView.setText( "" );
		}
	}

	/**
	 * This method prepares a filter's list with the String's array
	 * 
	 * @param fileFilters
	 *            - array of filters, the elements of the array will be used as
	 *            elements of the spinner
	 */
	private void prepareFilterSpinner(View view,
									  FileExtensionFilter[] fileFilters) {
		
		mFilterSpinner = (Spinner) view.findViewById(R.id.fileFilter);
		if (fileFilters == null || fileFilters.length == 0) {
			fileFilters
				= new FileExtensionFilter[] { 
						new FileExtensionFilter("All files", this ) };
			mFilterSpinner.setEnabled(false);
		}
		ArrayAdapter<FileExtensionFilter> adapter
			= new ArrayAdapter<FileExtensionFilter>(
					getActivity(), R.layout.file_selector_spinner_item,
					fileFilters );

		mFilterSpinner.setAdapter(adapter);
		OnItemSelectedListener onItemSelectedListener
			= new OnItemSelectedListener() {

			@Override
			public void onItemSelected( AdapterView<?> aAdapter, View aView,
										int position, long id ) {
				
				if( aView == null ) {
					return;
				}
				
				FileExtensionFilter filter
					= (FileExtensionFilter) aAdapter.getItemAtPosition(position);
				mSaveLoadClickListener.setFileExtensionFilter( filter );
				mPermissedListMaker.checkPermissionAndMakeList(
						mCurrentLocation.getUri(), filter, mIncludeSubDir);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		};
		mFilterSpinner.setOnItemSelectedListener(onItemSelectedListener);
	}
	
	@Override
	public Uri getCurrentLocation() {
		return mCurrentLocation.getUri();
	}

	@Override
	public String getInputedName() {
		return mNameView.getText().toString();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d( "DocumentListFragment Result: ", "requestCode", requestCode,
			   "resultCode", resultCode );
		switch( requestCode ) {
			case PermissedListMaker.REQUEST_FOR_GRANT:
				mPermissedListMaker
					.doForGrant(resultCode, data,
						(FileExtensionFilter)mFilterSpinner.getSelectedItem(),
						mIncludeSubDir );
				break;
		}
	}

	@Override
	public void show() {
		mPermissedListMaker.setMakingListFlag();
		setMessage( "" );
	}

	@Override
	public void dismiss() {
		mPermissedListMaker.clearMakingListFlag();
		setMessage( "" );
		super.dismiss();
	}
	
	private CharSequence mMessage = "";
	private Handler mHandler;
	private Runnable mMessageShower;
	
	private void setMessage( final CharSequence charSequence ) {
		mMessage = charSequence;
		
		if( mHandler == null ) {
			mHandler = new Handler( Looper.getMainLooper() );
		}
		if( mMessageShower == null ) {
			mMessageShower = new Runnable() {
				@Override
				public void run() {
					mMessageView.setText(mMessage);
					mMessageShower = null;
					System.out.println( mMessage );
				}
			};
			mHandler.post( mMessageShower );
		}
	}

	private void setMessage( final int strId ) {
		setMessage( mActivity.getText(strId) );
	}
	
	@Override
	public boolean notifyFile(String filename, boolean isFile) {
		String message
			= getActivity()
				.getString( R.string.documenSelector_message_progress );
		
		setMessage( message + filename );
		
		return mPermissedListMaker.isMakingListFlagSet();
	}

	@Override
	public void beforeToMakeOrder() {
		setMessage( R.string.documenSelector_message_sorting );
	}

	@Override
	public void cancelled() {
		mPermissedListMaker.clearMakingListFlag();
	}

	@Override
	public void finished() {
		setMessage( "" );
	}

	@Override
	public void forAcception(String filename, boolean accepted) {
	}

	public void showDialog() {
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction
        	= mActivity.getFragmentManager().beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(0, this).addToBackStack(null).commit();
//	    show( fragmentManager, "fileDialog" );
	}

}
