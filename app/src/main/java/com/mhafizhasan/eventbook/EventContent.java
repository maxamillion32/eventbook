package com.mhafizhasan.eventbook;

import android.app.Activity;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mhafizhasan.eventbook.net.EventUploadCoverRequest;
import com.mhafizhasan.eventbook.net.EventUploadIconRequest;
import com.mhafizhasan.eventbook.net.model.AffiliationModel;
import com.mhafizhasan.eventbook.net.model.EventModel;
import com.mhafizhasan.eventbook.utils.CallChannel;
import com.mhafizhasan.eventbook.utils.ContentView;
import com.mhafizhasan.eventbook.utils.PictureRequest;
import com.mhafizhasan.eventbook.utils.Storage;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by Dermis on 12,March,2016.
 */
public class EventContent {


    public EventContent(EventModel event) {
        this.event = event;
    }


    public static class ViewHolder extends ContentView<EventContent> {

        @Bind(R.id.le_cover) ImageView coverView;
        @Bind(R.id.le_icon) ImageView iconView;
        @Bind(R.id.le_name) TextView nameView;
        @Bind(R.id.le_details) TextView detailsView;
        @Bind(R.id.le_distance) TextView distanceView;
        @Bind(R.id.le_editable) ImageView editableView;

        private final Activity activity;
        private final CallChannel channel;


        public ViewHolder(Activity activity, CallChannel channel) {
            super(R.layout.list_event);
            this.activity = activity;
            this.channel = channel;
        }

        @Override
        public ViewHolder instantiate() {
            return new ViewHolder(activity, channel);
        }

        @Override
        protected void bind(EventContent content) {
            nameView.setText(content.event.name);
            detailsView.setText(content.event.entry_maximum + "+ people");
            if(content.event.cover_url != null)
                Picasso.with(activity).load(content.event.cover_url.full).into(coverView);
            else
                coverView.setImageDrawable(null);
            if(content.event.icon_url != null)
                Picasso.with(activity).load(content.event.icon_url.full).into(iconView);
            else
                iconView.setImageDrawable(null);
            // Show editable if user is the owner of this event
            UserLoginDetails login = UserLoginDetails.from(activity);
            if(content.event.affiliation.status == AffiliationModel.AffiliationStatus.CHECKED_IN) {
                editableView.setVisibility(View.VISIBLE);
            } else {
                editableView.setVisibility(View.GONE);
            }

        }

        @OnClick(R.id.le_cover)
        void onClickCover() {
            if(getContent().event.affiliation.status == AffiliationModel.AffiliationStatus.CHECKED_IN) {
                // User can edit this event, select image for cover
                new PictureRequest(activity, channel, -1, -1, -1, -1) {
                    @Override
                    protected void onPictureSelected(Uri uri) {
                        coverView.setImageURI(uri);
                        UserLoginDetails login = UserLoginDetails.from(activity);
                        new EventUploadCoverRequest(
                                channel,
                                getView(),
                                login.token.access_token,
                                getContent().event.event_id,
                                new File(uri.getPath())
                        ).send();
                    }
                }.start();
            }
        }

        @OnClick(R.id.le_icon)
        void onClickIcon() {
            if(getContent().event.affiliation.status == AffiliationModel.AffiliationStatus.CHECKED_IN) {
                // User can edit this event, select image for cover
                new PictureRequest(activity, channel, 1, 1, -1, -1) {
                    @Override
                    protected void onPictureSelected(Uri uri) {
                        iconView.setImageURI(uri);
                        UserLoginDetails login = UserLoginDetails.from(activity);
                        new EventUploadIconRequest(
                                channel,
                                getView(),
                                login.token.access_token,
                                getContent().event.event_id,
                                new File(uri.getPath())
                        ).send();
                    }
                }.start();
            }
        }
    }

    // Contents for each row
    private final EventModel event;

    @Override
    public String toString() {
        return "event-" + event.event_id;
    }

    @Override
    public boolean equals(Object o) {
        return Storage.equals(this, o);
    }
}
