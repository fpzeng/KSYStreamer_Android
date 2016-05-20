package com.ksy.recordlib.demo;

import com.ksy.recordlib.service.hardware.filter.KSYImageGroupFilter;
import com.ksy.recordlib.service.hardware.ksyfilter.KSYImageFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hansentian on 5/5/16.
 */
public class GroupFilterDemo extends KSYImageGroupFilter {
    public GroupFilterDemo() {
        super(initFilters());
    }

    private static List<KSYImageFilter> initFilters() {
        List<KSYImageFilter> filters = new ArrayList<>();
        filters.add(new DEMOFILTER2());
        filters.add(new DEMOFILTER3());
        filters.add(new DEMOFILTER4());

        return filters;
    }
}
