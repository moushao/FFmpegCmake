package com.pvirtch.ffmpegcmake;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.pvirtch.ffmpegcmake.permission.CheckPermListener;
import com.pvirtch.ffmpegcmake.permission.EasyPermissions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.main_surface_play) SurfaceView mSurface;
    @BindView(R.id.sample_text) TextView mSampleText;
    @BindView(R.id.play) Button mPlay;
    @BindView(R.id.compress) Button mCompress;
    @BindView(R.id.video_name) TextView mVideoName;
    @BindView(R.id.video_length) TextView mVideoLength;
    @BindView(R.id.video_size) TextView mVideoSize;
    @BindView(R.id.video_compress_size) TextView mVideoCompressSize;
    @BindView(R.id.video_path) TextView mVideoPath;
    @BindView(R.id.video_compress_path) TextView mVideoCompressPath;
    @BindView(R.id.print) Button mPrint;
    @BindView(R.id.ffmpeg_info) TextView mInfo;

    private SurfaceHolder mHolder;
    private String[] s = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission
            .WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission
            .INTERNET, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS};
    private LocalMedia localMedia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mHolder = mSurface.getHolder();

        printABIInfo();
    }

    @OnClick({R.id.play, R.id.compress, R.id.print})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.play:
                if (localMedia == null) {
                    gotoSelectVedio(99);
                } else {
                    playVideo(localMedia.getPath());
                }
                break;
            case R.id.compress:
                gotoSelectVedio(PictureConfig.CHOOSE_REQUEST);
                break;
            case R.id.print:
                mInfo.setText(FFmpegKit.stringFromFFmpeg());
                break;
        }
    }

    //打印当前手机支持哪些CPU架构
    private void printABIInfo() {
        String[] abis = new String[]{};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            abis = Build.SUPPORTED_ABIS;
        } else {
            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
        StringBuilder abiStr = new StringBuilder();
        for (String abi : abis) {
            abiStr.append(abi);
            abiStr.append(',');
        }

        Log.e("abi", abiStr.toString());
    }


    private void playVideo(final String path) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                FFmpegKit.play(mHolder.getSurface(), path);
            }
        }).start();
    }

    private CheckPermListener mPermissionListener;

    public void checkPermission(CheckPermListener listener, int resString, String... mPerms) {
        mPermissionListener = listener;
        if (EasyPermissions.hasPermissions(this, mPerms)) {
            if (mPermissionListener != null)
                mPermissionListener.superPermission();
        } else {
            EasyPermissions.requestPermissions(this, getString(resString), 123, mPerms);
        }
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            if (selectList.size() != 0) {
                localMedia = selectList.get(0);
                mVideoName.setText("视频名称: " + localMedia.getPath().substring(localMedia.getPath().lastIndexOf
                        ("/") + 1));
                mVideoLength.setText("视频时长: " + localMedia.getDuration() / 1000 + "秒");
                mVideoSize.setText("压缩前大小: " + new File(localMedia.getPath()).length() / 1000 + "kB");
                mVideoPath.setText("压缩前路径: " + localMedia.getPath());

            }
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    gotoCompress();
                    break;
                case 99:
                    playVideo(localMedia.getPath());
                    break;
                case 88:
                    localMedia.setCompressPath(data.getStringExtra("cutPath"));
                    mVideoCompressSize.setText("视频压缩后大小: " +
                            new File(localMedia.getCompressPath()).length() / 1000 + " kb");
                    mVideoCompressPath.setText("视频压缩后路径: "+localMedia.getCompressPath());
//                    playVideo(localMedia.getCutPath());
                    break;

            }
        }
    }

    private void gotoCompress() {
        Intent itt = new Intent(this, PlayActivity.class);
        itt.putExtra("path", localMedia.getPath());
        startActivityForResult(itt, 88);
    }


    private void gotoSelectVedio(int requestCode) {
        PictureSelector.create(MainActivity.this)
                .openGallery(PictureMimeType.ofVideo())//全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频
                // .ofAudio()
                .maxSelectNum(1)// 最大图片选择数量 int
                .minSelectNum(1)// 最小选择数量 int
                .imageSpanCount(4)// 每行显示个数 int
                .previewVideo(true)// 是否可预览视频 true or false
                .imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                .sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效
                .setOutputCameraPath("/CustomPath")// 自定义拍照保存路径,可不填
                .isGif(false)// 是否显示gif图片 true or false
                .synOrAsy(true)//同步true或异步false 压缩 默认同步
                .videoMaxSecond(1000)// 显示多少秒以内的视频or音频也可适用 int 
                .videoMinSecond(1)// 显示多少秒以内的视频or音频也可适用 int 
                .isDragFrame(false)// 是否可拖动裁剪框(固定)
                .forResult(requestCode);//结果回调onActivityResult code     
    }
}
