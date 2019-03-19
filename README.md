# FunEditText

最近需要做一个比较有趣的输入框效果，用原生的EditText不好实现，效果是这样的。

![](https://github.com/walker0402/FunEditText/blob/master/show/需要的效果.png)

这是一个数字的输入框，这种样式一般用于类似验证码之类的输入。我们看到底下的线是跟着每个输入的数字而不是一条完整的线。

这里我把每一个数字跟底部的线当成一个整体，也就是每个这样的整体，它是由一个**TextView + 底部横线**组成的。同时这个是个输入框，我们需要吊起软键盘并捕捉到用户输入什么，再赋值给我们的TextView显示出来。

用一个比较取巧的办法，就是我们定义一个宽高都为0的EditText，用来捕捉输入事件，而EditText监听输入字符，我们很容易联想到添加一个TextWatcher来监听EditText的字符输入，但是TextWatcher有一个问题，它监听的是键盘的输入，而不是键盘的点击（当然你可以根据输入情况来判断是点击的什么按键，这里主要是删除键的判断）。

###InputConnection 和 InputConnectionWrapper

我们知道EditText本身是可以监听键盘输入的，我们在EditText里面搜索一下有关输入Input的相关信息，最后可以发现在View下有这么一个方法

	 * Create a new InputConnection for an InputMethod to interact
     * with the view.  The default implementation returns null, since it doesn't
     * support input methods.  You can override this to implement such support.
     * This is only needed for views that take focus and text input.
	 public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

截取了一部分的注释，可以看到，这个方法实在view需要捕捉焦点并且获取文本输入的时候才重写并且返回一个InputConnection的。那这个InputConnection应该就是我们要找的能处理键盘点击跟文本输入的类了。

InputConnection是一个接口类，是键盘输入事件跟应用程序沟通的渠道，可以处理光标，文本输入，已经按键点击等事件


InputConnectionWrapper 是InputConnection的代理类，借助InputConnectionWrapper，我们可以重写InputConnection的一些方法，来做一些有趣的事情。

ok，现在我们新建一个类，继承InputConnectionWrapper，这里主要重写了三个方法，

1. sendKeyEvent(KeyEvent event),我需要捕捉删除按钮点击，
2. commitText(CharSequence text, int newCursorPosition)我需要捕捉文本的输入，
3. deleteSurroundingText(int beforeLength, int afterLength)我查资料的时候听说有的输入法在点击删除按钮时不会回调sendKeyEvent()但是会回调这个方法，我需要在这里手动调用一次sendKeyEvent(),**姑且信他一波**

所以整个代码是这样的



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


因为是只能输入数字，所以我在commitText时过滤了其他的输入文本，

再回到我们的EditText里，重写我们的onCreateInputConnection方法，把我们重写的FunInputConnection传进去

     @Override
        public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
            return new FunInputConnection(super.onCreateInputConnection(outAttrs), true);
        }

至此，拦截键盘点击跟输入，已经只能输入数字的处理已经完成了。
加个背景色，看看最后的效果

![](https://github.com/walker0402/FunEditText/blob/master/show/FunEditText.gif)
