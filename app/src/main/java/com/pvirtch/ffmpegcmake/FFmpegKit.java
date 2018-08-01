package com.pvirtch.ffmpegcmake;

import android.os.AsyncTask;
import android.view.Surface;

/**
 * Created by MouShao on 2018/2/28.
 */

public class FFmpegKit {

    public interface KitInterface {
        void onStart();

        void onProgress(int progress);

        void onEnd(int result);
    }

    static {
        System.loadLibrary("native-lib");
    }

    public static void execute(String[] commands, final KitInterface kitIntenrface) {
        new AsyncTask<String[], Integer, Integer>() {
            @Override
            protected void onPreExecute() {
                if (kitIntenrface != null) {
                    kitIntenrface.onStart();
                }
            }

            @Override
            protected Integer doInBackground(String[]... params) {
                return run(params[0]);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                if (kitIntenrface != null) {
                    kitIntenrface.onProgress(values[0]);
                }
            }

            @Override
            protected void onPostExecute(Integer integer) {
                if (kitIntenrface != null) {
                    kitIntenrface.onEnd(integer);
                }
            }
        }.execute(commands);
    }

    public static native String stringFromFFmpeg();

    public native static int play(Object surface, String path);

    public native static int run(String[] commands);

    public native void init(int codec_id, int srcW, int srcH, int fps, int bit_rate, int gop_size);

    public native int decode(byte[] in, int len, byte[] out, int scalerW, int scalerH, int needScaler);

    public native void uninit();

}
