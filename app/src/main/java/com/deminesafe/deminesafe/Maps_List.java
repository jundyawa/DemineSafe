package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

import Classes.Map;
import Methods.Maps_ListViewAdapter;
import REST_API.FirestoreRequest;

public class Maps_List extends Fragment {

    // Attributes
    private Main_Activity myParentActivity_;

    private ArrayList<Map> myMaps;

    private Maps_ListViewAdapter myListViewAdapter;
    private ListView myListView;
    private EditText inputsearch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_maps_list, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Main_Activity) getActivity();

        // UI Init
        myListView = rootView.findViewById(R.id.list_layout);
        inputsearch = rootView.findViewById(R.id.inputSearch);
        FloatingActionButton add_floatingbutton = rootView.findViewById(R.id.add_button);
        inputsearch.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        // Clear text
        inputsearch.setText("");

        // Update UI
        updateAdapter();

        // Set on click listeners
        add_floatingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myParentActivity_.changeFragment(myParentActivity_.getAddMapFragment());
            }
        });

        myListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                Map mapClicked = myListViewAdapter.getFilteredMaps().get(position);

                getActivity().finish();
                Intent i = new Intent(getActivity(),Map_Operation_Activity.class);
                i.putExtra("map_id",mapClicked.getCloudID());
                i.putExtra("map_name",mapClicked.getMapName());
                startActivity(i);
            }
        });

        // Set the input search functionality
        inputsearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                String input_constraint = cs.toString().trim();
                myListViewAdapter.getFilter().filter(input_constraint);

            }
            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
            }
        });

        // Sync Maps
        sendMaps();

        return rootView;
    }

    private void getMaps(){

        FirestoreRequest myRequest = new FirestoreRequest(getActivity(), new FirestoreRequest.RESPONSE() {
            @Override
            public void RESPONSE(int NbrOfMaps) {
                if(NbrOfMaps > 0){

                    updateAdapter();
                }
                myParentActivity_.unfreezeUI();
            }
        });
        myRequest.getMaps(myMaps);
    }

    private void sendMaps(){

        myParentActivity_.freezeUI();
        FirestoreRequest myRequest = new FirestoreRequest(getActivity(), new FirestoreRequest.RESPONSE() {
            @Override
            public void RESPONSE(int NbrOfMaps) {


                getMaps();
            }
        });
        myRequest.sendMaps(myMaps);
    }

    private void updateAdapter(){


        // Import Maps from database
        myMaps = myParentActivity_.getMyDBAdapter().getAllMaps();

        // Build custom adapter
        myListViewAdapter = new Maps_ListViewAdapter(getActivity(), myMaps);

        // Set adapter to the listview
        myListView.setAdapter(myListViewAdapter);
    }

}
