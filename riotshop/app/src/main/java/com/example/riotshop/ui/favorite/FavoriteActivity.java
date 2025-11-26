package com.example.riotshop.ui.favorite;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.riotshop.R;
import com.example.riotshop.adapters.FavoriteAdapter;
import com.example.riotshop.data.DataSource;
import com.example.riotshop.models.Account;
import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements FavoriteAdapter.OnRemoveFavoriteListener {

    private RecyclerView rvFavorites;
    private FavoriteAdapter favoriteAdapter;
    private List<Account> favoriteAccountList;
    private TextView tvNoFavorites;

    private DataSource dataSource;
    private String currentUserId = "1"; // Placeholder, you need a real user session

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        dataSource = new DataSource(this);
        dataSource.open();

        rvFavorites = findViewById(R.id.rv_favorites);
        tvNoFavorites = findViewById(R.id.tv_no_favorites);

        favoriteAccountList = new ArrayList<>();
        favoriteAdapter = new FavoriteAdapter(this, favoriteAccountList, this);
        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        rvFavorites.setAdapter(favoriteAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteAccounts();
    }

    private void loadFavoriteAccounts() {
        favoriteAccountList.clear();
        favoriteAccountList.addAll(dataSource.getFavoriteAccounts(currentUserId));
        favoriteAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (favoriteAccountList.isEmpty()) {
            tvNoFavorites.setVisibility(View.VISIBLE);
            rvFavorites.setVisibility(View.GONE);
        } else {
            tvNoFavorites.setVisibility(View.GONE);
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRemoveFavorite(Account account) {
        dataSource.removeFavorite(currentUserId, account.getId());
        Toast.makeText(this, "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show();
        loadFavoriteAccounts(); // Refresh the list
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataSource.close();
    }
}
