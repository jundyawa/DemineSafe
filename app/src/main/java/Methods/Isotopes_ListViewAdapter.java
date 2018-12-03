package Methods;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

import java.util.ArrayList;

import Classes.Isotope;

public class Isotopes_ListViewAdapter extends BaseAdapter {

    // Attributes
    private Context myContext_;

    private ArrayList<Isotope> myIsotopes_;

    private int itemSelected;

    // Constructor
    public Isotopes_ListViewAdapter(Context myContext, ArrayList<Isotope> myIsotopes) {

        this.myContext_ = myContext;

        this.myIsotopes_ = myIsotopes;

        this.itemSelected = -1;
    }

    @Override
    public int getCount() {
        if(myIsotopes_ == null){
            return 0;
        }
        return myIsotopes_.size();
    }

    @Override
    public Isotope getItem(int position) {
        return myIsotopes_.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder viewHolder;

        // When view is not null, we can reuse it directly, there is no need
        // to reinflate it. We only inflate a new View when the view supplied
        // by ListView is null.
        if (view == null) { // On create

            // Get the layout
            view = LayoutInflater.from(myContext_).inflate(R.layout.isotopes_list, null);

            // Creates a ViewHolder and store references to the children views
            // we want to bind data to.
            viewHolder = new ViewHolder();
            viewHolder.name_ = view.findViewById(R.id.name_items);
            viewHolder.energy_ = view.findViewById(R.id.energy_items);

            // Binds the data efficiently with the holder.
            view.setTag(viewHolder);

        } else {    // Populating the already existent

            // Get the ViewHolder back to get fast access to the children's
            viewHolder = (ViewHolder) view.getTag();
        }

        // Set the Name column value
        viewHolder.name_.setText(getItem(position).getName());

        String energyStr = String.valueOf(getItem(position).getEnergy()) + " keV";
        viewHolder.energy_.setText(energyStr);


        if(position == itemSelected){
            viewHolder.name_.setTextColor(Color.parseColor("#3e86a0"));
            viewHolder.energy_.setTextColor(Color.parseColor("#3e86a0"));
        }else{
            viewHolder.name_.setTextColor(Color.parseColor("#2e2c2a"));
            viewHolder.energy_.setTextColor(Color.parseColor("#2e2c2a"));
        }

        return view;
    }

    public int changeColor(int position){

        if(itemSelected == position){
            itemSelected = -1;
        }else{
            itemSelected = position;
        }
        notifyDataSetChanged();
        return itemSelected;
    }

    // The ViewHolder Class
    static class ViewHolder {
        TextView name_;
        TextView energy_;
    }
}
