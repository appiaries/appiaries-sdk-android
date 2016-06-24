//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 座標モデル。
 * <p>座標（位置情報）を表現するモデルです。位置情報検索機能に使用します。</p>
 * @version 2.0.0
 * @since 2.0.0
 * @see <a href="http://docs.appiaries.com/?p=20">アピアリーズドキュメント &raquo; 機能概要 &raquo; ジオロケーション</a>
 */
public class ABGeoPoint {

    @JsonProperty("_lat")
    private double mLatitude;

    @JsonProperty("_lng")
    private double mLongitude;

    public ABGeoPoint(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    /**
     * 緯度を取得します。
     * @return 緯度
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * 緯度をセットします。
     * @param latitude 緯度
     */
    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    /**
     * 経度を取得します。
     * @return 経度
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * 経度をセットします。
     * @param longitude 経度
     */
    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

}
