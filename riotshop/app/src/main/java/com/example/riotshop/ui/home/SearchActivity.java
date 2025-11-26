package com.example.riotshop.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.adapters.AccountAdapter;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.Account;
import com.example.riotshop.ui.detail.DetailActivity;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity implements AccountAdapter.OnAccountListener {

    private EditText etSearchQuery;
    private ImageButton btnSearch;
    private RecyclerView rvSearchResults;
    private TextView tvNoResults;

    private AccountAdapter accountAdapter;
    private List<Account> accountList;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        dataSource = new DataSource(this);
        dataSource.open();

        etSearchQuery = findViewById(R.id.et_search_query);
        btnSearch = findViewById(R.id.btn_search);
        rvSearchResults = findViewById(R.id.rv_search_results);
        tvNoResults = findViewById(R.id.tv_no_search_results);

        accountList = new ArrayList<>();
        accountAdapter = new AccountAdapter(this, accountList, this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(accountAdapter);

        btnSearch.setOnClickListener(v -> performSearch());

        etSearchQuery.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = etSearchQuery.getText().toString().trim();
        if (query.isEmpty()) {
            return;
        }
        
        accountList.clear();
        accountList.addAll(dataSource.searchAccountsByName(query));
        accountAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (accountList.isEmpty()) {
            tvNoResults.setVisibility(View.VISIBLE);
            rvSearchResults.setVisibility(View.GONE);
        } else {
            tvNoResults.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAccountClick(Account account) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ACCOUNT_ID", account.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteClick(Account account) {
        // You can add favorite logic here as well if needed
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
