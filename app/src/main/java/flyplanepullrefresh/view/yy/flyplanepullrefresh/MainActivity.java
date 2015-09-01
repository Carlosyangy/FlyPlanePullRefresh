package flyplanepullrefresh.view.yy.flyplanepullrefresh;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import flyplanepullrefresh.view.yy.flyplanepullrefresh.header.FlyPlaneHeaderView;
import flyplanepullrefresh.view.yy.flyplanepullrefresh.util.LocalDisplay;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPTRFrame();
    }

    private void initPTRFrame() {
        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        final PtrFrameLayout frame = (PtrFrameLayout) findViewById(R.id.ptrFrameLayout);

        // header
        final FlyPlaneHeaderView header = new FlyPlaneHeaderView(this);
        header.setLayoutParams(new PtrFrameLayout.LayoutParams(-1, -2));
        LocalDisplay.init(this);
        header.setPadding(0, LocalDisplay.dp2px(15), 0, LocalDisplay.dp2px(10));
        header.setUp(frame);

        frame.setLoadingMinTime(2000);
        frame.setDurationToCloseHeader(1500);
        frame.setHeaderView(header);
        frame.addPtrUIHandler(header);

        frame.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return true;
            }

            @Override
            public void onRefreshBegin(final PtrFrameLayout frame) {
                long delay = 4500;
                frame.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        frame.refreshComplete();
                    }
                }, delay);
            }
        });
    }
}
