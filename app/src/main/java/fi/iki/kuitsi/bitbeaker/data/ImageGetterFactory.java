package fi.iki.kuitsi.bitbeaker.data;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.Html;
import android.widget.TextView;

public interface ImageGetterFactory {
	@Nullable Html.ImageGetter create(Context context, TextView textView);
}
