package Methods;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

import java.util.ArrayList;

import Classes.Map;

/**
 * Created by jean-romain on 22/03/18.
 */

public class Maps_ListViewAdapter extends BaseAdapter implements Filterable {


    // Attributes
    private Context myContext_;

    private ArrayList<Map> myMaps_;
    private ArrayList<Map> myFilteredMaps_;

    private ItemFilter mFilter = new ItemFilter();

    // Constructor
    public Maps_ListViewAdapter(Context myContext, ArrayList<Map> myMaps) {

        this.myContext_ = myContext;

        this.myMaps_ = myMaps;
        this.myFilteredMaps_ = myMaps;

    }

    // Default Methods
    @Override
    public int getCount() {
        return myFilteredMaps_.size();
    }

    @Override
    public Map getItem(int position) {
        return myFilteredMaps_.get(position);
    }

    @Override
    public long getItemId(int position) {
        return myFilteredMaps_.get(position).getLocalID();
    }

    public ArrayList<Map> getFilteredMaps() {
        return myFilteredMaps_;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;

        // When view is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the view supplied
        // by ListView is null.
        if (view == null) { // On create

            // Get the layout
            view = LayoutInflater.from(myContext_).inflate(R.layout.maps_list, null);

            // Creates a ViewHolder and store references to the children views
            // we want to bind data to.
            viewHolder = new ViewHolder();
            viewHolder.name_ = view.findViewById(R.id.name_items);
            viewHolder.value_ = view.findViewById(R.id.progress_items);

            // Binds the data efficiently with the holder.
            view.setTag(viewHolder);

        } else {    // Populating the already existent

            // Get the ViewHolder back to get fast access to the children's
            viewHolder = (ViewHolder) view.getTag();
        }


        // Set the Name column value
        viewHolder.name_.setText(getItem(position).getMapName());

        String progressSTR = getItem(position).getProgress() + " %";
        viewHolder.value_.setText(progressSTR);

        return view;
    }

    // The ViewHolder Class
    static class ViewHolder {
        TextView value_;
        TextView name_;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            // Object containing the filtered array
            FilterResults results = new FilterResults();

            // We format the input string constraints to be all lower case
            String filterString = constraint.toString().toLowerCase();

            // The new array containing only the filtered farmers
            final ArrayList<Map> filteredFarmerArray = new ArrayList<>();

            // Attributes checked
            Map my_map;
            String my_map_name;

            // We scan the full array of farmers and filter the valid ones
            for (int i = 0; i < myMaps_.size(); i++) {

                my_map = myMaps_.get(i);
                my_map_name = my_map.getMapName();

                // If input is numeric
                if (my_map_name.toLowerCase().contains(filterString)) {
                    filteredFarmerArray.add(my_map);
                }
            }

            // We inject the filtered farmer array to the returned object
            results.values = filteredFarmerArray;
            results.count = filteredFarmerArray.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            myFilteredMaps_ = (ArrayList<Map>) results.values;
            notifyDataSetChanged();
        }

    }
}