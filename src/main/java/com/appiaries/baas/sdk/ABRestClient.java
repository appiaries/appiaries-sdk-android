//
// Created by Appiaries Corporation on 15/04/03.
// Copyright (c) 2015 Appiaries Corporation. All rights reserved.
//

package com.appiaries.baas.sdk;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.RequestLine;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;

import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST API クライアント。
 * @version 2.0.0
 * @since 2.0.0
 */
@SuppressWarnings("unused")
final class ABRestClient {

    static final int OUTPUT_BUFFER_SIZE = 1024 * 4; //progress処理時のバッファサイズ (このバッファが一杯になったタイミングでpublishProgressがコールされる)

    private static final Map<String, AsyncTask> mRequestQueue = new ConcurrentHashMap<>();

    public static class AsyncGet extends AsyncTask<Void, Integer, ABResult<Map<String, Object>>> {
        private String mUrl;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;
        private ABException mException = null;

        public AsyncGet(String url, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet request = new HttpGet(mUrl);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);
                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        long contentLength = entity.getContentLength();
                        if (contentLength <= 0) {
                            ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                        } else {
                            InputStream is = entity.getContent();
                            File outputDir = (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) || !isExternalStorageRemovable())
                                            ? getExternalCacheDir(AB.sApplicationContext) : AB.sApplicationContext.getCacheDir();
                            String extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentType);
                            if (extension == null) {
                                extension = "bin";
                            }
                            File outputFile = File.createTempFile(UUID.randomUUID().toString(), "." + extension, outputDir); //XXX: キャッシュさせようとした場合にこのファイル名は扱いにくい
                            OutputStream os = new FileOutputStream(outputFile);
                            byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
                            int size;
                            long wroteBytes = 0;

                            try {
                                while (-1 != (size = is.read(buffer))) {
                                    os.write(buffer, 0, size);
                                    wroteBytes += OUTPUT_BUFFER_SIZE;
                                    //ABLog.d(TAG, "current wrote bytes: " + wroteBytes);
                                    if (isCancelled()) {
                                        for (String key : mRequestQueue.keySet()) {
                                            AsyncTask t = mRequestQueue.get(key);
                                            if (t == AsyncGet.this) {
                                                Log.d(TAG, "Request CANCELLED: [url=" + key + "]" + " [task=" + t + "]");
                                                mRequestQueue.remove(key);
                                                throw new InterruptedException("Cancelled");
                                            }
                                        }
                                    }
                                    publishProgress((int) (((float) wroteBytes / (float) contentLength) * 100));
                                }
                                os.flush();
                                ret.putExtra(ABResult.EXTRA_KEY_DOWNLOAD_FILE_PATH, outputFile.getAbsolutePath());
                            } catch (IOException e) {
                                ABLog.e(TAG, e.getMessage());
                                throw e;
                            } finally {
                                try {
                                    os.close();
                                } catch (IOException e) {
                                    ABLog.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData(), statusCode);

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                mException = e;
            } catch (Exception e) {
                mException = new ABException(e);
            }
            return ret;
        }

        //ref) http://y-anz-m.blogspot.jp/2012/08/androidbitmap-github.html
        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        private static boolean isExternalStorageRemovable() {
            if (hasGingerbread()) {
                return Environment.isExternalStorageRemovable();
            }
            return true;
        }
        //ref) http://y-anz-m.blogspot.jp/2012/08/androidbitmap-github.html
        @TargetApi(Build.VERSION_CODES.FROYO)
        private static File getExternalCacheDir(Context context) {
            if (hasFroyo()) {
                File cacheDir = context.getExternalCacheDir();
                if (cacheDir != null) {
                    return cacheDir;
                }
            }

            // Froyo 以前は自前でディレクトリを作成する
            final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
            return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
        }
        //ref) http://y-anz-m.blogspot.jp/2012/08/androidbitmap-github.html
        public static boolean hasFroyo() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
        }
        //ref) http://y-anz-m.blogspot.jp/2012/08/androidbitmap-github.html
        public static boolean hasGingerbread() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) mCallback.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null && !isCancelled()) mCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(ABResult<Map<String, Object>> result) {
            super.onPostExecute(result);
            if (mCallback != null) mCallback.onPostExecute(result, mException);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) mCallback.onCancelled();
        }
/*
        //@see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
        public void execute() {
            super.execute();
        }
*/
    }

    public static class AsyncPut extends AsyncTask<Void, Integer, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;
        private ABException mException = null;

        public AsyncPut(String url, String body, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
            mCallback = asyncCallback;
        }
        public AsyncPut(String url, HttpEntity entity, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPut request = new HttpPut(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        long contentLength = entity.getContentLength();
                        if (contentLength <= 0) {
                            ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                        } else {
                            InputStream inputStream = entity.getContent();
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            OutputStream os = new BufferedOutputStream(b);
                            int c;
                            long wroteBytes = 0;
                            try {
                                while ((c = inputStream.read()) != -1) {
                                    if (isCancelled()) {
                                        throw new InterruptedException();
                                    }
                                    os.write(c);
                                    wroteBytes++;
                                    if ((wroteBytes % OUTPUT_BUFFER_SIZE) == 0) {
                                        publishProgress((int) (((float) wroteBytes / (float) contentLength) * 100));
                                    }
                                }
                                ABLog.d(TAG, "wroteBytes: " + wroteBytes);
                                byte[] bin = b.toByteArray();
                                ret.setRawData(bin);
                            } catch (IOException e) {
                                ABLog.e(TAG, e.getMessage());
                                throw e;
                            } catch (InterruptedException e) {
                                statusCode = ABStatus.OPERATION_CANCELLED;
                                ret.setCode(statusCode);
                            } finally {
                                try {
                                    os.flush();
                                    os.close();
                                } catch (IOException e) {
                                    ABLog.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData(), statusCode);

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                mException = e;
            } catch (Exception e) {
                mException = new ABException(e);
            }
            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) mCallback.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null) mCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(ABResult<Map<String, Object>> result) {
            super.onPostExecute(result);
            if (mCallback != null && !isCancelled()) mCallback.onPostExecute(result, mException);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) mCallback.onCancelled();
        }

        //@see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
        public void execute() {
            super.execute();
        }
    }

    public static class AsyncPost extends AsyncTask<Void, Integer, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;
        private ABException mException = null;

        public AsyncPost(String url, String body, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        public AsyncPost(String url, HttpEntity entity, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost request = new HttpPost(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //TODO: formDataの場合は request.setEntity(new UrlEncodedFormEntity(params));
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        long contentLength = entity.getContentLength();
                        if (contentLength <= 0) {
                            ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                        } else {
                            InputStream inputStream = entity.getContent();
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            OutputStream os = new BufferedOutputStream(b);
                            int c;
                            long wroteBytes = 0;
                            try {
                                while ((c = inputStream.read()) != -1) {
                                    if (isCancelled()) {
                                        throw new InterruptedException();
                                    }
                                    os.write(c);
                                    wroteBytes++;
                                    if ((wroteBytes % OUTPUT_BUFFER_SIZE) == 0) {
                                        publishProgress((int) (((float) wroteBytes / (float) contentLength) * 100));
                                    }
                                }
                                ABLog.d(TAG, "wroteBytes: " + wroteBytes);
                                byte[] bin = b.toByteArray();
                                ret.setRawData(bin);
                            } catch (IOException e) {
                                ABLog.e(TAG, e.getMessage());
                                throw e;
                            } catch (InterruptedException e) {
                                statusCode = ABStatus.OPERATION_CANCELLED;
                                ret.setCode(statusCode);
                            } finally {
                                try {
                                    os.flush();
                                    os.close();
                                } catch (IOException e) {
                                    ABLog.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData(), statusCode);

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                mException = e;
            } catch (Exception e) {
                mException = new ABException(e);
            }
            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) mCallback.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null) mCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(ABResult<Map<String, Object>> result) {
            super.onPostExecute(result);
            if (mCallback != null && !isCancelled()) mCallback.onPostExecute(result, mException);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) mCallback.onCancelled();
        }

        /*
        //@see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
        public void execute() {
            super.execute();
        }
        */
    }

    public static class AsyncPatch extends AsyncTask<Void, Integer, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;
        private ABException mException = null;

        public AsyncPatch(String url, String body, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
            mCallback = asyncCallback;
        }
        public AsyncPatch(String url, HttpEntity entity, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPatch request = new HttpPatch(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        long contentLength = entity.getContentLength();
                        if (contentLength <= 0) {
                            ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                        } else {
                            InputStream inputStream = entity.getContent();
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            OutputStream os = new BufferedOutputStream(b);
                            int c;
                            long wroteBytes = 0;
                            try {
                                while ((c = inputStream.read()) != -1) {
                                    if (isCancelled()) {
                                        throw new InterruptedException();
                                    }
                                    os.write(c);
                                    wroteBytes++;
                                    if ((wroteBytes % OUTPUT_BUFFER_SIZE) == 0) {
                                        publishProgress((int) (((float) wroteBytes / (float) contentLength) * 100));
                                    }
                                }
                                ABLog.d(TAG, "wroteBytes: " + wroteBytes);
                                byte[] bin = b.toByteArray();
                                ret.setRawData(bin);
                            } catch (IOException e) {
                                ABLog.e(TAG, e.getMessage());
                                throw e;
                            } catch (InterruptedException e) {
                                statusCode = ABStatus.OPERATION_CANCELLED;
                                ret.setCode(statusCode);
                            } finally {
                                try {
                                    os.flush();
                                    os.close();
                                } catch (IOException e) {
                                    ABLog.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData(), statusCode);

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                mException = e;
            } catch (Exception e) {
                mException = new ABException(e);
            }
            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) mCallback.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null && !isCancelled()) mCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(ABResult<Map<String, Object>> result) {
            super.onPostExecute(result);
            if (mCallback != null) mCallback.onPostExecute(result, mException);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) mCallback.onCancelled();
        }

        //@see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
        public void execute() {
            super.execute();
        }
    }

    public static class AsyncDelete extends AsyncTask<Void, Integer, ABResult<Map<String, Object>>> {
        private String mUrl;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;
        private ABException mException = null;

        public AsyncDelete(String url, Map<String, String> headers, AsyncCallback<Map<String, Object>> asyncCallback) {
            mUrl = url;
            mHeaders = headers;
            mCallback = asyncCallback;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpDelete request = new HttpDelete(mUrl);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        long contentLength = entity.getContentLength();
                        if (contentLength <= 0) {
                            ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                        } else {
                            InputStream inputStream = entity.getContent();
                            ByteArrayOutputStream b = new ByteArrayOutputStream();
                            OutputStream os = new BufferedOutputStream(b);
                            int c;
                            long wroteBytes = 0;
                            try {
                                while ((c = inputStream.read()) != -1) {
                                    if (isCancelled()) {
                                        throw new InterruptedException();
                                    }
                                    os.write(c);
                                    wroteBytes++;
                                    if ((wroteBytes % OUTPUT_BUFFER_SIZE) == 0) {
                                        publishProgress((int) (((float) wroteBytes / (float) contentLength) * 100));
                                    }
                                }
                                ABLog.d(TAG, "wroteBytes: " + wroteBytes);
                                byte[] bin = b.toByteArray();
                                ret.setRawData(bin);
                            } catch (IOException e) {
                                ABLog.e(TAG, e.getMessage());
                                throw e;
                            } catch (InterruptedException e) {
                                statusCode = ABStatus.OPERATION_CANCELLED;
                                ret.setCode(statusCode);
                            } finally {
                                try {
                                    os.flush();
                                    os.close();
                                } catch (IOException e) {
                                    ABLog.e(TAG, e.getMessage());
                                }
                            }
                        }
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData(), statusCode);

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                mException = e;
            } catch (Exception e) {
                mException = new ABException(e);
            }
            return ret;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mCallback != null) mCallback.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (mCallback != null && !isCancelled()) mCallback.onProgressUpdate(values[0]);
        }

        @Override
        protected void onPostExecute(ABResult<Map<String, Object>> result) {
            super.onPostExecute(result);
            if (mCallback != null) mCallback.onPostExecute(result, mException);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mCallback != null) mCallback.onCancelled();
        }

        //@see http://stackoverflow.com/questions/20455644/object-cannot-be-cast-to-void-in-asynctask
        public void execute() {
            super.execute();
        }
    }

    public static class SyncGet extends AsyncTask<Void, Void, ABResult<Map<String, Object>>> {
        private String mUrl;
        private Map<String, String> mHeaders;

        public SyncGet(String url, Map<String, String> headers) {
            mUrl = url;
            mHeaders = headers;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet request = new HttpGet(mUrl);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData());

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, e);
            } catch (Exception e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, new ABException(e));

            }
            return ret;
        }
    }

    public static class SyncPut extends AsyncTask<Void, Void, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;

        public SyncPut(String url, String body, Map<String, String> headers) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
        }
        public SyncPut(String url, HttpEntity entity, Map<String, String> headers) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPut request = new HttpPut(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData());

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, e);
            } catch (Exception e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, new ABException(e));
            }
            return ret;
        }
    }

    public static class SyncPost extends AsyncTask<Void, Void, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;

        public SyncPost(String url, String body, Map<String, String> headers) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
        }
        public SyncPost(String url, HttpEntity entity, Map<String, String> headers) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPost request = new HttpPost(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData());

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, e);
            } catch (Exception e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, new ABException(e));
            }
            return ret;
        }
    }

    public static class SyncPatch extends AsyncTask<Void, Void, ABResult<Map<String, Object>>> {
        private String mUrl;
        private String mBody;
        private HttpEntity mEntity;
        private Map<String, String> mHeaders;

        public SyncPatch(String url, String body, Map<String, String> headers) {
            mUrl = url;
            mBody = body;
            mEntity = null;
            mHeaders = headers;
        }
        public SyncPatch(String url, HttpEntity entity, Map<String, String> headers) {
            mUrl = url;
            mBody = null;
            mEntity = entity;
            mHeaders = headers;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpPatch request = new HttpPatch(mUrl);
            request.setEntity(mEntity);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            if (mBody != null) {
                try {
                    request.setEntity(new StringEntity(mBody, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData());

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, e);
            } catch (Exception e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, new ABException(e));
            }
            return ret;
        }

    }

    public static class SyncDelete extends AsyncTask<Void, Void, ABResult<Map<String, Object>>> {
        private String mUrl;
        private Map<String, String> mHeaders;
        private AsyncCallback<Map<String, Object>> mCallback = null;

        public SyncDelete(String url, Map<String, String> headers) {
            mUrl = url;
            mHeaders = headers;
        }

        @Override
        protected ABResult<Map<String, Object>> doInBackground(Void... params) {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, AB.Config.getConnectionTimeoutInterval() * 1000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpDelete request = new HttpDelete(mUrl);
            for (Map.Entry<String, String> entry : mHeaders.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
            HttpHelper.dumpHttpRequest(client, request);

            ABResult<Map<String, Object>> ret = new ABResult<>();
            ret.putExtra(ABResult.EXTRA_KEY_REQUEST, request);

            try {
                HttpResponse response = client.execute(request);

                ret.putExtra(ABResult.EXTRA_KEY_RESPONSE, response);
                int statusCode = response.getStatusLine().getStatusCode();
                ret.setCode(statusCode);

                HttpEntity entity = response.getEntity();
                SortedSet<String> detailCodes = null;
                SortedSet<String> errorFields = null;
                String responseBody = null;
                if (entity != null) {
                    String contentType = response.getFirstHeader("Content-Type").getValue();
                    if ("application/json".equals(contentType)) {
                        responseBody = EntityUtils.toString(entity, "UTF-8");
                        if (responseBody.length() > 0) {
                            ret.setRawData(responseBody.getBytes());
                            ObjectMapper mapper = new ObjectMapper();
                            @SuppressWarnings("unchecked") Map<String, Object> jsonMap = mapper.readValue(responseBody, Map.class);

                            if (jsonMap.containsKey("_error")) {
                                @SuppressWarnings("unchecked") List<String> detailCodeArray = (List<String>)jsonMap.get("_error");
                                detailCodes = new TreeSet<>(detailCodeArray);
                                jsonMap.remove("_error");
                            }
                            if (jsonMap.containsKey("_invalid_name")) {
                                @SuppressWarnings("unchecked") List<String> errorFieldArray = (List<String>)jsonMap.get("_invalid_name");
                                errorFields = new TreeSet<>(errorFieldArray);
                                jsonMap.remove("_invalid_name");
                            }
                            ret.setData(jsonMap);
                        }
                    } else {
                        ret.setRawData(AB.Helper.StreamHelper.getBytesFromInputStream(entity.getContent()));
                    }
                }
                HttpHelper.dumpHttpResponse(response, responseBody, ret.getRawData());

                if (200 > statusCode || statusCode >= 400) {
                    throw new ABException(statusCode, detailCodes, errorFields);
                }

            } catch (ABException e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, e);
            } catch (Exception e) {
                ret.putExtra(ABResult.EXTRA_KEY_EXCEPTION, new ABException(e));
            }
            return ret;
        }
    }

    public static class HttpHelper {

        public static void dumpHttpResponse(HttpResponse response, String responseBody, byte[] rawData) {
            dumpHttpResponse(response, responseBody, rawData, response.getStatusLine().getStatusCode());
        }
        public static void dumpHttpResponse(HttpResponse response, String responseBody, byte[] rawData, int statusCode) {
            //NOTE: 4つ目の引数 statusCode は、Cancel 時などにレスポンスステータスを強制的に書き換えたい場合のために設けたもの

            if (response == null) return;

            StatusLine statusLine = response.getStatusLine();
            String meaningOfStatusCode = ABException.getDefaultMessage(statusCode);
            String responsePhase = statusLine.getReasonPhrase();

            Locale locale = response.getLocale();
            String localString = locale.toString();
            ProtocolVersion protocolVersion = response.getProtocolVersion();
            String protocol = protocolVersion.getProtocol();
            HttpParams params = response.getParams();
            Header[] headers = response.getAllHeaders();
            for (Header h : headers) {
                ABLog.d(TAG, String.format("header: %s=%s", h.getName(), h.getValue()));
            }

            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP RESPONSE] <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            buff.append(String.format("\nCode : %d", statusCode));
            if (meaningOfStatusCode != null) {
                buff.append(String.format(" (%s)", meaningOfStatusCode));
            }
            buff.append("\nHeaders:");
            for (int i = 0; i < headers.length; i++) {
                buff.append(String.format("\n[%d] %s: %s", i, headers[i].getName(), headers[i].getValue()));
            }
            if (responseBody != null) {
                buff.append(String.format("\nBody:(bytes=%d) %s", response.getEntity().getContentLength(), responseBody));
            } else {
                if (rawData != null) {
                    buff.append("\nBody: <BINARY>");
                } else {
                    buff.append("\nBody: (empty)");
                }
            }
            buff.append("\n<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            ABLog.d(TAG, buff.toString());
        }

        public static void dumpHttpRequest(HttpClient client, HttpGet request) {
            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod();
            String uri = requestLine.getUri();
            HttpParams params = client.getParams();
            Header[] headers = request.getAllHeaders();
            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP REQUEST] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            buff.append(String.format("\nMethod : %s", method));
            buff.append(String.format("\nURI    : %s", uri));
            buff.append("\nHeaders:");
            for (int i = 0; i < headers.length; i++) {
                buff.append(String.format("\n[%d] %s: %s", i, headers[i].getName(), headers[i].getValue()));
            }
            buff.append("\nBody: (empty)");
            buff.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ABLog.d(TAG, buff.toString());
        }

        public static void dumpHttpRequest(HttpClient client, HttpPut request) {
            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod();
            String uri = requestLine.getUri();
            HttpParams params = client.getParams();
            Header[] headers = request.getAllHeaders();
            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP REQUEST] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            buff.append(String.format("\nMethod : %s", method));
            buff.append(String.format("\nURI    : %s", uri));
            buff.append("\nHeaders:");
            boolean isMultipart = false;
            for (int i = 0; i < headers.length; i++) {
                String name = headers[i].getName();
                String val  = headers[i].getValue();
                buff.append(String.format("\n[%d] %s: %s", i, name, val));
                if (val.toLowerCase().contains("multipart/form-data")) {
                    isMultipart = true;
                }
            }
            buff.append("\nBody:");
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                if (isMultipart) {
                    buff.append(String.format(" <multipart/form-data>"));
                } else {
                    try {
                        String responseBody = EntityUtils.toString(entity);
                        long length = entity.getContentLength();
                        buff.append(String.format("(bytes=%d): %s", length, responseBody));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            buff.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ABLog.d(TAG, buff.toString());
        }

        public static void dumpHttpRequest(HttpClient client, HttpPost request) {
            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod();
            String uri = requestLine.getUri();
            HttpParams params = client.getParams();
            Header[] headers = request.getAllHeaders();
            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP REQUEST] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            buff.append(String.format("\nMethod : %s", method));
            buff.append(String.format("\nURI    : %s", uri));
            buff.append("\nHeaders:");
            boolean isMultipart = false;
            for (int i = 0; i < headers.length; i++) {
                String name = headers[i].getName();
                String val  = headers[i].getValue();
                buff.append(String.format("\n[%d] %s: %s", i, name, val));
                if (val.toLowerCase().contains("multipart/form-data")) {
                    isMultipart = true;
                }
            }
            buff.append("\nBody:");
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                if (isMultipart) {
                    buff.append(String.format(" <multipart/form-data>"));
                } else {
                    try {
                        String responseBody = EntityUtils.toString(entity);
                        long length = entity.getContentLength();
                        buff.append(String.format("(bytes=%d): %s", length, responseBody));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            buff.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ABLog.d(TAG, buff.toString());
        }

        public static void dumpHttpRequest(HttpClient client, HttpPatch request) {
            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod();
            String uri = requestLine.getUri();
            HttpParams params = client.getParams();
            Header[] headers = request.getAllHeaders();
            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP REQUEST] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            buff.append(String.format("\nMethod : %s", method));
            buff.append(String.format("\nURI    : %s", uri));
            buff.append("\nHeaders:");
            boolean isMultipart = false;
            for (int i = 0; i < headers.length; i++) {
                String name = headers[i].getName();
                String val  = headers[i].getValue();
                buff.append(String.format("\n[%d] %s: %s", i, name, val));
                if (val.toLowerCase().contains("multipart/form-data")) {
                    isMultipart = true;
                }
            }
            buff.append("\nBody:");
            HttpEntity entity = request.getEntity();
            if (entity != null) {
                if (isMultipart) {
                    buff.append(String.format(" <multipart/form-data>"));
                } else {
                    try {
                        String responseBody = EntityUtils.toString(entity);
                        long length = entity.getContentLength();
                        buff.append(String.format("(bytes=%d): %s", length, responseBody));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            buff.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ABLog.d(TAG, buff.toString());
        }
        public static void dumpHttpRequest(HttpClient client, HttpDelete request) {
            RequestLine requestLine = request.getRequestLine();
            String method = requestLine.getMethod();
            String uri = requestLine.getUri();
            HttpParams params = client.getParams();
            Header[] headers = request.getAllHeaders();
            StringBuilder buff = new StringBuilder();

            buff.append(".\n[DUMP REQUEST] >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            buff.append(String.format("\nMethod : %s", method));
            buff.append(String.format("\nURI    : %s", uri));
            buff.append("\nHeaders:");
            for (int i = 0; i < headers.length; i++) {
                buff.append(String.format("\n[%d] %s: %s", i, headers[i].getName(), headers[i].getValue()));
            }
            buff.append("\nBody: (empty)");
            buff.append("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            ABLog.d(TAG, buff.toString());
        }

    }

    private static String TAG = ABRestClient.class.getSimpleName();

    static final String BOUNDARY = "--------------------------1d6e3a3a1c734ea";

    /**
     * デフォルトヘッダを返す
     * @return デフォルトヘッダ
     */
    public static Map<String, String> getDefaultHeaders() {
        //XXX: 都度生成は無駄 (だけど applicationToken は都度取得しないとダメ)
        Map<String, String> headers = new HashMap<>();
        headers.put("X-APPIARIES-TOKEN", AB.Config.getApplicationToken());
        headers.put("Content-Type", "application/json");
        headers.put("User-Agent", AB.Config.getUserAgent());
        return headers;
    }

    /**
     * Multipart用のデフォルトヘッダを返す
     * @return Multipart用のデフォルトヘッダ
     */
    public static Map<String, String>getDefaultMultipartHeaders() {
        //XXX: 都度生成は無駄 (だけど applicationToken は都度取得しないとダメ)
        Map<String, String> headers = new HashMap<>();
        headers.put("X-APPIARIES-TOKEN", AB.Config.getApplicationToken());
        headers.put("Content-Type", String.format("multipart/form-data; boundary=%s", BOUNDARY));
        headers.put("User-Agent", AB.Config.getUserAgent());
        return headers;
    }

    /**
     * デフォルタヘッダを返す (ログイン中であれば X-APPIARIES-TOKEN にストアトークンを指定する)
     * @return デフォルタヘッダ
     */
    public static Map<String, String> getDefaultHeadersWithStoreTokenIfPossible() {
        Map<String, String> headers = ABRestClient.getDefaultHeaders();
        String storeToken = AB.Session.getToken();
        if (storeToken != null) {
            headers.put("X-APPIARIES-TOKEN", storeToken);
        }
        return headers;
    }

    /**
     * Multipart用のデフォルトヘッダを返す (ログイン中であれば X-APPIARIES-TOKEN にストアトークンを指定する)
     * @return Multipart用のデフォルトヘッダ
     */
    public static Map<String, String> getDefaultMultipartHeadersWithStoreTokenIfPossible() {
        Map<String, String> headers = ABRestClient.getDefaultMultipartHeaders();
        String storeToken = AB.Session.getToken();
        if (storeToken != null) {
            headers.put("X-APPIARIES-TOKEN", storeToken);
        }
        return headers;
    }

    public static void GET(final String url, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.GET(url, ABRestClient.getDefaultHeaders(), callback, null);
    }

    public static void GET(final String url, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.GET(url, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }

    public static void GET(final String url, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.GET(url, headers, callback, null);
    }

    public static void GET(final String url, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        if (!isConnectedNetwork()) {
            if (callback != null) {
                ABException e = new ABException(1001, "Network is unreachable");
                callback.internalDone(null, e);
            }
            return;
        }

        final String REQUEST_KEY = "GET_" + url;

        final ABResult<Map<String, Object>> ret = new ABResult<>();

        AsyncGet request = new AsyncGet(url, headers, new AsyncCallback<Map<String, Object>>() {

            @Override
            public void onPreExecute() {
                ABLog.d(TAG, "AsyncGet::onPreExecute");
            }

            @Override
            public void onPostExecute(ABResult<Map<String, Object>> restResult, ABException e) {
                //ABLog.d(TAG, "AsyncGet::onPostExecute");
                ABRestClient.dequeue(REQUEST_KEY);

                if (callback != null) {
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    ret.setData(restResult.getData());
                    callback.internalDone(ret, e);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {
                //ABLogger.d(TAG, "AsyncGet::onProgressUpdate");
                float p = (float)progress / 100;
                if (p <= 1.0 && progressCallback != null) {
                    progressCallback.internalUpdateProgress(p);
                }
            }

            @Override
            public void onCancelled() {
                //ABLog.d(TAG, "AsyncGet::onCancelled");
                if (callback != null) {
                    ret.setCode(ABStatus.OPERATION_CANCELLED);
                    ABException e = new ABException(ABStatus.OPERATION_CANCELLED);
                    callback.internalDone(ret, e);
                }
            }
        });

        ABRestClient.enqueue(REQUEST_KEY, request);

        request.execute();
    }

    public static void PUT(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PUT(url, null, entity, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void PUT(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PUT(url, null, entity, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void PUT(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PUT(url, null, entity, headers, callback, null);
    }
    public static void PUT(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PUT(url, null, entity, headers, callback, progressCallback);
    }
    public static void PUT(final String url, final String body, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PUT(url, body, null, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void PUT(final String url, final String body, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PUT(url, body, null, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void PUT(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PUT(url, body, null, headers, callback, null);
    }
    public static void PUT(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PUT(url, body, null, headers, callback, progressCallback);
    }
    private static void PUT(final String url, final String body, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        if (!isConnectedNetwork()) {
            if (callback != null) {
                ABException e = new ABException(1001, "Network is unreachable");
                callback.internalDone(null, e);
            }
            return;
        }

        final String REQUEST_KEY = "PUT_" + url;

        final ABResult<Map<String, Object>> ret = new ABResult<>();

        AsyncCallback<Map<String, Object>> asyncCallback = new AsyncCallback<Map<String, Object>>() {

            @Override
            public void onPreExecute() {
                ABLog.d(TAG, "AsyncPut::onPreExecute");
            }

            @Override
            public void onPostExecute(ABResult<Map<String, Object>> restResult, ABException e) {
                //ABLog.d(TAG, "AsyncPut::onPostExecute");
                ABRestClient.dequeue(REQUEST_KEY);

                if (callback != null) {
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    ret.setData(restResult.getData());
                    callback.internalDone(ret, e);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {
                //ABLog.d(TAG, "AsyncPut::onProgressUpdate");
                float p = (float)progress / 100;
                if (p <= 1.0 && progressCallback != null) {
                    progressCallback.internalUpdateProgress(p);
                }
            }

            @Override
            public void onCancelled() {
                //ABLog.d(TAG, "AsyncPut::onCancelled");
                if (callback != null) {
                    ret.setCode(ABStatus.OPERATION_CANCELLED);
                    ABException e = new ABException(ABStatus.OPERATION_CANCELLED);
                    callback.internalDone(ret, e);
                }
            }
        };

        AsyncPut request;
        if (body != null) {
            request = new AsyncPut(url, body, headers, asyncCallback);
        } else {
            request = new AsyncPut(url, entity, headers, asyncCallback);
        }

        ABRestClient.enqueue(REQUEST_KEY, request);

        request.execute();
    }

    public static void POST(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.POST(url, entity, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void POST(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.POST(url, entity, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void POST(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.POST(url, entity, headers, callback, null);
    }
    public static void POST(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        POST(url, null, entity, headers, callback, progressCallback);
    }
    public static void POST(final String url, final String body, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.POST(url, body, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void POST(final String url, final String body, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.POST(url, body, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void POST(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.POST(url, body, headers, callback, null);
    }
    public static void POST(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        POST(url, body, null, headers, callback, progressCallback);
    }
    private static void POST(final String url, final String body, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        if (!isConnectedNetwork()) {
            if (callback != null) {
                ABException e = new ABException(1001, "Network is unreachable");
                callback.internalDone(null, e);
            }
            return;
        }

        final String REQUEST_KEY = "POST_" + url;

        final ABResult<Map<String, Object>> ret = new ABResult<>();

        AsyncCallback<Map<String, Object>> asyncCallback = new AsyncCallback<Map<String, Object>>() {

            @Override
            public void onPreExecute() {
                ABLog.d(TAG, "AsyncPost::onPreExecute");
            }

            @Override
            public void onPostExecute(ABResult<Map<String, Object>> restResult, ABException e) {
                //ABLog.d(TAG, "AsyncPost::onPostExecute");
                ABRestClient.dequeue(REQUEST_KEY);

                if (callback != null) {
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    ret.setData(restResult.getData());
                    callback.internalDone(ret, e);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {
                //ABLog.d(TAG, "AsyncPost::onProgressUpdate");
                float p = (float)progress / 100;
                if (p <= 1.0 && progressCallback != null) {
                    progressCallback.internalUpdateProgress(p);
                }
            }

            @Override
            public void onCancelled() {
                //ABLog.d(TAG, "AsyncPost::onCancelled");
                if (callback != null) {
                    ret.setCode(ABStatus.OPERATION_CANCELLED);
                    ABException e = new ABException(ABStatus.OPERATION_CANCELLED);
                    callback.internalDone(ret, e);
                }
            }
        };

        AsyncPost request;
        if (body != null) {
            request = new AsyncPost(url, body, headers, asyncCallback);
        } else {
            request = new AsyncPost(url, entity, headers, asyncCallback);
        }

        ABRestClient.enqueue(REQUEST_KEY, request);

        request.execute();
    }

    public static void PATCH(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PATCH(url, null, entity, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void PATCH(final String url, final HttpEntity entity, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PATCH(url, null, entity, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void PATCH(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PATCH(url, null, entity, headers, callback, null);
    }
    public static void PATCH(final String url, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PATCH(url, null, entity, headers, callback, progressCallback);
    }
    public static void PATCH(final String url, final String body, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PATCH(url, body, null, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void PATCH(final String url, final String body, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PATCH(url, body, null, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void PATCH(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.PATCH(url, body, null, headers, callback, null);
    }
    public static void PATCH(final String url, final String body, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.PATCH(url, body, null, headers, callback, progressCallback);
    }
    private static void PATCH(final String url, final String body, final HttpEntity entity, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        if (!isConnectedNetwork()) {
            if (callback != null) {
                ABException e = new ABException(1001, "Network is unreachable");
                callback.internalDone(null, e);
            }
            return;
        }

        final String REQUEST_KEY = "PATCH_" + url;

        final ABResult<Map<String, Object>> ret = new ABResult<>();

        AsyncCallback<Map<String, Object>> asyncCallback = new AsyncCallback<Map<String, Object>>() {

            @Override
            public void onPreExecute() {
                ABLog.d(TAG, "AsyncPatch::onPreExecute");
            }

            @Override
            public void onPostExecute(ABResult<Map<String, Object>> restResult, ABException e) {
                //ABLog.d(TAG, "AsyncPatch::onPostExecute");
                ABRestClient.dequeue(REQUEST_KEY);

                if (callback != null) {
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    ret.setData(restResult.getData());
                    callback.internalDone(ret, e);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {
                //ABLog.d(TAG, "AsyncPatch::onProgressUpdate");
                float p = (float)progress / 100;
                if (p <= 1.0 && progressCallback != null) {
                    progressCallback.internalUpdateProgress(p);
                }
            }

            @Override
            public void onCancelled() {
                //ABLog.d(TAG, "AsyncPatch::onCancelled");
                if (callback != null) {
                    ret.setCode(ABStatus.OPERATION_CANCELLED);
                    ABException e = new ABException(ABStatus.OPERATION_CANCELLED);
                    callback.internalDone(ret, e);
                }
            }
        };

        AsyncPatch request;
        if (body != null) {
            request = new AsyncPatch(url, body, headers, asyncCallback);
        } else {
            request = new AsyncPatch(url, entity, headers, asyncCallback);
        }

        ABRestClient.enqueue(REQUEST_KEY, request);

        request.execute();
    }

    public static void DELETE(final String url, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.DELETE(url, ABRestClient.getDefaultHeaders(), callback, null);
    }
    public static void DELETE(final String url, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        ABRestClient.DELETE(url, ABRestClient.getDefaultHeaders(), callback, progressCallback);
    }
    public static void DELETE(final String url, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback) {
        ABRestClient.DELETE(url, headers, callback, null);
    }
    public static void DELETE(final String url, final Map<String, String> headers, final ResultCallback<Map<String, Object>> callback, final ProgressCallback progressCallback) {
        if (!isConnectedNetwork()) {
            if (callback != null) {
                ABException e = new ABException(1001, "Network is unreachable");
                callback.internalDone(null, e);
            }
            return;
        }

        final String REQUEST_KEY = "DELETE_" + url;

        final ABResult<Map<String, Object>> ret = new ABResult<>();

        AsyncDelete request = new AsyncDelete(url, headers, new AsyncCallback<Map<String, Object>>() {

            @Override
            public void onPreExecute() {
                ABLog.d(TAG, "AsyncDelete::onPreExecute");
            }

            @Override
            public void onPostExecute(ABResult<Map<String, Object>> restResult, ABException e) {
                //ABLog.d(TAG, "AsyncDelete::onPostExecute");
                ABRestClient.dequeue(REQUEST_KEY);

                if (callback != null) {
                    ret.setCode(restResult.getCode());
                    ret.setExtra(restResult.getExtra());
                    ret.setRawData(restResult.getRawData());
                    ret.setData(restResult.getData());
                    callback.internalDone(ret, e);
                }
            }

            @Override
            public void onProgressUpdate(int progress) {
                //ABLog.d(TAG, "AsyncDelete::onProgressUpdate");
                float p = (float)progress / 100;
                if (p <= 1.0 && progressCallback != null) {
                    progressCallback.internalUpdateProgress(p);
                }
            }

            @Override
            public void onCancelled() {
                //ABLog.d(TAG, "AsyncDelete::onCancelled");
                if (callback != null) {
                    ret.setCode(ABStatus.OPERATION_CANCELLED);
                    ABException e = new ABException(ABStatus.OPERATION_CANCELLED);
                    callback.internalDone(ret, e);
                }
            }
        });

        ABRestClient.enqueue(REQUEST_KEY, request);

        request.execute();
    }

    public static ABResult<Map<String, Object>> syncGET(final String url) throws ABException {
        return ABRestClient.syncGET(url, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncGET(final String url, final Map<String, String> headers) throws ABException {
        if (!isConnectedNetwork()) {
            throw new ABException(1001, "Network is unreachable");
        }

        SyncGet request = new SyncGet(url, headers);
        ABResult<Map<String, Object>> ret = new ABResult<>();
        try {
            ABResult<Map<String, Object>> restResult = request.execute().get();
            ABException e = (ABException)restResult.getExtra().get("exception");
            if (e != null) throw e;
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            ret.setData(restResult.getData());
        } catch (InterruptedException | ExecutionException e) {
            ABLog.e(TAG, e.getMessage());
            throw new ABException(e);
        }
        return ret;
    }

    public static ABResult<Map<String, Object>> syncPUT(final String url, final HttpEntity entity) throws ABException {
        return ABRestClient.syncPUT(url, null, entity, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPUT(final String url, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPUT(url, null, entity, headers);
    }
    public static ABResult<Map<String, Object>> syncPUT(final String url, final String body) throws ABException {
        return ABRestClient.syncPUT(url, body, null, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPUT(final String url, final String body, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPUT(url, body, null, headers);
    }
    private static ABResult<Map<String, Object>> syncPUT(final String url, final String body, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        if (!isConnectedNetwork()) {
            throw new ABException(1001, "Network is unreachable");
        }

        SyncPut request;
        if (body != null) {
            request = new SyncPut(url, body, headers);
        }  else {
            request = new SyncPut(url, entity, headers);
        }
        ABResult<Map<String, Object>> ret = new ABResult<>();
        try {
            ABResult<Map<String, Object>> restResult = request.execute().get();
            ABException e = (ABException)restResult.getExtra().get("exception");
            if (e != null) throw e;
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            ret.setData(restResult.getData());
        } catch (InterruptedException | ExecutionException e) {
            ABLog.e(TAG, e.getMessage());
            throw new ABException(e);
        }
        return ret;
    }

    public static ABResult<Map<String, Object>> syncPOST(final String url, final HttpEntity entity) throws ABException {
        return ABRestClient.syncPOST(url, null, entity, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPOST(final String url, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPOST(url, null, entity, headers);
    }
    public static ABResult<Map<String, Object>> syncPOST(final String url, final String body) throws ABException {
        return ABRestClient.syncPOST(url, body, null, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPOST(final String url, final String body, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPOST(url, body, null, headers);
    }
    private static ABResult<Map<String, Object>> syncPOST(final String url, final String body, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        if (!isConnectedNetwork()) {
            throw new ABException(1001, "Network is unreachable");
        }

        SyncPost request;
        if (body != null) {
            request = new SyncPost(url, body, headers);
        } else {
            request = new SyncPost(url, entity, headers);
        }
        ABResult<Map<String, Object>> ret = new ABResult<>();
        try {
            ABResult<Map<String, Object>> restResult = request.execute().get();
            ABException e = (ABException)restResult.getExtra().get("exception");
            if (e != null) throw e;
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            ret.setData(restResult.getData());
        } catch (InterruptedException | ExecutionException e) {
            ABLog.e(TAG, e.getMessage());
            throw new ABException(e);
        }
        return ret;
    }

    public static ABResult<Map<String, Object>> syncPATCH(final String url, final HttpEntity entity) throws ABException {
        return ABRestClient.syncPATCH(url, null, entity, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPATCH(final String url, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPATCH(url, null, entity, headers);
    }
    public static ABResult<Map<String, Object>> syncPATCH(final String url, final String body) throws ABException {
        return ABRestClient.syncPATCH(url, body, null, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncPATCH(final String url, final String body, final Map<String, String> headers) throws ABException {
        return ABRestClient.syncPATCH(url, body, null, headers);
    }
    private static ABResult<Map<String, Object>> syncPATCH(final String url, final String body, final HttpEntity entity, final Map<String, String> headers) throws ABException {
        if (!isConnectedNetwork()) {
            throw new ABException(1001, "Network is unreachable");
        }

        SyncPatch request;
        if (body != null) {
            request = new SyncPatch(url, body, headers);
        } else {
            request = new SyncPatch(url, entity, headers);
        }

        ABResult<Map<String, Object>> ret = new ABResult<>();
        try {
            ABResult<Map<String, Object>> restResult = request.execute().get();
            ABException e = (ABException)restResult.getExtra().get("exception");
            if (e != null) throw e;
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            ret.setData(restResult.getData());
        } catch (InterruptedException | ExecutionException e) {
            ABLog.e(TAG, e.getMessage());
            throw new ABException(e);
        }
        return ret;
    }

    public static ABResult<Map<String, Object>> syncDELETE(final String url) throws ABException {
        return ABRestClient.syncDELETE(url, ABRestClient.getDefaultHeaders());
    }
    public static ABResult<Map<String, Object>> syncDELETE(final String url, final Map<String, String> headers) throws ABException {
        if (!isConnectedNetwork()) {
            throw new ABException(1001, "Network is unreachable");
        }

        SyncDelete request = new SyncDelete(url, headers);
        ABResult<Map<String, Object>> ret = new ABResult<>();
        try {
            ABResult<Map<String, Object>> restResult = request.execute().get();
            ABException e = (ABException)restResult.getExtra().get("exception");
            if (e != null) throw e;
            ret.setCode(restResult.getCode());
            ret.setExtra(restResult.getExtra());
            ret.setRawData(restResult.getRawData());
            ret.setData(restResult.getData());
        } catch (InterruptedException | ExecutionException e) {
            ABLog.e(TAG, e.getMessage());
            throw new ABException(e);
        }
        return ret;
    }

    public static void enqueue(String requestKey, AsyncTask task) {
        if (mRequestQueue.containsKey(requestKey)) {
            AsyncTask oldTask = mRequestQueue.get(requestKey);
            oldTask.cancel(true);
        }
        mRequestQueue.put(requestKey, task);
    }
    public static void dequeue(String requestKey) {
        if (mRequestQueue.containsKey(requestKey)) {
            mRequestQueue.remove(requestKey);
        }
    }
    public static void cancel(Pattern requestPattern) {
        boolean success;
        List<String> keys = new ArrayList<>();
        Set<String> keySet = mRequestQueue.keySet();
        for (String key : keySet) {
            Matcher matcher = requestPattern.matcher(key);
            if (matcher.find()) {
                keys.add(key);
            }
        }
//        if (keys.size() == 0) return; //XXX: キャンセル後も複数回メソッドがコールされる場合があるので
//                                      //     操作対象がなければここで抜ける。
        for (String key : keys) {
            AsyncTask task = mRequestQueue.get(key);
            task.cancel(true);
        }
    }
    public static void cancelAll() {
        cancelAll(null);
    }
    public static void cancelAll(Pattern requestPattern) {
        if (requestPattern != null) {
            cancel(requestPattern);
        } else {
            //remove all
            for (Map.Entry<String, AsyncTask> entry : mRequestQueue.entrySet()) {
                AsyncTask task = entry.getValue();
                task.cancel(true);
            }
            mRequestQueue.clear();
        }
    }

    private static boolean isConnectedNetwork() {
        ConnectivityManager cm = (ConnectivityManager) AB.sApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
