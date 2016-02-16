package com.mhafizhasan.eventbook.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Azmi on 10/2/2016.
 */
public class DynamicViewPagerAdapter extends FragmentStatePagerAdapter implements ViewPager.OnPageChangeListener {

    public static class Page {
        final BaseArguments builder;
        final Class<? extends DynamicFragment> type;
        final String title;

        // Current
        DynamicFragment fragment = null;

        public BaseArguments getBuilder() {
            return builder;
        }

        public Class<? extends DynamicFragment> getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public Page(BaseArguments builder, Class<? extends DynamicFragment> type, String title) {
            this.builder = builder;
            this.type = type;
            this.title = title;
        }
    }

    final ArrayList<Page> pages = new ArrayList<>();
    final HashMap<Object, Page> lookup = new HashMap<>();
    DynamicFragment currentFragment = null;


    public DynamicViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public Page get(int position) {
        return pages.get(position);
    }

    public void put(Page page) {
        put(getCount(), page);
    }

    public void put(BaseArguments builder, Class<? extends DynamicFragment> type, String title) {
        put(new Page(builder, type, title));
    }

    public void put(int position, Page page) {
        pages.add(position, page);
        notifyDataSetChanged();
    }

    public void put(int position, BaseArguments builder, Class<? extends DynamicFragment> type, String title) {
        put(position, new Page(builder, type, title));
    }

    public Page remove(int position) {
        Page page = pages.remove(position);
        notifyDataSetChanged();
        return page;
    }


    @Override
    public Fragment getItem(int position) {
        Page page = pages.get(position);
        if (page.fragment != null)
            return page.fragment;           // already created
        page.fragment = page.builder.createSupportFragment(page.type);
        return page.fragment;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object item = super.instantiateItem(container, position);

        // Update item lookup
        lookup.put(item, pages.get(position));

        return item;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);

        // Update lookup
        Page page = lookup.remove(object);
        page.fragment = null;
    }

    @Override
    public int getItemPosition(Object object) {
        Page page = lookup.get(object);
        if (page == null)
            return POSITION_NONE;
        int index = pages.indexOf(page);
        if (index == -1)
            return POSITION_NONE;
        return index;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pages.get(position).title;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Page page = pages.get(position + (int) Math.signum(positionOffset));
        if (page.fragment != null && !page.fragment.isVisible) {
            page.fragment.isVisible = true;
            page.fragment.onResumeVisible();
        }
        page = pages.get(position);
        if (page.fragment != null && page.fragment.isSelected) {
            page.fragment.isSelected = false;
            page.fragment.onDeselected();
        }
    }

    @Override
    public void onPageSelected(int position) {
        currentFragment = null;
        // Update all fragment visibility
        for (int c = 0; c < pages.size(); c++) {
            Page page = pages.get(position);
            if (page.fragment == null)
                continue;
            if (c == position) {
                if (!page.fragment.isVisible) {
                    page.fragment.isVisible = true;
                    page.fragment.onResumeVisible();
                }
                if (!page.fragment.isSelected) {
                    page.fragment.isSelected = true;
                    page.fragment.onSelected();
                }
                currentFragment = page.fragment;
            } else {
                if (page.fragment.isSelected) {
                    page.fragment.isSelected = false;
                    page.fragment.onDeselected();
                }
                if (page.fragment.isVisible) {
                    page.fragment.isVisible = false;
                    page.fragment.onPauseVisible();
                }
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        // nothing
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    public boolean onBackPressed() {
        if (currentFragment == null)
            return false;
        return currentFragment.onBackPressed();

    }
}
