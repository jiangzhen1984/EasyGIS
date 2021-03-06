package com.easygis.map.layer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;

import com.easygis.map.Bounds;
import com.easygis.map.EMap;
import com.easygis.map.Layer;
import com.easygis.map.MapInfo;
import com.easygis.map.Tile;
import com.easygis.util.CoordinatorTranslation;
import com.easygis.util.EGISLog;

public class TileLayer extends Layer {
	
	private EMap mMap;

	private TileLayerDataLoader mTileLoader;

	private List<PixelTile> mCurrentTiles = new ArrayList<PixelTile>();

	private Bitmap mBufferedBitmap;

	private Bitmap mBackgroundBitmap;

	private Bounds mBounds;

	private Object mLock = new Object();

	private WorkerState mState = WorkerState.NONE;

	private CoordinatorTranslation mTranslation;

	private HandlerThread mMessageQueue = new HandlerThread(
			"TileLayerMessageQueue");

	private Handler mMessageHandler;

	private Paint paint = new Paint();

	public TileLayer(Context context, EMap map) {
		super(context);
		updateMapInfo(map);
	}

	public TileLayer(Context context, EMap map,
			TileLayerDataLoader mTileLoader) {
		super(context);
		updateMapInfo(map);
		this.mTileLoader = mTileLoader;
	}

	public void updateMapInfo(EMap map) {
		this.mMap = map;
		mTranslation = new CoordinatorTranslation(
				(int)mMap.getMapInfo().mTileWidth);
		startRender();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		startRender();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		stopRender();
		if (mBufferedBitmap != null) {
			EGISLog.i(mBufferedBitmap + " is recycled");
			mBufferedBitmap.recycle();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mMessageHandler.postAtFrontOfQueue(mConfigRunnable);
	}

	private Matrix m = new Matrix();
	float scale = 1.0F;
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int savePoint = canvas.save();
		canvas.scale(scale, scale);
		if (mBufferedBitmap != null) {
			canvas.drawBitmap(mBufferedBitmap, 0, 0, paint);
		}
		canvas.restoreToCount(savePoint);
	}

	@Override
	public void updateBounds(Bounds bounds) {
		this.mBounds = bounds;
		if (mMessageHandler != null) {
			mMessageHandler.post(mUpdateBoundsRunnable);
		}
	}

	@Override
	public void updateBounds(Bounds bounds, int zoom) {
		updateBounds(bounds);
	}
	
	

	@Override
	protected void scale(float sc) {
		scale *= sc;
	}

	private void drawTile(List<PixelTile> tileList, Bitmap target) {
		Canvas c = new Canvas(target);
		Paint p = new Paint();
		for (PixelTile ptile : tileList) {
			c.drawBitmap((Bitmap) ptile.tile.mTileData, ptile.offsetX,
					ptile.offsetY, p);
			postInvalidate();
		}
	}

	private void startRender() {
		if (!mWorker.isAlive()) {
			mWorker.start();
		}

		if (!mMessageQueue.isAlive()) {
			mMessageQueue.start();
		}
		if (mMessageHandler == null) {
			mMessageHandler = new Handler(mMessageQueue.getLooper());
		}
		synchronized (mLock) {
			mState = WorkerState.NONE;
			mLock.notify();
		}
	}

	private void stopRender() {
		mMessageQueue.quit();
		mMessageHandler = null;
		synchronized (mLock) {
			mState = WorkerState.DIED;
			mLock.notify();
		}
	}

	private TileDataLoaderCallback mTileDataLoadedCallback = new TileDataLoaderCallback() {

		@Override
		public void tileLoadedNotification(int row, int col, int zoom, Tile tile) {

		}

	};

	private Thread mWorker = new Thread() {

		@Override
		public void run() {
			while (true) {
				synchronized (mLock) {
					if (mState == WorkerState.DIED) {
						break;
					}

					if (mState == WorkerState.NONE
							|| mState == WorkerState.DONE) {
						try {
							mLock.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					if (mState != WorkerState.RENDER) {
						continue;
					}
					// Until bitmap is not null
					while (mBufferedBitmap == null) {
						try {
							mLock.wait(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					drawTile(mCurrentTiles, mBufferedBitmap);
					mState = WorkerState.DONE;

				}
			}
		}

	};

	private Runnable mUpdateBoundsRunnable = new Runnable() {

		@Override
		public void run() {
			int zoom =  mMap.getZoom();
			int tileWidth = (int)mMap.getMapInfo().mTileWidth;
			int tileHeight = (int) mMap.getMapInfo().mTileHeight;
			long start = System.currentTimeMillis();
			int[] topLeft = mTranslation.translateMetersToTile(mBounds.left,
					mBounds.top, zoom);
			int[] bottomRight = mTranslation.translateMetersToTile(
					mBounds.right, mBounds.bottom, zoom);
			int startRow = topLeft[1];
			int endRow = bottomRight[1];
			int startCol = topLeft[0];
			int endCol = bottomRight[0];

			double[] pixels = mTranslation.translateMetersToPixels(
					mBounds.left, mBounds.top, zoom);
			int offsetX = (int) -(pixels[0] - startCol * tileWidth);
			int offsetY = (int) -(pixels[1] - startRow * tileHeight);

			EGISLog.i(startRow + "," + startCol + " - " + endRow + "," + endCol
					+ "   offsetX:" + offsetX + "  offsetY:" + offsetY
					+ "  px:" + pixels[0] + "  py:" + pixels[1]);
			if (offsetX > 0) {
				startCol -= 1;
				offsetX -= tileWidth;
			} else if (offsetX < 0 && Math.abs(offsetX) > (int)tileWidth) {
				startCol += 1;
				offsetX += tileWidth;
			}

			if (offsetY > 0) {
				startRow -= 1;
				offsetY -= tileHeight;
			} else if (offsetY < 0 && Math.abs(offsetY) > (int)tileHeight) {
				startRow += 1;
				offsetY += tileHeight;
			}

			
			long start0 = System.currentTimeMillis();
			EGISLog.i("After adjust:" + startRow + "," + startCol + " - "
					+ endRow + "," + endCol + "   offsetX:" + offsetX
					+ "  offsetY:" + offsetY);
			List<PixelTile> list = new ArrayList<PixelTile>();
			int maxRow = mMap.getMapInfo().mSupportedLevels[zoom].mEndRow;
			int maxCol = mMap.getMapInfo().mSupportedLevels[zoom].mEndCol;
			for (int i = startRow, indexI = 0; i <= endRow; i++, indexI++) {
				for (int j = startCol, indexJ = 0; j <= endCol; j++, indexJ++) {
					int tileOffsetX = offsetX + indexJ
							* (int) tileWidth;
					int tileOffsetY = offsetY + indexI
							* (int) tileHeight;
					if (j <= maxCol && i <= maxRow) {
						Tile tile = mTileLoader.getTile(i, j, zoom);
						if (tile != null && tile.mTileData != null) {
							list.add(new PixelTile(tileOffsetX,
									tileOffsetY, tile));
						} else {
							EGISLog.e("row : " + i + "  col:" + j
									+ "  bitmap is null");
						}
					}
				}
			}
			
			
			
			long start1 = System.currentTimeMillis();
			synchronized (mLock) {
				mCurrentTiles.clear();
				mCurrentTiles.addAll(list);
				mState = WorkerState.RENDER;
				mLock.notify();
			}
			long end = System.currentTimeMillis();
			EGISLog.e("updateBounds cost:"+(end - start)+"   "+(start1 -start)+"   "+(start0 -start));
		}

	};

	private Runnable mConfigRunnable = new Runnable() {

		@Override
		public void run() {
			synchronized (mLock) {
				mMessageHandler.removeCallbacks(mConfigRunnable);
				mMessageHandler.removeCallbacks(mUpdateBoundsRunnable);

				int width = getWidth();
				int height = getHeight();
				if (mBufferedBitmap == null) {
					mBufferedBitmap = Bitmap.createBitmap(width, height,
							Bitmap.Config.ARGB_4444);
				} else {
					if (mBufferedBitmap.getWidth() != width
							|| mBufferedBitmap.getHeight() != height) {
						if (android.os.Build.VERSION_CODES.KITKAT >= android.os.Build.VERSION.SDK_INT) {
							mBufferedBitmap.reconfigure(width, height,
									Bitmap.Config.ARGB_4444);
						} else {
							mBufferedBitmap.recycle();
							mBufferedBitmap = Bitmap.createBitmap(width,
									height, Bitmap.Config.ARGB_4444);
						}
					}
				}

				EGISLog.i("reconfig bitmap ["+width+","+height+"]......" + mBufferedBitmap);
				mState = WorkerState.RENDER;
				mLock.notify();
			}

		}

	};

	class PixelTile {
		int offsetX;
		int offsetY;
		Tile tile;

		public PixelTile(int offsetX, int offsetY, Tile tile) {
			super();
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.tile = tile;
		}

	}

	enum WorkerState {
		NONE, RENDER, DONE, DIED;
	}

}
