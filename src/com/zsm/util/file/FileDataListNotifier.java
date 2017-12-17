package com.zsm.util.file;

public interface FileDataListNotifier {

	/**
	 * Show the notifier if necessary
	 * 
	 */
	void show( );
	
	/**
	 * Dismiss the notifier if necessary
	 * 
	 */
	void dismiss( );
	
	/**
	 * Notify that the file is being handled. When the file data list is made 
	 * in the method {@link FileDataListMaker.makeList}, this method MUST be invoked
	 * to notify the current file. And the retrieve MUST be check in case the 
	 * operation for the list is cancelled
	 * 
	 * @param filename
	 * 			Name of the file being handled
	 * @param isFile
	 * 			true, when the filename representing is a file; false, for a 
	 * 			directory
	 * @return true, when everything is OK. false, when the list list operation 
	 * 			is broken because of failure or cancel
	 */
	boolean notifyFile(String filename, boolean isFile);

	/**
	 * Invoked before to make the file list sorted
	 */
	void beforeToMakeOrder();

	/**
	 * Invoked when the list operation is cancelled
	 */
	void cancelled();

	/**
	 * Invoked when the list operation is finished
	 */
	void finished();

	/**
	 * Notified when the file is accepted or denied. This should be invoked
	 * in the methods like
	 * {@link com.zsm.util.file.android.StringFilter.accept StringFilter.accept}.
	 * 
	 * @param filename File name is accepted or denied
	 * @param accepted true, the file is accepted; false, it is denied
	 */
	void forAcception(String filename, boolean accepted);
}
