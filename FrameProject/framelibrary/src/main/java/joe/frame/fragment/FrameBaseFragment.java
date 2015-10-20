package joe.frame.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.simple.eventbus.EventBus;

import java.lang.reflect.Field;

import joe.frame.activity.FrameBaseActivity;
import joe.frame.annotations.ViewInject;
import joe.frame.utils.KeyBoardUtils;
import joe.framelibrary.R;

/**
 * Description  框架基础Fragment
 * Created by chenqiao on 2015/7/16.
 */
public abstract class FrameBaseFragment extends Fragment {

    protected FrameBaseActivity context;
    private FrameLayout frameLayout;
    private View contentView;
    private FragmentManager fragmentManager;
    private boolean isRegisterEventBus = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.frame_fragment_layout, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        frameLayout = (FrameLayout) view.findViewById(R.id.rootlayout_basefragment);
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = (FrameBaseActivity) getActivity();
        fragmentManager = getFragmentManager();
        onBaseFragmentCreate(savedInstanceState);
    }

    /**
     * Fragment创建
     *
     * @param savedInstanceState
     * @author chenqiao
     */
    protected abstract void onBaseFragmentCreate(Bundle savedInstanceState);

    /**
     * 注册EventBus
     */
    protected void registerEventBus() {
        EventBus.getDefault().register(this);
        isRegisterEventBus = true;
    }

    protected void registerEventBusForSticky() {
        EventBus.getDefault().registerSticky(this);
        isRegisterEventBus = true;
    }

    /**
     * 重写onDestroy，如果注册了EventBus，则需要注销
     */
    @Override
    public void onDestroy() {
        if (isRegisterEventBus) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    protected FrameLayout getRootLayout() {
        return frameLayout;
    }

    /**
     * 获取Fragment显示的View
     *
     * @return
     */
    public View getContentView() {
        return contentView;
    }

    /**
     * 设置内容
     *
     * @param resID
     */
    protected void setMyContentView(int resID) {
        frameLayout.removeAllViews();
        contentView = LayoutInflater.from(context).inflate(resID, frameLayout);
        autoInjectViewField();
    }

    protected void setMyContentView(View view) {
        contentView = view;
        frameLayout.removeAllViews();
        frameLayout.addView(view);
        autoInjectViewField();
    }

    /**
     * 解析注解，给带有@ViewInject注解的View赋值
     */
    private void autoInjectViewField() {
        try {
            Class<?> clazz = this.getClass();
            Field[] fields = clazz.getDeclaredFields();//获得Fragment中声明的字段
            for (Field field : fields) {
                // 查看这个字段是否有我们自定义的注解类标志的
                if (field.isAnnotationPresent(ViewInject.class)) {
                    ViewInject inject = field.getAnnotation(ViewInject.class);
                    int id = inject.value();
                    if (id > 0) {
                        field.setAccessible(true);
                        field.set(this, frameLayout.findViewById(id));//给我们要找的字段设置值
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换Activity的内容
     *
     * @param fragment
     * @param isBackStack
     */
    protected void replaceFragment(FrameBaseFragment fragment, String isBackStack) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (TextUtils.isEmpty(isBackStack)) {
            fragmentTransaction.replace(context.getRootFrameLayoutId(), fragment);
        } else {
            fragmentTransaction.replace(context.getRootFrameLayoutId(), fragment, isBackStack);
            fragmentTransaction.addToBackStack(isBackStack);
        }
        KeyBoardUtils.closeKeyboard(context);
        fragmentTransaction.commitAllowingStateLoss();
    }

    /**
     * 在{@link #setMyContentView(int) or #setMyContentView(View)}之后获取其中View
     *
     * @param resId
     * @return
     */
    protected View findViewById(int resId) {
        return frameLayout.findViewById(resId);
    }

    /**
     * 结束当前fragment
     */
    protected void finish() {
        /**
         * 如果当前fragment不是根fragment
         */
        if (fragmentManager.getBackStackEntryCount() > 1) {
            fragmentManager.popBackStack();
        }
    }
}
