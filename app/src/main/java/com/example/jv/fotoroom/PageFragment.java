/*
 * Лицензионное соглашение на использование набора средств разработки
 * «SDK Яндекс.Диска» доступно по адресу: http://legal.yandex.ru/sdk_agreement
 *
 */

package com.example.jv.fotoroom;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

public class PageFragment  extends IODialogFragment{

    private static final String FILE_ITEM = "example.file.item";

    private Uri ImageFile;
    private ImageView image;


    public static PageFragment newInstance(File item) {
        PageFragment fragment = new PageFragment();

        Bundle args = new Bundle();
        args.putParcelable(FILE_ITEM, Uri.fromFile(item));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ImageFile = getArguments().getParcelable(FILE_ITEM);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment, null);
        image = (ImageView) view.findViewById(R.id.tvPage);
        image.setImageURI(ImageFile);

        return view;
    }




}
