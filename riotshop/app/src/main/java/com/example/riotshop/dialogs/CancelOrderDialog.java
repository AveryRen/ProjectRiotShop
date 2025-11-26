package com.example.riotshop.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.riotshop.R;

public class CancelOrderDialog extends DialogFragment {

    private EditText etCancelReason;
    private CancelOrderListener listener;

    public interface CancelOrderListener {
        void onOrderCancelled(String reason);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Lấy listener từ Activity gọi nó
            listener = (CancelOrderListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement CancelOrderListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_cancel_order, null);

        etCancelReason = view.findViewById(R.id.et_cancel_reason);

        builder.setView(view)
               .setTitle("Hủy đơn hàng")
               .setPositiveButton("Xác nhận hủy", (dialog, id) -> {
                   String reason = etCancelReason.getText().toString().trim();
                   listener.onOrderCancelled(reason);
               })
               .setNegativeButton("Không", (dialog, id) -> {
                   CancelOrderDialog.this.getDialog().cancel();
               });

        return builder.create();
    }
}
