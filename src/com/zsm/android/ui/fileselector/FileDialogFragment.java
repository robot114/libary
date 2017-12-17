package com.zsm.android.ui.fileselector;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.zsm.R;
import com.zsm.util.file.AsyncFileListMaker;
import com.zsm.util.file.FileData;
import com.zsm.util.file.FileExtensionFilter;
import com.zsm.util.file.FileListMaker;

public class FileDialogFragment extends DialogFragment {

	private static final String KEY_CURRENT_LOCATION = "CURRENT_LOCATION";
	private static final String KEY_FILTERS = "FILTERS";
	private static final String KEY_OPERATION = "OPERATION";
	private static final String KEY_TITLE = "TITLE";
	private static final String KEY_INCL_SUBDIR = "INCLUDE_SUBDIR";
	
	private FileExtensionFilter[] fileFilters;
	private boolean includeSubDir;
	private String title;
	private Spinner mFilterSpinner;
	private File mCurrentLocation;
	private ListView mFileListView;
	private Button mSaveLoadButton;
	private Button mNewFolderButton;
	private Button mCancelButton;
	private FileOperation operation;
	private OnHandleFileListener onHandleFileListener;
	private EditText textFileName;
	private SaveLoadClickListener saveLoadClickListener;
	private FragmentManager fragmentManager;
	private boolean showFileName;
	private boolean mCustomerFilter;
	private TextView mFilterTextView;
	private Activity mActivity;
	private AlertDialog mFilterDialog;
	private AsyncFileListMaker mAsyncFileListMaker;
	private ListFileDataDialog mListFileDataDialog;

	public FileDialogFragment() {
		
	}

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
	public FileDialogFragment(Activity activity, String title,
							  final FileOperation operation,
							  final String currentPath,
							  final OnHandleFileListener onHandleFileListener,
							  final FileExtensionFilter[] filters,
							  final boolean includeSubDir,
							  final boolean showFileName,
							  final boolean customerFilter) {

		init(activity, title, operation, currentPath, onHandleFileListener,
			 filters, includeSubDir, showFileName, customerFilter );
	}

	public FileDialogFragment(Activity activity, String title,
							  final FileOperation operation,
							  final String currentPath,
							  final OnHandleFileListener onHandleFileListener,
							  final boolean includeSubDir,
							  final boolean showFileName) {
		
		init(activity, title, operation, currentPath, onHandleFileListener,
			 null, includeSubDir, showFileName, true );
	}

	public FileDialogFragment(Activity activity, String title,
							  final FileOperation operation,
							  final String currentPath,
							  final OnHandleFileListener onHandleFileListener,
							  final FileExtensionFilter[] filters,
							  final boolean includeSubDir,
							  final boolean showFileName ) {
				
		init(activity, title, operation, currentPath, onHandleFileListener,
			 filters, includeSubDir, showFileName, false );
	}

	public FileDialogFragment(Activity activity, String title,
							  final FileOperation operation,
							  final String currentPath,
							  final OnHandleFileListener onHandleFileListener,
							  final FileExtensionFilter[] filters) {

		init(activity, title, operation, currentPath, onHandleFileListener,
			 filters, true, true, false );
	}

	private void init(Activity activity, String title,
					  final FileOperation operation,
					  final String currentPath,
					  final OnHandleFileListener onHandleFileListener,
					  final FileExtensionFilter[] filters,
					  final boolean includeSubDir,
					  boolean showFileName,
					  boolean customerFilter) {
		
		mActivity = activity;
		fragmentManager = activity.getFragmentManager();
		this.title = title;
		this.includeSubDir = includeSubDir;

		this.operation = operation;
		this.onHandleFileListener = onHandleFileListener;
		this.fileFilters = filters;

		try {
			mCurrentLocation = new File( currentPath );
		} catch ( Exception e ) {
		}
		
		if( mCurrentLocation != null && !mCurrentLocation.isDirectory() ) {
			mCurrentLocation = mCurrentLocation.getParentFile();
		}
		
		if( mCurrentLocation == null || !mCurrentLocation.exists() ) {
			final File sdCard = Environment.getExternalStorageDirectory();
			if (sdCard.canRead()) {
				mCurrentLocation = sdCard;
			} else {
				mCurrentLocation = Environment.getRootDirectory();
			}
		}
		
		saveLoadClickListener
			= new SaveLoadClickListener(operation, this, activity);
		this.showFileName = showFileName;
		
		mCustomerFilter = customerFilter;
		
		mListFileDataDialog = new ListFileDataDialog(mActivity);
		mAsyncFileListMaker = new AsyncFileListMaker( new FileListMaker() );
	}

	public FileDialogFragment( Activity activity, String title,
							   final FileOperation operation,
							   final String currentPath,
							   final OnHandleFileListener onHandleFileListener,
							   final String[] extensions,
							   final String filterDescription,
							   final boolean includeSubDir,
							   final boolean showFileName ) {

		FileExtensionFilter ff
			= new FileExtensionFilter( extensions, filterDescription );
		
		init( activity, title, operation, currentPath, onHandleFileListener,
			  new FileExtensionFilter[]{ ff }, includeSubDir, showFileName,
			  false );
	}
	
    @Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putString( KEY_TITLE, title );
		outState.putString( KEY_OPERATION, operation.name() );
		outState.putParcelableArray( KEY_FILTERS, fileFilters );
		outState.putString( KEY_CURRENT_LOCATION, mCurrentLocation.getPath() );
		outState.putBoolean( KEY_INCL_SUBDIR, includeSubDir);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if( savedInstanceState != null ) {
			title = savedInstanceState.getString( KEY_TITLE );
			operation
				= FileOperation.valueOf( 
						savedInstanceState.getString( KEY_OPERATION ) );
			fileFilters
				= (FileExtensionFilter[]) savedInstanceState
											.getParcelableArray( KEY_FILTERS );
			mCurrentLocation
				= new File( savedInstanceState.getString( KEY_CURRENT_LOCATION ) );
			includeSubDir = savedInstanceState.getBoolean(KEY_INCL_SUBDIR);
		}
	}

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            				 Bundle savedInstanceState) {
        // Inflate the layout to use as dialog or embedded fragment
        View view
        	= inflater.inflate(R.layout.file_selector_dialog, container, false);
        
		textFileName = (EditText) view.findViewById(R.id.fileName);
		if( !showFileName ) {
			textFileName.setVisibility( View.GONE );
			view.findViewById(R.id.fileTextView).setVisibility( View.GONE );
		}
		
		prepareFilterSpinner(view, fileFilters);
		prepareFilesList(view);

		setSaveLoadButton(view, operation);
		setNewFolderButton(view, operation);
		setCancelButton(view);

		View textLayout = view.findViewById( R.id.fileFilterTextLayout );
		mFilterTextView = (TextView)view.findViewById( R.id.fileFilterText );
		if( mCustomerFilter ) {
			ImageView imageFileFilter
				= (ImageView)view.findViewById( R.id.fileFilterImage );
			textLayout.setVisibility( View.VISIBLE );
			imageFileFilter.setOnClickListener( new OnClickListener() {
				@Override
				public void onClick(View v) {
					initFilterDialog();
					mFilterDialog.show();
				}
			} );
		} else {
			textLayout.setVisibility( View.GONE );
		}
		
        return view;
    }

	private void initFilterDialog() {
		if( mFilterDialog != null ) {
			return;
		}
		
		final EditText edit = new EditText(mActivity);
		
		final OnClickListener okListener = new OnClickListener(){
			@Override
			public void onClick(View v) {
				FileExtensionFilter filter = null;
				try {
					filter = new FileExtensionFilter( edit.getText().toString(),
													  "", mListFileDataDialog );
				} catch ( IllegalArgumentException e ) {
					Toast.makeText( mActivity, R.string.messageInvalidFileFilter,
									Toast.LENGTH_SHORT )
						 .show();
					return;
				}
				
				mFilterTextView.setText( filter.getExtDescription() );
				makeList(mCurrentLocation, filter);
				mFilterDialog.dismiss();
			}
		};
		
		mFilterDialog = new AlertDialog.Builder(mActivity)
			.setTitle( R.string.titleFileFilter )
			.setMessage( R.string.messageFileFilter )
			.setView(edit)
			.setCancelable(false)
			.setPositiveButton( android.R.string.ok,  null )
			.setNegativeButton( android.R.string.cancel, null )
			.create();
		mFilterDialog.setOnShowListener(new DialogInterface.OnShowListener() {

		    @Override
		    public void onShow(DialogInterface dialog) {
		        Button button
		        	= ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
		        button.setOnClickListener(okListener);
				edit.setText( mFilterTextView.getText() );
				edit.selectAll();
		    }
		});
	}
	
    /** The system calls this only when creating the layout in a dialog. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if( title == null || title.length() == 0 ) {
        	dialog.setTitle( operation.name() );
        } else {
        	dialog.setTitle(title);
        }
        
        dialog.getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
        							LayoutParams.FLAG_FULLSCREEN);
        
        return dialog;
    }

	/**
	 * This method prepares a filter's list with the String's array
	 * 
	 * @param aFilesFilter
	 *            - array of filters, the elements of the array will be used as
	 *            elements of the spinner
	 */
	private void prepareFilterSpinner(View view,
									  FileExtensionFilter[] fitlesFilter) {
		
		mFilterSpinner = (Spinner) view.findViewById(R.id.fileFilter);
		if (fitlesFilter == null || fitlesFilter.length == 0) {
			fitlesFilter
				= new FileExtensionFilter[] { 
						new FileExtensionFilter("All files", mListFileDataDialog ) };
			mFilterSpinner.setEnabled(false);
		}
		ArrayAdapter<FileExtensionFilter> adapter
			= new ArrayAdapter<FileExtensionFilter>(
					getActivity(), R.layout.file_selector_spinner_item,
					fitlesFilter );

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
				mFilterTextView.setText( filter.getExtDescription() );
				saveLoadClickListener.setFileExtensionFilter( filter );
				makeList(mCurrentLocation, filter);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		};
		mFilterSpinner.setOnItemSelectedListener(onItemSelectedListener);
	}

	/**
	 * This method prepares the mFileListView
	 * @param view 
	 * 
	 */
	private void prepareFilesList(final View view) {
		mFileListView = (ListView) view.findViewById(R.id.fileList);

		mFileListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> parent, final View view,
									final int position, final long id) {
				
				// Check if "../" item should be added.
				textFileName.setText("");
				if (id == 0) {
					final String parentLocation = mCurrentLocation.getParent();
					if (parentLocation != null) { // text == "../"
						mCurrentLocation = new File(parentLocation);
						makeList();
					} else {
						onItemSelect(view, parent, position);
					}
				} else {
					onItemSelect(view, parent, position);
				}
			}
		});
		
		makeList();
	}

	/**
	 * The method that fills the list with a directories contents.
	 * 
	 * @param location
	 *            Indicates the directory whose contents should be displayed in
	 *            the dialog.
	 * @param filesFilter
	 *            The filter specifies the type of file to be displayed
	 */
	private void makeList(final File location, final FileExtensionFilter filesFilter) {
		// To void multiple threads operate same adapter making files in the list error
		FileListAdapter listAdapter = new FileListAdapter(getActivity());
		mFileListView.setAdapter(listAdapter);
		mListFileDataDialog.show();
		
		mAsyncFileListMaker.makeList(Uri.fromFile(location), filesFilter,
									 listAdapter, includeSubDir,
									 mListFileDataDialog );
	}

	private void makeList() {
		FileExtensionFilter filtr
			= (FileExtensionFilter) mFilterSpinner.getSelectedItem();
		makeList(mCurrentLocation, filtr);
	}

	/**
	 * Handle the file list item selection.
	 * 
	 * Change the directory on the list or change the name of the saved file if
	 * the user selected a file.
	 * 
	 * @param parent
	 *            First parameter of the onItemClick() method of
	 *            OnItemClickListener. It's a value of text property of the
	 *            item.
	 * @param position
	 *            Third parameter of the onItemClick() method of
	 *            OnItemClickListener. It's the index on the list of the
	 *            selected item.
	 */
	private void onItemSelect(View view, final AdapterView<?> parent,
							 final int position) {
		final String itemText
			= ((FileData) parent.getItemAtPosition(position)).getFileName();
		final String itemPath
			= mCurrentLocation.getAbsolutePath() + File.separator + itemText;
		final File itemLocation = new File(itemPath);

		if (!itemLocation.canRead()) {
			Toast.makeText(getActivity(), "Access denied!!!", Toast.LENGTH_SHORT).show();
		} else if (itemLocation.isDirectory()) {
			mCurrentLocation = itemLocation;
			makeList();
		} else if (itemLocation.isFile()) {
			textFileName.setText(itemText);
			if( !showFileName && operation == FileOperation.LOAD ) {
				saveLoadClickListener.onClick( mSaveLoadButton );
			}
		}
	}

	/**
	 * Set button name and click handler for Save or Load button.
	 * 
	 * @param operation
	 *            Performed file operation.
	 */
	private void setSaveLoadButton(View view, final FileOperation operation) {
		mSaveLoadButton = (Button) view.findViewById(R.id.fileSaveLoad);
		switch (operation) {
			case SAVE:
				mSaveLoadButton.setText(R.string.saveButtonText);
				break;
			case LOAD:
			case FOLDER:
				mSaveLoadButton.setText(R.string.loadButtonText);
				break;
			default:
				throw new IllegalArgumentException(
							"Invalid operation: " + operation );
		}
		mSaveLoadButton.setOnClickListener(saveLoadClickListener);
	}

	/**
	 * Set button visibility and click handler for New folder button.
	 * 
	 * @param operation
	 *            Performed file operation.
	 */
	private void setNewFolderButton(View view, final FileOperation operation) {
		mNewFolderButton = (Button) view.findViewById(R.id.newFolder);
		OnClickListener newFolderListener = new OnClickListener() {
			@Override
			public void onClick(final View v) {
				openNewFolderDialog();
			}
		};
		switch (operation) {
			case SAVE:
				mNewFolderButton.setVisibility(View.VISIBLE);
				mNewFolderButton.setOnClickListener(newFolderListener);
				break;
			case FOLDER:
			case LOAD:
				mNewFolderButton.setVisibility(View.GONE);
				break;
			default:
				throw new IllegalArgumentException(
							"Invalid operation: " + operation );
		}
	}

	/** Opens a dialog for creating a new folder. */
	private void openNewFolderDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
		alert.setTitle(R.string.newFolderButtonText);
		alert.setMessage(R.string.newFolderDialogMessage);
		final EditText input = new EditText(getActivity());
		alert.setView(input);
		alert.setPositiveButton(R.string.createButtonText, 
								new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int whichButton) {
				String path
					= mCurrentLocation.getAbsolutePath()
						+ File.separator + input.getText().toString();
				
				File file = new File(path);
				int resId;
				if (file.mkdir()) {
					resId = R.string.folderCreationOk;
				} else {
					resId = R.string.folderCreationError;
				}
				Toast t = Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				
				makeList();
			}
		});
		alert.show();
	}

	/** Set onClick() event handler for the cancel button. */
	private void setCancelButton( View view ) {
		mCancelButton = (Button) view.findViewById(R.id.fileCancel);
		mCancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View view) {
				dismiss();
			}
		});
	}

	public String getSelectedFileName() {
		return textFileName.getText().toString();
	}

	public File getCurrentLocation() {
		return mCurrentLocation;
	}

	public void handleFile(FileOperation mOperation, String filePath) {
		onHandleFileListener.handleFile(mOperation, filePath);
	}

	public void showDialog() {
        // The device is smaller, so show the fragment fullscreen
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // For a little polish, specify a transition animation
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        // To make it fullscreen, use the 'content' root view as the container
        // for the fragment, which is always the root view for the activity
        transaction.add(0, this).addToBackStack(null).commit();
//	    show( fragmentManager, "fileDialog" );
	}

}
