package com.aula.twittercourse;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;

public class BarChartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_bar_chart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BarChart barChart = view.findViewById(R.id.barChart);

        // Crie e configure os dados do gráfico aqui
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1, 10f));
        entries.add(new BarEntry(2, 20f));
        // Adicione mais entradas conforme necessário

        BarDataSet dataSet = new BarDataSet(entries, "Label");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate(); // Atualize o gráfico
    }

}
