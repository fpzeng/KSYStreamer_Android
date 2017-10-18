package com.ksyun.media.streamer.demo;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ksyun.media.streamer.filter.imgtex.ImgBeautyDenoiseFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyIllusionFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySkinWhitenFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySmoothFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySoftFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyStylizeFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyToneCurveFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.kit.KSYStreamer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Video filter choose fragment.
 */

public class VideoFilterFragment extends Fragment {
    public static final String TAG = "VideoFilterFragment";

    @BindView(R.id.filter_idx_spin)
    protected Spinner mFilterIdxSpinner;
    @BindView(R.id.filter_spin)
    protected Spinner mFilterSpinner;
    @BindView(R.id.beauty_grind)
    protected LinearLayout mBeautyGrindLayout;
    @BindView(R.id.beauty_whiten)
    protected LinearLayout mBeautyWhitenLayout;
    @BindView(R.id.beauty_ruddy)
    protected LinearLayout mBeautyRuddyLayout;
    @BindView(R.id.grind_seek_bar)
    protected SeekBar mGrindSeekBar;
    @BindView(R.id.whiten_seek_bar)
    protected SeekBar mWhitenSeekBar;
    @BindView(R.id.ruddy_seek_bar)
    protected SeekBar mRuddySeekBar;

    protected StdCameraActivity mActivity;
    protected KSYStreamer mStreamer;

    protected String[] mFilterIdxs = new String[]{"Filter1:", "Filter2:", "Filter3:"};
    protected String[] mPresetFilters = new String[]{
            "DISABLED", "BEAUTY_SOFT", "SKIN_WHITEN", "BEAUTY_ILLUSION", "BEAUTY_DENOISE",
            "BEAUTY_SMOOTH", "BEAUTY_PRO", "BEAUTY_PRO2", "BEAUTY_PRO3", "BEAUTY_PRO4",
            "DEMO_FILTER", "RGBABufFilter", "YUVBufFilter",
            "ToneCurve(ACV)", "复古(ACV)", "胶片(ACV)",
            "小清新", "青春靓丽", "甜美可人", "怀旧", "蓝调", "老照片", "樱花", "樱花(暗光)",
            "红润(暗光)", "阳光(暗光)", "红润", "阳光", "自然", "恋人", "高雅",
            "1977", "Amaro", "Brannan", "EarlyBird", "Hefe", "Hudson", "ink", "Lomo", "LordKelvin",
            "Nash", "Rise", "Sierra", "Sutro", "Toaster", "Valencia", "Walden", "XproII"
    };
    protected int mFilterPos = 0;
    protected ImgFilterBase[] mFilterArray = new ImgFilterBase[mFilterIdxs.length];
    protected int[] mFilterIdxArray = new int[mFilterIdxs.length];
    protected boolean mUIFirstInit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.video_filter_fragment, container, false);
        ButterKnife.bind(this, view);
        mActivity = (StdCameraActivity) getActivity();
        mStreamer = mActivity.mStreamer;
        initBeautyUI();
        return view;
    }

    protected void initBeautyUI() {
        mUIFirstInit = true;
        mFilterIdxArray[0] = 8;
        ArrayAdapter<String> filterIdxAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item, mFilterIdxs);
        filterIdxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilterIdxSpinner.setAdapter(filterIdxAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterIdxSpinner.setPopupBackgroundResource(R.color.transparent1);
        }
        mFilterIdxSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }
                mFilterPos = position;
                mFilterSpinner.setSelection(mFilterIdxArray[mFilterPos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mFilterIdxSpinner.setSelection(0);

        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(mActivity,
                android.R.layout.simple_spinner_item, mPresetFilters);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mFilterSpinner.setAdapter(filterAdapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterSpinner.setPopupBackgroundResource(R.color.transparent1);
        }
        mFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }

                if (mUIFirstInit || mFilterIdxArray[mFilterPos] != position) {
                    mUIFirstInit = false;
                    mFilterIdxArray[mFilterPos] = position;
                    setFilter();
                }

                final ImgFilterBase curFilter = mFilterArray[mFilterPos];
                if (curFilter != null) {
                    mBeautyGrindLayout.setVisibility(curFilter.isGrindRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyWhitenLayout.setVisibility(curFilter.isWhitenRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyRuddyLayout.setVisibility(curFilter.isRuddyRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                            new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress,
                                                              boolean fromUser) {
                                    if (!fromUser) {
                                        return;
                                    }
                                    float val = progress / 100.f;
                                    if (seekBar == mGrindSeekBar) {
                                        curFilter.setGrindRatio(val);
                                    } else if (seekBar == mWhitenSeekBar) {
                                        curFilter.setWhitenRatio(val);
                                    } else if (seekBar == mRuddySeekBar) {
                                        if (curFilter instanceof ImgBeautyProFilter) {
                                            val = progress / 50.f - 1.0f;
                                        }
                                        curFilter.setRuddyRatio(val);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            };
                    mGrindSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mWhitenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mRuddySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mGrindSeekBar.setProgress((int)(curFilter.getGrindRatio() * 100));
                    mWhitenSeekBar.setProgress((int)(curFilter.getWhitenRatio() * 100));
                    int ruddyVal = (int) (curFilter.getRuddyRatio() * 100);
                    if (curFilter instanceof ImgBeautyProFilter) {
                        ruddyVal = (int) (curFilter.getRuddyRatio() * 50 + 50);
                    }
                    mRuddySeekBar.setProgress(ruddyVal);
                } else {
                    mBeautyGrindLayout.setVisibility(View.GONE);
                    mBeautyWhitenLayout.setVisibility(View.GONE);
                    mBeautyRuddyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected void setFilter() {
        ImgFilterBase filter = chooseFilter(mFilterIdxArray[mFilterPos]);
        ImgFilterBase old = mFilterArray[mFilterPos];
        if (old != null) {
            mStreamer.getImgTexFilterMgt().replaceFilter(old, filter);
        } else {
            int idx_pre = -1;
            int idx_after = -1;
            for (int i = 0; i < mFilterIdxArray.length; i++) {
                if (mFilterArray[i] != null) {
                    if (i < mFilterPos) {
                        idx_pre = i;
                    } else if (i > mFilterPos) {
                        idx_after = i;
                        break;
                    }
                }
            }
            if (idx_pre != -1) {
                // filter in front
                mStreamer.getImgTexFilterMgt().addFilterAfter(mFilterArray[idx_pre], filter);
            } else if (idx_after != -1) {
                // filter in rear
                mStreamer.getImgTexFilterMgt().addFilterBefore(mFilterArray[idx_after], filter);
            } else {
                // no filter yet
                mStreamer.getImgTexFilterMgt().setFilter(filter);
            }
        }
        mFilterArray[mFilterPos] = filter;
    }

    protected ImgFilterBase chooseFilter(int idx) {
        ImgFilterBase filter;
        switch (idx) {
            case 1:
                filter = new ImgBeautySoftFilter(mStreamer.getGLRender());
                break;
            case 2:
                filter = new ImgBeautySkinWhitenFilter(mStreamer.getGLRender());
                break;
            case 3:
                filter = new ImgBeautyIllusionFilter(mStreamer.getGLRender());
                break;
            case 4:
                filter = new ImgBeautyDenoiseFilter(mStreamer.getGLRender());
                break;
            case 5:
                filter = new ImgBeautySmoothFilter(mStreamer.getGLRender(), mActivity);
                break;
            case 6:
                filter = new ImgBeautyProFilter(mStreamer.getGLRender(), mActivity);
                break;
            case 7:
                filter = new ImgBeautyProFilter(mStreamer.getGLRender(), mActivity, 2);
                break;
            case 8:
                filter = new ImgBeautyProFilter(mStreamer.getGLRender(), mActivity, 3);
                break;
            case 9:
                filter = new ImgBeautyProFilter(mStreamer.getGLRender(), mActivity, 4);
                break;
            case 10:
                filter = new DemoFilter(mStreamer.getGLRender());
                break;
            case 11:
                filter = new RGBABufDemoFilter(mStreamer.getGLRender());
                break;
            case 12:
                filter = new YUVI420BufDemoFilter(mStreamer.getGLRender());
                break;
            case 13:
                filter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                ((ImgBeautyToneCurveFilter) filter).setFromCurveFileInputStream(
                        mActivity.getResources().openRawResource(R.raw.tone_cuver_sample));
                break;
            case 14:
                filter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                ((ImgBeautyToneCurveFilter) filter).setFromCurveFileInputStream(
                        mActivity.getResources().openRawResource(R.raw.fugu));
                break;
            case 15:
                filter = new ImgBeautyToneCurveFilter(mStreamer.getGLRender());
                ((ImgBeautyToneCurveFilter) filter).setFromCurveFileInputStream(
                        mActivity.getResources().openRawResource(R.raw.jiaopian));
                break;
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
            case 27:
            case 28:
            case 29:
            case 30:
                filter = new ImgBeautySpecialEffectsFilter(mStreamer.getGLRender(),
                        mActivity, idx - 15);
                break;
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            case 38:
            case 39:
            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
                filter = new ImgBeautyStylizeFilter(mStreamer.getGLRender(),
                        mActivity, idx - 31);
                break;
            default:
                filter = null;
        }
        return filter;
    }
}
