/*
 *  Copyright (C) 2014 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.omnirom.omniswitch.ui;

import org.omnirom.omniswitch.SwitchConfiguration;
import org.omnirom.omniswitch.TaskDescription;
import org.omnirom.omniswitch.RecentTasksLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;
import android.util.Log;

public class PackageTextView extends TextView implements TaskDescription.ThumbChangeListener {
    private static final String TAG = "PackageTextView";
    private static boolean DEBUG = true;
    private String mIntent;
    private Drawable mOriginalImage;
    private TaskDescription mTask;
    private CharSequence mLabel;
    private Runnable mAction;
    private Handler mHandler = new Handler();
    private static Bitmap setDefaultThumb;

    public PackageTextView(Context context) {
        super(context);
    }

    public PackageTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PackageTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setIntent(String intent) {
        mIntent = intent;
    }

    public String getIntent() {
        return mIntent;
    }

    public void setOriginalImage(Drawable image) {
        mOriginalImage = image;
    }

    public Drawable getOriginalImage() {
        return mOriginalImage;
    }

    public TaskDescription getTask() {
        return mTask;
    }

    public void setTask(TaskDescription task, boolean loadThumb) {
        mTask = task;
        if (loadThumb){
            mTask.setThumbChangeListener(this);
        }
    }

    public CharSequence getLabel() {
        return mLabel;
    }

    public void setLabel(CharSequence label) {
        mLabel = label;
    }

    public void runAction() {
        if (mAction != null) {
            mAction.run();
        }
    }

    public void setAction(Runnable action) {
        mAction = action;
    }

    public boolean isAction() {
        return mAction != null;
    }

    @Override
    public String toString() {
        return getLabel().toString();
    }

    private void updateThumb(final Bitmap thumb, boolean cache) {
        if (getTask() != null){
            // called because the thumb has changed from the default
            if (thumb != null){
                if (DEBUG) {
                    Log.d(TAG, "updateThumb: " + getTask().getLabel()
                            + " " + getTask().getPersistentTaskId());
                }
                SwitchConfiguration configuration = SwitchConfiguration.getInstance(mContext);
                Drawable d = BitmapUtils.overlay(mContext.getResources(), thumb,
                        getTask().getIcon(),
                        configuration.mThumbnailWidth,
                        configuration.mThumbnailHeight,
                        getLabel().toString(),
                        configuration.mDensity,
                        30,
                        configuration.mBgStyle == 0,
                        configuration.mShowLabels);
                if (cache) {
                    BitmapCache.getInstance(mContext).putSharedThumbnail(mContext.getResources(), getTask(), d);
                }
                setThumb(d);
            }
        }
    }

    public void setDefaultThumb() {
        if (getTask() != null){
            if (setDefaultThumb == null) {
                setDefaultThumb = RecentTasksLoader.getInstance(mContext).getDefaultThumb();
            }
            updateThumb(setDefaultThumb, false);
        }
    }

    @Override
    public void thumbChanged(final int persistentTaskId,  final Bitmap thumb) {
        if (getTask() != null){
            if (persistentTaskId == getTask().getPersistentTaskId()) {
                mHandler.post(new Runnable(){
                    @Override
                    public void run() {
                        updateThumb(thumb, true);
                    }});
            }
        }
    }

    @Override
    public int getPersistentTaskId() {
        if (getTask() != null){
            return getTask().getPersistentTaskId();
        }
        return -1;
    }

    public void setThumb(Drawable d) {
        setCompoundDrawablesWithIntrinsicBounds(null, d,  null, null);
    }
    
    public Drawable getCachedThumb() {
        if (getTask() != null){
            return BitmapCache.getInstance(mContext).getSharedThumbnail(getTask());
        }
        return null;
    }   
}
