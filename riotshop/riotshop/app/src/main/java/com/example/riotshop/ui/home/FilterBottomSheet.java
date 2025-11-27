package com.example.riotshop.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.riotshop.R;
import com.example.riotshop.adapters.CategoryAdapter;
import com.example.riotshop.models.Category;
import com.example.riotshop.models.GameType;
import com.example.riotshop.api.ApiService;
import com.example.riotshop.api.RetrofitClient;
import com.example.riotshop.models.ApiResponse;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    public interface FilterListener {
        void onFilterApplied(Integer gameId, Boolean isFeatured, String priceRange, String sortBy);
        void onFilterReset();
    }

    private FilterListener filterListener;
    private CheckBox cbFeatured;
    private RadioGroup rgPriceFilter, rgSort;
    private RadioButton rbPriceAll, rbPriceLow, rbPriceMedium, rbPriceHigh;
    private RadioButton rbSortDefault, rbSortPriceLow, rbSortPriceHigh, rbSortName;
    private Button btnApply, btnReset;
    private RecyclerView rvFilterGames;
    private CategoryAdapter gameAdapter;
    private List<Category> gameList;
    private Integer selectedGameId = null;

    public void setFilterListener(FilterListener listener) {
        this.filterListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        cbFeatured = view.findViewById(R.id.cb_featured);
        rgPriceFilter = view.findViewById(R.id.rg_price_filter);
        rgSort = view.findViewById(R.id.rg_sort);
        rbPriceAll = view.findViewById(R.id.rb_price_all);
        rbPriceLow = view.findViewById(R.id.rb_price_low);
        rbPriceMedium = view.findViewById(R.id.rb_price_medium);
        rbPriceHigh = view.findViewById(R.id.rb_price_high);
        rbSortDefault = view.findViewById(R.id.rb_sort_default);
        rbSortPriceLow = view.findViewById(R.id.rb_sort_price_low);
        rbSortPriceHigh = view.findViewById(R.id.rb_sort_price_high);
        rbSortName = view.findViewById(R.id.rb_sort_name);
        btnApply = view.findViewById(R.id.btn_apply_filter);
        btnReset = view.findViewById(R.id.btn_reset_filter);
        rvFilterGames = view.findViewById(R.id.rv_filter_games);

        gameList = new ArrayList<>();
        setupGameFilter();
        loadGames();

        btnApply.setOnClickListener(v -> applyFilter());
        btnReset.setOnClickListener(v -> resetFilter());

        return view;
    }

    private void setupGameFilter() {
        gameList.add(new Category("0", "Tất cả", R.drawable.ic_category_default));
        gameAdapter = new CategoryAdapter(getContext(), gameList);
        gameAdapter.setOnCategoryClickListener(category -> {
            if (category.getId().equals("0")) {
                selectedGameId = null;
            } else {
                try {
                    selectedGameId = Integer.parseInt(category.getId());
                } catch (NumberFormatException e) {
                    selectedGameId = null;
                }
            }
            // Update selected position in adapter
            gameAdapter.setSelectedCategoryById(category.getId());
        });
        rvFilterGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvFilterGames.setAdapter(gameAdapter);
        
        // Set initial selected position to "Tất cả"
        gameAdapter.setSelectedPosition(0);
    }

    private void loadGames() {
        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<ApiResponse<List<GameType>>> call = apiService.getGames();
        
        call.enqueue(new Callback<ApiResponse<List<GameType>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<GameType>>> call, Response<ApiResponse<List<GameType>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<GameType> games = response.body().getData();
                    if (games != null) {
                        for (GameType gameType : games) {
                            gameList.add(new Category(
                                    String.valueOf(gameType.getGameId()),
                                    gameType.getName(),
                                    R.drawable.ic_category_default
                            ));
                        }
                        gameAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<GameType>>> call, Throwable t) {
                // Silent fail
            }
        });
    }

    private void applyFilter() {
        if (filterListener == null) {
            dismiss();
            return;
        }

        Boolean isFeatured = cbFeatured.isChecked() ? true : null;
        
        String priceRange = null;
        int selectedPriceId = rgPriceFilter.getCheckedRadioButtonId();
        if (selectedPriceId == R.id.rb_price_low) {
            priceRange = "low";
        } else if (selectedPriceId == R.id.rb_price_medium) {
            priceRange = "medium";
        } else if (selectedPriceId == R.id.rb_price_high) {
            priceRange = "high";
        }

        String sortBy = null;
        int selectedSortId = rgSort.getCheckedRadioButtonId();
        if (selectedSortId == R.id.rb_sort_price_low) {
            sortBy = "price_asc";
        } else if (selectedSortId == R.id.rb_sort_price_high) {
            sortBy = "price_desc";
        } else if (selectedSortId == R.id.rb_sort_name) {
            sortBy = "name_asc";
        }

        filterListener.onFilterApplied(selectedGameId, isFeatured, priceRange, sortBy);
        dismiss();
    }

    private void resetFilter() {
        cbFeatured.setChecked(false);
        rbPriceAll.setChecked(true);
        rbSortDefault.setChecked(true);
        selectedGameId = null;
        gameAdapter.setSelectedPosition(0); // Reset to "Tất cả"
        
        if (filterListener != null) {
            filterListener.onFilterReset();
        }
        dismiss();
    }
}
