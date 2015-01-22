package com.example.choosephotos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore.Audio.Albums;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;

/**
 * 专辑帮助类
 * 
 * @author Administrator
 * 
 */
public class AlbumHelper {

	public interface BucketFilter {
		public boolean accept(ImageBucket bucket);
	}

	final String TAG = getClass().getSimpleName();
	Context context;
	ContentResolver cr;

	// 缩略图列表
	HashMap<String, String> thumbnailList = new HashMap<String, String>();
	// 专辑列表
	List<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();
	HashMap<String, ImageBucket> bucketList = new HashMap<String, ImageBucket>();

	List<ImageItem> imageList = new ArrayList<ImageItem>();

	private static AlbumHelper instance;

	private AlbumHelper() {
	}

	public static AlbumHelper getHelper() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		if (this.context == null) {
			this.context = context;
			cr = context.getContentResolver();
		}
	}

	/**
	 * 得到缩略图
	 */
	private void getThumbnail() {
		String[] projection = { Thumbnails._ID, Thumbnails.IMAGE_ID,
		        Thumbnails.DATA };
		Cursor cursor = cr.query(Thumbnails.EXTERNAL_CONTENT_URI, projection,
		        null, null, null);
		getThumbnailColumnData(cursor);
	}

	/**
	 * 从数据库中得到缩略图
	 * 
	 * @param cur
	 */
	private void getThumbnailColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			// int _id;
			int image_id;
			String image_path;
			// int _idColumn = cur.getColumnIndex(Thumbnails._ID);
			int image_idColumn = cur.getColumnIndex(Thumbnails.IMAGE_ID);
			int dataColumn = cur.getColumnIndex(Thumbnails.DATA);

			do {
				// _id = cur.getInt(_idColumn);
				image_id = cur.getInt(image_idColumn);
				image_path = cur.getString(dataColumn);
				thumbnailList.put("" + image_id, image_path);
			} while (cur.moveToNext());
		}
	}

	/**
	 * 得到原图
	 */
	void getAlbum() {
		String[] projection = { Albums._ID, Albums.ALBUM, Albums.ALBUM_ART,
		        Albums.ALBUM_KEY, Albums.ARTIST, Albums.NUMBER_OF_SONGS };
		Cursor cursor = cr.query(Albums.EXTERNAL_CONTENT_URI, projection, null,
		        null, null);
		getAlbumColumnData(cursor);

	}

	/**
	 * 从本地数据库中得到原图
	 * 
	 * @param cur
	 */
	private void getAlbumColumnData(Cursor cur) {
		if (cur.moveToFirst()) {
			int _id;
			String album;
			String albumArt;
			String albumKey;
			String artist;
			int numOfSongs;

			int _idColumn = cur.getColumnIndex(Albums._ID);
			int albumColumn = cur.getColumnIndex(Albums.ALBUM);
			int albumArtColumn = cur.getColumnIndex(Albums.ALBUM_ART);
			int albumKeyColumn = cur.getColumnIndex(Albums.ALBUM_KEY);
			int artistColumn = cur.getColumnIndex(Albums.ARTIST);
			int numOfSongsColumn = cur.getColumnIndex(Albums.NUMBER_OF_SONGS);

			do {
				// Get the field values
				_id = cur.getInt(_idColumn);
				album = cur.getString(albumColumn);
				albumArt = cur.getString(albumArtColumn);
				albumKey = cur.getString(albumKeyColumn);
				artist = cur.getString(artistColumn);
				numOfSongs = cur.getInt(numOfSongsColumn);

				// Do something with the values.
				Log.i(TAG, _id + " album:" + album + " albumArt:" + albumArt
				        + "albumKey: " + albumKey + " artist: " + artist
				        + " numOfSongs: " + numOfSongs + "---");
				HashMap<String, String> hash = new HashMap<String, String>();
				hash.put("_id", _id + "");
				hash.put("album", album);
				hash.put("albumArt", albumArt);
				hash.put("albumKey", albumKey);
				hash.put("artist", artist);
				hash.put("numOfSongs", numOfSongs + "");
				albumList.add(hash);

			} while (cur.moveToNext());

		}
	}

	boolean hasBuildImageList = false;
	boolean hasBuildImagesBucketList = false;
	private static final int MIN_SIZE = 100 * 1024;

	/**
	 * 得到图片集
	 */
	void buildImagesBucketList() {
		long startTime = System.currentTimeMillis();

		// 构造缩略图索引
		getThumbnail();

		String columns[] = new String[] {
		        Media._ID, Media.DATA,
		        Media.SIZE, Media.DATE_ADDED,
		        Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME };
		String selector = Media.SIZE + ">" + MIN_SIZE + " AND "
		        + Media.BUCKET_DISPLAY_NAME + " NOT LIKE 'drawable%'";
		String order = Media.DATE_ADDED + " DESC LIMIT 100";

		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns,
		        selector, null, order);

		if (cur.moveToFirst()) {
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
			int dateIndex = cur.getColumnIndexOrThrow(Media.DATE_ADDED);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
			int bucketNameIndex = cur
			        .getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);

			Log.d(TAG, ">_id>path>size>date>bucketId>bucketName>");
			do {
				String _id = cur.getString(photoIDIndex);
				String path = cur.getString(photoPathIndex);
				int size = cur.getInt(photoSizeIndex);
				long date = cur.getLong(dateIndex);
				String bucketId = cur.getString(bucketIdIndex);
				String bucketName = cur.getString(bucketNameIndex);

				Log.d(TAG, ">" + _id + ">" + path + ">" + size + ">"
				        + date + ">" + bucketId + ">" + bucketName);

				ImageBucket bucket = bucketList.get(bucketId);
				if (bucket == null) {
					bucket = new ImageBucket();
					bucketList.put(bucketId, bucket);
					bucket.imageList = new ArrayList<ImageItem>();
					bucket.bucketName = bucketName;
				}
				bucket.count++;
				ImageItem imageItem = new ImageItem();
				imageItem.imageId = _id;
				imageItem.imagePath = path;
				imageItem.thumbnailPath = thumbnailList.get(_id);
				imageItem.imageSize = size;
				bucket.imageList.add(imageItem);

			} while (cur.moveToNext());
		}

		hasBuildImagesBucketList = true;
		long endTime = System.currentTimeMillis();
		Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
	}

	/**
	 * 得到图片集
	 */
	void buildImageItemList() {
		long startTime = System.currentTimeMillis();

		// 构造缩略图索引
		getThumbnail();

		String columns[] = new String[] {
		        Media._ID, Media.DATA,
		        Media.SIZE, Media.DATE_ADDED,
		        Media.BUCKET_ID, Media.BUCKET_DISPLAY_NAME };
		String selector = Media.SIZE + ">" + MIN_SIZE + " AND "
		        + Media.BUCKET_DISPLAY_NAME + " NOT LIKE 'drawable%'";
		String order = Media.DATE_ADDED + " DESC LIMIT 100";

		Cursor cur = cr.query(Media.EXTERNAL_CONTENT_URI, columns,
		        selector, null, order);

		if (cur.moveToFirst()) {
			int photoIDIndex = cur.getColumnIndexOrThrow(Media._ID);
			int photoPathIndex = cur.getColumnIndexOrThrow(Media.DATA);
			int photoSizeIndex = cur.getColumnIndexOrThrow(Media.SIZE);
			int dateIndex = cur.getColumnIndexOrThrow(Media.DATE_ADDED);
			int bucketIdIndex = cur.getColumnIndexOrThrow(Media.BUCKET_ID);
			int bucketNameIndex = cur
			        .getColumnIndexOrThrow(Media.BUCKET_DISPLAY_NAME);

			Log.d(TAG, ">_id>path>size>date>bucketId>bucketName>");
			do {
				String _id = cur.getString(photoIDIndex);
				String path = cur.getString(photoPathIndex);
				int size = cur.getInt(photoSizeIndex);
				long date = cur.getLong(dateIndex);
				String bucketId = cur.getString(bucketIdIndex);
				String bucketName = cur.getString(bucketNameIndex);

				Log.d(TAG, ">" + _id + ">" + path + ">" + size + ">"
				        + date + ">" + bucketId + ">" + bucketName);

				ImageItem imageItem = new ImageItem();
				imageItem.imageId = _id;
				imageItem.imagePath = path;
				imageItem.thumbnailPath = thumbnailList.get(_id);
				imageItem.imageSize = size;
				imageItem.time = date;
				imageList.add(imageItem);

			} while (cur.moveToNext());
		}

		hasBuildImageList = true;
		long endTime = System.currentTimeMillis();
		Log.d(TAG, "use time: " + (endTime - startTime) + " ms");
	}

	/**
	 * 得到图片集
	 * 
	 * @param refresh
	 * @return
	 */
	public List<ImageItem> getImageList(boolean refresh) {
		if (refresh || !hasBuildImageList) {
			buildImageItemList();
		}
		return imageList;
	}

	/**
	 * 得到图片集
	 * 
	 * @param refresh
	 * @return
	 */
	@Deprecated
	private List<ImageBucket> getImagesBucketList(boolean refresh,
	        BucketFilter filter) {
		if (refresh || (!refresh && !hasBuildImagesBucketList)) {
			buildImagesBucketList();
		}
		List<ImageBucket> tmpList = new ArrayList<ImageBucket>();
		Iterator<Entry<String, ImageBucket>> itr = bucketList.entrySet()
		        .iterator();
		while (itr.hasNext()) {
			Entry<String, ImageBucket> entry =
			        (Entry<String, ImageBucket>) itr.next();
			ImageBucket bucket = entry.getValue();
			if (null != filter && !filter.accept(bucket)) {
				continue;
			}
			tmpList.add(bucket);
		}
		return tmpList;
	}

	/**
	 * 得到原始图像路径
	 * 
	 * @param image_id
	 * @return
	 */
	String getOriginalImagePath(String image_id) {
		String path = null;
		Log.i(TAG, "---(^o^)----" + image_id);
		String[] projection = { Media._ID, Media.DATA };
		Cursor cursor = cr.query(Media.EXTERNAL_CONTENT_URI, projection,
		        Media._ID + "=" + image_id, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			path = cursor.getString(cursor.getColumnIndex(Media.DATA));

		}
		return path;
	}

}
