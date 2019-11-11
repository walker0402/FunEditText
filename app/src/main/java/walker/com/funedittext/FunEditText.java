package walker.com.funedittext;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * author 刘鉴钊
 * create at 2019/3/14
 * description
 */
public class FunEditText extends LinearLayout implements View.OnClickListener {

    /**
     * 默认数量
     */
    private static final int DEFAULT_NUMBER = 5;

    /**
     * 需要输入的个数
     */
    private int number;
    /**
     * 控件宽
     */
    private int width;

    /**
     * 字体颜色
     */
    private int textColor;

    /**
     * 横线颜色
     */
    private int lineColor;

    private int itemBackGround;
    /**
     * 横线宽
     */
    private int lineWidth;

    /**
     * 横线高，粗细
     */
    private int lineHeight;


    private int textSize;

    /**
     * 两个item的间隔
     */
    private int interval;

    /**
     * 存储TextView的集合
     */
    private TextView[] tvArray;

    /**
     * 存储输入数字的集合
     */
    private LinkedList<String> numArray;

    private InvisibilityEditText invisibilityEditText;

    private OnTextChangeListener textChangeListener;

    public void setTextChangeListener(OnTextChangeListener listener) {
        this.textChangeListener = listener;
    }

    public FunEditText(Context context) {
        this(context, null);
    }

    public FunEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FunEditText);
        number = typedArray.getInt(R.styleable.FunEditText_number, DEFAULT_NUMBER);
        lineWidth = typedArray.getDimensionPixelOffset(R.styleable.FunEditText_line_width, 0);
        textSize = typedArray.getDimensionPixelOffset(R.styleable.FunEditText_text_size, 14);
        interval = typedArray.getDimensionPixelOffset(R.styleable.FunEditText_interval, 10);
        lineHeight = typedArray.getDimensionPixelOffset(R.styleable.FunEditText_line_height, 1);
        textColor = typedArray.getColor(R.styleable.FunEditText_text_color, Color.BLACK);
        lineColor = typedArray.getColor(R.styleable.FunEditText_line_color, Color.BLACK);
        itemBackGround = typedArray.getColor(R.styleable.FunEditText_item_background, Color.TRANSPARENT);
        typedArray.recycle();
        init();
    }

    private void init() {
        addViews();
    }

    /**
     * 添加控件
     */
    private void addViews() {
        tvArray = new TextView[number];
        numArray = new LinkedList<>();

        invisibilityEditText = new InvisibilityEditText(getContext());
        LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(0, 0);
        invisibilityEditText.setLayoutParams(etParams);
        invisibilityEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        invisibilityEditText.setOnInputListener(new OnInputListener() {
            @Override
            public void onTextInput(CharSequence text) {
                if (numArray.size() < number) {
                    numArray.add(text.toString());
                    setInputText(numArray.getLast());
                    if (null != textChangeListener) {
                        textChangeListener.onTextChanged(getText());
                        if (numArray.size() == number) {
                            textChangeListener.onInputFinish();
                        }
                    }
                }
            }

            @Override
            public void onDelete() {
                if (numArray.size() > 0) {
                    numArray.removeLast();
                    deleteLastText();
                    if (null != textChangeListener) {
                        textChangeListener.onTextChanged(getText());
                    }
                }
            }
        });

        for (int i = 0; i < number; i++) {
            LinearLayout itemLayout = new LinearLayout(getContext());
            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(lineWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i != number - 1) {
                itemParams.rightMargin = interval;
            }
            itemLayout.setLayoutParams(itemParams);
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            itemLayout.setBackgroundColor(itemBackGround);
            //显示文本
            TextView tv = new TextView(getContext());
            LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvParams.gravity = Gravity.CENTER;
            tv.setLayoutParams(tvParams);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            tv.setTextColor(textColor);

            //底部横线
            View line = new View(getContext());
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(lineWidth, lineHeight);
            line.setBackgroundColor(Color.parseColor("#000000"));
            line.setLayoutParams(lineParams);
            line.setBackgroundColor(lineColor);
            tvArray[i] = tv;
            itemLayout.addView(tv);
            itemLayout.addView(line);

            addView(itemLayout);
        }
        addView(invisibilityEditText);

        setOnClickListener(this);
    }


    /**
     * 获取当前已经输入的数字
     *
     * @return
     */
    private String getText() {
        StringBuilder sb = new StringBuilder();
        LinkedList<String> clone = (LinkedList<String>) numArray.clone();
        while (!clone.isEmpty()) {
            sb.append(clone.pop());
        }
        return sb.toString();
    }

    /**
     * 用户输入时设置显示数字
     */
    private void setInputText(String text) {
        tvArray[getTextLength() - 1].setText(text);
    }

    /**
     * 删除显示的最后一个数字
     */
    private void deleteLastText() {
        tvArray[getTextLength()].setText("");
    }

    /**
     * 获取已经输入数字的个数
     *
     * @return numArray.size()
     */
    private int getTextLength() {
        return numArray.size();
    }

    @Override
    public void onClick(View v) {
        invisibilityEditText.setFocusable(true);
        invisibilityEditText.setFocusableInTouchMode(true);
        invisibilityEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(invisibilityEditText, InputMethodManager.SHOW_IMPLICIT);
    }


    /**
     * 隐藏的内部EditText，用于捕捉用户输入文本
     */
    public static class InvisibilityEditText extends android.support.v7.widget.AppCompatEditText {

        private OnInputListener listener;

        public void setOnInputListener(OnInputListener textChangeListener) {
            this.listener = textChangeListener;
        }

        public InvisibilityEditText(Context context) {
            super(context);
        }

        @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            return new FunInputConnection(super.onCreateInputConnection(outAttrs), true);
        }

        public class FunInputConnection extends InputConnectionWrapper {

            String[] digits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

            /**
             * Initializes a wrapper.
             *
             * <p><b>Caveat:</b> Although the system can accept {@code (InputConnection) null} in some
             * places, you cannot emulate such a behavior by non-null {@link InputConnectionWrapper} that
             * has {@code null} in {@code target}.</p>
             *
             * @param target  the {@link InputConnection} to be proxied.
             * @param mutable set {@code true} to protect this object from being reconfigured to target
             *                another {@link InputConnection}.  Note that this is ignored while the target is {@code null}.
             */
            public FunInputConnection(InputConnection target, boolean mutable) {
                super(target, mutable);
            }

            @Override
            public boolean sendKeyEvent(KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                    if (null != listener) {
                        listener.onDelete();
                        return false;
                    }
                }else if (event.getAction() == KeyEvent.ACTION_DOWN
                        && (event.getKeyCode() == KeyEvent.KEYCODE_0 || event.getKeyCode() == KeyEvent.KEYCODE_1 || event.getKeyCode() == KeyEvent.KEYCODE_2 || event.getKeyCode() == KeyEvent.KEYCODE_3 || event.getKeyCode() == KeyEvent.KEYCODE_4 || event.getKeyCode() == KeyEvent.KEYCODE_5
                        || event.getKeyCode() == KeyEvent.KEYCODE_6 || event.getKeyCode() == KeyEvent.KEYCODE_7 || event.getKeyCode() == KeyEvent.KEYCODE_8 || event.getKeyCode() == KeyEvent.KEYCODE_9)) {
                    //google输入法点击数字键也是调用sendKeyEvent而不调用commitText,这里主动转发一次
                    commitText(event.getKeyCode() - 7+"", 1);
                    return false;
                }
                return super.sendKeyEvent(event);
            }

            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                if (null != listener && Arrays.asList(digits).contains(text.toString())) {
                    listener.onTextInput(text);
                }
                return super.commitText(text, newCursorPosition);
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                if (beforeLength == 1 && afterLength == 0) {
                    return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL)) && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
                }
                return super.deleteSurroundingText(beforeLength, afterLength);
            }
        }
    }

    private interface OnInputListener {
        void onTextInput(CharSequence text);

        void onDelete();
    }

    public interface OnTextChangeListener {
        void onTextChanged(CharSequence text);

        void onInputFinish();
    }

}
