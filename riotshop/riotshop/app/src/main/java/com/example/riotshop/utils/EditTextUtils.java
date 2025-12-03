package com.example.riotshop.utils;

import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

public class EditTextUtils {
    
    /**
     * Cấu hình EditText để hỗ trợ tiếng Việt đúng cách
     * Đảm bảo composition text không bị mất khi gõ
     */
    public static void configureForVietnamese(EditText editText) {
        // Đảm bảo EditText không có filter nào can thiệp vào composition text
        editText.setImportantForAutofill(android.view.View.IMPORTANT_FOR_AUTOFILL_NO);
        
        // Đảm bảo IME options được set đúng
        int imeOptions = editText.getImeOptions();
        if (imeOptions == EditorInfo.IME_NULL) {
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }
    
    /**
     * Kiểm tra xem có composition text đang diễn ra không
     * Composition text là text đang được gõ (chưa commit) - ví dụ khi gõ "Huỳ" thì "ỳ" là composition
     */
    public static boolean hasCompositionText(Editable editable) {
        if (editable == null) {
            return false;
        }
        
        // Kiểm tra xem có composition span không
        // Composition span được đánh dấu bằng một object đặc biệt từ IME
        if (editable instanceof Spanned) {
            Spanned spanned = (Spanned) editable;
            // Kiểm tra xem có span nào có flag SPAN_COMPOSING không
            Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
            if (spans != null) {
                for (Object span : spans) {
                    int flags = spanned.getSpanFlags(span);
                    // SPAN_COMPOSING = 256 (0x100)
                    if ((flags & Spanned.SPAN_COMPOSING) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Kiểm tra xem có composition text đang diễn ra không (dùng cho CharSequence)
     */
    public static boolean hasCompositionText(CharSequence text) {
        if (text instanceof Spanned) {
            Spanned spanned = (Spanned) text;
            Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
            if (spans != null) {
                for (Object span : spans) {
                    int flags = spanned.getSpanFlags(span);
                    if ((flags & Spanned.SPAN_COMPOSING) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

