package com.aula.twittercourse;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aula.twittercourse.model.Produto;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Produto> productList;

    public ProductAdapter(List<Produto> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Produto product = productList.get(position);
        holder.textViewProductId.setText("Código: " + product.getId() + " ");
        holder.textViewProductName.setText(product.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_description", product.getDescription());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_quantity", product.getQuantity());
            holder.itemView.getContext().startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName;
        TextView textViewProductId; // Adicione esta linha

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductId = itemView.findViewById(R.id.textViewProductId); // Inicialize aqui
        }
    }
}
