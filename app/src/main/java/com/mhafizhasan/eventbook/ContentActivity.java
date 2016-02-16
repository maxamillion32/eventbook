package com.mhafizhasan.eventbook;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.mhafizhasan.eventbook.utils.DynamicViewPagerAdapter;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ContentActivity extends AppCompatActivity {

    // Views
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.tabLayout) TabLayout tabLayout;
    @Bind(R.id.viewPager) ViewPager viewPager;

    // View Pager
    DynamicViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and bind
        setContentView(R.layout.activity_content);
        ButterKnife.bind(this);

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Setup view pager
        viewPagerAdapter = new DynamicViewPagerAdapter(getSupportFragmentManager());

        {
            EventsMapFragment.Arguments builder = new EventsMapFragment.Arguments();
            viewPagerAdapter.put(builder, EventsMapFragment.class, "Map");
        }

        {
            EventsFragment.Arguments builder = new EventsFragment.Arguments();
            viewPagerAdapter.put(builder, EventsFragment.class, "Events");
        }

        {
            AwardsFragment.Arguments builder = new AwardsFragment.Arguments();
            viewPagerAdapter.put(builder, AwardsFragment.class, "Award");
        }

        {
            PostsFragment.Arguments builder = new PostsFragment.Arguments();
            viewPagerAdapter.put(builder, PostsFragment.class, "Me");
        }

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerAdapter);
        viewPager.setPageTransformer(false, new DepthPageTransformer());

        // Link TabLayout with viewPager
        tabLayout.setupWithViewPager(viewPager);


    }

}
