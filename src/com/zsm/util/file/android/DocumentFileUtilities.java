package com.zsm.util.file.android;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.zsm.log.Log;
import com.zsm.util.file.FileDataListMakerNotifier;


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import static android.provider.DocumentsContract.Document.*;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

public class DocumentFileUtilities {

	private static final String[] PROJECTION_DOCUMENT_ID = new String[] {
	        DocumentsContract.Document.COLUMN_DOCUMENT_ID };
	private static final String SLASH_STRING = "%2F";
	private static final int SLASH_STR_LEN = SLASH_STRING.length();
	private static final String COLON_STRING = "%3A";
	private static final int COLON_STR_LEN = COLON_STRING.length();
	private static final String COLON_CHAR = ":";
	
	private static final String PATH_ROOT = "root";
	private static final String PATH_DOCUMENT = "document";
	private static final String PATH_TREE = "tree";
	private static final String EXTERNAL_STORAGE_AUTHORITY
		= "com.android.externalstorage.documents";
	private static final String EXTERNAL_STORAGE_PRIMARY_ID = "primary";
	private static final String EXTERNAL_STORAGE_PRIMARY_PATH
		= EXTERNAL_STORAGE_PRIMARY_ID + COLON_STRING;

	private static final String[] LISTFILE_QUERY_COLS
		= new String[] { COLUMN_DOCUMENT_ID, COLUMN_MIME_TYPE };

	@SuppressLint("NewApi")
	public static DocumentFile[] listFiles(Context context, Uri path,
										   StringFilter filter,
										   boolean includeSubDir,
										   FileDataListMakerNotifier notifier) {

		final ContentResolver resolver = context.getContentResolver();
		String docId;
		if( DocumentsContract.isDocumentUri( context, path ) ) {
			docId = DocumentsContract.getDocumentId(path);
		} else  if( DocumentsContract.isTreeUri( path) ) {
			docId = DocumentFileUtilities.getTreeDocumentId(path);
		} else {
			return null;
		}
		
		final Uri childrenUri
					= DocumentsContract.buildChildDocumentsUriUsingTree( path, docId );
		final ArrayList<DocumentFile> results = new ArrayList<DocumentFile>();

		try (Cursor c = resolver.query(childrenUri, LISTFILE_QUERY_COLS,
									   null, null, null)) {

			int idIndex = c.getColumnIndex( COLUMN_DOCUMENT_ID );
			int typeIndex = c.getColumnIndex( COLUMN_MIME_TYPE );
			while (c.moveToNext()) {
				if(!oneFile(context, path, filter, includeSubDir, notifier,
							results, c, idIndex, typeIndex) ) {
					
					break;
				}
			}
		}

		return results.toArray(new DocumentFile[results.size()]);
	}

	private static boolean oneFile(Context context, Uri path,
								   StringFilter filter,
								   boolean includeSubDir,
								   FileDataListMakerNotifier notifier,
								   final ArrayList<DocumentFile> results,
								   Cursor c, int idIndex, int typeIndex) {
		
		final String type = c.getString(typeIndex);
		final boolean isDir = isDirectory( type );
		final boolean isFile = isFile(type);
		if( isFile || ( isDir && includeSubDir ) ) {
			final String documentId = c.getString(idIndex);
			final Uri documentUri = DocumentsContract
					.buildDocumentUriUsingTree(path, documentId);
			if( notifier != null ) {
				if( !notifier.notifyFile( 
						documentUri.getLastPathSegment(), !isFile )) {
					
					return false;
				}
			}
			if( isDir ) {
				final DocumentFile doc
					= DocumentFile.fromSingleUri(context, documentUri);
		
				results.add(doc);
			} else if( filter.accept( documentUri.toString() ) ) {
				final DocumentFile doc
					= DocumentFile.fromSingleUri(context, documentUri);
		
				results.add(doc);
			}
		}
		
		return true;
	}

    public static boolean isDirectory(String type) {
    	return DocumentsContract.Document.MIME_TYPE_DIR.equals(type);
    }
    
    public static boolean isFile(String type) {
        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(type)
        	|| TextUtils.isEmpty(type)) {
        	
            return false;
        } else {
            return true;
        }
    }

    public static Uri getTreeParentUri( Uri treeUri ) {
    	String documentId;
    	
    	if( isTreeDocuemntUri(treeUri) ) {
    		documentId = getEncodeDocumentId(treeUri);
    	} else if( isTreeUri(treeUri) ) {
    		documentId = getEncodeTreeId(treeUri);
    	} else {
    		throw new IllegalArgumentException( treeUri + " is not a tree uri!" );
    	}
    	
    	String parentId = getParentPath(documentId, true);
    	
    	if( parentId == null ) {
    		return null;
    	}
    	
    	if( isTreeDocuemntUri(treeUri) ) {
    		String treeId = getEncodeTreeId(treeUri);
    		if( !parentId.startsWith(treeId) ) {
    			return buildEncodeTreeDocumentUri( treeUri.getAuthority(),
    											   parentId, parentId);
    		}
    	}
    	
    	return buildDocumentUriUsingEncodeTree(treeUri, parentId);
    }
    
    /**
     * Get the path part of the uri. If the uri points to a directory,
     * the uri itself will be returned. Otherwise, the uri without the documentId
     * will be returned.
     * @param context 
     * @param uri
     * @return the path part of the uri
     */
    public static Uri getEncodePathUri( Context context, Uri uri ) {
    	if( DocumentsContract.isDocumentUri(context, uri) ) {
        	DocumentFile df = DocumentFile.fromSingleUri(context, uri);
    		if( df.isDirectory() ) {
    			return uri;
    		}
    	}
    	
    	return getEncodeParentUri(uri);
    }

	public static Uri buildDocumentUriUsingEncodeTree( Uri treeUri,
													   String documentId ) {
		String encodeTreeId = getEncodeTreeId(treeUri);
		String authority = treeUri.getAuthority();
		return buildEncodeTreeDocumentUri(authority, encodeTreeId, documentId);
	}

	public static Uri buildEncodeTreeDocumentUri(String authority,
												 String encodeTreeId,
												 String documentId) {
		
		if( !encodeTreeId.startsWith( documentId ) ) {
			throw new InvalidParameterException( 
				encodeTreeId + " is not the descendant directory of " + documentId );
		}
		
		return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
		        .authority(authority).appendPath(PATH_TREE)
		        .appendEncodedPath(encodeTreeId)
		        .appendPath(PATH_DOCUMENT).appendEncodedPath(documentId).build();
	}
    
	public static Uri removeDocumentIdFromEncodeTreeDocumentUri( Uri treeDocumentUri ) {

		return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
				.authority(treeDocumentUri.getAuthority())
				.appendPath(PATH_TREE)
				.appendEncodedPath(getEncodeTreeId(treeDocumentUri)).build();
	}

	public static Uri buildTreeUriUsingEncodeDocument( Uri treeDocumentUri ) {

		return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
				.authority(treeDocumentUri.getAuthority())
				.appendPath(PATH_TREE)
				.appendEncodedPath(getEncodeDocumentId(treeDocumentUri)).build();
	}

	public static Uri buildExternalStorageTreeDocumentUri( boolean isPrimary,
			   											   String uuid ) {

		String path;
		if( isPrimary ) {
			path = EXTERNAL_STORAGE_PRIMARY_PATH;
		} else {
			path = uuid + COLON_STRING;
		}
		return buildExternalStorageTreeDocumentUri( path );
	}

	public static Uri buildExternalStorageTreeDocumentUri( String path ) {
	
		return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT)
				.authority(EXTERNAL_STORAGE_AUTHORITY).appendPath(PATH_TREE)
				.appendEncodedPath(path)
				.appendPath(PATH_DOCUMENT)
				.appendEncodedPath(path)
				.build();
	}
	
	public static boolean isExternalStorageVolumeUri( Uri uri ) {
		return uri.getAuthority().equals( EXTERNAL_STORAGE_AUTHORITY )
				&& uri.getLastPathSegment().endsWith( COLON_STRING );
	}

    public static Uri getEncodeParentUri( Uri uri ) {
    	// content://com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2Fa.xml
    	// --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2Fa.xml
    	String path = uri.getEncodedPath();
    	
    	String parent = getParentPath( path, true );
    	
    	if( parent == null ) {
    		return null;
    	}
    	
    	Uri.Builder b = uri.buildUpon();
    	b.encodedPath(parent);
    	
    	return b.build();
    }
    
    private static String getParentPath( String path, boolean withLastSlash ) {
    	String parent = path;
    	
    	int colonIndex = parent.lastIndexOf( COLON_STRING );
    	if( colonIndex <= 0 ) {
    		throw new IllegalArgumentException( "No mount point in the uri: " + parent );
    	}
    	if( colonIndex == parent.length() - COLON_STR_LEN ) {
    		// com.android.externalstorage.documents/document/primary%3A
    		return null;
    	}
    	int lastSlash = parent.lastIndexOf( SLASH_STRING );
    	if( lastSlash == parent.length() - SLASH_STR_LEN ) {
    		// com.android.externalstorage.documents/document/primary%3Abackup%2F
        	// --> com.android.externalstorage.documents/document/primary%3Abackup
        	// com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2F
        	// --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data
    		parent = parent.substring( 0, lastSlash );
        	lastSlash = parent.lastIndexOf( SLASH_STRING );
    	}
    	if( lastSlash < 0 ) {
    		// com.android.externalstorage.documents/document/primary%3Abackup
    		// --> com.android.externalstorage.documents/document/primary%3A
    		parent = parent.substring( 0, colonIndex + COLON_STR_LEN );
    	} else {
        	// com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2Fa.xml
    		// --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2F
    		// or --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data
        	// com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data
    		// --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20
    		// or --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest
    		int end = withLastSlash ? (lastSlash + SLASH_STR_LEN) : lastSlash;
    				
    		parent = parent.substring( 0, end );
    	}

    	return parent;
    }
    
    public static boolean isFileUri( Uri uri ) {
    	return uri.getScheme().equalsIgnoreCase( "file" );
    }

    public static boolean isContentProviderUri( Uri uri ) {
    	return uri.getScheme().equalsIgnoreCase( ContentResolver.SCHEME_CONTENT );
    }

    /**
     * Get the permission of the uri, which is set by
     * {@link ContentResolver#takePersistableUriPermission(Uri, int)} 
     * @param context
     * @param uri
     * @return Combine of {@link Intent#FLAG_GRANT_READ_URI_PERMISSION} and
     * 			{@link Intent#FLAG_GRANT_WRITE_URI_PERMISSION}
     */
	@SuppressLint("NewApi")
	public static int getUriPermission(Context context, Uri uri) {
		if( !DocumentsContract.isTreeUri( uri ) ) {
			return 0;
		}
		
		List<UriPermission> permissionList
			= context.getContentResolver().getPersistedUriPermissions();
		for( UriPermission p : permissionList ) {
			Uri perUri = p.getUri();
			if( !DocumentsContract.isTreeUri( perUri ) ) {
				continue;
			}
			String permissionTreeId
				= DocumentsContract.getTreeDocumentId(perUri);
			String uriTreeId = DocumentsContract.getTreeDocumentId(uri);

			if (uriTreeId.startsWith(permissionTreeId)) {
				int flag
					= p.isReadPermission() 
						? Intent.FLAG_GRANT_READ_URI_PERMISSION : 0;
				flag
					|= p.isWritePermission()
						? Intent.FLAG_GRANT_WRITE_URI_PERMISSION : 0;
				return flag;
			}
		}
		return 0;
	}
	
	public static boolean hasUriReadPermission(Context context, Uri uri) {
		int flag = getUriPermission(context, uri);
		return (flag & Intent.FLAG_GRANT_READ_URI_PERMISSION) != 0;
	}
	
	public static boolean hasUriWritePermission(Context context, Uri uri) {
		int flag = getUriPermission(context, uri);
		return (flag & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0;
	}
	
	public static boolean hasUriReadAndWritePermission(Context context, Uri uri) {
		int flag = getUriPermission(context, uri);
		return ( (flag & Intent.FLAG_GRANT_READ_URI_PERMISSION ) != 0
				  && (flag & Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != 0 );
	}
	
	public static boolean hasUriReadOrWritePermission(Context context, Uri uri) {
		int flag = getUriPermission(context, uri);
		return ( flag 
				 & ( Intent.FLAG_GRANT_READ_URI_PERMISSION 
					| Intent.FLAG_GRANT_WRITE_URI_PERMISSION) ) != 0;
	}
	
	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * Extract the via {@link DocumentsContract.Document#COLUMN_DOCUMENT_ID} from the given URI.
	 * From {@link DocumentsContract} but return null instead of throw
	 */
	public static String getTreeDocumentId(Uri uri) {
		final List<String> paths = uri.getPathSegments();
		if (paths.size() >= 2 && PATH_TREE.equals(paths.get(0))) {
			return paths.get(1);
		}
		return null;
	}

	public static boolean isTreeUri(Uri uri) {
		final List<String> paths = uri.getPathSegments();
		return (paths.size() == 2 && PATH_TREE.equals(paths.get(0)));
	}

	public static boolean isTreeDocuemntUri(Uri uri) {
		final List<String> paths = uri.getPathSegments();
		return (paths.size() == 4 && PATH_TREE.equals(paths.get(0))
                && PATH_DOCUMENT.equals(paths.get(2)));
	}

	public static boolean isRootUri(Uri uri) {
		final List<String> paths = uri.getPathSegments();
		return (paths.size() > 0 && PATH_ROOT.equals(paths.get(0)));
	}

	/**
	 * True if the uri has a tree segment
	 */
	public static boolean hasTreeDocumentId(Uri uri) {
		return getTreeDocumentId(uri) != null;
	}

	/**
	 * Extract the {@link DocumentsContract.Document#COLUMN_DOCUMENT_ID} from the given URI.
	 * From {@link DocumentsContract} but return null instead of throw
	 */
	@Nullable
	public static String getDocumentId(@NonNull Uri documentUri) {
		final List<String> paths = documentUri.getPathSegments();
		if (paths.size() >= 2 && PATH_DOCUMENT.equals(paths.get(0))) {
			return paths.get(1);
		}
		if (paths.size() >= 4 && PATH_TREE.equals(paths.get(0))
				&& PATH_DOCUMENT.equals(paths.get(2))) {
			return paths.get(3);
		}
		return null;
	}

	/**
	 * Given a typical document id this will return the root id.
	 * <p>
	 * Example:
	 * 0000-0000:folder/file.ext
	 * will return '0000-0000'
	 *
	 * @param documentId A valid document Id
	 * @see {@link #getDocumentId(Uri)}
	 * @see {@link DocumentsContract#getDocumentId(Uri)}
	 *
	 * @return the root id of the document id or null
	 */
	@Nullable
	public static String getRoot(@NonNull String documentId)
	{
		String[] parts = documentId.split(":");
		if (parts.length > 0)
			return parts[0];
		return null;
	}

	/**
	 * Given a typical document id this will split at the root id.
	 * <p>
	 * Example:
	 * 0000-0000:folder/file.ext
	 * will return ['0000-0000','folder/file.ext']
	 *
	 * @param documentId A valid document Id
	 * @see {@link #getDocumentId(Uri)}
	 * @see {@link DocumentsContract#getDocumentId(Uri)}
	 *
	 * @return just the document portion of the id without the root
	 */
	@Nullable
	public static String[] getIdSegments(@NonNull String documentId)
	{
		return documentId.split(":");
	}

	/**
	 * Given a typical document id this will split the path segments.
	 * <p>
	 * Example:
	 * 0000-0000:folder/file.ext
	 * will return ['folder','file.ext']
	 *
	 * @param documentUri A valid document uri
	 * @see {@link #getDocumentId(Uri)}
	 * @see {@link DocumentsContract#getDocumentId(Uri)}
	 *
	 * @return tokenized path segments within the document portion of uri
	 */
	@Nullable
	public static String[] getPathSegments(Uri documentUri)
	{
		String documentId = getDocumentId(documentUri);
		if (documentId == null)
			return null;
		return getPathSegments(documentId);
	}

	/**
	 * Given a typical document id this will split the path segements.
	 * <p>
	 * Example:
	 * 0000-0000:folder/file.ext
	 * will return ['folder','file.ext']
	 *
	 * @param documentId A valid document Id
	 * @see {@link #getDocumentId(Uri)}
	 * @see {@link DocumentsContract#getDocumentId(Uri)}
	 *
	 * @return Tokenized path segments within documentId
	 */
	@Nullable
	public static String[] getPathSegments(@NonNull String documentId)
	{
		String[] idParts = getIdSegments(documentId);
		// If there's only one part it's a root
		if (idParts.length <= 1) {
			return null;
		}

		// The last part should be the path for both document and tree uris
		String path = idParts[idParts.length-1];
		return path.split("/");
	}

	/**
	 * Given a valid document root this will create a new id with the appended path.
	 * <p>
	 * Example:
	 * 0000-0000:folder/file.ext
	 * will return ['folder','file.ext']
	 *
	 * @param root A valid document root
	 * @see {@link #getDocumentId(Uri)}
	 * @see {@link #getRoot(String)}}
	 * @see {@link DocumentsContract#getDocumentId(Uri)}
	 *
	 * @return Document ID for the new file
	 */
	public static String createNewDocumentId(@NonNull String root, @NonNull String path)
	{
		return root + ":" + path;
	}

    /**
     * Get the path part of the uri. If the uri points to a directory,
     * the uri itself will be returned. Otherwise, the uri without the documentId
     * will be returned.
     * @param context 
     * @param uri
     * @param withLastSlash 
     * @return the path part of the uri
     */
    public static Uri getPathUri( Context context, Uri uri, boolean withLastSlash ) {
    	if( uri == null ) {
    		return null;
    	}
    	if( DocumentsContract.isDocumentUri(context, uri) ) {
        	DocumentFile df = DocumentFile.fromSingleUri(context, uri);
    		if( df.isDirectory() ) {
    			return uri;
    		}
    	}
    	
    	return getParentUri(uri, withLastSlash);
    }


	/**
	 * Processes the URL encoded path of a document uri to be easy on the eyes
	 * <p>
	 * Example:
	 * <p>
	 * ...tree/0000-0000%3A/document/0000-0000%3Afolder%2Ffile.ext
	 * <p>returns
	 * <p>
	 * '0000-0000:folder/file.ext'
	 * @param uri uri
	 * @return path for display or null if invalid
	 */
	public static String getNicePath(Uri uri) {
		String documentId = getDocumentId(uri);
		if (documentId == null)
			documentId = getTreeDocumentId(uri);    // If there's no document id resort to tree id
		return documentId;
	}

    public static Uri getParentUri( Uri uri, boolean withLastSlash ) {
    	// content://com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2Fa.xml
    	// --> com.android.externalstorage.documents/document/primary%3Abackup%2Ftest%20data%2Fa.xml
    	String path = uri.getEncodedPath();
    	
    	String parent = getParentPath( path, withLastSlash );
    	
    	if( parent == null ) {
    		return null;
    	}
    	
    	Uri.Builder b = uri.buildUpon();
    	b.encodedPath(parent);
    	
    	return b.build();
    }
    
    public static Uri getParentUri( Uri uri ) {
    	return getParentUri( uri, true );
    }
    
	/**
	 * Returns a uri to a child file within a folder.  This can be used to get an assumed uri
	 * to a child within a folder.  This avoids heavy calls to DocumentFile.listFiles or
	 * write-locked createFile
	 *
	 * This will only work with a uri that is an hierarchical tree similar to SCHEME_FILE
	 * @param dirUri folder to install into
	 * @param filename filename of child file
	 * @return Uri to the child file
	 */
	public static Uri getChildUri(Uri dirUri, String filename) {
		String parentDocumentId = getDocumentId(dirUri);
		boolean isStorageVolumn
			= parentDocumentId.endsWith(COLON_CHAR)
				| parentDocumentId.endsWith(COLON_STRING);
		String split = isStorageVolumn ? "" : "/";
		String childDocumentId = parentDocumentId + split + filename;
		return DocumentsContract.buildDocumentUriUsingTree(dirUri, childDocumentId);
	}

	/**
	 * Returns a uri to a neighbor file within the same folder.  This can be used to get an assumed uri
	 * to a neighbor within a folder.  This avoids heavy calls to DocumentFile.listFiles or
	 * write-locked createFile
	 *
	 * This will only work with a uri that is an hierarchical tree similar to SCHEME_FILE
	 * @param hierarchicalTreeUri folder to install into
	 * @param filename filename of child file
	 * @return Uri to the child file
	 */
	@Nullable
	public static Uri getNeighborUri(@NonNull Uri hierarchicalTreeUri, String filename) {
		String documentId = getDocumentId(hierarchicalTreeUri);
		if (documentId == null)
			return null;

		String root = getRoot(documentId);
		if (root == null)
			return null;

		String[] parts = getPathSegments(documentId);
		if (parts == null)
			return null;

		parts[parts.length-1] = filename; // replace the filename
		String path = TextUtils.join("/", parts);
		String neighborId = createNewDocumentId(root, path);
		return DocumentsContract.buildDocumentUriUsingTree(hierarchicalTreeUri, neighborId);
	}

	public static String[] getEncodePathSegments( Uri uri ) {
		String path = uri.getEncodedPath();
		if( path.startsWith( "/" ) ) {
			path = path.substring( 1 );
		}
		
		return path.split( "/" );
	}
	
    public static String getEncodeTreeId(Uri documentUri) {
        final String[] paths = getEncodePathSegments( documentUri );
        if (paths.length >= 2 && PATH_TREE.equals(paths[0])) {
            return paths[1];
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }

    public static String getEncodeDocumentId(Uri documentUri) {
        final String[] paths = getEncodePathSegments( documentUri );
        if (paths.length >= 2 && PATH_DOCUMENT.equals(paths[0])) {
            return paths[1];
        }
        if (paths.length >= 4 && PATH_TREE.equals(paths[0])
                && PATH_DOCUMENT.equals(paths[2])) {
            return paths[3];
        }
        throw new IllegalArgumentException("Invalid URI: " + documentUri);
    }
    
    public static String getName( Uri uri ) {
    	String lastSeg = uri.getLastPathSegment();
    	int index = lastSeg.lastIndexOf( '/' );
    	if( index > 0 && index < lastSeg.length() -1 ) {
    		lastSeg = lastSeg.substring(index+1);
    	}
    	index = lastSeg.lastIndexOf( ':' );
    	if( index > 0 && index < lastSeg.length() -1 ) {
    		lastSeg = lastSeg.substring(index+1);
    	}
    	
    	return lastSeg;
    }
    
    public static boolean documentExists(Context context, Uri documentUri) {
    	final ContentResolver resolver = context.getContentResolver();

    	try( Cursor c
    			= resolver.query(documentUri, PROJECTION_DOCUMENT_ID,
    							 null, null, null) ) {

    		return c.getCount() > 0;
    	} catch ( SecurityException e ) {
    		throw e;
    	} catch (Exception e) {
    	    Log.d( "Failed query ",  documentUri );
    	    return false;
    	}
    }
}
