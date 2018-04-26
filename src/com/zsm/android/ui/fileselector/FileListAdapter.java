package com.zsm.android.ui.fileselector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.zsm.R;
import com.zsm.android.ui.TextViewWithImage;
import com.zsm.util.file.FileData;
import com.zsm.util.file.SortableAdapter;

/**
 * Adapter used to display a files list
 */
public class FileListAdapter extends BaseAdapter implements SortableAdapter<FileData> {

	/** Array of FileData objects that will be used to display a list */
	private final ArrayList<FileData> mFileDataArray;

	private final Context mContext;

	private Handler mHandler;

	public FileListAdapter(Context context) {
		mFileDataArray = new ArrayList<FileData>( 16 );
		mContext = context;
		mHandler = new Handler(Looper.getMainLooper());
	}

	@Override
	public int getCount() {
		return mFileDataArray.size();
	}

	@Override
	public Object getItem(int position) {
		return mFileDataArray.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileData tempFileData = mFileDataArray.get(position);
		TextViewWithImage tempView = new TextViewWithImage(mContext);
		tempView.setText(tempFileData.getFileName());
		int imgRes = -1;
		switch (tempFileData.getFileType()) {
			case FileData.UP_FOLDER: {
				imgRes = R.drawable.folder_up;
				break;
			}
			case FileData.DIRECTORY: {
				imgRes = R.drawable.folder;
				break;
			}
			case FileData.FILE: {
				imgRes = R.drawable.file;
				break;
			}
		}
		tempView.setImageResource(imgRes);
		return tempView;
	}

	@Override
	public void add(final FileData file) {
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			addAndNotifyInner(file);
		} else {
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					addAndNotifyInner(file);
				}
			});
		}
	}

	private void addAndNotifyInner(FileData file) {
		mFileDataArray.add(file);
		super.notifyDataSetChanged();
	}

	@Override
	public void clear() {
		mFileDataArray.clear();
	}

	@Override
	public void sort( final Comparator<FileData> c ) {
		if( Looper.myLooper() == Looper.getMainLooper() ) {
			sortAndNotifyInner(c);
		} else {
			mHandler.post( new Runnable() {
				@Override
				public void run() {
					sortAndNotifyInner(c);
				}
			});
		}
	}

	private void sortAndNotifyInner(Comparator<FileData> c) {
		Collections.sort(mFileDataArray, c);
	}
}
