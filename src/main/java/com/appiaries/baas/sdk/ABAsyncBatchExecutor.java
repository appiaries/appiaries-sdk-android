package com.appiaries.baas.sdk;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * 非同期APIをシーケンシャルに一括実行するためのクラス。
 * saveAll, deleteAll などでの使用を想定。(ただし、トランザクション制御不可なのであくまで一時的な利用に止めること）
 * 処理の途中(例えば10件中の3件目)でエラーが発生した場合は、
 * そこまでの正常処理件数(取得は ABResult#getTotal())と、エラー原因となった ABException を返す。
 *
 * また、progressCallback に渡されるパーセンテージは、"処理済み件数 / 処理対象総数"。(e.g. 10件中2件処理完了時には 0.2 が渡される)
 */
abstract class ABAsyncBatchExecutor <E extends ABModel> {

    List<E> mTargets;
    Integer mCount;
    Integer mIndex;
    ResultCallback mCallback;
    ProgressCallback mProgressCallback;
    ExecutorService mExecutor;
    boolean mForce;

    public ABAsyncBatchExecutor(List<E> targets, ResultCallback callback, ProgressCallback progressCallback) {
        this(targets, callback, progressCallback, false);
    }
    public ABAsyncBatchExecutor(List<E> targets, ResultCallback callback, ProgressCallback progressCallback, boolean force) {
        mTargets = targets;
        mCount = mTargets.size();
        mCallback = callback;
        mProgressCallback = progressCallback;
        mIndex = 0;
        mExecutor = Executors.newSingleThreadExecutor();
        mForce = force;
    }

    abstract void onProcess(E target);

    public void execute() {
        final E t = mTargets.get(mIndex);
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                onProcess(t);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T extends ABModel> void postProcess(final ABResult<T> result, final ABException e) {
        if (!mForce && e != null) {
            //abort
            final ABResult ret = new ABResult();
            ret.setCode(e.getCode());
            ret.setTotal(mIndex + 1); //NOTE: 処理済みの件数は渡す
            if (mCallback != null) {
                new Handler(AB.sApplicationContext.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        mCallback.internalDone(ret, e);
                    }
                });
            }
        } else {
            if (mProgressCallback != null) {
                final float progress = (float)(mIndex + 1) / (float)mCount;
                new Handler(AB.sApplicationContext.getMainLooper()).post(new Runnable(){
                    @Override
                    public void run() {
                        mProgressCallback.internalUpdateProgress(progress);
                    }
                });
            }
            if (mCount -1 > mIndex) {
                mIndex++;
                execute();
            } else {
                //(正常)終了処理
                result.setTotal(mCount);
                if (mCallback != null) {
                    new Handler(AB.sApplicationContext.getMainLooper()).post(new Runnable(){
                        @Override
                        public void run() {
                            mCallback.internalDone(result, e);
                        }
                    });
                }
            }
        }
    }

}
