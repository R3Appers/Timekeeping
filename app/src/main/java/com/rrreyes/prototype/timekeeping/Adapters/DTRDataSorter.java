package com.rrreyes.prototype.timekeeping.Adapters;

import com.rrreyes.prototype.timekeeping.Models.DTRData;
import com.rrreyes.prototype.timekeeping.Models.DTRDataSorted;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by R. R. Reyes on 2/12/2018.
 */

public class DTRDataSorter {

    public List<DTRDataSorted> SortData(List<DTRData> data) {
        List<DTRDataSorted> sortedData = new ArrayList<>();
        for(int i = 0; i < data.size(); i++) {
            DTRData dtrTemp = data.get(i);
            DTRDataSorted dataTemp = new DTRDataSorted();
            if(sortedData.size() != 0) {
                int ctr = 0;
                for(int j = 0; j < sortedData.size(); j++) {
                    if(sortedData.get(j).getDate().equals(dtrTemp.getDate())
                            && sortedData.get(j).getBarcode().equals(dtrTemp.getBarcode())) {
                        ctr++;
                        dataTemp = AddData(sortedData.get(j), dtrTemp);
                        sortedData.remove(j);
                        sortedData.add(dataTemp);
                    }
                }
                if(ctr == 0) {
                    dataTemp.setDate(dtrTemp.getDate());
                    dataTemp.setBarcode(dtrTemp.getBarcode());
                    dataTemp = AddData(dataTemp, dtrTemp);
                    sortedData.add(dataTemp);
                }
            } else {
                dataTemp.setDate(dtrTemp.getDate());
                dataTemp.setBarcode(dtrTemp.getBarcode());
                dataTemp = AddData(dataTemp, dtrTemp);
                sortedData.add(dataTemp);
            }
        }
        return sortedData;
    }

    private DTRDataSorted AddData(DTRDataSorted dtrDataSorted, DTRData dtrData) {
        DTRDataSorted sortedData = dtrDataSorted;
        switch (dtrData.getType()) {
            case "1" :
                if(sortedData.getTimeIn() == null) {
                    sortedData.setTimeIn(dtrData.getTime());
                }
                break;
            case "2" :
                if(sortedData.getBreakOut() == null) {
                    sortedData.setBreakOut(dtrData.getTime());
                }
                break;
            case "3" :
                sortedData.setBreakIn(dtrData.getTime());
                break;
            case "4" :
                sortedData.setTimeOut(dtrData.getTime());
                break;
        }
        return sortedData;
    }
}
