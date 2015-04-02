package com.easygis.map.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

import com.easygis.map.MapInfo;
import com.easygis.map.Tile;
import com.easygis.map.layer.TileDataLoaderCallback;
import com.easygis.map.layer.TileLayerDataLoader;
import com.easygis.util.CoordinatorTranslation;
import com.easygis.util.EGISLog;

public class LocalTileDataLoader implements TileLayerDataLoader {
	
	private MapInfo mMapInfo;
	
	private CoordinatorTranslation mTranslation;
	
	
	public LocalTileDataLoader(MapInfo mMapInfo) {
		super();
		this.mMapInfo = mMapInfo;
		this.mTranslation = new CoordinatorTranslation((int)mMapInfo.mTileWidth);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void load() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unLoad() {
		mCache.evictAll();
	}

	@Override
	public Tile getTile(int row, int col, int zoom) {
		Marker key = new Marker(row, col, zoom, System.currentTimeMillis());
		Tile data = mCache.get(key);
		if (data == null) {
			data = new Tile();
			data.mCol = col;
			data.mRow = row;
			data.mZoom = zoom;
			data.mResolution = mTranslation.resolution(zoom);
			data.mScale = mTranslation.scale(zoom);
			data.mBounds = mTranslation.translateTileBounds(row, col, zoom);
			data.isDirty = false;
			
			String path = mMapInfo.mTilePath+"/"+zoom+"/"+row+"_"+col+"."+mMapInfo.mTileExtension;
			data.mTileData = loadBitmap(path);
			if (data.mTileData != null) {
				mCache.put(key, data);
			} else {
				return null;
			}
		} else {
			key.timestamp = System.currentTimeMillis();
		}
		return data;
	}

	@Override
	public Tile getTileAsync(int row, int col, int zoom,
			TileDataLoaderCallback callback) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private Bitmap loadBitmap(String path) {
		return  BitmapFactory.decodeFile(path);
	}
	
	
	
	private LruCache<Marker, Tile> mCache = new LruCache<Marker, Tile>(10 * 1024 * 1024) {

		@Override
		protected Tile create(Marker key) {
			return super.create(key);
		}

		@Override
		protected void entryRemoved(boolean evicted, Marker key,
				Tile oldValue, Tile newValue) {
			if (oldValue != null && oldValue != newValue) {
				((Bitmap)oldValue.mTileData).recycle();
				oldValue.isDirty = true;
			}
			super.entryRemoved(evicted, key, oldValue, newValue);
			EGISLog.i("[" + this.size() + "/" + this.maxSize() + "]   " + key
					+ "  evicted:" + evicted + " oldValue: " + oldValue
					+ " newValue:" + newValue);
		}

		@Override
		protected int sizeOf(Marker key, Tile value) {
			if (android.os.Build.VERSION_CODES.KITKAT >= android.os.Build.VERSION.SDK_INT) {
				return ((Bitmap)value.mTileData).getAllocationByteCount();
			} else {
				return ((Bitmap)value.mTileData).getByteCount();
			}
		}

		@Override
		public void trimToSize(int maxSize) {
			super.trimToSize(maxSize);
		}
		
	};

	
	
	
	class Marker implements Comparable<Marker> {
		
		int row;
		int col;
		int zoom;
		long timestamp;

		

		public Marker(int row, int col, int zoom, long timestamp) {
			super();
			this.row = row;
			this.col = col;
			this.zoom = zoom;
			this.timestamp = timestamp;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + col;
			result = prime * result + row;
			result = prime * result + zoom;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Marker other = (Marker) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (col != other.col)
				return false;
			if (row != other.row)
				return false;
			if (zoom != other.zoom)
				return false;
			return true;
		}

		private LocalTileDataLoader getOuterType() {
			return LocalTileDataLoader.this;
		}

		@Override
		public int compareTo(Marker another) {
			return this.timestamp > another.timestamp ? 1 : -1;
		}
		
		@Override
		public String toString() {
			return "["+row+"_"+col+"_"+zoom+"]";
		}
	}
	
	
	
	

}
