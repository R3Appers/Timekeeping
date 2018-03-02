package com.rrreyes.prototype.timekeeping.Adapters;

import android.content.Context;
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
    private Context context;

    public MainLogAdapter(Context context, List<DTRDataSorted> data) {
        this.context = context;
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
            holder.TV_TimeIn.setTextColor(context.getResources().getColor(R.color.tabTextColorB));
        } else {
            holder.TV_TimeIn.setText("**:**");
        }
        if(data.getTimeOut() != null) {
            if(GetHour(data.getTimeOut()) < 6) {
                holder.TVLbl_TimeOut.setTextColor(context.getResources().getColor(R.color.tabTextColorA));
            }
            holder.TV_TimeOut.setText(data.getTimeOut());
            holder.TV_TimeOut.setTextColor(context.getResources().getColor(R.color.tabTextColorA));
        } else {
            holder.TV_TimeOut.setText("**:**");
        }
        if(data.getBreakOut() != null) {
            holder.TV_BreakOut.setText(data.getBreakOut());
            holder.TV_BreakOut.setTextColor(context.getResources().getColor(R.color.tabTextColorA));
        } else {
            holder.TV_BreakOut.setText("**:**");
        }
        if(data.getBreakIn() != null) {
            holder.TV_BreakIn.setText(data.getBreakIn());
            holder.TV_BreakIn.setTextColor(context.getResources().getColor(R.color.tabTextColorB));
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
        TextView TVLbl_TimeOut;

        public MainLogViewHolder(View itemView) {
            super(itemView);

            TV_Date = itemView.findViewById(R.id.TV_Date);
            TV_EmpIDN = itemView.findViewById(R.id.TV_EmpIDN);
            TV_TimeIn = itemView.findViewById(R.id.TV_TimeIn);
            TV_TimeOut = itemView.findViewById(R.id.TV_TimeOut);
            TV_BreakOut = itemView.findViewById(R.id.TV_BreakOut);
            TV_BreakIn = itemView.findViewById(R.id.TV_BreakIn);
            TVLbl_TimeOut = itemView.findViewById(R.id.TVLbl_TimeOut);
        }
    }

    int GetHour(String time) {
        return Integer.parseInt(time.substring(0, 2));
    }
}
