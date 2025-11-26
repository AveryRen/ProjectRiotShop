package com.example.riotshop.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.slider.RangeSlider;
import com.example.riotshop.R;
import java.util.List;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private ChipGroup chipGroupCategory;
    private RangeSlider rangeSliderPrice;
    private Button btnReset, btnApply;

    private FilterListener mListener;
    private String selectedCategory = "Tất cả";
    private float minPrice = 0;
    private float maxPrice = 10000000;

    public interface FilterListener {
        void onFilterApplied(String category, float minPrice, float maxPrice);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (FilterListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FilterListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_filter, container, false);

        chipGroupCategory = view.findViewById(R.id.chipgroup_category_filter);
        rangeSliderPrice = view.findViewById(R.id.slider_price_range);
        btnReset = view.findViewById(R.id.btn_reset_filter);
        btnApply = view.findViewById(R.id.btn_apply_filter);

        setupListeners();

        return view;
    }

    private void setupListeners() {
        chipGroupCategory.setOnCheckedChangeListener((group, checkedId) -> {
            Chip chip = group.findViewById(checkedId);
            if (chip != null) {
                selectedCategory = chip.getText().toString();
            }
        });

        rangeSliderPrice.addOnChangeListener((slider, value, fromUser) -> {
            List<Float> values = slider.getValues();
            minPrice = values.get(0);
            maxPrice = values.get(1);
        });

        btnApply.setOnClickListener(v -> {
            mListener.onFilterApplied(selectedCategory, minPrice, maxPrice);
            dismiss();
        });

        btnReset.setOnClickListener(v -> {
            // Reset UI elements and apply default filter
            chipGroupCategory.check(R.id.chip_all); // Assuming you add this id
            rangeSliderPrice.setValues(0f, 10000000f);
            mListener.onFilterApplied("Tất cả", 0, 10000000);
            dismiss();
        });
    }
}
