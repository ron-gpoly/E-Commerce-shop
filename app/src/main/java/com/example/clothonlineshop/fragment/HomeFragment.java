package com.example.clothonlineshop.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.clothonlineshop.view.activity.ProductDetailActivity;
import com.example.clothonlineshop.adapter.CategoryAdapter;
import com.example.clothonlineshop.adapter.ProductAdapter;
import com.example.clothonlineshop.databinding.FragmentHomeBinding;
import com.example.clothonlineshop.model.CategoryModel;
import com.example.clothonlineshop.model.IGetAllProduct;
import com.example.clothonlineshop.model.Product;
import com.example.clothonlineshop.model.ProductResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Retrofit retrofit;
    private IGetAllProduct api;

    private int skip = 0;
    private final int limit = 20;
    private boolean isLoading = false;
    private ProductAdapter productAdapter;
    private final List<Product> productList = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        retrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(IGetAllProduct.class);

        setupCategoryRecyclerView();
        setupProductRecyclerView();
        
        // Initial fetch
        fetchProducts();

        // Pagination for NestedScrollView
        binding.getRoot().setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY == v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight()) {
                if (!isLoading) {
                    fetchProducts();
                }
            }
        });
    }

    private void fetchProducts() {
        isLoading = true;
        api.getAllProducts(limit, skip).enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                isLoading = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();
                    if (!products.isEmpty()) {
                        productAdapter.addProducts(products);
                        skip += products.size();
                    }
                } else {
                    Log.e("HomeFragment", "Response failed");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                isLoading = false;
                Log.e("HomeFragment", "Error: " + t.getMessage());
            }
        });
    }

    private void setupCategoryRecyclerView() {
        List<CategoryModel> categories = new ArrayList<>();
        categories.add(new CategoryModel("Shirt"));
        categories.add(new CategoryModel("Pant"));
        categories.add(new CategoryModel("Shoe"));
        categories.add(new CategoryModel("Watch"));
        categories.add(new CategoryModel("Bag"));
        categories.add(new CategoryModel("Accessory"));
        categories.add(new CategoryModel("Jacket"));

        CategoryAdapter adapter = new CategoryAdapter(categories);
        binding.rvCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvCategory.setAdapter(adapter);
    }

    private void setupProductRecyclerView() {
        productAdapter = new ProductAdapter(productList, product -> {
            Intent intent = new Intent(getContext(), ProductDetailActivity.class);
            intent.putExtra("PRODUCT_ID", product.getId());
            startActivity(intent);
        });
        binding.rvProductDetail.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvProductDetail.setNestedScrollingEnabled(false);
        binding.rvProductDetail.setAdapter(productAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
