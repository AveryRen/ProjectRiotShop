package com.example.riotshop.widgets;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;

import androidx.appcompat.widget.AppCompatEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom EditText được tối ưu để hỗ trợ gõ tiếng Việt
 * Đảm bảo composition text không bị mất khi gõ, đặc biệt khi gõ nhanh
 */
public class VietnameseEditText extends AppCompatEditText {

    private Handler handler = new Handler(Looper.getMainLooper());
    private List<OnCompositionFinishedListener> compositionFinishedListeners = new ArrayList<>();
    private boolean isComposing = false;

    public interface OnCompositionFinishedListener {
        void onCompositionFinished();
    }

    public VietnameseEditText(Context context) {
        super(context);
        init();
    }

    public VietnameseEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VietnameseEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Đảm bảo không có filter nào can thiệp vào composition text
        setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO);
        
        // Đảm bảo IME options được set
        if (getImeOptions() == EditorInfo.IME_NULL) {
            setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        // Tạo InputConnection wrapper để đảm bảo composition text được xử lý đúng
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if (inputConnection == null) {
            return null;
        }
        
        return new VietnameseInputConnection(inputConnection, true);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        
        // Cập nhật trạng thái composition
        boolean currentlyComposing = hasCompositionText();
        if (isComposing && !currentlyComposing) {
            // Composition vừa kết thúc
            isComposing = false;
            // Thông báo cho các listener sau một khoảng thời gian ngắn để đảm bảo text đã được commit
            handler.postDelayed(() -> {
                for (OnCompositionFinishedListener listener : compositionFinishedListeners) {
                    listener.onCompositionFinished();
                }
            }, 50);
        } else if (!isComposing && currentlyComposing) {
            // Composition vừa bắt đầu
            isComposing = true;
        }
    }

    /**
     * Thêm listener để nhận thông báo khi composition text kết thúc
     */
    public void addOnCompositionFinishedListener(OnCompositionFinishedListener listener) {
        if (listener != null && !compositionFinishedListeners.contains(listener)) {
            compositionFinishedListeners.add(listener);
        }
    }

    /**
     * Xóa listener
     */
    public void removeOnCompositionFinishedListener(OnCompositionFinishedListener listener) {
        compositionFinishedListeners.remove(listener);
    }

    /**
     * Kiểm tra xem có composition text đang diễn ra không
     */
    public boolean hasCompositionText() {
        Editable editable = getText();
        if (editable == null) {
            return false;
        }
        
        if (editable instanceof Spanned) {
            Spanned spanned = (Spanned) editable;
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
     * Kiểm tra xem có đang trong quá trình composition không
     */
    public boolean isComposing() {
        return isComposing || hasCompositionText();
    }

    /**
     * Custom InputConnection để đảm bảo composition text được xử lý đúng
     */
    private class VietnameseInputConnection extends InputConnectionWrapper {
        
        public VietnameseInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            // Đảm bảo composition text được commit đúng cách
            boolean result = super.commitText(text, newCursorPosition);
            return result;
        }

        @Override
        public boolean setComposingText(CharSequence text, int newCursorPosition) {
            // Đảm bảo composition text được set đúng cách
            isComposing = true;
            return super.setComposingText(text, newCursorPosition);
        }

        @Override
        public boolean finishComposingText() {
            // Đảm bảo composition text được finish đúng cách
            boolean result = super.finishComposingText();
            isComposing = false;
            // Thông báo cho các listener sau một khoảng thời gian ngắn
            handler.postDelayed(() -> {
                for (OnCompositionFinishedListener listener : compositionFinishedListeners) {
                    listener.onCompositionFinished();
                }
            }, 50);
            return result;
        }
    }
}

