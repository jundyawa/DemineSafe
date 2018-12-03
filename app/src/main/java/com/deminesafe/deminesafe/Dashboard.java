package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import Classes.TimeStamp;
import REST_API.OSMRequest;

public class Dashboard extends Fragment {

    // Attributes
    private Main_Activity myParentActivity_;

    private TextView date_TEXTVIEW;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Main_Activity) getActivity();


        // Fetch UI objects
        date_TEXTVIEW = rootView.findViewById(R.id.date_textview);

        date_TEXTVIEW.setText(TimeStamp.getTimeStamp_String_Clean());

        TextView hazards_TEXTVIEW = rootView.findViewById(R.id.hazards_textview);

        String nbrOfhazards = String.valueOf(myParentActivity_.getNbrOfHazards());
        hazards_TEXTVIEW.setText(nbrOfhazards);

        return rootView;
    }
}
