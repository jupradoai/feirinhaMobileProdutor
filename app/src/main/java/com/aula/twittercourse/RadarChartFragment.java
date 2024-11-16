package com.aula.twittercourse;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import java.util.ArrayList;
import java.util.List;

public class RadarChartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_radar_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RadarChart radarChart = view.findViewById(R.id.radarChart);

        List<RadarEntry> entries = new ArrayList<>();
        entries.add(new RadarEntry(5f));
        entries.add(new RadarEntry(10f));
        entries.add(new RadarEntry(15f));
        entries.add(new RadarEntry(20f));
        entries.add(new RadarEntry(25f));

        RadarDataSet dataSet = new RadarDataSet(entries, "Values");
        RadarData radarData = new RadarData(dataSet);
        radarChart.setData(radarData);
        radarChart.invalidate();
    }
}
