package com.easygis.map.layer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.HandlerThread;

import com.easygis.map.Bounds;
import com.easygis.map.Layer;
import com.easygis.map.MapInfo;
import com.easygis.map.Tile;
import com.easygis.util.CoordinatorTranslation;
import com.easygis.util.EGISLog;

public class TileLayer extends Layer {

	private MapInfo mMapInfo;

	private TileLayerDataLoader mTileLoader;

	private List<PixelTile> mCurrentTiles = new ArrayList<PixelTile>();

	private Bitmap mBufferedBitmap;
	
	private Bitmap mBackgroundBitmap;

	private int mZoom;

	private Bounds mBounds;

	private Object mLock = new Object();

	private WorkerState mState = WorkerState.NONE;

	private CoordinatorTranslation mTranslation;

	private HandlerThread mMessageQueue = new HandlerThread(
			"TileLayerMessageQueue");

	private Handler mMessageHandler;

	private Paint paint = new Paint();

	public TileLayer(Context context, MapInfo mMapInfo) {
		super(context);
		updateMapInfo(mMapInfo);
	}

	public TileLayer(Context context, MapInfo mMapInfo,
			TileLayerDataLoader mTileLoader) {
		super(context);
		updateMapInfo(mMapInfo);
		this.mTileLoader = mTileLoader;
	}

	public void updateMapInfo(MapInfo mapInfo) {
		this.mMapInfo = mapInfo;
		this.mZoom = this.mMapInfo.mInitialZoom;
		mTranslation = new CoordinatorTranslation(
				(int) this.mMapInfo.mTileWidth);
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
			EGISLog.i(mBufferedBitmap+" is recycled");
			mBufferedBitmap.recycle();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) { 
		super.onLayout(changed, left, top, right, bottom);
		mMessageHandler.postAtFrontOfQueue(mConfigRunnable);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int savePoint = canvas.save();

		canvas.drawBitmap(mBufferedBitmap, 0, 0, paint);
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
		mZoom = zoom;
		updateBounds(bounds);
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
					//Until bitmap is not null
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
			synchronized (mLock) {
				int[] topLeft = mTranslation.translateMetersToTile(
						mBounds.left, mBounds.top, mZoom);
				int[] bottomRight = mTranslation.translateMetersToTile(
						mBounds.right, mBounds.bottom, mZoom);
				int startRow = topLeft[1];
				int endRow = bottomRight[1];
				int startCol = topLeft[0];
				int endCol = bottomRight[0];


				double[] pixels = mTranslation.translateMetersToPixels(
						mBounds.left, mBounds.top, mZoom);
				int offsetX = (int) -(pixels[0] - startCol
						* mMapInfo.mTileWidth);
				int offsetY = (int) -(pixels[1] - startRow
						* mMapInfo.mTileHeight);

				EGISLog.i(startRow + "," + startCol + " - " + endRow + ","
						+ endCol + "   offsetX:" + offsetX + "  offsetY:"
						+ offsetY+"  px:" +pixels[0]+"  py:"+pixels[1]);
				if (offsetX > 0) {
					startCol -= 1;
					offsetX -= mMapInfo.mTileWidth;
				} else if (offsetX < 0
						&& Math.abs(offsetX) > mMapInfo.mTileWidth) {
					startCol += 1;
					offsetX += mMapInfo.mTileWidth;
				}

				if (offsetY > 0) {
					startRow -= 1;
					offsetY -= mMapInfo.mTileHeight;
				} else if (offsetY < 0
						&& Math.abs(offsetY) > mMapInfo.mTileHeight) {
					startRow += 1;
					offsetY += mMapInfo.mTileHeight;
				}

				EGISLog.i("After adjust:" + startRow + "," + startCol + " - "
						+ endRow + "," + endCol + "   offsetX:" + offsetX
						+ "  offsetY:" + offsetY);

				mCurrentTiles.clear();
				for (int i = startRow, indexI = 0; i <= endRow; i++, indexI++) {
					for (int j = startCol, indexJ = 0; j <= endCol; j++, indexJ++) {
						int tileOffsetX = offsetX + indexJ
								* (int) mMapInfo.mTileWidth;
						int tileOffsetY = offsetY + indexI
								* (int) mMapInfo.mTileHeight;
						Tile tile = mTileLoader.getTile(i, j, mZoom);
						if (tile != null && tile.mTileData != null) {
							mCurrentTiles.add(new PixelTile(tileOffsetX,
									tileOffsetY, tile));
						} else {
							EGISLog.e("row : " + i + "  col:" + j + "  bitmap is null");
						}
//						EGISLog.d("row : " + i + "  col:" + j + "  offsetX:"
//								+ tileOffsetX + "  tileOffsetY:" + tileOffsetY
//								+ "  mZoom:" + mZoom);
					}
				}

				mState = WorkerState.RENDER;
				mLock.notify();
			}

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

				EGISLog.i("mBufferedBitmap......" + mBufferedBitmap);
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
