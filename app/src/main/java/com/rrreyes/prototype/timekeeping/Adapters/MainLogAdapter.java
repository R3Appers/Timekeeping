package com.rrreyes.prototype.timekeeping.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rrreyes.prototype.timekeeping.Models.DTRDataSorted;
import com.rrreyes.prototype.timekeeping.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by R. R. Reyes on 2/12/2018.
 */

public class MainLogAdapter extends RecyclerView.Adapter<MainLogAdapter.MainLogViewHolder> {

    private List<DTRDataSorted> dtrDataList = new ArrayList<>();

    public MainLogAdapter(List<DTRDataSorted> data) {
        this.dtrDataList = data;
    }

    @Override
    public MainLogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_main_log, parent, false);
        return new MainLogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainLogViewHolder holder, int position) {
        DTRDataSorted data = dtrDataList.get(position);
        holder.TV_Date.setText(data.getDate());
        holder.TV_EmpIDN.setText(data.getBarcode());
        if(data.getTimeIn() != null) {
            holder.TV_TimeIn.setText(data.getTimeIn());
        } else {
            holder.TV_TimeIn.setText("**:**");
        }
        if(data.getTimeOut() != null) {
            holder.TV_TimeOut.setText(data.getTimeOut());
        } else {
            holder.TV_TimeOut.setText("**:**");
        }
        if(data.getBreakOut() != null) {
            holder.TV_BreakOut.setText(data.getBreakOut());
        } else {
            holder.TV_BreakOut.setText("**:**");
        }
        if(data.getBreakOut() != null) {
            holder.TV_BreakIn.setText(data.getBreakIn());
        } else {
            holder.TV_BreakIn.setText("**:**");
        }
    }

    @Override
    public int getItemCount() {
        return dtrDataList.size();
    }

    public class MainLogViewHolder extends RecyclerView.ViewHolder {

        TextView TV_Date, TV_EmpIDN;
        TextView TV_TimeIn, TV_TimeOut;
        TextView TV_BreakOut, TV_BreakIn;

        public MainLogViewHolder(View itemView) {
            super(itemView);

            TV_Date = itemView.findViewById(R.id.TV_Date);
            TV_EmpIDN = itemView.findViewById(R.id.TV_EmpIDN);
            TV_TimeIn = itemView.findViewById(R.id.TV_TimeIn);
            TV_TimeOut = itemView.findViewById(R.id.TV_TimeOut);
            TV_BreakOut = itemView.findViewById(R.id.TV_BreakOut);
            TV_BreakIn = itemView.findViewById(R.id.TV_BreakIn);
        }
    }
}
