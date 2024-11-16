package com.aula.twittercourse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.twittercourse.model.Produto;

import java.util.List;

public class ProductAdapterHome extends RecyclerView.Adapter<ProductAdapterHome.ViewHolder> {
    private List<Produto> productList;


    public ProductAdapterHome(List<Produto> productList) {
        this.productList = productList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView productName;
        public TextView productPrice;
        public TextView productQuantity;

        public ViewHolder(View view) {
            super(view);
            productName = view.findViewById(R.id.textViewProductName);
            productPrice = view.findViewById(R.id.textViewProductPrice);
            productQuantity = view.findViewById(R.id.textViewProductQuantity);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Produto product = productList.get(position);
        holder.productName.setText(product.getName());
        holder.productPrice.setText("R$ " + String.format("%.2f", product.getPrice()));
        holder.productQuantity.setText("Quantidade: " + product.getQuantity());
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }
}
