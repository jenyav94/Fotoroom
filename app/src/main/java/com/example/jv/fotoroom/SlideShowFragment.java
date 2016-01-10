package com.example.jv.fotoroom;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yandex.disk.client.Credentials;
import com.yandex.disk.client.ListItem;
import com.yandex.disk.client.ProgressListener;
import com.yandex.disk.client.TransportClient;
import com.yandex.disk.client.exceptions.WebdavException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by JV on 08.01.2016.
 */
public class SlideShowFragment extends Fragment {

    public static final String TAG = "SlideShowFragment";

    private static final String ITEM_LIST = "SlideShowList.itemList";

    protected static final String CREDENTIALS = "SlideShow.credentials";

    private Credentials credentials;
    private ViewPager viewPager;
    private Handler handler;
    private int currentPage = 0;


    public static SlideShowFragment newInstance(Credentials credentials, List<ListItem> item) {
        SlideShowFragment fragment = new SlideShowFragment();

        Bundle args = new Bundle();
        args.putParcelable(CREDENTIALS, credentials);
        args.putParcelableArrayList(ITEM_LIST, new ArrayList<ListItem>(item));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        credentials = getArguments().getParcelable(CREDENTIALS);
        ArrayList<ListItem> itemList = getArguments().getParcelableArrayList(ITEM_LIST);

        handler = new Handler();

        DownloadFileClass downLoader = new DownloadFileClass();
        downLoader.loadFile(getActivity(), credentials, itemList);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.main, container, false);

        viewPager = (ViewPager) root.findViewById(R.id.pager);

        return root;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.example_action_bar, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void pagerSetAdapter(final ArrayList<File> fileList){
        final Handler handler = new Handler();

        viewPager.setAdapter(new MyAdapter(getChildFragmentManager(), fileList, credentials));

        currentPage = 0;
        final Runnable Update = new Runnable() {
            public void run() {

                //if (currentPage == fileList.size() - 1) {
                 //   currentPage = 0;
                //}
                viewPager.setCurrentItem(currentPage++, true);
            }
        };

        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                handler.post(Update);
            }
        }, 200, 2000);
    }


    public static class MyAdapter extends FragmentPagerAdapter {
        private ArrayList<File> fileAdapterList;
        private Credentials credentials;

        public MyAdapter(FragmentManager fm, ArrayList<File> fileList, Credentials credentials) {
            super(fm);
            fileAdapterList = fileList;
            this.credentials = credentials;
        }

        @Override
        public int getCount() {
            return fileAdapterList.size();
        }

        @Override
        public Fragment getItem(int position) {
            //Log.d(TAG, "size of list=" + fileAdapterList.size());
           //Log.d(TAG, "p osition=" + position);
            return PageFragment.newInstance(fileAdapterList.get(position));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Child Fragment " + position;
        }

    }

    class DownloadFileClass  implements ProgressListener {

        private ArrayList<File> fileList;
        private File result;
        private boolean cancelled;


        public void loadFile(final Context context, final Credentials credentials, final ArrayList<ListItem> itemList) {
            fileList = new ArrayList<>();
            final ImageValidator imageValid = new ImageValidator();

            new Thread(new Runnable() {
                @Override
                public void run () {
                    TransportClient client = null;
                    try {
                        client = TransportClient.getInstance(context, credentials);
                        for (int i = 0; i < itemList.size(); ++i) {
                            if (imageValid.validate(itemList.get(i).getDisplayName())) {
                                result = new File(context.getFilesDir(), new File(itemList.get(i).getFullPath()).getName());
                                client.downloadFile(itemList.get(i).getFullPath(), result, DownloadFileClass.this);
                                fileList.add(result);
                            }
                        }
                        downloadComplete();
                    } catch (IOException ex) {
                        Log.d(TAG, "loadFile", ex);
                        //sendException(ex);
                    } catch (WebdavException ex) {
                        Log.d(TAG, "loadFile", ex);
                        // sendException(ex);
                    } finally {
                        if (client != null) {
                            client.shutdown();
                        }
                    }
                }
            }).start();

        }

        @Override
        public void updateProgress (final long loaded, final long total) {
        }

        @Override
        public boolean hasCancelled () {
            return cancelled;
        }

        public void downloadComplete() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                   pagerSetAdapter(fileList);
                }
            });
        }

        public void cancelDownload() {
            cancelled = true;
        }
    }
}
